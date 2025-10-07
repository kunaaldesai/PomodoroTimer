package com.example.pomodorotimer

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.kunaaldesai.pomodorotimer.R

class SettingsActivity : AppCompatActivity() {

    private lateinit var pomodoroTimeInput: EditText
    private lateinit var shortBreakTimeInput: EditText
    private lateinit var longBreakTimeInput: EditText
    private lateinit var cyclesUntilLongBreakInput: EditText
    private lateinit var saveButton: Button

    private val sharedPrefFile = "pomodoroTimerPreferences"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        pomodoroTimeInput = findViewById(R.id.pomodoroTimeInput)
        shortBreakTimeInput = findViewById(R.id.shortBreakTimeInput)
        longBreakTimeInput = findViewById(R.id.longBreakTimeInput)
        cyclesUntilLongBreakInput = findViewById(R.id.cyclesUntilLongBreakInput)
        saveButton = findViewById(R.id.saveButton)

        // Load saved settings
        loadSettings()

        saveButton.setOnClickListener {
            saveSettings()
            finish() // This will return to MainActivity
        }
    }

    private fun loadSettings() {
        val sharedPreferences: SharedPreferences = this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)

        pomodoroTimeInput.setText(sharedPreferences.getInt("POMODORO_TIME", 25).toString())
        shortBreakTimeInput.setText(sharedPreferences.getInt("SHORT_BREAK", 5).toString())
        longBreakTimeInput.setText(sharedPreferences.getInt("LONG_BREAK", 15).toString())
        cyclesUntilLongBreakInput.setText(sharedPreferences.getInt("CYCLES_UNTIL_LONG_BREAK", 4).toString())
    }

    private fun saveSettings() {
        val sharedPreferences: SharedPreferences = this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        editor.putInt("POMODORO_TIME", pomodoroTimeInput.text.toString().toInt())
        editor.putInt("SHORT_BREAK", shortBreakTimeInput.text.toString().toInt())
        editor.putInt("LONG_BREAK", longBreakTimeInput.text.toString().toInt())
        editor.putInt("CYCLES_UNTIL_LONG_BREAK", cyclesUntilLongBreakInput.text.toString().toInt())
        editor.putBoolean("SETTINGS_CHANGED", true) // Indicate that settings have been changed
        editor.apply()
    }
}
