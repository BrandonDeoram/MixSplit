package com.demo.MixSplit.DTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class ApiResponse {
    private String status;
    private Map<String, Object> data;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, Object> error;
}