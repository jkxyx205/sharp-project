(function($) {
    var DialogReportPicker = function(element, options) {
        this.element = element
        this.$element = $(element);
        this.options = $.extend({},
            $.fn.dialogReportPicker.defaults, options); //合并参数
        this.init();
    };

    DialogReportPicker.prototype = {
        constructor: DialogReportPicker,
        init: function() {
            let params = this.options.params ?  $.param(this.options.params) : undefined
            this.isSingle = (this.options.selectMode === 'single')

            let _this = this
            let $dialogElement = ($ === this.element) ? $ : this.$element

            $dialogElement.dialog({
                title: this.options.title,
                class: this.options.class,
                lazy: false,
                src: '/reports/'+this.options.reportId+'?mode=' + this.options.selectMode  + (params ? '&' + params : '') ,
                ok: {
                    label: this.options.ok.label,
                    success: function (dialog) {
                       _this._checkValueAndReturnSuccess(dialog)
                    }
                },
                hidden: this.options.hidden,
                mounted: function (dialog) {
                    window[dialog.iframeDom.id + 'loaded'] = function (iframe) {
                        if (_this.options.value) {
                            window.frames[dialog.iframeId].contentWindow['init'](_this.options.value)
                        }

                        window[dialog.iframeDom.id + 'dialogRowDbClick'] = function (row) {
                            _this._checkValueAndReturnSuccess(dialog)
                        }
                    }
                },
            })

            // init value
            if (this.options.value) {
                if (!this.isSingle && this.options.value.length === 0) {
                    return
                }

                $.get('/reports/' + this.options.reportId + "/" + (this.isSingle ? this.options.value : 'more/' + this.options.value.join(",")), (res) => {
                    _this.value = res
                    _this.options.render && _this.options.render(res, _this)
                })
            }
        },
        setParams: function (params) {
            this.options.params = params
        },
        _checkValueAndReturnSuccess: function (dialog) {
            let value
            if (this.options.selectMode === 'single') {
                value = window.frames[dialog.iframeId].contentWindow['activeRow']
                this.options.value = value.id
            } else {
                value = window.frames[dialog.iframeId].contentWindow.$listTable.ajaxTable('getCheckedValue')
                const maxRowLength = 300
                if (value.length > maxRowLength) {
                    toastr.error('一次性选择不能超过 '+maxRowLength+' 条记录！')
                    return;
                }

                this.options.value = value.map(row => row.id)
            }

            if (!value || value.length === 0) {
                toastr.error('请选择一条记录')
                return
            }

            this.value = value

            this.options.ok.success && this.options.ok.success(value, dialog)
        }
    }

    $.dialogReportPicker = $.fn.dialogReportPicker = function(options) {
        options = options || {}
        var args = arguments;
        var value;

        if (this === $) {
            new DialogReportPicker(this, options)
            return
        }

        var chain = this.each(function() {
            data = $(this).data("dialogReportPicker");
            if (!data) {
                if (options && typeof options == 'object') { //初始化
                    return $(this).data("dialogReportPicker", data = new DialogReportPicker(this, options));
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

    $.fn.dialogReportPicker.defaults = { //设置默认属性
        title: '请选择',
        selectMode: 'single', //  默认单选； 多选 multiple
        class: 'dialog'
    };
})(jQuery);