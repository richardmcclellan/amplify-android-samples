package com.amplifyframework.sample.auth.hostedui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.amplifyframework.core.Amplify
import com.amplifyframework.sample.R
import com.amplifyframework.sample.core.SettingsActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(com.amplifyframework.sample.core.R.id.toolbar)
        toolbar.title = "Hosted UI Sample"
        setSupportActionBar(toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_items, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sign_out -> {
                signOut()
                true
            }
            R.id.settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun signOut() {
        LOG.info("signOut")
        Amplify.Auth.signOut(
            {
                LOG.info("sign out succeeded")
                finish()
            }
        ) { LOG.error("sign out failed.", it) }
    }

    companion object {
        private val LOG = Amplify.Logging.forNamespace("auth-hostedui:MainActivity")
    }
}
