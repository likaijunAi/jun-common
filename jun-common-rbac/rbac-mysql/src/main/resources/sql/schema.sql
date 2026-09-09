-- =============================================================
--  jun-common-rbac  MySQL 表结构
--  约定：del_at BIGINT 毫秒时间戳，0 表示未删除
--        树结构统一使用物化路径 path，如 /0/1/5/
-- =============================================================

-- 1 商户（多级树，根为 admin）
CREATE TABLE IF NOT EXISTS sys_tenant (
  id           VARCHAR(32)  NOT NULL COMMENT '主键',
  parent_id    VARCHAR(32)  NOT NULL DEFAULT '0' COMMENT '父商户，根为 0',
  path         VARCHAR(512) NOT NULL DEFAULT '/0/' COMMENT '物化路径 /0/1/5/',
  level        INT          NOT NULL DEFAULT 0 COMMENT '层级，根为 0',
  name         VARCHAR(64)  NOT NULL COMMENT '商户名称',
  code         VARCHAR(64)  NOT NULL COMMENT '商户编码，全局唯一',
  status       TINYINT      NOT NULL DEFAULT 1 COMMENT '1 启用 0 停用',
  contact      VARCHAR(32)  NULL COMMENT '联系人',
  phone        VARCHAR(32)  NULL COMMENT '联系电话',
  remark       VARCHAR(255) NULL COMMENT '备注',
  create_at    BIGINT       NOT NULL DEFAULT 0 COMMENT '创建时间戳(ms)',
  update_at    BIGINT       NOT NULL DEFAULT 0 COMMENT '更新时间戳(ms)',
  del_at       BIGINT       NOT NULL DEFAULT 0 COMMENT '删除时间戳(ms)，0 未删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_tenant_code (code, del_at),
  KEY idx_tenant_path (path),
  KEY idx_tenant_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户';

-- 2 用户（只属于一个角色）
CREATE TABLE IF NOT EXISTS sys_user (
  id              VARCHAR(32)  NOT NULL COMMENT '主键',
  tenant_id       VARCHAR(32)  NOT NULL DEFAULT '0' COMMENT '所属商户',
  username        VARCHAR(64)  NOT NULL COMMENT '登录名，全平台唯一',
  password        VARCHAR(128) NOT NULL COMMENT '密码摘要',
  salt            VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '盐',
  nickname        VARCHAR(64)  NULL COMMENT '昵称',
  phone           VARCHAR(32)  NULL,
  email           VARCHAR(64)  NULL,
  avatar          VARCHAR(255) NULL,
  role_id         VARCHAR(32)  NOT NULL DEFAULT '0' COMMENT '角色，只属于一个',
  status          TINYINT      NOT NULL DEFAULT 1 COMMENT '1 启用 0 停用 2 锁定',
  login_fail_count INT         NOT NULL DEFAULT 0 COMMENT '连续失败次数',
  lock_until      BIGINT       NOT NULL DEFAULT 0 COMMENT '锁定截止时间戳(ms)',
  last_login_at   BIGINT       NOT NULL DEFAULT 0,
  last_login_ip   VARCHAR(64)  NULL,
  create_at       BIGINT       NOT NULL DEFAULT 0,
  update_at       BIGINT       NOT NULL DEFAULT 0,
  del_at          BIGINT       NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_name (username, del_at),
  KEY idx_user_role (role_id),
  KEY idx_user_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户';

-- 3 角色（多级继承，同一商户内成树）
CREATE TABLE IF NOT EXISTS sys_role (
  id                VARCHAR(32)  NOT NULL COMMENT '主键',
  tenant_id         VARCHAR(32)  NOT NULL DEFAULT '0' COMMENT '所属商户',
  parent_id         VARCHAR(32)  NOT NULL DEFAULT '0' COMMENT '父角色',
  path              VARCHAR(512) NOT NULL DEFAULT '/0/' COMMENT '物化路径',
  level             INT          NOT NULL DEFAULT 0,
  code              VARCHAR(64)  NOT NULL COMMENT '角色编码，商户内唯一',
  name              VARCHAR(64)  NOT NULL COMMENT '角色名称',
  description       VARCHAR(255) NULL,
  inherit           TINYINT      NOT NULL DEFAULT 1 COMMENT '是否继承父角色权限',
  data_scope        VARCHAR(32)  NOT NULL DEFAULT 'TENANT' COMMENT 'ALL/TENANT_AND_CHILDREN/TENANT/SELF',
  share_to_children TINYINT      NOT NULL DEFAULT 0 COMMENT '是否可被子商户引用为父角色',
  status            TINYINT      NOT NULL DEFAULT 1,
  builtin           TINYINT      NOT NULL DEFAULT 0 COMMENT '内置角色不允许删除',
  create_at         BIGINT       NOT NULL DEFAULT 0,
  update_at         BIGINT       NOT NULL DEFAULT 0,
  del_at            BIGINT       NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_code (tenant_id, code, del_at),
  KEY idx_role_path (path),
  KEY idx_role_shared (share_to_children, tenant_id),
  KEY idx_role_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色';

-- 4 权限（按商户隔离，tenant_id=0 为平台级）
CREATE TABLE IF NOT EXISTS sys_permission (
  id         VARCHAR(32)  NOT NULL COMMENT '主键',
  tenant_id  VARCHAR(32)  NOT NULL DEFAULT '0' COMMENT '所属商户，0 平台级',
  parent_id  VARCHAR(32)  NOT NULL DEFAULT '0',
  path       VARCHAR(512) NOT NULL DEFAULT '/0/',
  level      INT          NOT NULL DEFAULT 0,
  code       VARCHAR(128) NOT NULL COMMENT '权限标识，如 sys:user:add',
  name       VARCHAR(64)  NOT NULL,
  type       VARCHAR(16)  NOT NULL DEFAULT 'API' COMMENT 'MENU/BUTTON/API/DATA',
  sort       INT          NOT NULL DEFAULT 0,
  remark     VARCHAR(255) NULL,
  create_at  BIGINT       NOT NULL DEFAULT 0,
  update_at  BIGINT       NOT NULL DEFAULT 0,
  del_at     BIGINT       NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_perm_code (tenant_id, code, del_at),
  KEY idx_perm_path (path),
  KEY idx_perm_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限';

-- 5 角色直接权限（只存本层，granted=0 为显式 deny）
CREATE TABLE IF NOT EXISTS sys_role_permission (
  id              VARCHAR(32)  NOT NULL,
  role_id         VARCHAR(32)  NOT NULL,
  permission_code VARCHAR(128) NOT NULL,
  granted         TINYINT      NOT NULL DEFAULT 1 COMMENT '1 授予 0 拒绝(deny 优先)',
  create_at       BIGINT       NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_perm (role_id, permission_code),
  KEY idx_rp_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限';

-- 6 用户附加/临时权限
CREATE TABLE IF NOT EXISTS sys_user_permission (
  id              VARCHAR(32)  NOT NULL,
  user_id         VARCHAR(32)  NOT NULL,
  permission_code VARCHAR(128) NOT NULL,
  granted         TINYINT      NOT NULL DEFAULT 1 COMMENT '1 追加 0 禁用',
  expire_at       BIGINT       NOT NULL DEFAULT 0 COMMENT '过期时间戳(ms)，0 永久',
  create_at       BIGINT       NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_perm (user_id, permission_code),
  KEY idx_up_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户附加权限';

-- 7 商户可用权限白名单
CREATE TABLE IF NOT EXISTS sys_tenant_permission (
  id              VARCHAR(32)  NOT NULL,
  tenant_id       VARCHAR(32)  NOT NULL,
  permission_code VARCHAR(128) NOT NULL,
  source_tenant_id VARCHAR(32) NOT NULL DEFAULT '0' COMMENT '下发来源商户',
  create_at       BIGINT       NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_tenant_perm (tenant_id, permission_code),
  KEY idx_tp_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户可用权限';
