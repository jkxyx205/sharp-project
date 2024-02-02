(function($) {
    var Table = function(element, options) {
        this.$element = $(element);
        this.options = $.extend({},
            $.fn.table.defaults, options); //合并参数
        this.info = {}
        this.init();
    };

    Table.prototype = {
        constructor: Table,
        init: function() {
            if (this.options.scrollPanel) {
                this.$scrollPanel = $(this.options.scrollPanel)
            } else {
                this.$scrollPanel = this.$element.find('.table-fixed-container')
            }

            this.$thead =  $(this.$element).find('thead:eq(0)')
            this.$tbody = $(this.$element).find('tbody:eq(0)')

            this.info.columnSize = this.$thead.find('th, td').length

            this.options.fixedHead && this.initTableFixed()
            this.options.sortable && this.initSortable()
            this.options.summary && this.summary()
            this.options.search && this.initSearch()

            this.options.type === 'client' && this.initEmptyText()

        },
        initEmptyText: function() {
            // language=HTML
            // var emptyHTML = '<tbody class="tbody-empty hidden"><tr>\n    ' +
            //     '<td colspan="'+this.info.columnSize+'" style="text-align: center;"><span class="empty-text">暂无数据</span></td>\n' +
            //     '</tr></tbody>';
            //
            // this.$emptyText = $(emptyHTML)
            // this.$tbody.after(this.$emptyText)

            var emptyHTML = '<tr class="tr-empty non-data">\n    ' +
                '<td colspan="'+this.info.columnSize+'" style="text-align: center;"><span class="empty-text">暂无数据</span></td>\n' +
                '</tr>';

            this.$emptyText = $(emptyHTML)
            this.$tbody.append(this.$emptyText)


            this.checkEmpty()
        },
        checkEmpty: function() {
            if (this.hasData()) {
                this.$emptyText.addClass('hidden')
            } else {
                this.$emptyText.removeClass('hidden')
            }
        },
        initSearch: function() {
            var _this = this
            // this.$search = this.$element.find(this.options.search)
            this.$search = $(this.options.search)

            this.$search.on('keyup', _.debounce(function () { // loadsh support
                var value = this.value
                _this.$tbody.find('tr').removeClass('hidden').not(':contains('+value+')').addClass('hidden')
                _this.checkEmpty()
                _this.refresh()
            }, 200))
        },
        initIndex: function() { // 重置"序号"顺序
            var _this = this
            var sortId = 1
            this.$tbody.find('tr:not(.non-data)').each(function() {
                if (_this.numIdx > -1) {
                    if ($(this).css('display') !== 'none') {
                        $(this).find('td, th').eq(_this.numIdx).html(sortId++)
                    }
                }
            })
        },
        initTableFixed: function () {
            var $tableHead = this.$element.find('.table-thead')
            var $tableBody = this.$element.find('.table-tbody')
            this.tableOffsetTop = $tableBody.offset().top - this.$scrollPanel.offset().top

            var _this  = this
            $tableBody.prepend($tableHead.html())

            this.$scrollPanel.on('scroll', function() {
                var scrollTop = $(this).scrollTop()
                // $tableHead.css('top', scrollTop)
                $tableHead.css('top', scrollTop - _this.tableOffsetTop)
            })
        },
        /**
         brand: ""
         keyword: ""
         page: "1"
         sidx: "STOCK_COUNT"
         size: "15"
         sord: "desc"
         */
        initSort: function() {
            var urlObject = getUrlObject()
            this.initSortByParams(urlObject)
        },
        /**
         * {sidx: "STOCK_COUNT"}
         */
        initSortByParams: function(params) {
            if (!$.isEmptyObject(params)) {
                var $headerCell = this.$thead.find('th.sortable[data-name='+params.sidx+'], td.sortable[data-name='+params.sidx+']')

                if ($headerCell.length > 0) {
                    $headerCell.removeClass('asc desc').addClass('active ' + params.sord).siblings().removeClass('active')
                    $headerCell[0].sord = params.sord
                }
            }
        },
        initSortable: function () {
            this.initSort()

            var _this = this

            this.numIdx = this.$thead.find('td[data-index], th[data-index]').index()

            this.$thead.find('td.sortable, th.sortable').on('click', function () {
                var isActive = $(this).hasClass('active')
                var curSord = this.sord = $(this).hasClass('asc') ? 'asc' : 'desc'

                if (isActive) { // 取反
                    this.sord = (curSord === 'asc' ? 'desc' : 'asc')
                    $(this).addClass(this.sord).removeClass(curSord) // only work for client
                }

                $(this).addClass('active').siblings().removeClass('active') // only work for client also see ajaxTable updated on line 80

                if (_this.options.type === 'client') {
                    var index = $(this).index()
                    var trHtml = []
                    var sortType = $(this).data('sort-type')

                    if (sortType) {
                        _this.sortType = sortType
                    } else {
                        _this.sortType = 'string'
                    }

                    _this.$tbody.find('tr:not(.non-data)').each(function () {
                        var sortValue = $(this).find('td').eq(index).data('sort-value')
                        var html = $(this).prop("outerHTML")
                        trHtml.push({sortValue: sortValue, html: html})
                    })

                    var newTrHtml = trHtml.sort(sortFn.bind(this))

                    _this.$tbody.html(newTrHtml.map(function (o, index) {
                        return o.html
                    }).join(''))

                    _this.initEmptyText()
                    _this.refresh()
                }

                _this.options.clicked && _this.options.clicked(_this, this)

            }).css('cursor', 'pointer')

            function sortFn(a, b) {
                var result = 0

                if (_this.sortType === 'number') {
                    result = a.sortValue - b.sortValue
                } else {
                    result = new String(a.sortValue).localeCompare(new String(b.sortValue))
                }
                return (this.sord === 'asc') ? result : -result
            }
        },
        hasData: function() {
            return this.$tbody.find('tr.hidden:not(.non-data)').length !== this.$tbody.find('tr:not(.non-data)').length
        },
        summary: function () {
            this.$element.find('tr.tr-summary').remove()

            if (!this.options.summary || !this.hasData())
                return

            var summaryData = [];
            var valueTpl = []
            var _this = this
            for (var i = 0; i < this.info.columnSize; i++) {
                summaryData[i] = 0
                valueTpl[i] = ''
            }

            this.$tbody.find('tr:not(.hidden):not(.non-data)').each(function() {
                $(this).find('th, td').each(function (index) {
                    if (isNaN(summaryData[index]))
                        return

                    if (_this.options.ignoreSummaryIndex.indexOf(index) > -1) { // 排除合计
                        summaryData[index] = NaN
                    }

                    var value = $(this).data('sort-value')
                    value = typeof value === "undefined" ? $(this).text().trim() : value
                    if (typeof value === 'string' && value.length === 0)
                        return

                    if (_this.validator.isNumber(value)) {
                        valueTpl[index] = value.toString() // 模版数据
                        value = parseFloat(value)
                    } else {
                        summaryData[index] = NaN
                        return
                    }
                    summaryData[index] = math.add(math.bignumber(value), math.bignumber(summaryData[index]))
                })
            })

            summaryData[0] = this.options.summaryText

            for (var i = 1; i < this.info.columnSize; i++) {
                var value = summaryData[i]
                if (isNaN(value))
                    value = ''
                else {
                    value = formatInteger(value)
                }
                summaryData[i] = valueTpl[i].replace(/[0-9]+.?[0-9]*/, value)
            }

            this.$tbody.append('<tr class="tr-summary non-data" style="border-top: 0;"><td class="text-right bold">' + summaryData.join('</span></td><td class="text-right bold"><span>') + '</td></tr>')
        },
        refresh: function() {
            this.initIndex()
            this.summary()
        },
        validator: {
            isNumber: function (str) {
                return /^[-]?[0-9]+.?[0-9]*(%?)$/.test(str)
            }
        }
    }

    $.fn.table = function(options) {
        options = options || {}
        var args = arguments;
        var value;
        var chain = this.each(function() {
            data = $(this).data("table");
            if (!data) {
                if (options && typeof options == 'object') { //初始化
                    return $(this).data("table", data = new Table(this, options));
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

    $.fn.table.defaults = {
        type: 'client', // 数据加载模式，客户端／服务端，排序使用
        fixedHead: false, // 是否固定表头
        sortable: true, // 列排序
        summary: false,
        ignoreSummaryIndex: [], // 第一列下标index从0开始
        summaryText: '合计'
    };

})(jQuery);