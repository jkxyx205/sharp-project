( function() {
    function extractFileExt(ext) {
        if(ext == "xls" || ext == "xlsx")
            return "excel.png";
        if(ext == "doc" || ext == "docx")
            return "word.png";
        if(ext == "jpg" || ext == "png" || ext == "bmp" || ext == "jpeg" || ext == "gif" || ext == "ico")
            return "image.png";
        if(ext == "mp3" || ext == "wma")
            return "mp3.png";
        if(ext == "rar" || ext == "zip" || ext == "jar" || ext == "tar")
            return "rar.png";
        if(ext == "txt")
            return "txt.png";
        if(ext == "pdf")
            return "pdf.png";
        if(ext == "avi" || ext == "mp4" ||ext == "rmvb")
            return "video.png";

        return 'else.png'
    }
    function extractFileSize(size) {

        if(size >  1024 *1024) {
            return (size/(1024 *1024)).toFixed(2) + "M";
        } else {
            return (size/1024).toFixed(2) + "K";
        }
    }

	CKEDITOR.dialog.add( 'file', function( editor )  
			{  
			    return {   
			        title : '附件上传',
			        minWidth : 600,  
			        minHeight : 200,  
			        contents : [  
			            {
			                elements :  
			                [  
			                    {  
			                        type : 'html',
			                        html : '<span style="color: #8d8d8d; float: right;">大小不超过20M</span><input id="fileupload" type="file" name="upload" multiple data-url="/ckeditor/uploadFile" data-sequential-uploads="true"><div id="fileList" style="padding:10px;height:200px; border:1px solid #BCBCBC; margin-top:5px; overflow:auto;"></div>'
			                    }
			                ]  
			            }  
			        ],
			        onLoad:function() {
                        var maxFiles = 10;
                        var counter = 0;
			        	$('#fileupload').fileupload({
			     	        dataType: 'json',
							messages: {
                                maxFileSize: '文件大小不能超过20MB',
								acceptFileTypes: 'File type not allowed'
                            },
                            add: function (e, data) {

                                var size = data.files[0].size //data.originalFiles[0]["size"];
                                if(size > (20 * 1024 * 1024)){
                                    alert("【" + data.files[0].name + "】文件大小不能超过20MB！");
                                    return ;
                                }

                                if(counter < maxFiles){
                                    counter++;
                                } else {
                                	alert('最多只能同时上传10个文件')
                                    return false;
								}

                                data.submit();

                            },
			     	        done: function (e, data) {
                                counter = 0
			     	            $.each(data.result, function (index, file) {
			     	            	var a = "<span><img style='vertical-align: middle; width: 16px;' src=\"/plugins/ckeditor/plugins/file/icons/"+extractFileExt(file.extension)+"\"/>&nbsp;&nbsp;<a target='_blank' href=\""+file.url+"\">"+file.fullName+"</a>&nbsp;<span style='color:#BCBCBC;'>"+extractFileSize(file.size)+"</span>" +
			     	            			"\&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a class='delClass' style='color:#20a8d8; cursor: pointer' href='javascript:void(0);' onclick=\"$(this).parent().remove();\">删除</a></span><br/>";
			     	            	$("#fileList").append(a);
			     	            }); 
			     	        }
			     	    }).prop('disabled', !$.support.fileInput)
			     	        .parent().addClass($.support.fileInput ? undefined : 'disabled');

			        },
			        onShow:function() {
			        	 $('#upload').val('');  
			        	 $("#fileList").html('');
			        },
			        onOk: function() {
			        	$("#fileList a.delClass").remove();
			        	$("#fileList a.delClass").next().remove();
			        	editor.insertHtml($("#fileList").html());
			     	}

			    };  
			} );  
} )();