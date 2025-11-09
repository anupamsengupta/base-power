package com.power.base.dao.rdbms.jpa.persistence.swap;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.LocalDate;

@Embeddable
public class SwapMetadataEmbeddable implements Serializable {

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "termination_date")
    private LocalDate terminationDate;

    @Column(name = "governing_law")
    private String governingLaw;

    @Column(name = "is_clearable")
    private boolean clearable;

    @Column(name = "uti", length = 128)
    private String uti;

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public LocalDate getTerminationDate() {
        return terminationDate;
    }

    public void setTerminationDate(LocalDate terminationDate) {
        this.terminationDate = terminationDate;
    }

    public String getGoverningLaw() {
        return governingLaw;
    }

    public void setGoverningLaw(String governingLaw) {
        this.governingLaw = governingLaw;
    }

    public boolean isClearable() {
        return clearable;
    }

    public void setClearable(boolean clearable) {
        this.clearable = clearable;
    }

    public String getUti() {
        return uti;
    }

    public void setUti(String uti) {
        this.uti = uti;
    }
}

