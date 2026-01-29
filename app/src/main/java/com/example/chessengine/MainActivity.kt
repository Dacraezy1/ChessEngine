package com.example.chessengine

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private external fun getBestMove(): String
    private external fun setElo(elo: Int)

    private lateinit var chessBoardView: ChessBoardView
    private lateinit var statusText: TextView
    private var isFlipped = false
    private var showBestMove = true

    companion object {
        init {
            System.loadLibrary("humanchess")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        chessBoardView = findViewById(R.id.chess_board_view)
        val flipButton: Button = findViewById(R.id.flip_button)
        val bestMoveToggle: ToggleButton = findViewById(R.id.best_move_toggle)
        val eloSeekBar: SeekBar = findViewById(R.id.elo_seekbar)
        val eloValue: TextView = findViewById(R.id.elo_value)
        statusText = findViewById(R.id.status_text)

        // Initialize with standard chess position
        chessBoardView.initializeBoard()

        flipButton.setOnClickListener {
            isFlipped = !isFlipped
            chessBoardView.setFlipped(isFlipped)
        }

        bestMoveToggle.setOnCheckedChangeListener { _, isChecked ->
            showBestMove = isChecked
            chessBoardView.setShowBestMove(showBestMove)
        }

        eloSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val elo = 800 + progress
                eloValue.text = "$elo ELO"
                setElo(elo)
                updateStatus("Engine strength set to $elo ELO")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Initial ELO setting
        setElo(1500)
        eloValue.text = "1500 ELO"

        // Get initial best move
        updateBestMove()
    }

    private fun updateBestMove() {
        Thread {
            try {
                updateStatus("Engine thinking...")
                val bestMove = getBestMove()
                runOnUiThread {
                    chessBoardView.setBestMove(bestMove)
                    updateStatus("Best move: $bestMove")
                }
            } catch (e: Exception) {
                runOnUiThread {
                    updateStatus("Error calculating move: ${e.message}")
                }
            }
        }.start()
    }

    private fun updateStatus(message: String) {
        val currentTime = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        val currentText = statusText.text.toString()
        statusText.text = "$currentText\n[$currentTime] $message"
        // Scroll to bottom
        statusText.post {
            val scrollAmount = statusText.layout?.getLineTop(statusText.lineCount)?.minus(statusText.height)
            if (scrollAmount != null && scrollAmount > 0) {
                statusText.scrollTo(0, scrollAmount)
            }
        }
    }
}
