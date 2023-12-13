package com.mb.dna.datalakehouse.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.application.auth.CreatedByVO;
import com.daimler.data.application.auth.UserStore;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.UserInfoVO;
import com.mb.dna.datalakehouse.dto.TrinoConnectorsCollectionVO;
import com.mb.dna.datalakehouse.dto.TrinoDataLakeProjectCollectionVO;
import com.mb.dna.datalakehouse.dto.TrinoDataLakeProjectRequestVO;
import com.mb.dna.datalakehouse.dto.TrinoDataLakeProjectResponseVO;
import com.mb.dna.datalakehouse.dto.TrinoDataLakeProjectUpdateRequestVO;
import com.mb.dna.datalakehouse.dto.TrinoDataLakeProjectVO;
import com.mb.dna.datalakehouse.service.TrinoDatalakeService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@Api(value = "Trino Connectors Details API", tags = { "datalakes" })
@RequestMapping("/api")
@Slf4j
public class TrinoDatalakeController {

	@Autowired
	private UserStore userStore;
	
	private static final String BUCKETS_PREFIX = "dna-datalake-";
	private static final String SCHEMA_PREFIX = "dna_datalake_";
	private static final String ICEBERG_CONNECTOR = "Iceberg";
	private static final String DELTALAKE_CONNECTOR = "Delta";
	
	@Value("${trino.catalog.iceberg}")
	private String icebergCatalogName;
	
	@Value("${trino.catalog.delta}")
	private String deltaCatalogName;
	
	@Autowired
	private TrinoDatalakeService trinoDatalakeService;
	
	@ApiOperation(value = "Get all available trino datalake projects.", nickname = "getAll", notes = "Get all trino datalake projects. This endpoints will be used to Get all valid available datalake projects", response = TrinoConnectorsCollectionVO.class, tags = {
			"datalakes", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Successfully completed fetching all datalake projects", response = TrinoDataLakeProjectCollectionVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/datalakes", produces = { "application/json" }, method = RequestMethod.GET)
	public ResponseEntity<TrinoDataLakeProjectCollectionVO> getAll(
			@ApiParam(value = "page number from which listing of projects should start. Offset. Example 2") @Valid @RequestParam(value = "offset", required = false) Integer offset,
			@ApiParam(value = "page size to limit the number of projects, Example 15") @Valid @RequestParam(value = "limit", required = false) Integer limit) {
		int defaultLimit = 10;
		if (offset == null || offset < 0)
			offset = 0;
		if (limit == null || limit < 0) {
			limit = defaultLimit;
		}
		CreatedByVO requestUser = this.userStore.getVO();
		String user = requestUser.getId();
		final List<TrinoDataLakeProjectVO> projects = trinoDatalakeService.getAll(limit, offset, user);
		Long count = trinoDatalakeService.getCount(user);
		TrinoDataLakeProjectCollectionVO collectionVO = new TrinoDataLakeProjectCollectionVO();
		log.debug("Sending all trino datalake projects and their details");
		if (projects != null && projects.size() > 0) {
			collectionVO.setData(projects);
			collectionVO.setTotalCount(count);
			return new ResponseEntity<>(collectionVO, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(collectionVO, HttpStatus.NO_CONTENT);
		}
	}
	
	@ApiOperation(value = "Get datalake project details for a given Id.", nickname = "getById", notes = "Get datalake project details for a given Id.", response = TrinoDataLakeProjectVO.class, tags = {
			"datalakes", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure", response = TrinoDataLakeProjectVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/datalakes/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	public ResponseEntity<TrinoDataLakeProjectVO> getById(
			@ApiParam(value = "Data Lake project ID to be fetched", required = true) @PathVariable("id") String id){
		try {
		TrinoDataLakeProjectVO existingProject = trinoDatalakeService.getUpdatedById(id);
		if(existingProject==null || !id.equalsIgnoreCase(existingProject.getId())) {
			log.warn("No datalake project found with id {}, failed to fetch saved inputs for given id", id);
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}else {
			CreatedByVO requestUser = this.userStore.getVO();
			String user = requestUser.getId();
			Long count = trinoDatalakeService.getCountForUserAndProject(user,id);
			if(count<=0) {
				log.warn("User {} , not part of datalake project with id {}, access denied",user, id);
				return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
			}
		}
		
		return new ResponseEntity<>(existingProject, HttpStatus.OK);
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	@ApiOperation(value = "Create Data Lake project for user.", nickname = "createDataLakeProject", notes = "Create Data Lake project for user ", response = TrinoDataLakeProjectResponseVO.class, tags = {
			"datalakes", })
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Returns message of success or failure ", response = TrinoDataLakeProjectResponseVO.class),
			@ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/datalakes", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	public ResponseEntity<TrinoDataLakeProjectResponseVO> createDataLakeProject(
			@ApiParam(value = "Request Body that contains data required for create Data Lake project for user", required = true) @Valid @RequestBody TrinoDataLakeProjectRequestVO requestVO) {
		TrinoDataLakeProjectResponseVO serviceCreateResponse = new TrinoDataLakeProjectResponseVO();
		TrinoDataLakeProjectResponseVO responseVO = new TrinoDataLakeProjectResponseVO();
		TrinoDataLakeProjectVO request = requestVO.getData();
		String name = request.getProjectName();
		String connectorType = request.getConnectorType();
		String catalogName = connectorType!=null && connectorType.equalsIgnoreCase(ICEBERG_CONNECTOR) ? icebergCatalogName : deltaCatalogName;
		String schemaName = SCHEMA_PREFIX+name;
		TrinoDataLakeProjectVO existingProject = trinoDatalakeService.getByUniqueliteral("projectName", name);
		if(existingProject!=null && existingProject.getId()!=null) {
			log.error("Datalake project with this name {} already exists , failed to create datalake project", name);
			MessageDescription invalidMsg = new MessageDescription("Datalake project already exists with given name");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.setSuccess(HttpStatus.CONFLICT.name());
			errorMessage.addWarnings(invalidMsg);
			responseVO.setData(request);
			responseVO.setResponse(errorMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.CONFLICT);
		}
		List<String> existingSchemas = trinoDatalakeService.showSchemas(catalogName, schemaName);
		if(existingSchemas!=null && !existingSchemas.isEmpty()) {
			log.error("Datalake project with this name {} already exists , failed to create datalake project", name);
			MessageDescription invalidMsg = new MessageDescription("Datalake project already exists with given name");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.setSuccess(HttpStatus.CONFLICT.name());
			errorMessage.addWarnings(invalidMsg);
			responseVO.setData(request);
			responseVO.setResponse(errorMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.CONFLICT);
		}
		String bucketName = BUCKETS_PREFIX + name;
		Boolean isBucketExists = trinoDatalakeService.isBucketExists(bucketName);
		if(isBucketExists) {
			log.error("Datalake project with this name {} already exists , failed to create forecast project", name);
			MessageDescription invalidMsg = new MessageDescription("Datalake project already exists with given name");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.setSuccess(HttpStatus.CONFLICT.name());
			errorMessage.addWarnings(invalidMsg);
			responseVO.setData(request);
			responseVO.setResponse(errorMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.CONFLICT);
		}
		CreatedByVO requestUser = this.userStore.getVO();
		request.setBucketName(bucketName);
		UserInfoVO createdBy = new UserInfoVO();
		BeanUtils.copyProperties(requestUser, createdBy);
		request.setCreatedBy(createdBy);
		request.setSchemaName(schemaName);
		request.setCatalogName(catalogName);
		request.setBucketName(bucketName);
		request.setBucketId(null);
		request.setId(null);
		SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
		Date createdOn = new Date();
		try {
			createdOn = isoFormat.parse(isoFormat.format(createdOn));
		}catch(Exception e) {
			log.warn("Failed to format createdOn date to ISO format");
		}
		request.setCreatedOn(createdOn);
		TrinoDataLakeProjectVO data = new TrinoDataLakeProjectVO();
		try {
			serviceCreateResponse = trinoDatalakeService.createDatalake(request);
			data = serviceCreateResponse.getData();
			if(data!=null && data.getId()!=null) {
				responseVO.setData(data);
				responseVO.setResponse(serviceCreateResponse.getResponse());
				log.info("Datalake {} created successfully", name);
				return new ResponseEntity<>(responseVO, HttpStatus.CREATED);
			}else {
				GenericMessage failedResponse = new GenericMessage();
				List<MessageDescription> messages = new ArrayList<>();
				MessageDescription message = new MessageDescription();
				message.setMessage("Failed to save due to internal error");
				messages.add(message);
				failedResponse.addErrors(message);
				failedResponse.setSuccess("FAILED");
				responseVO.setData(request);
				responseVO.setResponse(failedResponse);
				log.error("Datalake project {} , failed to create", name);
				return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}catch(Exception e) {
			GenericMessage failedResponse = new GenericMessage();
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage("Failed to save due to internal error");
			messages.add(message);
			failedResponse.addErrors(message);
			failedResponse.setSuccess("FAILED");
			responseVO.setData(request);
			responseVO.setResponse(failedResponse);
			log.error("Exception occurred:{} while creating datalake project {} ", e.getMessage(), name);
			return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@ApiOperation(value = "update datalake project details for a given Id.", nickname = "updateById", notes = "update datalake project details for a given Id.", response = TrinoDataLakeProjectResponseVO.class, tags = {
			"datalakes", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Returns message of success or failure", response = TrinoDataLakeProjectResponseVO.class),
			@ApiResponse(code = 204, message = "Fetch complete, no content found."),
			@ApiResponse(code = 400, message = "Bad request."),
			@ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
			@ApiResponse(code = 403, message = "Request is not authorized."),
			@ApiResponse(code = 405, message = "Method not allowed"),
			@ApiResponse(code = 500, message = "Internal error") })
	@RequestMapping(value = "/datalakes/{id}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	public ResponseEntity<TrinoDataLakeProjectResponseVO> updateById(
			@ApiParam(value = "Data Lake project ID to be updated", required = true) @PathVariable("id") String id,
			@ApiParam(value = "Request Body that contains data required for updating of datalake project details like add/remove tables and manage collaborators of tables", required = true) @Valid @RequestBody TrinoDataLakeProjectUpdateRequestVO datalakeUpdateRequestVO) {
		TrinoDataLakeProjectVO existingProject = trinoDatalakeService.getById(id);
		List<MessageDescription> errors = new ArrayList<>();
		GenericMessage responseMessage = new GenericMessage();
		TrinoDataLakeProjectResponseVO responseVO = new TrinoDataLakeProjectResponseVO();
		TrinoDataLakeProjectResponseVO serviceUpdateResponse = new TrinoDataLakeProjectResponseVO();
		TrinoDataLakeProjectUpdateRequestVO request =datalakeUpdateRequestVO;
		if(existingProject==null || existingProject.getId()==null) {
			log.error("Datalake project with id {} is not found ", id);
			MessageDescription invalidMsg = new MessageDescription("Datalake project does not exist with given name");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.setSuccess("FAILED");
			errorMessage.addWarnings(invalidMsg);
			responseVO.setData(new TrinoDataLakeProjectVO());
			responseVO.setResponse(errorMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.NOT_FOUND);
		}
		//check if user is project owner
		CreatedByVO requestUser = this.userStore.getVO();
		String user = requestUser.getId();
		if(!(existingProject.getCreatedBy()!=null && existingProject.getCreatedBy().getId()!=null && existingProject.getCreatedBy().getId().equalsIgnoreCase(user))){
			log.error("Datalake project with id {} is not found ", id);
			MessageDescription invalidMsg = new MessageDescription("Only Owner can edit project details. Access denied.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.setSuccess("FAILED");
			errorMessage.addWarnings(invalidMsg);
			responseVO.setData(new TrinoDataLakeProjectVO());
			responseVO.setResponse(errorMessage);
			return new ResponseEntity<>(responseVO, HttpStatus.FORBIDDEN);
		}
		String name = existingProject.getProjectName();
		TrinoDataLakeProjectVO data = new TrinoDataLakeProjectVO();
		try {
			serviceUpdateResponse = trinoDatalakeService.updateDatalake(existingProject, request);           
			data = serviceUpdateResponse.getData();
			if(data!=null && data.getId()!=null) {
				responseVO.setData(data);
				responseVO.setResponse(serviceUpdateResponse.getResponse());
				log.info("Datalake {} updated successfully", name);
				return new ResponseEntity<>(responseVO, HttpStatus.OK);
			}else {
				GenericMessage failedResponse = new GenericMessage();
				List<MessageDescription> messages = new ArrayList<>();
				MessageDescription message = new MessageDescription();
				message.setMessage("Failed to save due to internal error");
				messages.add(message);
				failedResponse.addErrors(message);
				failedResponse.setSuccess("FAILED");
				responseVO.setData(existingProject);
				responseVO.setResponse(failedResponse);
				log.error("Datalake project {} , failed to update", name);
				return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		catch(Exception e) {
			GenericMessage failedResponse = new GenericMessage();
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage("Failed to save due to internal error");
			messages.add(message);
			failedResponse.addErrors(message);
			failedResponse.setSuccess("FAILED");
			responseVO.setData(existingProject);
			responseVO.setResponse(failedResponse);
			log.error("Exception occurred:{} while creating datalake project {} ", e.getMessage(), name);
			return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	
}
