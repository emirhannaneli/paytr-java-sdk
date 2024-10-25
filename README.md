# PayTR API Integration - JAVA SDK

This Kotlin project provides a comprehensive factory class to facilitate seamless integration with the PayTR payment gateway. It enables creating, sending, and handling payment requests, callback verification, and refund requests to PayTR, with fully customizable configurations. 

## Features

- **Payment Request**: Easily create and send payment requests to PayTR.
- **Callback Verification**: Verify callbacks for successful or failed payments.
- **Refund Requests**: Issue refunds for specified orders.
- **Configuration-based**: Set up via a `PayTRConfig` object.

## Repository

```groovy
repositories {
    maven {
        url = uri("https://repo.emirman.dev")
    }
}
```

## Example
```kotlin
fun main() {
    val config = PayTRConfig(
        merchantId = "merchantId",
        merchantKey = "merchant",
        merchantSalt = "merchantSalt",
        okUrl = "https://example.com/ok",
        failUrl = "https://example.com/fail",
        testMode = true,
        debugMode = true
    )

    val pay = Pay(
        id = "id-123",
        ip = "1.1.1.1",
        email = "mail@mail.com",
        amount = BigDecimal(100),
        card = Pay.Card(
            name = "PAYTR TEST",
            number = "4355084355084358",
            month = "12",
            year = "30",
            cvv = "000"
        ),
        billing = Pay.Billing(
            name = "John",
            surname = "Doe",
            phone = "1234567890",
            address = "123 Main St"
        ),
        items = listOf(
            Pay.Item(
                title = "Item",
                price = BigDecimal(100),
                quantity = 1
            )
        )
    )

    RequestFactory.config = config
    val html = RequestFactory.pay(pay)
}
```
