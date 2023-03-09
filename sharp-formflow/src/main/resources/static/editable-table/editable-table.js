;var style = document.createElement("style")
style.appendChild(document.createTextNode("tr:last-child .operator {display: none; }  .operator {cursor: pointer;}"))
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

            _bindEvent(this.options.columns, this.$tbody)
            _addEmptyLine(this.options.columns, this.$tbody)
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
    }

    function _renderTable($tbody, list) {
        var tbodyHTML = []

        for (arr of list) {
            tbodyHTML.push('<tr>')

            for(v of arr) {
                tbodyHTML.push('<td><input class="form-control" value="'+v+'"/></td>')
            }

            tbodyHTML.push('<td class="operator">x</td></tr>')
        }
        $tbody.append(tbodyHTML.join(''))
    }

    function input_focus(e, columns, $tbody) {
        let isLastChild = ($(e.target).parent().parent().next().length == 0)
        if (isLastChild) {
            _addEmptyLine(columns, $tbody)
        }
    }

    function _addEmptyLine(columns, $tbody) {
        var lineHTML = []
        lineHTML.push('<tr>')
        for(var i = 0; i < columns; i++) {
            lineHTML.push('<td><input class="form-control" value=""/></td>')
        }
        lineHTML.push('<td class="operator">x</td></tr>')

        $tbody.append(lineHTML.join(''))
        _bindEvent(columns, $tbody)
    }

    function _bindEvent(columns, $tbody) {
        $tbody.find('input').each(function() {
            this.addEventListener("input", function(e) {
                input_focus(e, columns, $tbody)
            });
        })

        $tbody.find('.operator').each(function() {
            this.addEventListener("click", function() {
                $(this).parent().remove()
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

    };
})(jQuery);