package com.apihub.admin.Enum;

import lombok.Data;

public enum HttpMethodEnum {

    /*
       查询方法
     */
    GET("GET","获取"),
    /*
    新增方法
     */
    POST("POST","新增"),
    /*
    修改方法
     */
    PUT("PUT","修改"),
    /*
   删除方法
    */
    DELETE("DELETE","删除"),
    /*
    部分修改方法
*/
    PATCH("PATCH","部分修改");

    private final String code;

    private final String desc;

    HttpMethodEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
