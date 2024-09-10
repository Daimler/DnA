package com.daimler.data.application.client;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.DeploymentManageDto;
import com.daimler.data.dto.WorkbenchManageDto;
import com.daimler.data.util.ConstantsUtility;
import com.daimler.data.dto.JupyterHubCreateUserDTO;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CodeServerClient {
	
	private static Logger LOGGER = LoggerFactory.getLogger(CodeServerClient.class);

	@Value("${codeServer.gitjob.deployuri}")
	private String codeServerGitJobDeployUri;
	
	@Value("${codeServer.gitjob.manageuri}")
	private String codeServerGitJobManageUri;

	@Value("${codeServer.git.orguri}")
	private String codeserverGitOrgUri;

	@Value("${codeServer.git.orgname}")
	private String codeServerGitOrgName;
	
	@Value("${codeServer.gitjob.pat}")
	private String personalAccessToken;
	
	@Value("${codeServer.base.uri}")
	private String codeServerBaseUri;
	
	@Value("${codeServer.env.ref}")
	private String codeServerEnvRef;

	@Value("${codeServer.jupyter.pat}")
	private String jupyterPersonalAccessToken;

	@Value("${codeServer.jupyter.url}")
	private String jupyterUrl;
	
	@Autowired
	RestTemplate restTemplate;
	

	
	// public String toDeployType(String recipeId) {
	// 	String recipeType = "";
	// 	String deployType = "";
	// 	if(recipeId!=null)
	// 		recipeType = recipeId.toLowerCase();
	// 	switch(recipeType) {
	// 		case "springboot":  deployType = ConstantsUtility.SPRINGBOOT; break;
	// 		case "springbootwithmaven": deployType = ConstantsUtility.SPRINGBOOTWITHMAVEN; break;
	// 		case "py-fastapi" : deployType = ConstantsUtility.PYFASTAPI; break;
	// 		case "vuejs" : deployType = ConstantsUtility.VUEJS; break;
	// 		case "react":  deployType = ConstantsUtility.REACT; break;
	// 		case "angular" : deployType = ConstantsUtility.ANGULAR; break;
	// 		case "quarkus" : deployType = ConstantsUtility.QUARKUS; break;
	// 		case "micronaut" : deployType = ConstantsUtility.MICRONAUT; break;
	// 		case "dash" : deployType = ConstantsUtility.DASHPYTHON; break;
	// 		case "streamlit" : deployType = ConstantsUtility.STREAMLIT; break;
	// 		case "expressjs" : deployType = ConstantsUtility.EXPRESSJS; break;
	// 		case "nestjs" : deployType = ConstantsUtility.NESTJS; break;
	// 		case "public-dna-backend" : deployType = ConstantsUtility.PUBLIC; break;
	// 		case "public-dna-frontend" : deployType = ConstantsUtility.PUBLIC; break;			
	// 		case "public-dna-report-backend" : deployType = ConstantsUtility.PUBLIC; break;
	// 		case "public-dna-codespace-backend" : deployType = ConstantsUtility.PUBLIC; break;
	// 		case "public-dna-malware-scanner" : deployType = ConstantsUtility.PUBLIC; break;
	// 		case "public-dna-storage-mfe" : deployType = ConstantsUtility.PUBLIC; break;
	// 		case "public-dna-storage-backend" : deployType = ConstantsUtility.PUBLIC; break;
	// 		case "public-dna-chronos-mfe" : deployType = ConstantsUtility.PUBLIC; break;
	// 		case "public-dna-chronos-backend" : deployType = ConstantsUtility.PUBLIC; break;
	// 		case "public-dna-data-product-mfe" : deployType = ConstantsUtility.PUBLIC; break;
	// 		case "public-dna-data-product-backend" : deployType = ConstantsUtility.PUBLIC; break;
	// 		case "public-dna-dss-mfe" : deployType = ConstantsUtility.PUBLIC; break;
	// 		case "public-dna-dataiku-backend" : deployType = ConstantsUtility.PUBLIC; break;
	// 		case "public-dna-airflow-backend" : deployType = ConstantsUtility.PUBLIC; break;
	// 		case "public-dna-modal-registry-backend" : deployType = ConstantsUtility.PUBLIC; break;
	// 		case "public-dna-trino-backend" : deployType = ConstantsUtility.PUBLIC; break;
	// 		case "public-dna-nass" : deployType = ConstantsUtility.PUBLIC; break;			
	// 		case "public-dna-authenticator-backend" : deployType = ConstantsUtility.PUBLIC; break;
	// 		case "public-dna-matomo-mfe" : deployType = ConstantsUtility.PUBLIC; break;
	// 		case "public-dna-matomo-backend" : deployType = ConstantsUtility.PUBLIC; break;
	// 		case "public-dna-datalake-mfe" : deployType = ConstantsUtility.PUBLIC; break;
	// 		case "public-user-defined" : deployType = ConstantsUtility.PUBLIC; break;
	// 		case "private-user-defined" : deployType = ConstantsUtility.PUBLIC; break;
	// 		case "bat-frontend" : deployType = ConstantsUtility.BAT_FRONTEND; break;
	// 		case "bat-backend" : deployType = ConstantsUtility.BAT_BACKEND; break;
	// 		case "public-dna-fabric-mfe" : deployType = ConstantsUtility.PUBLIC; break;
	// 		case "public-dna-dataentry-mfe" : deployType = ConstantsUtility.PUBLIC; break;
	// 		case "public-dna-fabric-backend" : deployType = ConstantsUtility.PUBLIC; break;
				
	// 		default: deployType = ConstantsUtility.DEFAULT; break;
	// 	}
	// 	return deployType;
	// }
	
	public GenericMessage manageWorkBench(WorkbenchManageDto manageDto) {
		GenericMessage response = new GenericMessage();
		String status = "FAILED";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", "Bearer " + personalAccessToken);
			HttpEntity<WorkbenchManageDto> entity = new HttpEntity<WorkbenchManageDto>(manageDto,headers);
			ResponseEntity<String> manageWorkbenchResponse = restTemplate.exchange(codeServerGitJobManageUri, HttpMethod.POST, entity, String.class);
						if (manageWorkbenchResponse != null && manageWorkbenchResponse.getStatusCode()!=null) {
				if(manageWorkbenchResponse.getStatusCode().is2xxSuccessful()) {
					status = "SUCCESS";
					LOGGER.info("Success while performing {} action for codeServer workbench for user {} ", manageDto.getInputs().getAction(), manageDto.getInputs().getShortid());
				}
				else {
					LOGGER.info("Warnings while performing {} for codeServer workbench of user {}, httpstatuscode is {}", manageDto.getInputs().getAction(), manageDto.getInputs().getShortid(), manageWorkbenchResponse.getStatusCodeValue());
					MessageDescription warning = new MessageDescription();
					warning.setMessage("Response from codeServer Initialize : " + manageWorkbenchResponse.getBody() + " Response Code is : " + manageWorkbenchResponse.getStatusCodeValue());
					warnings.add(warning);
				}
			}
			
		} catch (Exception e) {
			LOGGER.error("Error occured while calling codeServer manage workbench for user {} and action {} with exception {} ", manageDto.getInputs().getAction(), manageDto.getInputs().getShortid(), e.getMessage());
			MessageDescription error = new MessageDescription();
			error.setMessage("Failed while managing codeserver workbench with exception " + e.getMessage());
			errors.add(error);
		}
		response.setSuccess(status);
		response.setWarnings(warnings);
		response.setErrors(errors);
		return response;
	}

    // create code server using jupyter hub for a user 
	public GenericMessage doCreateCodeServer(WorkbenchManageDto manageDto, String codespaceName) {
		GenericMessage response = new GenericMessage();
		String status = "FAILED";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();
		String userId = manageDto.getInputs().getShortid().toLowerCase();
		try {
			boolean isUserCreated = isUserPresent(userId)
					|| createUser(userId, manageDto.getInputs().getIsCollaborator());

			if (isUserCreated) {
				boolean isCreateServerStatus = this.createServer(manageDto, codespaceName);
				if (isCreateServerStatus) {
					status = "SUCCESS";
				} else {
					LOGGER.info(
							"Warnings while performing {} for codeServer workbench of user {}, httpstatuscode is {}",
							manageDto.getInputs().getAction(), userId);
					MessageDescription warning = new MessageDescription();
					warnings.add(warning);
				}
			}
		} catch (Exception e) {
			LOGGER.error(
					"Error occurred while calling codeServer manage workbench for user {} and action {} with exception: {}",
					userId, manageDto.getInputs().getAction(), e.getMessage());
			MessageDescription error = new MessageDescription();
			error.setMessage("Failed while managing codeserver workbench with exception: " + e.getMessage());
			e.printStackTrace();
			errors.add(error);
		}

		response.setSuccess(status);
		response.setWarnings(warnings);
		response.setErrors(errors);
		return response;
	}

    private boolean isUserPresent(String userId){
		String userURI = jupyterUrl;
		HttpEntity<JupyterHubCreateUserDTO> entity = new HttpEntity<JupyterHubCreateUserDTO>(getHeaders());
        ResponseEntity<String> manageWorkbenchResponse = restTemplate.exchange(userURI, HttpMethod.GET,entity,String.class);
		if (manageWorkbenchResponse != null && manageWorkbenchResponse.getStatusCode().is2xxSuccessful()) {
			LOGGER.info("Checking if user {} is already registered successfully", userId);

			// Parse the JSON response and check if the userId is present
			String responseBody = manageWorkbenchResponse.getBody();
			if (responseBody != null) {
				JSONArray usersArray = new JSONArray(responseBody);
				for (int i = 0; i < usersArray.length(); i++) {
					JSONObject userObject = usersArray.getJSONObject(i);
					String name = userObject.optString("name");
					if (name.equals(userId)) {
						LOGGER.info("User {} is already registered", userId);
						return true;
					}
				}
			}
		}

	 LOGGER.info("User {} is not registered", userId);
	 return false;
}

	private boolean createUser(String userId, String isCollaborator){
		String userURI = jupyterUrl;
		JupyterHubCreateUserDTO userDto = new JupyterHubCreateUserDTO();
		List<String> userName = new ArrayList<>();
		userName.add(userId);
		userDto.setUsernames(userName);
		userDto.setAdmin(false);
		HttpEntity<JupyterHubCreateUserDTO> entity = new HttpEntity<JupyterHubCreateUserDTO>(userDto,getHeaders());
        ResponseEntity<String> manageWorkbenchResponse = restTemplate.exchange(userURI, HttpMethod.POST,entity, String.class);
		if (manageWorkbenchResponse != null && manageWorkbenchResponse.getStatusCode()!=null) {
			LOGGER.info("User {} has registered sucessfully", userId);
			return manageWorkbenchResponse.getStatusCode().is2xxSuccessful();
		}
		return false;
	}

	private HttpHeaders getHeaders(){
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", "application/json");
		headers.set("Content-Type", "application/json");
		headers.set("Authorization", "Bearer " + jupyterPersonalAccessToken);
		return headers;
	}

	//to create server
	public boolean createServer(WorkbenchManageDto manageDto, String codespaceName) {
		try {
			String url = jupyterUrl+"/"+ manageDto.getInputs().getShortid().toLowerCase() + "/servers/" + manageDto.getInputs().getWsid();
			String requestJsonString = "{\"profile\": \"default\", \"env\": {\"GITHUBREPO_URL\": \"" + manageDto.getInputs().getRepo()
			+ "\", \"SHORTID\" : \"" + manageDto.getInputs().getShortid().toLowerCase() + "\", \"isCollaborator\" : \"false\", "
			+ "\"pathCheckout\": \"" + manageDto.getInputs().getPathCheckout() + "\", \"GITHUB_TOKEN\": \"" + manageDto.getInputs().getPat() + "\"" +  "}, "
			+ "\"storage_capacity\": \"" + manageDto.getInputs().getStorage_capacity()
			+ "\", \"mem_guarantee\": \"" + manageDto.getInputs().getMem_guarantee()
			+ "\", \"mem_limit\": \"" + manageDto.getInputs().getMem_limit() + "\", \"cpu_limit\": "
			+ manageDto.getInputs().getCpu_limit() + ", \"cpu_guarantee\": "
			+ manageDto.getInputs().getCpu_guarantee() + ",\"extra_containers\": "
			+ manageDto.getInputs().getExtraContainers() + "}";
			HttpEntity<String> entity = new HttpEntity<>(requestJsonString, getHeaders());
			ResponseEntity<String> manageWorkbenchResponse = restTemplate.exchange(url, HttpMethod.POST, entity,
					String.class);
				if (manageWorkbenchResponse != null && manageWorkbenchResponse.getStatusCode().is2xxSuccessful()) {
				log.info("Completed creating Jupiter repo {} initiated by user with status {}", codespaceName,
						manageWorkbenchResponse.getStatusCode());
				return true;
			}
		} catch (Exception e) {
			log.error("Error occurred while intializing server {} with exception: {}", codespaceName, e.getMessage());
		}
		return false;
	}

	// create start code server using jupyterhub ends
	public GenericMessage doStartServer(String userId, String wsId) {
		GenericMessage response = new GenericMessage();
		String status = "FAILED";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();
		try {
			boolean isServerRunning = this.serverStatus(userId, wsId);
			if (!isServerRunning) {
				boolean isStartedServer = this.startNamedServer(userId, wsId);
				if (isStartedServer) {
					status = "SUCCESS";
				} else {
					MessageDescription warning = new MessageDescription();
					warning.setMessage("Failed to start codeServer workbench server for user " + userId);
					warnings.add(warning);
				}
			}
			else
			{
				MessageDescription warning = new MessageDescription();
				warning.setMessage("Server is already running server for user " + userId);
				warnings.add(warning);
			}
		} catch (Exception e) {
			MessageDescription error = new MessageDescription();
			error.setMessage("Failed while managing codeserver workbench with exception: " + e.getMessage());
			errors.add(error);
		}
		response.setSuccess(status);
		response.setWarnings(warnings);
		response.setErrors(errors);
		return response;
	}	

	//starting named server
	public boolean startNamedServer(String userName, String wsId) {
		try {
			String url = jupyterUrl+"/" +userName + "/servers/" + wsId;
			HttpEntity<String> entity = new HttpEntity<>(getHeaders());
			ResponseEntity<String> manageWorkbenchResponse = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
			if (manageWorkbenchResponse != null && manageWorkbenchResponse.getStatusCode() != null ) {
				HttpStatus statusCode = manageWorkbenchResponse.getStatusCode();
				if (statusCode.is2xxSuccessful()) {
					log.info("Successfully started server {} initiated by user with status {}", wsId, statusCode);
					return true;
				} else {
					log.info("Failed to start server {} initiated by user with status {}", wsId, statusCode);
				}
			} else {
				log.info("Failed to start server {} initiated by user. Null response or status code.", wsId);
			}
		} catch (Exception e) {
			log.error("Error occurred while starting server {} with exception: {}", wsId, e.getMessage());
		}
		return false;
	}

	public boolean serverStatus(String userId, String workspaceId) {
		String userURI = jupyterUrl+"/" + userId;
		HttpEntity<JupyterHubCreateUserDTO> entity = new HttpEntity<>(getHeaders());
		try {
			ResponseEntity<String> manageWorkbenchResponse = restTemplate.exchange(userURI, HttpMethod.GET, entity,
					String.class);
			if (manageWorkbenchResponse != null && manageWorkbenchResponse.getStatusCode() == HttpStatus.OK) {
				String responseBody = manageWorkbenchResponse.getBody();
				if (responseBody != null) {
					JSONObject jsonResponse = new JSONObject(responseBody);
					if (jsonResponse.has("servers")) {
						JSONObject serversObject = jsonResponse.getJSONObject("servers");
						if (serversObject.has(workspaceId)) {
							JSONObject serverDetails = serversObject.getJSONObject(workspaceId);
							boolean isServerReady = serverDetails.optBoolean("ready", false);
							if (isServerReady) {
								return true;
							} else {
								LOGGER.error("Server for user {} in workspace {} is not started", userId, workspaceId);
							}
						} else {
							LOGGER.error("Workspace {} not found for user {}", workspaceId, userId);
						}
					} else {
						LOGGER.error("No servers found for user {}", userId);
					}
				} else {
					LOGGER.error("Empty response body while fetching server details for user {}", userId);
				}
			} else {
				LOGGER.error("Failed to fetch server details for user {}. Status code: {}", userId,
						manageWorkbenchResponse != null ? manageWorkbenchResponse.getStatusCode() : "null");
			}
		} catch (Exception e) {
			LOGGER.error("Exception occurred while fetching server details for user " + userId, e);
		}
		return false;
	}

	public GenericMessage checkServerStatus(String userId, String workspaceId) {
		GenericMessage response = new GenericMessage();
		String status = "FAILED";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();

		try {
			boolean isServerStatus = serverStatus(userId, workspaceId);
			if (isServerStatus) {
				status = "SUCCESS";
				LOGGER.info("Successfully started codeServer workbench server for user {}", userId);
			} else {
				LOGGER.warn("Failed to start codeServer workbench server for user {}", userId);
				MessageDescription warning = new MessageDescription();
				warning.setMessage("Failed to start codeServer workbench server for user " + userId);
				warnings.add(warning);
			}
		} catch (Exception e) {
			LOGGER.error("Error occurred while starting codeServer workbench for user {}", userId, e);
			MessageDescription error = new MessageDescription();
			error.setMessage("Failed while managing codeServer workbench: " + e.getMessage());
			errors.add(error);
		}

		response.setSuccess(status);
		response.setWarnings(warnings);
		response.setErrors(errors);

		return response;
	}	

	//To stop server of  codespace jupyter hub
	public boolean stopServer(String wsId, String userId) {
		try {
			String url = jupyterUrl+"/" + userId + "/servers/" + wsId;
			String requestJsonString = "{\"remove\":false}";  
			HttpEntity<String> entity = new HttpEntity<>(requestJsonString, getHeaders());
			ResponseEntity<String> manageWorkbenchResponse = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
	
			if (manageWorkbenchResponse != null && manageWorkbenchResponse.getStatusCode() != null) {
				if (manageWorkbenchResponse.getStatusCode().is2xxSuccessful()) {
					log.info("Server {} stopped successfully for user {}", wsId, userId);
					return true;  // Server stopped successfully
				} else {
					log.error("Failed to stop server {} for user {}. Status code: {}", wsId, userId, manageWorkbenchResponse.getStatusCodeValue());
				}
			}
		} catch (Exception e) {
			log.error("Error occurred while stopping server {} for user {}", wsId, userId, e);
		}
		return false;  // Server stop failed
	}	

	//To delete server of codespace jupyter hub
	public boolean deleteServer(WorkbenchManageDto manageDto) {
		String userId = manageDto.getInputs().getShortid().toLowerCase();
		String wsId = manageDto.getInputs().getWsid();
	try {
		String url = jupyterUrl+"/" + userId + "/servers/" + wsId;
		String requestJsonString = "{\"remove\":true}";  // Changed to true for stopping the server
		HttpEntity<String> entity = new HttpEntity<>(requestJsonString, getHeaders());
		ResponseEntity<String> manageWorkbenchResponse = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
		
		if (manageWorkbenchResponse != null && manageWorkbenchResponse.getStatusCode() != null) {
			if (manageWorkbenchResponse.getStatusCode().is2xxSuccessful()) {
				log.info("{} action for {} is deleted successfully for user {}",manageDto.getInputs().getAction(), wsId, userId);
				return true;  // Server stopped successfully
			} else {
				log.error("Failed to delete server {} for user {}. Status code: {}", wsId, userId, manageWorkbenchResponse.getStatusCodeValue());
			}
		}
		} catch (Exception e) {
			log.error("Error occurred while deleting server {} for user {}", wsId, userId, e);
		}
		
		return false;  // Server stop failed
	}
	
	public GenericMessage manageDeployment(DeploymentManageDto deployDto) {
		GenericMessage response = new GenericMessage();
		String status = "FAILED";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", "Bearer " + personalAccessToken);
			HttpEntity<DeploymentManageDto> entity = new HttpEntity<DeploymentManageDto>(deployDto,headers);
			ResponseEntity<String> manageDeploymentResponse = restTemplate.exchange(codeServerGitJobDeployUri, HttpMethod.POST, entity, String.class);
			if (manageDeploymentResponse != null && manageDeploymentResponse.getStatusCode()!=null) {
				if(manageDeploymentResponse.getStatusCode().is2xxSuccessful()) {
					status = "SUCCESS";
					LOGGER.info("Success while performing {} action for codeServer workbench for user {} ", deployDto.getInputs().getAction(), deployDto.getInputs().getShortid());
				}
				else {
					LOGGER.info("Warnings while performing {} for codeServer workbench of user {}, httpstatuscode is {}", deployDto.getInputs().getAction(), deployDto.getInputs().getShortid(),manageDeploymentResponse.getStatusCodeValue());
					MessageDescription warning = new MessageDescription();
					warning.setMessage("Response from codeServer Initialize : " + manageDeploymentResponse.getBody() + " Response Code is : " + manageDeploymentResponse.getStatusCodeValue());
					warnings.add(warning);
				}
			}
			
		} catch (Exception e) {
			LOGGER.error("Error occured while calling codeServer manage workbench for user {} and action {} with exception {} ", deployDto.getInputs().getAction(), deployDto.getInputs().getShortid(), e.getMessage());
			MessageDescription error = new MessageDescription();
			error.setMessage("Failed while managing codeserver workbench with exception " + e.getMessage());
			errors.add(error);
		}
		response.setSuccess(status);
		response.setWarnings(warnings);
		response.setErrors(errors);
		return response;
	}

	public List<String> getAllWorkspaceStatus(String userId) {
		List<String> vo = new ArrayList<>();
		try {
			String userURI = jupyterUrl+"/"+userId;
			HttpEntity<JupyterHubCreateUserDTO> entity = new HttpEntity<>(getHeaders());
			ResponseEntity<String> manageWorkbenchResponse = restTemplate.exchange(userURI, HttpMethod.GET, entity,
					String.class);
			if (manageWorkbenchResponse != null && manageWorkbenchResponse.getStatusCode() == HttpStatus.OK) {
				String responseBody = manageWorkbenchResponse.getBody();
				if (responseBody != null) {
					JSONObject jsonResponse = new JSONObject(responseBody);
					if (jsonResponse.has("servers")) {
						JSONObject serversObject = jsonResponse.getJSONObject("servers");
						for (Object key : serversObject.keySet()) {
							JSONObject keyObject = serversObject.getJSONObject(key.toString());
							boolean isServerReady = keyObject.optBoolean("ready", false);
							if (isServerReady) {
								vo.add(key.toString());
							}
						}
					}
				}
				return vo;
			}
		} catch (Exception e) {
			log.info("Failed while getting all codeserver status details" + e.getMessage());
		}
		return null;
	}

	public GenericMessage toMoveExistingtoJupyterhub(WorkbenchManageDto manageDto, String codespaceName) {
		GenericMessage response = new GenericMessage();
		String status = "FAILED";
		List<MessageDescription> warnings = new ArrayList<>();
		List<MessageDescription> errors = new ArrayList<>();
		String userId = manageDto.getInputs().getShortid().toLowerCase();
		try {
			boolean isUserCreated = isUserPresent(userId)
					|| createUser(userId, manageDto.getInputs().getIsCollaborator());

			if (isUserCreated) {
				boolean isCreateServerStatus = createServerforExisting(manageDto, codespaceName);
				if (isCreateServerStatus) {
					status = "SUCCESS";
				} else {
					LOGGER.info(
							"Warnings while performing {} for codeServer workbench of user {}, httpstatuscode is {}",
							manageDto.getInputs().getAction(), userId);
					MessageDescription warning = new MessageDescription();
					warnings.add(warning);
				}
			}
		} catch (Exception e) {
			LOGGER.error(
					"Error occurred while calling codeServer manage workbench for user {} and action {} with exception: {}",
					userId, manageDto.getInputs().getAction(), e.getMessage());
			MessageDescription error = new MessageDescription();
			error.setMessage("Failed while managing codeserver workbench with exception: " + e.getMessage());
			errors.add(error);
		}

		response.setSuccess(status);
		response.setWarnings(warnings);
		response.setErrors(errors);
		return response;
	}

	private boolean createServerforExisting(WorkbenchManageDto manageDto, String codespaceName) {
		try {
			String url = jupyterUrl+"/"+ manageDto.getInputs().getShortid().toLowerCase() + "/servers/" + manageDto.getInputs().getWsid();
			String requestJsonString = "{\"profile\": \"" + manageDto.getInputs().getProfile()
					+ "\",\"env\": {\"GITHUBREPO_URL\": \"" + manageDto.getInputs().getRepo()
					+ "\",\"SHORTID\" : \"" + manageDto.getInputs().getShortid().toLowerCase()
					+ "\",\"isCollaborator\" : \"false\",\"pathCheckout\": \"" + manageDto.getInputs().getPathCheckout() + "\""
					+ "},\"storage_capacity\": \"" + manageDto.getInputs().getStorage_capacity()
					+ "\",\"mem_guarantee\": \"" + manageDto.getInputs().getMem_guarantee()
					+ "\",\"mem_limit\": \"" + manageDto.getInputs().getMem_limit() + "\",\"cpu_limit\": "
					+ manageDto.getInputs().getCpu_limit() + ",\"cpu_guarantee\": "
					+ manageDto.getInputs().getCpu_guarantee() + "}";
			HttpEntity<String> entity = new HttpEntity<>(requestJsonString, getHeaders());
			ResponseEntity<String> manageWorkbenchResponse = restTemplate.exchange(url, HttpMethod.POST, entity,
					String.class);
			if (manageWorkbenchResponse != null && manageWorkbenchResponse.getStatusCode().is2xxSuccessful()) {
				log.info("Completed creating Jupiter server {} initiated by user with status {}", codespaceName,
						manageWorkbenchResponse.getStatusCode());
				return true;
			}
		} catch (Exception e) {
			log.error("Error occurred while creating git repo {} with exception: {}", codespaceName, e.getMessage());
		}
		return false;
	}
	
	
}
