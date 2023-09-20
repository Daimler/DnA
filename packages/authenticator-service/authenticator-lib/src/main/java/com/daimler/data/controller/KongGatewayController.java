package com.daimler.data.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.daimler.data.api.kongGateway.KongApi;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.kongGateway.AttachJwtPluginRequestVO;
import com.daimler.data.dto.kongGateway.AttachJwtPluginVO;
import com.daimler.data.dto.kongGateway.AttachPluginConfigVO;
import com.daimler.data.dto.kongGateway.AttachPluginRequestVO;
import com.daimler.data.dto.kongGateway.AttachPluginVO;
import com.daimler.data.dto.kongGateway.CreateRouteRequestVO;
import com.daimler.data.dto.kongGateway.CreateRouteVO;
import com.daimler.data.dto.kongGateway.CreateServiceRequestVO;
import com.daimler.data.dto.kongGateway.CreateServiceVO;
import com.daimler.data.kong.client.KongClient;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(value = "Kong API", tags = { "kong" })
@RequestMapping(value = "/api")
public class KongGatewayController implements KongApi{
	
	@Autowired
	private KongClient kongClient;
	
	private static Logger LOGGER = LoggerFactory.getLogger(KongGatewayController.class);

	@Override
	@ApiOperation(value = "Attach a plugin to service.", nickname = "attachPlugin", notes = "Attach a plugin to service.", response = GenericMessage.class, tags={ "kong", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of success", response = GenericMessage.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 409, message = "Conflict", response = GenericMessage.class),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/kong/services/{serviceName}/plugins",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
	public ResponseEntity<GenericMessage> attachPlugin(@Valid AttachPluginRequestVO attachPluginRequestVO,
			String serviceName) {
		GenericMessage response = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		AttachPluginVO attachPluginVO = attachPluginRequestVO.getData();
		if(attachPluginVO.getName().name().toLowerCase().equalsIgnoreCase("jwt")) {
			if(Objects.nonNull(attachPluginVO.getConfig())) {
				AttachPluginConfigVO configVO = attachPluginVO.getConfig();
				if(Objects.isNull(configVO.getClaimsToVerify()) || Objects.isNull(configVO.getKeyClaimName())) {
					MessageDescription msg = new MessageDescription();
					msg.setMessage("Properties claims_to_verify and key_claim_name should not be null for attaching the JWT plugin to service ");
					errors.add(msg);
					response.setErrors(errors);
					return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
				}				
			}
		}
		if(attachPluginVO.getName().name().toLowerCase().equalsIgnoreCase("oidc")) {
			if(Objects.nonNull(attachPluginVO.getConfig())) {
				AttachPluginConfigVO configVO = attachPluginVO.getConfig();
				if(Objects.isNull(configVO.getBearerOnly()) || Objects.isNull(configVO.getClientId()) ||
					Objects.isNull(configVO.getClientSecret()) || Objects.isNull(configVO.getDiscovery()) ||
					Objects.isNull(configVO.getIntrospectionEndpoint()) || Objects.isNull(configVO.getIntrospectionEndpointAuthMethod()) ||
					Objects.isNull(configVO.getLogoutPath()) || Objects.isNull(configVO.getRealm()) || 
					Objects.isNull(configVO.getRedirectAfterLogoutUri()) || Objects.isNull(configVO.getResponseType()) ||
					Objects.isNull(configVO.getScope()) || Objects.isNull(configVO.getSslVerify()) ||
					Objects.isNull(configVO.getTokenEndpointAuthMethod()) || Objects.isNull(configVO.getRedirectUri())) {
					MessageDescription msg = new MessageDescription();
					msg.setMessage("Properties should not be null for attaching the OIDC plugin to service ");
					errors.add(msg);
					response.setErrors(errors);
					return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
				}				
			}
		}
		try {
			if(Objects.nonNull(attachPluginVO) && Objects.nonNull(serviceName)) {
				response = kongClient.attachPluginToService(attachPluginVO, serviceName);
			}
			if(Objects.nonNull(response) && Objects.nonNull(response.getSuccess()) && response.getSuccess().equalsIgnoreCase("Success")) {
				LOGGER.info("Plugin: {} attached successfully to the service {}", attachPluginVO.getName(),serviceName);
				return new ResponseEntity<>(response, HttpStatus.CREATED);
			}
			else {
				LOGGER.info("Attaching plugin {} to the service {} failed with error {}", attachPluginVO.getName(), serviceName);
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}
			
		}
		catch(Exception e) {
			LOGGER.error("Failed to attach plugin {} with exception {} ", attachPluginVO.getName(),e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@ApiOperation(value = "create a route.", nickname = "createRoute", notes = "create a route", response = GenericMessage.class, tags={ "kong", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success", response = GenericMessage.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 409, message = "Conflict", response = GenericMessage.class),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/kong/services/{serviceName}/routes",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
	public ResponseEntity<GenericMessage> createRoute(@Valid CreateRouteRequestVO createRouteRequestVO,
			String serviceName) {
		GenericMessage response = new GenericMessage();
		CreateRouteVO createRouteVO = createRouteRequestVO.getData();	
		try {
			if(Objects.nonNull(createRouteVO) && Objects.nonNull(serviceName)) {
				response = kongClient.createRoute(createRouteVO, serviceName);
			}
			if(Objects.nonNull(response) && Objects.nonNull(response.getSuccess()) && response.getSuccess().equalsIgnoreCase("Success")) {
				LOGGER.info("Kong route {} created successfully", createRouteVO.getName());
				return new ResponseEntity<>(response, HttpStatus.CREATED);
			}
			else {
				LOGGER.info("Kong route {} creation failed", createRouteVO.getName());
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}
							
		}catch(Exception e) {
			LOGGER.error("Failed to create Kong route {} with exception {} ", createRouteVO.getName(),e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		
	}

	@Override
	@ApiOperation(value = "Create new service ", nickname = "createService", notes = "Create new service.", response = GenericMessage.class, tags={ "kong", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Returns message of success or failure", response = GenericMessage.class),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 409, message = "Conflict", response = GenericMessage.class),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/kong/services",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
	public ResponseEntity<GenericMessage> createService(@ApiParam(value = "Request Body that contains data required for creating a service" ,required=true ) 
		@Valid @RequestBody CreateServiceRequestVO createServiceRequestVO) {
		CreateServiceVO createServiceVO = createServiceRequestVO.getData();
		GenericMessage response = new GenericMessage();
		try {	
			if(Objects.nonNull(createServiceVO)) {
				String serviceName = createServiceVO.getName();//ws342
				String url = createServiceVO.getUrl();//http://ws342.code-server:8080
				response = kongClient.createService(serviceName, url);		
				//return new ResponseEntity<>(response, HttpStatus.CREATED);
			}			
			if (Objects.nonNull(response) && Objects.nonNull(response.getSuccess()) && response.getSuccess().equalsIgnoreCase("Success")) {
				LOGGER.info("Kong service {} created successfully", createServiceVO.getName());
				return new ResponseEntity<>(response, HttpStatus.CREATED);
			} else {
				LOGGER.info("Kong service {} creation failed", createServiceVO.getName());
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			LOGGER.error("Failed to create Kong service {} with exception {} ", createServiceVO.getName(),
					e.getLocalizedMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}			
	}	

	@Override
	 @ApiOperation(value = "Get all the existing services ", nickname = "getAllServices", notes = "Get all the existing kong services.", response = String.class, responseContainer = "List", tags={ "kong", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of success or failure", response = String.class, responseContainer = "List"),
        @ApiResponse(code = 204, message = "Fetch complete, no content found."),
        @ApiResponse(code = 400, message = "Bad request."),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/kong/services",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.GET)
	public ResponseEntity<List<String>> getAllServices() {
		List<String> serviceNames = kongClient.getAllServices();
		return new ResponseEntity<>(serviceNames,HttpStatus.OK);
	}

	@Override
	@ApiOperation(value = "Attach jwtissuer plugin to service.", nickname = "attachJwtIssuerPlugin", notes = "Attach jwtissuer plugin to service.", response = GenericMessage.class, tags={ "kong", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Returns message of success", response = GenericMessage.class),
        @ApiResponse(code = 400, message = "Bad Request", response = GenericMessage.class),
        @ApiResponse(code = 401, message = "Request does not have sufficient credentials."),
        @ApiResponse(code = 403, message = "Request is not authorized."),
        @ApiResponse(code = 405, message = "Method not allowed"),
        @ApiResponse(code = 409, message = "Conflict", response = GenericMessage.class),
        @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/kong/services/{serviceName}/jwtplugins",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
	public ResponseEntity<GenericMessage> attachJwtIssuerPlugin(
			@Valid AttachJwtPluginRequestVO attachJwtPluginRequestVO, String serviceName) {
		GenericMessage response = new GenericMessage();
		List<MessageDescription> errors = new ArrayList<>();
		AttachJwtPluginVO attachJwtPluginVO = attachJwtPluginRequestVO.getData();
		try {
			if(Objects.nonNull(attachJwtPluginVO) && Objects.nonNull(serviceName)) {
				response = kongClient.attachJwtPluginToService(attachJwtPluginVO, serviceName);
			}
			if(Objects.nonNull(response) && Objects.nonNull(response.getSuccess()) && response.getSuccess().equalsIgnoreCase("Success")) {
				LOGGER.info("Plugin: {} attached successfully to the service {}", attachJwtPluginVO.getName(),serviceName);
				return new ResponseEntity<>(response, HttpStatus.CREATED);
			}
			else {
				LOGGER.info("Attaching plugin {} to the service {} failed with error {}", attachJwtPluginVO.getName(), serviceName);
				return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
			}
			
		}
		catch(Exception e) {
			LOGGER.error("Failed to attach plugin {} with exception {} ", attachJwtPluginVO.getName(),e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	
	}

//	@Override
//	public ResponseEntity<CreateRouteResponseVO> getRouteByName(String serviceName, String routeName) {
//		CreateRouteResponseVO createRouteResponseVO = kongClient.getRouteByName(serviceName,routeName);
//		if(Objects.nonNull(createRouteResponseVO)) {
//			return new ResponseEntity<>(createRouteResponseVO, HttpStatus.OK);
//		}
//		else {
//			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//	}

//	@Override
//	public ResponseEntity<CreateServiceResponseVO> getServiceByName(String serviceName) {
//		CreateServiceResponseVO response = kongClient.getServiceByName(serviceName);
//		if(Objects.nonNull(response.getData())) {
//			return new ResponseEntity<>(response, HttpStatus.OK);
//		}
//		else {
//			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//		
//	}

}
