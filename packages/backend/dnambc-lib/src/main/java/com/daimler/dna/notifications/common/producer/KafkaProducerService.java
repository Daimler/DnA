package com.daimler.dna.notifications.common.producer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daimler.dna.notifications.common.event.config.GenericEventRecord;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KafkaProducerService {

	@Value(value = "${kafka.centralTopic.name}")
	private String topicName;

	@Autowired
	private KafkaDynamicProducerService dynamicProducer;

	@Transactional
	public void send(String eventType, String resourceId, String messageDetails, String publishingUser, String message,
			Boolean mail_required, List<String> subscribedUsers) {
		GenericEventRecord record = this.defaultRecordBuilder(eventType, resourceId, messageDetails, publishingUser,
				message, mail_required, subscribedUsers);
		dynamicProducer.sendMessage(topicName, record);
	}

	private GenericEventRecord defaultRecordBuilder(String eventType, String resourceId, String messageDetails,
			String publishingUser, String message, Boolean mail_required, List<String> subscribedUsers) {
		GenericEventRecord eventRecord = new GenericEventRecord();
		eventRecord.setUuid(UUID.randomUUID().toString());
		eventRecord.setPublishingAppName("DNA");
		eventRecord.setPublishingUser(publishingUser);
		eventRecord.setEventType(eventType);
		eventRecord.setMessage(message);
		eventRecord.setMailRequired(false);
		eventRecord.setResourceId(resourceId);
		eventRecord.setMessageDetails(messageDetails);
		SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		eventRecord.setTime(dateFormatter.format(new Date()));
		eventRecord.setSubscribedUsers(subscribedUsers);
		log.debug("New event record created with detail eventtype : {} " + "publishingUser: {} and subscribers: {}",
				eventType, publishingUser, subscribedUsers);
		return eventRecord;
	}

}
