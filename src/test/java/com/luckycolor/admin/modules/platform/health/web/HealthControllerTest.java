package com.luckycolor.admin.modules.platform.health.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

class HealthControllerTest {

    @Test
    void shouldReportDatabaseUpWhenDatasourceIsReachable() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);

        HealthStatusResponse response = new HealthController("luckycolor-admin-springboot", dataSource).health().data();

        assertThat(response.status()).isEqualTo("ok");
        assertThat(response.database()).isEqualTo("up");
        assertThat(response.application()).isEqualTo("luckycolor-admin-springboot");
    }

    @Test
    void shouldReportDatabaseDownWhenDatasourceFails() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenThrow(new SQLException("connection refused"));

        HealthStatusResponse response = new HealthController("luckycolor-admin-springboot", dataSource).health().data();

        assertThat(response.status()).isEqualTo("error");
        assertThat(response.database()).isEqualTo("down");
    }
}
