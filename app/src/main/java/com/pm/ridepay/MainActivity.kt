package com.pm.ridepay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.pm.ridepay.ui.login.LoginScreen
import com.pm.ridepay.ui.login.LoginViewModel
import com.pm.ridepay.ui.login.LoginViewModelFactory
import com.pm.ridepay.ui.theme.RidePayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as RidePayApp

        setContent {
            RidePayTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var loggedIn by remember { mutableStateOf(app.authRepository.isLoggedIn()) }

                    if (loggedIn) {
                        HomePlaceholder(
                            onLogout = {
                                app.authRepository.logout()
                                FirebaseAuth.getInstance().signOut()
                                loggedIn = false
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    } else {
                        val loginViewModel: LoginViewModel = viewModel(
                            factory = LoginViewModelFactory(app.authRepository)
                        )
                        LoginScreen(
                            viewModel = loginViewModel,
                            onLoggedIn = {
                                loginViewModel.reset()
                                loggedIn = true
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomePlaceholder(onLogout: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Logged in", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onLogout) { Text("Log out") }
    }
}