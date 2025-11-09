package com.power.base.dao.rdbms.mybatis.service;

import com.power.base.dao.rdbms.jpa.persistence.physical.PhysicalLineItemEntity;
import com.power.base.dao.rdbms.jpa.persistence.physical.PhysicalSettlementItemEntity;
import com.power.base.dao.rdbms.jpa.persistence.physical.PhysicalTradeEntity;
import com.power.base.dao.rdbms.mybatis.repository.physical.PhysicalTradeMapper;
import com.power.base.dao.rdbms.mybatis.repository.physical.PhysicalTradeSearchCriteria;
import com.power.base.datamodel.dto.physicals.PhysicalPowerTradeDto;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MyBatis-backed service facade for physical trades.
 */
public class PhysicalTradeMyBatisService {

    private final PhysicalTradeMapper mapper;

    public PhysicalTradeMyBatisService(PhysicalTradeMapper mapper) {
        this.mapper = mapper;
    }

    public PhysicalPowerTradeDto persist(PhysicalPowerTradeDto tradeDto) {
        PhysicalTradeEntity entity = PhysicalTradeEntity.fromDto(tradeDto);
        String tradeId = entity.getTradeId();

        mapper.upsert(entity);

        // Replace child collections
        mapper.deleteSettlementLineRefsByTradeId(tradeId);
        mapper.deleteSettlementItemsByTradeId(tradeId);
        mapper.deleteLineItemsByTradeId(tradeId);

        if (entity.getLineItems() != null) {
            for (PhysicalLineItemEntity lineItem : entity.getLineItems()) {
                mapper.insertLineItem(tradeId, lineItem);
            }
        }

        if (entity.getSettlementItems() != null) {
            for (PhysicalSettlementItemEntity settlementItem : entity.getSettlementItems()) {
                mapper.insertSettlementItem(tradeId, settlementItem);
                Long settlementItemId = settlementItem.getId();
                if (settlementItem.getReferencedLineItems() != null) {
                    for (String lineRef : settlementItem.getReferencedLineItems()) {
                        mapper.insertSettlementLineRef(settlementItemId, lineRef);
                    }
                }
            }
        }

        return mapper.findByTradeId(tradeId)
                .map(PhysicalTradeEntity::toDto)
                .orElseGet(entity::toDto);
    }

    public Optional<PhysicalPowerTradeDto> findByTradeId(String tradeId) {
        return mapper.findByTradeId(tradeId)
                .map(PhysicalTradeEntity::toDto);
    }

    public List<PhysicalPowerTradeDto> searchByCriteria(PhysicalTradeSearchCriteria criteria) {
        return mapper.findByCriteria(criteria).stream()
                .map(PhysicalTradeEntity::toDto)
                .collect(Collectors.toList());
    }

    public void deleteByTradeId(String tradeId) {
        mapper.deleteSettlementLineRefsByTradeId(tradeId);
        mapper.deleteSettlementItemsByTradeId(tradeId);
        mapper.deleteLineItemsByTradeId(tradeId);
        mapper.deleteByTradeId(tradeId);
    }
}
