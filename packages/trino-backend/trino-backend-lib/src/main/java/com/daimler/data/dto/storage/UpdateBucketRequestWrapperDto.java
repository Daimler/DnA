package com.daimler.data.dto.storage;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateBucketRequestWrapperDto implements Serializable{

	private static final long serialVersionUID = 1L;
	private UpdateBucketRequestDto data;
	
}
