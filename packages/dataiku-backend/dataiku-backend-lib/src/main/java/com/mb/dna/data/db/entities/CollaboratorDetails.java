package com.mb.dna.data.db.entities;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CollaboratorDetails implements Serializable{

	private String userId;
	private String givenName;
	private String surName;
	private String permission;
	
}
