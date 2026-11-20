package com.pm.ridepay.ui.ride

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pm.ridepay.util.formatMoney
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle

@Composable
fun RideCompleteScreen(
    viewModel: RideStatusViewModel,
    onDone: () -> Unit,
    modifier: Modifier= Modifier
){
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val ride = state.ride
    val error = state.errorMessage

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Column(
        modifier=modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = "Ride completed",
            modifier= Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))

        when{
            ride!=null ->{
                Text(
                    "${formatMoney(ride.collectedAmount)} collected",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${ride.payments.size} payments received",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            error!=null -> Text(error, color = MaterialTheme.colorScheme.error)
            else -> CircularProgressIndicator()
        }
        Spacer(Modifier.height(32.dp))
        Button(onClick = onDone) {
            Text("Back to Home")
        }
    }
}