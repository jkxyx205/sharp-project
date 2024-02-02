package com.rick.admin.plugin.ztree.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class TreeNode implements Serializable {

	@JsonSerialize(using = ToStringSerializer.class)
	private Long id;
	
	private String name;

	@JsonSerialize(using = ToStringSerializer.class)
    @JsonProperty("pId")
	private Long pId;

	private boolean open;

	private String icon;
	
	private String iconSkin;

	private int level;

	private List<TreeNode> subTreeNodeList;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof TreeNode)) return false;

		TreeNode treeNode = (TreeNode) o;

		return getId().equals(treeNode.getId());
	}

	@Override
	public int hashCode() {
		return id.intValue();
	}
}
