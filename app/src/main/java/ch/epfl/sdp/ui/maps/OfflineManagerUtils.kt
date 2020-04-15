package ch.epfl.sdp.ui.maps

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast


object OfflineManagerUtils {


    // Progress bar methods
    fun startProgress(downloadButton : Button, listButton : Button, progressBar : ProgressBar) : Boolean{ // Disable buttons
        downloadButton.isEnabled = false
        listButton.isEnabled = false
        // Start and show the progress bar
        progressBar.isIndeterminate = true
        progressBar.visibility = View.VISIBLE
        return false //isEndNotified = false
    }

    fun endProgress(message : String, downloadButton: Button, listButton: Button, progressBar: ProgressBar, context : Context) : Boolean{ // Don't notify more than once

        // Enable buttons
        downloadButton.isEnabled = true
        listButton.isEnabled = true
        // Stop and hide the progress bar
        progressBar.isIndeterminate = false
        progressBar.visibility = View.GONE
        // Show a toast
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        return true
    }

}