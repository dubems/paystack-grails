package com.dubems

import grails.testing.services.ServiceUnitTest
import paystack.grails.PaystackPlanInterval
import paystack.grails.exceptions.PaystackException
import paystack.grails.exceptions.PaystackValidationExecption
import paystack.grails.exceptions.VerifyPaymentException
import spock.lang.Specification


class PaystackServiceSpec extends Specification implements ServiceUnitTest<PaystackService> {
    HttpUtilityService httpUtilityService

    def setup() {
        httpUtilityService = Mock(HttpUtilityService)
        service.httpUtilityService = httpUtilityService
        service.endPoint = 'https://api.paystack.co'
    }

    void "test getAuthUrl returns the expected url"() {
        setup:
        Map params = [email: 'nriagudubem@gmail.com', amount: '50000']
        String expectedUrl = "http://api.paystack.co/reference=XYZ"

        when: "response returns an authorization url"
        String actualUrl = service.getAuthUrl(params)

        then: "the actual url equals the expected url"
        service.httpUtilityService.postRequest(_ as String, _ as Map, _ as String) >> [status: 'true', data: [authorization_url: expectedUrl]]
        actualUrl == expectedUrl

        when: "response does not return expected url"
        String url = service.getAuthUrl(params)

        then: "PaystackException is thrown"
        service.httpUtilityService.postRequest(_ as String, _ as Map, _ as String) >> [status: 'false']
        url != expectedUrl
        thrown(PaystackException)
        0 * _
    }

    void "test verifyTransaction should verify transaction correctly"() {
        given:
        final String transactionReference = 'trxReference'
        Map<String, String> expectedResponse = [status: 'true']

        when: "response status is true"
        Map<String, String> response = service.verifyTransaction(transactionReference)

        then:
        service.httpUtilityService.getRequest(*_) >> expectedResponse
        response == expectedResponse
        noExceptionThrown()

        when: "response status is false"
        service.verifyTransaction(transactionReference)

        then: "VerifyPaymentException is thrown"
        service.httpUtilityService.getRequest(*_) >> [status: 'failed']
        thrown(VerifyPaymentException)
        0 * _
    }

    void "test listTransactions works as expected"() {
        given:
        final Map<String, String> params = [customer: '29', amount: '5000']
        Map<String, String> expectedResponse = [status: 'true', data: [:]]

        when:
        Map<String, String> response = service.listTransactions(params)

        then:
        1 * service.httpUtilityService.getRequest(_ as String, _ as String) >> { String url, String authString ->
            url == "https://api.paystack.co/transaction?customerr=29&amount=5000"
            return expectedResponse
        }

        expectedResponse == response
        0 * _
    }

    void "test fetchTransaction should fetch transaction from paystack"() {
        given:
        Map<String, String> expectedResponse = [status: 'true']

        when:
        Map<String, String> response = service.fetchTransaction(232)

        then:
        1 * service.httpUtilityService.getRequest(*_) >> expectedResponse
        response == expectedResponse
        0 * _
    }

    void "test createCustomer() works as expected"() {
        when: "There is no email parameter"
        Map<String, String> params = [email: "", first_name: 'chidubem']
        service.createCustomer(params)

        then: "An exception is thrown"
        thrown(PaystackValidationExecption)

        when: "There is email parameter and paystack request is not successful"
        Map<String, String> parameter = [email: "nriagudubem@gmail.com"]
        service.createCustomer(parameter)

        then: "request to paystack in made"
        service.httpUtilityService.postRequest(*_) >> [status: 'false']
        thrown(PaystackException)
        0 * _
    }

    void "test getAllCustomers works as expected"() {
        given:
        Map<String, String> mockResponse = [status: 'true']

        when:
        Map<String, String> response = service.getAllCustomers()

        then:
        1 * service.httpUtilityService.getRequest(*_) >> mockResponse
        assert mockResponse == response
        0 * _
    }

    void "fetchCustomer works as expected"() {
        given:
        Map<String, String> mockResponse = [status: 'true']

        when:
        Map<String, String> response = service.fetchCustomer(23L)

        then:
        service.httpUtilityService.getRequest(*_) >> mockResponse
        mockResponse == response
        0 * _

    }

    void "test getAllPlans works as expected"() {
        setup:
        Map<String, String> mockResponse = [status: 'true']

        when:
        Map<String, String> response = service.getAllPlans()

        then:
        service.httpUtilityService.getRequest(*_) >> mockResponse
        assert mockResponse == response
        0 * _
    }

    void "test getAllTransactions returns the transactions"() {
        setup:
        Map<String, String> mockResponse = [status: 'true']

        when:
        Map<String, String> response = service.getAllTransactions()

        then:
        service.httpUtilityService.getRequest(*_) >> mockResponse
        mockResponse == response
        0 * _
    }

    void "test createPlan works as expected"() {
        given:
        Map<String, String> mockResponse = [status: 'true']

        when: 'Plan interval is verified'
        Map correctParams = [interval: 'monthly']
        Map incorrectParams = [interval: 'timely']
        Map<String, String> response = service.createPlan(correctParams)

        then:
        service.httpUtilityService.postRequest(*_) >>
                { String url, Map<String, String> payload, String authString ->
                    payload.interval == PaystackPlanInterval.MONTHLY.name()

                    return mockResponse
                }

        mockResponse == response
        0 * _

        when: 'createPlan is called with in-correct interval'
        service.createPlan(incorrectParams)

        then: "PaystackException is thrown"
        thrown(PaystackException)
    }

    void "fetchPlan works as expected"() {
        when: "fetchPlan is called and Paystack request fails"
        service.fetchPlan(23)

        then: 'PaystackException is thrown'
        service.httpUtilityService.getRequest(*_) >> [:]
        thrown(PaystackException)
        0 * _
    }

    void "test exportTransaction works as expected"() {
        when: "fetchPlan is called and request to Paystack fails"
        service.exportTransaction([:])

        then: 'PaystackException is thrown'
        service.httpUtilityService.getRequest(*_) >> [:]
        thrown(PaystackException)
        0 * _
    }


    void " Test createSubscription works as expected"() {
        given:
        Map<String, String> mockResponse = [status: 'true']
        when:
        Map params = [customer: 'nriagu chidubem']
        Map<String, String> response = service.createSubscription(params)

        then:
        service.httpUtilityService.postRequest(*_) >> mockResponse
        mockResponse == response
    }

    void "test enableSubscription works as expected"() {
        given:
        Map<String, String> mockResponse = [status: 'true']
        Map params = [code: '123', token: 'qwerty']

        when: 'required parameters are present'
        Map<String, String> response = service.enableSubscription(params)

        then:
        service.httpUtilityService.postRequest(*_) >> { String url, Map<String, String> param, String authString ->
            param == params
            return mockResponse
        }
        mockResponse == response

        when: 'required parameters are not present'
        service.enableSubscription([:])

        then: 'PaystackValidationException is thrown'
        thrown(PaystackValidationExecption)
    }

    void "Test disableSubscription works as expected"() {
        given:
        Map<String, String> mockResponse = [status: 'true']
        Map params = [code: '123', token: 'qwerty']

        when: 'required parameters are present'
        Map<String, String> response = service.disableSubscription(params)

        then:
        service.httpUtilityService.postRequest(*_) >> { String url, Map<String, String> param, String authString ->
            param == params
            return mockResponse
        }
        mockResponse == response

        when: 'required parameters are not present'
        service.disableSubscription([:])

        then: 'PaystackValidationException is thrown'
        thrown(PaystackValidationExecption)
    }

    void 'test verifyBVNMatch verifies if BVN matches account number'() {
        given:
        Map<String, String> mockResponse = [status: 'true']

        when: 'required input parameters are present'
        Map<String, String> params = [bvn: '009872343211', account_number: '098732323', bank_code: '18973']
        Map<String, String> response = service.verifyBVNMatch(params)

        then:
        service.httpUtilityService.postRequest(*_) >> mockResponse
        assert mockResponse == response
        0 * _

        when: 'required input parameters are not present'
        Map<String, String> parameters = [bvn: '', account_number: '', last_name: 'Nriagu']
        service.verifyBVNMatch(parameters)

        then:
        service.httpUtilityService.postRequest(*_) >> mockResponse
        thrown(PaystackValidationExecption)
        0 * _
    }


    void 'test resolveBVN should return account data when BVN is provided'() {
        given:
        Map<String, String> mockResponse = [status: 'true']

        when:
        final String BVN = '1234567890'
        Map<String, String> response = service.resolveBVN(BVN)

        then:
        service.httpUtilityService.getRequest(*_) >> mockResponse
        response == mockResponse
        0 * _

        when: 'BVN is null'
        String bvn = null
        service.resolveBVN(bvn)

        then:
        thrown(PaystackValidationExecption)
        0 * _
    }

}


