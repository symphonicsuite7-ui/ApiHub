package com.apihub.gateway.open;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 开放应用数据访问：按 AppId 查应用（带 TTL 缓存）、校验接口开通关系。
 * <p>
 * 缓存策略：应用基础信息缓存 ttl 秒（默认 60），启用/禁用、密钥重置最多延迟生效；
 * 开通关系不缓存（grant/revoke 需立即生效）。
 */
@Repository
public class OpenAppRepository {

    private final JdbcTemplate jdbcTemplate;
    private final long cacheTtlMillis;
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, InterfaceCacheEntry> interfaceCache = new ConcurrentHashMap<>();

    public OpenAppRepository(
            JdbcTemplate jdbcTemplate,
            @Value("${apihub.open.app-cache.ttl-seconds:60}") long cacheTtlSeconds
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.cacheTtlMillis = Math.max(cacheTtlSeconds, 0) * 1000L;
    }

    public AppInfo findByAppId(String appId) {
        long now = System.currentTimeMillis();
        CacheEntry entry = cache.get(appId);
        if (entry != null && entry.expireAt > now) {
            return entry.info;
        }
        AppInfo info = queryDb(appId);
        if (info != null) {
            cache.put(appId, new CacheEntry(info, now + cacheTtlMillis));
        } else {
            // 负缓存不落地：应用刚创建即可用
            cache.remove(appId);
        }
        return info;
    }

    private AppInfo queryDb(String appId) {
        return jdbcTemplate.query(
                "SELECT id, app_id, app_secret, user_id, status, qps_limit, daily_quota "
                        + "FROM api_app WHERE app_id = ?",
                rs -> rs.next() ? new AppInfo(
                        rs.getLong("id"),
                        rs.getString("app_id"),
                        rs.getString("app_secret"),
                        rs.getLong("user_id"),
                        rs.getInt("status"),
                        rs.getInt("qps_limit"),
                        rs.getInt("daily_quota")
                ) : null,
                appId
        );
    }

    /** 应用是否已开通该接口，且接口处于上线状态 */
    public boolean isGranted(String appId, String path, String method) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM api_app_interface a JOIN api_interface i ON a.interface_id = i.id "
                        + "WHERE a.app_id = ? AND i.path = ? AND i.method = ? AND i.status = 1",
                Integer.class, appId, path, method.toUpperCase()
        );
        return count != null && count > 0;
    }

    /**
     * 按路径+方法解析接口 id（带 TTL 缓存）。
     * 接口资产 id 几乎不变，缓存时间复用应用缓存 TTL；查不到返回 null。
     */
    public Long findInterfaceId(String path, String method) {
        String key = method.toUpperCase() + " " + path;
        long now = System.currentTimeMillis();
        InterfaceCacheEntry entry = interfaceCache.get(key);
        if (entry != null && entry.expireAt > now) {
            return entry.interfaceId;
        }
        Long interfaceId = jdbcTemplate.query(
                "SELECT id FROM api_interface WHERE path = ? AND method = ?",
                rs -> rs.next() ? rs.getLong(1) : null,
                path, method.toUpperCase()
        );
        if (interfaceId != null) {
            interfaceCache.put(key, new InterfaceCacheEntry(interfaceId, now + cacheTtlMillis));
        }
        return interfaceId;
    }

    private record CacheEntry(AppInfo info, long expireAt) {
    }

    private record InterfaceCacheEntry(Long interfaceId, long expireAt) {
    }
}
