package com.dubems

import grails.core.GrailsApplication
import grails.util.Environment
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Value
import paystack.grails.PaystackPlanInterval
import paystack.grails.Utils
import paystack.grails.exceptions.PaystackException
import paystack.grails.exceptions.PaystackValidationExecption
import paystack.grails.exceptions.VerifyPaymentException

/**
 * @athor Nriagu Chidubem
 * @email <nriagudubem@gmail.com>
 */
@Slf4j
class PaystackService implements Utils {

    GrailsApplication grailsApplication

    HttpUtilityService httpUtilityService

    private final static String BEARER = "Bearer"

    @Value('${paystack.endpoint}')
    String endPoint

    /**
     * get the secret used for API Request
     * When app in dev, use the testSecretKey else use the liveSecretKey
     */
    private String getSecretKey() {
        if (Environment.current.name == "${Environment.DEVELOPMENT}".toLowerCase() || Environment.current.name == "${Environment.TEST}".toLowerCase()) {
            return grailsApplication.config.getProperty('paystack.testSecretKey')
        } else {
            return grailsApplication.config.getProperty('paystack.liveSecretKey')
        }

    }

    /**
     * get the public key for the API Request
     *  When app in dev, use the testPublicKey else use the livePublicKey
     */
    private String getPublicKey() {
        if (Environment.current.name == "${Environment.DEVELOPMENT}".toLowerCase() || Environment.current.name == "${Environment.TEST}".toLowerCase()) {
            return grailsApplication.config.getProperty('paystack.testPublicKey')
        } else {
            return grailsApplication.config.getProperty('paystack.livePublicKey')
        }
    }
    /**
     * Get authorization url from paystack
     * The authorization url is to redirect to paystack for payment
     * @params params : this contains info to be sent to PAYSTACK(email, amount,...)
     * @return Authorization Url
     */
    String getAuthUrl(Map<String, String> params) {
        if (!params.amount || !params.email) {
            throw new PaystackValidationExecption('Incomplete Parameters. either amount or email is missing')
        }
        Map<String, String> response = makePaymentRequest(params)
        validatePaystackResponse(response)
        return response?.data?.authorization_url
    }

    /**
     * Make Request to paystack
     * This returns the authorization url
     * @param params : THis contains info to be sent PAYSTACK(email,amount,...)
     */
    private Map<String, String> makePaymentRequest(final Map<String, String> params) {
        final String authString = "Bearer " + secretKey
        final String url = endPoint + "/transaction/initialize"

        Map reqParams = [
                amount      : params.amount,
                email       : params.email,
                reference   : generateTransactionRef(),
                plan        : params.plan ?: '',
                first_name  : params.first_name ?: '',
                last_name   : params.last_name ?: '',
                metadata    : params.metadata ?: [:],
                callback_url: params.callback_url ?: ''
        ]
        Map<String, String> response = httpUtilityService.postRequest(url, reqParams, authString)
        return response
    }


    /**
     * Verify a transaction
     * @param reference : transaction reference
     * @return : response from paystack
     */
    Map<String, String> verifyTransaction(final String reference) throws VerifyPaymentException {
        if (!reference) throw new PaystackValidationExecption("Kindly provide payment reference")

        final String authString = "${BEARER} ${secretKey}"
        final String url = endPoint + "/transaction/verify/${reference}"
        Map<String, String> response = httpUtilityService.getRequest(url, authString)
        if (response?.status != "true") {
            final String failureMessage = response?.message
            throw new VerifyPaymentException("Transaction could not be verified: ${failureMessage}")
        }
        return response
    }

    /**
     * Get the payment data from paystack after verification
     * @param params
     * @return
     */
    Map<String, String> getPaymentData(Map<String, String> params) {
        return verifyTransaction(params?.reference)
    }

    /**
     *
     * @param queryParam : Optional queryParam
     * @return
     */
    Map<String, String> listTransactions(final Map<String, String> queryParam) {
        final String authString = "${BEARER} ${secretKey}"
        final String url = endPoint + "/transaction?${mapQueryParams(queryParam)}"

        Map<String, String> response = httpUtilityService.getRequest(url, authString)
        validatePaystackResponse(response)

        return response
    }

    /**
     * Fetch a particular transaction
     * @param id : identifier of transaction to fetch
     * @return
     */
    Map<String, String> fetchTransaction(int id) {
        final String authString = "${BEARER} ${secretKey}"
        final String url = endPoint + "/transaction/${id}"
        Map<String, String> response = httpUtilityService.getRequest(url, authString)
        validatePaystackResponse(response)

        return response
    }

    /**
     * Create a customer
     * @param params
     * @return
     */
    Map<String, String> createCustomer(final Map<String, String> params) {
        if (!params.email) {
            throw new PaystackValidationExecption("Kindly provide customers email")
        }
        final String authString = "${BEARER} ${secretKey}"
        final String url = endPoint + "/customer"

        Map reqParams = [
                email     : params.email,
                first_name: params.first_name ?: "",
                last_name : params.last_name ?: "",
                phone     : params.phone ?: "",
                metadata  : params.metadata ?: [:]
        ]
        Map<String, String> response = httpUtilityService.postRequest(url, reqParams, authString)
        validatePaystackResponse(response)
        return response
    }

    /**
     * List all customers on your Paystack account
     * @return
     */
    Map<String, String> getAllCustomers(final Map<String, String> queryParam) {
        final String authString = "${BEARER} ${secretKey}"
        final String url = "${endPoint}/customer?${mapQueryParams(queryParam)}"
        Map<String, String> response = httpUtilityService.getRequest(url, authString)
        validatePaystackResponse(response)

        return response
    }

    /**
     * Return a single customer given its id
     * @param customerId
     * @return
     */
    Map<String, String> fetchCustomer(long customerId) {
        final String authString = "${BEARER} ${secretKey}"
        final String url = endPoint + "/customer/${customerId}"
        Map<String, String> response = httpUtilityService.getRequest(url, authString)
        validatePaystackResponse(response)

        return response
    }

    /**
     * Return all plans on your paystack account
     * @return
     */
    Map<String, String> getAllPlans() {
        final String authString = "${BEARER} ${secretKey}"
        String url = endPoint + '/plan'
        Map<String, String> response = httpUtilityService.getRequest(url, authString)
        validatePaystackResponse(response)
        return response
    }

    /**
     * Return all transactions on you paystack account
     * @return
     */
    Map<String, String> getAllTransactions() {
        final String authString = "${BEARER} ${secretKey}"
        String url = endPoint + '/transaction'
        Map<String, String> response = httpUtilityService.getRequest(url, authString)
        validatePaystackResponse(response)

        return response
    }

    /**
     * Create plan
     * @return
     */
    Map<String, String> createPlan(final Map<String, String> params) {
        final String authString = "${BEARER} ${secretKey}"
        final String url = endPoint + '/plan'

        PaystackPlanInterval planInterval = verifyPlanInterval(params.interval)

        Map reqParams = [
                name         : params.name,
                description  : params.description,
                amount       : params.amount,
                send_invoices: params.send_invoices,
                send_sms     : params.send_sms,
                currency     : params.currency,
                interval     : planInterval.name()
        ]

        Map<String, String> response = httpUtilityService.postRequest(url, reqParams, authString)
        validatePaystackResponse(response)
        return response
    }

    /**
     * Get a particular plan given the plan id
     * @param planId
     * @return
     */
    Map<String, String> fetchPlan(long planId) {
        final String authString = "${BEARER} ${secretKey}"
        String url = endPoint + "/plan/${planId}"

        Map<String, String> response = httpUtilityService.getRequest(url, authString)
        validatePaystackResponse(response)

        return response
    }

    /**
     * Export transaction
     * @param params
     * @return
     */
    Map<String, String> exportTransaction(final Map<String, String> params) {
        final String authString = "${BEARER} ${secretKey}"
        final String url = endPoint + "/transaction/export?${mapQueryParams(params)}"
        Map<String, String> response = httpUtilityService.getRequest(url, authString)
        validatePaystackResponse(response)
        return response
    }

    /**
     * Create a payment subscription
     * @param param
     * @internal email: customers email address
     * plan(string): plan code
     * authorization(string): customers autorization code
     * start_date(String): First debit date (ISO 8601 format)
     *
     * @return
     */
    Map<String, String> createSubscription(final Map<String, String> params) {
        final String authString = "${BEARER} ${secretKey}"
        final String url = endPoint + '/subscription'
        Map postParams = [
                customer     : params.customer,
                plan         : params.plan,
                authorization: params.authorization,
                start_date   : params.start_date
        ]
        Map<String, String> response = httpUtilityService.postRequest(url, postParams, authString)
        validatePaystackResponse(response)
        return response

    }

    /**
     * Enable a user subscription using code and token
     * @param params
     * @return
     */
    Map<String, String> enableSubscription(final Map<String, String> params) {
        if (!params.code || !params.token) throw new PaystackValidationExecption("Kindly provide code and token to enable subscription")
        final String authString = "${BEARER} ${secretKey}"
        final String url = endPoint + '/subscription/enable'

        Map postParam = [
                code : params.code,
                token: params.token
        ]
        Map<String, String> response = httpUtilityService.postRequest(url, postParam, authString)
        validatePaystackResponse(response)
        return response
    }

    /**
     * Disable subscription using  code and token
     * @param params
     * @return
     */
    Map<String, String> disableSubscription(final Map<String, String> params) {
        if (!params.code || !params.token) throw new PaystackValidationExecption("Kindly provide code and token to enable subscription")
        final String authString = "${BEARER} ${secretKey}"
        final String url = endPoint + '/subscription/disable'

        Map postParam = [
                code : params.code,
                token: params.token
        ]
        Map<String, String> response = httpUtilityService.postRequest(url, postParam, authString)
        validatePaystackResponse(response)
        return response
    }

    /**
     * Verify BVN Match
     * @param params
     * @return
     */
    Map<String, String> verifyBVNMatch(final Map<String, String> params) {
        if (!params.bvn || !params.account_number || !params.bank_code) {
            throw new PaystackValidationExecption('Either BVN, account_number or bank_code is missing')
        }

        final String authString = "${BEARER} ${secretKey}"
        final String url = endPoint + '/bvn/match'

        Map payload = [
                bvn           : params.bvn,
                account_number: params.account_number,
                bank_code     : params.bank_code,
                first_name    : params.first_name,
                last_name     : params.last_name,
                middle_name   : params.middle_name,
        ]

        Map<String, String> response = httpUtilityService.postRequest(url, payload, authString)
        validatePaystackResponse(response)
        return response
    }

    /**
     * Resolve BVN
     * @param BVN
     * @return
     */
    Map<String, String> resolveBVN(final String BVN) {
        if (!BVN) throw new PaystackValidationExecption("Kindly provide BVN")
        final String authString = "${BEARER} ${secretKey}"
        final String url = endPoint + '/bank/resolve_bvn/' + BVN
        Map<String, String> response = httpUtilityService.getRequest(url, authString)
        validatePaystackResponse(response)
        return response
    }

    /**
     * Confirms that an account number and bank code matches
     * @param accountNumber
     * @param bankCode
     * @return
     */
    Map<String, String> resolveAccountNumber(final String accountNumber, final String bankCode) {
        if (!accountNumber || !bankCode) throw new PaystackValidationExecption('either account_number or bank_code is missing')
        final String authString = "${BEARER} ${secretKey}"
        final String url = endPoint + "/bank/resolve?account_number=$accountNumber&bank_code=$bankCode"
        Map<String, String> response = httpUtilityService.getRequest(url, authString)
        validatePaystackResponse(response)
        return response
    }

    /**
     * Takes first 6 digits of the cardPAN and returns some info about the card
     * @param cardBIN
     * @return
     */
    Map<String, String> resolveCardBIN(long cardBIN) {
        final String authString = "${BEARER} ${secretKey}"
        final url = endPoint + "/decision/bin/$cardBIN"
        Map<String, String> response = httpUtilityService.getRequest(url, authString)
        validatePaystackResponse(response)
        return response
    }

    /**
     * Verify the plan interval for plan creation
     * @param interval
     * @return
     */
    private static PaystackPlanInterval verifyPlanInterval(String interval) {
        PaystackPlanInterval planInterval
        try {
            planInterval = PaystackPlanInterval.valueOf(interval.toUpperCase())
        } catch (IllegalArgumentException ex) {
            throw new PaystackException(interval + " is not a valid interval format, ${ex.message}")
        }
        return planInterval
    }

    private static void validatePaystackResponse(final Map<String, String> response) {
        log.error("status ${response?.status}")
        if (response?.status != "true") {
            throw new PaystackException("An error occurred: ${response?.message ?: "Request to paystack was not successful"}")
        }
    }
}
