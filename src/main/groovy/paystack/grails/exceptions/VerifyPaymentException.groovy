package paystack.grails.exceptions

class VerifyPaymentException extends RuntimeException {

    VerifyPaymentException(String message) {
        super(message)
    }
}
