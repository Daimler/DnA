package com.daimler.data.application.client;

import com.daimler.data.controller.exceptions.MessageDescription;
import com.daimler.data.dto.userinfo.dashboard.GetDashboardServiceLovResponseWrapperDto;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class DashboardServicesClient {

    @Value("${dashboard.uri}")
    private String reportBaseUri;

    private static final String AGILE_RELEASE_TRAIN_LOV_PATH = "/api/lov/agilereleasetrains";
    private static final String FRONTEND_TECHNOLOGIES_LOV_PATH = "/api/lov/frontendtechnologies";

    @Autowired
    HttpServletRequest httpRequest;

    @Autowired
    private RestTemplate restTemplate;

    public GetDashboardServiceLovResponseWrapperDto getAgileReleaseTrain() {
        GetDashboardServiceLovResponseWrapperDto getAgileReleasetrainResponse = new GetDashboardServiceLovResponseWrapperDto();
        List<MessageDescription> errors = new ArrayList<>();
        try {
            HttpHeaders headers = new HttpHeaders();
            String userinfo = httpRequest.getHeader("dna-request-userdetails");
            headers.set("Accept", "application/json");
            headers.set("dna-request-userdetails", userinfo);
            headers.setContentType(MediaType.APPLICATION_JSON);
            String getReportUrl = reportBaseUri + AGILE_RELEASE_TRAIN_LOV_PATH;

            HttpEntity<GetDashboardServiceLovResponseWrapperDto> requestEntity = new HttpEntity<>(headers);
            ResponseEntity<GetDashboardServiceLovResponseWrapperDto> response = restTemplate.exchange(getReportUrl, HttpMethod.GET, requestEntity, GetDashboardServiceLovResponseWrapperDto.class);
            if (response.hasBody()) {
                getAgileReleasetrainResponse = response.getBody();
            }
        } catch (Exception e) {
            log.error("Failed while getting the AgileReleaseTrain details of {} with an exception {}", e.getMessage());
            MessageDescription errMsg = new MessageDescription("Failed while getting the AgileReleaseTrain with name details with an exception " + e.getMessage());
            errors.add(errMsg);
            getAgileReleasetrainResponse.setErrors(errors);
            getAgileReleasetrainResponse.setStatus("FAILED");
        }
        return getAgileReleasetrainResponse;
    }


    public GetDashboardServiceLovResponseWrapperDto getFrontendTechnologies() {
        GetDashboardServiceLovResponseWrapperDto getAgileReleasetrainResponse = new GetDashboardServiceLovResponseWrapperDto();
        List<MessageDescription> errors = new ArrayList<>();
        try {
            HttpHeaders headers = new HttpHeaders();
            String userinfo = httpRequest.getHeader("dna-request-userdetails");
            headers.set("Accept", "application/json");
            headers.set("dna-request-userdetails", userinfo);
            headers.setContentType(MediaType.APPLICATION_JSON);
            String getReportUrl = reportBaseUri + FRONTEND_TECHNOLOGIES_LOV_PATH;

            HttpEntity<GetDashboardServiceLovResponseWrapperDto> requestEntity = new HttpEntity<>(headers);
            ResponseEntity<GetDashboardServiceLovResponseWrapperDto> response = restTemplate.exchange(getReportUrl, HttpMethod.GET, requestEntity, GetDashboardServiceLovResponseWrapperDto.class);
            if (response.hasBody()) {
                getAgileReleasetrainResponse = response.getBody();
            }
        } catch (Exception e) {
            log.error("Failed while getting the FrontendTechnologies details of {} with an exception {}", e.getMessage());
            MessageDescription errMsg = new MessageDescription("Failed while getting the FrontendTechnologies with name details with an exception " + e.getMessage());
            errors.add(errMsg);
            getAgileReleasetrainResponse.setErrors(errors);
            getAgileReleasetrainResponse.setStatus("FAILED");
        }
        return getAgileReleasetrainResponse;
    }
}
