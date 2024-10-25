package dev.emirman.sdk.paytr.model.pay

import java.math.BigDecimal
import java.util.Currency
import java.util.Locale

/**
 * Data class representing a payment request to the PayTR API.
 *
 * @property id The unique identifier of the payment.
 * @property ip The IP address of the user making the payment.
 * @property email The email address of the user making the payment.
 * @property amount The total amount of the payment.
 * @property installment The number of installments for the payment.
 * @property currency The currency of the payment.
 * @property locale The locale of the user making the payment.
 * @property non3d Indicates if the payment is non-3D secure.
 * @property card The card details used for the payment.
 * @property billing The billing details of the user.
 * @property items The collection of items included in the payment.
 */
data class Pay(
    val id: String,
    val ip: String,
    val email: String,
    val amount: BigDecimal,
    val installment: Int = 0,
    val currency: Currency = Currency.getInstance("TRY"),
    val locale: Locale = Locale.forLanguageTag("tr"),
    val non3d: Boolean = false,
    val card: Card,
    val billing: Billing,
    val items: Collection<Item>,
) {

    /**
     * Data class representing an item in the payment.
     *
     * @property title The title of the item.
     * @property price The price of the item.
     * @property quantity The quantity of the item.
     */
    data class Item(
        val title: String,
        val price: BigDecimal,
        val quantity: Int,
    )

    /**
     * Data class representing the card details used for the payment.
     *
     * @property name The name on the card.
     * @property number The card number.
     * @property month The expiry month of the card.
     * @property year The expiry year of the card.
     * @property cvv The CVV code of the card.
     */
    data class Card(
        val name: String = "",
        val number: String,
        val month: String,
        val year: String,
        val cvv: String,
    )

    /**
     * Data class representing the billing details of the user.
     *
     * @property name The first name of the user.
     * @property surname The surname of the user.
     * @property phone The phone number of the user.
     * @property address The address of the user.
     */
    data class Billing(
        val name: String,
        val surname: String,
        val phone: String,
        val address: String,
    )
}