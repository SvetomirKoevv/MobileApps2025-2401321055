package com.svetomir.spendly.ui.detail

import android.content.Context
import android.content.Intent
import com.svetomir.spendly.R
import com.svetomir.spendly.data.local.Expense
import com.svetomir.spendly.ui.util.formatAmount
import com.svetomir.spendly.ui.util.formatDate

fun buildShareText(expense: Expense): String = buildString {
    append("${expense.title}: ${formatAmount(expense.amount)}")
    append("\n${expense.category.label} · ${formatDate(expense.date)}")
    if (expense.note.isNotBlank()) {
        append("\nNote: ${expense.note}")
    }
}

fun shareExpense(context: Context, expense: Expense) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, buildShareText(expense))
    }
    val chooser = Intent.createChooser(sendIntent, context.getString(R.string.share_expense))
    context.startActivity(chooser)
}
