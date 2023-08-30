package com.daimler.data.auth.client;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateRouteVO implements Serializable{

	private static final long serialVersionUID = 1L;

	private String name;

	private List<String> paths;

	private List<String> protocols ;

	private List<String> hosts;

	private Boolean stripPath;

}

