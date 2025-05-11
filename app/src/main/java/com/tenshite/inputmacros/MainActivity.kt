package com.tenshite.inputmacros

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import android.widget.Button
import android.widget.TextView


class MainActivity : AppCompatActivity() {

    fun isAccessibilityServiceEnabled(context: Context, serviceClassName: String): Boolean {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices ?: "")

        while (colonSplitter.hasNext()) {
            val service = colonSplitter.next()
            if (service.equals("${context.packageName}/$serviceClassName", ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    fun promptUserToEnableAccessibility(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }


    override fun onResume() {
        super.onResume()

        val textView = findViewById<TextView>(R.id.text)
        if(isAccessibilityServiceEnabled(this, MyAccessibilityService::class.java.name)){
            textView.text = "Vše připraveno"
        }
        else{
            textView.text= "Zapnětě službu zpřístupnění pro tuto aplikaci\n\nV záložce se staženými službami vyberte tuto aplikaci\n Doporučuji zapnout i tlačtítko spojené s touto službou"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.SettingsButton)
        button.setOnClickListener {
            promptUserToEnableAccessibility(this)
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}