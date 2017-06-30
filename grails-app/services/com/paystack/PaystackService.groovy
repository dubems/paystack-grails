package com.paystack

import grails.transaction.Transactional

/**
 * @athor Nriagu Chidubem
 * @email nriagudubem@gmail.com
 */
@Transactional
class PaystackService {

    /**
     * PaystackService Constructor
     */
     PaystackService() {

    }
    /**
     * Get authorization url from paystack
     * The authorization url is to redirect to paystack for payment
     * @return
     */
    def getAuthorizationUrl()
    {
        return this.makePaymentRequest()
    }

    /**
     * Redirect to the paystack url
     */
    def redirectNow()
    {
        String url = this.getAuthorizationUrl()

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
     */
    def makePaymentRequest()
    {
        Map reqParams = [
            amount:params
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

    /**
     * Verify a transaction
     * @param reference : transaction reference
     * @return : response from paystack
     */
    Map verify(String reference) {
        String authString = ""
        if(Environment.current.name == "${Environment.DEVELOPMENT}".toLowerCase() || Environment.current.name == "${Environment.TEST}".toLowerCase()){
            authString = "Bearer " + grailsApplication.config.paystack.testSecretKey
        }
        else{
            authString = "Bearer " + grailsApplication.config.paystack.liveSecretKey
        }
        
        String contentType = "application/json"
        String url = "${grailsApplication.config.paystack.endpoint}/transaction/verify/${reference}"
        
        CredentialsProvider provider = new BasicCredentialsProvider()  
        
        HttpClient client = HttpClientBuilder.create().build()

        HttpGet httpGet = new HttpGet(url)
        httpGet.addHeader("Authorization",authString)


        CloseableHttpResponse  result = client.execute(httpGet)
        HttpEntity entity = result.getEntity() // get result
        String responseBody = EntityUtils.toString(entity); // extract response body
        def jsonSlurper = new JsonSlurper() // for parsing response
        def responseMap = jsonSlurper.parseText(responseBody); // parse into json object
        
        result.close()
        return responseMap as Map
    }
}
