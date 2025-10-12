package ai.visionit.utils

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Extension для AuthViewModel для управління станом дозволу на нотіфікації
 * 
 * Додайте це до вашого існуючого AuthViewModel класу:
 */

// Приклад, як можна розширити AuthViewModel:
/*
class AuthViewModel : ViewModel() {
    // ... існуючі поля ...
    
    private val _notificationPermissionGranted = MutableStateFlow<Boolean?>(null)
    val notificationPermissionGranted: StateFlow<Boolean?> = _notificationPermissionGranted.asStateFlow()
    
    private val _shouldRequestNotificationPermission = MutableStateFlow(false)
    val shouldRequestNotificationPermission: StateFlow<Boolean> = _shouldRequestNotificationPermission.asStateFlow()
    
    /**
     * Викликається після успішного логіну
     */
    fun onLoginSuccess() {
        // Ваша існуюча логіка логіну
        // ...
        
        // Тригеримо запит дозволу на нотіфікації
        _shouldRequestNotificationPermission.value = true
    }
    
    /**
     * Зберігає результат дозволу на нотіфікації
     */
    fun setNotificationPermissionGranted(granted: Boolean) {
        _notificationPermissionGranted.value = granted
        _shouldRequestNotificationPermission.value = false
        
        // Опціонально: зберегти в SharedPreferences або DataStore
        // preferencesRepository.saveNotificationPermissionStatus(granted)
    }
    
    /**
     * Скидає стан запиту дозволу
     */
    fun resetNotificationPermissionRequest() {
        _shouldRequestNotificationPermission.value = false
    }
}
*/

/**
 * Альтернативний підхід: створити окремий ViewModel для дозволів
 */
class PermissionsViewModel : ViewModel() {
    private val _notificationPermissionStatus = MutableStateFlow<NotificationPermissionStatus>(
        NotificationPermissionStatus.NotRequested
    )
    val notificationPermissionStatus: StateFlow<NotificationPermissionStatus> = 
        _notificationPermissionStatus.asStateFlow()
    
    fun requestNotificationPermission() {
        _notificationPermissionStatus.value = NotificationPermissionStatus.Requesting
    }
    
    fun onPermissionResult(granted: Boolean) {
        _notificationPermissionStatus.value = if (granted) {
            NotificationPermissionStatus.Granted
        } else {
            NotificationPermissionStatus.Denied
        }
    }
    
    fun resetPermissionState() {
        _notificationPermissionStatus.value = NotificationPermissionStatus.NotRequested
    }
}

sealed class NotificationPermissionStatus {
    object NotRequested : NotificationPermissionStatus()
    object Requesting : NotificationPermissionStatus()
    object Granted : NotificationPermissionStatus()
    object Denied : NotificationPermissionStatus()
}
