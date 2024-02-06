(function($) {
    var DialogInputReportPicker = function(element, options) {
        this.element = element
        this.$element = $(element);
        this.options = $.extend({},
            $.fn.dialogInputReportPicker.defaults, options); //合并参数
        this.init();
    };

    DialogInputReportPicker.prototype = {
        constructor: DialogInputReportPicker,
        init: function() {
            this.isSingle = (this.options.selectMode === 'single')

            this.$element.append('<input type="text" class="form-control" style="background-color: #ffffff;" placeholder="'+this.options.placeholder+'" '+(this.options.required === true ? 'required' : '')+'>\n' +
                '                    <input type="hidden" name="'+this.options.name+'" class="form-control">')

            this.input = this.$element.find('input[type=hidden]')
            this.label = this.$element.find('input[type=text]')

            let _this = this

            this.$element.dialogReportPicker({
                ...this.options,
                ok: {
                    label: '确定',
                    success: function (value, dialog) {
                        console.log('success', value)
                        _this._fillValue(value)

                        dialog.$modal.modal('hide')
                    }
                },
                beforeShow: function (reportDialog) {
                  _this.options.beforeShow && _this.options.beforeShow(reportDialog)
                },
                hidden: function () {
                    console.log('hidden')
                }
            })

            if (this.options.value) {
                if (!this.isSingle && this.options.value.length === 0) {
                    return
                }

                $.get('/reports/' + this.options.reportId + "/" + (this.isSingle ? this.options.value : 'more/' + this.options.value.join(",")), (res) => {
                   this._fillValue(res)
                })
            }
        },
        _fillValue: function (value) {
            if (this.isSingle) {
                this.input.val(value.id)
                this.label.val(value)
                this.options.value = value.id
            } else {
                let ids = value.map(row => row.id);
                this.options.value = ids
                this.input.val(ids.join(','))
                this.label.val(value.map(row => this.options.display(row)).join(','))
            }

            this.value = value
        }
    }

    $.dialogInputReportPicker = $.fn.dialogInputReportPicker = function(options) {
        options = options || {}
        var args = arguments;
        var value;

        if (this === $) {
            new DialogInputReportPicker(this, options)
            return
        }

        var chain = this.each(function() {
            data = $(this).data("dialogInputReportPicker");
            if (!data) {
                if (options && typeof options == 'object') { //初始化
                    return $(this).data("dialogInputReportPicker", data = new DialogInputReportPicker(this, options));
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

    $.fn.dialogInputReportPicker.defaults = { //设置默认属性
        title: '请选择',
        selectMode: 'single', //  默认单选； 多选 multiple
        class: 'dialog',
        required: false,
        placeholder: '请选择'
    };
})(jQuery);