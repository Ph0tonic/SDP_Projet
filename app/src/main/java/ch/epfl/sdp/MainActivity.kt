package ch.epfl.sdp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun login() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    fun greetMessage() {
        val editText = findViewById<EditText>(R.id.mainName)
        val message = editText.text.toString()
        val textView = findViewById<TextView>(R.id.greetingMessage)
        textView.text = "Hello $message!"
    }

    fun goToSettings(){
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
}