package com.power.base.rest.controller;

import com.power.base.dao.rdbms.jpa.repository.swap.SwapTradeSearchCriteria;
import com.power.base.dao.rdbms.jpa.service.SwapTradeService;
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
import org.springframework.web.util.UriUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/swap-trades")
public class SwapTradeController {

    private final SwapTradeService swapTradeService;

    public SwapTradeController(SwapTradeService swapTradeService) {
        this.swapTradeService = swapTradeService;
    }

    @GetMapping("/{tradeId}")
    public ResponseEntity<SwapPowerTradeDto> findByTradeId(@PathVariable("tradeId") String tradeId) {
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
            @RequestParam(name = "businessUnit", required = false) String businessUnit,
            @RequestParam(name = "market", required = false) String market,
            @RequestParam(name = "traderName", required = false) String traderName,
            @RequestParam(name = "agreementId", required = false) String agreementId,
            @RequestParam(name = "commodity", required = false) String commodity,
            @RequestParam(name = "transactionType", required = false) String transactionType,
            @RequestParam(name = "referenceZone", required = false) String referenceZone,
            @RequestParam(name = "tradeDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tradeDate,
            @RequestParam(name = "tradeTimeFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant tradeTimeFrom,
            @RequestParam(name = "tradeTimeTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant tradeTimeTo) {

        SwapTradeSearchCriteria criteria = new SwapTradeSearchCriteria();
        criteria.setBusinessUnit(decode(businessUnit));
        criteria.setMarket(decode(market));
        criteria.setTraderName(decode(traderName));
        criteria.setAgreementId(decode(agreementId));
        criteria.setCommodity(decode(commodity));
        criteria.setTransactionType(decode(transactionType));
        criteria.setReferenceZone(decode(referenceZone));
        criteria.setTradeDate(tradeDate);
        criteria.setTradeTimeFrom(tradeTimeFrom);
        criteria.setTradeTimeTo(tradeTimeTo);

        return swapTradeService.searchByCriteria(criteria);
    }

    @DeleteMapping("/{tradeId}")
    public ResponseEntity<Void> delete(@PathVariable("tradeId") String tradeId) {
        swapTradeService.deleteByTradeId(tradeId);
        return ResponseEntity.noContent().build();
    }

    private String decode(String value) {
        return value == null ? null : UriUtils.decode(value, StandardCharsets.UTF_8);
    }
}

