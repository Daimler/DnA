package com.daimler.data.application.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.fabric.AddGroupDto;
import com.daimler.data.dto.fabric.AddUserDto;
import com.daimler.data.dto.fabric.CreateWorkspaceDto;
import com.daimler.data.dto.fabric.ErrorResponseDto;
import com.daimler.data.dto.fabric.FabricGroupsCollectionDto;
import com.daimler.data.dto.fabric.FabricOAuthResponse;
import com.daimler.data.dto.fabric.MicrosoftGroupDetailCollectionDto;
import com.daimler.data.dto.fabric.MicrosoftGroupDetailDto;
import com.daimler.data.dto.fabric.WorkspaceDetailDto;
import com.daimler.data.dto.fabric.WorkspaceUpdateDto;
import com.daimler.data.dto.fabric.WorkspacesCollectionDto;
import com.daimler.data.util.ConstantsUtility;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FabricWorkspaceClient {

	@Value("${fabricWorkspaces.group.clientId}")
	private String groupSearchclientId;
	
	@Value("${fabricWorkspaces.group.clientSecret}")
	private String groupSearchclientSecret;
	
	@Value("${fabricWorkspaces.group.scope}")
	private String groupSearchscope;
	
	@Value("${fabricWorkspaces.group.grantType}")
	private String groupSearchgrantType;
	
	@Value("${fabricWorkspaces.group.login}")
	private String groupSearchloginUrl;
	
	
	@Value("${fabricWorkspaces.clientId}")
	private String clientId;
	
	@Value("${fabricWorkspaces.clientSecret}")
	private String clientSecret;
	
	@Value("${fabricWorkspaces.scope}")
	private String scope;
	
	@Value("${fabricWorkspaces.grantType}")
	private String grantType;
	
	@Value("${fabricWorkspaces.accessToken}")
	private String accessToken;
	
	@Value("${fabricWorkspaces.tokenTypeHint}")
	private String tokenTypeHint;
	
	@Value("${fabricWorkspaces.capacityId}")
	private String capacityId;
	
	@Value("${fabricWorkspaces.uri.login}")
	private String loginUrl;
	
	@Value("${fabricWorkspaces.uri.workspacesBase}")
	private String workspacesBaseUrl;
	
	@Value("${fabricWorkspaces.uri.capacitiesBase}")
	private String capacitiesBaseUrl;
	
	@Value("${fabricWorkspaces.uri.addUserUrl}")
	private String addUserUrl;
	
	@Autowired
	HttpServletRequest httpRequest;

	@Autowired
	private RestTemplate proxyRestTemplate;
	
	@Autowired
	private RestTemplate restTemplate;

	public String getToken() {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		String basicAuthenticationHeader = Base64.getEncoder()
				.encodeToString(new StringBuffer(clientId).append(":").append(clientSecret).toString().getBytes());
		map.add("token", accessToken);
		map.add("token_type_hint", tokenTypeHint);
		map.add("grant_type", grantType);
		map.add("scope", scope);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.set("Authorization", "Basic " + basicAuthenticationHeader);
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
		try {
			ResponseEntity<String> response = proxyRestTemplate.postForEntity(loginUrl, request, String.class);
			ObjectMapper objectMapper = new ObjectMapper();
			FabricOAuthResponse introspectionResponse = objectMapper.readValue(response.getBody(),
					FabricOAuthResponse.class);
			log.debug("Introspection Response:" + introspectionResponse);
			log.info("Successfully fetch oidc token post login for powerbi");
			return introspectionResponse.getAccess_token();
		} catch (Exception e) {
			log.error("Failed to fetch OIDC token with error {} ",e.getMessage());
			return null;
		}
	}
	
	public String getTokenForGroupSearch() {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		String basicAuthenticationHeader = Base64.getEncoder()
				.encodeToString(new StringBuffer(groupSearchclientId).append(":").append(groupSearchclientSecret).toString().getBytes());
		map.add("grant_type", groupSearchgrantType);
		map.add("scope", groupSearchscope);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.set("Authorization", "Basic " + basicAuthenticationHeader);
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
		try {
			ResponseEntity<String> response = proxyRestTemplate.postForEntity(groupSearchloginUrl, request, String.class);
			ObjectMapper objectMapper = new ObjectMapper();
			FabricOAuthResponse introspectionResponse = objectMapper.readValue(response.getBody(),
					FabricOAuthResponse.class);
			log.debug("Introspection Response:" + introspectionResponse);
			log.info("Successfully fetch oidc token post login for group search");
			return introspectionResponse.getAccess_token();
		} catch (Exception e) {
			log.error("Failed to fetch OIDC token for group search with error {} ",e.getMessage());
			return null;
		}
	}
	
	public MicrosoftGroupDetailDto searchGroup(String groupDisplayName) {
		MicrosoftGroupDetailDto microsoftGroupDetailDto = new MicrosoftGroupDetailDto();
		MicrosoftGroupDetailCollectionDto collection = new MicrosoftGroupDetailCollectionDto();
		try {
			String token = getTokenForGroupSearch();
			if(!Objects.nonNull(token)) {
				log.error("Failed to fetch token to invoke group search Api");
				return null;
			}
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity requestEntity = new HttpEntity<>(headers);
			String groupSearchUrl = ConstantsUtility.GROUPSEARCH_URL_PREFIX + groupDisplayName + ConstantsUtility.GROUPSEARCH_URL_SUFFIX;
			ResponseEntity<MicrosoftGroupDetailCollectionDto> response = proxyRestTemplate.exchange(groupSearchUrl , HttpMethod.GET,
					requestEntity, MicrosoftGroupDetailCollectionDto.class);
			if (response !=null && response.hasBody()) {
				collection = response.getBody();
				if(collection!=null && collection.getValue()!=null && !collection.getValue().isEmpty()) {
					microsoftGroupDetailDto = collection.getValue().get(0);
					log.info("Got success from group search, sending details for group {} ", groupDisplayName);
				}
			}
		}catch(Exception e) {
			log.error("Failed to get group identifier for group {} with exception {} ", groupDisplayName, e.getMessage());
			microsoftGroupDetailDto = null;
		}
		return microsoftGroupDetailDto;
	}
	
	public WorkspaceDetailDto createWorkspace(CreateWorkspaceDto createRequest) {
		WorkspaceDetailDto workspaceDetailDto = new WorkspaceDetailDto();
		try {
			String token = getToken();
			if(!Objects.nonNull(token)) {
				log.error("Failed to fetch token to invoke fabric Apis");
				workspaceDetailDto.setErrorCode("500");
				workspaceDetailDto.setMessage("Failed to login using service principal, please try later.");
				return workspaceDetailDto;
			}
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<CreateWorkspaceDto> requestEntity = new HttpEntity<>(createRequest,headers);
			ResponseEntity<WorkspaceDetailDto> response = proxyRestTemplate.exchange(workspacesBaseUrl, HttpMethod.POST,
					requestEntity, WorkspaceDetailDto.class);
			if (response!=null && response.hasBody()) {
				workspaceDetailDto = response.getBody();
			}
		}catch(HttpClientErrorException.Conflict e) {
			workspaceDetailDto.setErrorCode("409");
			workspaceDetailDto.setMessage("Workspace name already exists");
			log.error("Failed to create workspace with displayName {} with conflict error {} ", createRequest.getDisplayName(), e.getMessage());
		}catch(Exception e) {
			workspaceDetailDto.setErrorCode("500");
			workspaceDetailDto.setMessage(e.getMessage());
			log.error("Failed to create workspace with displayName {} with error {} ", createRequest.getDisplayName(), e.getMessage());
		}
		return workspaceDetailDto;
	}
	
	public WorkspaceDetailDto getWorkspaceDetails(String workspaceId) {
		WorkspaceDetailDto workspaceDetailDto = new WorkspaceDetailDto();
		try {
			String token = getToken();
			if(!Objects.nonNull(token)) {
				log.error("Failed to fetch token to invoke fabric Apis");
				workspaceDetailDto.setErrorCode("500");
				workspaceDetailDto.setMessage("Failed to login using service principal, please try later.");
				return null;
			}
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity requestEntity = new HttpEntity<>(headers);
			String workspaceUrl = workspacesBaseUrl + "/" + workspaceId;
			ResponseEntity<WorkspaceDetailDto> response = proxyRestTemplate.exchange(workspaceUrl , HttpMethod.GET,
					requestEntity, WorkspaceDetailDto.class);
			if (response !=null && response.hasBody()) {
				workspaceDetailDto = response.getBody();
			}
		}catch(Exception e) {
			if(e.getMessage()!=null) 
				if(e.getMessage().contains("InsufficientPrivileges")) {
					workspaceDetailDto.setErrorCode("InsufficientPrivileges");
					workspaceDetailDto.setMessage("Failed to fetch details, InsufficientPrivileges. Record might not exist.");
					log.error("Failed to get workspace details for id {} with {} exception . Which could mean that workspace doesnt exist anymore.", workspaceId, e.getMessage());
				}else if (e.getMessage().contains("WorkspaceNotFound")) {
					workspaceDetailDto.setErrorCode("WorkspaceNotFound");
					workspaceDetailDto.setMessage("Failed to fetch details, WorkspaceNotFound. Record might not exist.");
					log.error("Failed to get workspace details for id {} with {} exception . Which could mean that workspace doesnt exist anymore.", workspaceId, e.getMessage());
				}
			return workspaceDetailDto;
		}
		return workspaceDetailDto;
	}
	
	public WorkspacesCollectionDto getAllWorkspacesDetails() {
		WorkspacesCollectionDto collectionDto = new WorkspacesCollectionDto();
		WorkspaceDetailDto workspaceDetailDto = new WorkspaceDetailDto();
		try {
			String token = getToken();
			if(!Objects.nonNull(token)) {
				log.error("Failed to fetch token to invoke fabric Apis");
				workspaceDetailDto.setErrorCode("500");
				workspaceDetailDto.setMessage("Failed to login using service principal, please try later.");
				return collectionDto;
			}
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity requestEntity = new HttpEntity<>(headers);
			String workspacesUrl = workspacesBaseUrl;
			ResponseEntity<WorkspacesCollectionDto> response = proxyRestTemplate.exchange(workspacesUrl , HttpMethod.GET,
					requestEntity, WorkspacesCollectionDto.class);
			if (response !=null && response.hasBody()) {
				collectionDto = response.getBody();
			}
		}catch(Exception e) {
			log.error("Failed to get workspaces details with {} exception ", e.getMessage());
		}
		return collectionDto;
	}
	
	public WorkspacesCollectionDto listWorkspaces() {
		WorkspacesCollectionDto collection = new WorkspacesCollectionDto();
		try {
			String token = getToken();
			if(!Objects.nonNull(token)) {
				log.error("Failed to fetch token to invoke fabric Apis");
				return collection;
			}
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity requestEntity = new HttpEntity<>(headers);
			ResponseEntity<WorkspacesCollectionDto> response = proxyRestTemplate.exchange(workspacesBaseUrl , HttpMethod.GET,
					requestEntity, WorkspacesCollectionDto.class);
			if (response !=null && response.hasBody()) {
				collection = response.getBody();
			}
		}catch(Exception e) {
			log.error("Failed to get workspaces with {} exception ", e.getMessage());
		}
		return collection;
	}
	
	public GenericMessage addUser(String groupId, String emailAddress) {
		GenericMessage response = new GenericMessage();
		try {
			String token = getToken();
			if(!Objects.nonNull(token)) {
				response.setSuccess("FAILED");
				List<MessageDescription> errors = new ArrayList<>();
				MessageDescription errorMessage = new MessageDescription("Failed to login using service principal, please try later.");
				errors.add(errorMessage);
				response.setErrors(errors);
				response.setWarnings(new ArrayList<>());
				log.error("Failed to fetch token to invoke fabric Apis");
				return response;
			}
			AddUserDto addUserDto = new AddUserDto();
			addUserDto.setEmailAddress(emailAddress);
			addUserDto.setGroupUserAccessRight("Admin");
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<AddUserDto> requestEntity = new HttpEntity<>(addUserDto,headers);
			String addUserToGroupUrl = addUserUrl + "/" + groupId + "/users";
			ResponseEntity<String> addUserResponse = proxyRestTemplate.exchange(addUserToGroupUrl, HttpMethod.POST,
					requestEntity, String.class);
			if (addUserResponse!=null && addUserResponse.getStatusCode().is2xxSuccessful()) {
				response.setSuccess("SUCCESS");
				response.setErrors(new ArrayList<>());
				response.setWarnings(new ArrayList<>());
				return response;
			}
		}catch(Exception e) {
			response.setSuccess("FAILED");
			List<MessageDescription> errors = new ArrayList<>();
			MessageDescription errorMessage = new MessageDescription("Failed to add user to workspace with exception, please try again or contact Admin.");
			errors.add(errorMessage);
			response.setErrors(errors);
			response.setWarnings(new ArrayList<>());
			log.error("Failed to add user {} to workspace {} with exception ", emailAddress, groupId, e.getMessage());
			return response;
		}
		return null;
	}
	
	public FabricGroupsCollectionDto getGroupUsersInfo(String workspaceGroupId) {
		FabricGroupsCollectionDto response = new FabricGroupsCollectionDto();
		try {
			String token = getToken();
			if(!Objects.nonNull(token)) {
				log.error("Failed to fetch token to invoke fabric Apis");
				return null;
			}
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity requestEntity = new HttpEntity<>(headers);
			String getUserToGroupUrl = addUserUrl + "/" + workspaceGroupId + "/users";
			ResponseEntity<FabricGroupsCollectionDto> getUserResponse = restTemplate.exchange(getUserToGroupUrl, HttpMethod.GET,
					requestEntity, FabricGroupsCollectionDto.class);
			if (getUserResponse!=null && getUserResponse.getStatusCode().is2xxSuccessful()) {
				log.info("Got users and groups of workspace {} successfully", workspaceGroupId);
				response = getUserResponse.getBody();
				return response;
			}
		}catch(Exception e) {
			log.error("Failed to get users and groups of workspace {} with exception ",workspaceGroupId, e.getMessage());
			return null;
		}
		return null;
	}
	
	public GenericMessage removeUserGroup(String workspaceGroupId, String identifier) {
		GenericMessage response = new GenericMessage();
		try {
			String token = getToken();
			if(!Objects.nonNull(token)) {
				response.setSuccess("FAILED");
				List<MessageDescription> errors = new ArrayList<>();
				MessageDescription errorMessage = new MessageDescription("Failed to login using service principal, please try later.");
				errors.add(errorMessage);
				response.setErrors(errors);
				response.setWarnings(new ArrayList<>());
				log.error("Failed to fetch token to invoke fabric Apis");
				return response;
			}
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity requestEntity = new HttpEntity<>(headers);
			String removeUserToGroupUrl = addUserUrl + "/" + workspaceGroupId + "/users/" + identifier;
			ResponseEntity<Object> removeUserResponse = proxyRestTemplate.exchange(removeUserToGroupUrl, HttpMethod.DELETE,
					requestEntity, Object.class);
			if (removeUserResponse!=null && removeUserResponse.getStatusCode().is2xxSuccessful()) {
				log.info("Removed user/group {} from workspace {} successfully ", identifier , workspaceGroupId);
				response.setSuccess("SUCCESS");
				response.setErrors(new ArrayList<>());
				response.setWarnings(new ArrayList<>());
				return response;
			}
		}catch(Exception e) {
			response.setSuccess("FAILED");
			List<MessageDescription> errors = new ArrayList<>();
			MessageDescription errorMessage = new MessageDescription("Failed to remove user/group from workspace with exception, please try again or contact Admin.");
			errors.add(errorMessage);
			response.setErrors(errors);
			response.setWarnings(new ArrayList<>());
			log.error("Failed to remove user/group {} from workspace {} with exception ", identifier, workspaceGroupId, e.getMessage());
			return response;
		}
		return null;
	}
	
	public GenericMessage addGroup(String workspaceGroupId, AddGroupDto addGroupDto) {
		GenericMessage response = new GenericMessage();
		try {
			String token = getToken();
			if(!Objects.nonNull(token)) {
				response.setSuccess("FAILED");
				List<MessageDescription> errors = new ArrayList<>();
				MessageDescription errorMessage = new MessageDescription("Failed to login using service principal, please try later.");
				errors.add(errorMessage);
				response.setErrors(errors);
				response.setWarnings(new ArrayList<>());
				log.error("Failed to fetch token to invoke fabric Apis");
				return response;
			}
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<AddGroupDto> requestEntity = new HttpEntity<>(addGroupDto,headers);
			String addUserToGroupUrl = addUserUrl + "/" + workspaceGroupId + "/users";
			ResponseEntity<String> addUserResponse = proxyRestTemplate.exchange(addUserToGroupUrl, HttpMethod.POST,
					requestEntity, String.class);
			if (addUserResponse!=null && ( addUserResponse.getStatusCode().is2xxSuccessful() || addUserResponse.getStatusCode().is4xxClientError())) {
				log.info("Added default group {} to workspace {} successfully ", addGroupDto.getDisplayName(), workspaceGroupId);
				response.setSuccess("SUCCESS");
				response.setErrors(new ArrayList<>());
				response.setWarnings(new ArrayList<>());
				return response;
			}
		}catch(Exception e) {
			response.setSuccess("FAILED");
			List<MessageDescription> errors = new ArrayList<>();
			MessageDescription errorMessage = new MessageDescription("Failed to add default group to workspace with exception, please try again or contact Admin.");
			errors.add(errorMessage);
			response.setErrors(errors);
			response.setWarnings(new ArrayList<>());
			log.error("Failed to add group {} to workspace {} with exception ", addGroupDto.getDisplayName(), workspaceGroupId, e.getMessage());
			return response;
		}
		return null;
	}
	
	public WorkspaceDetailDto updateWorkspace(String workspaceId, WorkspaceUpdateDto updateRequest) {
		WorkspaceDetailDto workspaceDetailDto = new WorkspaceDetailDto();
		try {
			String token = getToken();
			if(!Objects.nonNull(token)) {
				log.error("Failed to fetch token to invoke fabric Apis");
				workspaceDetailDto.setErrorCode("500");
				workspaceDetailDto.setMessage("Failed to login using service principal, please try later.");
				return workspaceDetailDto;
			}
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<WorkspaceUpdateDto> requestEntity = new HttpEntity<>(updateRequest,headers);
			String workspaceUrl = workspacesBaseUrl + "/" + workspaceId;
			ResponseEntity<WorkspaceDetailDto> response = proxyRestTemplate.exchange(workspaceUrl, HttpMethod.PATCH,
					requestEntity, WorkspaceDetailDto.class);
			if (response!=null && response.hasBody()) {
				workspaceDetailDto = response.getBody();
			}
		}catch(Exception e) {
			log.error("Failed to update workspace {} with displayName {} with error {} ", workspaceId, updateRequest.getDisplayName(), e.getMessage());
		}
		return workspaceDetailDto;
	}
	
	public ErrorResponseDto assignCapacity(String workspaceId) {
		ErrorResponseDto errorResponse = new ErrorResponseDto();
		try {
			String token = getToken();
			if(!Objects.nonNull(token)) {
				log.error("Failed to fetch token to invoke fabric Apis");
				errorResponse.setErrorCode("500");
				errorResponse.setMessage("Failed to fetch token while assigning capacity to workspace. Please reassign or update workspace.");
				return errorResponse;
			}
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			String request = "{\"capacityId\": \"" + capacityId +"\"}";
			HttpEntity<String> requestEntity = new HttpEntity<>(request,headers);
			String assignCapacityUrl = workspacesBaseUrl + "/" + workspaceId + "/assignToCapacity";
			ResponseEntity<ErrorResponseDto> response = proxyRestTemplate.exchange(assignCapacityUrl, HttpMethod.POST,
					requestEntity, ErrorResponseDto.class);
			if (response!=null && response.hasBody()) {
				errorResponse = response.getBody();
			}
		}catch(Exception e) {
			errorResponse.setErrorCode("Failed");
			errorResponse.setMessage("Failed to assign capacity to workspace, with error " + e.getMessage());
			log.error("Failed to assign capacity {} to workspace with displayName {} with error {} ", capacityId, workspaceId , e.getMessage());
		}
		return errorResponse;
	}
	
	public ErrorResponseDto provisionWorkspace(String workspaceId) {
		ErrorResponseDto errorResponse = new ErrorResponseDto();
		try {
			String token = getToken();
			if(!Objects.nonNull(token)) {
				log.error("Failed to fetch token to invoke fabric Apis");
				errorResponse.setErrorCode("500");
				errorResponse.setMessage("Failed to login using service principal, please try later.");
				return errorResponse;
			}
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity requestEntity = new HttpEntity<>(headers);
			String assignCapacityUrl = workspacesBaseUrl + "/" + workspaceId + "/provisionIdentity";
			ResponseEntity<ErrorResponseDto> response = proxyRestTemplate.exchange(assignCapacityUrl, HttpMethod.POST,
					requestEntity, ErrorResponseDto.class);
			log.info("Workspace {} provisioned successfully", workspaceId);
			if (response!=null && response.hasBody()) {
				errorResponse = response.getBody();
			}
		}catch(Exception e) {
			if(e.getMessage()!=null) {
				if(e.getMessage().contains("WorkspaceIdentityAlreadyExists")) {
					errorResponse.setErrorCode("WorkspaceIdentityAlreadyExists");
					errorResponse.setMessage("Workspace identity already provisioned.");
					log.error("Workspace {} , identity already provisioned.", workspaceId);
				}
			}
			log.error("Workspace {} provisioning failed with exception {}", workspaceId, e.getMessage());
		}
		
		return errorResponse;
	}
	
	public ErrorResponseDto unassignCapacity(String workspaceId) {
		ErrorResponseDto errorResponse = new ErrorResponseDto();
		try {
			String token = getToken();
			if(!Objects.nonNull(token)) {
				log.error("Failed to fetch token to invoke fabric Apis");
				errorResponse.setErrorCode("500");
				errorResponse.setMessage("Failed to login using service principal, please try later.");
				return errorResponse;
			}
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			String request = "{\"capacityId\": \"" + capacityId +"\"}";
			HttpEntity requestEntity = new HttpEntity<>(headers);
			String assignCapacityUrl = workspacesBaseUrl + "/" + workspaceId + "/unassignFromCapacity";
			ResponseEntity<ErrorResponseDto> response = proxyRestTemplate.exchange(assignCapacityUrl, HttpMethod.POST,
					requestEntity, ErrorResponseDto.class);
			if (response!=null && response.hasBody()) {
				errorResponse = response.getBody();
			}
		}catch(Exception e) {
			errorResponse.setErrorCode("Failed to unassign capacity, with error " + e.getMessage());
			log.error("Failed to unassign capacity {} of workspace with displayName {} with error {} ", capacityId, workspaceId , e.getMessage());
		}
		return errorResponse;
	}
	
	public ErrorResponseDto deleteWorkspace(String workspaceId) {
		ErrorResponseDto errorResponse = new ErrorResponseDto();
		try {
			String token = getToken();
			if(!Objects.nonNull(token)) {
				log.error("Failed to fetch token to invoke fabric Apis");
				errorResponse.setErrorCode("500");
				errorResponse.setMessage("Failed to login using service principal, please try later.");
				return errorResponse;
			}
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Authorization", "Bearer "+token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity requestEntity = new HttpEntity<>(headers);
			String workspaceUrl = workspacesBaseUrl + "/" + workspaceId;
			ResponseEntity<WorkspaceDetailDto> response = proxyRestTemplate.exchange(workspaceUrl , HttpMethod.DELETE,
					requestEntity, WorkspaceDetailDto.class);
			if (response !=null && response.getStatusCode().is2xxSuccessful()) {
				errorResponse = null;
			}
		}catch(Exception e) {
			errorResponse.setMessage("Failed to delete workspace with error : " + e.getMessage());
			log.error("Failed to delete workspace details for id {} with {} exception ", workspaceId, e.getMessage());
		}
		return errorResponse;
	}
	
	
}
