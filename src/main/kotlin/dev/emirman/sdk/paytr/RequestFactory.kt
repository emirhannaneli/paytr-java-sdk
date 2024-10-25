package dev.emirman.sdk.paytr

import com.fasterxml.jackson.databind.ObjectMapper
import dev.emirman.sdk.paytr.config.PayTRConfig
import dev.emirman.sdk.paytr.model.callback.CallBack
import dev.emirman.sdk.paytr.model.pay.Pay
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.math.BigDecimal
import java.nio.charset.StandardCharsets
import java.text.DecimalFormat
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Factory class for creating and handling requests to the PayTR API.
 * @property client The HTTP client to be used for sending requests.
 * @property mapper The object mapper to be used for serializing and deserializing JSON.
 * @property config The PayTR configuration. (Required)
 */
class RequestFactory {

    companion object {
        private const val BASE_URL = "https://www.paytr.com/odeme"
        private val df = DecimalFormat("#.##")

        var client = OkHttpClient()
        var mapper = ObjectMapper()

        lateinit var config: PayTRConfig

        /**
         * Creates and sends a payment request to the PayTR API.
         *
         * @param pay The payment details.
         * @return The response from the PayTR API.
         */
        fun pay(pay: Pay): String {
            val basket = mapper.writeValueAsString(pay.items.map { item ->
                listOf(item.title, item.price, item.quantity)
            })

            val nonHashed = """
            |${config.merchantId}
            |${pay.ip}
            |${pay.id}
            |${pay.email}
            |${df.format(pay.amount)}
            |card
            |${pay.installment}
            |${if (pay.currency.currencyCode == "TRY") "TL" else pay.currency.currencyCode}
            |${if (config.testMode) "1" else "0"}
            |${if (pay.non3d) "1" else "0"}
        """.trimMargin().replace("\n", "")

            val charset = StandardCharsets.UTF_8
            val mac = mac()
            mac.update(nonHashed.toByteArray(charset))
            val hashed = mac.doFinal((config.merchantSalt).toByteArray(charset))
            val token = Base64.getEncoder().encodeToString(hashed)

            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("merchant_id", config.merchantId)
                .addFormDataPart("merchant_ok_url", config.okUrl)
                .addFormDataPart("merchant_fail_url", config.failUrl)
                .addFormDataPart("test_mode", if (config.testMode) "1" else "0")
                .addFormDataPart("debug_on", if (config.debugMode) "1" else "0")
                .addFormDataPart("user_ip", pay.ip)
                .addFormDataPart("merchant_oid", pay.id)
                .addFormDataPart("email", pay.email)
                .addFormDataPart("payment_amount", df.format(pay.amount))
                .addFormDataPart("payment_type", "card")
                .addFormDataPart("installment_count", pay.installment.toString())
                .addFormDataPart("no_installment", pay.installment.toString())
                .addFormDataPart("max_installment", pay.installment.toString())
                .addFormDataPart("currency", if (pay.currency.currencyCode == "TRY") "TL" else pay.currency.currencyCode)
                .addFormDataPart("lang", pay.locale.language)
                .addFormDataPart("non_3d", if (pay.non3d) "1" else "0")
                .addFormDataPart("cc_owner", pay.card.name)
                .addFormDataPart("card_number", pay.card.number)
                .addFormDataPart("expiry_month", pay.card.month)
                .addFormDataPart("expiry_year", pay.card.year)
                .addFormDataPart("cvv", pay.card.cvv)
                .addFormDataPart("user_name", "${pay.billing.name} ${pay.billing.surname}")
                .addFormDataPart("user_address", pay.billing.address)
                .addFormDataPart("user_phone", pay.billing.phone)
                .addFormDataPart("user_basket", basket)
                .addFormDataPart("paytr_token", token)
                .build()

            val request = Request.Builder().url(BASE_URL).post(body).build()

            return handleRequest(request)
        }

        /**
         * Handles callback verification from the PayTR API.
         *
         * @param callback The callback details.
         * @return True if the callback is verified successfully, false otherwise.
         */
        fun callback(callback: CallBack): Boolean {
            val nonHashed = """
            |${callback.id}
            |${config.merchantSalt}
            |${callback.status}
            |${callback.amount}
        """.trimMargin().replace("\n", "")

            val charset = StandardCharsets.UTF_8
            val mac = mac()
            val hashed = mac.doFinal((nonHashed).toByteArray(charset))
            val token = Base64.getEncoder().encodeToString(hashed)

            return callback.status == "success" && token == callback.hash
        }

        /**
         * Creates and sends a refund request to the PayTR API.
         *
         * @param oid The order ID.
         * @param amount The amount to be refunded.
         * @param reference An optional reference number.
         * @return True if the refund is successful, false otherwise.
         */
        fun refund(oid: String, amount: BigDecimal, reference: String? = null): Boolean {
            val nonHashed = """
            |${config.merchantId}
            |$oid
            |$amount
        """.trimMargin().replace("\n", "")

            val token = hash(nonHashed)

            val builder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("merchant_id", config.merchantId)
                .addFormDataPart("merchant_oid", oid)
                .addFormDataPart("return_amount", df.format(amount))
                .addFormDataPart("paytr_token", token)

            reference?.let { builder.addFormDataPart("reference_no", it) }
            val body = builder.build()

            val request = Request.Builder().url("$BASE_URL/iade").post(body).build()

            var response = handleRequest(request)
            val json = mapper.readTree(response)
            val status = json.get("status").asText()

            return status == "success"
        }

        private fun handleRequest(request: Request): String {
            lateinit var body: String
            runCatching {
                val response = client.newCall(request).execute()
                body = response.body?.string() ?: throw Exception("Response body is null")
                response.close()
            }.onFailure {
                throw it
            }
            return body
        }

        private fun mac(): Mac {
            val mac = Mac.getInstance("HmacSHA256")
            val charset = StandardCharsets.UTF_8
            val secret = SecretKeySpec(config.merchantKey.toByteArray(charset), "HmacSHA256")
            mac.init(secret)
            return mac
        }

        private fun hash(nonHashed: String): String {
            val charset = StandardCharsets.UTF_8
            val mac = mac()
            mac.init(SecretKeySpec(config.merchantKey.toByteArray(charset), "HmacSHA256"))
            mac.update(nonHashed.toByteArray(charset))
            val hashed = mac.doFinal((config.merchantSalt).toByteArray(charset))
            return Base64.getEncoder().encodeToString(hashed)
        }
    }
}