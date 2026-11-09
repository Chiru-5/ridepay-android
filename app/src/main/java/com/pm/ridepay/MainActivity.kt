package com.pm.ridepay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pm.ridepay.ui.login.LoginScreen
import com.pm.ridepay.ui.login.LoginViewModel
import com.pm.ridepay.ui.login.LoginViewModelFactory
import com.pm.ridepay.ui.onboarding.OnboardingScreen
import com.pm.ridepay.ui.onboarding.OnboardingViewModel
import com.pm.ridepay.ui.onboarding.OnboardingViewModelFactory
import com.pm.ridepay.ui.session.SessionState
import com.pm.ridepay.ui.session.SessionViewModel
import com.pm.ridepay.ui.session.SessionViewModelFactory
import com.pm.ridepay.ui.theme.RidePayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as RidePayApp

        setContent {
            RidePayTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val contentModifier = Modifier.padding(innerPadding)

                    val sessionViewModel: SessionViewModel = viewModel(
                        factory = SessionViewModelFactory(
                            app.authRepository, app.driverRepository
                        )
                    )
                    val sessionState by sessionViewModel.state.collectAsStateWithLifecycle()

                    when (val state = sessionState) {
                        SessionState.Loading -> LoadingScreen(contentModifier)

                        SessionState.LoggedOut -> {
                            val loginViewModel: LoginViewModel = viewModel(
                                factory = LoginViewModelFactory(app.authRepository)
                            )
                            LoginScreen(
                                viewModel = loginViewModel,
                                onLoggedIn = {
                                    loginViewModel.reset()
                                    sessionViewModel.onLoggedIn()
                                },
                                modifier = contentModifier
                            )
                        }

                        is SessionState.NeedsOnboarding->{
                            val onboardingViewModel: OnboardingViewModel=viewModel(
                                key = "onboarding-${state.driver.driverId}",
                                factory = OnboardingViewModelFactory(app.driverRepository)
                            )
                            OnboardingScreen(
                                viewModel = onboardingViewModel,
                                onFinished = {driver -> sessionViewModel.onOnboarded(driver)},
                                onLogout = sessionViewModel::logout,
                                modifier = contentModifier
                            )
                        }

                        is SessionState.Ready -> PlaceholderScreen(
                            title = "Welcome, ${state.driver.name ?: "Driver"}",
                            onLogout = sessionViewModel::logout,
                            modifier = contentModifier
                        )

                        is SessionState.Error -> ErrorScreen(
                            message = state.message,
                            onRetry = sessionViewModel::refresh,
                            onLogout = sessionViewModel::logout,
                            modifier = contentModifier
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onLogout) { Text("Log out") }
    }
}

@Composable
private fun ErrorScreen(
    message: String,
    onRetry: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Try again") }
        TextButton(onClick = onLogout) { Text("Log out") }
    }
}