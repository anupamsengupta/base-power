package com.power.base.benchmark.micro;

import com.power.base.dao.rdbms.jpa.persistence.swap.SwapTradeEntity;
import com.power.base.dao.rdbms.jpa.repository.swap.SwapTradeDaoJpa;
import com.power.base.dao.rdbms.jpa.repository.swap.SwapTradeSearchCriteria;
import com.power.base.datamodel.dto.common.BuySellIndicator;
import com.power.base.datamodel.dto.common.DocumentType;
import com.power.base.datamodel.dto.common.PartyDto;
import com.power.base.datamodel.dto.financials.SwapMetadataDto;
import com.power.base.datamodel.dto.financials.SwapPeriodDto;
import com.power.base.datamodel.dto.financials.SwapPowerTradeDto;
import com.power.base.datamodel.dto.financials.SwapSettlementInfoDto;
import com.power.base.datamodel.dto.financials.SwapTradeDetailsDto;
import com.power.base.datamodel.dto.financials.SwapTradeHeaderDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.openjdk.jmh.annotations.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class SwapTradeDaoJpaBenchmark {

    private static final String PERSISTENCE_UNIT = "test-pu";
    private static EntityManagerFactory entityManagerFactory;
    
    private EntityManager entityManager;
    private SwapTradeDaoJpa dao;
    private SwapTradeEntity testEntity;
    private SwapTradeSearchCriteria searchCriteria;
    private int tradeCounter = 0;

    @Setup(Level.Trial)
    public void setupTrial() {
        if (entityManagerFactory == null) {
            entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
        }
    }

    @Setup(Level.Iteration)
    public void setupIteration() {
        entityManager = entityManagerFactory.createEntityManager();
        dao = new SwapTradeDaoJpa();
        dao.setEntityManager(entityManager);
        
        // Create test data
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        try {
            // Clean up any existing data
            entityManager.createQuery("DELETE FROM SwapTradeEntity").executeUpdate();
            entityManager.flush();
            
            // Insert some test data for findByCriteria benchmarks
            for (int i = 0; i < 10; i++) {
                SwapTradeEntity entity = createTestEntity("SWP-BENCH-" + i);
                entityManager.persist(entity);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to setup test data", e);
        }
        
        entityManager.clear();
        
        // Create a fresh entity for save/delete benchmarks
        testEntity = createTestEntity("SWP-BENCH-SAVE");
        
        // Create search criteria
        searchCriteria = new SwapTradeSearchCriteria();
        searchCriteria.setTenantId("TENANT_A");
        searchCriteria.setBusinessUnit("Derivatives Desk");
    }

    @TearDown(Level.Iteration)
    public void tearDownIteration() {
        if (entityManager != null && entityManager.isOpen()) {
            entityManager.close();
        }
    }

    @TearDown(Level.Trial)
    public void tearDownTrial() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }

    @Benchmark
    public SwapTradeEntity benchmarkSave() {
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        try {
            SwapTradeEntity entity = createTestEntity("SWP-SAVE-" + (tradeCounter++));
            SwapTradeEntity saved = dao.save(entity);
            tx.commit();
            return saved;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Save benchmark failed", e);
        } finally {
            entityManager.clear();
        }
    }

    @Benchmark
    public void benchmarkFindByTradeId() {
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        try {
            dao.findByTradeId("SWP-BENCH-5");
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("FindByTradeId benchmark failed", e);
        } finally {
            entityManager.clear();
        }
    }

    @Benchmark
    public void benchmarkDeleteByTradeId() {
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        try {
            // First create an entity to delete
            SwapTradeEntity entity = createTestEntity("SWP-DELETE-" + (tradeCounter++));
            entityManager.persist(entity);
            entityManager.flush();
            entityManager.clear();
            
            dao.deleteByTradeId(entity.getTradeId());
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("DeleteByTradeId benchmark failed", e);
        } finally {
            entityManager.clear();
        }
    }

    @Benchmark
    public List<SwapTradeEntity> benchmarkFindByCriteria() {
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        try {
            List<SwapTradeEntity> results = dao.findByCriteria(searchCriteria);
            tx.commit();
            return results;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("FindByCriteria benchmark failed", e);
        } finally {
            entityManager.clear();
        }
    }

    @Benchmark
    public List<SwapTradeEntity> benchmarkFindByCriteriaWithMultipleFilters() {
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        try {
            SwapTradeSearchCriteria criteria = new SwapTradeSearchCriteria();
            criteria.setTenantId("TENANT_A");
            criteria.setBusinessUnit("Derivatives Desk");
            criteria.setTraderName("Jane Smith (TRDR-789)");
            criteria.setMarket("EEX");
            criteria.setCommodity("Power");
            criteria.setReferenceZone("DE-LU");
            
            List<SwapTradeEntity> results = dao.findByCriteria(criteria);
            tx.commit();
            return results;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("FindByCriteriaWithMultipleFilters benchmark failed", e);
        } finally {
            entityManager.clear();
        }
    }

    private SwapTradeEntity createTestEntity(String tradeId) {
        SwapTradeHeaderDto header = new SwapTradeHeaderDto();
        header.setTradeId(tradeId);
        header.setTenantId("TENANT_A");
        header.setTradeDate(LocalDate.of(2025, 11, 7));
        header.setTradeTime(Instant.parse("2025-11-07T14:30:00Z"));
        header.setDocumentType(DocumentType.CONFIRMATION);
        header.setDocumentVersion("1.0");
        header.setBuyerParty(new PartyDto("10X1001A1001A450F6", "Hedge Fund EU", "Buyer"));
        header.setSellerParty(new PartyDto("10XFR-ENTSOE-0000000A", "Bank US LLC", "Seller"));
        header.setBusinessUnit("Derivatives Desk");
        header.setBookStrategy("Price Hedging Book");
        header.setTraderName("Jane Smith (TRDR-789)");
        header.setAgreementId("ISDA-2024-001");
        header.setMarket("EEX");
        header.setCommodity("Power");
        header.setTransactionType("SWAP");
        header.setReferenceZone("DE-LU");
        header.setBuySellIndicator(BuySellIndicator.SELL);
        header.setAmendmentIndicator(false);

        SwapPeriodDto period = new SwapPeriodDto(
                100.0,
                "MWh",
                LocalDate.of(2025, 11, 1),
                Instant.parse("2025-11-01T00:00:00Z"),
                LocalDate.of(2025, 12, 1),
                Instant.parse("2025-12-01T00:00:00Z"),
                "Monthly",
                "EPEX-DA-BASE",
                "Base"
        );
        SwapTradeDetailsDto detailsDto = new SwapTradeDetailsDto(List.of(period));

        SwapSettlementInfoDto settlementInfoDto = new SwapSettlementInfoDto(
                8760.0,
                "MWh",
                "Index",
                70.00,
                0.0,
                "EUR",
                "EUR",
                "EUR/MWh",
                "EUR/MWh",
                "Cash",
                LocalDate.of(2025, 12, 8),
                LocalDate.of(2025, 11, 7),
                null,
                LocalDate.of(2025, 12, 7),
                null,
                2,
                0.0,
                4
        );

        SwapMetadataDto metadataDto = new SwapMetadataDto(
                LocalDate.of(2025, 11, 7),
                LocalDate.of(2025, 12, 7),
                "English Law",
                true,
                "FINPWR20251107ABC123"
        );

        SwapPowerTradeDto dto = new SwapPowerTradeDto(header, detailsDto, settlementInfoDto, metadataDto);
        return SwapTradeEntity.fromDto(dto);
    }
}

