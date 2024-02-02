;var style = document.createElement("style")
style.appendChild(document.createTextNode(".attachment .item {display: inline-block;position: relative;}  .attachment .item:before {content: '';position: absolute;width: calc(100% - 13px);height: 1px;background: #20a8d8;bottom: 6px;}"))
var head = document.getElementsByTagName("head")[0]
head.appendChild(style)

;(function (window) {

    let upload = {
        ajaxFileUpload: function (name) {
            name = name || 'attachment_file'
            $.ajaxFileUpload
            (
                {
                    url: '/documents/upload?name='+name+'&groupName=' + $('#' + name).data('group-name'), //用于文件上传的服务器端请求地址
                    secureuri: false, //是否需要安全协议，一般设置为false
                    fileElementId: name, //文件上传域的ID
                    dataType: 'json', //返回值类型 一般设置为json
                    success: function (data, status) { //服务器成功响应处理函数
                        upload.appendAttachment(data.data, name)
                    },
                    error: function (data, status, e) { //服务器响应失败处理函数
                        alert(e);
                    }
                }
            )
            return false;
        },
        setAttachment: function(attachments, name) {
            if (!attachments) {
                return
            }
            name = name || 'attachment_file'

            let $fileUpload = $('#' + name)
            $fileUpload.next().val('[]')
            $fileUpload.siblings('.item').remove()

            this.appendAttachment(attachments, name)
        },
        appendAttachment: function(attachments, name) {
            if (!attachments) {
                return
            }

            name = name || 'attachment_file'

            let $fileUpload = $('#' + name)
            let $parentContainer = $fileUpload.parent()
            let $fileList = $fileUpload.next()

            for (let attachment of attachments) {
                $parentContainer.append("<div class=\"item\">\n" +
                    "<a href=\""+attachment.url+"\" target=\"_blank\">"+attachment.fullName+"</a><button type=\"button\" class=\"btn btn-link attachment_delete_btn\" onclick=\"upload.deleteAttachment("+attachment.id+", this, name)\">删除</button>\n" +
                    "</div>")
            }

            let jsonString = $fileList.val()
            if(!jsonString) {
                jsonString = '[]'
            }
            let json = JSON.parse(jsonString);
            $fileList.val(JSON.stringify(json.concat(attachments)))
        },
        deleteAttachment: function (attachmentId, obj, name) {
            let $fileUpload = $('#' + name)
            let $fileList = $fileUpload.next()

            let jsonString = $fileList.val()
            if(jsonString) {
                let json = JSON.parse(jsonString);
                let filteredJson = json.filter(function (m) {
                    return m.id !== attachmentId;
                })

                $fileList.val(JSON.stringify(filteredJson))
            }

            $(obj).parent().remove()
        }
    }

    window.upload = upload

})(window)