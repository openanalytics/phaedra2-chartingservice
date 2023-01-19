package eu.openanalytics.phaedra.chartingservice.repository;

import eu.openanalytics.phaedra.chartingservice.model.ChartTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import support.Containers;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

@Testcontainers
@SpringBootTest
//@Sql({"/jdbc/initial.sql"})
@TestPropertySource(locations = "classpath:application-test.properties")
public class ChartTemplateRepositoryTest {

    @Autowired
    private ChartTemplateRepository chartTemplateRepository;
    @Container
    private static JdbcDatabaseContainer postgresSQLContaioner = new PostgreSQLContainer(DockerImageName.parse("public.ecr.aws/docker/library/postgres:13-alpine").asCompatibleSubstituteFor(PostgreSQLContainer.IMAGE))
            .withDatabaseName("phaedra2")
            .withUrlParam("currentSchema", "charting")
            .withPassword("phaedra2")
            .withUsername("phaedra2");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("DB_URL", Containers.postgreSQLContainer::getJdbcUrl);
        registry.add("DB_USER", Containers.postgreSQLContainer::getUsername);
        registry.add("DB_PASSWORD", Containers.postgreSQLContainer::getPassword);
    }

    @Test
    public void contextLoads() {
        assertThat(chartTemplateRepository).isNotNull();
    }

    @Test
    public void getAllChartTemplates() {
        ChartTemplate chartTemplate = new ChartTemplate();
        chartTemplate.setType("scatter");
        chartTemplate.setAxisY("Feature value");
        chartTemplate.setAxisX("Volume (mL)");
        chartTemplateRepository.save(chartTemplate);

        List<ChartTemplate> allChartTemplates = (List<ChartTemplate>) chartTemplateRepository.findAll();
        assertThat(allChartTemplates).isNotEmpty();
    }

    @Test
    public void createChartTemplate() {
        ChartTemplate chartTemplate = new ChartTemplate();
        chartTemplate.setType("scatter");
        chartTemplate.setAxisY("Feature value");
        chartTemplate.setAxisX("Volume (mL)");

        ChartTemplate result = chartTemplateRepository.save(chartTemplate);
        assertThat(result.getId()).isNotNull();
        assertThat(result.getType()).isEqualTo(chartTemplate.getType());
        assertThat(result.getAxisX()).isEqualTo(chartTemplate.getAxisX());
        assertThat(result.getAxisY()).isEqualTo(chartTemplate.getAxisY());
    }
}
