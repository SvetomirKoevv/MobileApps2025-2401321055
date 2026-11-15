package com.svetomir.spendly.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatAmount(amount: Double): String = String.format(Locale.US, "%.2f €", amount)

fun formatDate(millis: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))

fun formatMoney(amount: Double, currencyCode: String): String =
    String.format(Locale.US, "%.2f %s", amount, currencyCode)
