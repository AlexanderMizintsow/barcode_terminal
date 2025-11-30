package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Утилиты для работы с CipherLab RK26 сканером
 * 
 * Этот класс содержит дополнительные методы для диагностики и работы со сканером
 */
object ScannerUtils {
    
    private const val TAG = "ScannerUtils"
    
    /**
     * Проверяет доступность различных компонентов сканера
     */
    fun diagnoseScanner(context: Context): String {
        val diagnostics = StringBuilder()
        
        diagnostics.appendLine("=== CipherLab RK26 Scanner Diagnostics ===")
        
        // Проверяем доступность различных intent действий
        val testActions = listOf(
            "com.cipherlab.scanner.ACTION_SCAN",
            "com.cipherlab.scanner.ACTION_SCAN_START", 
            "com.cipherlab.barcode.ACTION_TRIGGER_SCAN",
            "com.cipherlab.scanner.SOFTWARE_TRIGGER"
        )
        
        diagnostics.appendLine("Testing broadcast actions:")
        for (action in testActions) {
            try {
                val intent = Intent(action)
                context.sendBroadcast(intent)
                diagnostics.appendLine("✓ $action - OK")
            } catch (e: SecurityException) {
                diagnostics.appendLine("✗ $action - Permission denied")
            } catch (e: Exception) {
                diagnostics.appendLine("✗ $action - Error: ${e.message}")
            }
        }
        
        return diagnostics.toString()
    }
    
    /**
     * Пробует запустить сканер с подробным логированием
     */
    fun triggerScannerWithLogging(context: Context): Boolean {
        Log.d(TAG, "Starting detailed scanner trigger attempt")
        
        val actions = listOf(
            "com.cipherlab.scanner.ACTION_SCAN",
            "com.cipherlab.scanner.ACTION_SCAN_START",
            "com.cipherlab.scanner.START_SCAN",
            "com.cipherlab.barcode.ACTION_TRIGGER_SCAN",
            "com.cipherlab.scanner.TRIGGER_SCAN",
            "com.cipherlab.scanner.SCANNER_TRIGGER",
            "com.cipherlab.scanner.SOFTWARE_TRIGGER"
        )
        
        for ((index, action) in actions.withIndex()) {
            Log.d(TAG, "Attempt ${index + 1}/${actions.size}: $action")
            
            try {
                val intent = Intent(action)
                intent.putExtra("trigger", true)
                intent.putExtra("scanner_trigger", true)
                intent.putExtra("com.cipherlab.scanner.trigger", true)
                
                context.sendBroadcast(intent)
                Log.d(TAG, "✓ Successfully sent broadcast: $action")
                return true
                
            } catch (e: SecurityException) {
                Log.w(TAG, "✗ Permission denied for $action: ${e.message}")
            } catch (e: Exception) {
                Log.w(TAG, "✗ Failed to send broadcast $action: ${e.message}")
            }
        }
        
        Log.e(TAG, "All scanner trigger attempts failed")
        return false
    }
    
    /**
     * Получает информацию о системе для диагностики
     */
    fun getSystemInfo(): String {
        return buildString {
            appendLine("=== System Information ===")
            appendLine("Manufacturer: ${android.os.Build.MANUFACTURER}")
            appendLine("Model: ${android.os.Build.MODEL}")
            appendLine("Device: ${android.os.Build.DEVICE}")
            appendLine("Product: ${android.os.Build.PRODUCT}")
            appendLine("Android Version: ${android.os.Build.VERSION.RELEASE}")
            appendLine("API Level: ${android.os.Build.VERSION.SDK_INT}")
            appendLine("Brand: ${android.os.Build.BRAND}")
            appendLine("Hardware: ${android.os.Build.HARDWARE}")
        }
    }
}
