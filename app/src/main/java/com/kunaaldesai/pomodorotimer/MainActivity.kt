package com.kunaaldesai.pomodorotimer

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private enum class TimerState {
        WORK, SHORT_BREAK, LONG_BREAK
    }

    private var pomodoroLength: Int = 25 * 60
    private var shortBreakLength: Int = 5 * 60
    private var longBreakLength: Int = 15 * 60
    private var currentTimerValue: Int = pomodoroLength
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var timerLabel: TextView
    private lateinit var startButton: Button
    private lateinit var resetButton: Button
    private lateinit var settingsButton: Button
    private lateinit var cyclesCompletedLabel: TextView
    private lateinit var cyclesUntilLongBreakLabel: TextView
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var vibrator: Vibrator
    private var isRunning = false
    private var cyclesCompleted = 0
    private var cyclesUntilLongBreak = 4
    private var currentTimerState = TimerState.WORK
    private var isVibrationEnabled = false

    private val sharedPrefFile = "pomodoroTimerPreferences"
    private val sharedPreferences by lazy {
        this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timerLabel = findViewById(R.id.timerLabel)
        startButton = findViewById(R.id.startButton)
        resetButton = findViewById(R.id.resetButton)
        settingsButton = findViewById(R.id.settingsButton)
        cyclesCompletedLabel = findViewById(R.id.cyclesCompleted)
        cyclesUntilLongBreakLabel = findViewById(R.id.cyclesUntilLongBreak)

        mediaPlayer = MediaPlayer.create(this, R.raw.timer_sound)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        startButton.setOnClickListener {
            if (isRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        resetButton.setOnClickListener {
            loadSettings() // Load settings and reset timer when 'reset' is pressed
        }

        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // Load saved settings on startup
        loadSettings()
    }

    override fun onResume() {
        super.onResume()
        // Reload settings only if they have changed
        if (sharedPreferences.getBoolean("SETTINGS_CHANGED", false)) {
            loadSettings()
            val editor = sharedPreferences.edit()
            editor.putBoolean("SETTINGS_CHANGED", false)
            editor.apply()
        }
    }

    private fun loadSettings() {
        pomodoroLength = sharedPreferences.getInt("POMODORO_TIME", 25) * 60
        shortBreakLength = sharedPreferences.getInt("SHORT_BREAK", 5) * 60
        longBreakLength = sharedPreferences.getInt("LONG_BREAK", 15) * 60
        cyclesUntilLongBreak = sharedPreferences.getInt("CYCLES_UNTIL_LONG_BREAK", 4)
        isVibrationEnabled = sharedPreferences.getBoolean("VIBRATE_SETTING", false)
        resetTimer()
        updateTimerDisplay()
    }

    private fun startTimer() {
        isRunning = true
        startButton.text = "Pause"
        handler.postDelayed(tickRunnable, 1000)
    }

    private fun pauseTimer() {
        isRunning = false
        startButton.text = "Start"
        handler.removeCallbacks(tickRunnable)
    }

    private val tickRunnable = object : Runnable {
        override fun run() {
            if (currentTimerValue > 0) {
                currentTimerValue--
                val mins = currentTimerValue / 60
                val secs = currentTimerValue % 60
                timerLabel.text = String.format("%02d:%02d", mins, secs)
                handler.postDelayed(this, 1000)
            } else {
                playSound()
                if (isVibrationEnabled) {
                    vibrator.vibrate(2000) // Vibrate for 2 seconds
                }
                when (currentTimerState) {
                    TimerState.WORK -> {
                        cyclesCompleted++
                        cyclesUntilLongBreak--
                        if (cyclesUntilLongBreak <= 0) {
                            cyclesUntilLongBreak = sharedPreferences.getInt("CYCLES_UNTIL_LONG_BREAK", 4)
                            currentTimerState = TimerState.LONG_BREAK
                            currentTimerValue = longBreakLength
                        } else {
                            currentTimerState = TimerState.SHORT_BREAK
                            currentTimerValue = shortBreakLength
                        }
                    }
                    TimerState.SHORT_BREAK -> {
                        currentTimerState = TimerState.WORK
                        currentTimerValue = pomodoroLength
                    }
                    TimerState.LONG_BREAK -> {
                        currentTimerState = TimerState.WORK
                        currentTimerValue = pomodoroLength
                    }
                }
                updateTimerDisplay()
                startTimer()  // Restart the timer automatically
            }
        }
    }

    private fun updateTimerDisplay() {
        val mins = currentTimerValue / 60
        val secs = currentTimerValue % 60
        timerLabel.text = String.format("%02d:%02d", mins, secs)
        cyclesCompletedLabel.text = "Cycles Completed: $cyclesCompleted"
        cyclesUntilLongBreakLabel.text = "Cycles until long break: $cyclesUntilLongBreak"
    }

    private fun resetTimer() {
        pauseTimer()
        currentTimerState = TimerState.WORK
        currentTimerValue = pomodoroLength
        updateTimerDisplay()
    }

    private fun playSound() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}
