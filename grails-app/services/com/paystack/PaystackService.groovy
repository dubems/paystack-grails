package com.paystack

import grails.transaction.Transactional
import grails.util.Environment
import groovy.json.JsonSlurper
import org.apache.http.client.CredentialsProvider
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import org.aspectj.apache.bcel.classfile.annotation.NameValuePair
import org.springframework.http.HttpEntity
import sun.net.www.http.HttpClient

/**
 * @athor Nriagu Chidubem
 * @email <nriagudubem@gmail.com>
 */
@Transactional
class PaystackService {

    String secretKey

    String publicKey

    String endPoint

    /**
     * PaystackService Constructor
     */
     PaystackService()
     {
         setSecretKey()
         setEndPoint()
     }

    /**
     * Set PAYSTACK endpoint
     */
    void setEndPoint(){
        endPoint = grailsApplication.config.paystack.endpoint
    }
    /**
     * Set the secret used for API Request
     * When app in dev, use the testSecretKey else use the liveSecretKey
     */
    void setSecretKey()
    {
        if(Environment.current.name == "${Environment.DEVELOPMENT}".toLowerCase() ||
                Environment.current.name == "${Environment.TEST}".toLowerCase()){
            secretKey =  grailsApplication.config.paystack.testSecretKey
        } else{
             secretKey = grailsApplication.config.paystack.liveSecretKey
        }

    }

    /**
     * Set the public key for the API Request
     *  When app in dev, use the testPublicKey else use the livePublicKey
     */
    void setPublicKey()
    {
        if(Environment.current.name == "${Environment.DEVELOPMENT}".toLowerCase() ||
                Environment.current.name == "${Environment.TEST}".toLowerCase()){
            publicKey =  grailsApplication.config.paystack.testPublicKey
        } else{
            publicKey = grailsApplication.config.paystack.livePublicKey
        }
    }
    /**
     * Get authorization url from paystack
     * The authorization url is to redirect to paystack for payment
     * @params params : this contains info to be sent to PAYSTACK(email, amount,...)
     * @return
     */
    def getAuthorizationUrl(params)
    {
        def response =  this.makePaymentRequest(params)
        return response.authorization_url
    }

    /**
     * Redirect to the paystack url
     */
    def redirectNow(String url)
    {

    }

    /**
     * Validate params being sent to paystack
     * @param params
     * @return
     */
    def validateParameters(Map params)
    {
        if(!params.amount || !params.emamil) {
            throw new Exception('Incomplete Parameters')
        }
        return this
    }

    /**
     * Make Request to paystack
     * This returns the authorization url
     * @param  params : THis contains info to be sent PAYSTACK(email,amount,...)
     */
    def makePaymentRequest(params)
    {
        String authString = "Bearer" +secretKey
        String url = endPoint+"/transaction/initialize"

        Map reqParams = [
                amount       :params.amount,
                email        :params.email,
                reference    :this.generateTrxnRef(),
                plan         :params.plan,
                first_name   :params.first_name,
                last_name    :params.last_name,
                metadata     :params.metadata,
                callback_url :params.callback_url
        ]

         return this.postRequest(url,reqParams,authString)

    }

    /**
     * Generate a Unique Transaction reference
     * this is used to verify the transaction
     * @return
     */
    String generateTrxnRef()
    {
        List numPool = 0..9
        List alphaPoolCapital = 'A'..'Z'
        List alphaPoolSmall   = 'a'..'z'
        List allPool      = (numPool + alphaPoolCapital + alphaPoolSmall)
        List shuffledPool = Collections.shuffle(allPool)
        def trxnReference = shuffledPool.subList(0,32)

        return trxnReference.toString()

    }


    /**
     * Verify a transaction
     * @param reference : transaction reference
     * @return : response from paystack
     */
     Map verify(String reference) {

         String authString = "Bearer" +secretKey
         String url = endPoint+"/transaction/verify/${reference}"

         return this.getRequest(url,authString)
    }

    /**
     * Make A get request to the specified resource(url)
     * @param url
     * @param authString
     * @return
     */
    Map getRequest(String url,String authString)
    {
        CredentialsProvider provider = new BasicCredentialsProvider()
        HttpClient client = HttpClientBuilder.create().build()
        HttpGet httpGet = new HttpGet(url)
        httpGet.addHeader("Authorization",authString)
        CloseableHttpResponse result = client.execute(httpGet)
        HttpEntity entity = result.getEntity() // get result
        String responseBody = EntityUtils.toString(entity); // extract response body
        def jsonSlurper = new JsonSlurper() // for parsing response
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
    Map postRequest(String url, Map data,String authString)
    {
        HttpClient client = HttpClientBuilder.create().build()
        HttpPost  request = new HttpPost(url)
        request.setHeader("Authorization",authString)
        request.setHeader("Content-Type",'application/json')
        List<NameValuePair> postParams = new ArrayList<NameValuePair>()

        //loop through the data sent and add them to the request body
        data.each {key,value->
            postParams.add(new BasicNameValuePair(key,value))
        }

        request.setEntity(new UrlEncodedFormEntity(postParams))
        CloseableHttpResponse response = client.execute(request)
        HttpEntity entity   = response.getEntity
        String responseBody = EntityUtils.toString(entity)
        def jsonSlurper = new JsonSlurper() // for parsing response
        def responseMap = jsonSlurper.parseText(responseBody); // parse into json object
        response.close()

        return responseMap as Map

    }

    /**
     * List All Transactions
     * @return
     */
    Map listTransactions(){
        String authString = "Bearer" +secretKey
        String url = endPoint+"/transaction/"

        return this.getRequest(url,authString)
    }

    /**
     * Fetch a particular transaction
     * @param id  : identifier of transaction to fetch
     * @return
     */
    Map fetchTransaction(int id){
        String authString = "Bearer" +secretKey
        String url = endPoint+"/transaction/"+id

        return this.getRequest(url,authString)
    }

    /**
     * Create a customer
     * @param params
     * @return
     */
    Map createCustomer(params){
        String authString = "Bearer" +secretKey
        String url = endPoint+"/customer"

        Map reqParams = [
                email        : params.email,
                first_name   : params.first_name,
                last_name    : params.last_name,
                phone        :params.phone,
                metadata     : params.metadata
        ]
        return this.postRequest(url,reqParams,authString)
    }

    /**
     *List all customers on your Paystack account
     * @return
     */
    Map getAllCustomers(){
        String authString = 'Bearer'+secretKey
        String url        = endPoint+'/customer'

        return this.getRequest(url,authString)
    }

    /**
     * Return all plans on your paystack account
     * @return
     */
    Map getAllPlans(){
        String authString = 'Bearer'+secretKey
        String url        = endPoint+'/plan'

        return this.getRequest(url,authString)
    }

    /**
     * Return all transactions on you paystack account
     * @return
     */
    Map getAllTransactions(){
        String authString = 'Bearer'+secretKey
        String url        = endPoint+'/transaction'

        return this.getRequest(url,authString)
    }

    /**
     *
     * @return
     */
    Map createPlan(params){
        String authString = 'Bearer'+secretKey
        String url        = endPoint+'/plan'

        Map reqParams = [
                name           : params.name,
                description    : params.description,
                amount         : params.amount,
                send_invoices  : params.send_invoices,
                send_sms       : params.send_sms,
                currency       : params.currency,
        ]

        return postRequest(url,reqParams,authString)
    }
}
