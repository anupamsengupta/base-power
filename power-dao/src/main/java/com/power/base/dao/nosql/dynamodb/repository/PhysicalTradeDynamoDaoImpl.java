package com.power.base.dao.nosql.dynamodb.repository;

import com.power.base.dao.nosql.dynamodb.DynamoDbDaoException;
import com.power.base.dao.rdbms.jpa.repository.physical.PhysicalTradeSearchCriteria;
import com.power.base.datamodel.dto.common.Profile;
import com.power.base.datamodel.dto.physicals.PhysicalLineItemDto;
import com.power.base.datamodel.dto.physicals.PhysicalMetadataDto;
import com.power.base.datamodel.dto.physicals.PhysicalPowerTradeDto;
import com.power.base.datamodel.dto.physicals.PhysicalSettlementInfoDto;
import com.power.base.datamodel.dto.physicals.PhysicalSettlementItemDto;
import com.power.base.datamodel.dto.physicals.PhysicalTradeDetailsDto;
import com.power.base.datamodel.dto.physicals.PhysicalTradeHeaderDto;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsRequest;
import software.amazon.awssdk.services.dynamodb.model.WriteRequest;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@ConditionalOnBean(DynamoDbClient.class)
public class PhysicalTradeDynamoDaoImpl implements PhysicalTradeDynamoDao {

    static final String TABLE_NAME = "PHYSICAL_TRADE";

    private static final String ATTR_PK = "trade_id";
    private static final String ATTR_SK = "sort_key";
    private static final String ATTR_ENTITY_TYPE = "entity_type";
    private static final String ATTR_TENANT_ID = "tenant_id";

    private static final String ENTITY_HEADER = "HEADER";
    private static final String ENTITY_LINE_ITEM = "LINE_ITEM";
    private static final String ENTITY_SETTLEMENT_ITEM = "SETTLEMENT_ITEM";

    private static final int TRANSACT_WRITE_MAX_ITEMS = 25;

    private final DynamoDbClient dynamoDbClient;

    public PhysicalTradeDynamoDaoImpl(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    @Override
    public PhysicalPowerTradeDto save(PhysicalPowerTradeDto tradeDto) {
        requireHeader(tradeDto);

        String tradeId = tradeDto.getTradeHeader().getTradeId();

        // Remove any existing representation before writing the new graph
        deleteByTradeId(tradeId);

        List<Map<String, AttributeValue>> items = buildItems(tradeDto);
        writeInTransactions(items);
        return tradeDto;
    }

    @Override
    public Optional<PhysicalPowerTradeDto> findByTradeId(String tradeId) {
        try {
            List<Map<String, AttributeValue>> items = queryAllItems(tradeId);
            if (items.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(fromItems(tradeId, items));
        } catch (Exception ex) {
            throw new DynamoDbDaoException("Failed to load physical trade " + tradeId, ex);
        }
    }

    @Override
    public List<PhysicalPowerTradeDto> searchByCriteria(PhysicalTradeSearchCriteria criteria) {
        ScanRequest request = buildHeaderScanRequest(criteria);
        try {
            ScanResponse response = dynamoDbClient.scan(request);
            List<String> tradeIds = response.items()
                    .stream()
                    .map(item -> item.get(ATTR_PK))
                    .filter(Objects::nonNull)
                    .map(AttributeValue::s)
                    .collect(Collectors.toList());

            List<PhysicalPowerTradeDto> results = new ArrayList<>();
            for (String tradeId : tradeIds) {
                findByTradeId(tradeId)
                        .filter(dto -> matchesTemporalCriteria(dto, criteria))
                        .ifPresent(results::add);
            }
            return results;
        } catch (Exception ex) {
            throw new DynamoDbDaoException("Failed to search physical trades", ex);
        }
    }

    @Override
    public void deleteByTradeId(String tradeId) {
        try {
            List<Map<String, AttributeValue>> items = queryAllItems(tradeId);
            if (items.isEmpty()) {
                return;
            }

            List<WriteRequest> deletes = items.stream()
                    .map(item -> WriteRequest.builder()
                            .deleteRequest(DeleteRequest.builder()
                                    .key(Map.of(
                                            ATTR_PK, item.get(ATTR_PK),
                                            ATTR_SK, item.get(ATTR_SK)))
                                    .build())
                            .build())
                    .collect(Collectors.toList());

            for (List<WriteRequest> chunk : chunks(deletes, 25)) {
                dynamoDbClient.batchWriteItem(BatchWriteItemRequest.builder()
                        .requestItems(Map.of(TABLE_NAME, chunk))
                        .build());
            }
        } catch (Exception ex) {
            throw new DynamoDbDaoException("Failed to delete physical trade " + tradeId, ex);
        }
    }

    private void requireHeader(PhysicalPowerTradeDto dto) {
        PhysicalTradeHeaderDto header = dto.getTradeHeader();
        if (header == null || header.getTradeId() == null || header.getTradeId().isBlank()) {
            throw new DynamoDbDaoException("Physical trade header with tradeId is required");
        }
        if (header.getTenantId() == null || header.getTenantId().isBlank()) {
            throw new DynamoDbDaoException("Physical trade header must include tenantId");
        }
    }

    private List<Map<String, AttributeValue>> buildItems(PhysicalPowerTradeDto dto) {
        List<Map<String, AttributeValue>> items = new ArrayList<>();

        items.add(buildHeaderItem(dto));

        List<PhysicalLineItemDto> lineItems = Optional.ofNullable(dto.getTradeDetails())
                .map(PhysicalTradeDetailsDto::getLineItems)
                .orElse(List.of());
        for (int i = 0; i < lineItems.size(); i++) {
            items.add(buildLineItem(dto, lineItems.get(i), i + 1));
        }

        PhysicalSettlementInfoDto settlementInfo = dto.getSettlementInfo();
        List<PhysicalSettlementItemDto> settlementItems = settlementInfo == null
                ? List.of()
                : Optional.ofNullable(settlementInfo.getSettlementItems()).orElse(List.of());
        for (int i = 0; i < settlementItems.size(); i++) {
            items.add(buildSettlementItem(dto, settlementItems.get(i), i + 1));
        }

        return items;
    }

    private Map<String, AttributeValue> buildHeaderItem(PhysicalPowerTradeDto dto) {
        PhysicalTradeHeaderDto header = dto.getTradeHeader();
        PhysicalSettlementInfoDto settlement = dto.getSettlementInfo();

        Map<String, AttributeValue> item = new HashMap<>();
        item.put(ATTR_PK, AttributeValue.builder().s(header.getTradeId()).build());
        item.put(ATTR_SK, AttributeValue.builder().s("HEADER#").build());
        item.put(ATTR_ENTITY_TYPE, AttributeValue.builder().s(ENTITY_HEADER).build());
        putString(item, ATTR_TENANT_ID, header.getTenantId());

        putString(item, "business_unit", header.getBusinessUnit());
        putString(item, "market", header.getMarket());
        putString(item, "trader_name", header.getTraderName());
        putString(item, "agreement_id", header.getAgreementId());
        putString(item, "commodity", header.getCommodity());
        putString(item, "transaction_type", header.getTransactionType());
        putString(item, "delivery_point", header.getDeliveryPoint());
        putString(item, "load_type", header.getLoadType());
        putString(item, "book_strategy", header.getBookStrategy());
        putString(item, "document_type", header.getDocumentType() == null ? null : header.getDocumentType().getValue());
        putString(item, "document_version", header.getDocumentVersion());
        putString(item, "buy_sell_indicator", header.getBuySellIndicator() == null ? null : header.getBuySellIndicator().name());
        putBoolean(item, "amendment_indicator", header.isAmendmentIndicator());
        putDate(item, "trade_date", header.getTradeDate());
        putInstant(item, "trade_time", header.getTradeTime());

        if (header.getBuyerParty() != null) {
            putString(item, "buyer_party_id", header.getBuyerParty().getId());
            putString(item, "buyer_party_name", header.getBuyerParty().getName());
            putString(item, "buyer_party_role", header.getBuyerParty().getRole());
        }
        if (header.getSellerParty() != null) {
            putString(item, "seller_party_id", header.getSellerParty().getId());
            putString(item, "seller_party_name", header.getSellerParty().getName());
            putString(item, "seller_party_role", header.getSellerParty().getRole());
        }

        if (settlement != null) {
            putNumber(item, "settlement_total_volume", settlement.getTotalVolume());
            putString(item, "settlement_total_volume_uom", settlement.getTotalVolumeUom());
            putString(item, "settlement_pricing_mechanism", settlement.getPricingMechanism());
            putNumber(item, "settlement_price", settlement.getSettlementPrice());
            putNumber(item, "trade_price", settlement.getTradePrice());
            putString(item, "settlement_currency", settlement.getSettlementCurrency());
            putString(item, "trade_currency", settlement.getTradeCurrency());
            putString(item, "settlement_uom", settlement.getSettlementUom());
            putString(item, "trade_uom", settlement.getTradeUom());
            putDate(item, "settlement_start_date", settlement.getStartApplicabilityDate());
            putInstant(item, "settlement_start_time", settlement.getStartApplicabilityTime());
            putDate(item, "settlement_end_date", settlement.getEndApplicabilityDate());
            putInstant(item, "settlement_end_time", settlement.getEndApplicabilityTime());
            putString(item, "settlement_payment_event", settlement.getPaymentEvent());
            putNumber(item, "settlement_payment_offset", settlement.getPaymentOffset());
            putNumber(item, "settlement_total_value", settlement.getTotalContractValue());
            putNumber(item, "settlement_rounding", settlement.getRounding());
        }

        if (dto.getMetadata() != null) {
            putDate(item, "metadata_effective_date", dto.getMetadata().getEffectiveDate());
            putDate(item, "metadata_termination_date", dto.getMetadata().getTerminationDate());
            putString(item, "metadata_governing_law", dto.getMetadata().getGoverningLaw());
        }

        return item;
    }

    private Map<String, AttributeValue> buildLineItem(PhysicalPowerTradeDto dto, PhysicalLineItemDto lineItem, int sequence) {
        PhysicalTradeHeaderDto header = dto.getTradeHeader();

        Map<String, AttributeValue> item = new HashMap<>();
        item.put(ATTR_PK, AttributeValue.builder().s(header.getTradeId()).build());
        item.put(ATTR_SK, AttributeValue.builder().s(String.format("LINE#%04d", sequence)).build());
        item.put(ATTR_ENTITY_TYPE, AttributeValue.builder().s(ENTITY_LINE_ITEM).build());
        putString(item, ATTR_TENANT_ID, header.getTenantId());

        putDate(item, "period_start_date", lineItem.getPeriodStartDate());
        putInstant(item, "period_start_time", lineItem.getPeriodStartTime());
        putDate(item, "period_end_date", lineItem.getPeriodEndDate());
        putInstant(item, "period_end_time", lineItem.getPeriodEndTime());
        putString(item, "day_hour_label", lineItem.getDayHour());
        putNumber(item, "quantity", lineItem.getQuantity());
        putString(item, "uom", lineItem.getUom());
        putNumber(item, "capacity", lineItem.getCapacity());
        putString(item, "profile", lineItem.getProfile() == null ? null : lineItem.getProfile().name());

        return item;
    }

    private Map<String, AttributeValue> buildSettlementItem(PhysicalPowerTradeDto dto, PhysicalSettlementItemDto settlementItem, int sequence) {
        PhysicalTradeHeaderDto header = dto.getTradeHeader();

        Map<String, AttributeValue> item = new HashMap<>();
        item.put(ATTR_PK, AttributeValue.builder().s(header.getTradeId()).build());
        item.put(ATTR_SK, AttributeValue.builder().s("SETTLEMENT#" + settlementItem.getSettlementId()).build());
        item.put(ATTR_ENTITY_TYPE, AttributeValue.builder().s(ENTITY_SETTLEMENT_ITEM).build());
        putString(item, ATTR_TENANT_ID, header.getTenantId());

        putString(item, "settlement_id", settlementItem.getSettlementId());
        putDate(item, "delivery_date", settlementItem.getDeliveryDate());
        putNumber(item, "actual_quantity", settlementItem.getActualQuantity());
        putString(item, "uom", settlementItem.getUom());
        putNumber(item, "settlement_price", settlementItem.getSettlementPrice());
        putNumber(item, "trade_price", settlementItem.getTradePrice());
        putString(item, "settlement_uom", settlementItem.getSettlementUom());
        putString(item, "trade_uom", settlementItem.getTradeUom());
        putNumber(item, "deviation_amount", settlementItem.getDeviationAmount());
        putNumber(item, "deviation_penalty", settlementItem.getDeviationPenalty());
        putNumber(item, "period_cashflow", settlementItem.getPeriodCashflow());
        putString(item, "settlement_currency", settlementItem.getSettlementCurrency());
        putString(item, "trade_currency", settlementItem.getTradeCurrency());
        putString(item, "invoice_status", settlementItem.getInvoiceStatus());

        if (settlementItem.getReferencedLineItems() != null && !settlementItem.getReferencedLineItems().isEmpty()) {
            item.put("line_refs", AttributeValue.builder().ss(settlementItem.getReferencedLineItems()).build());
        }

        return item;
    }

    private List<Map<String, AttributeValue>> queryAllItems(String tradeId) {
        List<Map<String, AttributeValue>> results = new ArrayList<>();
        Map<String, AttributeValue> keyValues = Map.of(":tradeId", AttributeValue.builder().s(tradeId).build());

        QueryRequest request = QueryRequest.builder()
                .tableName(TABLE_NAME)
                .keyConditionExpression(ATTR_PK + " = :tradeId")
                .expressionAttributeValues(keyValues)
                .build();

        QueryResponse response = dynamoDbClient.query(request);
        results.addAll(response.items());
        while (response.hasLastEvaluatedKey() && !response.lastEvaluatedKey().isEmpty()) {
            request = request.toBuilder()
                    .exclusiveStartKey(response.lastEvaluatedKey())
                    .build();
            response = dynamoDbClient.query(request);
            results.addAll(response.items());
        }
        return results;
    }

    private PhysicalPowerTradeDto fromItems(String tradeId, List<Map<String, AttributeValue>> items) {
        Map<String, AttributeValue> headerItem = items.stream()
                .filter(item -> ENTITY_HEADER.equals(attrString(item, ATTR_ENTITY_TYPE)))
                .findFirst()
                .orElseThrow(() -> new DynamoDbDaoException("Missing header record for trade " + tradeId));

        PhysicalTradeHeaderDto headerDto = buildHeaderDto(tradeId, headerItem);
        PhysicalTradeDetailsDto detailsDto = new PhysicalTradeDetailsDto(
                items.stream()
                        .filter(item -> ENTITY_LINE_ITEM.equals(attrString(item, ATTR_ENTITY_TYPE)))
                        .sorted((a, b) -> attrString(a, ATTR_SK).compareTo(attrString(b, ATTR_SK)))
                        .map(this::buildLineItemDto)
                        .collect(Collectors.toList())
        );

        PhysicalSettlementInfoDto settlementInfoDto = buildSettlementInfoDto(headerItem,
                items.stream()
                        .filter(item -> ENTITY_SETTLEMENT_ITEM.equals(attrString(item, ATTR_ENTITY_TYPE)))
                        .map(this::buildSettlementItemDto)
                        .collect(Collectors.toList()));

        PhysicalMetadataDto metadataDto = buildMetadataDto(headerItem);

        return new PhysicalPowerTradeDto(
                headerDto,
                detailsDto,
                settlementInfoDto,
                metadataDto
        );
    }

    private PhysicalTradeHeaderDto buildHeaderDto(String tradeId, Map<String, AttributeValue> item) {
        PhysicalTradeHeaderDto header = new PhysicalTradeHeaderDto();
        header.setTradeId(tradeId);
        header.setTenantId(attrString(item, ATTR_TENANT_ID));
        header.setBusinessUnit(attrString(item, "business_unit"));
        header.setBookStrategy(attrString(item, "book_strategy"));
        header.setTraderName(attrString(item, "trader_name"));
        header.setAgreementId(attrString(item, "agreement_id"));
        header.setMarket(attrString(item, "market"));
        header.setCommodity(attrString(item, "commodity"));
        header.setTransactionType(attrString(item, "transaction_type"));
        header.setDeliveryPoint(attrString(item, "delivery_point"));
        header.setLoadType(attrString(item, "load_type"));
        header.setDocumentVersion(attrString(item, "document_version"));
        header.setTradeDate(attrLocalDate(item, "trade_date"));
        header.setTradeTime(attrInstant(item, "trade_time"));
        header.setAmendmentIndicator(attrBoolean(item, "amendment_indicator"));
        header.setDocumentType(attrDocumentType(item, "document_type"));
        header.setBuySellIndicator(attrBuySell(item, "buy_sell_indicator"));
        header.setBuyerParty(attrParty(item, "buyer_party_id", "buyer_party_name", "buyer_party_role"));
        header.setSellerParty(attrParty(item, "seller_party_id", "seller_party_name", "seller_party_role"));

        return header;
    }

    private PhysicalMetadataDto buildMetadataDto(Map<String, AttributeValue> item) {
        if (item.get("metadata_effective_date") == null
                && item.get("metadata_termination_date") == null
                && item.get("metadata_governing_law") == null) {
            return new PhysicalMetadataDto();
        }
        PhysicalMetadataDto metadata = new PhysicalMetadataDto();
        metadata.setEffectiveDate(attrLocalDate(item, "metadata_effective_date"));
        metadata.setTerminationDate(attrLocalDate(item, "metadata_termination_date"));
        metadata.setGoverningLaw(attrString(item, "metadata_governing_law"));
        return metadata;
    }

    private PhysicalSettlementInfoDto buildSettlementInfoDto(Map<String, AttributeValue> headerItem,
                                                             List<PhysicalSettlementItemDto> settlementItems) {
        PhysicalSettlementInfoDto settlementInfo = new PhysicalSettlementInfoDto();
        settlementInfo.setTotalVolume(attrDouble(headerItem, "settlement_total_volume"));
        settlementInfo.setTotalVolumeUom(attrString(headerItem, "settlement_total_volume_uom"));
        settlementInfo.setPricingMechanism(attrString(headerItem, "settlement_pricing_mechanism"));
        settlementInfo.setSettlementPrice(attrDouble(headerItem, "settlement_price"));
        settlementInfo.setTradePrice(attrDouble(headerItem, "trade_price"));
        settlementInfo.setSettlementCurrency(attrString(headerItem, "settlement_currency"));
        settlementInfo.setTradeCurrency(attrString(headerItem, "trade_currency"));
        settlementInfo.setSettlementUom(attrString(headerItem, "settlement_uom"));
        settlementInfo.setTradeUom(attrString(headerItem, "trade_uom"));
        settlementInfo.setStartApplicabilityDate(attrLocalDate(headerItem, "settlement_start_date"));
        settlementInfo.setStartApplicabilityTime(attrInstant(headerItem, "settlement_start_time"));
        settlementInfo.setEndApplicabilityDate(attrLocalDate(headerItem, "settlement_end_date"));
        settlementInfo.setEndApplicabilityTime(attrInstant(headerItem, "settlement_end_time"));
        settlementInfo.setPaymentEvent(attrString(headerItem, "settlement_payment_event"));
        settlementInfo.setPaymentOffset(attrDouble(headerItem, "settlement_payment_offset"));
        settlementInfo.setTotalContractValue(attrDouble(headerItem, "settlement_total_value"));
        settlementInfo.setRounding(attrDouble(headerItem, "settlement_rounding"));
        settlementInfo.setSettlementItems(settlementItems);
        return settlementInfo;
    }

    private PhysicalSettlementItemDto buildSettlementItemDto(Map<String, AttributeValue> item) {
        PhysicalSettlementItemDto dto = new PhysicalSettlementItemDto();
        dto.setSettlementId(attrString(item, "settlement_id"));
        dto.setDeliveryDate(attrLocalDate(item, "delivery_date"));
        dto.setActualQuantity(attrDouble(item, "actual_quantity"));
        dto.setUom(attrString(item, "uom"));
        dto.setSettlementPrice(attrDouble(item, "settlement_price"));
        dto.setTradePrice(attrDouble(item, "trade_price"));
        dto.setSettlementUom(attrString(item, "settlement_uom"));
        dto.setTradeUom(attrString(item, "trade_uom"));
        dto.setDeviationAmount(attrDouble(item, "deviation_amount"));
        dto.setDeviationPenalty(attrDouble(item, "deviation_penalty"));
        dto.setPeriodCashflow(attrDouble(item, "period_cashflow"));
        dto.setSettlementCurrency(attrString(item, "settlement_currency"));
        dto.setTradeCurrency(attrString(item, "trade_currency"));
        dto.setInvoiceStatus(attrString(item, "invoice_status"));
        if (item.containsKey("line_refs") && item.get("line_refs").ss() != null) {
            dto.setReferencedLineItems(new ArrayList<>(item.get("line_refs").ss()));
        }
        return dto;
    }

    private PhysicalLineItemDto buildLineItemDto(Map<String, AttributeValue> item) {
        PhysicalLineItemDto dto = new PhysicalLineItemDto();
        dto.setPeriodStartDate(attrLocalDate(item, "period_start_date"));
        dto.setPeriodStartTime(attrInstant(item, "period_start_time"));
        dto.setPeriodEndDate(attrLocalDate(item, "period_end_date"));
        dto.setPeriodEndTime(attrInstant(item, "period_end_time"));
        dto.setDayHour(attrString(item, "day_hour_label"));
        dto.setQuantity(attrDouble(item, "quantity"));
        dto.setUom(attrString(item, "uom"));
        dto.setCapacity(attrDouble(item, "capacity"));
        dto.setProfile(attrProfile(item, "profile"));
        return dto;
    }

    private ScanRequest buildHeaderScanRequest(PhysicalTradeSearchCriteria criteria) {
        ScanRequest.Builder builder = ScanRequest.builder()
                .tableName(TABLE_NAME);

        Map<String, String> names = new LinkedHashMap<>();
        Map<String, AttributeValue> values = new LinkedHashMap<>();
        List<String> expressions = new ArrayList<>();

        names.put("#entity", ATTR_ENTITY_TYPE);
        values.put(":header", AttributeValue.builder().s(ENTITY_HEADER).build());
        expressions.add("#entity = :header");

        criteria.getTenantId().ifPresent(value -> equalityFilter("tenant_id", value, names, values, expressions));
        criteria.getBusinessUnit().ifPresent(value -> equalityFilter("business_unit", value, names, values, expressions));
        criteria.getMarket().ifPresent(value -> equalityFilter("market", value, names, values, expressions));
        criteria.getTraderName().ifPresent(value -> equalityFilter("trader_name", value, names, values, expressions));
        criteria.getAgreementId().ifPresent(value -> equalityFilter("agreement_id", value, names, values, expressions));
        criteria.getCommodity().ifPresent(value -> equalityFilter("commodity", value, names, values, expressions));
        criteria.getTransactionType().ifPresent(value -> equalityFilter("transaction_type", value, names, values, expressions));
        criteria.getTradeDate().ifPresent(value -> equalityFilter("trade_date", value.toString(), names, values, expressions));

        builder = builder.filterExpression(String.join(" AND ", expressions));
        builder = builder.expressionAttributeNames(names);
        builder = builder.expressionAttributeValues(values);
        return builder.build();
    }

    private void writeInTransactions(List<Map<String, AttributeValue>> items) {
        for (List<Map<String, AttributeValue>> chunk : chunks(items, TRANSACT_WRITE_MAX_ITEMS)) {
            List<TransactWriteItem> writes = chunk.stream()
                    .map(item -> TransactWriteItem.builder()
                            .put(builder -> builder.tableName(TABLE_NAME).item(item))
                            .build())
                    .collect(Collectors.toList());

            dynamoDbClient.transactWriteItems(TransactWriteItemsRequest.builder()
                    .transactItems(writes)
                    .build());
        }
    }

    private boolean matchesTemporalCriteria(PhysicalPowerTradeDto dto, PhysicalTradeSearchCriteria criteria) {
        return criteria.getTradeTimeFrom()
                .map(from -> Optional.ofNullable(dto.getTradeHeader())
                        .map(PhysicalTradeHeaderDto::getTradeTime)
                        .map(time -> !time.isBefore(from))
                        .orElse(false))
                .orElse(true)
                && criteria.getTradeTimeTo()
                .map(to -> Optional.ofNullable(dto.getTradeHeader())
                        .map(PhysicalTradeHeaderDto::getTradeTime)
                        .map(time -> !time.isAfter(to))
                        .orElse(false))
                .orElse(true);
    }

    private void equalityFilter(String attribute,
                                String value,
                                Map<String, String> names,
                                Map<String, AttributeValue> values,
                                List<String> expressions) {
        String placeholderName = "#attr" + expressions.size();
        String placeholderValue = ":val" + expressions.size();
        names.put(placeholderName, attribute);
        values.put(placeholderValue, AttributeValue.builder().s(value).build());
        expressions.add(placeholderName + " = " + placeholderValue);
    }

    private void putString(Map<String, AttributeValue> item, String key, String value) {
        if (value != null && !value.isBlank()) {
            item.put(key, AttributeValue.builder().s(value).build());
        }
    }

    private void putDate(Map<String, AttributeValue> item, String key, LocalDate value) {
        if (value != null) {
            item.put(key, AttributeValue.builder().s(value.toString()).build());
        }
    }

    private void putInstant(Map<String, AttributeValue> item, String key, Instant value) {
        if (value != null) {
            item.put(key, AttributeValue.builder().s(value.toString()).build());
        }
    }

    private void putNumber(Map<String, AttributeValue> item, String key, double value) {
        if (!Double.isNaN(value)) {
            item.put(key, AttributeValue.builder().n(Double.toString(value)).build());
        }
    }

    private void putBoolean(Map<String, AttributeValue> item, String key, boolean value) {
        item.put(key, AttributeValue.builder().bool(value).build());
    }

    private String attrString(Map<String, AttributeValue> item, String key) {
        AttributeValue value = item.get(key);
        return value == null ? null : value.s();
    }

    private double attrDouble(Map<String, AttributeValue> item, String key) {
        AttributeValue value = item.get(key);
        return value == null || value.n() == null ? 0d : Double.parseDouble(value.n());
    }

    private LocalDate attrLocalDate(Map<String, AttributeValue> item, String key) {
        AttributeValue value = item.get(key);
        return value == null || value.s() == null ? null : LocalDate.parse(value.s());
    }

    private Instant attrInstant(Map<String, AttributeValue> item, String key) {
        AttributeValue value = item.get(key);
        return value == null || value.s() == null ? null : Instant.parse(value.s());
    }

    private boolean attrBoolean(Map<String, AttributeValue> item, String key) {
        AttributeValue value = item.get(key);
        return value != null && Boolean.TRUE.equals(value.bool());
    }

    private Profile attrProfile(Map<String, AttributeValue> item, String key) {
        String value = attrString(item, key);
        return value == null ? null : Profile.valueOf(value);
    }

    private com.power.base.datamodel.dto.common.DocumentType attrDocumentType(Map<String, AttributeValue> item, String key) {
        String value = attrString(item, key);
        return value == null ? null : com.power.base.datamodel.dto.common.DocumentType.fromValue(value);
    }

    private com.power.base.datamodel.dto.common.BuySellIndicator attrBuySell(Map<String, AttributeValue> item, String key) {
        String value = attrString(item, key);
        return value == null ? null : com.power.base.datamodel.dto.common.BuySellIndicator.valueOf(value);
    }

    private com.power.base.datamodel.dto.common.PartyDto attrParty(Map<String, AttributeValue> item,
                                                                   String idKey,
                                                                   String nameKey,
                                                                   String roleKey) {
        String id = attrString(item, idKey);
        String name = attrString(item, nameKey);
        String role = attrString(item, roleKey);
        if (id == null && name == null && role == null) {
            return null;
        }
        return new com.power.base.datamodel.dto.common.PartyDto(id, name, role);
    }

    private <T> List<List<T>> chunks(List<T> source, int size) {
        List<List<T>> result = new ArrayList<>();
        for (int i = 0; i < source.size(); i += size) {
            result.add(new ArrayList<>(source.subList(i, Math.min(i + size, source.size()))));
        }
        return result;
    }
}