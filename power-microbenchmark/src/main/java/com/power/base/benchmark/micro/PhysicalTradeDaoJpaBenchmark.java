package com.power.base.benchmark.micro;

import com.power.base.dao.rdbms.jpa.persistence.physical.PhysicalTradeEntity;
import com.power.base.dao.rdbms.jpa.repository.physical.PhysicalTradeDaoJpa;
import com.power.base.dao.rdbms.jpa.repository.physical.PhysicalTradeSearchCriteria;
import com.power.base.datamodel.dto.common.BuySellIndicator;
import com.power.base.datamodel.dto.common.DocumentType;
import com.power.base.datamodel.dto.common.PartyDto;
import com.power.base.datamodel.dto.common.Profile;
import com.power.base.datamodel.dto.physicals.PhysicalLineItemDto;
import com.power.base.datamodel.dto.physicals.PhysicalMetadataDto;
import com.power.base.datamodel.dto.physicals.PhysicalPowerTradeDto;
import com.power.base.datamodel.dto.physicals.PhysicalSettlementInfoDto;
import com.power.base.datamodel.dto.physicals.PhysicalSettlementItemDto;
import com.power.base.datamodel.dto.physicals.PhysicalTradeDetailsDto;
import com.power.base.datamodel.dto.physicals.PhysicalTradeHeaderDto;
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
public class PhysicalTradeDaoJpaBenchmark {

    private static final String PERSISTENCE_UNIT = "test-pu";
    private static EntityManagerFactory entityManagerFactory;
    
    private EntityManager entityManager;
    private PhysicalTradeDaoJpa dao;
    private PhysicalTradeEntity testEntity;
    private PhysicalTradeSearchCriteria searchCriteria;
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
        dao = new PhysicalTradeDaoJpa();
        dao.setEntityManager(entityManager);
        
        // Create test data
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        try {
            // Clean up any existing data
            entityManager.createQuery("DELETE FROM PhysicalTradeEntity").executeUpdate();
            entityManager.flush();
            
            // Insert some test data for findByCriteria benchmarks
            for (int i = 0; i < 10; i++) {
                PhysicalTradeEntity entity = createTestEntity("PWR-BENCH-" + i);
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
        testEntity = createTestEntity("PWR-BENCH-SAVE");
        
        // Create search criteria
        searchCriteria = new PhysicalTradeSearchCriteria();
        searchCriteria.setTenantId("TENANT_A");
        searchCriteria.setBusinessUnit("Nord Pool Trading Desk");
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
    public PhysicalTradeEntity benchmarkSave() {
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        try {
            PhysicalTradeEntity entity = createTestEntity("PWR-SAVE-" + (tradeCounter++));
            PhysicalTradeEntity saved = dao.save(entity);
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
            dao.findByTradeId("PWR-BENCH-5");
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
            PhysicalTradeEntity entity = createTestEntity("PWR-DELETE-" + (tradeCounter++));
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
    public List<PhysicalTradeEntity> benchmarkFindByCriteria() {
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        try {
            List<PhysicalTradeEntity> results = dao.findByCriteria(searchCriteria);
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
    public List<PhysicalTradeEntity> benchmarkFindByCriteriaWithMultipleFilters() {
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        try {
            PhysicalTradeSearchCriteria criteria = new PhysicalTradeSearchCriteria();
            criteria.setTenantId("TENANT_A");
            criteria.setBusinessUnit("Nord Pool Trading Desk");
            criteria.setTraderName("John Doe (TRDR-456)");
            criteria.setMarket("EPEX-SPOT");
            criteria.setCommodity("Power");
            
            List<PhysicalTradeEntity> results = dao.findByCriteria(criteria);
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

    private PhysicalTradeEntity createTestEntity(String tradeId) {
        PhysicalTradeHeaderDto header = new PhysicalTradeHeaderDto();
        header.setTradeId(tradeId);
        header.setTenantId("TENANT_A");
        header.setTradeDate(LocalDate.of(2025, 11, 7));
        header.setTradeTime(Instant.parse("2025-11-07T14:30:00Z"));
        header.setDocumentType(DocumentType.CONFIRMATION);
        header.setDocumentVersion("1.0");
        header.setBuyerParty(new PartyDto("10X1001A1001A450F6", "Utility EU Ltd", "Buyer"));
        header.setSellerParty(new PartyDto("10XFR-ENTSOE-0000000A", "GenCo US Inc", "Seller"));
        header.setBusinessUnit("Nord Pool Trading Desk");
        header.setBookStrategy("Physical Hedging Book");
        header.setTraderName("John Doe (TRDR-456)");
        header.setAgreementId("EFET-2023-001");
        header.setMarket("EPEX-SPOT");
        header.setCommodity("Power");
        header.setTransactionType("FOR");
        header.setDeliveryPoint("DE-LU");
        header.setLoadType("Base Load");
        header.setBuySellIndicator(BuySellIndicator.BUY);
        header.setAmendmentIndicator(false);

        PhysicalLineItemDto lineItem = new PhysicalLineItemDto(
                LocalDate.of(2025, 11, 7),
                Instant.parse("2025-11-07T00:00:00Z"),
                LocalDate.of(2025, 11, 7),
                Instant.parse("2025-11-07T01:00:00Z"),
                "Fri 00:00-01:00",
                12.5,
                "MWh",
                50.0,
                Profile.ONE_HOUR
        );
        PhysicalTradeDetailsDto details = new PhysicalTradeDetailsDto(List.of(lineItem));

        PhysicalSettlementItemDto settlementItem = new PhysicalSettlementItemDto(
                "SET-2025-11-07",
                List.of("LI-001"),
                LocalDate.of(2025, 11, 7),
                1198.5,
                "MWh",
                75.50,
                75.50,
                "EUR/MWh",
                "EUR/MWh",
                0.0,
                0.0,
                90462.75,
                "EUR",
                "EUR",
                "Provisional"
        );
        PhysicalSettlementInfoDto settlementInfo = new PhysicalSettlementInfoDto(
                33600.0,
                "MWh",
                "Fixed",
                75.50,
                75.50,
                "EUR",
                "EUR",
                "EUR/MWh",
                "EUR/MWh",
                LocalDate.of(2025, 11, 7),
                null,
                LocalDate.of(2025, 11, 14),
                null,
                "Schedule_Date",
                5,
                2536800.00,
                2,
                List.of(settlementItem)
        );

        PhysicalMetadataDto metadata = new PhysicalMetadataDto(
                LocalDate.of(2025, 11, 7),
                LocalDate.of(2025, 11, 14),
                "EU (German Law)"
        );

        PhysicalPowerTradeDto dto = new PhysicalPowerTradeDto(header, details, settlementInfo, metadata);
        return PhysicalTradeEntity.fromDto(dto);
    }
}

