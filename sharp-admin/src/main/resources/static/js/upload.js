;var style = document.createElement("style")
style.appendChild(document.createTextNode(".attachment .item {display: inline-block;position: relative;}  .attachment .item:before {content: '';position: absolute;width: calc(100% - 13px);height: 1px;background: #20a8d8;bottom: 6px;}"))
var head = document.getElementsByTagName("head")[0]
head.appendChild(style)


 /**
  *  type = file 的name
  *  $itemContainer
  *     - false file 不写入 $itemContainer
  *     - null 自动获取 $itemContainer
  *     - 指定 $itemContainer
 * @param name
 * @constructor
 */
function FileUpload(name, $itemContainer, uploadConsumer, deleteConsumer, itemSupplier) {
    this.name = name || 'attachment_file'
    this.$fileUpload = $('#' + name)
    if (!this.$fileUpload.length) {
        console.log("input file name = ", name, " 找不到")
        return
    }
    this.fileUploadHtml = this.$fileUpload.prop('outerHTML')

    this.$itemContainer = $itemContainer === false ? false : ($itemContainer || this.$fileUpload.next())
    this.$valueContainer = this.$fileUpload.prev()

    if (this.$valueContainer[0].tagName === 'INPUT' && (this.$valueContainer[0].type === 'text' || this.$valueContainer[0].type === 'hidden')) {
        this.attachmentList = JSON.parse(this.$valueContainer.val())
    }

    this.uploadConsumer = uploadConsumer
    this.deleteConsumer = deleteConsumer
    this.itemSupplier = itemSupplier
}

FileUpload.prototype.ajaxFileUpload = function () {
    let that = this

    $.ajaxFileUpload(
        {
            url: '/documents/upload?name='+this.name+'&groupName=' + $('#' + this.name).data('group-name'), //用于文件上传的服务器端请求地址
            secureuri: false, //是否需要安全协议，一般设置为false
            fileElementId: this.name, //文件上传域的ID
            dataType: 'json', //返回值类型 一般设置为json
            success: (data, status)=> { //服务器成功响应处理函数
                this.appendAttachment(data.data, this.uploadConsumer, this.itemSupplier)
            },
            error: function (data, status, e) { //服务器响应失败处理函数
                alert(e);
            },
            complete: function () {
                // 重新添加file 控件
                let newFileInput = that.$fileUpload = $(that.fileUploadHtml)
                that.$valueContainer.after(newFileInput)
                newFileInput.next().remove()
            }
        }
    )


}

// 附件列表(上传文件默认样式)
FileUpload.prototype.appendAttachment = function(attachments, consumer, itemSupplier) {
    if (!attachments) {
        return
    }

    if (consumer) {
        if (consumer(attachments, this.$itemContainer) === false) {
            return
        }
    }

    this.attachmentList = this.attachmentList.concat(attachments)
    this.$valueContainer.val(JSON.stringify(this.attachmentList))

    if (this.$itemContainer === false || this.$itemContainer.length == 0) {
        console.log("$itemContainer 不存在！！")
        return;
    }

    for (let attachment of attachments) {
        let $item
        if (itemSupplier) {
            $item = $(itemSupplier(attachment))
        }

        $item = $item ? $item : $("<div class=\"item\">\n" +
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
    if (this.deleteConsumer) {
        if (this.deleteConsumer(attachmentId, deleteBtn) === false) {
            return
        }
    }

    this.attachmentList = this.attachmentList.filter(function (m) {
        return m.id !== attachmentId;
    })

    this.$valueContainer.val(JSON.stringify(this.attachmentList))

    $(deleteBtn).parent(".item").remove()
}

FileUpload.prototype.deleteAllAttachment = function () {
    this.attachmentList = []
    this.$valueContainer.val("[]")
    if (this.$itemContainer) {
        this.$itemContainer.html('')
    }
}

FileUpload.prototype.getAttachments = function () {
    return this.attachmentList
}

window.FileUpload = FileUpload
