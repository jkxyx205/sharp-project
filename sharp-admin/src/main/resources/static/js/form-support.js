function registerValid(formDOM) {
    formDOM.valid = function () {
        let validity = this.checkValidity()
        this.classList.add('was-validated')
        return validity;
    }
}