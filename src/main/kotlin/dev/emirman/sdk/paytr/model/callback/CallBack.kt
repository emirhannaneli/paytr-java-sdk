package dev.emirman.sdk.paytr.model.callback

import java.math.BigDecimal

/**
 * Data class representing a callback from the PayTR API.
 *
 * @property id The unique identifier of the callback.
 * @property status The status of the callback (e.g., success, failure).
 * @property amount The <strong>'total amount'</strong> involved in the transaction.
 * @property hash The hash used for verifying the callback.
 */
data class CallBack(
    val id: String,
    val status: String,
    val amount: BigDecimal,
    val hash: String
)
