package com.example.agita

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.*
import android.os.Bundle
import android.os.SystemClock
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var web: WebView
    private lateinit var sm: SensorManager
    private var lastShake = 0L

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        setContentView(web)

        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.mediaPlaybackRequiresUserGesture = false
        web.webViewClient = WebViewClient()
        web.loadUrl("file:///android_asset/index.html")

        sm = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (web.canGoBack()) web.goBack() else finish()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
            sm.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sm.unregisterListener(this)
    }

    override fun onSensorChanged(e: SensorEvent) {
        val g = sqrt(e.values[0] * e.values[0] + e.values[1] * e.values[1] + e.values[2] * e.values[2]) / SensorManager.GRAVITY_EARTH
        val now = SystemClock.elapsedRealtime()
        if (g > 2.7f && now - lastShake > 300) {
            lastShake = now
            web.evaluateJavascript("hit()", null)
        }
    }

    override fun onAccuracyChanged(s: Sensor?, a: Int) {}
}
