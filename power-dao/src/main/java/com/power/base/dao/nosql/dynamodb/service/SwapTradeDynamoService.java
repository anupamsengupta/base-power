package com.power.base.dao.nosql.dynamodb.service;

import com.power.base.dao.nosql.dynamodb.repository.SwapTradeDynamoDao;
import com.power.base.dao.rdbms.jpa.repository.swap.SwapTradeSearchCriteria;
import com.power.base.datamodel.dto.financials.SwapPowerTradeDto;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@ConditionalOnBean(SwapTradeDynamoDao.class)
public class SwapTradeDynamoService {

    private final SwapTradeDynamoDao dao;

    public SwapTradeDynamoService(SwapTradeDynamoDao dao) {
        this.dao = dao;
    }

    public SwapPowerTradeDto persist(SwapPowerTradeDto dto) {
        return dao.save(dto);
    }

    public Optional<SwapPowerTradeDto> findByTradeId(String tradeId) {
        return dao.findByTradeId(tradeId);
    }

    public List<SwapPowerTradeDto> searchByCriteria(SwapTradeSearchCriteria criteria) {
        return dao.searchByCriteria(criteria);
    }

    public void deleteByTradeId(String tradeId) {
        dao.deleteByTradeId(tradeId);
    }
}

