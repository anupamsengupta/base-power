package com.power.base.dao.rdbms.mybatis.repository.swap;

import com.power.base.dao.rdbms.mybatis.service.SwapTradeMyBatisService;
import com.power.base.datamodel.dto.financials.SwapPeriodDto;
import com.power.base.datamodel.dto.financials.SwapPowerTradeDto;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
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

import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SwapTradeMyBatisMapperDbUnitTest {

    private static final String JDBC_URL = "jdbc:h2:mem:power_test;DB_CLOSE_DELAY=-1;MODE=LEGACY";
    private static final String CONFIGURATION_RESOURCE = "mybatis/mybatis-config.xml";

    private static SqlSessionFactory sqlSessionFactory;
    private static EntityManagerFactory entityManagerFactory;

    private SqlSession sqlSession;
    private SwapTradeMyBatisService service;
    private IDatabaseTester databaseTester;

    @BeforeAll
    static void initFactory() throws Exception {
        entityManagerFactory = Persistence.createEntityManagerFactory("test-pu");
        try (InputStream inputStream = Resources.getResourceAsStream(CONFIGURATION_RESOURCE)) {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        }
    }

    @AfterAll
    static void closeFactory() {
        if (sqlSessionFactory != null) {
            sqlSessionFactory.getConfiguration().getEnvironment().getDataSource();
        }
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        sqlSession = sqlSessionFactory.openSession(true);
        SwapTradeMapper mapper = sqlSession.getMapper(SwapTradeMapper.class);
        service = new SwapTradeMyBatisService(mapper);

        databaseTester = new JdbcDatabaseTester("org.h2.Driver", JDBC_URL, "sa", "");
        databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
        databaseTester.setTearDownOperation(DatabaseOperation.DELETE_ALL);
        databaseTester.setDataSet(loadDataset("/datasets/swap-trades.xml"));
        databaseTester.onSetup();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (databaseTester != null) {
            databaseTester.onTearDown();
        }
        if (sqlSession != null) {
            sqlSession.close();
        }
    }

    @Test
    void findByTradeIdReturnsSwapTrade() {
        Optional<SwapPowerTradeDto> result = service.findByTradeId("FIN-2025-11-07-001");
        assertTrue(result.isPresent());
        SwapPowerTradeDto trade = result.get();
        assertEquals("Derivatives Desk", trade.getTradeHeader().getBusinessUnit());
        assertEquals(1, trade.getFinancialDetails().getPeriods().size());
        SwapPeriodDto period = trade.getFinancialDetails().getPeriods().get(0);
        assertEquals("Monthly", period.getPeriodFrequency());
        assertEquals(100.0, period.getNotionalQuantity());
    }

    @Test
    void searchByCriteriaMatchesTrade() {
        SwapTradeSearchCriteria criteria = new SwapTradeSearchCriteria();
        criteria.setTraderName("Jane Smith (TRDR-789)");
        criteria.setReferenceZone("DE-LU");

        List<SwapPowerTradeDto> trades = service.searchByCriteria(criteria);
        assertEquals(1, trades.size());
        SwapPowerTradeDto trade = trades.get(0);
        assertEquals("FIN-2025-11-07-001", trade.getTradeHeader().getTradeId());
        assertEquals("Power", trade.getTradeHeader().getCommodity());
        assertEquals(1, trade.getFinancialDetails().getPeriods().size());
    }

    @Test
    void persistReplacesPeriods() {
        SwapPowerTradeDto trade = service.findByTradeId("FIN-2025-11-07-001")
                .orElseThrow();

        SwapPeriodDto revisedPeriodA = new SwapPeriodDto(
                150.0,
                "MWh",
                LocalDate.of(2025, 11, 10),
                Instant.parse("2025-11-10T00:00:00Z"),
                LocalDate.of(2025, 11, 20),
                Instant.parse("2025-11-20T00:00:00Z"),
                "TenDay",
                "EPEX-DA-BASE",
                "Base"
        );

        SwapPeriodDto revisedPeriodB = new SwapPeriodDto(
                200.0,
                "MWh",
                LocalDate.of(2025, 11, 20),
                Instant.parse("2025-11-20T00:00:00Z"),
                LocalDate.of(2025, 11, 30),
                Instant.parse("2025-11-30T00:00:00Z"),
                "TenDay",
                "EPEX-DA-BASE",
                "Peak"
        );

        trade.getFinancialDetails().setPeriods(Arrays.asList(revisedPeriodA, revisedPeriodB));

        SwapPowerTradeDto persisted = service.persist(trade);
        assertEquals(2, persisted.getFinancialDetails().getPeriods().size());
        assertEquals("TenDay", persisted.getFinancialDetails().getPeriods().get(0).getPeriodFrequency());

        SwapPowerTradeDto reloaded = service.findByTradeId(trade.getTradeHeader().getTradeId())
                .orElseThrow();
        assertEquals(2, reloaded.getFinancialDetails().getPeriods().size());
        assertEquals(200.0, reloaded.getFinancialDetails().getPeriods().get(1).getNotionalQuantity());
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


