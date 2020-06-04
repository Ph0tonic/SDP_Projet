package ch.epfl.sdp.ui

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import ch.epfl.sdp.R
import ch.epfl.sdp.database.data.Role
import ch.epfl.sdp.database.data_manager.MainDataManager
import ch.epfl.sdp.drone.Drone
import ch.epfl.sdp.ui.maps.MapActivity
import ch.epfl.sdp.ui.maps.offline.OfflineManagerActivity
import ch.epfl.sdp.ui.search_group.selection.SearchGroupSelectionActivity
import ch.epfl.sdp.ui.settings.SettingsActivity
import ch.epfl.sdp.utils.Auth
import ch.epfl.sdp.utils.CentralLocationManager
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var snackbar: Snackbar

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        configureNavigationView()
    }

    private fun configureNavigationView() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navView = findViewById<NavigationView>(R.id.nav_view)
        snackbar = Snackbar.make(navView, R.string.not_connected_message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(Color.BLACK).setTextColor(Color.WHITE)

        val navController = findNavController(R.id.nav_host_fragment)
        navView.setupWithNavController(navController)

        Auth.loggedIn.observe(this, Observer {
            navView.menu.findItem(R.id.nav_signout_button).isVisible = it
            navView.menu.findItem(R.id.nav_signout_button).isEnabled = it
        })

        navView.menu.findItem(R.id.nav_signout_button).setOnMenuItemClickListener {
            Auth.logout()
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    override fun onStart() {
        super.onStart()
        MainDataManager.goOnline()
        CentralLocationManager.configure(this)
        if (MainDataManager.role.value == Role.OPERATOR && !Drone.isConnectedLiveData.value!!) {
            snackbar.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        CentralLocationManager.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.activity_main_settings, menu)
        return true
    }

    // Opens the drawer instead of navigating up
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun openSettings(view: View) {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        CentralLocationManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun checkConnexion(view: View, action: () -> Unit) {
        if (Auth.loggedIn.value == false) {
            Auth.login(this) { success ->
                if (success) {
                    checkConnexion(view, action)
                }
            }
        } else {
            action()
        }
    }

    fun goToSearchGroupSelect(view: View) {
        checkConnexion(view) {
            val intent = Intent(this, SearchGroupSelectionActivity::class.java)
            startActivity(intent)
        }
    }

    fun startMission(view: View) {
        if (MainDataManager.groupId.value.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.warning_no_group_selected), Toast.LENGTH_LONG).show()
            return
        }
        checkConnexion(view) {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
    }

    fun workOffline(view: View) {
        MainDataManager.goOffline()
        val intent = Intent(this, MapActivity::class.java)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Auth.onActivityResult(requestCode, resultCode, data)
    }

    fun openDrawer(view: View) {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawerLayout.openDrawer(GravityCompat.START)
    }

    fun openMapForOfflineDownload (view: View?) {
        startActivity(Intent(this, OfflineManagerActivity::class.java))
    }

    fun openExistingOfflineMap (view: View?) {
        val intent = Intent(this, OfflineManagerActivity::class.java)
        intent.putExtra(getString(R.string.intent_key_show_delete_button), true)
        startActivity(intent)
    }
}
