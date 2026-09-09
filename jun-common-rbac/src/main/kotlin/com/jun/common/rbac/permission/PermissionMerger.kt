package com.jun.common.rbac.permission

/**
 * 角色链上某一层的权限声明
 *
 * @param inherit 该角色是否继承父角色；false 时其祖先不再参与合并
 */
data class RoleGrant(
    val roleId: String,
    val inherit: Boolean = true,
    val grants: Set<String> = emptySet(),
    val denies: Set<String> = emptySet()
)

/**
 * 权限合并：读时沿父链展开，deny 优先
 *
 * 父角色权限减少 => 子角色立即生效（无需刷库）
 *
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
object PermissionMerger {

    /**
     * @param chain 自顶向下（root -> ... -> 目标角色）
     * @return 合并后的有效权限
     */
    fun merge(chain: List<RoleGrant>): Set<String> {
        if (chain.isEmpty()) return emptySet()

        // 从叶子往根找第一个 inherit=false 的角色，其之前的祖先全部截断
        var start = 0
        for (i in chain.size - 1 downTo 1) {
            if (!chain[i].inherit) {
                start = i
                break
            }
        }

        val grant = LinkedHashSet<String>()
        val deny = LinkedHashSet<String>()
        for (i in start until chain.size) {
            grant.addAll(chain[i].grants)
            deny.addAll(chain[i].denies)
        }
        return grant - deny
    }
}
