package com.jun.common.rbac.model

/**
 * 通用树节点（商户树 / 角色树 / 权限树）
 *
 * @author likaijun
 * https://github.com/likaijunAi
 * l@xsocket.cn
 * created at 2026/9/8
 **/
open class TreeNode<T> {
    var data: T? = null
    var children: MutableList<TreeNode<T>> = mutableListOf()

    fun add(child: TreeNode<T>) {
        children.add(child)
    }
}

object TreeNodes {

    /**
     * 由扁平列表构建树。
     * @param idOf 节点 id
     * @param parentIdOf 父节点 id
     * @param rootId 根 id
     */
    fun <T> build(
        list: List<T>,
        rootId: String = "0",
        idOf: (T) -> String,
        parentIdOf: (T) -> String
    ): List<TreeNode<T>> {
        if (list.isEmpty()) return emptyList()
        val nodes = LinkedHashMap<String, TreeNode<T>>()
        list.forEach { nodes[idOf(it)] = TreeNode<T>().apply { data = it } }

        val roots = mutableListOf<TreeNode<T>>()
        list.forEach { item ->
            val node = nodes[idOf(item)]!!
            val parent = nodes[parentIdOf(item)]
            if (parent == null || idOf(item) == rootId) roots.add(node) else parent.add(node)
        }
        return roots
    }
}
