package com.dubems.grails.paystack

import grails.core.GrailsApplication
import grails.util.Environment
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils

/**
 * @athor Nriagu Chidubem
 * @email <nriagudubem@gmail.com>
 */

class PaystackService {

    GrailsApplication grailsApplication

    /**
     * get PAYSTACK endpoint
     */
    String getEndPoint() {
        return grailsApplication.config.paystack.endpoint
    }
    /**
     * get the secret used for API Request
     * When app in dev, use the testSecretKey else use the liveSecretKey
     */
    String getSecretKey() {
        if (Environment.current.name == "${Environment.DEVELOPMENT}".toLowerCase() ||
                Environment.current.name == "${Environment.TEST}".toLowerCase()) {
            return grailsApplication.config.paystack.testSecretKey
        } else {
            return grailsApplication.config.paystack.liveSecretKey
        }

    }

    /**
     * get the public key for the API Request
     *  When app in dev, use the testPublicKey else use the livePublicKey
     */
    String getPublicKey() {
        if (Environment.current.name == "${Environment.DEVELOPMENT}".toLowerCase() ||
                Environment.current.name == "${Environment.TEST}".toLowerCase()) {
            return grailsApplication.config.paystack.testPublicKey
        } else {
            return grailsApplication.config.paystack.livePublicKey
        }
    }
    /**
     * Get authorization url from paystack
     * The authorization url is to redirect to paystack for payment
     * @params params : this contains info to be sent to PAYSTACK(email, amount,...)
     * @return
     */
    def getAuthUrl(params) {
        def response = this.makePaymentRequest(params)
        return response.data?.authorization_url
    }

    /**
     * Validate params being sent to paystack
     * @param params
     * @return
     */
    def validate(Map params) {
        if (!params.amount || !params.email) {
            throw new Exception('Incomplete Parameters')
        }
        return this
    }

    /**
     * Make Request to paystack
     * This returns the authorization url
     * @param params : THis contains info to be sent PAYSTACK(email,amount,...)
     */
    def makePaymentRequest(params) {
        String authString = "Bearer " + secretKey
        String url = endPoint + "/transaction/initialize"

        Map reqParams = [
                amount      : params.amount,
                email       : params.email,
                reference   : this.generateTrxnRef(),
                plan        : params.plan ?: '',
                first_name  : params.first_name ?: '',
                last_name   : params.last_name ?: '',
                metadata    : params.metadata ?: [:],
                callback_url: params.callback_url ?: ''
        ]

        return this.postRequest(url, reqParams, authString)

    }

    /**
     * Generate a Unique Transaction reference
     * this is used to verify the transaction
     * @return
     */
    String generateTrxnRef() {
        List numPool = 0..9
        List alphaPoolCapital = 'A'..'Z'
        List alphaPoolSmall = 'a'..'z'
        List allPool = (numPool + alphaPoolCapital + alphaPoolSmall)
        Collections.shuffle(allPool)
        def trxnReference = allPool.subList(0, 32)
        String result = trxnReference.join("")

        return result

    }

    /**
     * Verify a transaction
     * @param reference : transaction reference
     * @return : response from paystack
     */
    Map verify(String reference) {

        String authString = "Bearer " + secretKey
        String url = endPoint + "/transaction/verify/${reference}"

        Map response = this.getRequest(url, authString)
        if (response?.data?.status == "failed") {
            throw new Exception("Transaction could not be verified")
        }

        return response
    }
    /**
     * Get the payment data from paystack after verification
     * @param params
     * @return
     */
    Map getPaymentData(params) {
        return this.verify(params?.reference)
    }

    /**
     * Make A get request to the specified resource(url)
     * @param url
     * @param authString
     * @return
     */
    Map getRequest(String url, String authString) {
        CloseableHttpClient client = HttpClientBuilder.create().build()
        HttpGet httpGet = getHttpGet(url)
        httpGet.addHeader("Authorization", authString)
        CloseableHttpResponse result = client.execute(httpGet)
        def entity = result.getEntity() // get result
        String responseBody = EntityUtils.toString(entity); // extract response body
        def jsonSlurper = getJsonSlurper() // for parsing response
        def responseMap = jsonSlurper.parseText(responseBody); // parse into json object
        result.close()

        return responseMap as Map
    }

    /**
     * Make a post request the the specified resource(url)
     * @param url
     * @param data
     * @param authString
     * @return
     */
    Map postRequest(String url, Map data, String authString) {
        def postParams = getJsonBuilder(data).toPrettyString()
        CloseableHttpClient client = HttpClients.createDefault()
        StringEntity requestEntity = this.getStringEntity(
                postParams,
                ContentType.APPLICATION_JSON)
        HttpPost request = this.getHttpPost(url)
        request.setHeader("Authorization", authString)
        request.setEntity(requestEntity)
        CloseableHttpResponse response = client.execute(request)
        def entity = response.getEntity()
        String responseBody = EntityUtils.toString(entity)
        def jsonSlurper = this.getJsonSlurper() // for parsing response
        def responseMap = jsonSlurper.parseText(responseBody); // parse into json object
        response.close() // free system resources

        return responseMap as Map
    }

    JsonBuilder getJsonBuilder(Map data) {
        return new JsonBuilder(data)
    }

    StringEntity getStringEntity(postParams, contentType) {
        return new StringEntity(postParams, contentType)
    }

    HttpGet getHttpGet(String url) {
        return new HttpGet(url)
    }

    HttpPost getHttpPost(String url) {
        return new HttpPost(url)
    }

    JsonSlurper getJsonSlurper() {
        return new JsonSlurper()
    }

    /**
     * List All Transactions
     * @return
     */
    Map listTransactions() {
        String authString = "Bearer " + secretKey
        String url = endPoint + "/transaction/"

        return this.getRequest(url, authString)
    }

    /**
     * Fetch a particular transaction
     * @param id : identifier of transaction to fetch
     * @return
     */
    Map fetchTransaction(int id) {
        String authString = "Bearer " + secretKey
        String url = endPoint + "/transaction/" + id

        return this.getRequest(url, authString)
    }

    /**
     * Create a customer
     * @param params
     * @return
     */
    Map createCustomer(params) {
        String authString = "Bearer " + secretKey
        String url = endPoint + "/customer"

        if (!params.email) {
            throw new Exception("Kindly provide customers email")
        }

        Map reqParams = [
                email     : params.email,
                first_name: params.first_name ?: "",
                last_name : params.last_name ?: "",
                phone     : params.phone ?: "",
                metadata  : params.metadata ?: [:]
        ]
        return this.postRequest(url, reqParams, authString)
    }

    /**
     * List all customers on your Paystack account
     * @return
     */
    Map getAllCustomers() {
        String authString = 'Bearer ' + secretKey
        String url = endPoint + '/customer'

        return this.getRequest(url, authString)
    }

    /**
     * Return a single customer given its id
     * @param customerId
     * @return
     */
    Map fetchCustomer(customerId) {

        String authString = 'Bearer ' + secretKey
        String url = endPoint + "/customer/" + customerId

        return this.getRequest(url, authString)

    }

    /**
     * Return all plans on your paystack account
     * @return
     */
    Map getAllPlans() {
        String authString = 'Bearer ' + secretKey
        String url = endPoint + '/plan'

        return this.getRequest(url, authString)
    }

    /**
     * Return all transactions on you paystack account
     * @return
     */
    Map getAllTransactions() {
        String authString = 'Bearer ' + secretKey
        String url = endPoint + '/transaction'

        return this.getRequest(url, authString)
    }

    /**
     * Create plan
     * @return
     */
    Map createPlan(params) {
        String authString = 'Bearer ' + secretKey
        String url = endPoint + '/plan'

        if (verifyPlanInterval(params.interval)) {

            Map reqParams = [
                    name         : params.name,
                    description  : params.description,
                    amount       : params.amount,
                    send_invoices: params.send_invoices,
                    send_sms     : params.send_sms,
                    currency     : params.currency,
                    interval     : params.interval
            ]

            return postRequest(url, reqParams, authString)
        }

        throw new Exception(params.interval + " Is not a valid interval format")


    }

    /**
     * Verify the plan interval for plan creation
     * @param interval
     * @return
     */
    boolean verifyPlanInterval(String interval) {
        List intervals = ["hourly", "daily", "weekly", "monthly", "annually"]
        if (interval in intervals) {
            return true
        }

        return false
    }

    /**
     * Get a particular plan given the plan id
     * @param planId
     * @return
     */
    Map fetchPlan(planId) {
        String authString = 'Bearer ' + secretKey
        String url = endPoint + '/plan/' + planId

        return getRequest(url, authString)
    }

    /**
     * Export transaction
     * @param params
     * @return
     */
    Map exportTransaction(params) {
        String authString = 'Bearer ' + secretKey
        def from = params.from
        def to = params.to
        boolean settled = params.settled
        String url = endPoint + '/transaction/export?from=' + from + 'to=' + to + 'settled=' + settled

        return this.getRequest(url, authString)
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
    Map createSubscription(params) {
        String authString = 'Bearer ' + secretKey
        String url = endPoint + '/subscription'
        def postParams = [
                customer     : params.customer,
                plan         : params.plan,
                authorization: params.authorization,
                start_date   : params.startDate
        ]

        return this.postRequest(url, postParams, authString)
    }

    /**
     * Enable a user subscription using code and token
     * @param params
     * @return
     */
    Map enableSubscription(params) {
        String authString = 'Bearer ' + secretKey
        String url = endPoint + '/subscription/enable'

        def postParam = [
                code : params.code,
                token: params.token
        ]

        return this.postRequest(url, postParam, authString)
    }

    /**
     * Disable subscription using  code and token
     * @param params
     * @return
     */
    Map disableSubscription(params) {
        String authString = 'Bearer ' + secretKey
        String url = endPoint + '/subscription/disable'

        def postParam = [
                code : params.code,
                token: params.token
        ]

        return this.postRequest(url, postParam, authString)
    }


}
