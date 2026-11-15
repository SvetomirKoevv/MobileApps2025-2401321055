package com.svetomir.spendly

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Starts instrumented tests with Hilt's test application instead of SpendlyApp,
 * so tests can replace real modules (for example the database) with test versions.
 */
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        classLoader: ClassLoader?,
        className: String?,
        context: Context?,
    ): Application =
        super.newApplication(classLoader, HiltTestApplication::class.java.name, context)
}
