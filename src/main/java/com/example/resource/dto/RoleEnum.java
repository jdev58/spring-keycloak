package com.example.resource.dto;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Optional;

public enum RoleEnum implements Serializable {
    USER(1, "USER"),
    ADMIN(2, "ADMIN"),
    ORDER_MANAGER(3, "ORDER_MANAGER"),
    REPORT_VIEWER(4, "REPORT_VIEWER"),
    CHIEF_OFFICER(5, "CHIEF_OFFICER"),
    CALL_CENTER(6, "CALL_CENTER"),
    FINANCIAL_MANAGER(7, "FINANCIAL_MANAGER");


    private Integer id;
    private String value;


    RoleEnum(Integer id, String value) {
        this.value = value;
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public Integer getId() {
        return id;
    }

    public static RoleEnum fromValue(final int value) {
        Optional<RoleEnum> roleEnum = Arrays.stream(RoleEnum.values()).filter(role->role.id.equals(value)).findFirst();
        return roleEnum.orElse(null);
    }

    public static RoleEnum fromString(final String name){
        Optional<RoleEnum> roleEnum = Arrays.stream(RoleEnum.values()).filter(role->role.getValue().equals(name)).findFirst();
        return roleEnum.orElse(null);
    }

    @Override
    public String toString() {
        switch (this) {
            case USER: return "USER";
            case ADMIN: return "ADMIN";
            case ORDER_MANAGER: return "ORDER_MANAGER";
            case REPORT_VIEWER: return "REPORT_VIEWER";
            case CHIEF_OFFICER: return "CHIEF_OFFICER";
            case CALL_CENTER: return "CALL_CENTER";
            case FINANCIAL_MANAGER: return "FINANCIAL_MANAGER";
        }
        return null;
    }

}
