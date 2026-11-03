package com.eriktemiraliev.asmanpay.platform

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSDateFormatterShortStyle
import platform.Foundation.NSDateFormatterStyle
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterDecimalStyle
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.Foundation.NSURLRequestUseProtocolCachePolicy
import platform.Foundation.NSURLSession
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataTaskWithRequest
import platform.Foundation.dateWithTimeIntervalSince1970
import kotlin.coroutines.resume

actual suspend fun httpGetText(url: String, timeoutMillis: Long): String? {
    val nsUrl = NSURL.URLWithString(url) ?: return null
    val request = NSURLRequest.requestWithURL(
        URL = nsUrl,
        cachePolicy = NSURLRequestUseProtocolCachePolicy,
        timeoutInterval = timeoutMillis / 1000.0
    )
    return suspendCancellableCoroutine { continuation ->
        val task = NSURLSession.sharedSession.dataTaskWithRequest(request) { data, response, error ->
            val status = (response as? NSHTTPURLResponse)?.statusCode
            val body = if (error == null && status == 200L && data != null) {
                NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
            } else {
                null
            }
            continuation.resume(body)
        }
        continuation.invokeOnCancellation { task.cancel() }
        task.resume()
    }
}

actual fun formatDecimal(
    value: Double,
    minFractionDigits: Int,
    maxFractionDigits: Int,
    useGrouping: Boolean
): String {
    val formatter = NSNumberFormatter().apply {
        numberStyle = NSNumberFormatterDecimalStyle
        minimumFractionDigits = minFractionDigits.toULong()
        maximumFractionDigits = maxFractionDigits.toULong()
        usesGroupingSeparator = useGrouping
    }
    return formatter.stringFromNumber(NSNumber(double = value)) ?: value.toString()
}

actual fun formatDate(epochMillis: Long): String = formatEpoch(epochMillis, NSDateFormatterNoStyle)

actual fun formatDateTime(epochMillis: Long): String = formatEpoch(epochMillis, NSDateFormatterShortStyle)

private fun formatEpoch(epochMillis: Long, timeStyle: NSDateFormatterStyle): String {
    val formatter = NSDateFormatter().apply {
        dateStyle = NSDateFormatterMediumStyle
        this.timeStyle = timeStyle
    }
    return formatter.stringFromDate(NSDate.dateWithTimeIntervalSince1970(epochMillis / 1000.0))
}
