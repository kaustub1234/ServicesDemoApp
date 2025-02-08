package com.example.servicesdemoapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class MyBoundedService : Service() {
    private val TAG = "Services_logs";
    private val mBinder = MyServiceBinder()
    private var mPlayer: MediaPlayer? = null

    companion object {
        const val MUSIC_COMPLETE = "MusicComplete"
    }

    inner class MyServiceBinder : Binder() {
        fun getService(): MyBoundedService {
            return this@MyBoundedService;
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "ONCREATE CALLED")
        mPlayer = MediaPlayer.create(this, R.raw.youngasthemorning)

        mPlayer!!.setOnCompletionListener {
            val intent = Intent(MUSIC_COMPLETE)
            intent.putExtra(MainActivity.MESSAGE_KEY, "done")
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "ONSTARTCOMMAND CALLED")

        when (intent?.action) {
            MainActivity.MUSIC_SERVICE_ACTION_PLAY -> {
                Log.d(TAG,"onStartCommand: play called")
                play()
            }

            MainActivity.MUSIC_SERVICE_ACTION_PAUSE -> {
                Log.d(TAG,"onStartCommand: pause called")
                pause()
            }

            MainActivity.MUSIC_SERVICE_ACTION_STOP -> {
                Log.d(TAG,"onStartCommand: stop called")
                stopForeground(true)
                stopSelf()
            }

            MainActivity.MUSIC_SERVICE_ACTION_START -> {
                Log.d(TAG,"onStartCommand: start called")
                showNotification()
            }
        }

        return START_NOT_STICKY;
    }

    fun play() {
        mPlayer!!.start()
    }

    fun pause() {
        mPlayer!!.pause()
    }

    private fun showNotification() {
        createNotificationChannel() // Required for Android 8.0+
        //Intent for play button
        val pIntent = Intent(this, MyBoundedService::class.java)
        pIntent.action = MainActivity.MUSIC_SERVICE_ACTION_PLAY
        val playIntent = PendingIntent.getService(this, 100, pIntent, PendingIntent.FLAG_IMMUTABLE)

        //Intent for pause button
        val psIntent = Intent(this, MyBoundedService::class.java)
        psIntent.action = MainActivity.MUSIC_SERVICE_ACTION_PAUSE
        val pauseIntent =
            PendingIntent.getService(this, 100, psIntent, PendingIntent.FLAG_IMMUTABLE)

        //Intent for stop button
        val sIntent = Intent(this, MyBoundedService::class.java)
        sIntent.action = MainActivity.MUSIC_SERVICE_ACTION_STOP
        val stopIntent = PendingIntent.getService(this, 100, sIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, "CHANNEL_ID")
            .setContentTitle("This a demo Notification title")
            .setContentText("This is demo music player").setSmallIcon(R.mipmap.ic_launcher)
         .addAction(
             NotificationCompat.Action(
                 android.R.drawable.ic_media_play, "Play", playIntent
             )
         ).addAction(
             NotificationCompat.Action(
                 android.R.drawable.ic_media_play, "Pause", pauseIntent
             )
         ).addAction(
             NotificationCompat.Action(
                 android.R.drawable.ic_media_play, "Stop", stopIntent
             )
         ).build()

        startForeground(123, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "My Channel"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("CHANNEL_ID", name, importance)

            val notificationManager =
                this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "ONBIND CALLED")
        return mBinder;

    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
        Log.d(TAG, "ONUNBIND CALLED")
    }

    //public client methods
    fun isPlaying(): Boolean {
        return mPlayer!!.isPlaying
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "ONDESTROY CALLED")
        mPlayer!!.release()
    }
}