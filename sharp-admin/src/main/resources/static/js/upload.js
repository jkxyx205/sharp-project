;var style = document.createElement("style")
style.appendChild(document.createTextNode(".attachment .item {display: inline-block;position: relative;}  .attachment .item:before {content: '';position: absolute;width: calc(100% - 13px);height: 1px;background: #20a8d8;bottom: 6px;}"))
var head = document.getElementsByTagName("head")[0]
head.appendChild(style)


 /**
  *  type = file 的name
 * @param name
 * @constructor
 */
function FileUpload(name, $itemContainer, uploadConsumer, deleteConsumer) {
    this.name = name || 'attachment_file'
    this.$fileUpload = $('#' + name)
    this.$itemContainer = $itemContainer || this.$fileUpload.next()
    this.$valueContainer = this.$fileUpload.prev()
    this.attachmentList = JSON.parse(this.$valueContainer.val())

    this.uploadConsumer = uploadConsumer;
    this.deleteConsumer = deleteConsumer;
}

FileUpload.prototype.ajaxFileUpload = function () {
    $.ajaxFileUpload
    (
        {
            url: '/documents/upload?name='+this.name+'&groupName=' + $('#' + this.name).data('group-name'), //用于文件上传的服务器端请求地址
            secureuri: false, //是否需要安全协议，一般设置为false
            fileElementId: this.name, //文件上传域的ID
            dataType: 'json', //返回值类型 一般设置为json
            success: (data, status)=> { //服务器成功响应处理函数
                this.appendAttachment(data.data, this.uploadConsumer)
            },
            error: function (data, status, e) { //服务器响应失败处理函数
                alert(e);
            }
        }
    )
    return false;
}

// 附件列表(上传文件默认样式)
FileUpload.prototype.appendAttachment = function(attachments, consumer) {
    if (!attachments) {
        return
    }

    this.attachmentList = this.attachmentList.concat(attachments)
    this.$valueContainer.val(JSON.stringify(this.attachmentList))

    if (consumer) {
        consumer(attachments, this.$itemContainer)
        return;
    }

    if (this.$itemContainer.length == 0) {
        console.log("$itemContainer 不存在！！")
        return;

    }
    for (let attachment of attachments) {
        let $item = $("<div class=\"item\">\n" +
            "<a href=\""+attachment.url+"\" target=\"_blank\">"+attachment.fullName+"</a><button type=\"button\" class=\"btn btn-link attachment_delete_btn\" onclick=\"this.upload.deleteAttachment('"+attachment.id+"', this)\">删除</button>\n" +
            "</div>")
        this.$itemContainer.append($item)
        let upload = this;
        $item.find('.attachment_delete_btn').each(function () {
            this.upload = upload
        })
    }
}

FileUpload.prototype.deleteAttachment = function (attachmentId, deleteBtn) {
    this.attachmentList = this.attachmentList.filter(function (m) {
        return m.id !== attachmentId;
    })

    this.$valueContainer.val(JSON.stringify(this.attachmentList))

    if (this.deleteConsumer) {
        this.deleteConsumer(attachmentId, deleteBtn)
    } else {
        $(deleteBtn).parent(".item").remove()
    }
}

FileUpload.prototype.getAttachments = function () {
    return this.attachmentList
}

window.FileUpload = FileUpload
