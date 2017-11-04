
# Grails Paystack Plugin

## A Grails plugin to allow communication to paystack API

## Installation
Add the below line to your build.gradle file

```compile 'org.grails.plugins:paystack-grails:0.3.2'```
## Configuration
```yml
Include Paystack keys(in application.yml) gotten from your dashboard as follows
paystack: 
    liveSecretKey: '${PAYSTACK_LIVE_SECRET_KEY}'
    testSecretKey: '${PAYSTACK_TEST_SECRET_KEY}'
    livePublicKey: '${PAYSTACK_LIVE_PUBLIC_KEY}'
    testPublicKey: '${PAYSTACK_TEST_PUBLIC_KEY}'
    endpoint:      'https://api.paystack.co'
```

* Note:Make sure to have your webhook registered in paystack dashboard.
  eg: http://paystack.dev/test/handlePaystackCallback
where:
    * "test" is the controller name
    * "handlePayStackCallback" is the method name

## Usage

* Make Payment Request
* params
    * _email_ (required)
    * _amount_ (required)
    * _plan_
    * _first_name_
    * _last_name_
    * _metadata_
    * _callback_url_
  
```groovy
class TestController {

    PaystackService paystackService

    def index() { }

    def makePaymentRequest(){
        String authUrl =  paystackService.validate(params).getAuthUrl(params)
        redirect(url:authUrl)
    }
    }
```
* Handle Paystack callback
* params(paystack calls ur method with a reference parameter)

```groovy

    def handlePaystackCallback(){
        def paymentDetails =  paystackService.getPaymentData(params)
         println($paymentDetails)
        // Now you have the payment details,
        // you can store the authorization_code in your db to allow for recurrent subscriptions
        // you can then redirect or do whatever you want
    }
```

* Other useful methods that implement Paystack endpoints can be found below
* NB: Methods not listed below can be found in the PaystackService Class

```groovy

    // Injecting PaystackService 
    PaystackService paystackService
    
    /**
     * Return all customers on your Paystack account
     * @returns Map
     */
    paystackService.getAllCustomers()
    
     /**
     * Return all transactions on Your Paystack account
     * @returns Map
     */
    paystackService.listTransactions()

     /**
     * Fetch a particular transaction
     * @param  int id : identifier of transaction to fetch
     * @return Map
     */
    paystackService.fetchTransaction(id)
    
    /**
     * Return a single customer given its id
     * @param customerId
     * @return Map
     */
    paystackService.fetchCustomer(customerId)
    
    /**
     * Return all plans on your paystack account
     * @return Map
     */
    paystackService.getAllPlans()
        
    /**
     * Get a particular plan given the plan id
     * @param planId
     * @return Map
     */
    paystackService.fetchPlan(planId)
    
```

## TODO
- [ ] Complete All Api calls
- [X] Write Unit tests


## CONTRIBUTING
- Fork the repository, make necessary changes and send the PR.