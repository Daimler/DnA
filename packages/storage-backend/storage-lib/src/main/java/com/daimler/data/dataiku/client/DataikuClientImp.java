/* LICENSE START
 * 
 * MIT License
 * 
 * Copyright (c) 2019 Daimler TSS GmbH
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * LICENSE END 
 */

package com.daimler.data.dataiku.client;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.daimler.data.dto.DataikuPermission;
import com.daimler.data.dto.DataikuUserRole;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class DataikuClientImp implements DataikuClient {

	private Logger LOGGER = LoggerFactory.getLogger(DataikuClientImp.class);

	@Value("${dataiku.production.uri}")
	private String productionUri;

	@Value("${dataiku.production.apiKey}")
	private String productionApiKey;

	@Value("${dataiku.training.uri}")
	private String trainingUri;

	@Value("${dataiku.training.apiKey}")
	private String trainingApiKey;

	@Autowired
	RestTemplate restTemplate;

	@Value("${dataiku.apacCorpdir}")
	private String apacCorpdir;
	
	@Value("${dataiku.emeaCorpdir}")
	private String emeaCorpdir;
	
	@Value("${dataiku.projectsUriPath}")
	private String projectsUriPath;
	
	@Value("${dataiku.userRoleUriPath}")
	private String userRoleUriPath;
	
	@Value("${dataiku.projectPermissionUriPath}")
	private String projectPermissionUriPath;
	

	/**
	 * <p>
	 * To get user role
	 * </p>
	 * 
	 * @param userId
	 * @return DataikuUserRole
	 */
	@Override
	@SuppressWarnings({ "rawtypes" })
	public Optional<DataikuUserRole> getDataikuUserRole(String userId, Boolean live) {
		DataikuUserRole userRole = null;
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			String dataikuUri = setDataikuUri(live, headers, userRoleUriPath);
			HttpEntity entity = new HttpEntity<>(headers);
			ResponseEntity<String> response = null;
			try {
				LOGGER.debug("Fetching details of user {} from emea", userId);
				response = restTemplate.exchange(dataikuUri + userId.toLowerCase() +"@"+ emeaCorpdir, HttpMethod.GET,
						entity, String.class);
			} catch (Exception e) {
				LOGGER.error("Error occuried while fetching dataiku user role error:{}", e.getMessage());
				LOGGER.debug("Fetching details of user {} from apac", userId);
				response = restTemplate.exchange(dataikuUri + userId.toLowerCase() +"@"+ apacCorpdir, HttpMethod.GET,
						entity, String.class);
			}
			if (response != null && response.hasBody()) {
				LOGGER.debug("Successfully fetched user details");
				userRole = new DataikuUserRole();
				ObjectMapper mapper = new ObjectMapper();
				mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				userRole = mapper.readValue(response.getBody(), new TypeReference<DataikuUserRole>() {
				});
			}

		} catch (JsonParseException e) {
			LOGGER.error("JsonParseException occured:{}", e.getMessage());
		} catch (JsonMappingException e) {
			LOGGER.error("JsonMappingException occured:{}", e.getMessage());
		} catch (Exception e) {
			LOGGER.error("Error occured while calling dataiku user role service:{}", e.getMessage());
		}
		return Optional.ofNullable(userRole);
	}

	/**
	 * <p>
	 * To get dataiku project permission
	 * </p>
	 * 
	 * @param projectKey
	 * @return DataikuPermission
	 */
	@Override
	@SuppressWarnings({ "rawtypes" })
	public Optional<DataikuPermission> getDataikuProjectPermission(String projectKey, Boolean live) {
		DataikuPermission permission = null;
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			String dataikuUri = setDataikuUri(live, headers, projectsUriPath);
			dataikuUri = dataikuUri + projectKey + projectPermissionUriPath;
			HttpEntity entity = new HttpEntity<>(headers);
			ResponseEntity<String> response = restTemplate.exchange(dataikuUri, HttpMethod.GET, entity, String.class);
			if (response.hasBody()) {
				LOGGER.debug("In getDataikuProjectPermission,  Success from dataiku");
				permission = new DataikuPermission();
				ObjectMapper mapper = new ObjectMapper();
				mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				permission = mapper.readValue(response.getBody(), new TypeReference<DataikuPermission>() {
				});
			}

		} catch (JsonParseException e) {
			LOGGER.error("In getDataikuProjectPermission, JsonParseException occured:{}", e.getMessage());
		} catch (JsonMappingException e) {
			LOGGER.error("In getDataikuProjectPermission, JsonMappingException occured:{}", e.getMessage());
		} catch (Exception e) {
			LOGGER.error("In getDataikuProjectPermission, Error occured while calling dataiku user role service:{}",
					e.getMessage());
		}
		return Optional.ofNullable(permission);
	}

//	@Override
//	public Optional<DataikuProjectVO> getDataikuProject(String projectKey, Boolean live) {
//		DataikuProjectVO project = null;
//		try {
//			HttpHeaders headers = new HttpHeaders();
//			headers.set("Accept", "application/json");
//			headers.set("Content-Type", "application/json");
//			String dataikuUri = setDataikuUri(live, headers, projectsUriPath + projectKey);
//			HttpEntity entity = new HttpEntity<>(headers);
//			ResponseEntity<String> response = restTemplate.exchange(dataikuUri, HttpMethod.GET, entity, String.class);
//			if (response != null && response.hasBody()) {
//				LOGGER.debug("In getDataikuProject, Success from dataiku");
//				project = new DataikuProjectVO();
//				ObjectMapper mapper = new ObjectMapper();
//				mapper.enable(SerializationFeature.INDENT_OUTPUT);
//				mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//				project = mapper.readValue(response.getBody(), new TypeReference<DataikuProjectVO>() {
//				});
//			}
//		} catch (JsonParseException e) {
//			LOGGER.error("In getDataikuProject, JsonParseException occured:{}", e.getMessage());
//		} catch (JsonMappingException e) {
//			LOGGER.error("In getDataikuProject, JsonMappingException occured:{}", e.getMessage());
//		} catch (Exception e) {
//			LOGGER.error("In getDataikuProject, Error occured while calling dataiku service:{}", e.getMessage());
//		}
//		return Optional.ofNullable(project);
//
//	}

	/**
	 * Setting dataiku uri and basic auth
	 * 
	 * @param live
	 * @param headers
	 * @param dataikuUri
	 * @param uriExtension
	 */
	private String setDataikuUri(Boolean live, HttpHeaders headers, String uriExtension) {
		String dataikuUri = "";
		if (live) {
			LOGGER.debug("Forming uri for production environment");
			headers.setBasicAuth(productionApiKey, "");
			dataikuUri = productionUri + uriExtension;
		} else {
			LOGGER.debug("Forming uri for training environment");
			headers.setBasicAuth(trainingApiKey, "");
			dataikuUri = trainingUri + uriExtension;
		}
		LOGGER.debug("Returning from setDataikuUri.");
		return dataikuUri;
	}

}
