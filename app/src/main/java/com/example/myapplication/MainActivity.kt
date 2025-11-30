package com.example.myapplication

import android.content.Context
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import android.util.Log
import kotlinx.coroutines.launch
import android.annotation.SuppressLint
import android.content.SharedPreferences

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                BarcodeScannerScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerScreen() {
    var barcodeText by remember { mutableStateOf("") }
    var isSendButtonEnabled by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var sendStatus by remember { mutableStateOf<String?>(null) }
    var serverMessage by remember { mutableStateOf<String?>(null) }
    
    // Загружаем сохраненный номер ТСД из SharedPreferences
    var deviceNumber by remember { 
        mutableStateOf(
            context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .getString("device_number", "999") ?: "999"
        ) 
    }
    
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showDeviceNumberDialog by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Автоматически устанавливаем фокус на поле ввода при открытии приложения
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    // Обновляем состояние кнопки отправки при изменении текста
    LaunchedEffect(barcodeText) {
        isSendButtonEnabled = barcodeText.isNotBlank()
        
        // Если поле очищено, сразу возвращаем фокус
        if (barcodeText.isEmpty()) {
            focusRequester.requestFocus()
        }
    }
    
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
        // Статус отправки (по центру, над заголовком)
        if (sendStatus != null) {
            Text(
                text = sendStatus ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        // Ответ сервера с визуальным выделением
        if (!serverMessage.isNullOrBlank()) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                shape = MaterialTheme.shapes.medium,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp)
            ) {
                Text(
                    text = serverMessage ?: "",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize * 2,
                        lineHeight = MaterialTheme.typography.bodyMedium.fontSize * 2 * 1.2f
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
        // Заголовок
     
        
        
        // Поле ввода штрихкода
        OutlinedTextField(
            value = barcodeText,
            onValueChange = { newText ->
                barcodeText = newText
            },
            label = { Text("Штрихкод") },
            placeholder = { Text("Введите или сканируйте") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    if (isSendButtonEnabled) {
                        // Очищаем предыдущий ответ сервера перед новой отправкой
                        serverMessage = null
                        sendBarcode(
                            context = context,
                            barcode = barcodeText,
                            deviceNumber = deviceNumber,
                            onStatusUpdate = { sendStatus = it },
                            onServerMessage = { serverMessage = it }
                        )
                        barcodeText = ""
                        // Возвращаем фокус сразу после очистки
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                            kotlinx.coroutines.delay(100) // Небольшая задержка для корректной работы
                            focusRequester.requestFocus()
                        }
                    }
                }
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Кнопка отправки
        Button(
            onClick = {
                // Очищаем предыдущий ответ сервера перед новой отправкой
                serverMessage = null
                sendBarcode(
                    context = context,
                    barcode = barcodeText,
                    deviceNumber = deviceNumber,
                    onStatusUpdate = { sendStatus = it },
                    onServerMessage = { serverMessage = it }
                )
                barcodeText = ""
                // Возвращаем фокус сразу после очистки
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                    kotlinx.coroutines.delay(100) // Небольшая задержка для корректной работы
                    focusRequester.requestFocus()
                }
            },
            enabled = isSendButtonEnabled,
            modifier = Modifier
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                "Отправить",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        
        
        Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Кнопка настройки номера устройства в правом верхнем углу
        TextButton(
            onClick = { showPasswordDialog = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Text(
                text = "№ Устройства",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Кнопка тестирования TCP соединения в левом верхнем углу (как "№ Устройства")
        TextButton(
            onClick = {
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                    try {
                        val tcpClient = TcpClient()
                        val isAvailable = tcpClient.isServerAvailable()
                        Toast.makeText(
                            context,
                            if (isAvailable) {
                                "✅ TCP сервер доступен (192.168.57.77:8240)"
                            } else {
                                "❌ TCP сервер недоступен (192.168.57.77:8240)"
                            },
                            Toast.LENGTH_LONG
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            "❌ Ошибка проверки TCP соединения: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Text(
                text = "Тест TCP соединения",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // Диалог ввода пароля
        if (showPasswordDialog) {
            PasswordDialog(
                onDismiss = { showPasswordDialog = false },
                onPasswordCorrect = { 
                    showPasswordDialog = false
                    showDeviceNumberDialog = true
                }
            )
        }
        
        // Диалог настройки номера устройства
        if (showDeviceNumberDialog) {
            DeviceNumberDialog(
                currentNumber = deviceNumber,
                onDismiss = { showDeviceNumberDialog = false },
                onNumberSet = { newNumber ->
                    deviceNumber = newNumber
                    showDeviceNumberDialog = false
                    
                    // Сохраняем номер ТСД в SharedPreferences
                    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putString("device_number", newNumber).apply()
                    
                    Toast.makeText(context, "Номер ТСД установлен и сохранен: $newNumber", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordDialog(
    onDismiss: () -> Unit,
    onPasswordCorrect: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Введите пароль") },
        text = {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (password == "777") {
                        onPasswordCorrect()
                    } else {
                        // Пароль неверный - можно добавить Toast
                    }
                }
            ) {
                Text("ОК")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceNumberDialog(
    currentNumber: String,
    onDismiss: () -> Unit,
    onNumberSet: (String) -> Unit
) {
    var deviceNumber by remember { mutableStateOf(currentNumber) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Настройка номера ТСД") },
        text = {
            Column {
                Text(
                    text = "Введите трехзначный номер ТСД сканера:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = deviceNumber,
                    onValueChange = { newValue ->
                        // Разрешаем только цифры и максимум 3 символа
                        if (newValue.length <= 3 && newValue.all { it.isDigit() }) {
                            deviceNumber = newValue
                        }
                    },
                    label = { Text("Номер ТСД (3 цифры)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (deviceNumber.length == 3) {
                        onNumberSet(deviceNumber)
                    }
                },
                enabled = deviceNumber.length == 3
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

private fun sendBarcode(
    context: Context,
    barcode: String,
    deviceNumber: String,
    onStatusUpdate: (String) -> Unit = { },
    onServerMessage: (String) -> Unit = { }
) {
    if (barcode.isNotBlank()) {
        // Проверяем, что номер ТСД установлен
        if (deviceNumber == "999") {
            onStatusUpdate("⚠️ Необходимо установить номер ТСД сканера. Нажмите '№ Устройства'.")
            return
        }
        
        // Обновляем статус о начале отправки
        onStatusUpdate("📤 Отправляем: '$barcode'")
        
        // Запускаем отправку в корутине
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            try {
                val tcpClient = TcpClient()
                
                // Проверяем доступность сервера
                val isServerAvailable = tcpClient.isServerAvailable()
                if (!isServerAvailable) {
                    onStatusUpdate("⚠️ Сервер недоступен (192.168.57.77:8240). Проверьте сеть.")
                    return@launch
                }
                
                // Отправляем штрихкод с номером ТСД
                val result = tcpClient.sendBarcode(barcode, deviceNumber)
                
                result.fold(
                    onSuccess = { response ->
                        onStatusUpdate("✅ Отправлено: '$barcode' (ТСД: $deviceNumber)")
                        // Пытаемся вытащить первую смысловую строку ответа, как в Node: начинается с 's' и до CR
                        val msg = extractServerMessage(response)
                        if (msg.isNotBlank()) onServerMessage(msg)
                        Log.d("MainActivity", "TCP отправка успешна: $response")
                    },
                    onFailure = { error ->
                        onStatusUpdate("❌ Ошибка отправки '$barcode': ${error.message}")
                        Log.e("MainActivity", "TCP отправка не удалась", error)
                    }
                )
                
            } catch (e: Exception) {
                onStatusUpdate("❌ Неожиданная ошибка при отправке '$barcode': ${e.message}")
                Log.e("MainActivity", "Неожиданная ошибка при отправке", e)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BarcodeScannerPreview() {
    MyApplicationTheme {
        BarcodeScannerScreen()
    }
}

// Извлекает строку ответа сервера вида "s..." без начальной 's'
private fun extractServerMessage(raw: String): String {
    if (raw.isBlank()) return ""
    val firstCr = raw.indexOf('\r')
    val line = if (firstCr >= 0) raw.substring(0, firstCr) else raw
    return if (line.isNotEmpty() && line[0] == 's') line.substring(1) else line
}