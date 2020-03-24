package ch.epfl.sdp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import io.mavsdk.mission.Mission

class MainActivity : AppCompatActivity() {

    private val RC_SIGN_IN = 9001

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        /*fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }*/
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_home, R.id.nav_misson_design, R.id.nav_maps_managing), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        val gso = GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("272117878019-uf5rlbbkl6vhvkkmin8cumueil5ummfs.apps.googleusercontent.com")
                .requestEmail()
                .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    override fun onStart() {
        super.onStart()
        val account = GoogleSignIn.getLastSignedInAccount(this)
        //updateUserView(account?.displayName, account?.email)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main3, menu)
        return true
    }

    // Opens the drawer instead of navigating up
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun openSettings(menuItem: MenuItem?){
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    fun login(view: View?) {
        startActivityForResult(mGoogleSignInClient.signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException::class.java)
                updateUserView(account?.displayName, account?.email)
            } catch (e: ApiException) {
                Snackbar.make(findViewById(R.id.main_nav_header), "Could not sign in :(", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()
            }
        }
    }

    fun updateUserView(username: String?, userEmail: String?/*, userImage: Drawable*/){
        val usernameView: TextView = findViewById(R.id.nav_username)
        val userEmailView: TextView = findViewById(R.id.nav_user_email)
        val userImageView: ImageView = findViewById(R.id.nav_user_image)

        usernameView.text = username ?: "default_username"
        userEmailView.text = userEmail ?: "default_email"
        //userImageView.setImageDrawable(userImage)
    }

    fun goThere(view: View) {
        val latitudeTV: TextView = findViewById(R.id.targetLatitudeText)
        val longitudeTV: TextView = findViewById(R.id.targetLongitudeText)
        val targetLatitude = latitudeTV.text.toString().toDouble()
        val targetLongitude = longitudeTV.text.toString().toDouble()

        Log.d("--------------------","$targetLatitude")
        Log.d("--------------------","$targetLongitude")

        val target :Mission.MissionItem = Mission.MissionItem(
                targetLatitude,
                targetLongitude,
                10f,
                10f,
                true, Float.NaN, Float.NaN,
                Mission.MissionItem.CameraAction.NONE, Float.NaN,
                1.0)

        Drone.instance.mission
                .uploadMission(listOf(target))

        Drone.instance.mission
                .setReturnToLaunchAfterMission(true)
                .andThen(Drone.instance.action.arm())
                .andThen(Drone.instance.action.takeoff())
                .andThen(Drone.instance.mission.startMission())
                .subscribe()
    }
}
