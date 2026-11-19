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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pm.ridepay.RidePayApp
import com.pm.ridepay.ui.home.HomeScreen
import com.pm.ridepay.ui.home.HomeViewModel
import com.pm.ridepay.ui.home.HomeViewModelFactory
import com.pm.ridepay.ui.startride.StartRideScreen
import com.pm.ridepay.ui.startride.StartRideViewModel
import com.pm.ridepay.ui.startride.StartRideViewModelFactory


object Routes{
    const val HOME ="home"
    const val START_RIDE = "start_ride"
    const val TRACKER = "tracker/{rideId}"
    fun tracker(rideId: Long) = "tracker/${rideId}"
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
            val startRideViewModel: StartRideViewModel = viewModel(
                factory = StartRideViewModelFactory(app.rideRepository)
            )
            StartRideScreen(
                viewModel=startRideViewModel,
                onStarted = {
                    rideId -> navController.navigate(Routes.tracker(rideId)){
                        popUpTo(Routes.START_RIDE){ inclusive=true }
                    }
                },
                onBack = {navController.popBackStack()}
            )
        }

        composable(
            route=Routes.TRACKER,
            arguments = listOf(navArgument("rideId"){type= NavType.LongType})
        ){
            entry->
            val rideId=entry.arguments?.getLong("rideId")?:0L
            TrackerPlaceholder(
                rideId=rideId,
                onBackToHome={navController.popBackStack(Routes.HOME, inclusive = false)}
            )
        }
    }
}

@Composable
private fun TrackerPlaceholder(rideId: Long, onBackToHome: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Ride $rideId started", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("The live tracker comes next.")
        Spacer(Modifier.height(16.dp))
        Button(onClick = onBackToHome) { Text("Back to Home") }
    }
}