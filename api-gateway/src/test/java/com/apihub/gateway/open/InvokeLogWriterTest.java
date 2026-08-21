package com.apihub.gateway.open;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class InvokeLogWriterTest {

    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final InvokeLogWriter writer = new InvokeLogWriter(jdbcTemplate);

    @Test
    void write_insertsAllFields() {
        writer.write("trace-123", "app_1", "/api/open/weather", "GET", 200, 42, "127.0.0.1");

        verify(jdbcTemplate).update(
                eq("INSERT INTO api_invoke_log (trace_id, app_id, request_path, method, status_code, cost_ms, ip) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)"),
                eq("trace-123"), eq("app_1"), eq("/api/open/weather"), eq("GET"),
                eq(200), eq(42L), eq("127.0.0.1")
        );
    }

    @Test
    void write_acceptsNullAppIdForAdminCalls() {
        writer.write("trace-456", null, "/api/admin/apps", "GET", 200, 10, "10.0.0.1");

        verify(jdbcTemplate).update(anyString(),
                eq("trace-456"), eq(null), eq("/api/admin/apps"), eq("GET"),
                eq(200), eq(10L), eq("10.0.0.1"));
    }
}
