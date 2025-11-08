package com.power.base.rest.controller;

import com.power.base.dao.rdbms.repository.physical.PhysicalTradeSearchCriteria;
import com.power.base.dao.rdbms.service.PhysicalTradeService;
import com.power.base.datamodel.dto.physicals.PhysicalPowerTradeDto;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/physical-trades")
public class PhysicalTradeController {

    private final PhysicalTradeService physicalTradeService;

    public PhysicalTradeController(PhysicalTradeService physicalTradeService) {
        this.physicalTradeService = physicalTradeService;
    }

    @GetMapping("/{tradeId}")
    public ResponseEntity<PhysicalPowerTradeDto> findByTradeId(@PathVariable String tradeId) {
        return physicalTradeService.findByTradeId(tradeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PhysicalPowerTradeDto> persist(@RequestBody PhysicalPowerTradeDto tradeDto) {
        PhysicalPowerTradeDto saved = physicalTradeService.persist(tradeDto);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public List<PhysicalPowerTradeDto> search(
            @RequestParam(required = false) String businessUnit,
            @RequestParam(required = false) String market,
            @RequestParam(required = false) String traderName,
            @RequestParam(required = false) String agreementId,
            @RequestParam(required = false) String commodity,
            @RequestParam(required = false) String transactionType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tradeDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant tradeTimeFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant tradeTimeTo) {

        PhysicalTradeSearchCriteria criteria = new PhysicalTradeSearchCriteria();
        criteria.setBusinessUnit(businessUnit);
        criteria.setMarket(market);
        criteria.setTraderName(traderName);
        criteria.setAgreementId(agreementId);
        criteria.setCommodity(commodity);
        criteria.setTransactionType(transactionType);
        criteria.setTradeDate(tradeDate);
        criteria.setTradeTimeFrom(tradeTimeFrom);
        criteria.setTradeTimeTo(tradeTimeTo);

        return physicalTradeService.searchByCriteria(criteria);
    }

    @DeleteMapping("/{tradeId}")
    public ResponseEntity<Void> delete(@PathVariable String tradeId) {
        physicalTradeService.deleteByTradeId(tradeId);
        return ResponseEntity.noContent().build();
    }
}

