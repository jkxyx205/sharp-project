/**
 * @license Copyright (c) 2003-2015, CKSource - Frederico Knabben. All rights reserved.
 * For licensing, see LICENSE.md or http://ckeditor.com/license
 */

/**
 * @fileOverview File plugin
 */

( function() {
	CKEDITOR.plugins.add('file',  
		    {         
		        requires : ['dialog'],  
				icons: 'file', // %REMOVE_LINE_CORE%
		        init : function (editor)  
		        {  
		            var pluginName = 'file';  
		              
		            //加载自定义窗口，就是dialogs前面的那个/让我纠结了很长时间  
		            CKEDITOR.dialog.add(pluginName,this.path + "dialogs/file.js");
		              
		            //给自定义插件注册一个调用命令  
		            editor.addCommand( pluginName, new CKEDITOR.dialogCommand( pluginName ) );  
		            //注册一个按钮，来调用自定义插件  
		            editor.ui.addButton('File',  
		                    {  
		                        //editor.lang.mine是我在zh-cn.js中定义的一个中文项，  
		                        //这里可以直接写英文字符，不过要想显示中文就得修改zh-cn.js  
		                        label : '上传附件',  
		                        command : pluginName,
		                        toolbar:"insert,25" 
		                    });  
		        }  
		    }  
		);  

} )();

 