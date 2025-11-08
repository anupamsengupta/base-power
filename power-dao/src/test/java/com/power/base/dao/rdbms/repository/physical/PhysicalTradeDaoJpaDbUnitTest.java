package com.power.base.dao.rdbms.repository.physical;

import com.power.base.dao.rdbms.service.PhysicalTradeService;
import com.power.base.datamodel.dto.physicals.PhysicalPowerTradeDto;
import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhysicalTradeDaoJpaDbUnitTest {

    private static final String PERSISTENCE_UNIT = "test-pu";
    private static final String JDBC_URL = "jdbc:h2:mem:power_test;DB_CLOSE_DELAY=-1;MODE=LEGACY";
    private static EntityManagerFactory entityManagerFactory;

    private EntityManager entityManager;
    private IDatabaseTester databaseTester;
    private PhysicalTradeDaoJpa dao;
    private PhysicalTradeService service;

    @BeforeAll
    static void initFactory() {
        entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
    }

    @AfterAll
    static void closeFactory() {
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        entityManager = entityManagerFactory.createEntityManager();
        dao = new PhysicalTradeDaoJpa();
        dao.setEntityManager(entityManager);
        service = new PhysicalTradeService(dao);

        databaseTester = new JdbcDatabaseTester("org.h2.Driver", JDBC_URL, "sa", "");
        databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
        databaseTester.setTearDownOperation(DatabaseOperation.DELETE_ALL);
        databaseTester.setDataSet(loadDataset("/datasets/physical-trades.xml"));
        databaseTester.onSetup();

        entityManager.clear();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (databaseTester != null) {
            databaseTester.onTearDown();
        }
        if (entityManager != null) {
            entityManager.close();
        }
    }

    @Test
    void findByTradeIdReturnsPhysicalTrade() {
        Optional<PhysicalPowerTradeDto> result = service.findByTradeId("PWR-2025-11-07-001");
        assertTrue(result.isPresent());
        assertEquals("Nord Pool Trading Desk", result.get().getTradeHeader().getBusinessUnit());
    }

    @Test
    void searchByCriteriaMatchesTrade() {
        PhysicalTradeSearchCriteria criteria = new PhysicalTradeSearchCriteria();
        criteria.setBusinessUnit("Nord Pool Trading Desk");
        criteria.setTraderName("John Doe (TRDR-456)");

        List<PhysicalPowerTradeDto> trades = service.searchByCriteria(criteria);
        assertEquals(1, trades.size());
        PhysicalPowerTradeDto trade = trades.get(0);
        assertEquals("PWR-2025-11-07-001", trade.getTradeHeader().getTradeId());
        assertEquals("Power", trade.getTradeHeader().getCommodity());
    }

    private IDataSet loadDataset(String path) throws Exception {
        try (InputStream inputStream = getClass().getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IllegalStateException("Dataset not found at path: " + path);
            }
            return new FlatXmlDataSetBuilder()
                    .setColumnSensing(true)
                    .build(inputStream);
        }
    }
}

