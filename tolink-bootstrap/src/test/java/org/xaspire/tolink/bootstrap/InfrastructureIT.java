package org.xaspire.tolink.bootstrap;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.agentscope.core.ReActAgent;
import java.time.Duration;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import org.xaspire.tolink.bootstrap.persistence.ProbeMapper;
import org.xaspire.tolink.bootstrap.persistence.ProbeRecord;
import org.xaspire.tolink.bootstrap.persistence.InfrastructureProbeMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(TestPersistenceConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InfrastructureIT {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private Flyway flyway;

    @Autowired
    private ProbeMapper probeMapper;

    @Autowired
    private InfrastructureProbeMapper infrastructureProbeMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeAll
    void createProbeTable() {
        assertNotNull(dataSource);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS integration_probe (
                    id BIGINT PRIMARY KEY,
                    note VARCHAR(255) NOT NULL,
                    payload JSONB
                )
                """);
    }

    @AfterAll
    void dropProbeTable() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS integration_probe");
    }

    @Test
    void springContextAndFlywayMigrationStart() {
        assertNotNull(applicationContext);
        assertEquals(1, infrastructureProbeMapper.selectOne());
        assertNotNull(flyway.info().current());
        assertEquals(1, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tolink.flyway_schema_history "
                        + "WHERE version = '1' AND description = 'baseline' AND success = TRUE",
                Integer.class));
        assertThrows(
                NoSuchBeanDefinitionException.class,
                () -> applicationContext.getBean(ReActAgent.class));
    }

    @Test
    void mybatisPlusCanInsertAndSelectAgainstPostgres() {
        jdbcTemplate.update("DELETE FROM integration_probe");

        probeMapper.insert(new ProbeRecord(1L, "mybatis-plus"));
        ProbeRecord result = probeMapper.selectOne(
                new QueryWrapper<ProbeRecord>().eq("id", 1L));

        assertNotNull(result);
        assertEquals("mybatis-plus", result.getNote());
    }

    @Test
    void postgresJsonbCanBeWrittenAndRead() {
        jdbcTemplate.update("DELETE FROM integration_probe");
        jdbcTemplate.update(
                "INSERT INTO integration_probe (id, note, payload) VALUES (?, ?, CAST(? AS jsonb))",
                2L,
                "jsonb",
                "{\"answer\":42,\"valid\":true}");

        String payload = jdbcTemplate.queryForObject(
                "SELECT payload::text FROM integration_probe WHERE id = ?",
                String.class,
                2L);

        assertNotNull(payload);
        assertTrue(payload.contains("\"answer\": 42") || payload.contains("\"answer\":42"));
        assertTrue(payload.contains("\"valid\": true") || payload.contains("\"valid\":true"));
    }

    @Test
    void postgresTransactionRollsBack() {
        jdbcTemplate.update("DELETE FROM integration_probe");

        assertThrows(RuntimeException.class, () -> transactionTemplate.execute(status -> {
            jdbcTemplate.update(
                    "INSERT INTO integration_probe (id, note) VALUES (?, ?)",
                    3L,
                    "rollback");
            throw new RuntimeException("force rollback");
        }));

        assertEquals(0, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM integration_probe WHERE id = 3",
                Integer.class));
    }

    @Test
    void redisSupportsSetGetTtlAndDelete() {
        String key = "tolink:integration:" + System.nanoTime();
        redisTemplate.opsForValue().set(key, "value", Duration.ofSeconds(30));

        assertEquals("value", redisTemplate.opsForValue().get(key));
        assertTrue(redisTemplate.getExpire(key) > 0);
        assertTrue(Boolean.TRUE.equals(redisTemplate.delete(key)));
        assertFalse(Boolean.TRUE.equals(redisTemplate.hasKey(key)));
    }
}
