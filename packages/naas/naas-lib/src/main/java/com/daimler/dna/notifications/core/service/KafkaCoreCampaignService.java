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

package com.daimler.dna.notifications.core.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.daimler.data.dto.solution.ChangeLogVO;
import com.daimler.data.dto.usernotificationpref.UserNotificationPrefVO;
import com.daimler.dna.notifications.common.consumer.KafkaDynamicConsumerService;
import com.daimler.dna.notifications.common.dna.client.DnaNotificationPreferenceClient;
import com.daimler.dna.notifications.common.event.config.GenericEventRecord;
import com.daimler.dna.notifications.common.producer.KafkaDynamicProducerService;
import com.daimler.dna.notifications.common.util.CacheUtil;
import com.daimler.dna.notifications.dto.NotificationVO;
import com.mbc.dna.notifications.mailer.JMailer;

@Service
public class KafkaCoreCampaignService {

	private static Logger LOGGER = LoggerFactory.getLogger(KafkaCoreCampaignService.class);

	@Autowired
	private KafkaDynamicProducerService dynamicProducer;

	@Autowired
	private KafkaDynamicConsumerService dynamicConsumer;

	@Autowired
	private CacheUtil cacheUtil;
	
	@Autowired
	private DnaNotificationPreferenceClient userNotificationPreferencesClient;
	
	@Autowired
	private JMailer mailer;

	@Value("${kafka.centralTopic.name}")
	private String dnaCentralTopicName;
	
	private static String SOLUTION_NOTIFICATION_KEY = "Solution";
	private static String NOTEBOOK_NOTIFICATION_KEY = "Notebook";
	private static String STORAGE_NOTIFICATION_KEY = "Storage";
	
	/*
	 * @KafkaListener(topics = "dnaCentralEventTopic") public void
	 * centralTopicListnerToPublishToUsers(GenericEventRecord message) {
	 * LOGGER.debug("Received Message in group foo: " + message);
	 * if(message!=null) { List<String> users = message.getSubscribedUsers();
	 * for(String user: users) { dynamicProducer.sendMessage(user, message); } } }
	 */

	@KafkaListener(topics = "dnaCentralEventTopic")
	public void centralTopicListnerToPublishToUsers(GenericEventRecord message) {
		if (message != null) {
			List<String> users = message.getSubscribedUsers();
			List<String> usersEmails = message.getSubscribedUsersEmail();
			int userListPivot = 0;
			for (String user : users) {
				if (StringUtils.hasText(user) && user != "null") {
					if (cacheUtil.getCache(user) == null) {
						LOGGER.info("Creating cache for user " + user);
						cacheUtil.createCache(user);
					}
					UserNotificationPrefVO preferenceVO = userNotificationPreferencesClient.getUserNotificationPreferences(user);
					boolean appNotificationPreferenceFlag = true;
					boolean emailNotificationPreferenceFlag = false;
					if(message.getEventType().contains(SOLUTION_NOTIFICATION_KEY)) {
						appNotificationPreferenceFlag = preferenceVO.getSolutionNotificationPref().isEnableAppNotifications();
						emailNotificationPreferenceFlag =  preferenceVO.getSolutionNotificationPref().isEnableEmailNotifications();
					}
					if(message.getEventType().contains(NOTEBOOK_NOTIFICATION_KEY)) {
						appNotificationPreferenceFlag = preferenceVO.getNotebookNotificationPref().isEnableAppNotifications();
						emailNotificationPreferenceFlag =  preferenceVO.getNotebookNotificationPref().isEnableEmailNotifications();
					}
					if(message.getEventType().contains(STORAGE_NOTIFICATION_KEY)) {
						appNotificationPreferenceFlag = preferenceVO.getPersistenceNotificationPref().isEnableAppNotifications();
						emailNotificationPreferenceFlag =  preferenceVO.getPersistenceNotificationPref().isEnableEmailNotifications();
					}
					NotificationVO vo = new NotificationVO();
					vo.setDateTime(message.getTime());
					vo.setEventType(message.getEventType());
					vo.setChangeLogs(message.getChangeLogs());
					vo.setId(message.getUuid());
					vo.setResourceId(message.getResourceId());
					vo.setMessageDetails(message.getMessageDetails());
					vo.setIsRead("false");
					vo.setMessage(message.getMessage());
					String emailBody = "<br/>"+ message.getMessage() + "<br/>";
					if(!ObjectUtils.isEmpty(message.getChangeLogs())) {
						for (ChangeLogVO changeLog : message.getChangeLogs()) {
							emailBody += "<br/>" + "\u2022" + " " + changeLog.getChangeDescription() + "<br/>";
						}
					}
					if(appNotificationPreferenceFlag) {
						cacheUtil.addEntry(user, vo);
						LOGGER.info("New message with details- user {}, eventType {}, uuid {} added to user notifications", user,
								message.getEventType(), message.getUuid());
					}else {
						LOGGER.info("Skipped message as per user preference, Details: user {}, eventType {}, uuid {} ", user,
								message.getEventType(), message.getUuid());
					}
					if(emailNotificationPreferenceFlag) {
						String userEmail = usersEmails.get(userListPivot);
						if(userEmail!= null && !"".equalsIgnoreCase(userEmail)) {
							String emailSubject = message.getEventType()+" Email Notification";
							mailer.sendSimpleMail(message.getUuid(),userEmail, emailSubject , emailBody);
							LOGGER.info("Sent email as per user preference, Details: user {}, eventType {}, uuid {}", user,
									message.getEventType(), message.getUuid());
						}else {
							LOGGER.info("Skipped sending email even after user preference is enabled. Cause is email id not found for the user. Details: user {}, eventType {}, uuid {}", user,
									message.getEventType(), message.getUuid());
						}
					}else {
						LOGGER.info("Skipped email as per user preference, Details: user {}, eventType {}, uuid {}", user,
								message.getEventType(), message.getUuid());
					}
				}
				userListPivot++;
				// dynamicProducer.sendMessage(user, message);
			}
		}
	}

	public void publishMessageTocentralTopic(GenericEventRecord request) {
		dynamicProducer.sendMessage(dnaCentralTopicName, request);
	}

	public List<String> getEventCategories(String userId) {
		List<String> results = new ArrayList<>();
		List<GenericEventRecord> allRecords = dynamicConsumer.consumeRecordsFromTopic(Arrays.asList(userId));
		if (allRecords != null && !allRecords.isEmpty()) {
			results = allRecords.stream().map(n -> n.getEventType()).collect(Collectors.toList());
			results = results.stream().distinct().collect(Collectors.toList());
			LOGGER.debug("List of all event  catergories for user {} is {} ", userId, results);
		}
		return results;
	}

	public List<NotificationVO> getMessages(String userId, String eventCategory, String readType, String searchTerm,
			int offset, int limit) {
		try {
			List<NotificationVO> results = new ArrayList<>();
			List<NotificationVO> resultsAfterEventCategoryFilter = new ArrayList<>();
			List<NotificationVO> resultsAfterSearchTermFilter = new ArrayList<>();
			LOGGER.debug(
					"Get messages requested by user {} for eventCategory {} and readType {} "
							+ "with searchTerm {} and offset {} limit {}",
					userId, eventCategory, readType, searchTerm, offset, limit);
			List<GenericEventRecord> readRecords = dynamicConsumer
					.consumeRecordsFromTopic(Arrays.asList(userId + "_READ"));
			LOGGER.debug("fetched all readRecords");
			List<GenericEventRecord> deletedRecords = dynamicConsumer
					.consumeRecordsFromTopic(Arrays.asList(userId + "_DELETED"));
			LOGGER.debug("fetched all deletedRecords");
			List<String> readIds = readRecords.stream().map(n -> n.getUuid()).collect(Collectors.toList());
			LOGGER.trace("Ids of all readRecords {} ", readIds);
			List<String> deletedIds = deletedRecords.stream().map(n -> n.getUuid()).collect(Collectors.toList());
			LOGGER.trace("Ids of all deletedRecors {}", deletedIds);
			if ("READ".equalsIgnoreCase(readType)) {
				// read-deleted
				LOGGER.debug("removing deleted records from readRecords");
				for (GenericEventRecord record : readRecords) {
					if (!deletedIds.contains(record.getUuid())) {
						NotificationVO vo = new NotificationVO();
						vo.setDateTime(record.getTime());
						vo.setChangeLogs(record.getChangeLogs());
						vo.setEventType(record.getEventType());
						vo.setId(record.getUuid());
						vo.setIsRead("true");
						vo.setMessage(record.getMessage());
						results.add(vo);
					}
				}
			} else {
				List<GenericEventRecord> allRecords = dynamicConsumer.consumeRecordsFromTopic(Arrays.asList(userId));
				if ("UNREAD".equalsIgnoreCase(readType)) {
					LOGGER.debug("removing deleted and read records from allRecords");
					// allrecords-read-deleted
					readIds.addAll(deletedIds);
					for (GenericEventRecord record : allRecords) {
						if (!readIds.contains(record.getUuid())) {
							NotificationVO vo = new NotificationVO();
							vo.setDateTime(record.getTime());
							vo.setEventType(record.getEventType());
							vo.setChangeLogs(record.getChangeLogs());
							vo.setId(record.getUuid());
							vo.setIsRead("false");
							vo.setMessage(record.getMessage());
							results.add(vo);
						}
					}
				} else {
					// distinct(allrecords+read-deleted)
					LOGGER.debug("removing deleted records from distinct of allRecords and readRecords");
					for (GenericEventRecord record : allRecords) {
						if (!readIds.contains(record.getUuid()) && !deletedIds.contains(record.getUuid())) {
							NotificationVO vo = new NotificationVO();
							vo.setDateTime(record.getTime());
							vo.setEventType(record.getEventType());
							vo.setChangeLogs(record.getChangeLogs());
							vo.setId(record.getUuid());
							vo.setIsRead("false");
							vo.setMessage(record.getMessage());
							results.add(vo);
						}
					}
					for (GenericEventRecord record : readRecords) {
						if (!deletedIds.contains(record.getUuid())) {
							NotificationVO vo = new NotificationVO();
							vo.setDateTime(record.getTime());
							vo.setEventType(record.getEventType());
							vo.setChangeLogs(record.getChangeLogs());
							vo.setId(record.getUuid());
							vo.setIsRead("true");
							vo.setMessage(record.getMessage());
							results.add(vo);
						}
					}
				}
			}
			// filtering by eventCategory
			LOGGER.debug("Filtering by eventCategory");
			for (NotificationVO vo : results) {
				if (eventCategory != null && !"".equalsIgnoreCase(eventCategory)) {
					if (vo.getEventType() != null && !"".equalsIgnoreCase(vo.getEventType())
							&& vo.getEventType().equalsIgnoreCase(eventCategory))
						resultsAfterEventCategoryFilter.add(vo);
				} else {
					resultsAfterEventCategoryFilter = results;
				}
			}
			// filtering by searchType
			LOGGER.debug("Filtering by searchTerm");
			for (NotificationVO vo : resultsAfterEventCategoryFilter) {
				if (searchTerm != null && !"".equalsIgnoreCase(searchTerm)) {
					if (vo.getMessage() != null && !"".equalsIgnoreCase(vo.getMessage())
							&& vo.getMessage().toLowerCase().contains(searchTerm.toLowerCase()))
						resultsAfterSearchTermFilter.add(vo);
				} else {
					resultsAfterSearchTermFilter = resultsAfterEventCategoryFilter;
				}
			}
			// sorting by time - latest first
			LOGGER.debug("Sorting by time, latest first");
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
			Comparator<NotificationVO> compareByDateTime = (NotificationVO r1, NotificationVO r2) -> LocalDateTime
					.parse(r1.getDateTime(), formatter).compareTo(LocalDateTime.parse(r2.getDateTime(), formatter));
			Collections.sort(resultsAfterSearchTermFilter, compareByDateTime);
			// applying limit and offset
			if (limit == 0)
				limit = results.size();
			else
				limit = offset + limit;
			if (results != null && !results.isEmpty())
				results = results.subList(offset, limit);
			return results;
		} catch (Exception e) {
			LOGGER.error(
					"Failed while fetching message for user {} for eventCategory {} and readType {} "
							+ "with searchTerm {} and offset {} limit {}",
					userId, eventCategory, readType, searchTerm, offset, limit);
			return null;
		}

	}

	public void markMessage(String type, String user, List<String> messageIds) {
		String topicName = user;
		List<GenericEventRecord> existingRecords = new ArrayList<>();
		LOGGER.debug("Marking messages as {} for user {} and messageIds", type, user, messageIds);
		if ("READ".equalsIgnoreCase(type))
			topicName = user + "_READ";
		if ("DELETE".equalsIgnoreCase(type))
			topicName = user + "_DELETED";
		existingRecords = dynamicConsumer.consumeRecordsFromTopic(Arrays.asList(topicName));
		final List<String> existingRecordIds = existingRecords.stream().map(n -> n.getUuid())
				.collect(Collectors.toList());
		List<String> filteredIds = messageIds.stream().filter(n -> !existingRecordIds.contains(n))
				.collect(Collectors.toList());

		List<GenericEventRecord> messages = dynamicConsumer.consumeRecordsFromTopic(Arrays.asList(user));
		if (messages != null && !messages.isEmpty() && messageIds != null) {

			List<GenericEventRecord> selectedMessages = messages.stream().filter(x -> filteredIds.contains(x.getUuid()))
					.collect(Collectors.toList());

			if (selectedMessages != null && !selectedMessages.isEmpty()) {
				for (GenericEventRecord markedMessage : selectedMessages)
					dynamicProducer.sendMessage(topicName, markedMessage);
			}
		}
	}
}
