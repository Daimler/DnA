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

package com.daimler.data.minio.client;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.daimler.data.dto.MinioGenericResponse;

public interface DnaMinioClient {

	/**
	 * To make a new bucket
	 * 
	 * @param bucketName
	 * @return MinioGenericResponse
	 */
	public MinioGenericResponse createBucket(String bucketName);

	/**
	 * To onboard an user in minio and assign policies
	 * 
	 * @param userId
	 * @param policies
	 * @return MinioGenericResponse
	 */
	public MinioGenericResponse onboardUserMinio(String userId, List<String> policies);

	/**
	 * To list all the buckets for user
	 * 
	 * @param userId
	 * @return MinioGenericResponse
	 */
	public MinioGenericResponse getAllBuckets(String userId);

	/**
	 * To list all the objects inside buckets
	 * 
	 * @param userId
	 * @param bucketName
	 * @param prefix
	 * @return MinioGenericResponse
	 */
	public MinioGenericResponse getBucketObjects(String userId, String bucketName, String prefix);

	/**
	 * To get object contents for given path
	 * 
	 * @param userId
	 * @param bucketName
	 * @param prefix
	 * @return MinioGenericResponse
	 */
	public MinioGenericResponse getObjectContents(String userId, String bucketName, String prefix);

	/**
	 * To upload object in given path
	 * 
	 * @param userId
	 * @param uploadfile
	 * @param bucketName
	 * @param prefix
	 * @return MinioGenericResponse
	 */
	public MinioGenericResponse objectUpload(String userId, MultipartFile uploadfile, String bucketName, String prefix);

	/**
	 * To refresh user secret
	 * 
	 * @param userId
	 * @return MinioGenericResponse
	 */
	public MinioGenericResponse userRefresh(String userId);
}
