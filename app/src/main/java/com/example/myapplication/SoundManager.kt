package com.example.myapplication

import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log

/**
 * Менеджер звуков для воспроизведения звуковых сигналов
 */
class SoundManager(private val context: Context) {
    
    companion object {
        private const val TAG = "SoundManager"
    }
    
    private var mediaPlayer: MediaPlayer? = null
    
    /**
     * Воспроизводит звук успешной отправки
     */
    fun playSendSuccessSound() {
        try {
            // Сначала пытаемся воспроизвести кастомный звук
            playSound(R.raw.scan)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка воспроизведения кастомного звука успешной отправки", e)
            // Если не получилось, используем системный звук
            playSystemSound(RingtoneManager.TYPE_NOTIFICATION)
        }
    }
    
    /**
     * Воспроизводит звук ошибки
     */
    fun playErrorSound() {
        try {
            // Сначала пытаемся воспроизвести кастомный звук
            playSound(R.raw.error)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка воспроизведения кастомного звука ошибки", e)
            // Если не получилось, используем системный звук
            playSystemSound(RingtoneManager.TYPE_RINGTONE)
        }
    }
    
    /**
     * Воспроизводит звук по ID ресурса
     */
    private fun playSound(resourceId: Int) {
        try {
            // Останавливаем предыдущий звук если он играет
            stopCurrentSound()
            
            // Создаем новый MediaPlayer
            mediaPlayer = MediaPlayer.create(context, resourceId).apply {
                setOnCompletionListener {
                    // Освобождаем ресурсы после завершения воспроизведения
                    release()
                }
                setOnErrorListener { _, _, _ ->
                    Log.e(TAG, "Ошибка воспроизведения звука")
                    release()
                    true
                }
                start()
            }
            
            Log.d(TAG, "Воспроизводится звук: $resourceId")
            
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка создания MediaPlayer", e)
        }
    }
    
    /**
     * Воспроизводит системный звук
     */
    private fun playSystemSound(type: Int) {
        try {
            val notificationUri = RingtoneManager.getDefaultUri(type)
            val ringtone = RingtoneManager.getRingtone(context, notificationUri)
            ringtone?.play()
            Log.d(TAG, "Воспроизводится системный звук: $type")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка воспроизведения системного звука", e)
        }
    }
    
    /**
     * Останавливает текущий звук
     */
    private fun stopCurrentSound() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.stop()
            }
            player.release()
        }
        mediaPlayer = null
    }
    
    /**
     * Освобождает ресурсы
     */
    fun release() {
        stopCurrentSound()
    }
}
