package com.power.base.dao.rdbms.mybatis.service;

import com.power.base.dao.rdbms.jpa.persistence.swap.SwapPeriodEntity;
import com.power.base.dao.rdbms.jpa.persistence.swap.SwapTradeEntity;
import com.power.base.dao.rdbms.mybatis.repository.swap.SwapTradeMapper;
import com.power.base.dao.rdbms.mybatis.repository.swap.SwapTradeSearchCriteria;
import com.power.base.datamodel.dto.financials.SwapPowerTradeDto;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MyBatis-backed service facade for financial swap trades.
 */
public class SwapTradeMyBatisService {

    private final SwapTradeMapper mapper;

    public SwapTradeMyBatisService(SwapTradeMapper mapper) {
        this.mapper = mapper;
    }

    public SwapPowerTradeDto persist(SwapPowerTradeDto tradeDto) {
        SwapTradeEntity entity = SwapTradeEntity.fromDto(tradeDto);
        String tradeId = entity.getTradeId();

        mapper.upsert(entity);

        mapper.deletePeriodsByTradeId(tradeId);
        if (entity.getPeriods() != null) {
            for (SwapPeriodEntity period : entity.getPeriods()) {
                mapper.insertPeriod(tradeId, period);
            }
        }

        return mapper.findByTradeId(tradeId)
                .map(SwapTradeEntity::toDto)
                .orElseGet(entity::toDto);
    }

    public Optional<SwapPowerTradeDto> findByTradeId(String tradeId) {
        return mapper.findByTradeId(tradeId)
                .map(SwapTradeEntity::toDto);
    }

    public List<SwapPowerTradeDto> searchByCriteria(SwapTradeSearchCriteria criteria) {
        return mapper.findByCriteria(criteria).stream()
                .map(SwapTradeEntity::toDto)
                .collect(Collectors.toList());
    }

    public void deleteByTradeId(String tradeId) {
        mapper.deletePeriodsByTradeId(tradeId);
        mapper.deleteByTradeId(tradeId);
    }
}
