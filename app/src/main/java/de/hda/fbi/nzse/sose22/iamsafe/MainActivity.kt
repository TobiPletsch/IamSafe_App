package de.hda.fbi.nzse.sose22.iamsafe

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_containerview_main) as NavHostFragment
        navController = navHostFragment.findNavController()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation_main)
        val navController = findNavController(R.id.fragment_containerview_main)

        // change action bar title when switching between fragments
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.safeFragment, R.id.contactsFragment, R.id.eventsFragment)
        )

        setupActionBarWithNavController(navController, appBarConfiguration)

        bottomNavigationView.setupWithNavController(navController)
        // showing the back button in action bar
        if (actionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }

    //For the automatically generated back button in the toolbar
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_settings, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // @see https://stackoverflow.com/a/18852126
        if(resources.getResourceName(item.itemId).endsWith("id/logoutMenuButton")) {
            Firebase.auth.signOut()

            Intent(this, LoginActivity::class.java).also { intent = it }
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}