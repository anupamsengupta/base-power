package com.power.base.dao.rdbms.service;

import com.power.base.dao.rdbms.persistence.swap.SwapTradeEntity;
import com.power.base.dao.rdbms.repository.swap.SwapTradeDao;
import com.power.base.dao.rdbms.repository.swap.SwapTradeSearchCriteria;
import com.power.base.datamodel.dto.financials.SwapPowerTradeDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SwapTradeService {

    private final SwapTradeDao tradeDao;

    public SwapTradeService(SwapTradeDao tradeDao) {
        this.tradeDao = tradeDao;
    }

    public SwapPowerTradeDto persist(SwapPowerTradeDto tradeDto) {
        SwapTradeEntity entity = SwapTradeEntity.fromDto(tradeDto);
        SwapTradeEntity saved = tradeDao.save(entity);
        return saved.toDto();
    }

    public Optional<SwapPowerTradeDto> findByTradeId(String tradeId) {
        return tradeDao.findByTradeId(tradeId).map(SwapTradeEntity::toDto);
    }

    public List<SwapPowerTradeDto> searchByCriteria(SwapTradeSearchCriteria criteria) {
        return tradeDao.findByCriteria(criteria).stream()
                .map(SwapTradeEntity::toDto)
                .collect(Collectors.toList());
    }

    public void deleteByTradeId(String tradeId) {
        tradeDao.deleteByTradeId(tradeId);
    }
}

