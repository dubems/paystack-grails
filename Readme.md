
# Grails Paystack Plugin

## A Grails plugin to allow communication to paystack API

## Installation

```
```
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

## TODO
- [ ] Complete All Api calls
- [X] Write Unit tests


## CONTRIBUTING
- Fork the repository, make necessary changes and send the PR.