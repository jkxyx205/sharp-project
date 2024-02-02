(function($) {
    var ExportTable = function(element, options) {
        this.$element = $(element);
        this.options = $.extend({},
            $.fn.exportTable.defaults, options); //合并参数
        this.init();
    };

    ExportTable.prototype = {
        constructor: ExportTable,
        init: function() {
          this.$target = $(this.options.target)
          var _this = this

          this.$element.on('click', function () {
              _this.form("/report/html", {
                  name: _this.options.name,
                  html: _this.$target.html(),
                  columnsWidth: _this.options.columnsWidth
              }, 'post').submit().remove()
          })
        },
        form: function (url, data, method) {
            if (method == null) method = 'POST';
            if (data == null) data = {};

            var form = $('<form>').attr({
                method: method,
                action: url,
                "accept-charset": "utf-8",		// 重要，解决其他浏览器
                onsubmit: "document.charset='utf-8';"	//重要，解决IE提交时编码问题
            }).css({
                display: 'none'
            });

            var addData = function (name, data) {
                if ($.isArray(data)) {
                    for (var i = 0; i < data.length; i++) {
                        var value = data[i];
                        addData(name + '[]', value);
                    }
                } else if (typeof data === 'object') {
                    for (var key in data) {
                        if (data.hasOwnProperty(key)) {
                            addData(name + '[' + key + ']', data[key]);
                        }
                    }
                } else if (data != null) {
                    form.append($('<input>').attr({
                        type: 'hidden',
                        name: String(name),
                        value: String(data)
                    }));
                }
            };

            for (var key in data) {
                if (data.hasOwnProperty(key)) {
                    addData(key, data[key]);
                }
            }

            return form.appendTo('body');
        }
    }

    $.fn.exportTable = function(options) {
        options = options || {}
        var args = arguments;
        var value;
        var chain = this.each(function() {
            data = $(this).data("exportTable");
            if (!data) {
                if (options && typeof options == 'object') { //初始化
                    return $(this).data("exportTable", data = new ExportTable(this, options));
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

    $.fn.exportTable.defaults = {};
})(jQuery);
