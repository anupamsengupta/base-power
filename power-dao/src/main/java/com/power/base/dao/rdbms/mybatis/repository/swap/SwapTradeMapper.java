package com.power.base.dao.rdbms.mybatis.repository.swap;

import com.power.base.dao.rdbms.jpa.persistence.swap.SwapPeriodEntity;
import com.power.base.dao.rdbms.jpa.persistence.swap.SwapTradeEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * MyBatis mapper interface mirroring the JPA-based DAO contract for swap trades.
 */
public interface SwapTradeMapper {

    int upsert(SwapTradeEntity entity);

    Optional<SwapTradeEntity> findByTradeId(String tradeId);

    int deleteByTradeId(String tradeId);

    List<SwapTradeEntity> findByCriteria(SwapTradeSearchCriteria criteria);

    List<SwapPeriodEntity> selectPeriodsByTradeId(@Param("tradeId") String tradeId);

    int deletePeriodsByTradeId(@Param("tradeId") String tradeId);

    int insertPeriod(@Param("tradeId") String tradeId,
                     @Param("period") SwapPeriodEntity period);
}
