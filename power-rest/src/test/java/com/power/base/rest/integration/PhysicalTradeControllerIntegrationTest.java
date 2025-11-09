package com.power.base.rest.integration;

import com.power.base.datamodel.dto.common.BuySellIndicator;
import com.power.base.datamodel.dto.common.DocumentType;
import com.power.base.datamodel.dto.common.PartyDto;
import com.power.base.datamodel.dto.common.Profile;
import com.power.base.datamodel.dto.physicals.PhysicalLineItemDto;
import com.power.base.datamodel.dto.physicals.PhysicalMetadataDto;
import com.power.base.datamodel.dto.physicals.PhysicalPowerTradeDto;
import com.power.base.datamodel.dto.physicals.PhysicalSettlementInfoDto;
import com.power.base.datamodel.dto.physicals.PhysicalSettlementItemDto;
import com.power.base.datamodel.dto.physicals.PhysicalTradeDetailsDto;
import com.power.base.datamodel.dto.physicals.PhysicalTradeHeaderDto;
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
                "spring.datasource.url=jdbc:h2:mem:physical-controller;DB_CLOSE_DELAY=-1;MODE=LEGACY",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.classformat.ignore=true"
        })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestMethodOrder(MethodOrderer.MethodName.class)
class PhysicalTradeControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/physical-trades";
    }

    @Test
    void postAndRetrievePhysicalTrade() {
        PhysicalPowerTradeDto request = buildPhysicalTrade("PWR-INT-001");

        ResponseEntity<PhysicalPowerTradeDto> createResponse =
                restTemplate.postForEntity(baseUrl(), request, PhysicalPowerTradeDto.class);

        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        assertEquals("PWR-INT-001", createResponse.getBody().getTradeHeader().getTradeId());

        ResponseEntity<PhysicalPowerTradeDto> fetchResponse =
                restTemplate.getForEntity(baseUrl() + "/{tradeId}", PhysicalPowerTradeDto.class, "PWR-INT-001");

        assertEquals(HttpStatus.OK, fetchResponse.getStatusCode());
        assertNotNull(fetchResponse.getBody());
        assertEquals("Nord Pool Trading Desk", fetchResponse.getBody().getTradeHeader().getBusinessUnit());
    }

    @Test
    void searchPhysicalTradesByCriteria() {
        PhysicalPowerTradeDto request = buildPhysicalTrade("PWR-INT-002");
        restTemplate.postForEntity(baseUrl(), request, PhysicalPowerTradeDto.class);

        String url = UriComponentsBuilder.fromHttpUrl(baseUrl())
                .queryParam("businessUnit", "Nord Pool Trading Desk")
                .queryParam("traderName", "John Doe (TRDR-456)")
                .toUriString();

        ResponseEntity<List<PhysicalPowerTradeDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("PWR-INT-002", response.getBody().get(0).getTradeHeader().getTradeId());
    }

    private PhysicalPowerTradeDto buildPhysicalTrade(String tradeId) {
        PhysicalTradeHeaderDto header = new PhysicalTradeHeaderDto();
        header.setTradeId(tradeId);
        header.setTradeDate(LocalDate.of(2025, 11, 7));
        header.setTradeTime(Instant.parse("2025-11-07T14:30:00Z"));
        header.setDocumentType(DocumentType.CONFIRMATION);
        header.setDocumentVersion("1.0");
        header.setBuyerParty(new PartyDto("10X1001A1001A450F6", "Utility EU Ltd", "Buyer"));
        header.setSellerParty(new PartyDto("10XFR-ENTSOE-0000000A", "GenCo US Inc", "Seller"));
        header.setBusinessUnit("Nord Pool Trading Desk");
        header.setBookStrategy("Physical Hedging Book");
        header.setTraderName("John Doe (TRDR-456)");
        header.setAgreementId("EFET-2023-001");
        header.setMarket("EPEX-SPOT");
        header.setCommodity("Power");
        header.setTransactionType("FOR");
        header.setDeliveryPoint("DE-LU");
        header.setLoadType("Base Load");
        header.setBuySellIndicator(BuySellIndicator.BUY);
        header.setAmendmentIndicator(false);

        PhysicalLineItemDto lineItem = new PhysicalLineItemDto(
                LocalDate.of(2025, 11, 7),
                Instant.parse("2025-11-07T00:00:00Z"),
                LocalDate.of(2025, 11, 7),
                Instant.parse("2025-11-07T01:00:00Z"),
                "Fri 00:00-01:00",
                12.5,
                "MWh",
                50.0,
                Profile.ONE_HOUR
        );
        PhysicalTradeDetailsDto details = new PhysicalTradeDetailsDto(List.of(lineItem));

        PhysicalSettlementItemDto settlementItem = new PhysicalSettlementItemDto(
                "SET-2025-11-07",
                List.of("LI-001"),
                LocalDate.of(2025, 11, 7),
                1198.5,
                "MWh",
                75.50,
                75.50,
                "EUR/MWh",
                "EUR/MWh",
                0.0,
                0.0,
                90462.75,
                "EUR",
                "EUR",
                "Provisional"
        );
        PhysicalSettlementInfoDto settlementInfo = new PhysicalSettlementInfoDto(
                33600.0,
                "MWh",
                "Fixed",
                75.50,
                75.50,
                "EUR",
                "EUR",
                "EUR/MWh",
                "EUR/MWh",
                LocalDate.of(2025, 11, 7),
                null,
                LocalDate.of(2025, 11, 14),
                null,
                "Schedule_Date",
                5,
                2536800.00,
                2,
                List.of(settlementItem)
        );

        PhysicalMetadataDto metadata = new PhysicalMetadataDto(
                LocalDate.of(2025, 11, 7),
                LocalDate.of(2025, 11, 14),
                "EU (German Law)"
        );

        return new PhysicalPowerTradeDto(header, details, settlementInfo, metadata);
    }
}

