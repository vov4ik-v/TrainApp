package ai.visionit.core.permission

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

/**
 * State holder for notification permission request
 */
data class NotificationPermissionState(
    val isGranted: Boolean = false,
    val shouldShowRationale: Boolean = false,
    val isPermanentlyDenied: Boolean = false
)

/**
 * Composable для управління дозволом на нотіфікації
 * 
 * @param onPermissionResult колбек, який викликається після обробки дозволу
 * @param shouldRequest тригер для запиту дозволу
 */
@Composable
fun NotificationPermissionHandler(
    onPermissionResult: (Boolean) -> Unit = {},
    shouldRequest: Boolean = false
) {
    // Для Android 13+ потрібен дозвіл POST_NOTIFICATIONS
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        // На старіших версіях Android дозвіл не потрібен
        LaunchedEffect(Unit) {
            onPermissionResult(true)
        }
        return
    }

    val context = LocalContext.current
    var showRationaleDialog by remember { mutableStateOf(false) }
    var permissionDeniedCount by remember { mutableStateOf(0) }
    
    // Перевірка поточного стану дозволу
    val permissionStatus by remember {
        derivedStateOf {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Launcher для запиту дозволу
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                onPermissionResult(true)
            } else {
                permissionDeniedCount++
                // Якщо користувач відмовив 2+ рази, вважаємо це постійною відмовою
                if (permissionDeniedCount >= 2) {
                    showRationaleDialog = true
                } else {
                    // Показуємо rationale при першій відмові
                    showRationaleDialog = true
                }
            }
        }
    )

    // Запит дозволу при shouldRequest = true
    LaunchedEffect(shouldRequest, permissionStatus) {
        if (shouldRequest && !permissionStatus) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else if (permissionStatus) {
            onPermissionResult(true)
        }
    }

    // Діалог з поясненням необхідності дозволу
    if (showRationaleDialog) {
        NotificationPermissionRationaleDialog(
            isPermanentlyDenied = permissionDeniedCount >= 2,
            onDismiss = {
                showRationaleDialog = false
                onPermissionResult(false)
            },
            onConfirm = {
                showRationaleDialog = false
                if (permissionDeniedCount >= 2) {
                    // Відкриваємо налаштування додатку
                    openAppSettings(context)
                } else {
                    // Повторний запит дозволу
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        )
    }
}

/**
 * Діалог з поясненням чому потрібен дозвіл на нотіфікації
 */
@Composable
fun NotificationPermissionRationaleDialog(
    isPermanentlyDenied: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isPermanentlyDenied) {
                    "Дозвіл на сповіщення відхилено"
                } else {
                    "Дозвольте сповіщення"
                }
            )
        },
        text = {
            Text(
                text = if (isPermanentlyDenied) {
                    "Для отримання важливих повідомлень та оновлень, будь ласка, увімкніть " +
                    "дозвіл на сповіщення в налаштуваннях додатку.\n\n" +
                    "Ви не будете отримувати:\n" +
                    "• Важливі повідомлення від системи\n" +
                    "• Нагадування про події\n" +
                    "• Оновлення статусу завдань"
                } else {
                    "VisionIT використовує сповіщення для:\n\n" +
                    "• Надсилання важливих повідомлень\n" +
                    "• Нагадувань про важливі події\n" +
                    "• Оновлення статусу ваших завдань\n\n" +
                    "Дозвольте сповіщення, щоб не пропустити важливу інформацію."
                }
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = if (isPermanentlyDenied) {
                        "Відкрити налаштування"
                    } else {
                        "Дозволити"
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Не зараз")
            }
        }
    )
}

/**
 * Відкриває налаштування додатку
 */
private fun openAppSettings(context: android.content.Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
}

/**
 * Extension function для перевірки чи надано дозвіл на нотіфікації
 */
fun android.content.Context.hasNotificationPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true // На старіших версіях дозвіл не потрібен
    }
}
