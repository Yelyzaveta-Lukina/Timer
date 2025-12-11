package edu.lukina.timer

/**
 * Created by Yelyzaveta Luklina on 10/21/2025.
 */

// Imports the Context class, providing access to application-specific resources.
import android.content.Context
// Imports the Worker class from AndroidX WorkManager, the base class for background work.
import androidx.work.Worker
// Imports WorkerParameters, providing configuration for a Worker.
import androidx.work.WorkerParameters
// Imports the NotificationChannel class, required for creating notification channels on Android 8.0+.
import android.app.NotificationChannel
// Imports the NotificationManager class, used to manage and display notifications.
import android.app.NotificationManager
// Imports NotificationCompat.Builder, a helper for constructing notifications with backward compatibility.
import androidx.core.app.NotificationCompat
// Imports the Build class, which provides information about the current device's build.
import android.os.Build

// Defines a constant for the key used to pass the remaining milliseconds to the worker.
const val KEY_MILLISECONDS_REMAINING = "edu.lukina.timer.MILLIS_LEFT"
// Defines a constant for the ID of the notification channel used for the timer.
const val CHANNEL_ID_TIMER = "channel_timer"
// Defines a constant for the unique ID of the notification itself.
const val NOTIFICATION_ID = 0

// Declares the TimerWorker class, inheriting from Worker to perform background work.
class TimerWorker(context: Context, parameters: WorkerParameters) :
// The constructor takes the application context and worker parameters.
    Worker(context, parameters) {

    // Declares a private property to hold the system's NotificationManager service.
    private val notificationManager =
        // Gets a reference to the NotificationManager system service.
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Overrides the doWork method, which contains the code that will be executed in the background.
    override fun doWork(): Result {

        // Retrieves the remaining milliseconds from the input data passed to the worker.
        val remainingMillis = inputData.getLong(KEY_MILLISECONDS_REMAINING, 0)

        // Checks if no time was provided, which is considered a failure condition.
        if (remainingMillis == 0L) {
            // Returns a failure result, indicating the work could not be completed.
            return Result.failure()
        }

        // Calls the function to create the required notification channel for the timer notifications.
        createTimerNotificationChannel()

        // Creates a new instance of the TimerModel to manage the countdown logic.
        val timerModel = TimerModel()
        // Starts the timer model with the provided number of milliseconds.
        timerModel.start(remainingMillis)

        // A loop that continues as long as the timer model is running.
        while (timerModel.isRunning) {

            // Creates and displays a new notification showing the current remaining time.
            createTimerNotification(timerModel.toString())

            // Pauses the current thread for 1000 milliseconds (1 second) to create a one-second interval.
            Thread.sleep(1000)

            // Checks if the timer has reached zero.
            if (timerModel.remainingMilliseconds == 0L) {
                // Stops the timer model.
                timerModel.stop()

                // Creates the final notification to inform the user that the timer has finished.
                createTimerNotification("Timer is finished!")
            }
        }

        // Returns a success result, indicating that the work completed successfully.
        return Result.success()
    }

    // Defines a private function to create a notification channel,
    // which is required for Android 8.0 (Oreo) and higher.
    private fun createTimerNotificationChannel() {
        // Checks if the device's Android version is Oreo (API 26) or newer.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Gets the channel name string from the application's resources.
            val name = applicationContext.getString(R.string.channel_name)
            // Gets the channel description string from the application's resources.
            val description = applicationContext.getString(R.string.channel_description)
            // Sets the importance level for the notification channel
            // (low importance means less interruption).
            val importance = NotificationManager.IMPORTANCE_LOW
            // Creates a new NotificationChannel with the specified ID, name, and importance.
            val channel = NotificationChannel(CHANNEL_ID_TIMER, name, importance)
            // Assigns the description to the channel.
            channel.description = description

            // Registers the newly created channel with the system's NotificationManager.
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Defines a private function to create and display a notification with the given text.
    private fun createTimerNotification(text: String) {
        // Creates a notification using NotificationCompat.Builder for backward compatibility.
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID_TIMER)
            // Sets the small icon that appears in the status bar.
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            // Sets the title of the notification (e.g., the app's name).
            .setContentTitle(applicationContext.getString(R.string.app_name))
            // Sets the main content text of the notification (e.g., the remaining time).
            .setContentText(text)
            // Sets the priority of the notification,
            // which helps determine its intrusiveness on older Android versions.
            .setPriority(NotificationCompat.PRIORITY_LOW)
            // Builds the final Notification object.
            .build()

        // Posts the notification to be displayed to the user with its unique ID.
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
