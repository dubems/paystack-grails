package com.dubems

import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(PaystackService)
class PaystackServiceSpec extends Specification {

    def paystackService

    def setup() {
        paystackService = Spy(PaystackService) {

        }

    }

    def cleanup() {
    }

    void "Test getAuthUrl() returns the expected"() {
        setup:
        def params = [:]
        def expectedUrl = "jjdkfndjwiefjd"

        when: "response returns an authorization url"
        def actualUrl = paystackService.getAuthUrl(params)

        then: "the actual url equals the expected url"
        1 * paystackService.makePaymentRequest(params) >> [data: [authorization_url: expectedUrl]]
        actualUrl == expectedUrl

        when: "response does not return a url"
        def _actualUrl = paystackService.getAuthUrl(params)

        then: ""
        1 * paystackService.makePaymentRequest(params) >> [:]
        _actualUrl != expectedUrl
        _actualUrl == null

    }

    void "Test validate() works as expected"() {
        when: "all required parameters are present"
        def params = [:]
        params.email = "nriagudubem@gmail.com"
        params.amount = "2000000"

        def response = paystackService.validate(params)

        then: "an instance of paystackService is returned"
        assert response instanceof PaystackService

        when: "required parameters are not complete"
        def _params = [:]
        params.email = null
        params.amount = "300000"

        paystackService.validate(_params)

        then: "An exception is thrown"
        thrown(Exception)

    }


    void "Test makePaymentRequest() performs as expected"() {
        setup:
        def params = [:]

        when: "payment request is made"
        paystackService.makePaymentRequest(params)

        then: "verify various method calls"
        1 * paystackService.getSecretKey() >> "340830okowkeow"
        1 * paystackService.getEndPoint() >> "http://paystack.co"
        1 * paystackService.postRequest(*_) >> [:]

    }

    void "Test verify() works as expected "() {
        setup:
        2 * paystackService.getSecretKey() >> "340830okowkeow"
        2 * paystackService.getEndPoint() >> "http://paystack.co"

        when: "Verification fails"
        String reference = 'wewrieor9343'
        paystackService.verify(reference)

        then: "An Exception is thrown"
        1 * paystackService.getRequest(*_) >> [data: [status: "failed"]]
        thrown(Exception)

        when: "Verification passes"
        def response = paystackService.verify(reference)

        then: "response is returned"
        1 * paystackService.getRequest(*_) >> [data: [status: "success"]]
        response == [data: [status: "success"]]
    }

    void "Test listTransactions() works as expected"() {
        setup:
        1 * paystackService.getSecretKey() >> "340830okowkeow"
        1 * paystackService.getEndPoint() >> "http://paystack.co"

        when: "listTransactions() is called"
        paystackService.listTransactions()

        then: "getRequest() is called once"
        1 * paystackService.getRequest(*_) >> [:]
    }

    void "Test fetchTransaction() works as expected"() {
        setup:
        1 * paystackService.getSecretKey() >> "340830okowkeow"
        1 * paystackService.getEndPoint() >> "http://paystack.co"

        when: "fetchTransaction() is called"
        paystackService.fetchTransaction(232)

        then: ""
        1 * paystackService.getRequest(*_) >> [:]
    }

    void "Test createCustomer() works as expected"() {
        setup:
        2 * paystackService.getSecretKey() >> "340830okowkeow"
        2 * paystackService.getEndPoint() >> "http://paystack.co"

        when: "There is no email parameter"
        def params = [email: ""]
        paystackService.createCustomer(params)

        then: "An exception is thrown"
        thrown(Exception)

        when: "There is email parameter"
        def _params = [email: "nriagudubem@gmail.com"]
        paystackService.createCustomer(_params)

        then: "postRequest() is called"
        1 * paystackService.postRequest(*_) >> [:]

    }

    void "Test getAllCustomers()  works as expected"() {
        setup:
        1 * paystackService.getSecretKey() >> "340830okowkeow"
        1 * paystackService.getEndPoint() >> "http://paystack.co"

        when: "getAllCustomers() is called"
        paystackService.getAllCustomers()

        then: "getRequest() is called"
        1 * paystackService.getRequest(*_) >> [:]
    }

    void "fetchCustomer() works as expected"() {
        setup:
        1 * paystackService.getSecretKey() >> "340830okowkeow"
        1 * paystackService.getEndPoint() >> "http://paystack.co"

        when: "fetchCustomer() is called with a customerId"
        paystackService.fetchCustomer(23)

        then: ""
        1 * paystackService.getRequest(*_) >> [:]
    }

    void "Test getAllPlans() works as expected"() {
        setup:
        1 * paystackService.getSecretKey() >> "340830okowkeow"
        1 * paystackService.getEndPoint() >> "http://paystack.co"

        when: "getAllPlans() is called"
        paystackService.getAllPlans()

        then: "getRequest() is called once"
        1 * paystackService.getRequest(*_) >> [:]

    }

    void "Test getAllTransactions() works as expected"() {
        setup:
        1 * paystackService.getSecretKey() >> "340830okowkeow"
        1 * paystackService.getEndPoint() >> "http://paystack.co"

        when: "getAllTransactions() is called"
        paystackService.getAllTransactions()

        then: "getRequest() is called once"
        1 * paystackService.getRequest(*_) >> [:]
    }

    void "Test createPlan() works as expected"() {
        setup:
        2 * paystackService.getSecretKey() >> "340830okowkeow"
        2 * paystackService.getEndPoint() >> "http://paystack.co"

        paystackService.verifyPlanInterval(_) >> false

        when: "createPlan() is called is incorrect interval"
        Map params = [:]
        paystackService.createPlan(params)

        then: "an exception is thrown"
        thrown(Exception)

        when: "createPlan() is called with correct interval"
        paystackService.verifyPlanInterval(_) >> true
        Map _params = [:]
        paystackService.createPlan(_params)

        then: "postRequest() is called"
        1 * paystackService.postRequest(*_) >> [:]
    }

    void "fetchPlan() works as expected"() {
        setup:
        1 * paystackService.getSecretKey() >> "340830okowkeow"
        1 * paystackService.getEndPoint() >> "http://paystack.co"

        when: "fetchPlan() is called"
        paystackService.fetchPlan(23)

        then: "getRequest() is called once"
        1 * paystackService.getRequest(*_) >> [:]
    }

    void " Test exportTransaction() works as expected"() {
        1 * paystackService.getSecretKey() >> "340830okowkeow"
        1 * paystackService.getEndPoint() >> "http://paystack.co"

        when: "exportTransaction() is called"
        Map params = [:]
        paystackService.exportTransaction(params)

        then: "getRequest() is called once"
        1 * paystackService.getRequest(*_) >> [:]
    }


    void " Test createSubscription() works as expected"() {
        1 * paystackService.getSecretKey() >> "340830okowkeow"
        1 * paystackService.getEndPoint() >> "http://paystack.co"

        when: "createSubscription() is called"
        Map params = [:]
        paystackService.createSubscription(params)

        then: "postRequest() is called"
        1 * paystackService.postRequest(*_) >> [:]
    }

    void "Test enableSubscription() works as expected"() {
        1 * paystackService.getSecretKey() >> "340830okowkeow"
        1 * paystackService.getEndPoint() >> "http://paystack.co"

        when: "enableSubscription() is called"
        Map params = [:]
        paystackService.enableSubscription(params)

        then: "postRequest() is called once"
        1 * paystackService.postRequest(*_) >> [:]
    }

    void "Test disableSubscription() works as expected"() {
        1 * paystackService.getSecretKey() >> "340830okowkeow"
        1 * paystackService.getEndPoint() >> "http://paystack.co"

        when: "disableSubscription() is called"
        Map params = [:]
        paystackService.disableSubscription(params)

        then: "postRequest() is called once"
        1 * paystackService.postRequest(*_) >> [:]
    }
}
