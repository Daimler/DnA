package com.mb.dna.data.controller.dataiku;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataikuProjectDto implements Serializable {

	private String id;
	private String projectName;
	private String description;
	private String cloudProfile;
	private List<CollaboratorDetailsDto> collaborators;
}
