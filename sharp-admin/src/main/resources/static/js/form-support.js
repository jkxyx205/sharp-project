/**
 * 初始化表单
 * @param formDOM
 * @param idDOM
 * @param options originalFormData, formCode, p,readonly
 */
function sharpFormInit(formDOM, idDOM, options, reloadTabIds, elseValid) {
    formDOM.originalFormData = options.originalFormData
    controlInit(options.p)
    registerValid(formDOM, elseValid)

    // 设置只读
    $(document).ready(function () {
        if (options.readonly) {
            formDOM.classList.add("readonly")
        }

        if (options.readonly == 'true' || options.readonly == true) {
            $("form.readonly :input").prop("disabled", true);
            $('table .operator').hide()
        } else {
        }
    })

    function controlInit(p) {
        // multipleSelect
        $.fn.multipleSelect.locales['zh-CN'] = {
            formatSelectAll: function () {
                return '[全选]'
            },
            formatAllSelected: function () {
                return '已选择所有记录'
            },
            formatCountSelected: function(count, total) {
                return '已从' + total + '条记录中选择' + count + '条'
            },
            formatNoMatchesFound: function () {
                return '没有找到记录'
            }
        }
        $.extend($.fn.multipleSelect.defaults, $.fn.multipleSelect.locales['zh-CN'])
        let multipleSelect = p.filter(c => c.configurer.cpnType == 'MULTIPLE_SELECT')
        multipleSelect.forEach(c => {
            $('#' + c.name).multipleSelect({
                filter: true,
                selectAll: true,
                single: false,
                placeholder: c.configurer.placeholder
            }).multipleSelect('setSelects', formDOM.originalFormData[c.name])
        })
        //
        let searchSelect = p.filter(c => c.configurer.cpnType == 'SEARCH_SELECT')
        searchSelect.forEach(c => {
            $('#' + c.name).multipleSelect({
                filter: true,
                single: true,
                placeholder: c.configurer.placeholder
            })
        })
        // table
        formDOM.tables = p.filter(c => c.configurer.cpnType == 'TABLE')
        formDOM.tables.forEach(c => {
            $('#' + c.name).editableTable({
                columns: c.configurer.additionalInfo.columns.length
            })
        })

        // datepicker
        let dates = p.filter(c => c.configurer.cpnType == 'DATE')
        dates.forEach(c => {
            $('#'+c.name).datepicker({
                language: "zh-CN",
                autoclose: true,
                clearBtn: true,
                todayBtn: 'linked',
                todayHighlight: true,
                format: 'yyyy-mm-dd'
            })
        })

        // file
        let files = p.filter(c => c.configurer.cpnType == 'FILE')
        files.forEach(c => {
            window[formDOM.getAttribute("name") + '_' + c.name + '_file'] = new FileUpload(c.name + '_file',
                null,
                window[formDOM.getAttribute("name") + '_' + c.name + '_file_uploadConsumer'],
                window[formDOM.getAttribute("name") + '_' + c.name + '_file_deleteConsumer'],
                window[formDOM.getAttribute("name") + '_' + c.name + '_file_itemSupplier']
            )
        })
    }

    formDOM.save = function (successCallback, beforeSave) {
        if (this.tagName === 'FORM' && !formDOM.valid()) {
            return
        }

        let $code = $(formDOM).find("#code");
        if ($code.length !== 0) {
            $code.val($code.val().toUpperCase())
        }

        let formData = $(formDOM).form2json({
            multiValSelector: '[type=checkbox], select[multiple]'
        }) || {};

        formDOM.tables.forEach(c => {
            formData[c.name] = $('#' + c.name).editableTable('getValue')
        })

        beforeSave && beforeSave(formData)
        console.log("formData = ", formData)

        $.ajax({
            url: options.actionUrl.startsWith("/") ? options.actionUrl : "/forms/ajax/" + options.actionUrl,
            type: options.method,
            data: JSON.stringify(formData),
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            success: function(res){
                if (res.success) {
                    console.log("保存成功 res = ", res)
                    if (successCallback) {
                        successCallback(res)
                    } else {
                        toastr.success("保存成功")
                    }

                    if (!idDOM.value) { // 新增刷新页面
                        idDOM.value = options.originalFormData.id = res.data

                        // setTimeout(() => {
                        //     window.location.reload()
                        // }, 1000)
                    }

                    if ($code.length !== 0) {
                        $code.attr("disabled", true)
                    }

                    setTimeout(() => {
                        _reloadTab(reloadTabIds)
                    }, 1000)
                }
            }
        });

        function _reloadTab(reloadTabIds) {
            if (!reloadTabIds) {
                reloadTabIds = [options.formCode] //名字需要约定好
            }

            if (Array.isArray(reloadTabIds)) {
                for(let reloadTabId of reloadTabIds) {
                    reloadTab(reloadTabId)
                }
            } else {
                reloadTab(reloadTabIds)
            }
        }
    }
}

function registerValid(formDOM, elseValid) {
    formDOM.valid = function () {
        let formValidity = this.checkValidity()
        let elseValidity = elseValid && elseValid() || !elseValid
        this.classList.add('was-validated')

        return formValidity && elseValidity;
    }

    /**
     *
     * @param isValid 必须是布尔值
     * @param checkContainerDOM 错误信息的标红的 DOM 元素
     * @returns {*}
     */
    formDOM.validElse = function (isValid, checkContainerDOM) {
        if (isValid) {
            checkContainerDOM.classList.remove("is-invalid")
        } else {
            checkContainerDOM.classList.add("is-invalid")
        }

        return isValid;
    }

    formDOM.addEventListener('submit', function (event) {
        if (!formDOM.valid()) {
            event.preventDefault()
            event.stopPropagation()
        }
    }, false)
}



