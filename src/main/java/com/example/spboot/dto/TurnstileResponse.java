package com.example.spboot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TurnstileResponse(boolean success, @JsonProperty("error-codes") List<String> errorCodes) {}
