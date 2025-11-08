package com.power.base.rest.controller;

import com.power.base.dao.rdbms.repository.swap.SwapTradeSearchCriteria;
import com.power.base.dao.rdbms.service.SwapTradeService;
import com.power.base.datamodel.dto.financials.SwapPowerTradeDto;
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
@RequestMapping("/api/swap-trades")
public class SwapTradeController {

    private final SwapTradeService swapTradeService;

    public SwapTradeController(SwapTradeService swapTradeService) {
        this.swapTradeService = swapTradeService;
    }

    @GetMapping("/{tradeId}")
    public ResponseEntity<SwapPowerTradeDto> findByTradeId(@PathVariable String tradeId) {
        return swapTradeService.findByTradeId(tradeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SwapPowerTradeDto> persist(@RequestBody SwapPowerTradeDto tradeDto) {
        SwapPowerTradeDto saved = swapTradeService.persist(tradeDto);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public List<SwapPowerTradeDto> search(
            @RequestParam(required = false) String businessUnit,
            @RequestParam(required = false) String market,
            @RequestParam(required = false) String traderName,
            @RequestParam(required = false) String agreementId,
            @RequestParam(required = false) String commodity,
            @RequestParam(required = false) String transactionType,
            @RequestParam(required = false) String referenceZone,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tradeDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant tradeTimeFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant tradeTimeTo) {

        SwapTradeSearchCriteria criteria = new SwapTradeSearchCriteria();
        criteria.setBusinessUnit(businessUnit);
        criteria.setMarket(market);
        criteria.setTraderName(traderName);
        criteria.setAgreementId(agreementId);
        criteria.setCommodity(commodity);
        criteria.setTransactionType(transactionType);
        criteria.setReferenceZone(referenceZone);
        criteria.setTradeDate(tradeDate);
        criteria.setTradeTimeFrom(tradeTimeFrom);
        criteria.setTradeTimeTo(tradeTimeTo);

        return swapTradeService.searchByCriteria(criteria);
    }

    @DeleteMapping("/{tradeId}")
    public ResponseEntity<Void> delete(@PathVariable String tradeId) {
        swapTradeService.deleteByTradeId(tradeId);
        return ResponseEntity.noContent().build();
    }
}

