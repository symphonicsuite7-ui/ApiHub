-- ApiHub 课程设计 · 初始化脚本
CREATE DATABASE IF NOT EXISTS apihub DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE apihub;

CREATE TABLE IF NOT EXISTS sys_user (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    username      VARCHAR(64)  NOT NULL UNIQUE,
    password      VARCHAR(128) NOT NULL COMMENT 'BCrypt',
    nickname      VARCHAR(64)  NULL,
    status        TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '平台用户';

CREATE TABLE IF NOT EXISTS sys_role (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code  VARCHAR(32) NOT NULL UNIQUE,
    role_name  VARCHAR(64) NOT NULL
) COMMENT '角色';

CREATE TABLE IF NOT EXISTS sys_user_role (
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
    user_id BIGINT NOT NULL,
) COMMENT '用户角色';

CREATE TABLE IF NOT EXISTS api_app (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    app_id       VARCHAR(64)  NOT NULL UNIQUE,
    app_secret   VARCHAR(128) NOT NULL,
    app_name     VARCHAR(128) NOT NULL,
    user_id      BIGINT       NOT NULL,
    status       TINYINT      NOT NULL DEFAULT 1,
    qps_limit    INT          NOT NULL DEFAULT 10,
    daily_quota  INT          NOT NULL DEFAULT 1000,
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT '开放应用';

CREATE TABLE IF NOT EXISTS api_interface (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    name         VARCHAR(128) NOT NULL,
    path         VARCHAR(256) NOT NULL,
    method       VARCHAR(16)  NOT NULL DEFAULT 'GET',
    description  VARCHAR(512) NULL,
    version      VARCHAR(16)  NOT NULL DEFAULT 'v1',
    category     VARCHAR(64)  NULL,
    status       TINYINT      NOT NULL DEFAULT 0 COMMENT '0下线 1上线',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_path_method (path, method)
) COMMENT '接口资产';

CREATE TABLE IF NOT EXISTS api_app_interface (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    app_id        VARCHAR(64) NOT NULL,
    interface_id  BIGINT      NOT NULL,
    remain_count  INT         NULL COMMENT '剩余次数，空表示不限',
    create_time   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_app_iface (app_id, interface_id)
) COMMENT '应用开通接口';

CREATE TABLE IF NOT EXISTS api_invoke_log (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    trace_id      VARCHAR(64)  NOT NULL,
    app_id        VARCHAR(64)  NULL,
    interface_id  BIGINT       NULL,
    request_path  VARCHAR(256) NOT NULL,
    method        VARCHAR(16)  NOT NULL,
    status_code   INT          NOT NULL,
    cost_ms       INT          NOT NULL DEFAULT 0,
    ip            VARCHAR(64)  NULL,
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_trace (trace_id),
    KEY idx_app_time (app_id, create_time)
) COMMENT '调用日志';

CREATE TABLE IF NOT EXISTS api_category (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    name          VARCHAR(64) NOT NULL COMMENT '分类名称',
    code          VARCHAR(64) NOT NULL UNIQUE COMMENT '分类编码',
    parent_id     BIGINT NOT NULL DEFAULT 0 COMMENT '父分类',
    description   VARCHAR(255) NULL COMMENT '描述',
    status        TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
    sort          INT NOT NULL DEFAULT 0 COMMENT '排序',
    create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                  ON UPDATE CURRENT_TIMESTAMP
) COMMENT '接口分类';

INSERT INTO sys_role (role_code, role_name) VALUES
('ADMIN', '管理员'),
('USER', '普通用户')
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);

INSERT INTO api_interface (name, path, method, description, status, category) VALUES
('天气查询', '/api/open/weather', 'GET', '按城市查询天气（演示）', 1, '生活服务'),
('健康检查', '/api/open/health', 'GET', '开放服务健康检查', 1, '系统')
ON DUPLICATE KEY UPDATE description = VALUES(description);
