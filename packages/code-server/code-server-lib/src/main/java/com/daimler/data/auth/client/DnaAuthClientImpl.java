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

package com.daimler.data.auth.client;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.daimler.data.dto.userinfo.UsersCollection;

@Component
public class DnaAuthClientImpl implements DnaAuthClient {

	private Logger LOGGER = LoggerFactory.getLogger(DnaAuthClientImpl.class);

	@Value("${dna.uri}")
	private String dnaBaseUri;

	private static final String VERIFY_LOGIN = "/api/verifyLogin";
	private static final String ONBOARD_TECHNICAL_USER = "/api/users";
	private static final String GET_USERS = "/api/users?limit=0&offset=0";

	@Autowired
	RestTemplate restTemplate;
	
	@Autowired
	HttpServletRequest httpRequest;
	
	@Value("${codeserver.userauth}")
	private String codeserverAuth;

	@Override
	public JSONObject verifyLogin(String userinfo) {
		JSONObject res = null;
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			headers.set("dna-request-userdetails", userinfo);

			String dnaUri = dnaBaseUri + VERIFY_LOGIN;
			HttpEntity entity = new HttpEntity<>(headers);
			ResponseEntity<String> response = restTemplate.exchange(dnaUri, HttpMethod.POST, entity, String.class);
			if (response != null && response.hasBody()) {
				LOGGER.debug("Success from dna verify login");
				res = (JSONObject) new JSONObject(response.getBody()).get("data");
			}
		} catch (JSONException e) {
			LOGGER.error("Error occured while parsing jsonObject for DnA verifyLogin:{}", e.getMessage());
			throw e;
		} catch (Exception e) {
			LOGGER.error("Error occured while calling DnA verifyLogin:{}", e.getMessage());
			throw e;
		}
		return res;
	}

	@Override
	public UserInfoVO onboardTechnicalUser(UserRequestVO userRequestVO) {
		UserInfoVO userInfoVO = new UserInfoVO();
		try {
			String userinfo = httpRequest.getHeader("dna-request-userdetails");
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");			
			headers.set("dna-request-userdetails", userinfo);
			headers.set("codeserver-api-key", codeserverAuth);
			String onboardTechUserUri = dnaBaseUri + ONBOARD_TECHNICAL_USER;			
			HttpEntity<UserRequestVO> entity = new HttpEntity<UserRequestVO>(userRequestVO,headers);	
			ResponseEntity<UserInfoVO> response = restTemplate.exchange(onboardTechUserUri, HttpMethod.POST, entity, UserInfoVO.class);
			if (response != null) {
				HttpStatus statusCode = response.getStatusCode();
				if (statusCode != null && statusCode.is2xxSuccessful()) {
					LOGGER.info("Success from dna onboardTechnicalUser");
					userInfoVO = response.getBody();
				}
			}
		}
		catch(HttpClientErrorException ex) {
			if (ex.getRawStatusCode() == HttpStatus.CONFLICT.value()) {
				LOGGER.info("Technical user:{} already exists",userRequestVO.getData().getId());
				userInfoVO.setId(userRequestVO.getData().getId());
			}
			if (ex.getRawStatusCode() == HttpStatus.UNAUTHORIZED.value()) {
				LOGGER.info("Requested user does not have admin priviliges to onboard technical user");
				//userInfoVO.setId(userRequestVO.getData().getId());
			}
		}
		catch(Exception e) {
			LOGGER.error("On-boarding technical user failed while calling DnA onboardTechnicalUser:{}", e.getMessage());
			//throw e;
		}
		return userInfoVO;
	}

	@Override
	public UsersCollection getAll() {
		UsersCollection collection = new UsersCollection();

		try {
			String jwt = httpRequest.getHeader("Authorization");
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", jwt);
 
			String getUsersUri = dnaBaseUri + GET_USERS;
			HttpEntity entity = new HttpEntity<>(headers);
			ResponseEntity<UsersCollection> response = restTemplate.exchange(getUsersUri, HttpMethod.GET, entity,
					UsersCollection.class);
			if (response != null && response.hasBody()) {
				LOGGER.info("Success from dna client getAll");
				collection = response.getBody();
			}
		} catch (Exception e) {
			LOGGER.error("Error occured while calling dna getAll {}, returning empty", e.getMessage());
		}
		return collection;
	}

}
