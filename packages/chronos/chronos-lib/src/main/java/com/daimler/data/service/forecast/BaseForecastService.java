package com.daimler.data.service.forecast;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.daimler.data.dto.databricks.DataBricksJobRunOutputResponseWrapperDto;
import com.daimler.data.dto.forecast.*;
import com.daimler.data.dto.storage.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.daimler.data.application.client.DataBricksClient;
import com.daimler.data.application.client.StorageServicesClient;
import com.daimler.data.assembler.ForecastAssembler;
import com.daimler.data.auth.vault.VaultAuthClientImpl;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.ForecastNsql;
import com.daimler.data.db.json.RunDetails;
import com.daimler.data.db.json.RunState;
import com.daimler.data.db.json.UserDetails;
import com.daimler.data.db.repo.forecast.ForecastCustomRepository;
import com.daimler.data.db.repo.forecast.ForecastRepository;
import com.daimler.data.dto.databricks.RunNowNotebookParamsDto;
import com.daimler.data.dto.forecast.RunStateVO.ResultStateEnum;
import com.daimler.data.service.common.BaseCommonService;
import com.google.gson.JsonArray;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BaseForecastService extends BaseCommonService<ForecastVO, ForecastNsql, String> implements ForecastService{

	@Value("${databricks.jobId}")
	private String dataBricksJobId;

	@Value("${databricks.powerfulMachinesJobId}")
	private String dataBricksPowerfulMachinesJobId;
	
	@Value("${databricks.defaultConfigYml}")
	private String dataBricksJobDefaultConfigYml;
	
	@Autowired
	private StorageServicesClient storageClient;
	
	@Autowired
	private DataBricksClient dataBricksClient;
	
	@Autowired
	private ForecastCustomRepository customRepo;
	@Autowired
	private ForecastRepository jpaRepo;
	
	@Autowired
	private ForecastAssembler assembler;

	@Lazy
	@Autowired
	private VaultAuthClientImpl vaultAuthClient;

	public BaseForecastService() {
		super();
	}

	@Override
	public List<ForecastVO> getAll( int limit,  int offset, String user) {
		List<ForecastNsql> entities = customRepo.getAll(user, offset, limit);
		if (entities != null && !entities.isEmpty())
			return entities.stream().map(n -> assembler.toVo(n)).collect(Collectors.toList());
		else
			return new ArrayList<>();
	}

	@Override
	public Long getCount(String user) {
		return customRepo.getTotalCount(user);
	}

	@Override
	@Transactional
	public ForecastVO createForecast(ForecastVO vo) throws Exception {
		CreateBucketResponseWrapperDto bucketCreationResponse = storageClient.createBucket(vo.getBucketName(),vo.getCreatedBy(),vo.getCollaborators());
		if(bucketCreationResponse!= null && "SUCCESS".equalsIgnoreCase(bucketCreationResponse.getStatus())) {
			// To store data on minio once bucket is created.
			vo.setBucketId(bucketCreationResponse.getData().getId());
			new ForecastVO();
			ForecastVO forecastVO = super.create(vo);
			return forecastVO;
		}else {
			throw new Exception("Failed while creating bucket for Forecast project artifacts to be stored.");
		}
	}
	
	@Override
	@Transactional
	public ForecastRunResponseVO createJobRun(MultipartFile file,String savedInputPath, Boolean saveRequestPart, String runName,
			String configurationFile, String frequency, BigDecimal forecastHorizon, String hierarchy, String comment, Boolean runOnPowerfulMachines,
			ForecastVO existingForecast,String triggeredBy, Date triggeredOn) {
		
		String dataBricksJobidForRun = dataBricksJobId;
		ForecastRunResponseVO responseWrapper = new ForecastRunResponseVO();
		RunNowResponseVO runNowResponseVO = new RunNowResponseVO();
		GenericMessage responseMessage = new GenericMessage();
		RunNowNotebookParamsDto noteboookParams = new RunNowNotebookParamsDto();
		String correlationId = UUID.randomUUID().toString();
		String bucketName = existingForecast.getBucketName();
		String resultFolder = bucketName+"/results/"+correlationId + "-" + runName;
		String inputOrginalFolder= "/results/"+correlationId + "-" + runName + "/input_original";

		FileUploadResponseDto fileUploadResponse = this.saveFile(inputOrginalFolder,file, existingForecast.getBucketName());
		if(fileUploadResponse==null || (fileUploadResponse!=null && (fileUploadResponse.getErrors()!=null || !"SUCCESS".equalsIgnoreCase(fileUploadResponse.getStatus())))) {

			log.error("Error in uploading file to {} for forecast project {}",inputOrginalFolder,existingForecast.getName() );
			MessageDescription msg = new MessageDescription("Failed to  upload file to " + inputOrginalFolder + "for" + existingForecast.getName() );
			List<MessageDescription> errors = new ArrayList<>();
			errors.add(msg);
			responseMessage.setErrors(errors);
			responseWrapper.setData(null);
			responseWrapper.setResponse(responseMessage);

		return responseWrapper;

		}
		noteboookParams.setConfig(configurationFile);
		noteboookParams.setCorrelationId(correlationId);
		if(savedInputPath!=null) {
			if(savedInputPath.toLowerCase().contains(".xlsx")){
				noteboookParams.setExcel(savedInputPath);
				noteboookParams.setY("");
			}else {
				if(savedInputPath.toLowerCase().contains(".csv")){
					noteboookParams.setExcel("");
					noteboookParams.setY(savedInputPath);
				}
			}
		}
		noteboookParams.setFh(forecastHorizon.toString());
		noteboookParams.setHierarchy(hierarchy);
		noteboookParams.setFreq(this.toFrequencyParam(frequency));

		noteboookParams.setResults_folder(resultFolder);
		noteboookParams.setX("");
		noteboookParams.setX_pred("");

		RunNowResponseVO runNowResponse = dataBricksClient.runNow(correlationId, noteboookParams, runOnPowerfulMachines);
		if(runNowResponse!=null) {
			if(runNowResponse.getErrorCode()!=null || runNowResponse.getRunId()==null) 
				responseMessage.setSuccess("FAILED");
			else {
				responseMessage.setSuccess("SUCCESS");
				runNowResponse.setCorrelationId(correlationId);
				ForecastNsql entity = this.assembler.toEntity(existingForecast);
				List<RunDetails> existingRuns = entity.getData().getRuns();
				if(existingRuns==null || existingRuns.isEmpty())
					existingRuns = new ArrayList<>();
				RunDetails currentRun = new RunDetails();
				currentRun.setComment(comment);
				currentRun.setConfigurationFile(configurationFile);
				currentRun.setForecastHorizon(forecastHorizon.toString());
				currentRun.setHierarchy(hierarchy.toString());
				currentRun.setFrequency(frequency);
				currentRun.setId(correlationId);
				currentRun.setInputFile(savedInputPath);
				currentRun.setIsDelete(false);
				if (runOnPowerfulMachines) {
					dataBricksJobidForRun = dataBricksPowerfulMachinesJobId;
				} 
				currentRun.setJobId(dataBricksJobidForRun);
				currentRun.setNumberInJob(runNowResponse.getNumberInJob());
				currentRun.setRunId(runNowResponse.getRunId());
				currentRun.setRunName(runName);
				currentRun.setTriggeredBy(triggeredBy);
				currentRun.setTriggeredOn(triggeredOn);
				currentRun.setIsDelete(false);
				RunState newRunState = new RunState();
				newRunState.setLife_cycle_state("PENDING");
				newRunState.setUser_cancelled_or_timedout(false);
				currentRun.setRunState(newRunState);
				currentRun.setResultFolderPath(resultFolder);
				runNowResponse.setResultFolderPath(resultFolder);;
				existingRuns.add(currentRun);
				entity.getData().setRuns(existingRuns);
				try {
					this.jpaRepo.save(entity);
				}catch(Exception e) {
					log.error("Failed while saving details of run {} and correaltionId {} to database for project {}",runNowResponse.getRunId(),correlationId
							, existingForecast.getName());
					MessageDescription msg = new MessageDescription("Failed to save run details to table after creating databricks job run with runid "+runNowResponse.getRunId());
					List<MessageDescription> errors = new ArrayList<>();
					errors.add(msg);
					responseMessage.setErrors(errors);
				}
			}
			responseWrapper.setData(runNowResponse);
			responseWrapper.setResponse(responseMessage);
		}
		return responseWrapper;
	}
	
	private String toFrequencyParam(String value) {
		switch(value) {
		case "Daily" : return "D";
		case "Weekly" : return "W";
		case "Monthly" : return "M";
		case "Yearly" : return "Y";
		default: return "";
		}
	}

	@Override
	public FileUploadResponseDto saveFile(String prefix, MultipartFile file, String bucketName) {
		FileUploadResponseDto uploadResponse = storageClient.uploadFile(prefix,file,bucketName);
		return uploadResponse;
	}

	@Override
	public Long getRunsCount(String id) {
		return customRepo.getTotalRunsCount(id);
	}

	@Override
	@Transactional
	public List<RunVO> getAllRunsForProject(int limit, int offset, ForecastVO existingForecast) {
		List<RunDetails> updatedRuns = new ArrayList<>();
		List<RunVO> updatedRunVOList = new ArrayList<>();

		Optional<ForecastNsql> entityOptional = jpaRepo.findById(existingForecast.getId());
		if(entityOptional!=null) {
			ForecastNsql entity = entityOptional.get();
			if(entity!=null && entity.getData()!=null &&
					entity.getData().getRuns()!=null && !entity.getData().getRuns().isEmpty()) {
				List<RunDetails> existingRuns = entity.getData().getRuns();
				String bucketName = entity.getData().getBucketName();
				String resultsPrefix = "results/";
				for(RunDetails run: existingRuns) {
					RunState state = run.getRunState();
					String runId = run.getRunId();
					String correlationId= run.getId();
					if(runId!=null && (run.getIsDelete() == null || !run.getIsDelete()) &&
							(state==null || state.getResult_state()==null || state.getLife_cycle_state()==null ||
									"PENDING".equalsIgnoreCase(state.getLife_cycle_state()) ||
									"RUNNING".equalsIgnoreCase(state.getLife_cycle_state()))) {
						RunDetailsVO updatedRunResponse = this.dataBricksClient.getSingleRun(runId);
						if(updatedRunResponse!=null && runId.equals(updatedRunResponse.getRunId())) {
							log.info("Able to fetch updated run details for forecast {} and correlation {} which was in {}", existingForecast.getId(),correlationId,state.getLife_cycle_state());
							RunDetails updatedRunDetail = new RunDetails();
							BeanUtils.copyProperties(run, updatedRunDetail);
							updatedRunDetail.setCreatorUserName(updatedRunResponse.getCreatorUserName());
							if(updatedRunResponse.getEndTime()!=null)
								updatedRunDetail.setEndTime(updatedRunResponse.getEndTime().longValue());
							if(updatedRunResponse.getExecutionDuration()!=null)
								updatedRunDetail.setExecutionDuration(updatedRunResponse.getExecutionDuration().longValue());
							if(updatedRunResponse.getSetupDuration()!=null)
								updatedRunDetail.setSetupDuration(updatedRunResponse.getSetupDuration().longValue());
							if(updatedRunResponse.getStartTime()!=null)
								updatedRunDetail.setStartTime(updatedRunResponse.getStartTime().longValue());
							if(updatedRunResponse.getState()!=null) {
								RunStateVO updatedState = updatedRunResponse.getState();
								RunState newState = new RunState();
								if(updatedState.getLifeCycleState()!=null)
									newState.setLife_cycle_state(updatedState.getLifeCycleState().name());
								if(updatedState.getResultState()!=null) {
									newState.setResult_state(updatedState.getResultState().name());
									if("SUCCESS".equalsIgnoreCase(updatedState.getResultState().name())) {
										//check if .SUCCESS file exists
										String resultFolderPathForRun = resultsPrefix + updatedRunDetail.getId()+"-"+updatedRunDetail.getRunName()+"/";
										List<BucketObjectDetailsDto> bucketObjectDetails=storageClient.getFilesPresent(bucketName,resultFolderPathForRun);
										Boolean successFileFlag = storageClient.isFilePresent(resultFolderPathForRun+ "SUCCESS", bucketObjectDetails);
										Boolean warningsFileFlag = storageClient.isFilePresent(resultFolderPathForRun+ "WARNINGS.txt", bucketObjectDetails);
										log.info("Run state is success from databricks and successFileFlag value is {} and warningsFileFlag is {} , for bucket {} and prefix {} ", successFileFlag, warningsFileFlag, bucketName, resultFolderPathForRun);
										if(warningsFileFlag){
											newState.setResult_state(ResultStateEnum.WARNINGS.name());
											//fetch file content from warnings.txt file
											String commonPrefix = "/results/"+run.getId() + "-" + run.getRunName();
											String warningsPrefix = commonPrefix +"/WARNINGS.txt";
											String warningsResult = "";
											FileDownloadResponseDto warningsTextDownloadResponse = storageClient.getFileContents(bucketName, warningsPrefix);
											if(warningsTextDownloadResponse!= null && warningsTextDownloadResponse.getData()!=null && (warningsTextDownloadResponse.getErrors()==null || warningsTextDownloadResponse.getErrors().isEmpty())) {
												warningsResult = new String(warningsTextDownloadResponse.getData().getByteArray());
												log.info("successfully retrieved warnings.txt file contents for forecast {} and correaltionid{} and runname{}",
														bucketName, correlationId, run.getRunName());
											}
											updatedRunDetail.setWarnings(warningsResult);
										}
										else{
											if(!successFileFlag) {
												newState.setResult_state(ResultStateEnum.FAILED.name());
											}
										}
									}else {
										String taskRunId=updatedRunResponse.getTasks().get(0).getRunId();
										String errorMessage=processErrorMessages(taskRunId);
										updatedRunDetail.setError(errorMessage);
										updatedRunDetail.setTaskRunId(taskRunId);
									}
								}
								String updatedStateMsg = "";
								if(updatedRunResponse.getState().getStateMessage()!=null) {
									updatedStateMsg = updatedState.getStateMessage();
								}
								newState.setState_message(updatedStateMsg);
								newState.setUser_cancelled_or_timedout(updatedState.isUserCancelledOrTimedout());
								updatedRunDetail.setRunState(newState);

							}
							updatedRuns.add(updatedRunDetail);

						}else {
							updatedRuns.add(run);
						}
					}
					else {
						RunDetails updatedRunDetail = new RunDetails();
						if (runId != null && (run.getIsDelete() == null || !run.getIsDelete()) && (state != null ||
								"TERMINATED".equalsIgnoreCase(state.getLife_cycle_state()) ||
								"INTERNAL_ERROR".equalsIgnoreCase(state.getLife_cycle_state()) ||
								"SKIPPED".equalsIgnoreCase(state.getLife_cycle_state())) &&
								!"SUCCESS".equalsIgnoreCase(state.getResult_state()) && run.getError()!=null
						){
							RunDetailsVO updatedRunResponse = this.dataBricksClient.getSingleRun(runId);
							if(updatedRunResponse!=null && runId.equals(updatedRunResponse.getRunId())) {
								log.info(" Updating error msg for old run {} of forecast project {}", run.getRunName(), bucketName);
								BeanUtils.copyProperties(run, updatedRunDetail);
								String taskRunId=updatedRunResponse.getTasks().get(0).getRunId();
								String errorMessage=processErrorMessages(taskRunId);
								updatedRunDetail.setError(errorMessage);
								updatedRunDetail.setTaskRunId(taskRunId);
								updatedRuns.add(updatedRunDetail);
							}
							else {
								updatedRuns.add(run);
							}
						} else {
							log.info("Updating results for success for terminated lifeCycleState and Success resultState");
							updatedRuns.add(run);
						}
					}
				}
				entity.getData().setRuns(updatedRuns);
				this.jpaRepo.save(entity);
				updatedRunVOList = this.assembler.toRunsVO(updatedRuns);
			}

		}
		return updatedRunVOList;
	}


	private String processErrorMessages(String taskRunId) {
		DataBricksJobRunOutputResponseWrapperDto updatedRunOutputResponse = this.dataBricksClient.getSingleRunOutput(taskRunId);
		String errMessage=null;
		if(updatedRunOutputResponse!=null){
			if(updatedRunOutputResponse.getError()!=null && !"".equalsIgnoreCase(updatedRunOutputResponse.getError())){
				errMessage=updatedRunOutputResponse.getError();
			}
			else {
				errMessage = updatedRunOutputResponse.getMetadata().getState().getStateMessage();
			}
		}

		return errMessage;
	}

	@Override
	public RunVisualizationVO getRunVisualizationsByUUID(String id, String rid) {
		RunVisualizationVO visualizationVO = new RunVisualizationVO();
		GenericMessage responseMessage = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		Optional<ForecastNsql> entityOptional = jpaRepo.findById(id);
		ForecastNsql entity = entityOptional.get();
		String bucketName = entity.getData().getBucketName();
		Optional<RunDetails>  requestedRun = entity.getData().getRuns().stream().filter(x -> rid.equalsIgnoreCase(x.getId())).findFirst();
		RunDetails run = requestedRun.get();
		visualizationVO.setForecastHorizon(run.getForecastHorizon());
		visualizationVO.setHierarchy(run.getHierarchy());
		visualizationVO.setFrequency(run.getFrequency());
		visualizationVO.setId(run.getId());
		visualizationVO.setRunId(run.getRunId());
		visualizationVO.setRunName(run.getRunName());
		RunState state = run.getRunState();
		if(state!=null) {
			if(!(state.getResult_state()!=null && "SUCCESS".equalsIgnoreCase(state.getResult_state()))) {
				visualizationVO.setEda("");
				visualizationVO.setY("");
				visualizationVO.setYPred("");
				return visualizationVO;
			}
		}
		String commonPrefix = "/results/"+rid + "-" + run.getRunName();
		try {
			String yPrefix = commonPrefix +"/y.csv";
			String yPredPrefix = commonPrefix +"/y_pred.csv";
			String edaJsonPrefix = commonPrefix +"/eda.json";
			FileDownloadResponseDto yDownloadResponse = storageClient.getFileContents(bucketName, yPrefix);
			FileDownloadResponseDto yPredDownloadResponse = storageClient.getFileContents(bucketName, yPredPrefix);
			FileDownloadResponseDto edaJsonDownloadResponse = storageClient.getFileContents(bucketName, edaJsonPrefix);
			JsonArray jsonArray = new JsonArray();
			String yResult = "";
			String yPredResult = "";
			String edaResult = "";
			if(yDownloadResponse!= null && yDownloadResponse.getData()!=null && (yDownloadResponse.getErrors()==null || yDownloadResponse.getErrors().isEmpty())) {
				 yResult = new String(yDownloadResponse.getData().getByteArray()); 
			 }
			if(yPredDownloadResponse!= null && yPredDownloadResponse.getData()!=null && (yPredDownloadResponse.getErrors()==null || yPredDownloadResponse.getErrors().isEmpty())) {
				  yPredResult = new String(yPredDownloadResponse.getData().getByteArray()); 
			 }
			if(edaJsonDownloadResponse!= null && edaJsonDownloadResponse.getData()!=null && (edaJsonDownloadResponse.getErrors()==null || edaJsonDownloadResponse.getErrors().isEmpty())) {
				edaResult = new String(edaJsonDownloadResponse.getData().getByteArray()); 
			 }
			visualizationVO.setEda(edaResult);
			visualizationVO.setY(yResult);
			visualizationVO.setYPred(yPredResult);
		}catch(Exception e) {
			log.error("Failed while parsing results data for run rid {} with exception {} ",rid, e.getMessage());
		}
		return visualizationVO;
	}

	@Override
	public GenericMessage generateApiKey(String id) {
		GenericMessage responseMessage = new GenericMessage();
        List<MessageDescription> errors = new ArrayList<>();
        Optional<ForecastNsql> entityOptional = jpaRepo.findById(id);
		if (entityOptional != null) {
			try {
				ForecastNsql entity = entityOptional.get();
			
				String apiKey = UUID.randomUUID().toString();
				if (apiKey != null && id != null) {
					GenericMessage createApiKeyResponseMessage = vaultAuthClient.updateApiKey(id, apiKey);
						if (createApiKeyResponseMessage != null && "FAILED".equalsIgnoreCase(createApiKeyResponseMessage.getSuccess())) {
						throw new Exception("Failed to generate an Api key");
					}
				}
				responseMessage.setSuccess("SUCCESS");
			} catch(Exception e) {
				log.error("Failed to generate an API key for " + id);
				MessageDescription msg = new MessageDescription("Failed to generate an API key for " + id);
				errors.add(msg);
				responseMessage.setSuccess("FAILED");
				responseMessage.setErrors(errors);
				return responseMessage;
			}
		}
		return responseMessage;
	}

	@Override
	public ApiKeyVO getApiKey(String id) {
		GenericMessage responseMessage = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		ApiKeyVO response = new ApiKeyVO();
		Optional<ForecastNsql> entityOptional = jpaRepo.findById(id);
		if (entityOptional != null) {
			try {
				ForecastNsql entity = entityOptional.get();

				String apiKey = vaultAuthClient.getApiKeys(id);
				if (apiKey == null) {
					throw new Exception("Failed to get an Api key for " + id);
				}
				response.setApiKey(apiKey);
				responseMessage.setSuccess("SUCCESS");
			} catch(Exception e) {
				log.error("Failed to get an API key for " + id);
				MessageDescription msg = new MessageDescription("Failed to get an API key for " + id);
				errors.add(msg);
				responseMessage.setSuccess("FAILED");
				responseMessage.setErrors(errors);
				return response;
			}
		}
		return response;
	}

	@Override
	public GenericMessage updateForecastByID(String id, ForecastProjectUpdateRequestVO forecastUpdateRequestVO,
			ForecastVO existingForecast) {
		GenericMessage responseMessage = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		Optional<ForecastNsql> entityOptional = jpaRepo.findById(id);

		if (entityOptional != null) {
			try {
				ForecastNsql entity = entityOptional.get();
				List<UserDetails> exstingcollaborators = entity.getData().getCollaborators();

				List<UserDetails> addCollabrators = forecastUpdateRequestVO.getAddCollaborators().stream().map(n -> {
					UserDetails collaborator = new UserDetails();
					BeanUtils.copyProperties(n, collaborator);
					return collaborator;
				}).collect(Collectors.toList());

				List<UserDetails> removeCollabrators = forecastUpdateRequestVO.getRemoveCollaborators().stream()
						.map(n -> {
							UserDetails collaborator = new UserDetails();
							BeanUtils.copyProperties(n, collaborator);
							return collaborator;
						}).collect(Collectors.toList());

				if (exstingcollaborators != null) {
					exstingcollaborators.addAll(addCollabrators);
				} else {
					exstingcollaborators = addCollabrators;
				}

				// To remove collaborators from existing collaborators.
				for (UserDetails user : removeCollabrators) {
					UserDetails userToRemove = null;
					for (UserDetails usr : exstingcollaborators) {
						if (usr.getId().equals(user.getId())) {
							userToRemove = usr;
							break;
						}
					}

					if (userToRemove != null) {
						exstingcollaborators.remove(userToRemove);
					} else {
						MessageDescription msg = new MessageDescription("User ID not found for deleting " + user.getId());
						responseMessage.setSuccess("FAILED");
						errors.add(msg);
						responseMessage.setErrors(errors);
						log.error("User ID not found for deleting" + user.getId());
						return responseMessage;
					}
				}

				entity.getData().setCollaborators(exstingcollaborators);
				List<CollaboratorVO> addCollabratorsList = exstingcollaborators.stream().map(n -> {
					CollaboratorVO collaborator = new CollaboratorVO();
					BeanUtils.copyProperties(n, collaborator);
					return collaborator;
				}).collect(Collectors.toList());

				if (entity.getData().getBucketId() != null) {
					UpdateBucketResponseWrapperDto updateBucketResponse = storageClient.updateBucket(entity.getData().getBucketName(), entity.getData().getBucketId(), existingForecast.getCreatedBy(), addCollabratorsList);
					if (updateBucketResponse.getErrors() != null) {
						log.error("Failed while saving details of collaborator {} Caused due to Exception {}", existingForecast.getName(), updateBucketResponse.getErrors().get(0).getMessage());
						MessageDescription msg = new MessageDescription("Failed to save collaborator details.");
						errors.add(msg);
						responseMessage.setSuccess("FAILED");
						responseMessage.setErrors(errors);
						return responseMessage;
					}
				} else {
					GetBucketByNameResponseWrapperDto getBucketBynameResponse = storageClient.getBucketDetailsByName(entity.getData().getBucketName());
					if (getBucketBynameResponse != null && getBucketBynameResponse.getId() != null) {
						// setting bucket Id for the entity.
						entity.getData().setBucketId(getBucketBynameResponse.getId());
						UpdateBucketResponseWrapperDto updateBucketResponse = storageClient.updateBucket(entity.getData().getBucketName(), getBucketBynameResponse.getId(), existingForecast.getCreatedBy(), addCollabratorsList);
						if (updateBucketResponse.getErrors() != null) {
							log.error("Failed while saving details of collaborator {} Caused due to Exception {}", existingForecast.getName(), updateBucketResponse.getErrors().get(0).getMessage());
							MessageDescription msg = new MessageDescription("Failed to save collaborator details.");
							errors.add(msg);
							responseMessage.setSuccess("FAILED");
							responseMessage.setErrors(errors);
							return responseMessage;
						}
					} else {
						if (getBucketBynameResponse.getErrors() != null) {
							log.error("Failed while saving details of collaborator {} Caused due to Exception {}", existingForecast.getName(), getBucketBynameResponse.getErrors().get(0).getMessage());
							MessageDescription msg = new MessageDescription("Failed to save collaborator details.");
							errors.add(msg);
							responseMessage.setSuccess("FAILED");
							responseMessage.setErrors(errors);
							return responseMessage;
						}
					}
				}

				this.jpaRepo.save(entity);
				responseMessage.setSuccess("SUCCESS");
			} catch (Exception e) {
				log.error("Failed while saving details of collaborator " + existingForecast.getName());
				MessageDescription msg = new MessageDescription("Failed to save collaborator details.");
				errors.add(msg);
				responseMessage.setSuccess("FAILED");
				responseMessage.setErrors(errors);
				return responseMessage;
			}

		}

		return responseMessage;
	}

	@Override
	@Transactional
	public GenericMessage deleteForecastByID(String id) {
		GenericMessage responseMessage = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		Optional<ForecastNsql> entityOptional = jpaRepo.findById(id);

		if (entityOptional != null) {
			ForecastNsql entity = entityOptional.get();
			List<RunDetails> existingRuns = entity.getData().getRuns();
			String bucketName = entity.getData().getBucketName();

			// To delete all the runs which are associated to the entity.
			if (existingRuns != null && !existingRuns.isEmpty()) {
				for (RunDetails run : existingRuns) {
					DataBricksErrorResponseVO errResponse = this.dataBricksClient.deleteRun(run.getRunId());
					if (errResponse != null
							&& (errResponse.getErrorCode() != null || errResponse.getMessage() != null)) {
						String msg = "Failed to delete Run. Please delete them manually" + run.getRunId();
						if (errResponse.getErrorCode() != null) {
							msg += errResponse.getErrorCode();
						}
						if (errResponse.getMessage() != null) {
							msg += errResponse.getMessage();
						}
						MessageDescription errMsg = new MessageDescription(msg);
						warnings.add(errMsg);
						responseMessage.setWarnings(errors);
						log.error(msg);
					} else {
						run.setIsDelete(true);
					}
				}
			}

			// To delete bucket in minio storage.
			if (bucketName != null) {
				DeleteBucketResponseWrapperDto response = storageClient.deleteBucketCascade(bucketName);

				if (response != null && "FAILED".equalsIgnoreCase(response.getStatus())) {
					String msg = "Failed to delete Bucket.";
					MessageDescription errMsg = new MessageDescription(msg);
					errors.add(errMsg);
					responseMessage.setSuccess("FAILED");
					responseMessage.setErrors(errors);
					log.error("Failed to delete Bucket Please try again.");
					return responseMessage;
				}
			}

			// To delete an Entity.
			this.jpaRepo.delete(entity);

			responseMessage.setErrors(null);
			responseMessage.setSuccess("SUCCESS");
		}
		return responseMessage;
	}

	@Override
	@Transactional
	public GenericMessage deletRunByUUID(String id, String rid) {
		GenericMessage responseMessage = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		List<MessageDescription> warnings = new ArrayList<>();
		Optional<ForecastNsql> entityOptional = jpaRepo.findById(id);
		if(entityOptional!=null) {
			ForecastNsql entity = entityOptional.get();
			List<RunDetails> existingRuns = entity.getData().getRuns();
			List<RunDetails> updatedRuns = new ArrayList<>();
			for(RunDetails run : existingRuns) {
				if(rid.equalsIgnoreCase(run.getId())) {
					DataBricksErrorResponseVO errResponse = this.dataBricksClient.deleteRun(run.getRunId());
					if(errResponse!=null && (errResponse.getErrorCode()!=null || errResponse.getMessage()!=null)) {
						String msg = "Failed to delete Run." ;
						if(errResponse.getErrorCode()!=null) {
							msg+= errResponse.getErrorCode();
						}
						if(errResponse.getMessage()!=null) {
							msg+= errResponse.getMessage();
						}
						MessageDescription errMsg = new MessageDescription(msg);
						errors.add(errMsg);
						responseMessage.setSuccess("FAILED");
						responseMessage.setErrors(errors);
						return responseMessage;
					}else {
						run.setIsDelete(true);
					}
				}
				updatedRuns.add(run);
			}
			entity.getData().setRuns(updatedRuns);
			jpaRepo.save(entity);
		}
		responseMessage.setSuccess("SUCCESS");
		return responseMessage;
	}

	@Override
	@Transactional
	public Boolean isBucketExists(String bucketName) {
		return storageClient.isBucketExists(bucketName);
	}	
	
	
}
