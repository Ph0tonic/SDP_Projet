package ch.epfl.sdp.ui.maps

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import ch.epfl.sdp.MainApplication
import ch.epfl.sdp.OfflineManagerActivity
import ch.epfl.sdp.R
import com.mapbox.mapboxsdk.offline.OfflineRegion
import org.json.JSONObject
import timber.log.Timber
import java.nio.charset.Charset


object OfflineManagerUtils {

    // Progress bar methods
    fun startProgress(downloadButton: Button, listButton: Button, progressBar: ProgressBar): Boolean { // Disable buttons
        downloadButton.isEnabled = false
        listButton.isEnabled = false
        // Start and show the progress bar
        progressBar.isIndeterminate = true
        progressBar.visibility = View.VISIBLE
        return false //isEndNotified = false
    }

    fun endProgress(downloadButton: Button, listButton: Button, progressBar: ProgressBar, context: Context): Boolean { // Don't notify more than once
        // Enable buttons
        downloadButton.isEnabled = true
        listButton.isEnabled = true
        // Stop and hide the progress bar
        progressBar.isIndeterminate = false
        progressBar.visibility = View.GONE
        // Show a toast
        showToast(context.getString(R.string.end_progress_success))
        return true
    }

    fun deleteOfflineRegion(offRegion: OfflineRegion, progressBar: ProgressBar, context: Context) {
        offRegion.delete(object : OfflineRegion.OfflineRegionDeleteCallback {
            override fun onDelete() { // Once the region is deleted, remove the
                // progressBar and display a toast
                progressBar.visibility = View.INVISIBLE
                progressBar.isIndeterminate = false
                Toast.makeText(context, context.getString(R.string.toast_region_deleted),
                        Toast.LENGTH_LONG).show()
            }

            override fun onError(error: String) {
                progressBar.visibility = View.INVISIBLE
                progressBar.isIndeterminate = false
                Timber.e("Error: %s", error)
            }
        })
    }


    // Get the region name from the offline region metadata
    fun getRegionName(offlineRegion: OfflineRegion): String {
        val regionName: String
        regionName = try {
            JSONObject(String(offlineRegion.metadata, Charset.forName(OfflineManagerActivity.JSON_CHARSET)))
                    .getString(OfflineManagerActivity.JSON_FIELD_REGION_NAME)
        } catch (exception: Exception) {
            Timber.e("Failed to decode metadata: %s", exception.message)
            String.format(MainApplication.applicationContext().getString(R.string.region_name), offlineRegion.id)
        }
        return regionName
    }

    fun showToast(message : String){
        Toast.makeText(MainApplication.applicationContext(), message, Toast.LENGTH_LONG).show()
    }

}