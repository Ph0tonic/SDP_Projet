package ch.epfl.sdp.ui.toast

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import ch.epfl.sdp.MainApplication


/**
 * A class for showing a `Toast` from background processes using a
 * `Handler`.
 *
 * @author kaolick
 * @source https://stackoverflow.com/questions/7378936/how-to-show-toast-message-from-background-thread
 */
class ToastHandler {
    private val handler: Handler = Handler(Looper.getMainLooper())

    /**
     * Runs the `Runnable` in a separate `Thread`.
     *
     * @param runnable
     * The `Runnable` containing the `Toast`
     */
    private fun runRunnable(runnable: Runnable) {
        var thread: Thread? = object : Thread() {
            override fun run() {
                handler.post(runnable)
            }
        }
        thread!!.start()
        thread.interrupt()
        thread = null
    }

    /**
     * Shows a `Toast` using a `Handler`. Can be used in
     * background processes.
     *
     * @param resId
     * The resource id of the string resource to use. Can be
     * formatted text.
     * @param duration
     * How long to display the message. Only use LENGTH_LONG or
     * LENGTH_SHORT from `Toast`.
     */
    fun showToast(resId: Int, duration: Int) {
        val runnable = Runnable { // Get the text for the given resource ID
            val text = MainApplication.applicationContext().resources.getString(resId)
            Toast.makeText(MainApplication.applicationContext(), text, duration).show()
        }
        runRunnable(runnable)
    }

    /*
    NOT USEFUL YET (may be later or may delete that if juged useless)

    /**
     * Shows a `Toast` using a `Handler`. Can be used in
     * background processes.
     *
     * @param _text
     * The text to show. Can be formatted text.
     * @param _duration
     * How long to display the message. Only use LENGTH_LONG or
     * LENGTH_SHORT from `Toast`.
     */
    fun showToast(text: CharSequence?, duration: Int) {
        val runnable = Runnable { Toast.makeText(mContext, _text, _duration).show() }
        runRunnable(runnable)
    }
     */
}