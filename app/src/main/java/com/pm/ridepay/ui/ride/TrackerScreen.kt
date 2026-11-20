package com.pm.ridepay.ui.ride

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.pm.ridepay.data.model.PaymentDto
import com.pm.ridepay.data.model.RideStatus
import com.pm.ridepay.data.model.RideStatusResponse
import com.pm.ridepay.util.formatMoney
import com.pm.ridepay.util.formatTimeWithSeconds
import kotlinx.coroutines.delay
import java.math.RoundingMode


@Composable
fun TrackerScreen(
    viewModel: RideStatusViewModel,
    onCompleted: () -> Unit,
    onCancelled: () -> Unit,
    modifier: Modifier= Modifier
){
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val ride=state.ride
    var showCancelDialog by rememberSaveable { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

//    This will ask the backend every 3 seconds, but only while the screen is visible
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
            while (true){
                viewModel.refresh()
                delay(3_000)
            }
        }
    }


    LaunchedEffect(ride?.status) {
        when (ride?.status) {
            RideStatus.COMPLETED -> onCompleted()
            RideStatus.CANCELLED -> onCancelled()
            else -> Unit
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Payments", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        if (ride == null) {
            val error = state.errorMessage
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (error != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(error, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = viewModel::refresh) { Text("Try again") }
                    }
                } else {
                    CircularProgressIndicator()
                }
            }
        } else {
            RideProgress(ride)
            if (state.errorMessage != null) {
                Spacer(Modifier.height(4.dp))
                Text("Reconnecting...", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(24.dp))
            Text("List of payments", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (ride.payments.isEmpty()) {
                    Text(
                        "Waiting for the first payment...",
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(
                            ride.payments.sortedByDescending { it.id },
                            key = { it.id }
                        ) { payment -> PaymentRow(payment) }
                    }
                }
            }
        }

        state.actionMessage?.let { message ->
            Spacer(Modifier.height(8.dp))
            Text(message, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(
            onClick = { showCancelDialog = true },
            enabled = ride != null && !state.isCancelling,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.isCancelling) "Cancelling..." else "Cancel ride")
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel this ride?") },
            text = { Text("Payments already received stay recorded.") },
            confirmButton = {
                TextButton(onClick = {
                    showCancelDialog = false
                    viewModel.cancelRide()
                }) { Text("Cancel ride") }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("Keep going") }
            }
        )
    }
}

@Composable
private fun RideProgress(ride: RideStatusResponse) {
    val fraction = if (ride.targetAmount.signum() > 0) {
        ride.collectedAmount
            .divide(ride.targetAmount, 4, RoundingMode.HALF_UP)
            .toFloat()
            .coerceIn(0f, 1f)
    } else {
        0f
    }
    val animated by animateFloatAsState(targetValue = fraction, label = "progress")

    LinearProgressIndicator(
        progress = { animated },
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
    )
    Spacer(Modifier.height(8.dp))
    Text(
        "${formatMoney(ride.collectedAmount)} / ${formatMoney(ride.targetAmount)} collected",
        style = MaterialTheme.typography.titleLarge
    )
    Text(
        "About ${ride.estimatedPaid} of ${ride.passengerCount} passengers paid",
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun PaymentRow(payment: PaymentDto) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${formatMoney(payment.amount)} received",
                    style = MaterialTheme.typography.titleMedium
                )
                payment.payerUpiId?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
            }
            Text(
                formatTimeWithSeconds(payment.receivedAt),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
        }
    }
}