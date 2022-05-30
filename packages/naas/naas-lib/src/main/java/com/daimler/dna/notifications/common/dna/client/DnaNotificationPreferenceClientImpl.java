package com.daimler.dna.notifications.common.dna.client;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.daimler.data.dto.userinfo.UsersCollection;
import com.daimler.data.dto.usernotificationpref.UserNotificationPrefVO;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DnaNotificationPreferenceClientImpl implements DnaNotificationPreferenceClient{

	@Value("${dna.uri}")
	private String dnaBaseUri;
	
	@Autowired
	HttpServletRequest httpRequest;

	@Value("${dna.user.notificationPreferences.get.api}")
	private String notificationPreferencesApiUri;
	
	@Value("${dna.user.info.get.api}")
	private String usersUri;

	@Autowired
	RestTemplate restTemplate;

	@Override
	public UsersCollection getAllUsers() {
		UsersCollection collection = new UsersCollection();
		try {
			String jwt = httpRequest.getHeader("Authorization");
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", jwt);

			String getUsersUri = dnaBaseUri + usersUri;
			HttpEntity entity = new HttpEntity<>(headers);
			ResponseEntity<UsersCollection> response = restTemplate.exchange(getUsersUri, HttpMethod.GET, entity, UsersCollection.class);
			if (response != null && response.hasBody()) {
				log.info("Success from dna getUsers");
				collection = response.getBody();
			}
		}catch (Exception e) {
			log.error("Error occured while calling dna getUsers {}, returning empty", e.getMessage());
		}
		return collection;
	}
	
	@Override
	public UserNotificationPrefVO getUserNotificationPreferences(String userId) {
		
		UserNotificationPrefVO res = new UserNotificationPrefVO();
		try {
			String jwt = httpRequest.getHeader("Authorization");
			HttpHeaders headers = new HttpHeaders();
			headers.set("Accept", "application/json");
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", jwt);
			
			String getUserNotificationPrefUri = dnaBaseUri + notificationPreferencesApiUri + "?userId=" + userId;
			HttpEntity entity = new HttpEntity<>(headers);
			ResponseEntity<UserNotificationPrefVO> response = restTemplate.exchange(getUserNotificationPrefUri, HttpMethod.GET, entity, UserNotificationPrefVO.class);
			if (response != null && response.hasBody()) {
				log.info("Success from dna getUserNotificationPreferences");
				res = response.getBody();
			}
		}catch (Exception e) {
			log.error("Error occured while calling dna getUserNotificationPreferences {}, returning default preferences", e.getMessage());
		}
		return res;
	}
	
	
	
}
