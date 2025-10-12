package ai.visionit.core.navigation

import ai.visionit.android.core.ui.components.AppScaffold
import ai.visionit.core.navigation.destinations.NavigationModules.AuthModule
import ai.visionit.core.navigation.destinations.NavigationModules.CoreModule
import ai.visionit.core.permission.NotificationPermissionHandler
import ai.visionit.feature.profile.ProfileViewModel
import ai.visionit.utils.AuthViewModel
import ai.visionit.utils.RefreshDatabaseWorker.Companion.schedulePeriodicWork
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController

@Composable
fun VisionITApp(
    authViewModel: AuthViewModel,
    navController: NavHostController,
    personViewModel: ProfileViewModel
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val isRememberMe by authViewModel.isRememberMeEnabled.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val logoutEvent by authViewModel.logoutEvent.collectAsState()
    val context = LocalContext.current

    var startDestination by remember { mutableStateOf<String?>(null) }
    var shouldRequestNotificationPermission by remember { mutableStateOf(false) }
    var hasRequestedPermission by remember { mutableStateOf(false) }

    LaunchedEffect(isRememberMe, isLoading) {
        if (!isLoading) {
            startDestination = if (isRememberMe == true) {
                schedulePeriodicWork(context = context)
                CoreModule.Home.route
            } else {
                AuthModule.SignIn.route
            }
        }
    }

    LaunchedEffect(logoutEvent) {
        if (logoutEvent) {
            navController.navigate(AuthModule.SignIn.route) {
                popUpTo(CoreModule.Home.route) { inclusive = true }
            }
            authViewModel.onLogoutEventCompleted()
            // Скидаємо прапорець при виході
            hasRequestedPermission = false
        }
    }

    // Відстежуємо успішну автентифікацію та навігацію на Home
    LaunchedEffect(startDestination, isRememberMe) {
        if (startDestination == CoreModule.Home.route && 
            isRememberMe == true && 
            !hasRequestedPermission) {
            // Запитуємо дозвіл після успішної автентифікації
            shouldRequestNotificationPermission = true
            hasRequestedPermission = true
        }
    }

    // Обробник дозволу на нотіфікації
    NotificationPermissionHandler(
        shouldRequest = shouldRequestNotificationPermission,
        onPermissionResult = { granted ->
            shouldRequestNotificationPermission = false
            // Можна додати логіку збереження стану дозволу в ViewModel
            // authViewModel.setNotificationPermissionGranted(granted)
        }
    )

    if (startDestination != null) {
        AppScaffold(
            navController = navController,
            drawerState = drawerState,
            coroutineScope = scope,
            authViewModel = authViewModel,
            personViewModel = personViewModel,
            content = {
                VisionITNavHost(
                    navController = navController,
                    startDestination = startDestination!!
                )
            }
        )
    }
}
