package paystack.grails.exceptions

class PaystackValidationExecption extends Exception {
    PaystackValidationExecption(String message) {
        super(message)
    }
}
