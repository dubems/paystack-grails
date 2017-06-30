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
}
