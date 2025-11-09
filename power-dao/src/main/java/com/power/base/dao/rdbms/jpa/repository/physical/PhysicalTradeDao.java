package com.power.base.dao.rdbms.jpa.repository.physical;

import com.power.base.dao.rdbms.jpa.persistence.physical.PhysicalTradeEntity;

import java.util.List;
import java.util.Optional;

public interface PhysicalTradeDao {

    PhysicalTradeEntity save(PhysicalTradeEntity entity);

    Optional<PhysicalTradeEntity> findByTradeId(String tradeId);

    void deleteByTradeId(String tradeId);

    List<PhysicalTradeEntity> findByCriteria(PhysicalTradeSearchCriteria criteria);
}

