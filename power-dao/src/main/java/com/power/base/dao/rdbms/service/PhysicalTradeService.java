package com.power.base.dao.rdbms.service;

import com.power.base.dao.rdbms.persistence.physical.PhysicalTradeEntity;
import com.power.base.dao.rdbms.repository.physical.PhysicalTradeDao;
import com.power.base.dao.rdbms.repository.physical.PhysicalTradeSearchCriteria;
import com.power.base.datamodel.dto.physicals.PhysicalPowerTradeDto;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PhysicalTradeService {

    private final PhysicalTradeDao tradeDao;

    public PhysicalTradeService(PhysicalTradeDao tradeDao) {
        this.tradeDao = tradeDao;
    }

    public PhysicalPowerTradeDto persist(PhysicalPowerTradeDto tradeDto) {
        PhysicalTradeEntity entity = PhysicalTradeEntity.fromDto(tradeDto);
        PhysicalTradeEntity saved = tradeDao.save(entity);
        return saved.toDto();
    }

    public Optional<PhysicalPowerTradeDto> findByTradeId(String tradeId) {
        return tradeDao.findByTradeId(tradeId).map(PhysicalTradeEntity::toDto);
    }

    public List<PhysicalPowerTradeDto> searchByCriteria(PhysicalTradeSearchCriteria criteria) {
        return tradeDao.findByCriteria(criteria).stream()
                .map(PhysicalTradeEntity::toDto)
                .collect(Collectors.toList());
    }

    public void deleteByTradeId(String tradeId) {
        tradeDao.deleteByTradeId(tradeId);
    }
}

