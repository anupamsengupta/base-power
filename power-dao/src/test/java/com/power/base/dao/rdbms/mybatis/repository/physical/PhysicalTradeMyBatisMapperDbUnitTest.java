package com.power.base.dao.rdbms.mybatis.repository.physical;

import com.power.base.dao.rdbms.mybatis.service.PhysicalTradeMyBatisService;
import com.power.base.datamodel.dto.common.Profile;
import com.power.base.datamodel.dto.physicals.PhysicalLineItemDto;
import com.power.base.datamodel.dto.physicals.PhysicalPowerTradeDto;
import com.power.base.datamodel.dto.physicals.PhysicalSettlementItemDto;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
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

class PhysicalTradeMyBatisMapperDbUnitTest {

    private static final String JDBC_URL = "jdbc:h2:mem:power_test;DB_CLOSE_DELAY=-1;MODE=LEGACY";
    private static final String CONFIGURATION_RESOURCE = "mybatis/mybatis-config.xml";

    private static SqlSessionFactory sqlSessionFactory;
    private static EntityManagerFactory entityManagerFactory;

    private SqlSession sqlSession;
    private PhysicalTradeMyBatisService service;
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
        PhysicalTradeMapper mapper = sqlSession.getMapper(PhysicalTradeMapper.class);
        service = new PhysicalTradeMyBatisService(mapper);

        databaseTester = new JdbcDatabaseTester("org.h2.Driver", JDBC_URL, "sa", "");
        databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
        databaseTester.setTearDownOperation(DatabaseOperation.DELETE_ALL);
        databaseTester.setDataSet(loadDataset("/datasets/physical-trades.xml"));
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
    void findByTradeIdReturnsPhysicalTrade() {
        Optional<PhysicalPowerTradeDto> result = service.findByTradeId("PWR-2025-11-07-001");
        assertTrue(result.isPresent());
        assertEquals("Nord Pool Trading Desk", result.get().getTradeHeader().getBusinessUnit());

        PhysicalPowerTradeDto trade = result.get();
        assertEquals(1, trade.getTradeDetails().getLineItems().size());
        PhysicalLineItemDto lineItem = trade.getTradeDetails().getLineItems().get(0);
        assertEquals("Fri 00:00-01:00", lineItem.getDayHour());
        assertEquals(12.5, lineItem.getQuantity());
        assertEquals(Profile.ONE_HOUR, lineItem.getProfile());

        assertEquals(1, trade.getSettlementInfo().getSettlementItems().size());
        PhysicalSettlementItemDto settlementItem = trade.getSettlementInfo().getSettlementItems().get(0);
        assertEquals("SET-2025-11-07", settlementItem.getSettlementId());
        assertEquals(1, settlementItem.getReferencedLineItems().size());
        assertEquals("LI-001", settlementItem.getReferencedLineItems().get(0));
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
        assertEquals(1, trade.getTradeDetails().getLineItems().size());
    }

    @Test
    void persistReplacesChildCollections() {
        PhysicalPowerTradeDto existing = service.findByTradeId("PWR-2025-11-07-001")
                .orElseThrow();

        PhysicalLineItemDto amendedLineA = new PhysicalLineItemDto(
                LocalDate.of(2025, 11, 8),
                Instant.parse("2025-11-08T00:00:00Z"),
                LocalDate.of(2025, 11, 8),
                Instant.parse("2025-11-08T01:00:00Z"),
                "Sat 00:00-01:00",
                20.0,
                "MWh",
                60.0,
                Profile.ONE_HOUR
        );
        PhysicalLineItemDto amendedLineB = new PhysicalLineItemDto(
                LocalDate.of(2025, 11, 8),
                Instant.parse("2025-11-08T01:00:00Z"),
                LocalDate.of(2025, 11, 8),
                Instant.parse("2025-11-08T02:00:00Z"),
                "Sat 01:00-02:00",
                22.0,
                "MWh",
                60.0,
                Profile.ONE_HOUR
        );
        existing.getTradeDetails().setLineItems(Arrays.asList(amendedLineA, amendedLineB));

        PhysicalSettlementItemDto amendedSettlement = new PhysicalSettlementItemDto(
                "SET-2025-11-08",
                Arrays.asList("LI-100", "LI-101"),
                LocalDate.of(2025, 11, 8),
                42.0,
                "MWh",
                80.0,
                79.0,
                "EUR/MWh",
                "EUR/MWh",
                0.0,
                0.0,
                3360.0,
                "EUR",
                "EUR",
                "Provisional"
        );
        existing.getSettlementInfo().setSettlementItems(Collections.singletonList(amendedSettlement));

        PhysicalPowerTradeDto persisted = service.persist(existing);
        assertEquals(2, persisted.getTradeDetails().getLineItems().size());
        assertEquals("Sat 00:00-01:00", persisted.getTradeDetails().getLineItems().get(0).getDayHour());
        assertEquals(1, persisted.getSettlementInfo().getSettlementItems().size());
        assertEquals(2, persisted.getSettlementInfo().getSettlementItems().get(0).getReferencedLineItems().size());

        PhysicalPowerTradeDto reloaded = service.findByTradeId(existing.getTradeHeader().getTradeId())
                .orElseThrow();
        assertEquals(2, reloaded.getTradeDetails().getLineItems().size());
        assertEquals(2, reloaded.getSettlementInfo().getSettlementItems().get(0).getReferencedLineItems().size());
        assertEquals("LI-100", reloaded.getSettlementInfo().getSettlementItems().get(0).getReferencedLineItems().get(0));
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


