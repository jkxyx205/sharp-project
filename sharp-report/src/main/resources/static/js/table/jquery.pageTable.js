(function($) {
    var PageTable = function(element, options) {
        this.$element = $(element);
        this.options = $.extend({},
            $.fn.pageTable.defaults, options); //合并参数

        this.init();
    };

    PageTable.prototype = {
        constructor: PageTable,
        init: function() {
            var _this = this

            var $multipleSelect = this.$element.find("select[multiple]")
            var $search = this.$element.find('button[name=search]')

            $search.on('click', function () {
                var param = _this.$element.form2json({allowEmptyMultiVal:true});
                param.page = 1
                search(param)
            })

            this.$element.find('button[name=reset]').on('click', function () {
                _this.$element.resetForm()
                _this.$element.clearForm(true)
                //reset multi select
                $multipleSelect.each(function() {
                    $(this).find("option").each(function() {
                        $(this).prop('selected', false);
                    });
                });
                $search.click()
            })

            this.$element.find('select').on('change', function () {
                $search.click()
            })

            this.$element.on('keyup', function (e) {
                var theEvent = window.event || e;
                var code = theEvent.keyCode || theEvent.which || theEvent.charCode;

                if (code === 13) {
                    $search.click()
                    e.preventDefault();
                    e.stopPropagation();
                }
            })
        },
    }

    $.fn.pageTable = function(options) {
        options = options || {}
        var args = arguments;
        var value;
        var chain = this.each(function() {
            data = $(this).data("pageTable");
            if (!data) {
                if (options && typeof options == 'object') { //初始化
                    return $(this).data("pageTable", data = new PageTable(this, options));
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

    $.fn.pageTable.defaults = {};
})(jQuery);
