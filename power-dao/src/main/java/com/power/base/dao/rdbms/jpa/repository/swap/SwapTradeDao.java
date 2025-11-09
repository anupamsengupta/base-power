package com.power.base.dao.rdbms.jpa.repository.swap;

import com.power.base.dao.rdbms.jpa.persistence.swap.SwapTradeEntity;

import java.util.List;
import java.util.Optional;

public interface SwapTradeDao {

    SwapTradeEntity save(SwapTradeEntity entity);

    Optional<SwapTradeEntity> findByTradeId(String tradeId);

    void deleteByTradeId(String tradeId);

    List<SwapTradeEntity> findByCriteria(SwapTradeSearchCriteria criteria);
}

