package ch.epfl.sdp

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import java.io.IOException

class VlcActivity : AppCompatActivity() {
    private val USE_TEXTURE_VIEW = false
    private val ENABLE_SUBTITLES = true
    private val ASSET_FILENAME = "rtsp://192.168.1.120:8554/live"
    private lateinit var mVideoLayout: VLCVideoLayout
    private lateinit var mLibVLC: LibVLC
    private lateinit var mMediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vlc)
        val args: ArrayList<String> = ArrayList()
        args.add("-vvv")
        args.add("--live-caching=100")
        mLibVLC = LibVLC(this, args)
        mMediaPlayer = MediaPlayer(mLibVLC)
        mVideoLayout = findViewById(R.id.video_layout)
    }

    override fun onDestroy() {
        super.onDestroy()
        mMediaPlayer.release()
        mLibVLC.release()
    }

    override fun onStart() {
        super.onStart()
        mMediaPlayer.attachViews(mVideoLayout, null, ENABLE_SUBTITLES, USE_TEXTURE_VIEW)
        try {
            val media = Media(mLibVLC, Uri.parse(ASSET_FILENAME))
            mMediaPlayer.media = media
            media.release()
        } catch (e: IOException) {
            throw RuntimeException("Invalid asset folder")
            //return
        }
        mMediaPlayer.play()
    }

    override fun onStop() {
        super.onStop()
        mMediaPlayer.stop()
        mMediaPlayer.detachViews()
    }


}
