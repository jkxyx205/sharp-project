(function($) {
    var Dialog = function(element, options) {
        this.isGlobal = (element === $) // add global dialog 20200812

        this.$element = this.isGlobal ? $ : $(element);
        this.options = $.extend({},
            $.fn.dialog.defaults, options); //合并参数

        this.init();
    };

    Dialog.prototype = {
        constructor: Dialog,
        domBind: false,
        fetched: false,
        tpl: '<div class="modal fade" id="{{id}}" tabindex="-1" role="dialog" aria-labelledby="modalCenterTitle" aria-hidden="true">\n' +
            '    <div class="modal-dialog modal-primary {{class}}" role="document">\n' +
            '        <div class="modal-content">\n' +
            '            <div class="modal-header">\n' +
            '                <h5 class="modal-title">{{title}}</h5>\n' +
            '                <button class="close" type="button" data-dismiss="modal" aria-label="Close">\n' +
            '                    <span aria-hidden="true">×</span>\n' +
            '                </button>\n' +
            '            </div>\n' +
            '            <div class="modal-body">\n' +
            '            </div>\n' +
            '            <div class="modal-footer">\n' +
            '                <button class="btn btn-primary ok-show" id="{{okId}}"><i class="fa fa-cog"></i> {{ok.label}}</button>\n' +
            '                <button class="btn btn-secondary" type="button" data-dismiss="modal"><i class="fa fa-remove"></i> 关闭</button>\n' +
            '            </div>\n' +
            '        </div>\n' +
            '    </div>\n' +
            '</div>',
        init: function() {
            var _this = this
            _this._bindDom()
            if (this.isGlobal) {
                this._fetchData()
            } else {
                this.$element.on('click',  () => {
                    this._fetchData()
                })
            }

            _this.$modal.on('shown.bs.modal', function () {
                (!_this.fetched || !_this.options.lazy) && _this.options.onload && (typeof window[_this.options.onload] === 'function') && window[_this.options.onload]()
                _this.options.shown && _this.options.shown()
                _this.fetched = true
            })

            _this.$modal.on('hidden.bs.modal', function () {
                _this.options.hidden && _this.options.hidden()
                _this.isGlobal && _this.$modal.remove()

                if (_this.options.refreshTime) {
                    clearInterval(_this.intervalId);
                }
            })
        },
        _fetchData: function () {
            if (!this.options.lazy || (this.options.lazy && !this.fetched)) {
                $.get((!this.isGlobal && this.$element.data('url')) || this.options.content, res => {
                    this.$modal.find('.modal-body').html(res)
                    this.$modal.modal({
                        show: true,
                        backdrop: this.options.backdrop
                    })

                    if (this.options.refreshTime) {
                        this.intervalId = setInterval(() => {
                            $.get((!this.isGlobal && this.$element.data('url')) || this.options.content, res => {
                                this.$modal.find('.modal-body').html(res)
                                let stillRefresh = (this.options.afterRefresh && this.options.afterRefresh())
                                if (!stillRefresh) {
                                    clearInterval(this.intervalId);
                                }
                            })
                        }, this.options.refreshTime)
                    }
                })
            } else {
                this.$modal.modal('show');
            }
        },
        _bindDom: function () {
            if (!this.domBind) {
                this.modalId = "dialog_id_" + new Date().getTime();

                var dialogTpl = Dialog.prototype.tpl.replace('{{title}}', this.options.title)
                    .replace('{{id}}', this.modalId)
                    .replace('{{class}}', this.options.class)

                if (this.options.ok) {
                    this.okId = this.modalId + "_okBtn"
                    dialogTpl = dialogTpl.replace('{{ok.label}}', this.options.ok.label)
                    dialogTpl = dialogTpl.replace('{{okId}}', this.okId)
                }

                $('body').append(dialogTpl)

                this.$modal = $('#' + this.modalId)

                if (this.options.showFooter) {
                    if (this.options.ok) {
                        this.$OkBtn = $('#' + this.okId)
                        var _this = this

                        this.$OkBtn.on('click', function () {
                            _this.options.ok.success && _this.options.ok.success(_this.$modal)
                        })
                    } else {
                        this.$modal.find('.ok-show').remove()
                    }
                } else {
                    this.$modal.find('.modal-footer').remove()
                }

                if (!this.options.showHeader) {
                    this.$modal.find('.modal-header').remove()
                }

                this.domBind = true
            }
        }
    }

    $.dialog = $.fn.dialog = function(options) {
        options = options || {}
        var args = arguments;
        var value;

        if (this === $) {
            new Dialog(this, options)
            return
        }

        var chain = this.each(function() {
            data = $(this).data("dialog");
            if (!data) {
                if (options && typeof options == 'object') { //初始化
                    return $(this).data("dialog", data = new Dialog(this, options));
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

    $.fn.dialog.defaults = {
        title: '提示信息',
        class:"dialog",
        lazy: true,
        onload: 'onLoad',
        backdrop: 'static',
        showHeader: true,
        showFooter: true,
        refreshTime: 0
    };
})(jQuery);