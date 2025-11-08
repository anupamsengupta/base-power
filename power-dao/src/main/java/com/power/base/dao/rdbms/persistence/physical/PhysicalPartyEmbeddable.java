package com.power.base.dao.rdbms.persistence.physical;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class PhysicalPartyEmbeddable implements Serializable {

    @Column(name = "party_id")
    private String id;

    @Column(name = "party_name")
    private String name;

    @Column(name = "party_role")
    private String role;

    public PhysicalPartyEmbeddable() {
    }

    public PhysicalPartyEmbeddable(String id, String name, String role) {
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

