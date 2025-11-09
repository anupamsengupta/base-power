package com.power.base.dao.nosql.dynamodb.repository;

import com.power.base.dao.nosql.dynamodb.DynamoDbDaoException;
import com.power.base.dao.rdbms.jpa.repository.physical.PhysicalTradeSearchCriteria;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemResponse;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsRequest;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsResponse;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PhysicalTradeDynamoDaoImplTest {

    private static final String TENANT_ID = "TENANT_A";
    private static final String TABLE_NAME = PhysicalTradeDynamoDaoImpl.TABLE_NAME;

    private final Map<String, Map<String, Map<String, AttributeValue>>> dataStore = new ConcurrentHashMap<>();
    private DynamoDbClient dynamoDbClient;
    private PhysicalTradeDynamoDaoImpl dao;

    @BeforeEach
    void setUp() {
        dataStore.clear();
        dynamoDbClient = createMockClient();
        dao = new PhysicalTradeDynamoDaoImpl(dynamoDbClient);
    }

    @Test
    void saveAndFindByTradeId_roundTripsFullTradeGraph() {
        PhysicalPowerTradeDto trade = buildTrade("PWR-TEST-001", 2, 1);

        dao.save(trade);

        Optional<PhysicalPowerTradeDto> reloaded = dao.findByTradeId("PWR-TEST-001");
        assertThat(reloaded).isPresent();

        PhysicalPowerTradeDto dto = reloaded.get();
        assertThat(dto.getTradeHeader().getTenantId()).isEqualTo(TENANT_ID);
        assertThat(dto.getTradeDetails().getLineItems()).hasSize(2);
        assertThat(dto.getSettlementInfo().getSettlementItems()).hasSize(1);
        assertThat(dto.getSettlementInfo().getSettlementItems().get(0).getReferencedLineItems()).containsExactly("LINE#0001");
        assertThat(dto.getMetadata().getGoverningLaw()).isEqualTo("English Law");

        verify(dynamoDbClient, atLeastOnce()).transactWriteItems(any(TransactWriteItemsRequest.class));
    }

    @Test
    void save_overwritesExistingStateViaDelete() {
        PhysicalPowerTradeDto initial = buildTrade("PWR-TEST-002", 1, 0);
        PhysicalPowerTradeDto updated = buildTrade("PWR-TEST-002", 3, 2);

        dao.save(initial);
        dao.save(updated);

        Optional<PhysicalPowerTradeDto> reloaded = dao.findByTradeId("PWR-TEST-002");
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getTradeDetails().getLineItems()).hasSize(3);
        assertThat(reloaded.get().getSettlementInfo().getSettlementItems()).hasSize(2);

        verify(dynamoDbClient, atLeastOnce()).batchWriteItem(any(BatchWriteItemRequest.class));
    }

    @Test
    void save_handlesLargeGraphsWithMultipleTransactions() {
        PhysicalPowerTradeDto trade = buildTrade("PWR-TEST-003", 40, 0);

        dao.save(trade);

        verify(dynamoDbClient, times(2)).transactWriteItems(any(TransactWriteItemsRequest.class));
        Optional<PhysicalPowerTradeDto> reloaded = dao.findByTradeId("PWR-TEST-003");
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getTradeDetails().getLineItems()).hasSize(40);
    }

    @Test
    void searchByCriteria_filtersByHeadersAndTemporalBounds() {
        PhysicalPowerTradeDto match = buildTrade("PWR-TEST-004", 2, 0);
        match.getTradeHeader().setTraderName("John Doe");
        match.getTradeHeader().setBusinessUnit("North America Power");
        match.getTradeHeader().setTradeTime(Instant.parse("2025-11-09T10:30:00Z"));

        PhysicalPowerTradeDto otherTenant = buildTrade("PWR-TEST-005", 1, 0);
        otherTenant.getTradeHeader().setTenantId("TENANT_B");
        otherTenant.getTradeHeader().setTradeTime(Instant.parse("2025-11-10T10:30:00Z"));

        dao.save(match);
        dao.save(otherTenant);

        PhysicalTradeSearchCriteria criteria = new PhysicalTradeSearchCriteria();
        criteria.setTenantId(TENANT_ID);
        criteria.setBusinessUnit("North America Power");
        criteria.setTradeTimeFrom(Instant.parse("2025-11-09T00:00:00Z"));
        criteria.setTradeTimeTo(Instant.parse("2025-11-09T23:59:59Z"));

        List<PhysicalPowerTradeDto> results = dao.searchByCriteria(criteria);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTradeHeader().getTradeId()).isEqualTo("PWR-TEST-004");
    }

    @Test
    void deleteByTradeId_removesAllRowsForPartition() {
        PhysicalPowerTradeDto trade = buildTrade("PWR-TEST-006", 5, 1);
        dao.save(trade);

        dao.deleteByTradeId("PWR-TEST-006");

        Optional<PhysicalPowerTradeDto> reloaded = dao.findByTradeId("PWR-TEST-006");
        assertThat(reloaded).isEmpty();
        assertThat(dataStore).doesNotContainKey("PWR-TEST-006");
    }

    @Test
    void save_requiresTenantId() {
        PhysicalPowerTradeDto trade = buildTrade("PWR-TEST-007", 1, 0);
        trade.getTradeHeader().setTenantId(null);

        assertThatThrownBy(() -> dao.save(trade))
                .isInstanceOf(DynamoDbDaoException.class)
                .hasMessageContaining("tenantId");
    }

    private PhysicalPowerTradeDto buildTrade(String tradeId, int lineItemCount, int settlementItemCount) {
        PhysicalTradeHeaderDto header = new PhysicalTradeHeaderDto();
        header.setTenantId(TENANT_ID);
        header.setTradeId(tradeId);
        header.setTradeDate(LocalDate.of(2025, 11, 9));
        header.setTradeTime(Instant.parse("2025-11-09T12:30:00Z"));
        header.setDocumentType(DocumentType.CONFIRMATION);
        header.setDocumentVersion("1.0");
        header.setBuyerParty(new PartyDto("BUYER", "Buyer Corp", "Buyer"));
        header.setSellerParty(new PartyDto("SELLER", "Seller Corp", "Seller"));
        header.setBusinessUnit("Nord Pool Trading Desk");
        header.setBookStrategy("Physical Hedging Book");
        header.setTraderName("Jane Trader");
        header.setAgreementId("AGR-" + tradeId);
        header.setMarket("EPEX");
        header.setCommodity("Power");
        header.setTransactionType("FORWARD");
        header.setDeliveryPoint("DE-LU");
        header.setLoadType("Base");
        header.setBuySellIndicator(BuySellIndicator.BUY);
        header.setAmendmentIndicator(false);

        List<PhysicalLineItemDto> lineItems = new ArrayList<>();
        for (int i = 0; i < lineItemCount; i++) {
            PhysicalLineItemDto lineItem = new PhysicalLineItemDto();
            lineItem.setPeriodStartDate(LocalDate.of(2025, 11, 9));
            lineItem.setPeriodStartTime(Instant.parse("2025-11-09T" + String.format("%02d", i % 24) + ":00:00Z"));
            lineItem.setPeriodEndDate(LocalDate.of(2025, 11, 9));
            lineItem.setPeriodEndTime(Instant.parse("2025-11-09T" + String.format("%02d", i % 24) + ":59:59Z"));
            lineItem.setDayHour("H" + (i + 1));
            lineItem.setQuantity(10.0 + i);
            lineItem.setCapacity(5.0 + i);
            lineItem.setUom("MWh");
            lineItem.setProfile(Profile.ONE_HOUR);
            lineItems.add(lineItem);
        }

        PhysicalTradeDetailsDto details = new PhysicalTradeDetailsDto(lineItems);

        PhysicalSettlementInfoDto settlementInfo = new PhysicalSettlementInfoDto();
        settlementInfo.setTotalVolume(1000);
        settlementInfo.setTotalVolumeUom("MWh");
        settlementInfo.setPricingMechanism("Fixed");
        settlementInfo.setSettlementPrice(75.5);
        settlementInfo.setTradePrice(74.0);
        settlementInfo.setSettlementCurrency("EUR");
        settlementInfo.setTradeCurrency("EUR");
        settlementInfo.setSettlementUom("EUR/MWh");
        settlementInfo.setTradeUom("EUR/MWh");
        settlementInfo.setStartApplicabilityDate(LocalDate.of(2025, 11, 10));
        settlementInfo.setStartApplicabilityTime(Instant.parse("2025-11-10T00:00:00Z"));
        settlementInfo.setEndApplicabilityDate(LocalDate.of(2025, 11, 11));
        settlementInfo.setEndApplicabilityTime(Instant.parse("2025-11-11T00:00:00Z"));
        settlementInfo.setPaymentEvent("Monthly");
        settlementInfo.setPaymentOffset(2);
        settlementInfo.setTotalContractValue(75000);
        settlementInfo.setRounding(2);

        List<PhysicalSettlementItemDto> settlementItems = new ArrayList<>();
        for (int i = 0; i < settlementItemCount; i++) {
            PhysicalSettlementItemDto item = new PhysicalSettlementItemDto();
            item.setSettlementId("SET-" + (i + 1));
            item.setReferencedLineItems(List.of("LINE#" + String.format("%04d", Math.min(i + 1, Math.max(1, lineItemCount)))));
            item.setDeliveryDate(LocalDate.of(2025, 11, 10 + i));
            item.setActualQuantity(50 + i);
            item.setUom("MWh");
            item.setSettlementPrice(76 + i);
            item.setTradePrice(75 + i);
            item.setSettlementUom("EUR/MWh");
            item.setTradeUom("EUR/MWh");
            item.setDeviationAmount(1.5 + i);
            item.setDeviationPenalty(0.5 + i);
            item.setPeriodCashflow(1000 + i);
            item.setSettlementCurrency("EUR");
            item.setTradeCurrency("EUR");
            item.setInvoiceStatus("Pending");
            settlementItems.add(item);
        }
        settlementInfo.setSettlementItems(settlementItems);

        PhysicalMetadataDto metadata = new PhysicalMetadataDto(
                LocalDate.of(2025, 11, 9),
                LocalDate.of(2025, 12, 9),
                "English Law"
        );

        return new PhysicalPowerTradeDto(header, details, settlementInfo, metadata);
    }

    private DynamoDbClient createMockClient() {
        DynamoDbClient client = mock(DynamoDbClient.class);

        doAnswer(invocation -> {
            TransactWriteItemsRequest request = invocation.getArgument(0);
            request.transactItems().stream()
                    .map(TransactWriteItem::put)
                    .filter(Objects::nonNull)
                    .forEach(put -> {
                        Map<String, AttributeValue> item = new HashMap<>(put.item());
                        String tradeId = attributeString(item.get("trade_id"));
                        String sortKey = attributeString(item.get("sort_key"));
                        dataStore.computeIfAbsent(tradeId, key -> new LinkedHashMap<>())
                                .put(sortKey, deepCopy(item));
                    });
            return TransactWriteItemsResponse.builder().build();
        }).when(client).transactWriteItems(any(TransactWriteItemsRequest.class));

        when(client.query(any(QueryRequest.class))).thenAnswer(invocation -> {
            QueryRequest request = invocation.getArgument(0);
            AttributeValue tradeIdAttr = Optional.ofNullable(request.expressionAttributeValues())
                    .map(values -> values.get(":tradeId"))
                    .orElse(null);
            String tradeId = attributeString(tradeIdAttr);
            List<Map<String, AttributeValue>> items = dataStore.getOrDefault(tradeId, Map.of())
                    .values()
                    .stream()
                    .map(this::deepCopy)
                    .sorted(Comparator.comparing(map -> attributeString(map.get("sort_key"))))
                    .collect(Collectors.toList());
            return QueryResponse.builder().items(items).build();
        });

        doAnswer(invocation -> {
            BatchWriteItemRequest request = invocation.getArgument(0);
            request.requestItems().getOrDefault(TABLE_NAME, List.of())
                    .stream()
                    .map(software.amazon.awssdk.services.dynamodb.model.WriteRequest::deleteRequest)
                    .filter(Objects::nonNull)
                    .forEach(delete -> {
                        String tradeId = attributeString(delete.key().get("trade_id"));
                        String sortKey = attributeString(delete.key().get("sort_key"));
                        dataStore.computeIfPresent(tradeId, (key, value) -> {
                            value.remove(sortKey);
                            return value.isEmpty() ? null : value;
                        });
                    });
            return BatchWriteItemResponse.builder().build();
        }).when(client).batchWriteItem(any(BatchWriteItemRequest.class));

        when(client.scan(any(ScanRequest.class))).thenAnswer(invocation -> {
            ScanRequest request = invocation.getArgument(0);
            List<Map<String, AttributeValue>> headers = dataStore.values()
                    .stream()
                    .map(map -> map.values()
                            .stream()
                            .filter(item -> "HEADER".equals(attributeString(item.get("entity_type"))))
                            .findFirst()
                            .orElse(null))
                    .filter(Objects::nonNull)
                    .map(this::deepCopy)
                    .filter(item -> matchesFilter(item,
                            request.filterExpression(),
                            request.expressionAttributeNames(),
                            request.expressionAttributeValues()))
                    .collect(Collectors.toList());
            return ScanResponse.builder().items(headers).build();
        });

        return client;
    }

    private Map<String, AttributeValue> deepCopy(Map<String, AttributeValue> source) {
        Map<String, AttributeValue> copy = new HashMap<>();
        source.forEach((key, value) -> copy.put(key, copyAttribute(value)));
        return copy;
    }

    private AttributeValue copyAttribute(AttributeValue value) {
        AttributeValue.Builder builder = AttributeValue.builder();
        if (value.s() != null) {
            builder.s(value.s());
        }
        if (value.n() != null) {
            builder.n(value.n());
        }
        if (value.bool() != null) {
            builder.bool(value.bool());
        }
        if (value.ss() != null) {
            builder.ss(new ArrayList<>(value.ss()));
        }
        return builder.build();
    }

    private boolean matchesFilter(Map<String, AttributeValue> item,
                                  String filterExpression,
                                  Map<String, String> names,
                                  Map<String, AttributeValue> values) {
        if (filterExpression == null || filterExpression.isBlank()) {
            return true;
        }
        String[] predicates = filterExpression.split("\\s+AND\\s+");
        for (String predicate : predicates) {
            String[] parts = predicate.split("\\s*=\\s*");
            if (parts.length != 2) {
                continue;
            }
            String nameToken = parts[0].trim();
            String valueToken = parts[1].trim();

            String attributeName = Optional.ofNullable(names)
                    .map(map -> map.get(nameToken))
                    .orElseGet(() -> nameToken.startsWith("#") ? nameToken.substring(1) : nameToken);

            AttributeValue expected = Optional.ofNullable(values).map(map -> map.get(valueToken)).orElse(null);
            AttributeValue actual = item.get(attributeName);
            if (!attributeEquals(actual, expected)) {
                return false;
            }
        }
        return true;
    }

    private boolean attributeEquals(AttributeValue actual, AttributeValue expected) {
        String actualValue = attributeString(actual);
        String expectedValue = attributeString(expected);
        return Objects.equals(actualValue, expectedValue);
    }

    private String attributeString(AttributeValue attributeValue) {
        if (attributeValue == null) {
            return null;
        }
        if (attributeValue.s() != null) {
            return attributeValue.s();
        }
        if (attributeValue.n() != null) {
            return attributeValue.n();
        }
        if (attributeValue.bool() != null) {
            return attributeValue.bool().toString();
        }
        return null;
    }

}

