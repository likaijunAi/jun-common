-- =============================================================
--  初始化数据：平台级权限 + admin 根商户 + 内置管理员角色
--  管理员账号请用 TenantService.create(adminUsername/adminPassword) 创建，
--  或在系统初始化后调用 UserService.create 绑定 ADMIN 角色。
-- =============================================================

-- admin 根商户
INSERT INTO sys_tenant (id, parent_id, path, level, name, code, status, create_at, update_at, del_at)
VALUES ('00000000000000000000000000000001', '0', '/0/00000000000000000000000000000001/', 0,
        '平台管理', 'admin', 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 0);

-- 内置管理员角色（不继承，数据范围为本商户及子商户）
INSERT INTO sys_role (id, tenant_id, parent_id, path, level, code, name, inherit, data_scope, status, builtin,
                      create_at, update_at, del_at)
VALUES ('00000000000000000000000000000011', '00000000000000000000000000000001', '0', '/0/', 0,
        'ADMIN', '超级管理员', 0, 'ALL', 1, 1,
        UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 0);

-- 平台级权限（tenant_id = 0）
INSERT INTO sys_permission (id, tenant_id, parent_id, path, level, code, name, type, sort, create_at, update_at, del_at)
VALUES
 ('00000000000000000000000000000101', '0', '0', '/0/', 0, 'sys:tenant',     '商户管理',   'MENU', 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 0),
 ('00000000000000000000000000000102', '0', '0', '/0/', 0, 'sys:tenant:add', '新增商户',   'BUTTON', 2, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 0),
 ('00000000000000000000000000000103', '0', '0', '/0/', 0, 'sys:tenant:edit','编辑商户',   'BUTTON', 3, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 0),
 ('00000000000000000000000000000104', '0', '0', '/0/', 0, 'sys:tenant:del', '删除商户',   'BUTTON', 4, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 0),
 ('00000000000000000000000000000201', '0', '0', '/0/', 0, 'sys:user',       '用户管理',   'MENU', 5, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 0),
 ('00000000000000000000000000000202', '0', '0', '/0/', 0, 'sys:user:add',   '新增用户',   'BUTTON', 6, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 0),
 ('00000000000000000000000000000203', '0', '0', '/0/', 0, 'sys:user:edit',  '编辑用户',   'BUTTON', 7, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 0),
 ('00000000000000000000000000000204', '0', '0', '/0/', 0, 'sys:user:del',   '删除用户',   'BUTTON', 8, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 0),
 ('00000000000000000000000000000301', '0', '0', '/0/', 0, 'sys:role',       '角色管理',   'MENU', 9, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 0),
 ('00000000000000000000000000000302', '0', '0', '/0/', 0, 'sys:role:grant', '角色授权',   'BUTTON', 10, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 0),
 ('00000000000000000000000000000401', '0', '0', '/0/', 0, 'sys:permission', '权限管理',   'MENU', 11, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 0);

-- admin 商户可用权限（全量平台权限）
INSERT INTO sys_tenant_permission (id, tenant_id, permission_code, source_tenant_id, create_at)
SELECT REPLACE(UUID(), '-', ''), '00000000000000000000000000000001', code, '0', UNIX_TIMESTAMP() * 1000
FROM sys_permission WHERE tenant_id = '0' AND del_at = 0;

-- ADMIN 角色授予全部平台权限
INSERT INTO sys_role_permission (id, role_id, permission_code, granted, create_at)
SELECT REPLACE(UUID(), '-', ''), '00000000000000000000000000000011', code, 1, UNIX_TIMESTAMP() * 1000
FROM sys_permission WHERE tenant_id = '0' AND del_at = 0;
