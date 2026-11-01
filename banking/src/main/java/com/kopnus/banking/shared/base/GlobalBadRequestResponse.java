package com.kopnus.banking.shared.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GlobalBadRequestResponse {
    @JsonProperty("field")
    private String payload;
    @JsonProperty("message")
    private String message;
}