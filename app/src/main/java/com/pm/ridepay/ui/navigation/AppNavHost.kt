package com.pm.ridepay.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import com.pm.ridepay.ui.ride.RideCompleteScreen
import com.pm.ridepay.ui.ride.RideDetailsScreen
import com.pm.ridepay.ui.ride.RideStatusViewModel
import com.pm.ridepay.ui.ride.RideStatusViewModelFactory
import com.pm.ridepay.ui.ride.TrackerScreen
import com.pm.ridepay.ui.startride.StartRideScreen
import com.pm.ridepay.ui.startride.StartRideViewModel
import com.pm.ridepay.ui.startride.StartRideViewModelFactory
import com.pm.ridepay.ui.unassigned.UnassignedPaymentsScreen
import com.pm.ridepay.ui.unassigned.UnassignedPaymentsViewModel
import com.pm.ridepay.ui.unassigned.UnassignedPaymentsViewModelFactory

object Routes {
    const val HOME = "home"
    const val START_RIDE = "start_ride?assignPaymentId={assignPaymentId}"
    const val UNASSIGNED = "unassigned"
    const val TRACKER = "tracker/{rideId}"
    const val COMPLETE = "complete/{rideId}"

    const val DETAILS = "details/{rideId}"

    fun startRide(assignPaymentId: Long? = null) =
        if (assignPaymentId == null) "start_ride"
        else "start_ride?assignPaymentId=$assignPaymentId"

    fun tracker(rideId: Long) = "tracker/$rideId"
    fun complete(rideId: Long) = "complete/$rideId"

    fun details(rideId: Long) = "details/$rideId"
}

@Composable
fun AppNavHost(
    app: RidePayApp,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val rideIdArgument = listOf(navArgument("rideId") { type = NavType.LongType })

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier
    ) {
        composable(Routes.HOME) {
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModelFactory(app.rideRepository, app.paymentRepository)
            )
            HomeScreen(
                viewModel = homeViewModel,
                onStartRide = { navController.navigate(Routes.startRide()) },
                onResumeRide = { rideId -> navController.navigate(Routes.tracker(rideId)) },
                onReviewPayments = { navController.navigate(Routes.UNASSIGNED) },
                onOpenRide = { rideId -> navController.navigate(Routes.details(rideId)) },
                onLogout = onLogout
            )
        }

        composable(
            route = Routes.START_RIDE,
            arguments = listOf(
                navArgument("assignPaymentId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { entry ->
            val assignPaymentId = entry.arguments
                ?.getLong("assignPaymentId")
                ?.takeIf { it > 0 }
            val startRideViewModel: StartRideViewModel = viewModel(
                factory = StartRideViewModelFactory(app.rideRepository, assignPaymentId)
            )
            StartRideScreen(
                viewModel = startRideViewModel,
                onStarted = { rideId ->
                    navController.navigate(Routes.tracker(rideId)) {
                        popUpTo(Routes.START_RIDE) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.UNASSIGNED) {
            val viewModel: UnassignedPaymentsViewModel = viewModel(
                factory = UnassignedPaymentsViewModelFactory(
                    app.rideRepository, app.paymentRepository
                )
            )
            UnassignedPaymentsScreen(
                viewModel = viewModel,
                onStartRideFor = { paymentId ->
                    navController.navigate(Routes.startRide(paymentId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.TRACKER, arguments = rideIdArgument) { entry ->
            val rideId = entry.arguments?.getLong("rideId") ?: 0L
            val viewModel: RideStatusViewModel = viewModel(
                factory = RideStatusViewModelFactory(app.rideRepository, rideId)
            )
            TrackerScreen(
                viewModel = viewModel,
                onCompleted = {
                    navController.navigate(Routes.complete(rideId)) {
                        popUpTo(Routes.TRACKER) { inclusive = true }
                    }
                },
                onCancelled = { navController.popBackStack(Routes.HOME, inclusive = false) }
            )
        }

        composable(Routes.COMPLETE, arguments = rideIdArgument) { entry ->
            val rideId = entry.arguments?.getLong("rideId") ?: 0L
            val viewModel: RideStatusViewModel = viewModel(
                factory = RideStatusViewModelFactory(app.rideRepository, rideId)
            )
            RideCompleteScreen(
                viewModel = viewModel,
                onViewDetails = { navController.navigate(Routes.details(rideId)) },
                onDone = { navController.popBackStack(Routes.HOME, inclusive = false) }
            )
        }

        composable(Routes.DETAILS, arguments = rideIdArgument) { entry ->
            val rideId = entry.arguments?.getLong("rideId") ?: 0L
            val viewModel: RideStatusViewModel = viewModel(
                factory = RideStatusViewModelFactory(app.rideRepository, rideId)
            )
            RideDetailsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}