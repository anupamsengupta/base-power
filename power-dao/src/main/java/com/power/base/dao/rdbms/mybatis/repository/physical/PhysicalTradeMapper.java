package com.power.base.dao.rdbms.mybatis.repository.physical;

import com.power.base.dao.rdbms.jpa.persistence.physical.PhysicalLineItemEntity;
import com.power.base.dao.rdbms.jpa.persistence.physical.PhysicalSettlementItemEntity;
import com.power.base.dao.rdbms.jpa.persistence.physical.PhysicalTradeEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * MyBatis mapper interface mirroring the JPA-based DAO contract for physical trades.
 */
public interface PhysicalTradeMapper {

    int upsert(PhysicalTradeEntity entity);

    Optional<PhysicalTradeEntity> findByTradeId(String tradeId);

    int deleteByTradeId(String tradeId);

    List<PhysicalTradeEntity> findByCriteria(PhysicalTradeSearchCriteria criteria);

    List<PhysicalLineItemEntity> selectLineItemsByTradeId(@Param("tradeId") String tradeId);

    List<PhysicalSettlementItemEntity> selectSettlementItemsByTradeId(@Param("tradeId") String tradeId);

    List<String> selectSettlementLineRefsBySettlementId(@Param("settlementItemId") Long settlementItemId);

    int deleteLineItemsByTradeId(@Param("tradeId") String tradeId);

    int deleteSettlementLineRefsByTradeId(@Param("tradeId") String tradeId);

    int deleteSettlementItemsByTradeId(@Param("tradeId") String tradeId);

    int insertLineItem(@Param("tradeId") String tradeId,
                       @Param("item") PhysicalLineItemEntity lineItem);

    int insertSettlementItem(@Param("tradeId") String tradeId,
                             @Param("item") PhysicalSettlementItemEntity settlementItem);

    int insertSettlementLineRef(@Param("settlementItemId") Long settlementItemId,
                                @Param("lineRef") String lineReference);
}
