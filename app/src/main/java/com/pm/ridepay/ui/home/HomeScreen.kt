package com.pm.ridepay.ui.home

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pm.ridepay.data.model.RideHistoryItem
import com.pm.ridepay.data.model.RideStatus
import com.pm.ridepay.util.formatMoney
import com.pm.ridepay.util.formatTime
import com.pm.ridepay.util.label

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onStartRide: () -> Unit,
    onResumeRide: (Long)-> Unit,
    onReviewPayments: () -> Unit,
    onOpenRide: (Long) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val error = state.errorMessage
    val activeRide = state.activeRide

    LifecycleEventEffect((Lifecycle.Event.ON_RESUME)) {
        viewModel.load()
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Today's Summary",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onLogout) { Text("Log out") }
        }
        Text(
            "Collected today: ${formatMoney(state.totalCollected)}",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(16.dp))

        activeRide?.let { ride ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Ride in progress", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${formatMoney(ride.collectedAmount)} of ${formatMoney(ride.targetAmount)} collected",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { onResumeRide(ride.rideId) }) { Text("Resume") }
                }
            }
            Spacer(Modifier.height(16.dp))
        }


        if (state.unassignedCount > 0) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "${state.unassignedCount} unassigned payment(s)",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Money arrived that is not part of any ride.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = onReviewPayments) { Text("Review") }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when {
                state.isLoading && state.todaysRides.isEmpty() && activeRide == null ->
                    CircularProgressIndicator(Modifier.align(Alignment.Center))

                error != null && state.todaysRides.isEmpty() ->
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(error, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = viewModel::load) { Text("Try again") }
                    }

                state.todaysRides.isEmpty() ->
                    Text(
                        "No finished rides yet today.",
                        modifier = Modifier.align(Alignment.Center)
                    )

                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.todaysRides, key = { it.rideId }) { ride ->
                        RideCard(ride, onClick ={onOpenRide(ride.rideId)})
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onStartRide,
            enabled = activeRide == null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Ride")
        }
    }
}

@Composable
private fun RideCard(ride: RideHistoryItem) {
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
                    "${formatMoney(ride.collectedAmount)} of ${formatMoney(ride.targetAmount)}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(ride.status.label(), style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                formatTime(ride.startedAt),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun RideCard(ride: RideHistoryItem, onClick: () -> Unit){
    Card(modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick=onClick)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${formatMoney(ride.collectedAmount)} of ${formatMoney(ride.targetAmount)}",
                    style= MaterialTheme.typography.titleMedium
                )
                Text(ride.status.label(), style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                formatTime(ride.startedAt),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
        }
    }
}