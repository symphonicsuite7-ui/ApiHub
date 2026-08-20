package com.apihub.admin.entity;

import com.apihub.admin.Enum.HttpMethodEnum;
import com.apihub.admin.Enum.StatusEnum;
import lombok.Data;
import lombok.NonNull;
import org.springframework.http.HttpStatus;

@Data
public class InterfaceEntity {
    @NonNull
    String name;
    @NonNull
    String path;

    HttpMethodEnum method;

    String description;

    StatusEnum versiong;

    String categoty;

    StatusEnum status;

}
