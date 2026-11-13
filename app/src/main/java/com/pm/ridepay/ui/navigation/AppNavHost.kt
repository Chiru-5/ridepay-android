package com.pm.ridepay.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pm.ridepay.RidePayApp
import com.pm.ridepay.ui.home.HomeScreen
import com.pm.ridepay.ui.home.HomeViewModel
import com.pm.ridepay.ui.home.HomeViewModelFactory


object Routes{
    const val HOME ="home"
    const val START_RIDE = "start_ride"
}


@Composable
fun AppNavHost(
    app: RidePayApp,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
){
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier=modifier
    ){
        composable(Routes.HOME){
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModelFactory(app.rideRepository)
            )
            HomeScreen(
                viewModel=homeViewModel,
                onStartRide = {navController.navigate(Routes.START_RIDE)},
                onLogout = onLogout
            )
        }

        composable(Routes.START_RIDE){
            StartRidePlaceholder(onBack = {navController.popBackStack()})
        }
    }
}

@Composable
private fun StartRidePlaceholder(onBack: ()-> Unit){
    Column(
        modifier = Modifier.fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Start ride (coming next)", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onBack) {
            Text("Back")
        }
    }
}