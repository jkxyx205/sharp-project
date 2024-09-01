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
}
