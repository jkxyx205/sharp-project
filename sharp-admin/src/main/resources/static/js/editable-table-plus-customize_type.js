// https://segmentfault.com/a/1190000015975240
;(function () {
    let customizeType = {
        'user': {
            formatTr: function ($td, columnConfig, editableTable) {
                columnConfig = $.extend({}, columnConfig, {
                                    type: "dialog",
                                    name: "userId",
                                    label: 'userName',
                                    reportId: '786015805669142528',
                                    success: function (value, $tr) {
                                        if (Array.isArray(value)) {
                                            editableTable.appendValue(value.map(row => this.valueMapping(row)))
                                        } else {
                                            // 设置值 mergeValue or setValue, 根据业务情况选择
                                            editableTable.mergeValue(this.valueMapping(value), $tr)
                                        }
                                    },
                                    valueMapping: function (value) {
                                        return {
                                            "userId": value.id,
                                            "userName": value.name
                                        }
                                    }
                                })

                // 调用 dialog
                customizeType.dialog.formatTr($td, columnConfig)
            }
        },
        'dialog' : {
            mounted: function (columnConfig) {

            },
            formatTr: function ($td, columnConfig) {
                $td.find('input')
                    .attr('name', columnConfig.label)
                    .attr('readonly', false)

                $td.append('<input class="form-control" type="hidden" name="'+columnConfig.name+'">')
                this.columnConfig = columnConfig
                // 注册事件
                let _this = this

                $td.find('input[type=text]').on('click keydown', function(event) {
                    if ((event.keyCode && event.keyCode !== 9) || !event.keyCode) {
                        if (event.keyCode === 8) {
                            event.preventDefault();
                            return;
                        }

                        let $tr = $(this).parent().parent()
                        let inputDom = this
                        let context = {
                            params: {}
                        }

                        if (!_this.columnConfig.beforeShow || _this.columnConfig.beforeShow($tr, context)) {
                            // 设置参数
                            if (!$.isEmptyObject(context.params)) {
                                columnConfig['params'] = context.params
                            }

                            // 获取客户端参数查询
                            $.dialogReportPicker({
                                ...columnConfig,
                                selectMode: $(this).parents("tr").next().length ? 'single' : columnConfig.selectMode,
                                class: 'report-dialog modal-lg',
                                ok: {
                                    label: '确定',
                                    success: function (value, dialog, reportDialog) {
                                        if (reportDialog.options.selectMode === 'single') {
                                            if ("createEvent" in document) {
                                                var evt = document.createEvent("HTMLEvents");
                                                evt.initEvent("input", false, true);
                                                inputDom.dispatchEvent(evt);
                                            }
                                            else
                                                inputDom.fireEvent("input");

                                            setTimeout(() => setCaretPosition(inputDom, inputDom.value.length), 200)
                                        }

                                        console.log('success', value)
                                        _this.columnConfig.success && _this.columnConfig.success(value, $tr)
                                        dialog.$modal.modal('hide')
                                    }
                                },
                                hidden: function () {
                                    console.log('hidden')
                                }
                            })
                        }
                    }

                    if (event.keyCode !== 9) {
                        event.preventDefault();
                        return;
                    }
                })

                function setCaretPosition(ctrl, pos) {
                    // Modern browsers
                    if (ctrl.setSelectionRange) {
                        ctrl.focus();
                        ctrl.setSelectionRange(pos, pos);
                        // IE8 and below
                    } else if (ctrl.createTextRange) {
                        var range = ctrl.createTextRange();
                        range.collapse(true);
                        range.moveEnd('character', pos);
                        range.moveStart('character', pos);
                        range.select();
                    }
                }
            }
        },
    }

    window.customizeType = customizeType
})()



