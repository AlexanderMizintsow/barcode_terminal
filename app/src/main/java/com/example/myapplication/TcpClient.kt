package com.example.myapplication

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.Socket
import java.net.SocketTimeoutException
import java.nio.charset.Charset

/**
 * TCP клиент для отправки данных на сервер
 * Аналог Node.js TCP клиента для мобильного приложения
 */
class TcpClient {
    
    companion object {
        private const val TAG = "TcpClient"
        private const val SERVER_HOST = "192.168.57.77"
        private const val SERVER_PORT = 8240
        private const val TIMEOUT_MS = 10000 // 10 секунд
        private const val CHARSET_NAME = "windows-1251"
    }
    
    /**
     * Отправляет штрихкод на сервер
     * @param barcode Штрихкод для отправки
     * @param deviceNumber Трехзначный номер ТСД сканера
     * @return Результат операции (успех/ошибка)
     */
    suspend fun sendBarcode(barcode: String, deviceNumber: String = "999"): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Начинаем отправку штрихкода: $barcode")
            
            // Создаем TCP сокет
            val socket = Socket()
            socket.soTimeout = TIMEOUT_MS
            
            // Подключаемся к серверу
            socket.connect(java.net.InetSocketAddress(SERVER_HOST, SERVER_PORT), TIMEOUT_MS)
            Log.d(TAG, "Соединение установлено с сервером $SERVER_HOST:$SERVER_PORT")
            
            // Формируем сообщение в формате: Q11\x01TSD{deviceNumber}\x02\t{barcode}\r
            val message = "Q11\u0001TSD$deviceNumber\u0002\t$barcode\r"
            Log.d(TAG, "Отправляем сообщение: $message (ТСД: $deviceNumber)")
            
            // Конвертируем в кодировку windows-1251
            val charset = try {
                Charset.forName(CHARSET_NAME)
            } catch (e: Exception) {
                Log.w(TAG, "Не удалось загрузить кодировку $CHARSET_NAME, используем UTF-8")
                Charsets.UTF_8
            }
            
            val messageBytes = message.toByteArray(charset)
            
            // Используем один блок use для всего сокета
            val responseText = socket.use { sock ->
                // Отправляем данные (НЕ закрываем поток, чтобы не закрыть сокет раньше времени)
                val outputStream = sock.getOutputStream()
                outputStream.write(messageBytes)
                outputStream.flush()
                Log.d(TAG, "Данные отправлены успешно")
                
                // Небольшая задержка для обработки сервером
                kotlinx.coroutines.delay(100)
                
                // Читаем ответ от сервера блокирующе до первого CR или таймаута (НЕ закрывая inputStream принудительно)
                val response = StringBuilder()
                try {
                    sock.soTimeout = TIMEOUT_MS // 10 секунд
                    val inputStream = sock.getInputStream()
                    val buffer = ByteArray(1024)
                    while (true) {
                        try {
                            val bytesRead = inputStream.read(buffer)
                            if (bytesRead == -1) {
                                Log.d(TAG, "Сервер закрыл соединение во время чтения")
                                break
                            }
                            if (bytesRead > 0) {
                                val chunk = String(buffer, 0, bytesRead, charset)
                                response.append(chunk)
                                // Диагностика: логируем сырые байты и текст в 1251
                                val hex = buffer.copyOf(bytesRead).joinToString(" ") { String.format("%02X", it) }
                                Log.d(TAG, "RX bytes: [$hex]")
                                Log.d(TAG, "RX text (1251): $chunk")
                                // Как в Node: прекращаем чтение после первого пришедшего блока
                                break
                            }
                        } catch (e: SocketTimeoutException) {
                            Log.d(TAG, "Таймаут чтения ответа от сервера")
                            break
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Ошибка при чтении ответа: ${e.message}")
                }
                
                Log.d(TAG, "Соединение закрыто")
                response.toString()
            }
            
            val finalResponse = if (responseText.isBlank()) {
                "Данные отправлены (сервер не отправил ответ)"
            } else {
                responseText
            }
            Log.d(TAG, "Операция завершена успешно. Ответ: $finalResponse")
            Result.success(finalResponse)
            
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Таймаут соединения - сервер не ответил", e)
            Result.failure(Exception("Таймаут соединения с сервером"))
            
        } catch (e: IOException) {
            Log.e(TAG, "Ошибка сети при отправке данных", e)
            Result.failure(Exception("Ошибка сети: ${e.message}"))
            
        } catch (e: Exception) {
            Log.e(TAG, "Неожиданная ошибка при отправке данных", e)
            Result.failure(Exception("Ошибка: ${e.message}"))
        }
    }
    
    /**
     * Проверяет доступность сервера
     * @return true если сервер доступен
     */
    suspend fun isServerAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            val socket = Socket()
            socket.soTimeout = 5000 // 5 секунд для проверки
            
            socket.connect(java.net.InetSocketAddress(SERVER_HOST, SERVER_PORT), 5000)
            socket.close()
            
            Log.d(TAG, "Сервер $SERVER_HOST:$SERVER_PORT доступен")
            true
            
        } catch (e: Exception) {
            Log.w(TAG, "Сервер $SERVER_HOST:$SERVER_PORT недоступен: ${e.message}")
            false
        }
    }
}
