package com.power.base.dao.nosql.dynamodb.repository;

import com.power.base.dao.rdbms.jpa.repository.physical.PhysicalTradeSearchCriteria;
import com.power.base.datamodel.dto.physicals.PhysicalPowerTradeDto;

import java.util.List;
import java.util.Optional;

public interface PhysicalTradeDynamoDao {

    PhysicalPowerTradeDto save(PhysicalPowerTradeDto tradeDto);

    Optional<PhysicalPowerTradeDto> findByTradeId(String tradeId);

    List<PhysicalPowerTradeDto> searchByCriteria(PhysicalTradeSearchCriteria criteria);

    void deleteByTradeId(String tradeId);
}

