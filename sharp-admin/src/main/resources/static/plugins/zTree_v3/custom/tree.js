/*
	author:Rick
	email:jkxyx205@163.com
*/
(function($) {
	var zTree = function(element,options) {
		this.$element = $(element);
		this.options = $.extend({},$.fn.tree.defaults, options);
		this.select = true;
		if(this.options.setting.check)
			this.select = false;
		this.init();
	}
	zTree.prototype = {
		constructor:zTree,
		abc:function() {},
		init:function() { 
			var zTree = this;
			var zNodes = this.options.zNodes;
			var setting = this.options.setting;
			//if async bind event
			if(this.options.async) {
				setting.async = {
					enable: true,
					url: function(treeId, treeNode) {
						var setting	 = zTree.options;
						var url = setting.url + "/" + treeNode.id;
						
						var data = $.extend({},setting.data,{"queryName":setting.queryName});
							
						data = $.param(data);
						url = url  + "?" + data;
						return url;
					}
				};
				
				var callback = setting.callback;
				if(!callback) 
					callback = setting.callback = {};
				 
				callback.onAsyncSuccess = function(event, treeId, treeNode, msg) {
					if (!msg || msg.length == 0) {
						return;
					}
					var tree = zTree.treeObject;
					totalCount = treeNode.count;
					if (treeNode.children.length < totalCount) {
						//setTimeout(function() {ajaxGetNodes(treeNode);}, perTime);
					} else {
						treeNode.icon = "";
						tree.updateNode(treeNode);
						tree.selectNode(treeNode.children[0]);
					}
				};
				callback.onAsyncError = function(event, treeId, treeNode, XMLHttpRequest, textStatus, errorThrown) {
					var tree = zTree.treeObject;
					alert("异步获取数据出现异常。");
					treeNode.icon = "";
					tree.updateNode(treeNode);
				};
				
				var customExtend;
				if (callback.beforeExpand) {
						customExtend = callback.beforeExpand;
				}
						
				callback.beforeExpand = function(treeId, treeNode) {
						zTree.getDataFromServer(treeId, treeNode,zTree);
						if(customExtend)
							customExtend();
				};	
			}
			
			if(!zNodes) {//需要后台调用
				var queryName = this.options.initQueryName == undefined ? this.options.queryName : this.options.initQueryName; 
				var data = $.extend({},this.options.data,{"queryName":queryName});
				$.ajax({
					url:this.options.url,
					data:data,
					async:false,
					dataType:'json',
					success:function(nodes) {
						zNodes = nodes;
					}
					
				});
			}
			
			this.treeObject = $.fn.zTree.init(this.$element,setting, zNodes);

			
			if(this.options.values) { //defalut value
				var ids = this.options.values.split(",");
				for (var i in ids) {
				var node = this.treeObject.getNodeByParam("id", ids[i], null);
				 if(this.select) {
					this.treeObject.selectNode(node,true);
				 } else  {
					node.checked = true;
					this.treeObject.updateNode(node);					
				 }
				}	
			}
		},
		getSelectedNode:function() {
			var treeObject = this.getTreeObject();
			var nodes ;
			if(this.select)
				nodes = treeObject.getSelectedNodes();
			else 
				nodes = treeObject.getCheckedNodes(true);
			return nodes;
		},
		getSelectedIds:function() {
			var nodes = this.getSelectedNode();
			var nodeIds = [];
				if (nodes.length > 0) {
					for (var i in  nodes) {
						nodeIds.push(nodes[i].id);
					}
				}
			return nodeIds.join(",");
		},
		select:function() {
			return this.select;
		},
		getTreeObject:function() {
			return this.treeObject;
		},
		ajaxGetNodes:function(treeId,treeNode, reloadType,zTree) {
			var tree = zTree.treeObject;
			if (reloadType == "refresh") {
				treeNode.icon = "css/zTreeStyle/img/loading.gif";
				tree.updateNode(treeNode);
			}
			tree.reAsyncChildNodes(treeNode, reloadType, true);
		},
		getDataFromServer:function(treeId, treeNode,zTree) {
			if (!treeNode.isAjaxing) {
				zTree.ajaxGetNodes(treeId,treeNode, "refresh",zTree);
				return true;
			} else {
				alert("zTree 正在下载数据中，请稍后展开节点。。。");
				return false;
			}
		},
	};

	$.fn.tree = function(options) {
		var args = arguments;
        var value;
        var chain = this.each(function() {
            data = $(this).data("tree");
            if (!data) {
                if (options && typeof options == 'object') {
                    return $(this).data("tree", data = new zTree(this, options));
                }
            } else {
                if (typeof options == 'string') {
                    if (data[options] instanceof Function) { 
                        var property = options; 
						[].shift.apply(args);
                        value = data[property].apply(data, args);
                    } else {
                        return value = data.options[options];
                    }
                }
            }

        });

        if (value !== undefined) {
            return value;
        } else {
            return chain;
        }
	}

	function getBase() {
		var pathName = window.document.location.pathname;
		var projectName = pathName.substring(0, pathName.substr(1).indexOf('/') + 1);
		return projectName;
	}
	
$.fn.tree.defaults = {
		async:false,
		url:getBase() + "/ztree/init"
	};		
})(jQuery);