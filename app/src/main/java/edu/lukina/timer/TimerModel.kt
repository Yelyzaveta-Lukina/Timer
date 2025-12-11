package edu.lukina.timer

/**
 * Created by Yelyzaveta Luklina on 10/21/2025.
 */

// Imports the SystemClock class, which provides access to a monotonic clock suitable for timing.
import android.os.SystemClock
// Imports the Locale class, used for formatting strings based on language and region.
import java.util.Locale

// Declares the TimerModel class, which encapsulates the logic for a countdown timer.
class TimerModel {
    // Declares a private property to store the future time (in milliseconds)
    // when the timer should end.
    private var targetTime: Long = 0
    // Declares a private property to store the remaining time when the timer is paused.
    private var timeLeft: Long = 0
    // Declares a private property to track whether the timer is currently running.
    private var running = false
    // Declares a private property to store the total initial duration of the timer.
    private var durationMillis: Long = 0

    // Declares a public read-only property to expose the running state of the timer.
    val isRunning: Boolean
        // The getter simply returns the value of the private 'running' property.
        get() {
            // Returns the current running state.
            return running
        }

    // Defines a function to start the timer with a specific duration in milliseconds.
    fun start(millisLeft: Long) {
        // Sets the total duration for this timer session.
        durationMillis = millisLeft
        // Calculates the target end time by adding the duration to the current system uptime.
        targetTime = SystemClock.uptimeMillis() + durationMillis
        // Sets the timer's state to running.
        running = true
    }

    // Defines an overloaded function to start the timer with hours, minutes, and seconds.
    fun start(hours: Int, minutes: Int, seconds: Int) {

        // Adds 1 second to the duration so the timer appears to stay on the current second for a full second.
        durationMillis = ((hours * 60 * 60 + minutes * 60 + seconds + 1) * 1000).toLong()
        // Calculates the target end time based on the new duration.
        targetTime = SystemClock.uptimeMillis() + durationMillis
        // Sets the timer's state to running.
        running = true
    }

    // Defines a function to stop the timer completely, resetting its state.
    fun stop() {
        // Sets the timer's state to not running.
        running = false
    }

    // Defines a function to pause the timer.
    fun pause() {
        // Calculates and saves the remaining time based on the current time and the target time.
        timeLeft = targetTime - SystemClock.uptimeMillis()
        // Sets the timer's state to not running.
        running = false
    }

    // Defines a function to resume the timer from a paused state.
    fun resume() {
        // Recalculates the target end time by adding the saved remaining time to the current time.
        targetTime = SystemClock.uptimeMillis() + timeLeft
        // Sets the timer's state to running.
        running = true
    }

    // Declares a public read-only property to get the remaining time in milliseconds.
    val remainingMilliseconds: Long
        // The getter calculates the remaining time.
        get() {
            // Checks if the timer is currently running.
            return if (running) {
                // Returns the greater of 0 or the calculated remaining time to prevent negative values.
                0L.coerceAtLeast(targetTime - SystemClock.uptimeMillis())
                // If the timer is not running, returns 0.
            } else 0
        }

    // Declares a public read-only property to get the seconds part of the remaining time.
    val remainingSeconds: Int
        // The getter calculates the seconds part.
        get() {
            // Checks if the timer is currently running.
            return if (running) {
                // Calculates the remaining seconds within the current minute.
                (remainingMilliseconds / 1000 % 60).toInt()
                // If the timer is not running, returns 0.
            } else 0
        }

    // Declares a public read-only property to get the minutes part of the remaining time.
    val remainingMinutes: Int
        // The getter calculates the minutes part.
        get() {
            // Checks if the timer is currently running.
            return if (running) {
                // Calculates the remaining minutes within the current hour.
                (remainingMilliseconds / 1000 / 60 % 60).toInt()
                // If the timer is not running, returns 0.
            } else 0
        }

    // Declares a public read-only property to get the hours part of the remaining time.
    val remainingHours: Int
        // The getter calculates the hours part.
        get() {
            // Checks if the timer is currently running.
            return if (running) {
                // Calculates the total remaining hours.
                (remainingMilliseconds / 1000 / 60 / 60).toInt()
                // If the timer is not running, returns 0.
            } else 0
        }

    // Declares a public read-only property to calculate the timer's progress as a percentage.
    val progressPercent: Int
        // The getter calculates the percentage.
        get() {
            // Checks if the duration is not the default 1000ms to avoid division by zero or incorrect calculation.
            return if (durationMillis != 1000L) {
                // Ensures the progress is at most 100%.
                100.coerceAtMost(
                    // Calculates the elapsed time as a percentage of the total duration.
                    100 - ((remainingMilliseconds - 1000) * 100 /
                            // The total duration, adjusted by 1000ms.
                            (durationMillis - 1000)).toInt()
                )
                // If the duration is the default 1000ms, returns 0 to avoid errors.
            } else 0
        }

    // Overrides the default toString() method to provide a custom string representation of the timer.
    override fun toString(): String {
        // Returns a formatted string in HH:MM:SS format using the remaining time components.
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", remainingHours,
            remainingMinutes, remainingSeconds)
    }
}
