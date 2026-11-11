package com.pm.ridepay.ui.qr

import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import android.media.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle


@Composable
fun QrScreen(
    viewModel: QrViewModel,
    continueLabel: String,
    onContinue: ()-> Unit,
    modifier: Modifier=Modifier
){
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val bitmap = state.qrBitmap


    Column(
        modifier=Modifier.fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Your payment QR", style= MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("Print it and stick it on your vehicle.", style= MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))

        when{
            state.isLoading -> CircularProgressIndicator()

            bitmap!=null -> Image(
                bitmap=bitmap.asImageBitmap(),
                contentDescription = "Payment QR Code",
                filterQuality = FilterQuality.None,
                modifier = Modifier.size(260.dp)
            )
            else ->{
                Text(
                    state.errorMessage ?: "Something went wrong.",
                    color= MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = viewModel::load) {
                    Text("Try again")
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = viewModel::download,
            enabled = bitmap!=null && !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Download")
        }
        state.saveMessage?. let { message->
            Spacer(Modifier.height(8.dp))
            Text(message, style= MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onContinue, modifier= Modifier.fillMaxWidth()) {
            Text(continueLabel)
        }
    }
}