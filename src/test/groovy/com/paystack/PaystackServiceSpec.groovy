package com.paystack

import grails.core.GrailsApplication
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(PaystackService)
class PaystackServiceSpec extends Specification {

    def paystackService

    def setup() {
         paystackService = Spy(PaystackService){

        }

    }

    def cleanup() {
    }

    void "Test getAuthUrl() returns the expected"(){
        setup:
        def params = [:]
        def expectedUrl = "jjdkfndjwiefjd"

        when:"response returns an authorization url"
        def actualUrl = paystackService.getAuthUrl(params)

        then:"the actual url equals the expected url"
        1*paystackService.makePaymentRequest(params) >> [data:[authorization_url:expectedUrl]]
        actualUrl == expectedUrl

        when:"response does not return a url"
        def _actualUrl = paystackService.getAuthUrl(params)

        then:""
        1*paystackService.makePaymentRequest(params) >> [:]
        _actualUrl != expectedUrl
        _actualUrl == null

    }

    void "Test validate() works as expected"(){
        when:"all required parameters are present"
        def params = [:]
        params.email = "nriagudubem@gmail.com"
        params.amount = "2000000"

        def response = paystackService.validate(params)

        then:"an instance of paystackService is returned"
        assert response instanceof PaystackService

        when:"required parameters are not complete"
        def _params = [:]
        params.email = null
        params.amount = "300000"

        paystackService.validate(_params)

        then:"An exception is thrown"
        thrown(Exception)

    }


    void "Test makePaymentRequest() performs as expected"(){
        setup:
        def params = [:]

        when:"payment request is made"
        paystackService.makePaymentRequest(params)

        then:"verify various method calls"
        1*paystackService.getSecretKey() >> "340830okowkeow"
        1*paystackService.getEndPoint() >> "http://paystack.co"
        1*paystackService.postRequest(*_) >> [:]

    }

    void "Test verify() works as expected "(){
        setup:
        2*paystackService.getSecretKey() >> "340830okowkeow"
        2*paystackService.getEndPoint()  >> "http://paystack.co"

        when:"Verification fails"
        String reference = 'wewrieor9343'
        paystackService.verify(reference)

        then:"An Exception is thrown"
        1*paystackService.getRequest(*_) >> [data:[status:"failed"]]
        thrown(Exception)

        when:"Verification passes"
        def response = paystackService.verify(reference)

        then:"response is returned"
        1*paystackService.getRequest(*_) >> [data:[status:"success"]]
        response == [data:[status:"success"]]
    }

    void "Test listTransactions() works as expected"() {
        setup:
        1*paystackService.getSecretKey() >> "340830okowkeow"
        1*paystackService.getEndPoint()  >> "http://paystack.co"

        when:"listTransactions() is called"
        paystackService.listTransactions()

        then:"getRequest() is called once"
        1*paystackService.getRequest(*_) >> [:]
    }

    void "Test fetchTransaction() works as expected"(){
        setup:
        1*paystackService.getSecretKey() >> "340830okowkeow"
        1*paystackService.getEndPoint()  >> "http://paystack.co"

        when:"fetchTransaction() is called"
        paystackService.fetchTransaction(232)

        then:""
        1*paystackService.getRequest(*_) >> [:]
    }

    void "Test createCustomer() works as expected"(){
        setup:
        2*paystackService.getSecretKey() >> "340830okowkeow"
        2*paystackService.getEndPoint()  >> "http://paystack.co"

        when:"There is no email parameter"
        def params = [email:""]
        paystackService.createCustomer(params)

        then:"An exception is thrown"
        thrown(Exception)

        when:"There is email parameter"
        def _params = [email: "nriagudubem@gmail.com"]
        paystackService.createCustomer(_params)

        then:"postRequest() is called"
        1*paystackService.postRequest(*_) >> [:]



    }

}
