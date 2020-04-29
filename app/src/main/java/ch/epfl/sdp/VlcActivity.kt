package ch.epfl.sdp

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import java.io.IOException

class VlcActivity : AppCompatActivity() {

    companion object {
        private const val USE_TEXTURE_VIEW = false
        private const val ENABLE_SUBTITLES = false
        private const val ASSET_FILENAME = "rtsp://192.168.1.131:8554/live" //must be your IP
        private val ARGS = arrayListOf("-vvv")//, "--live-caching=200")
    }

    private lateinit var mVideoLayout: VLCVideoLayout
    private lateinit var mLibVLC: LibVLC
    private lateinit var mMediaPlayer: MediaPlayer
    private var started: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vlc)

        mLibVLC = LibVLC(this, ARGS)
        mMediaPlayer = MediaPlayer(mLibVLC)
        mVideoLayout = findViewById(R.id.video_layout)
    }

    override fun onStart() {
        super.onStart()
        mMediaPlayer.attachViews(mVideoLayout, null, ENABLE_SUBTITLES, USE_TEXTURE_VIEW)
    }

    fun switchVideo(view: View) {
        started = if (started) stopVideo()
        else startVideo()
    }

    private fun startVideo(): Boolean {
        try {
            val media = Media(mLibVLC, Uri.parse(ASSET_FILENAME))
            mMediaPlayer.media = media
            media.release()
        } catch (e: IOException) {
            return false
        }
        mMediaPlayer.play()
        return true
    }

    private fun stopVideo(): Boolean {
        mMediaPlayer.stop()
        return false
    }

    override fun onStop() {
        super.onStop()
        if (started) stopVideo()
        mMediaPlayer.detachViews()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMediaPlayer.release()
        mLibVLC.release()
    }
}
