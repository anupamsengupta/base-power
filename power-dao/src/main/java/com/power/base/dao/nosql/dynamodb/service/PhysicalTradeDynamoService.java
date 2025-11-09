package com.power.base.dao.nosql.dynamodb.service;

import com.power.base.dao.nosql.dynamodb.repository.PhysicalTradeDynamoDao;
import com.power.base.dao.rdbms.jpa.repository.physical.PhysicalTradeSearchCriteria;
import com.power.base.datamodel.dto.physicals.PhysicalPowerTradeDto;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@ConditionalOnBean(PhysicalTradeDynamoDao.class)
public class PhysicalTradeDynamoService {

    private final PhysicalTradeDynamoDao dao;

    public PhysicalTradeDynamoService(PhysicalTradeDynamoDao dao) {
        this.dao = dao;
    }

    public PhysicalPowerTradeDto persist(PhysicalPowerTradeDto dto) {
        return dao.save(dto);
    }

    public Optional<PhysicalPowerTradeDto> findByTradeId(String tradeId) {
        return dao.findByTradeId(tradeId);
    }

    public List<PhysicalPowerTradeDto> searchByCriteria(PhysicalTradeSearchCriteria criteria) {
        return dao.searchByCriteria(criteria);
    }

    public void deleteByTradeId(String tradeId) {
        dao.deleteByTradeId(tradeId);
    }
}

