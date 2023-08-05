;var style = document.createElement("style")
style.appendChild(document.createTextNode("tr:last-child .operator {display: none; }  table .operator {cursor: pointer;vertical-align: middle!important;}"))
var head = document.getElementsByTagName("head")[0]
head.appendChild(style)

;(function($) {

    var EditableTable = function(element, options) {
        this.$element = $(element);
        this.options = $.extend({}, $.fn.editableTable.defaults, options); //合并参数
        this.init();
    };

    EditableTable.prototype = {
        constructor: EditableTable,
        init: function() {
            this.$tbody = this.$element.find('tbody')
            if (this.options.value) {
                _renderTable(this.$tbody, this.options.value)
            }

            this.$tbody.find('.operator').each((e, elem) => {
                elem.addEventListener("click", (e) => {
                    if ((this.options.beforeRemoveCallback && this.options.beforeRemoveCallback($(e.target).parent())) || !this.options.beforeRemoveCallback) {
                        $(e.target).parent().remove()
                        this.options.afterRemoveCallback && this.options.afterRemoveCallback($(e.target).parent())
                    }
                });
            })

            _addEmptyLine(this.options.columns, this.$tbody, this.options.addEmptyLineCallback, this.options.beforeRemoveCallback, this.options.afterRemoveCallback)

            if (this.options.readonly !== undefined) {
                this.readonly(this.options.readonly)
            }
        },
        getValue: function() {
            let list = []
            this.$tbody.find('tr:not(:last-child)').each(function() {
                let value = []
                $(this).find('td:not(:last-child)').each(function() {
                    value.push($(this).find('input').val())
                })
                list.push(value)
            })
            return list
        },
        addEmptyLine: function () {
            _addEmptyLine(this.options.columns, this.$tbody, this.options.addEmptyLineCallback, this.options.beforeRemoveCallback, this.options.afterRemoveCallback)
        },
        readonly:function (readonly) {
            this.options.readonly = (readonly === 'true' || readonly === true)
            this.$tbody.find(':input').prop('disabled', this.options.readonly).prop('readonly', this.options.readonly)

            if (this.$tbody.find('tr').length > 1) {
                if (this.options.readonly) {
                    this.$tbody.find('tr:not(:last-child) .operator, tr:last-child').hide()
                } else {
                    this.$tbody.find('tr:last-child').show()
                    this.$tbody.find('tr:not(:last-child) .operator').css('display', 'table-cell')
                }
            }
        },
    }

    function _renderTable($tbody, list) {
        var tbodyHTML = []

        for (arr of list) {
            tbodyHTML.push('<tr>')

            for(v of arr) {
                tbodyHTML.push('<td><input class="form-control" type="text" value="'+v+'" autocomplete="off"/></td>')
            }

            tbodyHTML.push('<td class="operator btn-link">X</td></tr>')
        }

        $tbody.append(tbodyHTML.join(''))
    }

    function input_focus(e, columns, $tbody, addEmptyLineCallback, beforeRemoveCallback, afterRemoveCallback) {
        let isLastChild = ($(e.target).parent().parent().next().length == 0)
        if (isLastChild) {
            _addEmptyLine(columns, $tbody, addEmptyLineCallback, beforeRemoveCallback, afterRemoveCallback)
        }
    }

    function _addEmptyLine(columns, $tbody, addEmptyLineCallback, beforeRemoveCallback, afterRemoveCallback) {
        var lineHTML = []
        lineHTML.push('<tr>')
        for(var i = 0; i < columns; i++) {
            lineHTML.push('<td><input class="form-control" type="text" value="" autocomplete="off"/></td>')
        }


        lineHTML.push('<td class="operator btn-link">X</td></tr>')

        $tbody.append(lineHTML.join(''))
        addEmptyLineCallback && addEmptyLineCallback($tbody.find('tr:last-child'))
        _bindEvent(columns, $tbody, addEmptyLineCallback, beforeRemoveCallback, afterRemoveCallback)
    }

    function _bindEvent(columns, $tbody, addEmptyLineCallback, beforeRemoveCallback, afterRemoveCallback) {
        $tbody.find('tr:last-child :input').each(function() {
            if (this.tagName === 'INPUT') {
                this.addEventListener("input", function(e) {
                    input_focus(e, columns, $tbody, addEmptyLineCallback, beforeRemoveCallback, afterRemoveCallback)
                });
            } else if (this.tagName === 'SELECT') {
                this.addEventListener("change", function(e) {
                    input_focus(e, columns, $tbody, addEmptyLineCallback, beforeRemoveCallback, afterRemoveCallback)
                });
            }
        })

        $tbody.find('tr:last-child .operator').each((e, elem) => {
            elem.addEventListener("click", (e) => {
                if ((beforeRemoveCallback && beforeRemoveCallback($(e.target).parent())) || !beforeRemoveCallback) {
                    $(e.target).parent().remove()
                    afterRemoveCallback && afterRemoveCallback($(e.target).parent())
                }
            });
        })
    }

    $.fn.editableTable = function(options) {
        options = options || {}
        var args = arguments;
        var value;
        var chain = this.each(function() {
            data = $(this).data("editableTable");
            if (!data) {
                if (options && typeof options == 'object') { //初始化
                    return $(this).data("editableTable", data = new EditableTable(this, options));
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

    $.fn.editableTable.defaults = {
        readonly: undefined
    };
})(jQuery);