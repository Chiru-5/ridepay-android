package com.pm.ridepay.ui.login

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle


@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoggedIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current

    LaunchedEffect(state.loggedIn) {
        if (state.loggedIn) onLoggedIn()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("RidePay", style= MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(4.dp))
        Text("Driver login", style= MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(32.dp))

        when(state.step){
            LoginStep.PHONE->{
                OutlinedTextField(
                    value = state.phone,
                    onValueChange = viewModel::onPhoneChange,
                    label = {Text("Mobile number")},
                    prefix = {Text("+91 ")},
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    enabled = !state.isLoading,
                    modifier= Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { activity?.let { viewModel.sendOtp(it) } },
                    enabled = !state.isLoading && activity != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Send OTP")
                }
            }

            LoginStep.OTP->{
                Text(
                    "Enter the 6-digit code sent to +91 ${state.phone}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = state.otp,
                    onValueChange = viewModel::onOtpChange,
                    label = {Text("6-digit code")},
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    enabled = !state.isLoading,
                    modifier=Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = viewModel::verifyOtp,
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Verify and log in")
                }
                TextButton(
                    onClick = viewModel::backToPhone,
                    enabled = !state.isLoading
                ) {
                    Text("Change number")
                }
            }
        }
        if(state.isLoading){
            Spacer(Modifier.height(24.dp))
            CircularProgressIndicator()
        }

        state.errorMessage?.let {
            message-> Spacer(Modifier.height(16.dp))
            Text(message, color= MaterialTheme.colorScheme.error)
        }
    }
}