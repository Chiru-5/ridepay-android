package com.pm.ridepay.ui.ride

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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pm.ridepay.data.model.RideStatusResponse
import com.pm.ridepay.util.formatDateTime
import com.pm.ridepay.util.formatDuration
import com.pm.ridepay.util.formatMoney
import com.pm.ridepay.util.label

@Composable
fun RideDetailsScreen(
    viewModel: RideStatusViewModel,
    onBack: () -> Unit,
    modifier: Modifier= Modifier
){
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val ride = state.ride
    val error = state.errorMessage

    LaunchedEffect(Unit) {viewModel.refresh() }

    Column(
        modifier= Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Ride details", style= MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        when{
            ride!=null -> RideDetailsContent(ride, Modifier.weight(1f))

            error!=null -> Box(
                modifier= Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ){
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = viewModel::refresh) {Text("Try again") }
                }
            }
            else -> Box(
                modifier= Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ){
                CircularProgressIndicator()
            }
        }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onBack, modifier=Modifier.fillMaxWidth()) { Text("Back")}
    }
}

@Composable
private fun RideDetailsContent(ride: RideStatusResponse, modifier: Modifier) {
    val payments = ride.payments.sortedBy { it.id }

    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { SummaryCard(ride) }
        item {
            Spacer(Modifier.height(8.dp))
            Text("Payments (${payments.size})", style = MaterialTheme.typography.titleMedium)
        }
        if (payments.isEmpty()) {
            item { Text("No payments for this ride.", style = MaterialTheme.typography.bodyMedium) }
        } else {
            items(payments, key = { it.id }) { payment -> PaymentRow(payment) }
        }
    }
}

@Composable
private fun SummaryCard(ride: RideStatusResponse) {
    val fare = ride.farePerPerson
    val duration = formatDuration(ride.startedAt, ride.completedAt)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(ride.status.label(), style = MaterialTheme.typography.labelLarge)
            Text(
                "${formatMoney(ride.collectedAmount)} of ${formatMoney(ride.targetAmount)} collected",
                style = MaterialTheme.typography.titleLarge
            )
            if (fare != null) {
                Text("${ride.passengerCount} passengers × ${formatMoney(fare)}")
            }
            if (ride.startedAt != null) {
                Text("Started: ${formatDateTime(ride.startedAt)}")
            }
            if (ride.completedAt != null) {
                Text("Ended: ${formatDateTime(ride.completedAt)}")
            }
            if (duration != null) {
                Text("Duration: $duration")
            }
        }
    }
}
