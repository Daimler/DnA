package com.mb.dna.data.controller.userprivilege;

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
public class UserPrivilegeCollectionDto  implements Serializable{

	private List<UserPrivilegeDto> data;
	private Integer totalcount;
}
