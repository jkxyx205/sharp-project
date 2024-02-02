$.validator.addMethod("notBlank", function(value) {
    return (value && value.trim().length > 0);
}, "这是必填字段");