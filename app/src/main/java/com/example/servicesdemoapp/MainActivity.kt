package com.example.servicesdemoapp

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), View.OnClickListener {

    lateinit private var mPlayButton: Button
    lateinit private var mProgressBar: ProgressBar
    lateinit private var mLog: TextView
    lateinit private var mScroll: ScrollView;
    lateinit private var myBoundedService: MyBoundedService;
    private var isBounded: Boolean = false;
    private val TAG = javaClass.kotlin.simpleName;
    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val myServiceBinder: MyBoundedService.MyServiceBinder =
                (binder as MyBoundedService.MyServiceBinder)
            myBoundedService = myServiceBinder.getService();
            isBounded = true;
            Log.d(TAG, "OnServiceConnected")

            if (myBoundedService.isPlaying()) {
                mPlayButton.text = "Pause"
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "OnServiceDisconnected")
            isBounded = false;
        }

    }

    companion object {
        const val MESSAGE_KEY = "message_key"
        const val MUSIC_SERVICE_ACTION_START = "com.example.servicesdemoapp.MainActivity.start"
        const val MUSIC_SERVICE_ACTION_PLAY = "com.example.servicesdemoapp.MainActivity.play"
        const val MUSIC_SERVICE_ACTION_PAUSE = "com.example.servicesdemoapp.MainActivity.pause"
        const val MUSIC_SERVICE_ACTION_STOP = "com.example.servicesdemoapp.MainActivity.stop"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mScroll = findViewById<ScrollView>(R.id.scrollLog)
        mLog = findViewById<TextView>(R.id.tvLog)
        mProgressBar = findViewById<ProgressBar>(R.id.progress_bar)
        mPlayButton = findViewById<Button>(R.id.btnPlayMusic)
    }


    override fun onStart() {
        super.onStart()
        var intent = Intent(this@MainActivity, MyBoundedService::class.java)
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);

        //BIND_AUTO_CREATE WORKS AS LONG AS COMPONENT ARE BOUND TO IT
        //BIND_DEBUG_UNBIND CREATES DEBUG INFO FOR UNMATCHED BIND/UNBIND CALLS
        //BIND_NOT_FOREGROUND THE BOUND SERVICE WILL NEVER BE BROUGHTTO THE FOREGROUND PROCESS LEVEL
    }

    override fun onStop() {
        super.onStop()
        if (isBounded) {
            unbindService(serviceConnection);
            isBounded = false;
        }
    }


    fun displayProgressBar(display: Boolean) {
        if (display) {
            mProgressBar.visibility = View.VISIBLE
        } else {
            mProgressBar.visibility = View.INVISIBLE
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnPlayMusic -> {
                if (isBounded) {
                    if (myBoundedService.isPlaying()) {
                        myBoundedService.pause()
                        mPlayButton.text = "Play"
                    } else {
                        val intent = Intent(this@MainActivity, MyBoundedService::class.java)
                        startService(intent)
                        myBoundedService.play()
                        mPlayButton.text = "Pause"
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Start service before playing music...",
                        Toast.LENGTH_SHORT
                    ).show();
                }
            }

            R.id.btnRun -> {
                Log.d(TAG, "Playing Music Buddy!")
                displayProgressBar(true)
            }

            R.id.btnClear -> {
                val intent = Intent(this@MainActivity, MyBoundedService::class.java)
                stopService(intent)
            }
        }
    }
}