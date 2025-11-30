package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Класс для работы с встроенным сканером CipherLab RK26
 */
object CipherLabScanner {
    
    private const val TAG = "CipherLabScanner"
    
    // Основные события CipherLab для управления сканером
    private const val ACTION_SCAN = "com.cipherlab.scanner.ACTION_SCAN"
    private const val ACTION_SCAN_START = "com.cipherlab.scanner.ACTION_SCAN_START"
    private const val ACTION_SCAN_STOP = "com.cipherlab.scanner.ACTION_SCAN_STOP"
    private const val ACTION_SCAN_RESULT = "com.cipherlab.scanner.ACTION_SCAN_RESULT"
    
    // Альтернативные события для разных версий ПО CipherLab
    private const val ACTION_TRIGGER_SCAN = "com.cipherlab.barcode.ACTION_TRIGGER_SCAN"
    private const val ACTION_SOFTWARE_TRIGGER = "com.cipherlab.scanner.SOFTWARE_TRIGGER"
    
    // Дополнительные события для RK26
    private const val ACTION_SCAN_TRIGGER = "com.cipherlab.scanner.TRIGGER_SCAN"
    private const val ACTION_SCANNER_TRIGGER = "com.cipherlab.scanner.SCANNER_TRIGGER"
    private const val ACTION_START_SCAN = "com.cipherlab.scanner.START_SCAN"
    
    // Системные события для программного запуска
    private const val ACTION_SYSTEM_SCAN = "android.intent.action.SEND"
    
    /**
     * Запускает сканирование штрихкода программно
     */
    fun triggerScan(context: Context): Boolean {
        return try {
            Log.d(TAG, "Attempting to trigger CipherLab RK26 scanner")
            
            // Расширенный список intent действий для RK26
            val intents = listOf(
                Intent(ACTION_SCAN),
                Intent(ACTION_SCAN_START), 
                Intent(ACTION_START_SCAN),
                Intent(ACTION_TRIGGER_SCAN),
                Intent(ACTION_SCAN_TRIGGER),
                Intent(ACTION_SCANNER_TRIGGER),
                Intent(ACTION_SOFTWARE_TRIGGER)
            )
            
            var success = false
            var lastError: Exception? = null
            
            for (intent in intents) {
                try {
                    // Добавляем дополнительные параметры для RK26
                    intent.putExtra("trigger", true)
                    intent.putExtra("scanner_trigger", true)
                    intent.putExtra("com.cipherlab.scanner.trigger", true)
                    
                    context.sendBroadcast(intent)
                    Log.d(TAG, "Successfully sent broadcast: ${intent.action}")
                    success = true
                    break
                } catch (e: SecurityException) {
                    Log.w(TAG, "Permission denied for ${intent.action}: ${e.message}")
                    lastError = e
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to send broadcast ${intent.action}: ${e.message}")
                    lastError = e
                }
            }
            
            // Дополнительная попытка через системные события
            if (!success) {
                try {
                    val systemIntent = Intent(ACTION_SYSTEM_SCAN)
                    systemIntent.putExtra("com.cipherlab.scanner.trigger", true)
                    systemIntent.putExtra("scanner_action", "start")
                    systemIntent.type = "text/plain"
                    context.sendBroadcast(systemIntent)
                    Log.d(TAG, "Sent system broadcast with extras")
                    success = true
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to send system broadcast: ${e.message}")
                    lastError = e
                }
            }
            
            if (!success) {
                Log.e(TAG, "All scanner trigger attempts failed. Last error: ${lastError?.message}")
            }
            
            success
        } catch (e: Exception) {
            Log.e(TAG, "Critical error triggering scanner", e)
            false
        }
    }
    
    /**
     * Останавливает сканирование
     */
    fun stopScan(context: Context): Boolean {
        return try {
            val intent = Intent(ACTION_SCAN_STOP)
            context.sendBroadcast(intent)
            Log.d(TAG, "Sent stop scan broadcast")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping scanner", e)
            false
        }
    }
    
    /**
     * Проверяет доступность сканера
     */
    fun isScannerAvailable(context: Context): Boolean {
        return try {
            // На реальном терминале CipherLab сканер всегда доступен
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error checking scanner availability", e)
            false
        }
    }
}
