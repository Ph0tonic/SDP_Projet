package ch.epfl.sdp.ui.offlineMapsManaging

import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import ch.epfl.sdp.MainApplication
import ch.epfl.sdp.R

object DownloadProgressBarUtils {

    // Progress bar methods
    fun startProgress(downloadButton: Button, listButton: Button, progressBar: ProgressBar) { // Disable buttons
        downloadButton.isEnabled = false
        listButton.isEnabled = false
        // Start and show the progress bar
        progressBar.isIndeterminate = true
        progressBar.visibility = View.VISIBLE
    }

    fun endProgress(downloadButton: Button, listButton: Button, progressBar: ProgressBar) { // Don't notify more than once
        // Enable buttons
        downloadButton.isEnabled = true
        listButton.isEnabled = true
        // Stop and hide the progress bar
        progressBar.isIndeterminate = false
        progressBar.visibility = View.GONE
        // Show a toast
        val context = MainApplication.applicationContext()
        Toast.makeText(context, context.getString(R.string.end_progress_success), Toast.LENGTH_SHORT).show()
    }

    fun downloadingInProgress(percentage: Int, progressBar: ProgressBar) {
        progressBar.isIndeterminate = false
        progressBar.progress = percentage
    }

    fun deletingInProgress(progressBar: ProgressBar) {
        progressBar.isIndeterminate = true
        progressBar.visibility = View.VISIBLE
    }

    fun hideProgressBar(progressBar: ProgressBar) {
        progressBar.visibility = View.INVISIBLE
        progressBar.isIndeterminate = false
    }
}