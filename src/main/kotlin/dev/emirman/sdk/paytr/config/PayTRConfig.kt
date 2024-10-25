package dev.emirman.sdk.paytr.config

/**
 * Configuration data class for PayTR integration.
 *
 * @property merchantId The merchant ID provided by PayTR.
 * @property merchantKey The merchant key provided by PayTR.
 * @property merchantSalt The merchant salt provided by PayTR.
 * @property okUrl The URL to redirect to upon successful payment.
 * @property failUrl The URL to redirect to upon failed payment.
 * @property testMode Indicates if the test mode is enabled.
 * @property debugMode Indicates if the debug mode is enabled.
 */
data class PayTRConfig(
    val merchantId: String,
    val merchantKey: String,
    val merchantSalt: String,
    val okUrl: String,
    val failUrl: String,
    val testMode: Boolean = false,
    val debugMode: Boolean = false
)