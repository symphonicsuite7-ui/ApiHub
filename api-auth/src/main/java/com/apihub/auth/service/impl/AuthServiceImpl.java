package com.apihub.auth.service.impl;

import com.apihub.auth.dto.LoginRequest;
import com.apihub.auth.dto.LoginVO;
import com.apihub.auth.dto.RegisterRequest;
import com.apihub.auth.service.AuthService;
import com.apihub.common.exception.BizException;
import com.apihub.common.jwt.JwtUtil;
import com.apihub.common.result.ErrorCode;
import io.jsonwebtoken.JwtException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM sys_user WHERE username = ?",
                Integer.class,
                request.getUsername()
        );
        if (exists != null && exists > 0) {
            throw new BizException(ErrorCode.USER_EXISTS);
        }
        String nickname = StringUtils.hasText(request.getNickname())
                ? request.getNickname()
                : request.getUsername();
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO sys_user(username, password, nickname, status) VALUES (?, ?, ?, 1)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, request.getUsername());
            ps.setString(2, passwordEncoder.encode(request.getPassword()));
            ps.setString(3, nickname);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new BizException(ErrorCode.INTERNAL_ERROR);
        }
        long userId = key.longValue();
        // 用户名为 admin 时授予管理员，便于课程演示
        String roleCode = "admin".equalsIgnoreCase(request.getUsername()) ? "ADMIN" : "USER";
        Long roleId = jdbcTemplate.queryForObject(
                "SELECT id FROM sys_role WHERE role_code = ?",
                Long.class,
                roleCode
        );
        jdbcTemplate.update("INSERT INTO sys_user_role(user_id, role_id) VALUES (?, ?)", userId, roleId);
    }

    @Override
    public LoginVO login(LoginRequest request) {
        List<Map<String, Object>> users = jdbcTemplate.queryForList(
                "SELECT id, username, password, nickname, status FROM sys_user WHERE username = ?",
                request.getUsername()
        );
        if (users.isEmpty()) {
            throw new BizException(ErrorCode.USER_PASSWORD_ERROR);
        }
        Map<String, Object> user = users.get(0);
        int status = ((Number) user.get("status")).intValue();
        if (status != 1) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        String encoded = String.valueOf(user.get("password"));
        if (!passwordEncoder.matches(request.getPassword(), encoded)) {
            throw new BizException(ErrorCode.USER_PASSWORD_ERROR);
        }
        Long userId = ((Number) user.get("id")).longValue();
        return buildLoginVO(userId, String.valueOf(user.get("username")), (String) user.get("nickname"));
    }

    @Override
    public LoginVO currentUser(String token) {
        Long userId;
        try {
            userId = jwtUtil.getUserId(jwtUtil.parse(token));
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        List<Map<String, Object>> users = jdbcTemplate.queryForList(
                "SELECT id, username, nickname, status FROM sys_user WHERE id = ?",
                userId
        );
        if (users.isEmpty()) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        Map<String, Object> user = users.get(0);
        return buildLoginVO(
                ((Number) user.get("id")).longValue(),
                String.valueOf(user.get("username")),
                (String) user.get("nickname")
        );
    }

    private LoginVO buildLoginVO(Long userId, String username, String nickname) {
        List<String> roles = jdbcTemplate.query(
                "SELECT r.role_code FROM sys_role r INNER JOIN sys_user_role ur ON r.id = ur.role_id WHERE ur.user_id = ?",
                (rs, rowNum) -> rs.getString("role_code"),
                userId
        );
        LoginVO vo = new LoginVO();
        vo.setToken(jwtUtil.createToken(userId, username, roles));
        vo.setUserId(userId);
        vo.setUsername(username);
        vo.setNickname(nickname);
        vo.setRoles(roles);
        return vo;
    }
}
