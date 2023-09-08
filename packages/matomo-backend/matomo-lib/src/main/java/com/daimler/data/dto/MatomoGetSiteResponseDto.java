package com.daimler.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatomoGetSiteResponseDto {
    private String result;
    private String  message;
    private String idsite;
    private String name;
    private String main_url;
}
