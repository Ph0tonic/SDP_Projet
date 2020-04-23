package ch.epfl.sdp.ui.offlineMapsManaging

import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import ch.epfl.sdp.MainApplication
import ch.epfl.sdp.R

object ProgressBar {
    private lateinit var progressBar: ProgressBar

    fun initProgressBar(progressBar: ProgressBar){
        this.progressBar = progressBar
    }

    // Progress bar methods
    fun startProgress(downloadButton: Button, listButton: Button) { // Disable buttons
        downloadButton.isEnabled = false
        listButton.isEnabled = false
        // Start and show the progress bar
        progressBar.isIndeterminate = true
        progressBar.visibility = View.VISIBLE
    }

    fun endProgress(downloadButton: Button, listButton: Button) { // Don't notify more than once
        // Enable buttons
        downloadButton.isEnabled = true
        listButton.isEnabled = true
        // Stop and hide the progress bar
        progressBar.isIndeterminate = false
        progressBar.visibility = View.GONE
        // Show a toast
        OfflineManagerUtils.
                showToast(MainApplication.applicationContext().getString(R.string.end_progress_success))
    }

    fun downloadingInProgress(percentage : Int){
        progressBar.isIndeterminate = false
        progressBar.progress = percentage
    }

    fun deletingInProgress(){
        progressBar.isIndeterminate = true
        progressBar.visibility = View.VISIBLE
    }

    fun hideProgressBar(){
        progressBar.visibility = View.INVISIBLE
        progressBar.isIndeterminate = false
    }
}