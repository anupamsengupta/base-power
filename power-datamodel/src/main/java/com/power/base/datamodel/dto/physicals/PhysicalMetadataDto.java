package com.power.base.datamodel.dto.physicals;

import java.io.Serializable;
import java.time.LocalDate;

public class PhysicalMetadataDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDate effectiveDate;
    private LocalDate terminationDate;
    private String governingLaw;

    public PhysicalMetadataDto() {
    }

    public PhysicalMetadataDto(LocalDate effectiveDate, LocalDate terminationDate, String governingLaw) {
        this.effectiveDate = effectiveDate;
        this.terminationDate = terminationDate;
        this.governingLaw = governingLaw;
    }

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
}


