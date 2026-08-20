package com.apihub.admin.dto;

import com.apihub.admin.Enum.HttpMethodEnum;
import com.apihub.admin.Enum.StatusEnum;
import lombok.Data;
import lombok.NonNull;
import org.springframework.http.HttpMethod;

import java.time.LocalDate;

@Data
public class InterfaceDTO {

    @NonNull
    String name;

    @NonNull
    String path;

    HttpMethodEnum httpMethod;

    String description;

    String version;


}
