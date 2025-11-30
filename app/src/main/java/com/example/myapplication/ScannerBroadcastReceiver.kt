package com.example.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * BroadcastReceiver для получения результатов сканирования от CipherLab RK26
 */
class ScannerBroadcastReceiver(
    private val onScanResult: (String) -> Unit
) : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "ScannerBroadcastReceiver"
        
        // Основные действия для получения результатов сканирования CipherLab
        private const val ACTION_SCAN_RESULT = "com.cipherlab.scanner.ACTION_SCAN_RESULT"
        private const val ACTION_BARCODE_RESULT = "com.cipherlab.barcode.ACTION_RESULT"
        private const val ACTION_SCAN_DATA = "com.cipherlab.scanner.ACTION_SCAN_DATA"
        private const val ACTION_SCAN = "com.cipherlab.scanner.ACTION_SCAN"
        
        // Дополнительные действия для RK26
        private const val ACTION_SCAN_START = "com.cipherlab.scanner.ACTION_SCAN_START"
        private const val ACTION_SCAN_STOP = "com.cipherlab.scanner.ACTION_SCAN_STOP"
        private const val ACTION_SCAN_STATUS = "com.cipherlab.scanner.ACTION_SCAN_STATUS"
        
        // Ключи для получения данных
        private const val EXTRA_BARCODE_DATA = "com.cipherlab.scanner.EXTRA_BARCODE_DATA"
        private const val EXTRA_SCAN_DATA = "com.cipherlab.scanner.EXTRA_SCAN_DATA"
        private const val EXTRA_RESULT = "com.cipherlab.scanner.EXTRA_RESULT"
        private const val EXTRA_DATA = "data"
        private const val EXTRA_BARCODE = "barcode"
        private const val EXTRA_SCAN_RESULT = "scan_result"
        private const val EXTRA_SCANNER_DATA = "scanner_data"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received broadcast: ${intent.action}")
        Log.d(TAG, "Intent extras: ${intent.extras?.keySet()}")
        
        when (intent.action) {
            ACTION_SCAN_RESULT,
            ACTION_BARCODE_RESULT,
            ACTION_SCAN_DATA,
            ACTION_SCAN -> {
                extractScanData(intent)
            }
            ACTION_SCAN_START -> {
                Log.d(TAG, "Scanner started")
            }
            ACTION_SCAN_STOP -> {
                Log.d(TAG, "Scanner stopped")
            }
            ACTION_SCAN_STATUS -> {
                Log.d(TAG, "Scanner status update")
            }
            else -> {
                // Пробуем извлечь данные из любых других intent'ов
                // Это поможет поймать нестандартные события
                if (intent.extras != null) {
                    extractScanData(intent)
                }
            }
        }
    }
    
    private fun extractScanData(intent: Intent) {
        try {
            // Расширенный список ключей для получения данных сканирования RK26
            val scanData = intent.getStringExtra(EXTRA_BARCODE_DATA)
                ?: intent.getStringExtra(EXTRA_SCAN_DATA)
                ?: intent.getStringExtra(EXTRA_RESULT)
                ?: intent.getStringExtra(EXTRA_DATA)
                ?: intent.getStringExtra(EXTRA_BARCODE)
                ?: intent.getStringExtra(EXTRA_SCAN_RESULT)
                ?: intent.getStringExtra(EXTRA_SCANNER_DATA)
                ?: intent.getStringExtra("barcode")
                ?: intent.getStringExtra("result")
                ?: intent.getStringExtra("scan_result")
                ?: intent.getStringExtra("data")
                ?: intent.getStringExtra("value")
                ?: intent.getStringExtra("text")
            
            if (!scanData.isNullOrBlank()) {
                Log.d(TAG, "Successfully extracted scan result: $scanData")
                onScanResult(scanData)
            } else {
                Log.d(TAG, "No scan data found in intent extras")
                // Логируем все доступные ключи для отладки
                intent.extras?.let { extras ->
                    Log.d(TAG, "Available extras: ${extras.keySet()}")
                    for (key in extras.keySet()) {
                        val value = extras.get(key)
                        Log.d(TAG, "Extra '$key': $value (type: ${value?.javaClass?.simpleName})")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting scan data", e)
        }
    }
}
