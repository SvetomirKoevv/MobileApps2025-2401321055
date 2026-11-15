package com.svetomir.spendly.ui.currency

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.svetomir.spendly.R
import com.svetomir.spendly.ui.util.formatMoney

@Composable
fun CurrencyConversionCard(
    totalInEur: Double,
    modifier: Modifier = Modifier,
    viewModel: CurrencyViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.converted_total),
                style = MaterialTheme.typography.labelLarge,
            )
            when (val ratesState = uiState.ratesState) {
                RatesState.Loading -> LoadingRow()

                RatesState.Error -> ErrorContent(onRetry = viewModel::retry)

                is RatesState.Success -> RatesContent(
                    totalInEur = totalInEur,
                    ratesState = ratesState,
                    selectedCurrency = uiState.selectedCurrency,
                    onCurrencySelected = viewModel::selectCurrency,
                )
            }
        }
    }
}

@Composable
private fun LoadingRow() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        Text(stringResource(R.string.loading_rates))
    }
}

@Composable
private fun ErrorContent(onRetry: () -> Unit) {
    Text(
        text = stringResource(R.string.rates_error),
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium,
    )
    TextButton(onClick = onRetry) {
        Text(stringResource(R.string.retry))
    }
}

@Composable
private fun RatesContent(
    totalInEur: Double,
    ratesState: RatesState.Success,
    selectedCurrency: String,
    onCurrencySelected: (String) -> Unit,
) {
    val rate = ratesState.rates[selectedCurrency]
    if (rate != null) {
        Text(
            text = formatMoney(totalInEur * rate, selectedCurrency),
            style = MaterialTheme.typography.headlineSmall,
        )
    }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(ratesState.rates.keys.toList()) { code ->
            val selected = code == selectedCurrency
            FilterChip(
                selected = selected,
                onClick = { onCurrencySelected(code) },
                label = { Text(code) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected,
                    borderColor = MaterialTheme.colorScheme.primary,
                    selectedBorderColor = MaterialTheme.colorScheme.primary,
                    borderWidth = 1.dp,
                    selectedBorderWidth = 1.dp,
                ),
            )
        }
    }
    Text(
        text = stringResource(R.string.rates_date, ratesState.date),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
