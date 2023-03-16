package com.mb.dna.data.dataiku.api.dto;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataikuProjectsCollectionDto implements Serializable{

	private List<DataikuProjectDto> data;
	private BigInteger totalCount;
	
}
