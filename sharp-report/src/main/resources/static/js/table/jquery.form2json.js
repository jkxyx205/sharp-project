(function($) {

    var defaults = {
            inputSelectors: 'input:not([type=radio], [type=checkbox], [type=reset]), input[type=checkbox], input[type=radio]:checked, textarea, select',
            multiValSelector: '[type=checkbox], select',
            keyAttr: 'name',
            wrapped: false,
            allowEmptyMultiVal: true,
            allowEmptySingleVal: true,
            keyTransform: null
        },
        settings = {},
        // borrow isEmpty from underscore to prevent a hard dependency
        // http://documentcloud.github.com/underscore
        empty = function(obj) {
            //if (_.isArray(obj) || _.isString(obj)) return obj.length === 0;
            if (obj && obj.length) return obj.length === 0;
            for (var key in obj) if (hasOwnProperty.call(obj, key)) return false;
            return true;
        };

    $.fn.form2json = function(options) {
        var form = $(this);

        if (!form.is('form')) {
            return;
        }

        settings = $.extend(true, {}, defaults, options);

        var data = {},
            fields = form.find(settings.inputSelectors),
            singleVal = fields.filter(':not(' + settings.multiValSelector + ')'),
            multiVal = fields.filter(settings.multiValSelector);

        singleVal.each(function() {
            var item = $(this),
                key = item.attr(settings.keyAttr) || item.attr('name') || item.attr('id');
            var val = item.val();
            if (item.attr('type') != 'password') {
                val = item.val().trim();
            }

            if (!settings.allowEmptySingleVal && empty(val)) return true;
            if (key) {
                var keyHierarchy = key.split('.'),
                    dKeys = $.map(keyHierarchy, function(k) {
                        return $.isFunction(settings.keyTransform) ? settings.keyTransform(k, item) : k;
                    }),
                    obj = data,
                    prop;

                while (prop = dKeys.shift()) {
                    obj[prop] = obj[prop] || {};
                    if (dKeys.length) {
                        obj = obj[prop];
                    } else {
                        break;
                    }
                }

                obj[prop] = val;
            }
        });

        multiVal.each(function() {
            var item = $(this),
                key = item.attr(settings.keyAttr) || item.attr('name') || item.attr('id'),
                val = item.is(':checkbox:not(:checked)') ? null : item.val();

            if (key && (val || settings.allowEmptyMultiVal)) {
                var keyHierarchy = key.split('.'),
                    dKeys = $.map(keyHierarchy, function(k) {
                        return $.isFunction(settings.keyTransform) ? settings.keyTransform(k, item) : k;
                    }),
                    obj = data,
                    prop;

                while (prop = dKeys.shift()) {
                    obj[prop] = obj[prop] || (dKeys.length ? {} : null);
                    if (dKeys.length) {
                        obj = obj[prop];
                    } else {
                        break;
                    }
                }

                if (obj[prop]) {
                    if (val) {
                        //already exists, but needs to be turned into an array
                        $.isArray(obj[prop]) || (obj[prop] = [ obj[prop] ]);

                        obj[prop].push(val);
                    }
                } else {
                    //data doesn't have the item yet, create it
                    obj[prop] = val;
                }
            }
        });

        if (!settings.wrapped) {
            return data;
        }

        var ajax = { data: data },
            method = form.attr('method'),
            action = form.attr('action');

        (method && method.toUpperCase() != 'GET') && (ajax.type = method);
        (action) && (ajax.url = action);

        return ajax;
    };

})(jQuery);