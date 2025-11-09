package com.power.base.dao.rdbms.jpa.repository.physical;

import com.power.base.dao.rdbms.jpa.persistence.physical.PhysicalTradeEntity;
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

import org.springframework.stereotype.Repository;

@Repository
@Transactional
public class PhysicalTradeDaoJpa implements PhysicalTradeDao {

    @PersistenceContext
    private EntityManager entityManager;

    void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public PhysicalTradeEntity save(PhysicalTradeEntity entity) {
        if (entityManager.find(PhysicalTradeEntity.class, entity.getTradeId()) == null) {
            entityManager.persist(entity);
            return entity;
        }
        return entityManager.merge(entity);
    }

    @Override
    public Optional<PhysicalTradeEntity> findByTradeId(String tradeId) {
        return Optional.ofNullable(entityManager.find(PhysicalTradeEntity.class, tradeId));
    }

    @Override
    public void deleteByTradeId(String tradeId) {
        findByTradeId(tradeId).ifPresent(entityManager::remove);
    }

    @Override
    public List<PhysicalTradeEntity> findByCriteria(PhysicalTradeSearchCriteria criteria) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<PhysicalTradeEntity> query = cb.createQuery(PhysicalTradeEntity.class);
        Root<PhysicalTradeEntity> root = query.from(PhysicalTradeEntity.class);

        List<Predicate> predicates = new ArrayList<>();

        criteria.getTenantId()
                .ifPresent(tenant -> predicates.add(cb.equal(root.get("header").get("tenantId"), tenant)));

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
