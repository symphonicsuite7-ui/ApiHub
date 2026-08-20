package com.apihub.admin.service.serviceImpl;

import com.apihub.admin.entity.InterfaceEntity;
import com.apihub.admin.mapper.InterfaceMapper;
import com.apihub.admin.service.InterfaceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.swagger.v3.oas.annotations.servers.Server;

@Server
public class InterfaceServiceImpl extends ServiceImpl<InterfaceMapper, InterfaceEntity> implements InterfaceService {

}
