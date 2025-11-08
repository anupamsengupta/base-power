package com.power.base.datamodel.dto.physicals;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PhysicalTradeDetailsDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<PhysicalLineItemDto> lineItems = new ArrayList<>();

    public PhysicalTradeDetailsDto() {
    }

    public PhysicalTradeDetailsDto(List<PhysicalLineItemDto> lineItems) {
        this.lineItems = lineItems;
    }

    public List<PhysicalLineItemDto> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<PhysicalLineItemDto> lineItems) {
        this.lineItems = lineItems;
    }
}


