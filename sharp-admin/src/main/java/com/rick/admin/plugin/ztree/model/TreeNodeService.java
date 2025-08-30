package com.rick.admin.plugin.ztree.model;

import com.rick.db.repository.TableDAO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/9/2 21:14
 */
@Service
@RequiredArgsConstructor
public class TreeNodeService {

    private static final int COLLAPSE_SIZE = 30;

    private final TableDAO tableDAO;

    public List<TreeNode> getSelectTreeNode(String querySql, Map<String, Object> params) {
        List<TreeNode> treeNodeList = getStructuredTreeNode(querySql, params);

        List<TreeNode> containerList = new ArrayList<>();
        recursiveStructuredTreeNode(treeNodeList, treeNode -> {
            treeNode.setName(StringUtils.repeat("&nbsp;&nbsp;&nbsp;&nbsp;", treeNode.getLevel()) + treeNode.getName());
            containerList.add(treeNode);
        });

        return containerList;
    }

    public List<TreeNode> getStructuredTreeNode(String querySql, Map<String, Object> params) {
        List<TreeNode> treeNodeList = getTreeNode(querySql, params);
        List<TreeNode> ret = recursive(treeNodeList, 0L, 0);
        return ret;
    }

    public List<TreeNode> getTreeNode(String querySql, Map<String, Object> params) {
       return tableDAO.select(TreeNode.class, querySql, params);
    }

    public List<TreeNode> getCollapseSubNode(String querySql, Map<String, Object> params) {
        return collapseSubNode(getTreeNode(querySql, params));
    }

    public List<TreeNode> collapseSubNode(List<TreeNode> treeList) {
        if (treeList.size() <= COLLAPSE_SIZE) {
            return treeList;
        }

        // 子节点收起
        Map<Long, TreeNode> treeNodeMap = treeList.stream().collect(Collectors.toMap(TreeNode::getId, treeNode -> treeNode));
        Set<Long> parentIds = treeList.stream().map(TreeNode::getPId).collect(Collectors.toSet());
        Set<TreeNode> leafNodeSet = treeList.stream().filter(treeNode -> !parentIds.contains(treeNode.getId()) && treeNode.getPId() != 0).collect(Collectors.toSet());

        for (TreeNode treeNode : leafNodeSet) {
            treeNode.setOpen(false);

            TreeNode parentNode = treeNodeMap.get(treeNode.getPId());
            boolean allLeafNode = true;
            for (TreeNode node : treeList) {
                if (Objects.equals(node.getPId(), parentNode.getId())) {
                    if (!leafNodeSet.contains(node)) {
                        allLeafNode = false;
                    }
                }
            }

            if (allLeafNode) {
                parentNode.setOpen(false);
            }
        }

        return treeList;
    }

    private List<TreeNode> recursive(List<TreeNode> treeNodeList, Long pid, int level) {
        List<TreeNode> _children = new ArrayList<>();
        List<TreeNode> otherNode = new ArrayList<>();

        treeNodeList.forEach(node -> {
            if (Objects.equals(node.getPId(), pid)) {
                _children.add(node);
                node.setLevel(level);
            } else {
                otherNode.add(node);
            }
        });

        if (_children.size() > 0) {
            _children.forEach(_node -> {
                List<TreeNode> node_child = recursive(otherNode, _node.getId(), level + 1);
                if (node_child.size() > 0) {
                    _node.setSubTreeNodeList(node_child);
                }
            });
        }

        return _children;
    }

    private void recursiveStructuredTreeNode(List<TreeNode> treeNodeList, Consumer<TreeNode> treeNodeConsumer) {
        if (CollectionUtils.isEmpty(treeNodeList)) {
            return;
        }

        for (TreeNode treeNode : treeNodeList) {
            treeNodeConsumer.accept(treeNode);
            recursiveStructuredTreeNode(treeNode.getSubTreeNodeList(), treeNodeConsumer);
        }
    }

}
