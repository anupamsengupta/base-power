package com.power.base.dao.nosql.dynamodb.repository;

import com.power.base.dao.rdbms.jpa.repository.swap.SwapTradeSearchCriteria;
import com.power.base.datamodel.dto.financials.SwapPowerTradeDto;

import java.util.List;
import java.util.Optional;

public interface SwapTradeDynamoDao {

    SwapPowerTradeDto save(SwapPowerTradeDto tradeDto);

    Optional<SwapPowerTradeDto> findByTradeId(String tradeId);

    List<SwapPowerTradeDto> searchByCriteria(SwapTradeSearchCriteria criteria);

    void deleteByTradeId(String tradeId);
}

