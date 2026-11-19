package com.pm.ridepay.ui.startride

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pm.ridepay.util.formatMoney


@Composable
fun StartRideScreen(
    viewModel: StartRideViewModel,
    onStarted: (Long)-> Unit,
    onBack: ()-> Unit,
    modifier: Modifier= Modifier
){
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val startedRideId = state.startedRideId
    val total=state.total
    val error=state.errorMessage

    LaunchedEffect(startedRideId) {
        if(startedRideId!=null) onStarted(startedRideId)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Start a ride", style= MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = state.fareText,
            onValueChange = viewModel::onFareChange,
            label = {Text("Fare per person")},
            prefix = {Text("₹ ")},
            isError = state.fareError!=null,
            supportingText = {state.fareError?.let { Text(it) }},
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            enabled = !state.isSubmitting,
            modifier= Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = state.passengersText,
            onValueChange = viewModel::onPassengersChange,
            label = {Text("Number of passengers")},
            isError = state.passengersError!=null,
            supportingText = {state.passengersError?.let { Text(it) }},
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            singleLine = true,
            enabled = !state.isSubmitting,
            modifier= Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))


        Card(modifier= Modifier.fillMaxWidth()) {
            Column(modifier= Modifier.padding(16.dp)) {
                Text("Total needed to collect", style = MaterialTheme.typography.bodyMedium)
                Text(
                    total?.let { formatMoney(it) }?:"-",
                    style= MaterialTheme.typography.headlineSmall
                )
            }
        }

        error?.let {
            message-> Spacer(Modifier.height(16.dp))
            Text(message, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = viewModel::start,
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.isSubmitting) "Starting..." else "Start Ride")
        }
        TextButton(onClick = onBack, enabled = !state.isSubmitting) {
            Text("Back")
        }
    }
}