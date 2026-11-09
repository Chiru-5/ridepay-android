package com.pm.ridepay.ui.onboarding

import android.R.id.message
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pm.ridepay.data.model.DriverStatusResponse
import com.pm.ridepay.data.model.VerificationStatus


@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onFinished: (DriverStatusResponse)-> Unit,
    onLogout: ()-> Unit,
    modifier: Modifier= Modifier
){
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val completed=state.completedDriver


    when{
        completed!=null -> VerifiedContent(
            onContinue={onFinished(completed)},
            modifier=modifier
        )
        state.isSubmitting -> VerificationProgress(modifier)
        else-> DetailsForm(state, viewModel, onLogout, modifier)
    }
}


@Composable
private fun DetailsForm(
    state: OnboardingUiState,
    viewModel: OnboardingViewModel,
    onLogout: () -> Unit,
    modifier: Modifier
){
    Column(
        modifier=modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Your details", style= MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = state.name,
            onValueChange = viewModel::onNameChange,
            label = {Text("Full Name")},
            isError = state.nameError!=null,
            supportingText = {state.nameError?.let { Text(it) }},
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            modifier=Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))


        OutlinedTextField(
            value = state.bankAccountNumber,
            onValueChange = viewModel::onAccountChange,
            label = { Text("Account Number") },
            isError = state.accountError != null,
            supportingText = { state.accountError?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))


        OutlinedTextField(
            value = state.ifscCode,
            onValueChange = viewModel::onIfscChange,
            label = {Text("IFSC Code")},
            isError = state.ifscError!=null,
            supportingText = {state.ifscError?.let { Text(it) }},
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            modifier=Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))



        OutlinedTextField(
            value = state.vehicleNumber,
            onValueChange = viewModel::onVehicleChange,
            label = {Text("Vehicle Number (optional)")},
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Done
            ),
            singleLine = true,
            modifier=Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))


        Text(
            "Used to set up your payment QR.",
            style= MaterialTheme.typography.bodySmall
        )
        state.errorMessage?.let {
            message-> Spacer(Modifier.height(16.dp))
            Text(message, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(24.dp))
        Button(onClick = viewModel::submit, modifier=Modifier.fillMaxWidth()) {
            Text("Submit")
        }
        TextButton(onClick = onLogout) {
            Text("Log out")
        }
    }
}


@Composable
private fun VerificationProgress(modifier: Modifier){
    Column(
        modifier=modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(24.dp))
        Text("Verification in progress", style=MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text("Setting up your payment QR.....", style= MaterialTheme.typography.bodyMedium)
    }
}


@Composable
private fun VerifiedContent(onContinue: ()-> Unit, modifier: Modifier){
    Column(
        modifier=modifier.fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        Text("You're verified!", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("Your payment QR is next.", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onContinue) { Text("Continue") }
    }
}