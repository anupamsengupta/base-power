package com.power.base.rest.integration;

import com.power.base.datamodel.dto.common.BuySellIndicator;
import com.power.base.datamodel.dto.common.DocumentType;
import com.power.base.datamodel.dto.common.PartyDto;
import com.power.base.datamodel.dto.financials.SwapMetadataDto;
import com.power.base.datamodel.dto.financials.SwapPeriodDto;
import com.power.base.datamodel.dto.financials.SwapPowerTradeDto;
import com.power.base.datamodel.dto.financials.SwapSettlementInfoDto;
import com.power.base.datamodel.dto.financials.SwapTradeDetailsDto;
import com.power.base.datamodel.dto.financials.SwapTradeHeaderDto;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        classes = TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:swap-controller;DB_CLOSE_DELAY=-1;MODE=LEGACY",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.classformat.ignore=true"
        })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestMethodOrder(MethodOrderer.MethodName.class)
class SwapTradeControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/swap-trades";
    }

    @Test
    void postAndRetrieveSwapTrade() {
        SwapPowerTradeDto request = buildSwapTrade("SWP-INT-001");

        ResponseEntity<SwapPowerTradeDto> createResponse =
                restTemplate.postForEntity(baseUrl(), request, SwapPowerTradeDto.class);

        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        assertEquals("SWP-INT-001", createResponse.getBody().getTradeHeader().getTradeId());

        ResponseEntity<SwapPowerTradeDto> fetchResponse =
                restTemplate.getForEntity(baseUrl() + "/{tradeId}", SwapPowerTradeDto.class, "SWP-INT-001");

        assertEquals(HttpStatus.OK, fetchResponse.getStatusCode());
        assertNotNull(fetchResponse.getBody());
        assertEquals("Derivatives Desk", fetchResponse.getBody().getTradeHeader().getBusinessUnit());
    }

    @Test
    void searchSwapTradesByCriteria() {
        SwapPowerTradeDto request = buildSwapTrade("SWP-INT-002");
        restTemplate.postForEntity(baseUrl(), request, SwapPowerTradeDto.class);

        String url = UriComponentsBuilder.fromHttpUrl(baseUrl())
                .queryParam("traderName", "Jane Smith (TRDR-789)")
                .queryParam("referenceZone", "DE-LU")
                .toUriString();

        ResponseEntity<List<SwapPowerTradeDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("SWP-INT-002", response.getBody().get(0).getTradeHeader().getTradeId());
    }

    private SwapPowerTradeDto buildSwapTrade(String tradeId) {
        SwapTradeHeaderDto header = new SwapTradeHeaderDto();
        header.setTradeId(tradeId);
        header.setTradeDate(LocalDate.of(2025, 11, 7));
        header.setTradeTime(Instant.parse("2025-11-07T14:30:00Z"));
        header.setDocumentType(DocumentType.CONFIRMATION);
        header.setDocumentVersion("1.0");
        header.setBuyerParty(new PartyDto("10X1001A1001A450F6", "Hedge Fund EU", "Buyer"));
        header.setSellerParty(new PartyDto("10XFR-ENTSOE-0000000A", "Bank US LLC", "Seller"));
        header.setBusinessUnit("Derivatives Desk");
        header.setBookStrategy("Price Hedging Book");
        header.setTraderName("Jane Smith (TRDR-789)");
        header.setAgreementId("ISDA-2024-001");
        header.setMarket("EEX");
        header.setCommodity("Power");
        header.setTransactionType("SWAP");
        header.setReferenceZone("DE-LU");
        header.setBuySellIndicator(BuySellIndicator.SELL);
        header.setAmendmentIndicator(false);

        SwapPeriodDto period = new SwapPeriodDto(
                100.0,
                "MWh",
                LocalDate.of(2025, 11, 1),
                Instant.parse("2025-11-01T00:00:00Z"),
                LocalDate.of(2025, 12, 1),
                Instant.parse("2025-12-01T00:00:00Z"),
                "Monthly",
                "EPEX-DA-BASE",
                "Base"
        );
        SwapTradeDetailsDto detailsDto = new SwapTradeDetailsDto(List.of(period));

        SwapSettlementInfoDto settlementInfoDto = new SwapSettlementInfoDto(
                8760.0,
                "MWh",
                "Index",
                70.00,
                0.0,
                "EUR",
                "EUR",
                "EUR/MWh",
                "EUR/MWh",
                "Cash",
                LocalDate.of(2025, 12, 8),
                LocalDate.of(2025, 11, 7),
                null,
                LocalDate.of(2025, 12, 7),
                null,
                2,
                0.0,
                4
        );

        SwapMetadataDto metadataDto = new SwapMetadataDto(
                LocalDate.of(2025, 11, 7),
                LocalDate.of(2025, 12, 7),
                "English Law",
                true,
                "FINPWR20251107ABC123"
        );

        return new SwapPowerTradeDto(header, detailsDto, settlementInfoDto, metadataDto);
    }
}

