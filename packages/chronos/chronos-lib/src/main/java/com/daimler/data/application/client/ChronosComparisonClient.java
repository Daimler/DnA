package com.daimler.data.application.client;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.db.json.ComparisonState;
import com.daimler.data.dto.comparison.ChronosComparisonRequestDto;
import com.daimler.data.dto.comparison.CreateComparisonResponseDataDto;
import com.daimler.data.dto.comparison.CreateComparisonResponseWrapperDto;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ChronosComparisonClient {

	
	@Value("${chronosComparison.uri}")
	private String chronosComparisonBaseUri;
	
	@Value("${chronosMinio.env}")
	private String chronosMinioEnv; //"prod"/"dev"/"test"
	
	private static final String COMPARISON_PATH = "/api/comparison";
	
	@Autowired
	private RestTemplate restTemplate;
	
	public CreateComparisonResponseWrapperDto createComparison(String comparisonName, String requestUser, ChronosComparisonRequestDto requestDto) {
		CreateComparisonResponseDataDto createComparisonResponse = new CreateComparisonResponseDataDto();
		ComparisonState data = new ComparisonState();
		data.setLifeCycleState("FAILED");
		CreateComparisonResponseWrapperDto createComparisonResponseWrapperDto = new CreateComparisonResponseWrapperDto();
		createComparisonResponseWrapperDto.setStatus("FAILED");
		List<MessageDescription> errors = new ArrayList<>();
		try {
				HttpHeaders headers = new HttpHeaders();
				headers.set("Accept", "application/json");
				headers.setContentType(MediaType.APPLICATION_JSON);
				String url = chronosComparisonBaseUri + COMPARISON_PATH;
				requestDto.setMinio_endpoint(chronosMinioEnv);
				HttpEntity<ChronosComparisonRequestDto> requestEntity = new HttpEntity<>(requestDto,headers);
				ResponseEntity<CreateComparisonResponseDataDto> response = restTemplate.exchange(url, HttpMethod.POST,
						requestEntity, CreateComparisonResponseDataDto.class);
				if (response.hasBody()) {
					createComparisonResponse = response.getBody();
					if(response.getStatusCode().is2xxSuccessful()) {
						createComparisonResponseWrapperDto.setStatus("SUCCESS");
					}else {
						
					}
				}
			}catch(Exception e) {
					log.error("Failed while creating comparison {} , triggeredBy {} with exception {}", comparisonName ,requestUser, e.getMessage());
					MessageDescription errMsg = new MessageDescription("Failed while creating comparison with exception." + e.getMessage());
					errors.add(errMsg);
					createComparisonResponseWrapperDto.setErrors(errors);
					createComparisonResponseWrapperDto.setStatus("FAILED");
			}
			createComparisonResponseWrapperDto.setData(data);
			return createComparisonResponseWrapperDto;
		}
	}

	
