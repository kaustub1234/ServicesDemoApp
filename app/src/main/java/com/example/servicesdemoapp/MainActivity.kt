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
    lateinit private var btnRun: Button
    lateinit private var btnClear: Button
    lateinit private var startNewActivityBtn: Button
    lateinit private var mProgressBar: ProgressBar
    lateinit private var mLog: TextView
    lateinit private var mScroll: ScrollView;
    lateinit private var myBoundedService: MyBoundedService;
    private var isBounded: Boolean = false;
    private val TAG = "Services_logs";
    private val TAG2 = "Main Activity logs";

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

        Log.d(TAG2, "****** ONCREATE CALLED ******")

        mScroll = findViewById<ScrollView>(R.id.scrollLog)
        mLog = findViewById<TextView>(R.id.tvLog)
        mProgressBar = findViewById<ProgressBar>(R.id.progress_bar)
        mPlayButton = findViewById<Button>(R.id.btnPlayMusic)
        btnRun = findViewById<Button>(R.id.btnRun)
        btnClear = findViewById<Button>(R.id.btnClear)
        startNewActivityBtn = findViewById<Button>(R.id.startNewActivityBtn)

        mPlayButton.setOnClickListener(this)
        btnRun.setOnClickListener(this)
        btnClear.setOnClickListener(this)
        startNewActivityBtn.setOnClickListener(this)

    }


    override fun onStart() {
        Log.d(TAG2, "****** ONSTART CALLED ******")
        super.onStart()
    }

    override fun onResume() {
        Log.d(TAG2, "****** ONRESUME CALLED ******")
        super.onResume()
    }

    override fun onPause() {
        Log.d(TAG2, "****** ONPAUSE CALLED ******")
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG2, "****** ONSTOP CALLED ******")
        Log.d(TAG, "OnStop called..")
        if (isBounded) {
            unbindService(serviceConnection);
            isBounded = false;
        }
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG2, "****** ONRESTART CALLED ******")
    }

    override fun onDestroy() {
        Log.d(TAG2, "****** ONDESTROY CALLED ******")
        super.onDestroy()
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
                        intent.action = MainActivity.MUSIC_SERVICE_ACTION_START
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
                Log.d(TAG, "Bounded Service started!")
                var intent = Intent(this@MainActivity, MyBoundedService::class.java)
                bindService(intent, serviceConnection, BIND_AUTO_CREATE);

                //BIND_AUTO_CREATE WORKS AS LONG AS COMPONENT ARE BOUND TO IT
                //BIND_DEBUG_UNBIND CREATES DEBUG INFO FOR UNMATCHED BIND/UNBIND CALLS
                //BIND_NOT_FOREGROUND THE BOUND SERVICE WILL NEVER BE BROUGHTTO THE FOREGROUND PROCESS LEVEL
                displayProgressBar(true)
            }

            R.id.btnClear -> {
                val intent = Intent(this@MainActivity, MyBoundedService::class.java)
                stopService(intent)
                if (isBounded) {
                    unbindService(serviceConnection);
                    isBounded = false;
                }
                displayProgressBar(false)
            }

            R.id.startNewActivityBtn -> {
                val intent = Intent(this@MainActivity, SecondActivity::class.java)
                startActivity(intent)
            }
        }
    }
}