// https://segmentfault.com/a/1190000015975240
;(function () {
    let customizeType = {
        'material': {
            mounted: function (columnConfig) {
                this.columnConfig = columnConfig

                if (!columnConfig.mode) {
                    columnConfig.mode = 'single'
                }
                
                let _this = this

                this.$dialogInput = $('#dialogInput').dialogInput({
                    title: '选择物料',
                    reportId: _this.columnConfig.reportId || '697147523487240192',
                    params: _this.columnConfig.params || undefined,
                    labelDisplay: function (row) {
                        return row.name + ' ' + this.specification(row)
                    },
                    specification: function (row) {
                        return !row.specification ? '' : row.specification;
                    },
                    selected: function (row) {
                        if ("createEvent" in document) {
                            var evt = document.createEvent("HTMLEvents");
                            evt.initEvent("input", false, true);
                            currentMaterialDom.dispatchEvent(evt);
                        }
                        else
                            currentMaterialDom.fireEvent("input");

                        setCaretPosition(currentMaterialDom, currentMaterialDom.value.length)

                        let $tr = $(currentMaterialDom).parent().parent()

                        $editableTable.editableTablePlus('setValue', materialDataMap(row, this), $tr)
                        _this.columnConfig.selected && _this.columnConfig.selected($tr, row)
                    }
                })

                this.$multipleDialogInput = $('#multipleDialogInput').dialogInput({
                    title: '选择物料',
                    reportId: _this.columnConfig.reportId || '697147523487240192',
                    params: _this.columnConfig.params || undefined,
                    mode: 'multiple', // 多选
                    labelDisplay: function (row) {
                        return row.name + ' ' + this.specification(row)
                    },
                    specification: function (row) {
                        return !row.specification ? '' : row.specification;
                    },
                    selected: function (rows) {
                        $editableTable.editableTablePlus('appendValue', rows.map(row => materialDataMap(row, this)))

                        _this.columnConfig.selected && _this.columnConfig.selected($(currentMaterialDom).parent().parent(), rows)
                    }
                })

                function materialDataMap(row, context) {
                    return {
                        "materialId": row.materialId ? row.materialId : row.id,
                        "materialCode": row.code,
                        "materialText": context.labelDisplay(row),
                        "materialName": row.name,
                        "materialType": row.material_type,
                        "materialSpecification": context.specification(row),
                        "unit": row.base_unit,
                        "unitPrice": row.unitPrice,
                        "unitText": row.base_unit_name,
                        "materialCategoryId": row.category_id,
                        "remark": row.remark,
                        "color": row.color,
                    }
                }

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
            },
            formatTr: function ($td, columnConfig) {
                $td.find('input')
                    .attr('name', 'materialCode')
                    .attr('readonly', false)

                $td.append('<input class="form-control" type="hidden" name="'+columnConfig.name+'">')

                // 注册事件
                let _this = this
                $td.find('input[type=text]').on('click keydown', function(event) {
                    // if ((event.keyCode && event.keyCode === 13) || !event.keyCode) {
                    if ((event.keyCode && event.keyCode !== 9) || !event.keyCode) {
                        if (event.keyCode === 8) {
                            // 删除 TODO
                            // let $tr = $(currentMaterialDom).parent().parent()
                            // $editableTable.editableTablePlus('setValue', _this.materialDataMap({}, _this), $tr)

                            event.preventDefault();
                            return;
                        }

                        currentMaterialDom = this
                        let context = {
                            params: {}
                        }
                        if (!_this.columnConfig.beforeShow || _this.columnConfig.beforeShow($(currentMaterialDom).parent().parent(), context)) {
                            // 获取客户端参数查询
                            if (columnConfig.mode === 'single' || $(this).parents("tr").next().length) {
                                // 设置参数
                                if (!$.isEmptyObject(context.params)) {
                                    _this.$dialogInput.dialogInput('setParams', context.params)
                                }
                                _this.$dialogInput.dialogInput().click()
                            } else {
                                // 设置参数
                                if (!$.isEmptyObject(context.params)) {
                                    _this.$multipleDialogInput.dialogInput('setParams', context.params)
                                }
                                _this.$multipleDialogInput.dialogInput().click()
                            }
                        }
                    }

                    if (event.keyCode !== 9) {
                        event.preventDefault();
                        return;
                    }
                })
            }
        },
        'characteristic': {
            mounted: function () {
                $(document).on('click', function (e) {
                    let $td = $(e.target).parents('td.characteristic-td')
                    if ($td.length === 0) {
                        characteristicPopupHide($('.characteristic-popup'))
                        // $('.characteristic-popup').hide()
                    } else {
                        characteristicPopupHide($td.parent().siblings().find('.characteristic-popup'))
                    }
                })

                function characteristicPopupHide($characteristicPopup) {
                    $characteristicPopup.each(function () {
                        if ($(this).css('display') === 'block') {
                            let value = []
                            $(this).find(":input").each(function () {
                                value.push($(this).val())
                            })

                            $(this).prev().val(value.join(' ').trim())

                            $(this).hide()
                        }
                    })
                }

                // $('.characteristic-popup').delegate('select', 'change', function () {
                //     $(this).parents('td.characteristic-td').find('input[name=characteristic]').val('')
                // })
            },
            formatTr: function ($td, columnConfig) {
                $td.append("<div class='characteristic-popup' style=\'position: fixed; z-index: 999; padding: 12px; background: #ffffff; /*border: 1px solid #c8ced3;*/ box-shadow: 0 2px 12px 0 rgba(0,0,0,0.12); display: none;\'>\n" +
                    "    <div class=\'items\'>\n " +
                    "    </div>\n    " +
                    // "<div style=\'text-align: right\'>\n<button type=\'button\' class=\'btn btn-primary btn-sm\'>确定</button>\n    </div>\n" +
                    "</div>").addClass('characteristic-td')

                $td.find('input').on('click', function (e) {
                    $(this).next().css('display', 'block').css('top', ($(e.target).offset().top + 32) + 'px')
                })

                $td.find('input[type=text]').on('click keydown', function(event) {
                    if ((event.keyCode && event.keyCode !== 9) || !event.keyCode) {
                        currentMaterialDom = this
                    }

                    if (event.keyCode !== 9) {
                        event.preventDefault();
                        return;
                    }
                })

                // $td.find('button').on('click', function () {
                //     $(this).parent().parent().css('display', 'none')
                //
                //     let value = []
                //     $(this).parent().parent().find(":input").each(function () {
                //         value.push($(this).val())
                //     })
                //
                //     $(this).parent().parent().prev().val(value.join(' ').trim())
                // })
            }
        }
    }


    window.customizeType = customizeType

    window.showMaterialDialog = function (id) {
        $.dialog({
            title: '物料详情',
            content: '/forms/page/695978675677433856/' + id + '?readonly=true',
            class: 'modal-dialog-auto',
            lazy: true,
            showFooter: true
        })
    }

    /**
     * 特征值
     * @type {{selectedCharacteristic: Window.characteristic.selectedCharacteristic}}
     */
    window.characteristic = {
        selectedCharacteristic: function ($tr, value) {
            if (Array.isArray(value)) {
                // 获取特征值
                $.get("/materials/classifications?materialIds=" + value.map(row => row.id).join(','), (res) => {
                    for (let row of value) {
                        if (res[row.id] && res[row.id].length > 0) {
                            this._render($tr.find('input[name=characteristic]'), res[row.id])
                        }

                        $tr = $tr.next()
                    }
                })
            } else {
                $.get(`/materials/${value.id}/classifications`, (res) => {
                    this._render($tr.find('input[name=characteristic]'), res)
                })
            }
        },
        setInputCharacteristic: function ($input, classificationList) {
            let characteristic = []
            let characteristicValueList = []
            this._render($input, classificationList.map(classification => classification.classification), (characteristicValue) => {
                characteristic.push(characteristicValue.value)
                characteristicValueList.push(characteristicValue)
            })

            for (let characteristicValue of characteristicValueList) {
                $input.next().find(':input[name='+characteristicValue.code+']').val(characteristicValue.value)
            }

            if (classificationList.length > 0) {
                $input.val(characteristic.join(' '))
            }

        },
        setCharacteristic: function ($tr, value) {
            let characteristic = []

            this._render($tr.find('input[name=characteristic]'), value.classificationList.map(classification => classification.classification), (characteristicValue) => {
                value[characteristicValue.code] = characteristicValue.value
                characteristic.push(characteristicValue.value)
            })
            if (value.classificationList.length > 0) {
                value.characteristic = characteristic.join(' ')
            }

            $editableTable.editableTablePlus('setValue', value, $tr)
        },
        loadCharacteristic: function ($editableTable, itemList) {
            // 获取特征值
            let $cursorTr = $editableTable.find("tbody tr:eq(0)")
            for (let row of itemList) {
                let characteristic = []
                this._render($cursorTr.find('input[name=characteristic]'), row.classificationList.map(classification => classification.classification), (characteristicValue) => {
                    row[characteristicValue.code] = characteristicValue.value
                    characteristic.push(characteristicValue.value)
                })
                if (row.classificationList.length > 0) {
                    row.characteristic = characteristic.join(' ')
                }

                $editableTable.editableTablePlus('appendValue', [row])
                $cursorTr = $cursorTr.next()
            }
        },
        getCharacteristicParams: function ($editableTable, itemList) {
            let index = 0
            for (let item of itemList) {
                // 获取特征值
                item.classificationList = []

                $editableTable.find("tbody tr:eq("+(index++)+") td.characteristic-td .items > div").each(function () {
                    let characteristicValueList = []

                    $(this).find('> div :input').map((index, input) => {
                        characteristicValueList.push({
                            characteristicCode: input.name,
                            val: input.value
                        })
                    })

                    item.classificationList.push({
                        materialId: item.materialId,
                        classificationCode: $(this).data('classification_code'),
                        characteristicValueList
                    })
                })
            }
        },
        _render: function($input, classificationList, characteristicValueConsumer) {
            if (classificationList.length > 0) {
                $input.attr('disabled', false).attr('readonly', false)

                let html = []
                for (let classification of classificationList) {
                    for (let characteristicValue of classification.characteristicValue) {
                        let item
                        if (characteristicValue.options && characteristicValue.options.length > 0) {
                           item = "<div class='mb-2'>\n" +
                                "            <label style='width: 60px; text-align: left' class='"+(characteristicValue.required ? 'required = \"required\"' : '')+"' for=\"" + characteristicValue.code + "\">" + characteristicValue.description + "</label>\n" +
                                "            <select class='form-control' style='display: inline-block; width: auto;border-width: 1px;' id=\"" + characteristicValue.code + "\" name=\"" + characteristicValue.code + "\" " + (characteristicValue.required ? 'required = "required"' : '') + ">\n" +
                                "<option value=''></option>" +
                                "" + characteristicValue.options.map(d => {
                                    return '<option value="' + d.name + '">' + d.label + '</option>'
                                }).join('') + "" +
                                "            </select>\n" +
                                "        </div>\n"
                        } else {
                            item = "<div class='mb-2'>\n" +
                                "            <label style='width: 60px; text-align: left' class='"+(characteristicValue.required ? 'required = "required"' : '')+"' for=\"" + characteristicValue.code + "\">" + characteristicValue.description + "</label>\n" +
                                "            <input class='form-control' style='display: inline-block; width: auto;border-width: 1px;' id=\"" + characteristicValue.code + "\" name=\"" + characteristicValue.code + "\" " + (characteristicValue.required ? 'required = "required"' : '') + " placeholder='"+characteristicValue.placeholder+"'" +
                                " "+(characteristicValue.type === 'NUMBER' ? 'pattern="-?\\d+(\\.\\d+)?"' : '')+">\n" +
                                "        </div>\n"
                        }

                        html.push(item)
                        characteristicValueConsumer && characteristicValueConsumer(characteristicValue)
                    }

                    $input.next().find('.items').html('<div data-classification_code="' + classification.code + '">' + html.join('') + '</div>')
                }
            } else {
                $input.attr('disabled', true).attr('readonly', true)
                $input.next().find('.items').html('')
            }
        }

    }
})()



