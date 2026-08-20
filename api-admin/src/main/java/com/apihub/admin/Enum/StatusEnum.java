package com.apihub.admin.Enum;

public enum StatusEnum {


    /*
    启用
     */
    ENABLE(1,"启用"),
    /*
    禁用
     */
    DISABLE(0,"禁用");

    private final Integer code;

    private final String message;

    StatusEnum(Integer code, String message) {

        this.code = code;

        this.message = message;
    }
}
