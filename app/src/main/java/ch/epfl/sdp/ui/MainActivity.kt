package ch.epfl.sdp.ui

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import ch.epfl.sdp.R
import ch.epfl.sdp.database.data.Role
import ch.epfl.sdp.drone.Drone
import ch.epfl.sdp.ui.maps.MapActivity
import ch.epfl.sdp.ui.search_group.selection.SearchGroupSelectionActivity
import ch.epfl.sdp.ui.search_group.selection.SearchGroupSelectionActivity.Companion.SEARH_GROUP_ID_SELECTION_RESULT_TAG
import ch.epfl.sdp.utils.Auth
import ch.epfl.sdp.utils.CentralLocationManager
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var snackbar: Snackbar

    companion object {
        private const val SEARCH_GROUP_SELECTION_ACTIVITY_REQUEST_CODE = 7865
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val currentGroupId: MutableLiveData<String?> = MutableLiveData(null)
    private val currentRole: MutableLiveData<Role> = MutableLiveData(Role.RESCUER)

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        configureNavigationView()
        loadActiveGroupFromPrefs()

        findViewById<AppCompatImageButton>(R.id.mainSettingsButton).setOnClickListener(Navigation.createNavigateOnClickListener(R.id.nav_settings, null))
    }

    private fun configureNavigationView() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navView = findViewById<NavigationView>(R.id.nav_view)
        snackbar = Snackbar.make(navView, R.string.not_connected_message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(Color.BLACK).setTextColor(Color.WHITE)

        val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
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

    private fun loadActiveGroupFromPrefs() {
        currentGroupId.value = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString(getString(R.string.pref_key_current_group_id), null)
        currentGroupId.observe(this, Observer {
            PreferenceManager
                    .getDefaultSharedPreferences(this)
                    .edit()
                    .putString(getString(R.string.pref_key_current_group_id), it)
                    .apply()
        })

        //TODO Get role of current search group
    }

    override fun onStart() {
        super.onStart()
        CentralLocationManager.configure(this)
        if (currentRole.value == Role.OPERATOR && !Drone.isConnectedLiveData.value!!) {
            snackbar.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        CentralLocationManager.onDestroy()
    }

    // Opens the drawer instead of navigating up
    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
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
            startActivityForResult(intent, SEARCH_GROUP_SELECTION_ACTIVITY_REQUEST_CODE)
        }
    }

    fun startMission(view: View) {
        if (currentGroupId.value.isNullOrEmpty()) {
            Toast.makeText(this, getString(R.string.warning_no_group_selected), Toast.LENGTH_LONG).show()
            return
        }
        checkConnexion(view) {
            val intent = Intent(this, MapActivity::class.java)
                    .putExtra(getString(R.string.intent_key_group_id), currentGroupId.value)
                    .putExtra(getString(R.string.intent_key_role), Role.OPERATOR)
            startActivity(intent)
        }
    }

    fun workOffline(view: View) {
        checkConnexion(view) {
            val intent = Intent(this, MapActivity::class.java)
                    .putExtra(getString(R.string.intent_key_group_id), "dummy")
                    .putExtra(getString(R.string.intent_key_role), Role.RESCUER)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SEARCH_GROUP_SELECTION_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                currentGroupId.value = data!!.getStringExtra(SEARH_GROUP_ID_SELECTION_RESULT_TAG)
            }
        }
        Auth.onActivityResult(requestCode, resultCode, data)
    }

    fun openDrawer(view: View) {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawerLayout.openDrawer(GravityCompat.START)
    }
}
