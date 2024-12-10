;var style = document.createElement("style")
style.appendChild(document.createTextNode("@media (min-width: 576px) {.dialog-input .modal-dialog {max-width: 720px!important;} .dialog-input .modal-dialog .modal-body {padding: 0!important;height: 540px;}}"))
var head = document.getElementsByTagName("head")[0]
head.appendChild(style)

;(function($) {
    var DialogInput = function(element, options) {
        this.$element = $(element);
        this.options = $.extend({},
            $.fn.dialogInput.defaults, options); //合并参数
        this.init();
    };

    DialogInput.prototype = {
        constructor: DialogInput,
        tpl: '<div class="dialog-input modal fade" id="{{id}}" tabindex="-1" role="dialog" aria-labelledby="modalCenterTitle" aria-hidden="true">\n' +
            '    <div class="modal-dialog modal-primary modal-dialog-auto" role="document">\n' +
            '        <div class="modal-content">\n' +
            '            <div class="modal-header">\n' +
            '                <h5 class="modal-title" id="{{title}}"></h5>\n' +
            '                <button class="close" type="button" data-dismiss="modal" aria-label="Close">\n' +
            '                    <span aria-hidden="true">×</span>\n' +
            '                </button>\n' +
            '            </div>\n' +
            '            <div class="modal-body">\n' +
            '                <iframe id="{{iframeId}}" name="{{iframeId}}" src="" width="100%" style="border: 0; padding: 0; margin: 0; height: calc(100% - 6px);"></iframe>\n' +
            '            </div>\n' +
            '            <div class="modal-footer">\n' +
            '                <button class="btn btn-primary ok-show dialog-input-ok-btn" id="{{okId}}"><i class="fa fa-cog"></i> 确定</button>\n' +
            '                <button class="btn btn-secondary" type="button" data-dismiss="modal"><i class="fa fa-remove"></i> 关闭</button>\n' +
            '            </div>\n' +
            '        </div>\n' +
            '    </div>\n' +
            '</div>',
        init: function() {
            var _this = this
            _this._bindDom()
        },
        getValue: function () {
            return this.$element.find('input[type=hidden]').val()
        },
        setParams: function (params) {
            this.options.params = params
        },
        _bindDom: function () {
            if (!this.domBind) {
                // bind dialog
                this.modalId = "dialog_id_" + new Date().getTime();
                this.okId = this.modalId + "_okBtn"
                this.iframeId = this.modalId + "_iframeId"
                this.title = this.modalId + "_title"

                var dialogTpl = DialogInput.prototype.tpl
                    .replace('{{id}}', this.modalId)
                    .replace('{{okId}}', this.okId)
                    .replace('{{title}}', this.title)
                    .replace(/{{iframeId}}/g, this.iframeId)
                    // .replaceAll('{{iframeId}}', this.iframeId) // 老版浏览器不支持

                $('body').append(dialogTpl)

                this.$modal = $('#' + this.modalId)
                this.$OkBtn = $('#' + this.okId)

                this.$OkBtn.on('click', () => {
                    let rows = window.frames[this.iframeId].document.getElementById('qid').row
                    // 如果是多选
                    if (this.options.mode === 'multiple') {
                        rows = window.frames[this.iframeId].$listTable.ajaxTable('getCheckedValue')
                    }
                    this._dialogRowDbClick(rows);
                })

                // bind control
                if (this.$element[0].tagName === 'DIV') {
                    this.$element.append('<input type="text" class="form-control" style="background-color: #ffffff;" placeholder="'+this.options.placeholder+'" '+(this.options.required === true ? 'required' : '')+'>\n' +
                        '                    <input type="hidden" name="'+this.options.name+'" class="form-control">')
                }

                this.$element.on('click', () => {
                    if ((this.options.beforeShowDialog && this.options.beforeShowDialog()) || (!this.options.beforeShowDialog)) {
                        this._showReportDialog(this.options.title)
                    }
                })

                this.iframe = document.getElementById(this.iframeId)
                this.iframe.input = this.$element.find('input[type=hidden]')
                this.iframe.label = this.$element.find('input[type=text]')

                this.iframe.label.on('keydown', function (event) {
                    event.preventDefault();
                    return;
                })

                this.domBind = true

                // init value
                if (this.options.value) {
                    let isSingle = this.options.mode === 'single'
                    if (!isSingle && this.options.value.length === 0) {
                        return
                    }

                    $.get('/reports/' + this.options.reportId + "/" + (isSingle ? this.options.value : 'more/' + this.options.value.join(",")), (res) => {
                        this._fillValue(res)
                    })
                }
            }
        },
        _showReportDialog:function (title) {
            $('#' + this.title).text(title)
            let params = $.param(this.options.params)
            this.iframe.src = '/reports/' + this.options.reportId + "?mode=" + this.options.mode + (params ? '&' + params : '')

            this.$modal.modal({
                show: true,
                backdrop: 'static'
            })
        },
        _dialogRowDbClick: function (rows) {
            if (rows && (this.options.mode === 'single' || rows.length > 0)) {

                let maxRowLength = 300
                if (this.options.mode === 'multiple' && rows.length > maxRowLength) {
                    alert('一次性选择不能超过 '+maxRowLength+' 条记录！')
                    return
                }

                this.$modal.modal('hide')
                this._fillValue(rows)
                this.options.selected && this.options.selected(rows)
            } else {
                toastr.error('请先选择一条记录后再点击确定');
            }
        },
        _fillValue: function (rows) {
            if (this.options.mode === 'single') {
                let row = Array.isArray(rows) ? rows[0] : rows
                this.iframe.input.val(row.id)
                this.iframe.label.val(this.options.labelDisplay(row))
            } else {
                this.iframe.input.val(rows.map(r => r.id).join(","))
                this.iframe.label.val(this.options.labelDisplay(rows))
            }
        }
    }

    $.fn.dialogInput = function(options) {
        options = options || {}
        var args = arguments;
        var value;
        var chain = this.each(function() {
            data = $(this).data("dialogInput");
            if (!data) {
                if (options && typeof options == 'object') { //初始化
                    return $(this).data("dialogInput", data = new DialogInput(this, options));
                }
            } else {
                if (typeof options == 'string') {
                    if (data[options] instanceof Function) { //调用方法
                        var property = options; [].shift.apply(args);
                        value = data[property].apply(data, args);
                    } else { //获取属性
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

    };

    //设置默认属性
    $.fn.dialogInput.defaults = {
        placeholder: '请选择',
        required: false,
        mode: 'single' //  默认单选； 多选 multiple
        // beforeShowDialog
    };

})(jQuery);

function dialogRowDbClick(row) {
    // $('.dialogInput').dialogInput('_dialogRowDbClick', row)
    $('.modal.show .btn.dialog-input-ok-btn').eq(-1).click()
}