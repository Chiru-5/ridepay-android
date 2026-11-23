package com.pm.ridepay.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pm.ridepay.data.model.DriverProfileResponse
import com.pm.ridepay.data.model.VerificationStatus


@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onViewQr: ()-> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier= Modifier
){
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val profile = state.profile
    val error = state.errorMessage
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }



    Column(
        modifier=modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text("My profile", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))


        when{
            profile!=null -> ProfileDetails(profile)

            error!=null -> Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(error, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = viewModel::load) {Text("Try again") }
            }
            else -> Box(modifier=Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
                CircularProgressIndicator()
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(onClick = onViewQr, modifier=Modifier.fillMaxWidth()) {
            Text("View payment QR")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = {showLogoutDialog = true},
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Log out")
        }
        TextButton(onClick = onBack) {Text("Back") }
    }

    // If user clicks on log out it shows a dialog box to confirm.
    if(showLogoutDialog){
        AlertDialog(
            onDismissRequest = {showLogoutDialog=false},
            title = {Text("Log out?")},
            text = {Text("You will need to verify your mobile number again to log in.")},
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) { Text("Log out")}
            },
            dismissButton = {
                TextButton(onClick = {showLogoutDialog = false}) {Text("Stay") }
            }
        )
    }
}



@Composable
private fun ProfileDetails(profile: DriverProfileResponse){
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailRow("Name", profile.name ?: "-")
            DetailRow("Mobile Number", profile.phoneNumber ?: "-")
            DetailRow("Vehicle number", profile.vehicleNumber ?: "Not added")
            DetailRow("Bank account", profile.maskedAccountNumber ?: "-")
            DetailRow("IFSC code", profile.ifscCode ?: "-")
            profile.upiId?.let { DetailRow("UPI ID", it) }
            DetailRow("Status", profile.verificationStatus.label())
        }
    }
}


@Composable
private fun DetailRow(label: String, value: String){
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

private fun VerificationStatus.label(): String = when (this) {
    VerificationStatus.PENDING -> "Pending"
    VerificationStatus.VERIFIED -> "Verified"
    VerificationStatus.REJECTED -> "Rejected"
}