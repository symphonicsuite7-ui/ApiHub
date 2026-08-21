package com.apihub.gateway.open;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InvokeLogWriterTest {

    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final OpenAppRepository repository = mock(OpenAppRepository.class);
    private final InvokeLogWriter writer = new InvokeLogWriter(jdbcTemplate, repository, 1, 2, 100);

    @Test
    void write_openCallResolvesInterfaceIdAndInsertsAllFields() {
        when(repository.findInterfaceId("/api/open/weather", "GET")).thenReturn(7L);

        writer.write("trace-123", "app_1", "/api/open/weather", "GET", 200, 42, "127.0.0.1");

        verify(repository).findInterfaceId("/api/open/weather", "GET");
        verify(jdbcTemplate).update(
                eq("INSERT INTO api_invoke_log (trace_id, app_id, interface_id, request_path, method, status_code, "
                        + "cost_ms, ip) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"),
                eq("trace-123"), eq("app_1"), eq(7L), eq("/api/open/weather"), eq("GET"),
                eq(200), eq(42L), eq("127.0.0.1")
        );
    }

    @Test
    void write_adminCallSkipsInterfaceLookup() {
        writer.write("trace-456", null, "/api/admin/apps", "GET", 200, 10, "10.0.0.1");

        verify(repository, never()).findInterfaceId(anyString(), anyString());
        verify(jdbcTemplate).update(anyString(),
                eq("trace-456"), eq(null), eq(null), eq("/api/admin/apps"), eq("GET"),
                eq(200), eq(10L), eq("10.0.0.1"));
    }

    @Test
    void write_dbFailureIsSwallowed() {
        when(repository.findInterfaceId(anyString(), anyString())).thenReturn(null);
        when(jdbcTemplate.update(anyString(), org.mockito.ArgumentMatchers.<Object>any()))
                .thenThrow(new RuntimeException("db down"));

        writer.write("trace-789", "app_2", "/api/open/weather", "GET", 200, 5, "127.0.0.1");
        // 不抛异常即通过
    }
}
