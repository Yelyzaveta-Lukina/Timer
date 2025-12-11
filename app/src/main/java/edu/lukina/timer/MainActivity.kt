package edu.lukina.timer

/**
 * Created by Yelyzaveta Luklina on 10/21/2025.
 */

// Imports the Bundle class, used for passing data between Android components and saving instance state.
import android.os.Bundle
// Imports the View class, the basic building block for user interface components.
import android.view.View
// Imports the Button widget class.
import android.widget.Button
// Imports the NumberPicker widget class, used for selecting a number from a predefined range.
import android.widget.NumberPicker
// Imports the ProgressBar widget class.
import android.widget.ProgressBar
// Imports the TextView widget class.
import android.widget.TextView
// Imports AppCompatActivity, a base class for activities that use the support library action bar features.
import androidx.appcompat.app.AppCompatActivity
// Imports OneTimeWorkRequestBuilder, a helper for creating a one-time background work request.
import androidx.work.OneTimeWorkRequestBuilder
// Imports WorkManager, the class used to enqueue and manage background work.
import androidx.work.WorkManager
// Imports the WorkRequest class, the base class for defining background work.
import androidx.work.WorkRequest
// Imports the workDataOf function, a helper to easily create a Data object for a WorkRequest.
import androidx.work.workDataOf
// Imports Kotlin Coroutines components for managing asynchronous tasks.
import kotlinx.coroutines.*
// Imports DecimalFormat for formatting numbers, such as adding leading zeros.
import java.text.DecimalFormat

// Declares the MainActivity class, inheriting from AppCompatActivity to gain Activity functionality.
class MainActivity : AppCompatActivity() {
    // Declares a private, late-initialized property to hold the hour NumberPicker view.
    private lateinit var hourPicker: NumberPicker
    // Declares a private, late-initialized property to hold the minute NumberPicker view.
    private lateinit var minutePicker: NumberPicker
    // Declares a private, late-initialized property to hold the second NumberPicker view.
    private lateinit var secondPicker: NumberPicker
    // Declares a private, late-initialized property to hold the Start button.
    private lateinit var startButton: Button
    // Declares a private, late-initialized property to hold the Pause button.
    private lateinit var pauseButton: Button
    // Declares a private, late-initialized property to hold the Cancel button.
    private lateinit var cancelButton: Button
    // Declares a private, late-initialized property to hold the progress bar.
    private lateinit var timeRemainingProgressBar: ProgressBar
    // Declares a private, late-initialized property to hold the TextView for displaying remaining time.
    private lateinit var timeLeftTextView: TextView
    // Creates a private instance of the TimerModel to manage the timer's logic.
    private val timerModel = TimerModel()
    // Declares a private, nullable property to hold a reference to the running coroutine Job.
    private var timerJob: Job? = null

    // Overrides the onCreate method, which is called when the activity is first created.
    override fun onCreate(savedInstanceState: Bundle?) {
        // Calls the superclass's implementation of onCreate.
        super.onCreate(savedInstanceState)
        // Sets the user interface layout for this activity from the specified XML resource file.
        setContentView(R.layout.activity_main)

        // Finds and assigns the TextView for displaying the time left.
        timeLeftTextView = findViewById(R.id.time_left_text_view)
        // Hides the time left TextView initially.
        timeLeftTextView.visibility = View.INVISIBLE
        // Finds and assigns the ProgressBar for showing timer progress.
        timeRemainingProgressBar = findViewById(R.id.progress_bar)
        // Hides the progress bar initially.
        timeRemainingProgressBar.visibility = View.INVISIBLE

        // Finds and assigns the Start button view.
        startButton = findViewById(R.id.start_button)
        // Finds and assigns the Pause button view.
        pauseButton = findViewById(R.id.pause_button)
        // Finds and assigns the Cancel button view.
        cancelButton = findViewById(R.id.cancel_button)

        // Sets a click listener for the Start button to call the 'startButtonClick' method.
        startButton.setOnClickListener { startButtonClick() }
        // Sets a click listener for the Pause button to call the 'pauseButtonClick' method.
        pauseButton.setOnClickListener { pauseButtonClick() }
        // Sets a click listener for the Cancel button to call the 'cancelButtonClick' method.
        cancelButton.setOnClickListener { cancelButtonClick() }

        // Hides the Pause button until the timer is started.
        pauseButton.visibility = View.GONE
        // Hides the Cancel button until the timer is started.
        cancelButton.visibility = View.GONE

        // Creates a custom formatter to ensure the NumberPicker displays two digits (e.g., "05").
        val numFormat = NumberPicker.Formatter { i: Int ->
            // Uses DecimalFormat to format the integer with a leading zero if needed.
            DecimalFormat("00").format(i)
        }

        // Finds and assigns the hour NumberPicker view.
        hourPicker = findViewById(R.id.hours_picker)
        // Sets the minimum value for the hour picker to 0.
        hourPicker.minValue = 0
        // Sets the maximum value for the hour picker to 99.
        hourPicker.maxValue = 99
        // Applies the two-digit formatter to the hour picker.
        hourPicker.setFormatter(numFormat)

        // Finds and assigns the minute NumberPicker view.
        minutePicker = findViewById(R.id.minutes_picker)
        // Sets the minimum value for the minute picker to 0.
        minutePicker.minValue = 0
        // Sets the maximum value for the minute picker to 59.
        minutePicker.maxValue = 59
        // Applies the two-digit formatter to the minute picker.
        minutePicker.setFormatter(numFormat)

        // Finds and assigns the second NumberPicker view.
        secondPicker = findViewById(R.id.seconds_picker)
        // Sets the minimum value for the second picker to 0.
        secondPicker.minValue = 0
        // Sets the maximum value for the second picker to 59.
        secondPicker.maxValue = 59
        // Applies the two-digit formatter to the second picker.
        secondPicker.setFormatter(numFormat)
    }

    // Defines a private function to be executed when the Start button is clicked.
    private fun startButtonClick() {

        // Gets the selected value from the hour picker.
        val hours = hourPicker.value
        // Gets the selected value from the minute picker.
        val minutes = minutePicker.value
        // Gets the selected value from the second picker.
        val seconds = secondPicker.value

        // Checks if the total selected time is greater than zero.
        if (hours + minutes + seconds > 0) {

            // Makes the time left TextView visible.
            timeLeftTextView.visibility = View.VISIBLE
            // Resets the progress bar to 0.
            timeRemainingProgressBar.progress = 0
            // Makes the progress bar visible.
            timeRemainingProgressBar.visibility = View.VISIBLE

            // Hides the Start button.
            startButton.visibility = View.GONE
            // Makes the Pause button visible.
            pauseButton.visibility = View.VISIBLE
            // Sets the text of the Pause button to "Pause".
            pauseButton.setText(R.string.pause)
            // Makes the Cancel button visible.
            cancelButton.visibility = View.VISIBLE

            // Starts the timer logic in the TimerModel with the selected time.
            timerModel.start(hours, minutes, seconds)

            // Launches a coroutine on the main UI thread to update the timer display.
            timerJob = CoroutineScope(Dispatchers.Main).launch {
                // Calls the suspending function to update the UI periodically.
                updateTimer()
            }
        }
    }

    // Defines a private suspending function to update the UI while the timer is running.
    private suspend fun updateTimer() {
        // Loops as long as the timer's progress is less than 100%.
        while (timerModel.progressPercent < 100) {

            // Sets the TextView to the current formatted time string from the model (e.g., "01:23:45").
            timeLeftTextView.text = timerModel.toString()
            // Updates the progress bar with the current percentage from the model.
            timeRemainingProgressBar.progress = timerModel.progressPercent

            // Checks if the timer is still not complete.
            if (timerModel.progressPercent < 100) {
                // Pauses the coroutine for 100 milliseconds before the next update.
                delay(100)
            }
        }

        // Calls the function to handle the timer completion once the loop finishes.
        timerCompleted()
    }

    // Defines a private function to be executed when the Pause/Resume button is clicked.
    private fun pauseButtonClick() {
        // Checks if the timer is currently running.
        if (timerModel.isRunning) {
            // If running, pauses the timer logic in the model.
            timerModel.pause()
            // Cancels the existing coroutine job that updates the UI.
            timerJob?.cancel()
            // Changes the button text to "Resume".
            pauseButton.setText(R.string.resume)
        } else {
            // If paused, resumes the timer logic in the model.
            timerModel.resume()
            // Launches a new coroutine job to continue updating the UI.
            timerJob = CoroutineScope(Dispatchers.Main).launch {
                // Calls the suspending function to update the UI.
                updateTimer()
            }
            // Changes the button text back to "Pause".
            pauseButton.setText(R.string.pause)
        }
    }

    // Defines a private function to be executed when the Cancel button is clicked.
    private fun cancelButtonClick() {
        // Cancels the coroutine job that updates the UI.
        timerJob?.cancel()
        // Hides the time left TextView.
        timeLeftTextView.visibility = View.INVISIBLE
        // Hides the progress bar.
        timeRemainingProgressBar.visibility = View.INVISIBLE
        // Calls the function to reset the UI and timer state.
        timerCompleted()
    }

    // Overrides the onDestroy method, which is called just before the activity is destroyed.
    override fun onDestroy() {
        // Calls the superclass's implementation of onDestroy.
        super.onDestroy()
        // Cancels the coroutine job to prevent memory leaks if the activity is destroyed.
        timerJob?.cancel()
    }

    // Defines a private function to reset the UI and model when the timer is completed or canceled.
    private fun timerCompleted() {
        // Stops the timer logic in the model.
        timerModel.stop()
        // Sets the TextView to its initial "00:00:00" state.
        timeLeftTextView.text = getString(R.string.no_time)

        // Makes the Start button visible again.
        startButton.visibility = View.VISIBLE
        // Hides the Pause button.
        pauseButton.visibility = View.GONE
        // Hides the Cancel button.
        cancelButton.visibility =
            View.GONE
    }

    // Overrides the onStop method, which is called when the activity is no longer visible to the user.
    override fun onStop() {
        // Calls the superclass's implementation of onStop.
        super.onStop()

        // Checks if the timer is running when the app goes into the background.
        if (timerModel.isRunning) {
            // Creates a one-time work request for the TimerWorker.
            val timerWorkRequest: WorkRequest = OneTimeWorkRequestBuilder<TimerWorker>()
                // Creates a Data object to pass the remaining time to the worker.
                .setInputData(workDataOf(
                    // Puts the remaining milliseconds into the Data object with a specific key.
                    KEY_MILLISECONDS_REMAINING to timerModel.remainingMilliseconds
                ))
                // Builds the final WorkRequest.
                .build()

            // Enqueues the work request with the WorkManager to be executed in the background.
            WorkManager.getInstance(applicationContext).enqueue(timerWorkRequest)
        }
    }
}
