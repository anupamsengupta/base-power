package com.power.base.datamodel.dto.financials;

import java.io.Serializable;
import java.time.LocalDate;

public class SwapMetadataDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDate effectiveDate;
    private LocalDate terminationDate;
    private String governingLaw;
    private boolean clearable;
    private String uti;

    public SwapMetadataDto() {
    }

    public SwapMetadataDto(LocalDate effectiveDate,
                           LocalDate terminationDate,
                           String governingLaw,
                           boolean clearable,
                           String uti) {
        this.effectiveDate = effectiveDate;
        this.terminationDate = terminationDate;
        this.governingLaw = governingLaw;
        this.clearable = clearable;
        this.uti = uti;
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


