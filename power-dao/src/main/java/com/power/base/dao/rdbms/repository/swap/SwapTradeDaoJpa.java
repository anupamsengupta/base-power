package com.power.base.dao.rdbms.repository.swap;

import com.power.base.dao.rdbms.persistence.swap.SwapTradeEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Transactional
public class SwapTradeDaoJpa implements SwapTradeDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public SwapTradeEntity save(SwapTradeEntity entity) {
        if (entityManager.find(SwapTradeEntity.class, entity.getTradeId()) == null) {
            entityManager.persist(entity);
            return entity;
        }
        return entityManager.merge(entity);
    }

    @Override
    public Optional<SwapTradeEntity> findByTradeId(String tradeId) {
        return Optional.ofNullable(entityManager.find(SwapTradeEntity.class, tradeId));
    }

    @Override
    public void deleteByTradeId(String tradeId) {
        findByTradeId(tradeId).ifPresent(entityManager::remove);
    }

    @Override
    public List<SwapTradeEntity> findByCriteria(SwapTradeSearchCriteria criteria) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<SwapTradeEntity> query = cb.createQuery(SwapTradeEntity.class);
        Root<SwapTradeEntity> root = query.from(SwapTradeEntity.class);

        List<Predicate> predicates = new ArrayList<>();

        criteria.getBusinessUnit()
                .ifPresent(bu -> predicates.add(cb.equal(root.get("header").get("businessUnit"), bu)));

        criteria.getMarket()
                .ifPresent(market -> predicates.add(cb.equal(root.get("header").get("market"), market)));

        criteria.getTraderName()
                .ifPresent(trader -> predicates.add(cb.equal(root.get("header").get("traderName"), trader)));

        criteria.getAgreementId()
                .ifPresent(agreement -> predicates.add(cb.equal(root.get("header").get("agreementId"), agreement)));

        criteria.getCommodity()
                .ifPresent(commodity -> predicates.add(cb.equal(root.get("header").get("commodity"), commodity)));

        criteria.getTransactionType()
                .ifPresent(type -> predicates.add(cb.equal(root.get("header").get("transactionType"), type)));

        criteria.getReferenceZone()
                .ifPresent(zone -> predicates.add(cb.equal(root.get("header").get("referenceZone"), zone)));

        criteria.getTradeDate()
                .ifPresent(date -> predicates.add(cb.equal(root.get("header").get("tradeDate"), date)));

        if (criteria.getTradeTimeFrom().isPresent() || criteria.getTradeTimeTo().isPresent()) {
            Instant from = criteria.getTradeTimeFrom().orElse(Instant.MIN);
            Instant to = criteria.getTradeTimeTo().orElse(Instant.MAX);
            predicates.add(cb.between(root.get("header").get("tradeTime"), from, to));
        }

        query.where(predicates.toArray(Predicate[]::new));

        return entityManager.createQuery(query).getResultList();
    }
}
package com.power.base.dao.rdbms.repository.swap;


