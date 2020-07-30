package paystack.grails

enum PaystackPlanInterval {

    HOURLY('hourly'), DAILY('daily'), WEEKLY('weekly'), MONTHLY('monthly'),
    BIANNUALLY('biannually'), ANNUALLY('annually')

    private final String plan

    PaystackPlanInterval(String plan) {
        this.plan = plan
    }
}