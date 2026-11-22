package com.pm.ridepay.ui.unassigned

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.pm.ridepay.data.model.PaymentDto
import com.pm.ridepay.util.formatMoney
import com.pm.ridepay.util.formatTime


@Composable
fun UnassignedPaymentsScreen(
    viewModel: UnassignedPaymentsViewModel,
    onStartRideFor: (Long) -> Unit,
    onBack: ()-> Unit,
    modifier: Modifier= Modifier
){
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var ignoreCandidate by rememberSaveable{ mutableStateOf<Long?>(null) }
    val error = state.errorMessage
    val hasActiveRide = state.activeRide!=null

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.load()
    }

    Column(
        modifier= Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Unassigned payments", style= MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            "These arrived when no ride was waiting for them.",
            style= MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        ){
            when{
                state.isLoading && state.payments.isEmpty() ->
                    CircularProgressIndicator(Modifier.align(Alignment.Center))

                error!=null && state.payments.isEmpty() ->
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(error, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = viewModel::load) {Text("Try again") }
                    }
                state.payments.isEmpty() ->
                    Text("No unassigned payments.", Modifier.align(Alignment.Center))
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.payments, key = {it.id}){
                        payment -> PaymentCard(
                            payment = payment,
                            hasActiveRide = hasActiveRide,
                            busy = state.busyPaymentId!=null,
                            onAdd = {viewModel.addToCurrentRide(payment.id)},
                            onStartRide = {onStartRideFor(payment.id)},
                            onIgnore = {ignoreCandidate = payment.id}
                        )
                    }
                }
            }
        }

        state.message?.let { message ->
            Spacer(Modifier.height(8.dp))
            Text(message, style = MaterialTheme.typography.bodySmall)
        }
        TextButton(onClick = onBack) {Text("Back") }
    }

    ignoreCandidate?.let { paymentId ->
        AlertDialog(
            onDismissRequest = {ignoreCandidate = null},
            title = { Text("Ignore this payment?") },
            text = {Text("It stays in your record but will not count toward any ride.")},
            confirmButton = {
                TextButton(onClick = {
                    ignoreCandidate = null
                    viewModel.ignore(paymentId)
                }) {Text("Ignore") }
            },
            dismissButton = {
                TextButton(onClick = {ignoreCandidate = null}) { Text("Keep")}
            }
        )
    }
}

@Composable
private fun PaymentCard(
    payment: PaymentDto,
    hasActiveRide: Boolean,
    busy: Boolean,
    onAdd: () -> Unit,
    onStartRide: () -> Unit,
    onIgnore: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "${formatMoney(payment.amount)} at ${formatTime(payment.receivedAt)}",
                style = MaterialTheme.typography.titleMedium
            )
            payment.payerUpiId?.let {
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(12.dp))

            if (hasActiveRide) {
                Button(
                    onClick = onAdd,
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Add to current ride") }
            } else {
                Button(
                    onClick = onStartRide,
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Start a new ride") }
            }
            TextButton(onClick = onIgnore, enabled = !busy) { Text("Ignore") }
        }
    }
}