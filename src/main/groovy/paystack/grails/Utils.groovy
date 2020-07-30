package paystack.grails

trait Utils {

    static String mapQueryParams(final Map<String, String> queryParams) {
        queryParams.collect {"${it.key}=${it.value}"}.join("&")
    }

    /**
     * Generate a Unique Transaction reference
     * this is used to verify the transaction
     * @return
     */
    // Change this implementation
    static String generateTransactionRef() {
        List numPool = 0..9
        List alphaPoolCapital = 'A'..'Z'
        List alphaPoolSmall = 'a'..'z'
        List allPool = (numPool + alphaPoolCapital + alphaPoolSmall)
        Collections.shuffle(allPool)
        List<String> trxnReference = allPool.subList(0, 32)
        return trxnReference.join("")
    }
}