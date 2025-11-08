package com.power.base.datamodel.dto.financials;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SwapTradeDetailsDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<SwapPeriodDto> periods = new ArrayList<>();

    public SwapTradeDetailsDto() {
    }

    public SwapTradeDetailsDto(List<SwapPeriodDto> periods) {
        this.periods = periods;
    }

    public List<SwapPeriodDto> getPeriods() {
        return periods;
    }

    public void setPeriods(List<SwapPeriodDto> periods) {
        this.periods = periods;
    }
}


