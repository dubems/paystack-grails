package com.paystack

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

}
