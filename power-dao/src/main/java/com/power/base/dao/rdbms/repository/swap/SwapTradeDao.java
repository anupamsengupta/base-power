package com.power.base.dao.rdbms.repository.swap;

import com.power.base.dao.rdbms.persistence.swap.SwapTradeEntity;

import java.util.List;
import java.util.Optional;

public interface SwapTradeDao {

    SwapTradeEntity save(SwapTradeEntity entity);

    Optional<SwapTradeEntity> findByTradeId(String tradeId);

    void deleteByTradeId(String tradeId);

    List<SwapTradeEntity> findByCriteria(SwapTradeSearchCriteria criteria);
}

