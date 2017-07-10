package com.paystack

import grails.transaction.Transactional
import grails.util.Environment
import groovy.json.JsonSlurper
import org.apache.http.client.CredentialsProvider
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.HttpClientBuilder
import org.springframework.http.HttpEntity
import sun.net.www.http.HttpClient

/**
 * @athor Nriagu Chidubem
 * @email nriagudubem@gmail.com
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
        if(Environment.current.name == "${Environment.DEVELOPMENT}".toLowerCase() || Environment.current.name == "${Environment.TEST}".toLowerCase()){
            secretKey =  grailsApplication.config.paystack.testSecretKey
        }
        else{
             secretKey = grailsApplication.config.paystack.liveSecretKey
        }

    }

    /**
     * Set the public key for the API Request
     *  When app in dev, use the testPublicKey else use the livePublicKey
     */
    void setPublicKey()
    {
        if(Environment.current.name == "${Environment.DEVELOPMENT}".toLowerCase() || Environment.current.name == "${Environment.TEST}".toLowerCase()){
            publicKey =  grailsApplication.config.paystack.testPublicKey
        }
        else{
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
        return this.makePaymentRequest(params)
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
        if(!params.amount || !params.emamil)
        {
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
        Map reqParams = [
                amount:params.amount,
                email:params.email,
                reference:this.generateTrxnRef(),
                plan:params.plan,
                first_name:params.first_name,
                last_name:params.last_name,
                metadata:params.metadata,
                callback_url:params.callback_url

        ]
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

     def  callAPIEndpoint(String url, Map body,String method)
    {

    }

    /**
     * Verify a transaction
     * @param reference : transaction reference
     * @return : response from paystack
     */
     Map verify(String reference) {

         String authString = "Bearer" +secretKey
         String contentType = "application/json"
         String url = endPoint+"/transaction/verify/${reference}"
        
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
}
