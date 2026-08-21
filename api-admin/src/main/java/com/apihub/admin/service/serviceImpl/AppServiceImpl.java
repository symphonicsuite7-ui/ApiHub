package com.apihub.admin.service.serviceImpl;

import com.apihub.admin.Enum.StatusEnum;
import com.apihub.admin.dto.AppCreateRequest;
import com.apihub.admin.dto.AppCreatedVO;
import com.apihub.admin.dto.AppDetailVO;
import com.apihub.admin.dto.AppVO;
import com.apihub.admin.dto.GrantedInterfaceVO;
import com.apihub.admin.entity.AppEntity;
import com.apihub.admin.entity.AppInterfaceEntity;
import com.apihub.admin.entity.InterfaceEntity;
import com.apihub.admin.mapper.AppInterfaceMapper;
import com.apihub.admin.mapper.AppMapper;
import com.apihub.admin.mapper.InterfaceMapper;
import com.apihub.admin.service.AppService;
import com.apihub.admin.util.AppKeyGenerator;
import com.apihub.common.exception.BizException;
import com.apihub.common.result.ErrorCode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 应用管理最终实现（v2）：MyBatis-Plus 实体/Mapper 分层，含数据权限、Secret 脱敏、
 * 业务校验与事务。
 */
@Service
public class AppServiceImpl implements AppService {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final int LIST_LIMIT = 200;

    private final AppMapper appMapper;
    private final AppInterfaceMapper appInterfaceMapper;
    private final InterfaceMapper interfaceMapper;

    public AppServiceImpl(AppMapper appMapper, AppInterfaceMapper appInterfaceMapper, InterfaceMapper interfaceMapper) {
        this.appMapper = appMapper;
        this.appInterfaceMapper = appInterfaceMapper;
        this.interfaceMapper = interfaceMapper;
    }

    @Override
    public AppCreatedVO create(AppCreateRequest request, Long operatorId, String rolesHeader) {
        Long ownerId = operatorId != null ? operatorId : request.getUserId();
        if (ownerId == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }

        AppEntity entity = new AppEntity();
        entity.setAppId(AppKeyGenerator.generateAppId());
        entity.setAppSecret(AppKeyGenerator.generateAppSecret());
        entity.setAppName(request.getAppName().trim());
        entity.setUserId(ownerId);
        entity.setStatus(1);
        entity.setQpsLimit(request.getQpsLimit() == null ? 10 : request.getQpsLimit());
        entity.setDailyQuota(request.getDailyQuota() == null ? 1000 : request.getDailyQuota());
        appMapper.insert(entity);

        return toCreatedVO(entity);
    }

    @Override
    public List<AppVO> list(Long operatorId, String rolesHeader) {
        LambdaQueryWrapper<AppEntity> wrapper = new LambdaQueryWrapper<AppEntity>()
                .orderByDesc(AppEntity::getId)
                .last("LIMIT " + LIST_LIMIT);
        if (operatorId != null && !isAdmin(rolesHeader)) {
            wrapper.eq(AppEntity::getUserId, operatorId);
        }
        return appMapper.selectList(wrapper).stream().map(this::toVO).toList();
    }

    @Override
    public AppDetailVO detail(Long id, Long operatorId, String rolesHeader) {
        AppEntity entity = requireApp(id);
        requireOwner(entity, operatorId, rolesHeader);

        AppDetailVO vo = new AppDetailVO();
        copyBase(entity, vo);
        vo.setGrantedInterfaces(listGrantedInterfaces(entity.getAppId()));
        return vo;
    }

    @Override
    public void updateStatus(Long id, Integer status, Long operatorId, String rolesHeader) {
        AppEntity entity = requireApp(id);
        requireOwner(entity, operatorId, rolesHeader);
        entity.setStatus(status);
        appMapper.updateById(entity);
    }

    @Override
    @Transactional
    public void grant(String appId, List<Long> interfaceIds, Long operatorId, String rolesHeader) {
        AppEntity app = requireEnabledApp(appId);
        requireOwner(app, operatorId, rolesHeader);

        for (Long interfaceId : interfaceIds) {
            InterfaceEntity iface = interfaceMapper.selectById(interfaceId);
            if (iface == null || iface.getStatus() != StatusEnum.ENABLE) {
                throw new BizException(ErrorCode.INTERFACE_OFFLINE);
            }
            Long exists = appInterfaceMapper.selectCount(new LambdaQueryWrapper<AppInterfaceEntity>()
                    .eq(AppInterfaceEntity::getAppId, appId)
                    .eq(AppInterfaceEntity::getInterfaceId, interfaceId));
            if (exists != null && exists > 0) {
                throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "接口 " + interfaceId + " 已开通，请勿重复开通");
            }
            AppInterfaceEntity relation = new AppInterfaceEntity();
            relation.setAppId(appId);
            relation.setInterfaceId(interfaceId);
            appInterfaceMapper.insert(relation);
        }
    }

    @Override
    public void revoke(String appId, Long interfaceId, Long operatorId, String rolesHeader) {
        AppEntity app = requireAppByAppId(appId);
        requireOwner(app, operatorId, rolesHeader);
        appInterfaceMapper.delete(new LambdaQueryWrapper<AppInterfaceEntity>()
                .eq(AppInterfaceEntity::getAppId, appId)
                .eq(AppInterfaceEntity::getInterfaceId, interfaceId));
    }

    // ---------- 私有辅助 ----------

    private List<GrantedInterfaceVO> listGrantedInterfaces(String appId) {
        List<AppInterfaceEntity> relations = appInterfaceMapper.selectList(
                new LambdaQueryWrapper<AppInterfaceEntity>()
                        .eq(AppInterfaceEntity::getAppId, appId)
                        .orderByAsc(AppInterfaceEntity::getId));
        if (relations.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> ids = relations.stream().map(AppInterfaceEntity::getInterfaceId).toList();
        Map<Long, InterfaceEntity> ifaceMap = interfaceMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(InterfaceEntity::getId, Function.identity()));
        return relations.stream()
                .map(r -> toGrantedVO(ifaceMap.get(r.getInterfaceId())))
                .filter(Objects::nonNull)
                .toList();
    }

    private GrantedInterfaceVO toGrantedVO(InterfaceEntity iface) {
        if (iface == null) {
            return null;
        }
        GrantedInterfaceVO vo = new GrantedInterfaceVO();
        vo.setId(iface.getId());
        vo.setName(iface.getName());
        vo.setPath(iface.getPath());
        vo.setMethod(iface.getMethod() == null ? null : iface.getMethod().getCode());
        vo.setDescription(iface.getDescription());
        vo.setVersion(iface.getVersion());
        vo.setCategory(iface.getCategory());
        vo.setStatus(iface.getStatus() == null ? null : iface.getStatus().getCode());
        return vo;
    }

    private AppEntity requireApp(Long id) {
        AppEntity entity = appMapper.selectById(id);
        if (entity == null) {
            throw new BizException(ErrorCode.APP_INVALID);
        }
        return entity;
    }

    private AppEntity requireAppByAppId(String appId) {
        AppEntity entity = appMapper.selectOne(new LambdaQueryWrapper<AppEntity>()
                .eq(AppEntity::getAppId, appId));
        if (entity == null) {
            throw new BizException(ErrorCode.APP_INVALID);
        }
        return entity;
    }

    private AppEntity requireEnabledApp(String appId) {
        AppEntity entity = requireAppByAppId(appId);
        if (!Objects.equals(entity.getStatus(), 1)) {
            throw new BizException(ErrorCode.APP_INVALID);
        }
        return entity;
    }

    /**
     * 数据权限：非管理员只能操作自己名下的应用。
     */
    private void requireOwner(AppEntity entity, Long operatorId, String rolesHeader) {
        if (isAdmin(rolesHeader)) {
            return;
        }
        if (operatorId == null || !Objects.equals(operatorId, entity.getUserId())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
    }

    private boolean isAdmin(String rolesHeader) {
        if (!StringUtils.hasText(rolesHeader)) {
            return false;
        }
        for (String role : rolesHeader.split(",")) {
            if (ROLE_ADMIN.equals(role.trim())) {
                return true;
            }
        }
        return false;
    }

    private AppVO toVO(AppEntity e) {
        AppVO vo = new AppVO();
        copyBase(e, vo);
        return vo;
    }

    private void copyBase(AppEntity e, AppVO vo) {
        vo.setId(e.getId());
        vo.setAppId(e.getAppId());
        vo.setAppName(e.getAppName());
        vo.setUserId(e.getUserId());
        vo.setStatus(e.getStatus());
        vo.setQpsLimit(e.getQpsLimit());
        vo.setDailyQuota(e.getDailyQuota());
        vo.setCreateTime(e.getCreateTime());
    }

    private AppCreatedVO toCreatedVO(AppEntity e) {
        AppCreatedVO vo = new AppCreatedVO();
        copyBase(e, vo);
        vo.setAppSecret(e.getAppSecret());
        return vo;
    }
}
