package com.power.base.dao.rdbms.repository.swap;

import com.power.base.dao.rdbms.service.SwapTradeService;
import com.power.base.datamodel.dto.financials.SwapPowerTradeDto;
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

class SwapTradeDaoJpaDbUnitTest {

    private static final String PERSISTENCE_UNIT = "test-pu";
    private static final String JDBC_URL = "jdbc:h2:mem:power_test;DB_CLOSE_DELAY=-1;MODE=LEGACY";
    private static EntityManagerFactory entityManagerFactory;

    private EntityManager entityManager;
    private IDatabaseTester databaseTester;
    private SwapTradeDaoJpa dao;
    private SwapTradeService service;

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
        dao = new SwapTradeDaoJpa();
        dao.setEntityManager(entityManager);
        service = new SwapTradeService(dao);

        databaseTester = new JdbcDatabaseTester("org.h2.Driver", JDBC_URL, "sa", "");
        databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
        databaseTester.setTearDownOperation(DatabaseOperation.DELETE_ALL);
        databaseTester.setDataSet(loadDataset("/datasets/swap-trades.xml"));
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
    void findByTradeIdReturnsSwapTrade() {
        Optional<SwapPowerTradeDto> result = service.findByTradeId("FIN-2025-11-07-001");
        assertTrue(result.isPresent());
        assertEquals("Derivatives Desk", result.get().getTradeHeader().getBusinessUnit());
    }

    @Test
    void searchByCriteriaMatchesSwapTrade() {
        SwapTradeSearchCriteria criteria = new SwapTradeSearchCriteria();
        criteria.setTraderName("Jane Smith (TRDR-789)");
        criteria.setReferenceZone("DE-LU");

        List<SwapPowerTradeDto> trades = service.searchByCriteria(criteria);
        assertEquals(1, trades.size());
        SwapPowerTradeDto trade = trades.get(0);
        assertEquals("FIN-2025-11-07-001", trade.getTradeHeader().getTradeId());
        assertEquals("SWAP", trade.getTradeHeader().getTransactionType());
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

