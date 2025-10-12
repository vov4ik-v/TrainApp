# Інтеграція дозволу на нотіфікації - Керівництво

## Senior Android Developer Approach 🚀

### Огляд архітектури

Рішення побудоване на принципах:
- **Single Responsibility**: Кожен компонент має одну чітку відповідальність
- **Separation of Concerns**: UI логіка відділена від бізнес-логіки
- **Reusability**: Компоненти можна легко переносити між проектами
- **Testability**: Логіка легко тестується

---

## Крок 1: Додайте дозвіл в AndroidManifest.xml

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <!-- Для Android 13+ (API 33+) -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    
    <application>
        ...
    </application>
</manifest>
```

---

## Крок 2: Скопіюйте файл NotificationPermissionHandler.kt

Розмістіть файл `NotificationPermissionHandler.kt` в:
```
app/src/main/java/ai/visionit/core/permission/NotificationPermissionHandler.kt
```

---

## Крок 3: Інтеграція з VisionITApp

### Варіант А: Просте рішення (рекомендовано для швидкого старту)

Замініть ваш існуючий `VisionITApp.kt` на:

```kotlin
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

    LaunchedEffect(isRememberMe, isLoading) {
        if (!isLoading) {
            startDestination = if (isRememberMe == true) {
                schedulePeriodicWork(context = context)
                // Тригеримо запит дозволу після успішного логіну
                shouldRequestNotificationPermission = true
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
        }
    }

    // Обробник дозволу на нотіфікації
    NotificationPermissionHandler(
        shouldRequest = shouldRequestNotificationPermission,
        onPermissionResult = { granted ->
            shouldRequestNotificationPermission = false
            if (granted) {
                // Дозвіл надано - можна ініціалізувати FCM
                // FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                //     val token = task.result
                //     // Відправити токен на сервер
                // }
            }
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
```

### Варіант Б: З ViewModel (рекомендовано для великих проектів)

1. Додайте поля в `AuthViewModel`:

```kotlin
class AuthViewModel : ViewModel() {
    // Існуючі поля...
    
    private val _shouldRequestNotificationPermission = MutableStateFlow(false)
    val shouldRequestNotificationPermission: StateFlow<Boolean> = 
        _shouldRequestNotificationPermission.asStateFlow()
    
    private val _notificationPermissionGranted = MutableStateFlow<Boolean?>(null)
    val notificationPermissionGranted: StateFlow<Boolean?> = 
        _notificationPermissionGranted.asStateFlow()
    
    fun onLoginSuccess() {
        // Ваша логіка
        _shouldRequestNotificationPermission.value = true
    }
    
    fun setNotificationPermissionGranted(granted: Boolean) {
        _notificationPermissionGranted.value = granted
        _shouldRequestNotificationPermission.value = false
        
        // Зберегти в DataStore/SharedPreferences
        viewModelScope.launch {
            preferencesRepository.saveNotificationPermissionStatus(granted)
        }
    }
}
```

2. Використайте в VisionITApp:

```kotlin
@Composable
fun VisionITApp(
    authViewModel: AuthViewModel,
    navController: NavHostController,
    personViewModel: ProfileViewModel
) {
    // ... існуючий код ...
    
    val shouldRequestPermission by authViewModel.shouldRequestNotificationPermission.collectAsState()
    
    NotificationPermissionHandler(
        shouldRequest = shouldRequestPermission,
        onPermissionResult = { granted ->
            authViewModel.setNotificationPermissionGranted(granted)
        }
    )
    
    // ... решта коду ...
}
```

---

## Крок 4: Кастомізація діалогу

Якщо хочете змінити тексти діалогу, створіть string ресурси:

```xml
<!-- res/values/strings.xml -->
<resources>
    <string name="notification_permission_title">Дозвольте сповіщення</string>
    <string name="notification_permission_rationale">
        VisionIT використовує сповіщення для:\n\n
        • Надсилання важливих повідомлень\n
        • Нагадувань про важливі події\n
        • Оновлення статусу ваших завдань
    </string>
    <string name="notification_permission_denied_title">Дозвіл відхилено</string>
    <string name="notification_permission_settings_message">
        Увімкніть дозвіл на сповіщення в налаштуваннях
    </string>
</resources>
```

Потім оновіть `NotificationPermissionRationaleDialog`:

```kotlin
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
                text = stringResource(
                    if (isPermanentlyDenied) 
                        R.string.notification_permission_denied_title
                    else 
                        R.string.notification_permission_title
                )
            )
        },
        text = {
            Text(
                text = stringResource(
                    if (isPermanentlyDenied)
                        R.string.notification_permission_settings_message
                    else
                        R.string.notification_permission_rationale
                )
            )
        },
        // ... решта коду
    )
}
```

---

## Крок 5: Тестування

### Тестові сценарії:

1. ✅ **Успішний логін + дозвіл надано**
   - Користувач логінується
   - Діалог дозволу з'являється
   - Користувач натискає "Дозволити"
   - Дозвіл надано

2. ✅ **Успішний логін + дозвіл відхилено один раз**
   - Користувач логінується
   - Діалог дозволу з'являється
   - Користувач натискає "Не дозволяти"
   - Rationale діалог з'являється
   - Користувач може повторити спробу

3. ✅ **Успішний логін + дозвіл відхилено двічі**
   - Користувач логінується
   - Діалог дозволу з'являється
   - Користувач відхиляє двічі
   - Діалог пропонує відкрити налаштування

4. ✅ **Android 12 і нижче**
   - Діалог не з'являється
   - Дозвіл автоматично надано (не потрібен)

---

## Крок 6: Best Practices

### 1. Не запитуйте дозвіл відразу при першому запуску
❌ Погано:
```kotlin
onCreate {
    requestNotificationPermission() // Занадто рано!
}
```

✅ Добре:
```kotlin
// Після успішного логіну або коли користувач хоче увімкнути нотіфікації
onLoginSuccess {
    requestNotificationPermission()
}
```

### 2. Поясніть користувачу навіщо потрібен дозвіл
✅ Показуйте чіткі переваги в rationale діалозі

### 3. Не запитуйте дозвіл багато разів
✅ Відстежуйте кількість відмов і переводьте на налаштування

### 4. Зберігайте стан дозволу
```kotlin
// DataStore
suspend fun saveNotificationPermissionStatus(granted: Boolean) {
    dataStore.edit { preferences ->
        preferences[NOTIFICATION_PERMISSION_KEY] = granted
    }
}
```

### 5. Обробляйте зміни дозволу в runtime
```kotlin
// Додайте BroadcastReceiver для відстеження змін дозволу
class NotificationPermissionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (context.hasNotificationPermission()) {
            // Дозвіл надано
        }
    }
}
```

---

## Додаткові можливості

### Інтеграція з Firebase Cloud Messaging

```kotlin
NotificationPermissionHandler(
    shouldRequest = shouldRequestNotificationPermission,
    onPermissionResult = { granted ->
        if (granted) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    // Відправити токен на backend
                    viewModel.registerFcmToken(token)
                }
            }
        }
    }
)
```

### Аналітика

```kotlin
onPermissionResult = { granted ->
    analytics.logEvent("notification_permission_result") {
        param("granted", granted)
        param("timestamp", System.currentTimeMillis())
    }
}
```

---

## Troubleshooting

### Діалог не з'являється
- Перевірте що Build.VERSION.SDK_INT >= 33 (Android 13+)
- Переконайтеся що дозвіл додано в AndroidManifest.xml
- Перевірте що shouldRequest = true

### Діалог з'являється багато разів
- Використовуйте `remember { mutableStateOf(false) }` для відстеження
- Скидайте стан після обробки результату

### Rationale діалог не показується
- Переконайтеся що користувач відхилив дозвіл принаймні один раз
- Перевірте логіку `permissionDeniedCount`

---

## Підсумок

✨ **Переваги цього підходу:**

1. ✅ Чистий, модульний код
2. ✅ Легко тестувати
3. ✅ Повторно використовуваний
4. ✅ Дотримання Android best practices
5. ✅ Відмінний UX
6. ✅ Підтримка всіх версій Android

---

## Контакт

Якщо виникли питання, перегляньте:
- [Android Permissions Guide](https://developer.android.com/training/permissions/requesting)
- [Notification Runtime Permission](https://developer.android.com/develop/ui/views/notifications/notification-permission)
