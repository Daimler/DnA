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

package com.daimler.data.service.dataproduct;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.daimler.data.application.auth.UserStore;
import com.daimler.data.assembler.DataProductAssembler;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.entities.DataProductNsql;
import com.daimler.data.db.repo.dataproduct.DataProductCustomRepository;
import com.daimler.data.db.repo.dataproduct.DataProductRepository;
import com.daimler.data.dto.datacompliance.CreatedByVO;
import com.daimler.data.dto.dataproduct.ChangeLogVO;
import com.daimler.data.dto.dataproduct.ConsumerVO;
import com.daimler.data.dto.dataproduct.DataProductConsumerResponseVO;
import com.daimler.data.dto.dataproduct.DataProductProviderResponseVO;
import com.daimler.data.dto.dataproduct.DataProductVO;
import com.daimler.data.dto.dataproduct.ProviderVO;
import com.daimler.data.dto.dataproduct.TeamMemberVO;
import com.daimler.data.notifications.common.producer.KafkaProducerService;
import com.daimler.data.service.common.BaseCommonService;

@Service
@SuppressWarnings(value = "unused")
public class BaseDataProductService extends BaseCommonService<DataProductVO, DataProductNsql, String>
		implements DataProductService {

	private static Logger LOGGER = LoggerFactory.getLogger(BaseDataProductService.class);

	@Value(value = "${dataproduct.base.url}")
	private String dataProductBaseUrl;

	@Autowired
	private UserStore userStore;

	@Autowired
	private DataProductAssembler dataProductAssembler;

	@Autowired
	private KafkaProducerService kafkaProducer;

	@Autowired
	private DataProductCustomRepository dataProductCustomRepository;

	@Autowired
	private DataProductRepository dataProductRepository;

	public BaseDataProductService() {
		super();
	}

	@Override
	@Transactional
	public DataProductVO create(DataProductVO vo) {
		return super.create(vo);
	}

	@Override
	public List<DataProductVO> getAllWithFilters(Boolean published, int offset, int limit, String sortBy,
			String sortOrder) {
		List<DataProductNsql> dataProductEntities = dataProductCustomRepository
				.getAllWithFiltersUsingNativeQuery(published, offset, limit, sortBy, sortOrder);
		if (!ObjectUtils.isEmpty(dataProductEntities))
			return dataProductEntities.stream().map(n -> dataProductAssembler.toVo(n)).collect(Collectors.toList());
		else
			return new ArrayList<>();
	}

	@Override
	public Long getCount(Boolean published) {
		return dataProductCustomRepository.getCountUsingNativeQuery(published);
	}

	@Override
	@Transactional
	public ResponseEntity<DataProductProviderResponseVO> createDataProductProvider(ProviderVO requestVO) {
		DataProductProviderResponseVO responseVO = new DataProductProviderResponseVO();
		DataProductVO dataProductVO = new DataProductVO();
		try {
			String uniqueProductName = requestVO.getDataProductName();
			DataProductVO existingVO = super.getByUniqueliteral("dataProductName", uniqueProductName);
			if (existingVO != null && existingVO.getProviderInformation() != null && existingVO.getProviderInformation().getDataProductName() != null) {
				responseVO.setData(existingVO.getProviderInformation());
				List<MessageDescription> messages = new ArrayList<>();
				MessageDescription message = new MessageDescription();
				message.setMessage("DataProduct already exists.");
				messages.add(message);
				responseVO.setErrors(messages);
				LOGGER.debug("DataProduct {} already exists, returning as CONFLICT", uniqueProductName);
				return new ResponseEntity<>(responseVO, HttpStatus.CONFLICT);
			}
			requestVO.setCreatedBy(this.userStore.getVO());
			requestVO.setCreatedDate(new Date());
			requestVO.setId(null);

			if (requestVO.isProviderFormSubmitted() == null)
				requestVO.setProviderFormSubmitted(false);
			dataProductVO.setProviderInformation(requestVO);
			DataProductVO vo = this.create(dataProductVO);
			if (vo != null && vo.getProviderInformation().getId() != null) {
				responseVO.setData(vo.getProviderInformation());
				LOGGER.info("DataProduct {} created successfully", uniqueProductName);
				return new ResponseEntity<>(responseVO, HttpStatus.CREATED);
			} else {
				List<MessageDescription> messages = new ArrayList<>();
				MessageDescription message = new MessageDescription();
				message.setMessage("Failed to save due to internal error");
				messages.add(message);
				responseVO.setData(requestVO);
				responseVO.setErrors(messages);
				LOGGER.error("DataProduct {} , failed to create", uniqueProductName);
				return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			LOGGER.error("Exception occurred:{} while creating dataProduct {} ", e.getMessage(),
					requestVO.getDataProductName());
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage(e.getMessage());
			messages.add(message);
			responseVO.setData(requestVO);
			responseVO.setErrors(messages);
			return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	@Transactional
	public ResponseEntity<DataProductProviderResponseVO> updateDataProductProvider(ProviderVO requestVO) {
		DataProductProviderResponseVO responseVO = new DataProductProviderResponseVO();
		DataProductVO dataProductVO = new DataProductVO();
		try {
			String id = requestVO.getId();
			DataProductVO existingVO = this.getById(id);
			DataProductVO mergedVO = null;
			if (requestVO.isProviderFormSubmitted() == null) {
				requestVO.setProviderFormSubmitted(false);
			}
			if (existingVO != null && existingVO.getProviderInformation().getId() != null) {
				CreatedByVO createdBy = existingVO.getProviderInformation().getCreatedBy();
				if (true) {
					requestVO.setCreatedBy(createdBy);
					requestVO.setCreatedDate(existingVO.getProviderInformation().getCreatedDate());
					requestVO.lastModifiedDate(new Date());
					requestVO.setModifiedBy(this.userStore.getVO());
					dataProductVO.setProviderInformation(requestVO);
					dataProductVO.setConsumerInformation(existingVO.getConsumerInformation());
					mergedVO = this.create(dataProductVO);
					if (mergedVO != null && mergedVO.getProviderInformation().getId() != null) {
						responseVO.setData(mergedVO.getProviderInformation());
						responseVO.setErrors(null);
						LOGGER.info("DataProduct with id {} updated successfully", id);
						this.publishEventMessages(existingVO, mergedVO);
						return new ResponseEntity<>(responseVO, HttpStatus.OK);
					} else {
						List<MessageDescription> messages = new ArrayList<>();
						MessageDescription message = new MessageDescription();
						message.setMessage("Failed to update due to internal error");
						messages.add(message);
						responseVO.setData(requestVO);
						responseVO.setErrors(messages);
						LOGGER.debug("DataProduct with id {} cannot be edited. Failed with unknown internal error", id);
						return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
					}
				} else {
					List<MessageDescription> notAuthorizedMsgs = new ArrayList<>();
					MessageDescription notAuthorizedMsg = new MessageDescription();
					notAuthorizedMsg.setMessage(
							"Not authorized to edit dataProduct. Only user who created the dataProduct or with admin role can edit.");
					notAuthorizedMsgs.add(notAuthorizedMsg);
					responseVO.setErrors(notAuthorizedMsgs);
					LOGGER.debug("DataProduct with id {} cannot be edited. User not authorized", id);
					return new ResponseEntity<>(responseVO, HttpStatus.FORBIDDEN);
				}
			} else {
				List<MessageDescription> notFoundmessages = new ArrayList<>();
				MessageDescription notFoundmessage = new MessageDescription();
				notFoundmessage.setMessage("No dataProduct found for given id. Update cannot happen");
				notFoundmessages.add(notFoundmessage);
				responseVO.setErrors(notFoundmessages);
				LOGGER.debug("No dataProduct found for given id {} , update cannot happen.", id);
				return new ResponseEntity<>(responseVO, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			LOGGER.error("DataProduct with id {} cannot be edited. Failed due to internal error {} ", requestVO.getId(),
					e.getMessage());
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage("Failed to update due to internal error. " + e.getMessage());
			messages.add(message);
			responseVO.setData(requestVO);
			responseVO.setErrors(messages);
			return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@Override
	@Transactional
	public ResponseEntity<DataProductConsumerResponseVO> updateDataProductConsumer(ConsumerVO requestVO) {
		DataProductConsumerResponseVO responseVO = new DataProductConsumerResponseVO();
		DataProductVO dataProductVO = new DataProductVO();
		try {
			String id = requestVO.getId();
			DataProductVO existingVO = this.getById(id);
			DataProductVO mergedVO = null;
			if (requestVO.isPublish() == null) {
				requestVO.setPublish(false);
			}
			if (existingVO != null && existingVO.getProviderInformation().getId() != null) {
				if (true) {
					if(existingVO.getConsumerInformation() == null) {
						requestVO.setCreatedBy(this.userStore.getVO());
						requestVO.setCreatedDate(new Date());
					}else {
						CreatedByVO createdBy = existingVO.getConsumerInformation().getCreatedBy();
						requestVO.setCreatedBy(createdBy);
						requestVO.setCreatedDate(existingVO.getConsumerInformation().getCreatedDate());
						requestVO.lastModifiedDate(new Date());
						requestVO.setModifiedBy(this.userStore.getVO());
					}
					dataProductVO.setConsumerInformation(requestVO);
					dataProductVO.setProviderInformation(existingVO.getProviderInformation());
					mergedVO = this.create(dataProductVO);
					if (mergedVO != null && mergedVO.getConsumerInformation().getId() != null) {
						responseVO.setData(mergedVO.getConsumerInformation());
						responseVO.setErrors(null);
						LOGGER.info("DataProduct with id {} updated successfully", id);
						this.publishEventMessages(existingVO, mergedVO);
						return new ResponseEntity<>(responseVO, HttpStatus.OK);
					} else {
						List<MessageDescription> messages = new ArrayList<>();
						MessageDescription message = new MessageDescription();
						message.setMessage("Failed to update due to internal error");
						messages.add(message);
						responseVO.setData(requestVO);
						responseVO.setErrors(messages);
						LOGGER.debug("DataProduct with id {} cannot be edited. Failed with unknown internal error", id);
						return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
					}
				} else {
					List<MessageDescription> notAuthorizedMsgs = new ArrayList<>();
					MessageDescription notAuthorizedMsg = new MessageDescription();
					notAuthorizedMsg.setMessage(
							"Not authorized to edit dataProduct. Only user who created the dataProduct or with admin role can edit.");
					notAuthorizedMsgs.add(notAuthorizedMsg);
					responseVO.setErrors(notAuthorizedMsgs);
					LOGGER.debug("DataProduct with id {} cannot be edited. User not authorized", id);
					return new ResponseEntity<>(responseVO, HttpStatus.FORBIDDEN);
				}
			} else {
				List<MessageDescription> notFoundmessages = new ArrayList<>();
				MessageDescription notFoundmessage = new MessageDescription();
				notFoundmessage.setMessage("No dataProduct found for given id. Update cannot happen");
				notFoundmessages.add(notFoundmessage);
				responseVO.setErrors(notFoundmessages);
				LOGGER.debug("No dataProduct found for given id {} , update cannot happen.", id);
				return new ResponseEntity<>(responseVO, HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			LOGGER.error("DataProduct with id {} cannot be edited. Failed due to internal error {} ", requestVO.getId(),
					e.getMessage());
			List<MessageDescription> messages = new ArrayList<>();
			MessageDescription message = new MessageDescription();
			message.setMessage("Failed to update due to internal error. " + e.getMessage());
			messages.add(message);
			responseVO.setData(requestVO);
			responseVO.setErrors(messages);
			return new ResponseEntity<>(responseVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	private void publishEventMessages(DataProductVO prevDataProductVO, DataProductVO currDataProductVO) {
		try {
			ProviderVO currProviderVO = currDataProductVO.getProviderInformation();
			ConsumerVO currConsumerVO = currDataProductVO.getConsumerInformation();
			ConsumerVO prevConsumerVO = prevDataProductVO.getConsumerInformation();
			if (currProviderVO.isNotifyUsers()) {
				CreatedByVO currentUser = this.userStore.getVO();
				String resourceID = currProviderVO.getId();
				String dataProductName = currProviderVO.getDataProductName();
				String eventType = "";
				String eventMessage = "";
				String userName = super.currentUserName(currentUser);
				String userId = currentUser != null ? currentUser.getId() : "dna_system";
				List<ChangeLogVO> changeLogs = new ArrayList<>();
				List<String> teamMembers = new ArrayList<>();
				List<String> teamMembersEmails = new ArrayList<>();
				if (!ObjectUtils.isEmpty(currProviderVO.getUsers())) {
					for (TeamMemberVO user : currProviderVO.getUsers()) {
						if (user != null) {
							String shortId = user.getShortId();
							if (StringUtils.hasText(shortId) && !teamMembers.contains(shortId)) {
								teamMembers.add(shortId);
							}
							String emailId = user.getEmail();
							if (StringUtils.hasText(emailId) && !teamMembersEmails.contains(emailId)) {
								teamMembersEmails.add(emailId);
							}
						}
					}
				}

				if (currProviderVO.getCreatedBy() != null) {
					String providerUserId = currProviderVO.getCreatedBy().getId();
					String providerEmailId = currProviderVO.getCreatedBy().getEmail();

					if (StringUtils.hasText(providerUserId)) {
						teamMembers.add(providerUserId);
					}

					if (StringUtils.hasText(providerEmailId)) {
						teamMembersEmails.add(providerEmailId);
					}
				}

				if (!prevConsumerVO.isPublish() && currConsumerVO.isPublish()) {
					eventType = "DataProduct - Consumer form Published";
					// teamMembers.remove(publishingUserId);
					teamMembersEmails.remove(0);
					eventMessage = "A Minimum Information Documentation data transfer is complete. [view]("
							+ dataProductBaseUrl + "summary/" + resourceID + ")";
					LOGGER.info("Publishing message on consumer form submission for dataProduct {} by userId {}",
							dataProductName, userId);

				} else if (prevConsumerVO.isPublish() && currConsumerVO.isPublish()) {
					eventType = "DataProduct_Update";
					eventMessage = "DataProduct " + dataProductName + " is updated by user " + userName;
					changeLogs = dataProductAssembler.jsonObjectCompare(currDataProductVO, prevDataProductVO,
							currentUser);
					LOGGER.info("Publishing message on update for dataProduct {} by userId {}", dataProductName,
							userId);

				} else if (!ObjectUtils.isEmpty(currProviderVO.getUsers())) {
					eventType = "DataProduct - Provider Form Submitted";
					eventMessage = "A Minimum Information Documentation is ready for you. Please [provide information]("
							+ dataProductBaseUrl + "consume/" + resourceID + ")"
							+ " about the receiving side to finalise the Data Transfer.";
					LOGGER.info("Publishing message on provider form submission for dataProduct {} by userId {}",
							dataProductName, userId);
				}
				if (StringUtils.hasText(eventType)) {
					kafkaProducer.send(eventType, resourceID, "", userId, eventMessage, true, teamMembers,
							teamMembersEmails, changeLogs);
					LOGGER.info("Published successfully event {} for data product {}", eventType, dataProductName);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Failed while publishing dataProduct event msg {} ", e.getMessage());
		}
	}

	@Override
	@Transactional
	public ResponseEntity<GenericMessage> deleteDataProduct(String id) {
		try {
			DataProductVO dataProduct = this.getById(id);
			if (true) {
				this.deleteById(id);
				GenericMessage successMsg = new GenericMessage();
				successMsg.setSuccess("success");
				LOGGER.info("DataProduct with id {} deleted successfully", id);
				return new ResponseEntity<>(successMsg, HttpStatus.OK);
			} else {
				MessageDescription notAuthorizedMsg = new MessageDescription();
				notAuthorizedMsg.setMessage(
						"Not authorized to delete dataProduct. Only the dataProduct owner or an admin can delete the dataProduct.");
				GenericMessage errorMessage = new GenericMessage();
				errorMessage.addErrors(notAuthorizedMsg);
				LOGGER.debug("DataProduct with id {} cannot be deleted. User not authorized", id);
				return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
			}
		} catch (EntityNotFoundException e) {
			MessageDescription invalidMsg = new MessageDescription("No dataProduct with the given id");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(invalidMsg);
			LOGGER.error("No dataProduct with the given id {} , could not delete.", id);
			return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
		} catch (Exception e) {
			MessageDescription exceptionMsg = new MessageDescription("Failed to delete due to internal error.");
			GenericMessage errorMessage = new GenericMessage();
			errorMessage.addErrors(exceptionMsg);
			LOGGER.error("Failed to delete dataProduct with id {} , due to internal error.", id);
			return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
