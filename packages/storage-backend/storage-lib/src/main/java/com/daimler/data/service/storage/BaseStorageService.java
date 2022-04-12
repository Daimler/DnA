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

package com.daimler.data.service.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.daimler.data.application.auth.UserStore;
import com.daimler.data.application.config.AVScannerClient;
import com.daimler.data.application.config.VaultConfig;
import com.daimler.data.controller.exceptions.GenericMessage;
import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.ErrorDTO;
import com.daimler.data.dto.FileScanDetailsVO;
import com.daimler.data.dto.MinioGenericResponse;
import com.daimler.data.dto.storage.BucketCollectionVO;
import com.daimler.data.dto.storage.BucketObjectResponseVO;
import com.daimler.data.dto.storage.BucketObjectResponseWrapperVO;
import com.daimler.data.dto.storage.BucketResponseVO;
import com.daimler.data.dto.storage.BucketResponseWrapperVO;
import com.daimler.data.dto.storage.BucketVo;
import com.daimler.data.dto.storage.PermissionVO;
import com.daimler.data.dto.storage.UserRefreshWrapperVO;
import com.daimler.data.dto.storage.UserVO;
import com.daimler.data.minio.client.DnaMinioClient;
import com.daimler.data.util.CacheUtil;
import com.daimler.data.util.ConstantsUtility;

import io.minio.admin.UserInfo;
import io.minio.messages.Bucket;

@Service
public class BaseStorageService implements StorageService {

	private static Logger LOGGER = LoggerFactory.getLogger(BaseStorageService.class);

	@Value("${minio.endpoint}")
	private String minioBaseUri;
	
	@Autowired
	private CacheUtil cacheUtil;
	
	@Autowired
	private UserStore userStore;

	@Autowired
	private DnaMinioClient dnaMinioClient;
	
	@Autowired
	private VaultConfig vaultConfig;

	@Value("${dna.feature.attachmentMalwareScan}")
	private Boolean attachmentMalwareScan;
	
	@Autowired
	private AVScannerClient aVScannerClient;
	
	public BaseStorageService() {
		super();
	}

	@Override
	public ResponseEntity<BucketResponseWrapperVO> createBucket(BucketVo bucketVo) {
		BucketResponseWrapperVO responseVO = new BucketResponseWrapperVO();
		HttpStatus httpStatus;

		LOGGER.debug("Fetching Current user.");
		String currentUser = userStore.getUserInfo().getId();
		PermissionVO permissionVO = null;

		LOGGER.debug("Validate Bucket before create.");
		List<MessageDescription> validateMsg = validateCreateBucket(bucketVo);
		if (!ObjectUtils.isEmpty(validateMsg)) {
			responseVO.setStatus(ConstantsUtility.FAILURE);
			responseVO.setErrors(validateMsg);
			httpStatus = HttpStatus.BAD_REQUEST;
		} else {
			LOGGER.debug("Make bucket:{} request for user:{}", bucketVo.getBucketName(), currentUser);
			MinioGenericResponse createBucketResponse = dnaMinioClient.createBucket(bucketVo.getBucketName());
			if (createBucketResponse != null && createBucketResponse.getStatus().equals(ConstantsUtility.SUCCESS)) {
				LOGGER.info("Success from make minio bucket");
				responseVO.setStatus(ConstantsUtility.SUCCESS);
				LOGGER.info("Onboarding current user:{}", currentUser);
				MinioGenericResponse onboardOwnerResponse = dnaMinioClient.onboardUserMinio(currentUser,
						createBucketResponse.getPolicies());
				if (onboardOwnerResponse != null && onboardOwnerResponse.getStatus().equals(ConstantsUtility.SUCCESS)) {
					LOGGER.info("Onboard bucket owner:{} successfull.", currentUser);
					// Setting R/W access for owner
					permissionVO = new PermissionVO();
					permissionVO.setRead(true);
					permissionVO.setWrite(true);
					
					UserVO ownerUserVO = onboardOwnerResponse.getUser();
					//Setting permission
					ownerUserVO.setPermission(permissionVO);
					//Setting uri 
					//TODO need to fetch from MinioClient
					ownerUserVO.setUri(minioBaseUri+"/"+"buckets/"+bucketVo.getBucketName());
					
					//Setting bucket access info for owner
					responseVO.setBucketAccessinfo(ownerUserVO);					
					
					LOGGER.info("Onboarding collaborators");
					if (!ObjectUtils.isEmpty(bucketVo.getCollaborators())) {
						for (UserVO userVO : bucketVo.getCollaborators()) {
							if (Objects.nonNull(userVO.getPermission())) {
								List<String> policies = new ArrayList<String>();
								if (userVO.getPermission().isRead() != null && userVO.getPermission().isRead()) {
									LOGGER.debug("Setting READ access.");
									policies.add(bucketVo.getBucketName() + "_" + ConstantsUtility.READ);
								}
								if (userVO.getPermission().isWrite() && userVO.getPermission().isWrite()) {
									LOGGER.debug("Setting READ/WRITE access.");
									policies.add(bucketVo.getBucketName() + "_" + ConstantsUtility.READWRITE);
								}

								LOGGER.info("Onboarding collaborator:{}", userVO.getAccesskey());
								MinioGenericResponse onboardUserResponse = dnaMinioClient
										.onboardUserMinio(userVO.getAccesskey().toUpperCase(), policies);
								if (onboardUserResponse != null
										&& onboardUserResponse.getStatus().equals(ConstantsUtility.SUCCESS)) {
									LOGGER.info("Collaborator:{} onboarding successfull", userVO.getAccesskey());
								} else {
									LOGGER.info("Collaborator:{} onboarding failed", userVO.getAccesskey());
								}
							} else {
								LOGGER.info("Collaborator:{} onboarding not possible since permission is not given.",
										userVO.getAccesskey());
							}
						}
					}

				} else {
					LOGGER.info("Failure from onboard bucket owner.");
				}

				responseVO.setStatus(createBucketResponse.getStatus());
				httpStatus = HttpStatus.OK;
			} else {
				LOGGER.info("Failure from make bucket minio client");
				responseVO.setStatus(ConstantsUtility.FAILURE);
				httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
				responseVO.setErrors(getMessages(createBucketResponse.getErrors()));
			}
		}

		responseVO.setData(bucketVo);
		return new ResponseEntity<>(responseVO, httpStatus);
	}

	/*
	 * To validate create bucket.
	 * 
	 */
	private List<MessageDescription> validateCreateBucket(BucketVo bucketVo) {
		List<MessageDescription> messages = new ArrayList<MessageDescription>();
		MessageDescription message = null;
		LOGGER.debug("Check if bucket already exists.");
		Boolean isBucketExists = dnaMinioClient.isBucketExists(bucketVo.getBucketName());
		if (isBucketExists == null) {
			message = new MessageDescription();
			message.setMessage("Error occurred while validating bucket: " + bucketVo.getBucketName());
			messages.add(message);
		} else if (isBucketExists) {
			LOGGER.info("Bucket already exists: {}", bucketVo.getBucketName());
			message = new MessageDescription();
			message.setMessage("Bucket already exists: " + bucketVo.getBucketName());
			messages.add(message);
		} else {
			LOGGER.info("Bucket not exists proceed to make new bucket.");
		}

		return messages;
	}
	
	/*
	 * To validate update bucket.
	 * 
	 */
	private List<MessageDescription> validateUpdateBucket(BucketVo bucketVo) {
		List<MessageDescription> messages = new ArrayList<MessageDescription>();
		MessageDescription message = null;
		LOGGER.debug("Check if bucket already exists.");
		Boolean isBucketExists = dnaMinioClient.isBucketExists(bucketVo.getBucketName());
		if (isBucketExists == null) {
			message = new MessageDescription();
			message.setMessage("Error occurred while validating bucket: " + bucketVo.getBucketName());
			messages.add(message);
		} else if (!isBucketExists) {
			LOGGER.info("Bucket:{} not found.", bucketVo.getBucketName());
			message = new MessageDescription();
			message.setMessage("Bucket:{} "+bucketVo.getBucketName()+ "not found");
			messages.add(message);
		} else {
			LOGGER.info("Bucket:{} exists.",bucketVo.getBucketName());
		}

		return messages;
	}
	
	@Override
	public ResponseEntity<BucketCollectionVO> getAllBuckets() {
		LOGGER.debug("Fetching Current user.");
		String currentUser = userStore.getUserInfo().getId();
		HttpStatus httpStatus;

		BucketCollectionVO bucketCollectionVO = new BucketCollectionVO();
		LOGGER.debug("list buckets for user:{}", currentUser);
		MinioGenericResponse minioResponse = dnaMinioClient.getAllBuckets(currentUser);
		if (minioResponse != null && minioResponse.getStatus().equals(ConstantsUtility.SUCCESS)) {
			LOGGER.info("Success from list buckets minio client");
			httpStatus = minioResponse.getHttpStatus();
			List<BucketResponseVO> bucketsResponseVO = new ArrayList<BucketResponseVO>();
			BucketResponseVO bucketResponseVO = null;
			if(!ObjectUtils.isEmpty(minioResponse.getBuckets())) {
				for (Bucket bucket : minioResponse.getBuckets()) {
					bucketResponseVO = new BucketResponseVO();
					bucketResponseVO.setBucketName(bucket.name());
					bucketResponseVO.setCreationDate(bucket.creationDate().toString());
					// Setting current user permission for bucket
					bucketResponseVO.setPermission(dnaMinioClient.getBucketPermission(bucket.name(), currentUser));
					LOGGER.debug("Setting collaborators for bucket:{}", bucket.name());
					bucketResponseVO.setCollaborators(dnaMinioClient.getBucketCollaborators(bucket.name(), currentUser));

					bucketsResponseVO.add(bucketResponseVO);
				}
				bucketCollectionVO.setData(bucketsResponseVO);
			}
		} else {
			LOGGER.info("Failure from list buckets minio client");
			httpStatus = minioResponse.getHttpStatus();
			bucketCollectionVO.setErrors(getMessages(minioResponse.getErrors()));
		}
		return new ResponseEntity<>(bucketCollectionVO, httpStatus);
	}

	@Override
	public ResponseEntity<BucketObjectResponseWrapperVO> getBucketObjects(String bucketName, String prefix) {
		LOGGER.debug("Fetching Current user.");
		String currentUser = userStore.getUserInfo().getId();
		HttpStatus httpStatus;
		BucketObjectResponseWrapperVO objectResponseWrapperVO = new BucketObjectResponseWrapperVO();

		LOGGER.debug("list bucket objects through minio client");
		MinioGenericResponse minioObjectResponse = dnaMinioClient.getBucketObjects(currentUser, bucketName, prefix);
		if (minioObjectResponse != null && minioObjectResponse.getStatus().equals(ConstantsUtility.SUCCESS)) {
			LOGGER.info("Success from list objects minio client");
			BucketObjectResponseVO bucketObjectResponseVO = new BucketObjectResponseVO();
			//setting Bucket's object response from minio
			bucketObjectResponseVO.setBucketObjects(minioObjectResponse.getObjects());
			
			LOGGER.info("Fetching bucket:{} permission for user:{}",bucketName,currentUser);
			bucketObjectResponseVO.setBucketPermission(dnaMinioClient.getBucketPermission(bucketName, currentUser));
			
			objectResponseWrapperVO.setData(bucketObjectResponseVO);
			httpStatus = minioObjectResponse.getHttpStatus();

		} else {
			LOGGER.info("Failure from list objects minio client");
			objectResponseWrapperVO
					.setErrors(getMessages(minioObjectResponse.getErrors()));
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		return new ResponseEntity<>(objectResponseWrapperVO, httpStatus);
	}

	@Override
	public ResponseEntity<ByteArrayResource> getObjectContent(String bucketName, String prefix) {
		String currentUser = userStore.getUserInfo().getId();
		HttpStatus httpStatus;
		
		/* work in progress*/
		// ObjectMetadataWrapperVO objectMetadataWrapperVO = new
		// ObjectMetadataWrapperVO();

		LOGGER.debug("fetch object/file content through minio client");
		MinioGenericResponse minioResponse = dnaMinioClient.getObjectContents(currentUser, bucketName, prefix);

		if (minioResponse != null && minioResponse.getStatus().equals(ConstantsUtility.SUCCESS)) {
			if (Objects.nonNull(minioResponse.getObjectMetadata().getObjectContent())) {
				LOGGER.info("Success from get object minio client");
				ByteArrayResource resource = minioResponse.getObjectMetadata().getObjectContent();
				return ResponseEntity.ok().contentLength(resource.contentLength()).contentType(contentType(prefix))
						.header("Content-disposition", "attachment; filename=\"" + fileName(prefix) + "\"")
						.body(resource);

				/* work in progress*/
				// objectMetadataWrapperVO.setData(minioResponse.getData());
				// httpStatus = HttpStatus.OK;
			} else {
				LOGGER.info("No content available.");
				httpStatus = HttpStatus.NO_CONTENT;
			}

		} else {
			LOGGER.info("Failure from get object minio client");
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			
			/* work in progress*/
			// objectMetadataWrapperVO.setErrors(Arrays.asList(new
			// MessageDescription(minioResponse.getMessage())));
		}

		return new ResponseEntity<>(null, httpStatus);
	}

	/*
	 * fetching filename from given prefix
	 * 
	 */
	private String fileName(String prefix) {
		String[] arr = prefix.split("/");
		String fileName = arr[arr.length - 1];
		return fileName;
	}

	/*
	 * Setting media type with file extension
	 * 
	 */
	private MediaType contentType(String fileName) {
		String[] arr = fileName.split("\\.");
		String type = arr[arr.length - 1];
		switch (type) {
		case "txt":
			return MediaType.TEXT_PLAIN;
		case "png":
			return MediaType.IMAGE_PNG;
		case "jpg":
			return MediaType.IMAGE_JPEG;
		default:
			return MediaType.APPLICATION_OCTET_STREAM;
		}
	}

	@Override
	public ResponseEntity<BucketResponseWrapperVO> objectUpload(MultipartFile uploadfile, String bucketName,
			String prefix) {
		LOGGER.debug("Fetching Current user.");
		String currentUser = userStore.getUserInfo().getId();
		HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
		boolean proceedToUpload = true;
		BucketResponseWrapperVO bucketResponseWrapperVO = new BucketResponseWrapperVO();

		//Checking for feature malware scan
		if (Boolean.TRUE.equals(attachmentMalwareScan)) {
			LOGGER.debug("Scanning for malware for file {}", uploadfile.getName());
			FileScanDetailsVO fileScanDetailsVO = this.scan(uploadfile);
			if (Objects.nonNull(fileScanDetailsVO) && Boolean.TRUE.equals(fileScanDetailsVO.getDetected())) {
				LOGGER.info("File:{} is infected with malware.", uploadfile.getName());
				//setting upload as false
				proceedToUpload = false;
				bucketResponseWrapperVO.setErrors(Arrays
						.asList(new MessageDescription("File:" + uploadfile.getName() + " is infected with malware.")));
			} else if (Objects.isNull(fileScanDetailsVO) || StringUtils.hasText(fileScanDetailsVO.getErrorMessage())) {
				LOGGER.info("Failed to scan file:{}", uploadfile.getName());
				//setting upload as false
				proceedToUpload = false;
				bucketResponseWrapperVO.setErrors(
						Arrays.asList(new MessageDescription("Failed to scan file:" + uploadfile.getName())));
			}
		}

		if (proceedToUpload) {
			LOGGER.debug("upload object/file through minio client.");
			MinioGenericResponse minioResponse = dnaMinioClient.objectUpload(currentUser, uploadfile, bucketName,
					prefix);
			if (minioResponse.getStatus().equals(ConstantsUtility.SUCCESS)) {
				LOGGER.info("Success from put object minio client.");
				httpStatus = HttpStatus.OK;
			} else {
				LOGGER.info("Failure from put object minio client.");
				bucketResponseWrapperVO.setErrors(getMessages(minioResponse.getErrors()));
				httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			}
			bucketResponseWrapperVO.setStatus(minioResponse.getStatus());
		}
		return new ResponseEntity<>(bucketResponseWrapperVO, httpStatus);
	}

	@Override
	public ResponseEntity<UserRefreshWrapperVO> userRefresh(String userId) {
		HttpStatus httpStatus;
		UserRefreshWrapperVO userRefreshWrapperVO = new UserRefreshWrapperVO();

		LOGGER.debug("Fetching Current user.");
		String currentUser = userStore.getUserInfo().getId();

		// Setting current user as user Id if userId is null
		userId = StringUtils.hasText(userId) ? userId.toUpperCase() : currentUser;

		if (!userId.equals(currentUser) && !userStore.getUserInfo().hasAdminAccess()) {
			LOGGER.info("No permission to refresh user:{}, only owner or admin can refresh", userId);
			userRefreshWrapperVO.setErrors(Arrays.asList(new MessageDescription(
					"No permission to refresh user:" + userId + ", only owner or admin can refresh")));
			httpStatus = HttpStatus.FORBIDDEN;
			userRefreshWrapperVO.setStatus(ConstantsUtility.FAILURE);
		} else {

			LOGGER.debug("Refresh user through minio client.");
			MinioGenericResponse minioResponse = dnaMinioClient.userRefresh(userId.toUpperCase());
			if (minioResponse != null && minioResponse.getStatus().equals(ConstantsUtility.SUCCESS)) {
				LOGGER.info("Success from refresh minio client.");
				httpStatus = HttpStatus.OK;
				userRefreshWrapperVO.setData(minioResponse.getUser());
			} else {
				LOGGER.info("Failure from refresh minio client.");
				userRefreshWrapperVO
						.setErrors(getMessages(minioResponse.getErrors()));
				httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			}
			userRefreshWrapperVO.setStatus(minioResponse.getStatus());
		}

		return new ResponseEntity<>(userRefreshWrapperVO, httpStatus);
	}

	@Override
	public ResponseEntity<UserRefreshWrapperVO> getConnection(String bucketName, String userId, String prefix) {
		HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
		UserRefreshWrapperVO userRefreshWrapperVO = new UserRefreshWrapperVO();
		userRefreshWrapperVO.setStatus(ConstantsUtility.FAILURE);

		LOGGER.debug("Fetching Current user.");
		String currentUser = userStore.getUserInfo().getId();

		// Setting current user as user Id if userId is null
		userId = StringUtils.hasText(userId) ? userId.toUpperCase() : currentUser;
		if (!userId.equals(currentUser) && !userStore.getUserInfo().hasAdminAccess()) {
			LOGGER.info(
					"No permission to get Connection details for user:{}, only owner or admin can get connection details.",
					userId);
			userRefreshWrapperVO
					.setErrors(Arrays.asList(new MessageDescription("No permission to get Connection details for user:"
							+ userId + ", only owner or admin can get connection details.")));
			httpStatus = HttpStatus.FORBIDDEN;
		} else if (!dnaMinioClient.validateUserInMinio(userId)) {
			LOGGER.info("User:{} not present in Minio.", userId);
			userRefreshWrapperVO
					.setErrors(Arrays.asList(new MessageDescription("User:" + userId + " not present in Minio.")));
			httpStatus = HttpStatus.NO_CONTENT;
		} else {
			String secretKey = vaultConfig.validateUserInVault(userId);
			if (StringUtils.hasText(secretKey)) {
				UserVO userVO = new UserVO();
				//setting credentials
				userVO.setAccesskey(userId);
				userVO.setSecretKey(secretKey);
				//Setting permission
				userVO.setPermission(dnaMinioClient.getBucketPermission(bucketName, userId));
				String uri = minioBaseUri+"/"+"buckets/"+bucketName;
				userVO.setUri(uri);
				
				userRefreshWrapperVO.setData(userVO);
				userRefreshWrapperVO.setStatus(ConstantsUtility.SUCCESS);
				httpStatus = HttpStatus.OK;
				

			} else {
				LOGGER.info("User:{} not present in Vault.", userId);
				userRefreshWrapperVO
						.setErrors(Arrays.asList(new MessageDescription("User:" + userId + " not present in Vault.")));
				httpStatus = HttpStatus.NO_CONTENT;
			}
		}

		return new ResponseEntity<>(userRefreshWrapperVO, httpStatus);
	}

	@Override
	public ResponseEntity<GenericMessage> cacheRefresh() {
		GenericMessage genericMessage = new GenericMessage();
		HttpStatus httpStatus;
		LOGGER.debug("Fetching users from Minio.");
		Map<String, UserInfo> users = dnaMinioClient.listUsers();
		if (users.isEmpty()) {
			genericMessage.setSuccess(ConstantsUtility.FAILURE);
			genericMessage.setErrors(
					Arrays.asList(new MessageDescription("Cache refresh failed as no data got from Minio.")));
			httpStatus = HttpStatus.NOT_FOUND;
		} else {
			// updating minioUsersCache
			LOGGER.debug("Removing all enteries from minioUsersCache.");
			cacheUtil.removeAll(ConstantsUtility.MINIO_USERS_CACHE);
			LOGGER.debug("Updating minioUsersCache.");
			cacheUtil.updateCache(ConstantsUtility.MINIO_USERS_CACHE, users);

			genericMessage.setSuccess(ConstantsUtility.SUCCESS);
			httpStatus = HttpStatus.OK;
		}

		return new ResponseEntity<>(genericMessage, httpStatus);
	}

	@Override
	public ResponseEntity<GenericMessage> deleteBucketObjects(String bucketName, String prefix) {
		GenericMessage genericMessage = new GenericMessage();
		HttpStatus httpStatus;
		
		LOGGER.debug("Fetching Current user.");
		String currentUser = userStore.getUserInfo().getId();
		
		MinioGenericResponse minioResponse = dnaMinioClient.removeObjects(currentUser, bucketName, prefix);
		if (minioResponse != null && minioResponse.getStatus().equals(ConstantsUtility.SUCCESS)) {
			LOGGER.info("Success from minio remove objects.");
			genericMessage.setSuccess(ConstantsUtility.SUCCESS);
			httpStatus = HttpStatus.OK;
		} else {
			LOGGER.info("Failure from minio remove objects.");
			genericMessage.setSuccess(ConstantsUtility.FAILURE);
			genericMessage
					.setErrors(getMessages(minioResponse.getErrors()));
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<>(genericMessage, httpStatus);
	}

	@Override
	public ResponseEntity<GenericMessage> deleteBucket(String bucketName) {
		GenericMessage genericMessage = new GenericMessage();
		HttpStatus httpStatus;
		
		LOGGER.debug("Fetching Current user.");
		String currentUser = userStore.getUserInfo().getId();
		
		LOGGER.info("Removing bucket:{}",bucketName);
		MinioGenericResponse minioResponse = dnaMinioClient.removeBucket(currentUser, bucketName);
		if (minioResponse != null && minioResponse.getStatus().equals(ConstantsUtility.SUCCESS)) {
			LOGGER.info("Success from minio remove bucket.");
			genericMessage.setSuccess(ConstantsUtility.SUCCESS);
			httpStatus = HttpStatus.OK;
		} else {
			LOGGER.info("Failure from minio remove bucket.");
			genericMessage.setSuccess(ConstantsUtility.FAILURE);
			genericMessage
					.setErrors(getMessages(minioResponse.getErrors()));
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<>(genericMessage, httpStatus);
	}
	
	/*
	 * To convert List<Error> errors to List<MessageDescription> 
	 * 
	 */
	private List<MessageDescription> getMessages(List<ErrorDTO> errors){
		List<MessageDescription> messages = null;
		if(!ObjectUtils.isEmpty(errors)) {
			messages = new ArrayList<MessageDescription>();
			for(ErrorDTO error:errors) {
				messages.add(new MessageDescription(error.getErrorMsg()));
			}
		}
		return messages;
	}

	@Override
	public ResponseEntity<BucketResponseWrapperVO> updateBucket(BucketVo bucketVo) {
		BucketResponseWrapperVO responseVO = new BucketResponseWrapperVO();
		HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

		LOGGER.debug("Fetching Current user.");
		String currentUser = userStore.getUserInfo().getId();

		LOGGER.debug("Validate Bucket before update.");
		List<MessageDescription> errors = validateUpdateBucket(bucketVo);
		if (!ObjectUtils.isEmpty(errors)) {
			responseVO.setStatus(ConstantsUtility.FAILURE);
			responseVO.setErrors(errors);
			httpStatus = HttpStatus.BAD_REQUEST;
		} else {
			LOGGER.info("Fetching existing collaborators for bucket:{}", bucketVo.getBucketName());
			List<UserVO> existingCollaborators = dnaMinioClient.getBucketCollaborators(bucketVo.getBucketName(),
					currentUser);

			// To update collaborators list
			errors = updateBucketCollaborator(bucketVo.getBucketName(), existingCollaborators,
					bucketVo.getCollaborators());
			if (ObjectUtils.isEmpty(errors)) {
				responseVO.setStatus(ConstantsUtility.SUCCESS);
				httpStatus = HttpStatus.OK;
			} else {
				responseVO.setStatus(ConstantsUtility.FAILURE);
				responseVO.setErrors(errors);
				httpStatus = HttpStatus.BAD_REQUEST;
			}

		}

		responseVO.setData(bucketVo);
		return new ResponseEntity<>(responseVO, httpStatus);
	}

	/*
	 * To get Union of list return list of user by making unison of 2 userVO list
	 */
	private List<String> getUsersUnion(List<UserVO> list1, List<UserVO> list2) {
		// Set<UserVO> set = new HashSet<UserVO>();

		Set<UserVO> set = new TreeSet<>(new Comparator<UserVO>() {
			@Override
			public int compare(UserVO u1, UserVO u2) {
				return u1.getAccesskey().compareTo(u2.getAccesskey());
			}
		});
		// Adding list one to set
		set.addAll(list1);
		// Adding list two to set
		set.addAll(list2);

		// fetching users
		List<String> usersId = set.stream().map(t -> t.getAccesskey()).collect(Collectors.toList());
		return usersId;
	}
	
	/*
	 * update collaborator for bucket by comparing existing and new collaborator
	 * list
	 */
	private List<MessageDescription> updateBucketCollaborator(String bucketName, List<UserVO> existingCollaborators,
			List<UserVO> newCollaborators) {
		List<MessageDescription> errors = new ArrayList<MessageDescription>();
		LOGGER.info("Fetching users from Minio user cache.");
		Map<String, UserInfo> usersInfo = cacheUtil.getMinioUsers(ConstantsUtility.MINIO_USERS_CACHE);
		String readPolicy = bucketName + "_" + ConstantsUtility.READ;
		String readWritePolicy = bucketName + "_" + ConstantsUtility.READWRITE;

		// To get all users list
		List<String> usersId = getUsersUnion(existingCollaborators, newCollaborators);
		for (String userId : usersId) {
			// To get User details from newCollaborators
			Optional<UserVO> newCollaborator = newCollaborators.stream()
					.filter(userVO -> userVO.getAccesskey().equals(userId)).findAny();

			// To get User details from existingCollaborators
			Optional<UserVO> existingCollaborator = existingCollaborators.stream()
					.filter(userVO -> userVO.getAccesskey().equals(userId)).findAny();

			// To get user info from Minio
			UserInfo userInfo = usersInfo.get(userId);
			String policy = "";

			// if user presents in new and existing
			if (newCollaborator.isPresent() && existingCollaborator.isPresent()) {
				PermissionVO permissionVO = newCollaborator.get().getPermission();

				// Getting policy from user
				policy = userInfo.policyName();

				// Checking for read permission
				// if read permission available adding it
				// if read permission not available removing it
				if (permissionVO.isRead()) {
					policy = !policy.contains(readPolicy) ? policy.concat("," + readPolicy) : policy;
				} else {
					policy = policy.contains(readPolicy) ? policy.replace(readPolicy, "") : policy;
				}

				// Checking for read/write permission
				// if read/write permission available adding it
				// if read/write permission not available removing it
				if (permissionVO.isWrite()) {
					policy = !policy.contains(readWritePolicy) ? policy.concat("," + readWritePolicy) : policy;
				} else {
					policy = policy.contains(readWritePolicy) ? policy.replace(readWritePolicy, "") : policy;
				}
				// Setting permission in Minio
				dnaMinioClient.setPolicy(userId, false, policy);

			}
			// If user presents only in new
			else if (newCollaborator.isPresent() && !existingCollaborator.isPresent()) {
				PermissionVO permissionVO = newCollaborator.get().getPermission();
				List<String> policies = new ArrayList<String>();
				// for read permission
				if (permissionVO.isRead()) {
					LOGGER.debug("Setting READ access.");
					policies.add(readPolicy);
				}
				// for write permission
				if (permissionVO.isWrite()) {
					LOGGER.debug("Setting READ/WRITE access.");
					policies.add(readWritePolicy);
				}

				LOGGER.info("Onboarding collaborator:{}", userId);
				MinioGenericResponse onboardUserResponse = dnaMinioClient.onboardUserMinio(userId.toUpperCase(),
						policies);
				if (onboardUserResponse != null && onboardUserResponse.getStatus().equals(ConstantsUtility.SUCCESS)) {
					LOGGER.info("Collaborator:{} onboarding successfull", userId);

				} else {
					LOGGER.info("Collaborator:{} onboarding failed", userId);
					errors = getMessages(onboardUserResponse.getErrors());
					break;
				}
			}
			// If user presents only in existing
			else if (!newCollaborator.isPresent() && existingCollaborator.isPresent()) {
				// Getting policy from user
				policy = userInfo.policyName();

				// Removing read permission
				policy = policy.contains(readPolicy) ? policy.replace(readPolicy, "") : policy;
				// Removing read/write permission
				policy = policy.contains(readWritePolicy) ? policy.replace(readWritePolicy, "") : policy;

				// Setting permission in Minio
				dnaMinioClient.setPolicy(userId, false, policy);

			}
		}
		return errors;
	}
	
	
	@Override
	public ResponseEntity<BucketResponseVO> getByBucketName(String bucketName) {
		HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
		BucketResponseVO bucketResponseVO = new BucketResponseVO();

		List<MessageDescription> errors = new ArrayList<MessageDescription>();
		LOGGER.debug("Check if bucket exists.");
		Boolean isBucketExists = dnaMinioClient.isBucketExists(bucketName);
		if (!isBucketExists) {
			httpStatus = HttpStatus.NOT_FOUND;
			errors.add(new MessageDescription("Bucket not found."));
			//bucketResponseVO.setStatus(ConstantsUtility.FAILURE);
		} else {
			LOGGER.debug("Fetching Current user.");
			String currentUser = userStore.getUserInfo().getId();

			// Setting bucket details
			bucketResponseVO.setBucketName(bucketName);
			bucketResponseVO.setCollaborators(dnaMinioClient.getBucketCollaborators(bucketName, currentUser));
			bucketResponseVO.setPermission(dnaMinioClient.getBucketPermission(bucketName, currentUser));
			
			httpStatus = HttpStatus.OK;
			//responseWrapperVO.setStatus(ConstantsUtility.SUCCESS);
			errors = null;
		}

		//responseWrapperVO.setErrors(errors);
		return new ResponseEntity<>(bucketResponseVO, httpStatus);
	}

	/**
	 * To scan file by calling AVscan service
	 * 
	 * @param multiPartFile
	 * @return FileScanDetailsVO
	 */
	private FileScanDetailsVO scan(MultipartFile multiPartFile) {
		LOGGER.debug("Calling avscan client to scan file:{}",multiPartFile.getName());
		Optional<FileScanDetailsVO> aVScannerRes = aVScannerClient.scan(multiPartFile);
		return aVScannerRes.isPresent()?aVScannerRes.get():null;
	}
	
}
