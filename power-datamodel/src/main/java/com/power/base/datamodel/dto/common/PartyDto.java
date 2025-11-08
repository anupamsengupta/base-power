package com.power.base.datamodel.dto.common;

import java.io.Serializable;

public class PartyDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String role;

    public PartyDto() {
    }

    public PartyDto(String id, String name, String role) {
        this.id = id;
        this.name = name;
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

