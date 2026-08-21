package com.apihub.admin.service.serviceImpl;

import com.apihub.admin.dto.AppCreateRequest;
import com.apihub.admin.dto.AppVO;
import com.apihub.admin.service.AppService;
import com.apihub.admin.util.AppKeyGenerator;
import com.apihub.common.exception.BizException;
import com.apihub.common.result.ErrorCode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 应用管理简单实现（v1）：JdbcTemplate 直连，先跑通主流程。
 * <p>
 * 待优化点（v2）：MyBatis-Plus 实体/Mapper 分层、参数校验、Secret 脱敏、数据权限、事务细化。
 */
@Service
public class AppServiceImpl implements AppService {

    private static final RowMapper<AppVO> APP_ROW_MAPPER = (rs, i) -> {
        AppVO vo = new AppVO();
        vo.setId(rs.getLong("id"));
        vo.setAppId(rs.getString("app_id"));
        vo.setAppSecret(rs.getString("app_secret"));
        vo.setAppName(rs.getString("app_name"));
        vo.setUserId(rs.getLong("user_id"));
        vo.setStatus(rs.getInt("status"));
        vo.setQpsLimit(rs.getInt("qps_limit"));
        vo.setDailyQuota(rs.getInt("daily_quota"));
        vo.setCreateTime(rs.getString("create_time"));
        return vo;
    };

    private static final String BASE_SELECT =
            "SELECT id, app_id, app_secret, app_name, user_id, status, qps_limit, daily_quota, create_time FROM api_app";

    private final JdbcTemplate jdbcTemplate;

    public AppServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public AppVO create(AppCreateRequest request, Long operatorId) {
        if (request == null || !StringUtils.hasText(request.getAppName())) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "应用名称不能为空");
        }
        Long ownerId = operatorId != null ? operatorId : request.getUserId();
        String appId = AppKeyGenerator.generateAppId();
        String appSecret = AppKeyGenerator.generateAppSecret();
        int qps = request.getQpsLimit() == null ? 10 : request.getQpsLimit();
        int quota = request.getDailyQuota() == null ? 1000 : request.getDailyQuota();

        jdbcTemplate.update(
                "INSERT INTO api_app (app_id, app_secret, app_name, user_id, status, qps_limit, daily_quota) "
                        + "VALUES (?, ?, ?, ?, 1, ?, ?)",
                appId, appSecret, request.getAppName().trim(), ownerId, qps, quota);

        AppVO vo = new AppVO();
        vo.setAppId(appId);
        vo.setAppSecret(appSecret);
        vo.setAppName(request.getAppName().trim());
        vo.setUserId(ownerId);
        vo.setStatus(1);
        vo.setQpsLimit(qps);
        vo.setDailyQuota(quota);
        return vo;
    }

    @Override
    public List<AppVO> list(Long operatorId) {
        if (operatorId == null) {
            // 本地直连调试：查全部
            return jdbcTemplate.query(BASE_SELECT + " ORDER BY id DESC", APP_ROW_MAPPER);
        }
        return jdbcTemplate.query(BASE_SELECT + " WHERE user_id = ? ORDER BY id DESC", APP_ROW_MAPPER, operatorId);
    }

    @Override
    public AppVO detail(Long id, Long operatorId) {
        List<AppVO> apps = jdbcTemplate.query(BASE_SELECT + " WHERE id = ?", APP_ROW_MAPPER, id);
        if (apps.isEmpty()) {
            throw new BizException(ErrorCode.APP_INVALID);
        }
        AppVO vo = apps.get(0);
        List<Map<String, Object>> granted = jdbcTemplate.queryForList(
                "SELECT i.id, i.name, i.path, i.method, i.description, i.version, i.category, i.status "
                        + "FROM api_app_interface g JOIN api_interface i ON i.id = g.interface_id "
                        + "WHERE g.app_id = ? ORDER BY g.id",
                vo.getAppId());
        vo.setGrantedInterfaces(granted);
        return vo;
    }

    @Override
    public void updateStatus(Long id, Integer status, Long operatorId) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "status 只能为 0（禁用）或 1（启用）");
        }
        Integer exists = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM api_app WHERE id = ?", Integer.class, id);
        if (exists == null || exists == 0) {
            throw new BizException(ErrorCode.APP_INVALID);
        }
        jdbcTemplate.update("UPDATE api_app SET status = ? WHERE id = ?", status, id);
    }

    @Override
    @Transactional
    public void grant(String appId, List<Long> interfaceIds, Long operatorId) {
        requireEnabledApp(appId);
        if (interfaceIds == null || interfaceIds.isEmpty()) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "interfaceIds 不能为空");
        }
        for (Long interfaceId : interfaceIds) {
            if (interfaceId == null) {
                continue;
            }
            Integer online = jdbcTemplate.queryForObject(
                    "SELECT COUNT(1) FROM api_interface WHERE id = ? AND status = 1", Integer.class, interfaceId);
            if (online == null || online == 0) {
                throw new BizException(ErrorCode.INTERFACE_OFFLINE);
            }
            jdbcTemplate.update(
                    "INSERT INTO api_app_interface (app_id, interface_id) VALUES (?, ?) ON DUPLICATE KEY UPDATE id = id",
                    appId, interfaceId);
        }
    }

    @Override
    public void revoke(String appId, Long interfaceId, Long operatorId) {
        if (interfaceId == null) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "interfaceId 不能为空");
        }
        jdbcTemplate.update("DELETE FROM api_app_interface WHERE app_id = ? AND interface_id = ?", appId, interfaceId);
    }

    private void requireEnabledApp(String appId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id FROM api_app WHERE app_id = ? AND status = 1", appId);
        if (rows.isEmpty()) {
            throw new BizException(ErrorCode.APP_INVALID);
        }
    }
}
