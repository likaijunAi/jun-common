package com.jun.common.rbac.tree

/**
 * 物化路径工具（商户树 / 角色树 / 权限树通用）
 *
 * 路径格式：/0/1/5/，根节点 id 固定为 0
 *
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
object PathTree {

    const val ROOT_ID = "0"
    const val ROOT_PATH = "/0/"
    const val SEP = "/"

    /** 父路径 + 自身 id = 自身路径 */
    fun build(parentPath: String, id: String): String {
        val p = if (parentPath.endsWith(SEP)) parentPath else parentPath + SEP
        return "$p$id$SEP"
    }

    /** 路径上的全部 id（含根、含自身），自顶向下 */
    fun ids(path: String): List<String> = path.split(SEP).filter { it.isNotBlank() }

    fun selfId(path: String): String = ids(path).lastOrNull() ?: ROOT_ID

    /** 祖先 id（不含自身） */
    fun ancestorIds(path: String): List<String> = ids(path).dropLast(1)

    fun parentId(path: String): String = ancestorIds(path).lastOrNull() ?: ROOT_ID

    /** 根 level = 0 */
    fun level(path: String): Int = (ids(path).size - 1).coerceAtLeast(0)

    /** ancestorPath 是否为 childPath 的祖先（不含自身） */
    fun isAncestor(ancestorPath: String, childPath: String): Boolean {
        val a = if (ancestorPath.endsWith(SEP)) ancestorPath else ancestorPath + SEP
        return childPath.startsWith(a) && childPath != a
    }

    /** 子孙查询用的 LIKE 前缀 */
    fun descendantPrefix(path: String): String = if (path.endsWith(SEP)) path else path + SEP

    /** 把 oldPrefix 打头的路径替换为 newPrefix，用于移动子树 */
    fun replacePrefix(path: String, oldPrefix: String, newPrefix: String): String =
        newPrefix + path.removePrefix(oldPrefix)
}
