package com.power.base.dao.nosql.dynamodb.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.power.base.dao.nosql.dynamodb.DynamoDbDaoException;
import com.power.base.dao.rdbms.jpa.repository.swap.SwapTradeSearchCriteria;
import com.power.base.datamodel.dto.financials.SwapPowerTradeDto;
import com.power.base.datamodel.dto.financials.SwapTradeHeaderDto;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@ConditionalOnBean(DynamoDbClient.class)
public class SwapTradeDynamoDaoImpl implements SwapTradeDynamoDao {

    static final String TABLE_NAME = "SWAP_TRADE";
    static final String TRADE_ID = "trade_id";
    static final String PAYLOAD = "payload";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private final DynamoDbClient dynamoDbClient;

    public SwapTradeDynamoDaoImpl(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    @Override
    public SwapPowerTradeDto save(SwapPowerTradeDto tradeDto) {
        requireHeader(tradeDto);
        Map<String, AttributeValue> item = toItem(tradeDto);
        try {
            dynamoDbClient.putItem(PutItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .item(item)
                    .build());
            return tradeDto;
        } catch (Exception ex) {
            throw new DynamoDbDaoException("Failed to persist swap trade " + tradeDto.getTradeHeader().getTradeId(), ex);
        }
    }

    @Override
    public Optional<SwapPowerTradeDto> findByTradeId(String tradeId) {
        Map<String, AttributeValue> key = Map.of(TRADE_ID, AttributeValue.builder().s(tradeId).build());
        GetItemRequest request = GetItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .build();
        try {
            return Optional.ofNullable(dynamoDbClient.getItem(request).item())
                    .filter(item -> !item.isEmpty())
                    .map(this::fromItem);
        } catch (Exception ex) {
            throw new DynamoDbDaoException("Failed to load swap trade " + tradeId, ex);
        }
    }

    @Override
    public List<SwapPowerTradeDto> searchByCriteria(SwapTradeSearchCriteria criteria) {
        ScanRequest scanRequest = buildScanRequest(criteria);
        try {
            ScanResponse response = dynamoDbClient.scan(scanRequest);
            return response.items().stream()
                    .map(this::fromItem)
                    .filter(dto -> matchesTemporalCriteria(dto, criteria))
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new DynamoDbDaoException("Failed to search swap trades", ex);
        }
    }

    @Override
    public void deleteByTradeId(String tradeId) {
        Map<String, AttributeValue> key = Map.of(TRADE_ID, AttributeValue.builder().s(tradeId).build());
        DeleteItemRequest request = DeleteItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .build();
        try {
            dynamoDbClient.deleteItem(request);
        } catch (Exception ex) {
            throw new DynamoDbDaoException("Failed to delete swap trade " + tradeId, ex);
        }
    }

    private void requireHeader(SwapPowerTradeDto dto) {
        SwapTradeHeaderDto header = dto.getTradeHeader();
        if (header == null || header.getTradeId() == null || header.getTradeId().isBlank()) {
            throw new DynamoDbDaoException("Swap trade header with tradeId is required");
        }
        if (header.getTenantId() == null || header.getTenantId().isBlank()) {
            throw new DynamoDbDaoException("Swap trade header must include tenantId");
        }
    }

    private Map<String, AttributeValue> toItem(SwapPowerTradeDto dto) {
        Map<String, AttributeValue> item = new HashMap<>();
        SwapTradeHeaderDto header = dto.getTradeHeader();

        item.put(TRADE_ID, AttributeValue.builder().s(header.getTradeId()).build());
        item.put(PAYLOAD, AttributeValue.builder().s(writePayload(dto)).build());

        putIfPresent(item, "tenant_id", header.getTenantId());
        putIfPresent(item, "business_unit", header.getBusinessUnit());
        putIfPresent(item, "market", header.getMarket());
        putIfPresent(item, "trader_name", header.getTraderName());
        putIfPresent(item, "agreement_id", header.getAgreementId());
        putIfPresent(item, "commodity", header.getCommodity());
        putIfPresent(item, "transaction_type", header.getTransactionType());
        putIfPresent(item, "reference_zone", header.getReferenceZone());
        putIfPresent(item, "trade_date", Optional.ofNullable(header.getTradeDate()).map(Object::toString).orElse(null));
        putIfPresent(item, "trade_time", Optional.ofNullable(header.getTradeTime()).map(Instant::toString).orElse(null));

        return item;
    }

    private String writePayload(SwapPowerTradeDto dto) {
        try {
            return OBJECT_MAPPER.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new DynamoDbDaoException("Failed to serialize swap trade payload", e);
        }
    }

    private SwapPowerTradeDto fromItem(Map<String, AttributeValue> item) {
        AttributeValue payload = item.get(PAYLOAD);
        if (payload == null || payload.s() == null) {
            throw new DynamoDbDaoException("Missing payload attribute for swap trade record");
        }
        try {
            SwapPowerTradeDto dto = OBJECT_MAPPER.readValue(payload.s(), SwapPowerTradeDto.class);
            if (dto.getTradeHeader() != null && (dto.getTradeHeader().getTenantId() == null || dto.getTradeHeader().getTenantId().isBlank())) {
                AttributeValue tenantAttr = item.get("tenant_id");
                if (tenantAttr != null && tenantAttr.s() != null) {
                    dto.getTradeHeader().setTenantId(tenantAttr.s());
                }
            }
            return dto;
        } catch (IOException e) {
            throw new DynamoDbDaoException("Failed to deserialize swap trade payload", e);
        }
    }

    private ScanRequest buildScanRequest(SwapTradeSearchCriteria criteria) {
        ScanRequest.Builder builder = ScanRequest.builder().tableName(TABLE_NAME);
        Map<String, String> names = new HashMap<>();
        Map<String, AttributeValue> values = new HashMap<>();
        List<String> expressions = new ArrayList<>();

        criteria.getTenantId().ifPresent(value -> equalityFilter("tenant_id", value, names, values, expressions));
        criteria.getBusinessUnit().ifPresent(value -> equalityFilter("business_unit", value, names, values, expressions));
        criteria.getMarket().ifPresent(value -> equalityFilter("market", value, names, values, expressions));
        criteria.getTraderName().ifPresent(value -> equalityFilter("trader_name", value, names, values, expressions));
        criteria.getAgreementId().ifPresent(value -> equalityFilter("agreement_id", value, names, values, expressions));
        criteria.getCommodity().ifPresent(value -> equalityFilter("commodity", value, names, values, expressions));
        criteria.getTransactionType().ifPresent(value -> equalityFilter("transaction_type", value, names, values, expressions));
        criteria.getReferenceZone().ifPresent(value -> equalityFilter("reference_zone", value, names, values, expressions));
        criteria.getTradeDate().ifPresent(value -> equalityFilter("trade_date", value.toString(), names, values, expressions));

        if (!expressions.isEmpty()) {
            builder = builder.filterExpression(String.join(" AND ", expressions));
            if (!names.isEmpty()) {
                builder = builder.expressionAttributeNames(names);
            }
            if (!values.isEmpty()) {
                builder = builder.expressionAttributeValues(values);
            }
        }
        return builder.build();
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

    private boolean matchesTemporalCriteria(SwapPowerTradeDto dto, SwapTradeSearchCriteria criteria) {
        return criteria.getTradeTimeFrom()
                .map(from -> Optional.ofNullable(dto.getTradeHeader())
                        .map(SwapTradeHeaderDto::getTradeTime)
                        .map(time -> !time.isBefore(from))
                        .orElse(false))
                .orElse(true)
                && criteria.getTradeTimeTo()
                .map(to -> Optional.ofNullable(dto.getTradeHeader())
                        .map(SwapTradeHeaderDto::getTradeTime)
                        .map(time -> !time.isAfter(to))
                        .orElse(false))
                .orElse(true);
    }

    private void putIfPresent(Map<String, AttributeValue> item, String key, String value) {
        if (value != null && !value.isBlank()) {
            item.put(key, AttributeValue.builder().s(value).build());
        }
    }
}

