package com.amplifyframework.sample.auth.hostedui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.sample.R
import com.amplifyframework.sample.core.SettingsActivity
import com.google.android.material.snackbar.Snackbar

class SignInActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LOG.info("SignInActivity onCreate")
        setContentView(R.layout.activity_sign_in)
        findViewById<Button>(R.id.sign_in_button).setOnClickListener { signIn() }
    }

    private fun signIn() {
        Amplify.Auth.signInWithWebUI(this,
            {
                LOG.info("Sign in succeeded: " + it)
                runOnUiThread {
                    startActivity(Intent(this, MainActivity::class.java))
                }
            },
            {
                LOG.error("Sign in failed: ${it.message}", it)
                runOnUiThread {
                    Snackbar.make(
                        findViewById(R.id.constraint_layout),
                        "Sign in failed: ${it.message}",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AWSCognitoAuthPlugin.WEB_UI_SIGN_IN_ACTIVITY_CODE) {
            Amplify.Auth.handleWebUISignInResponse(data)
        }
    }

    companion object {
        private val LOG = Amplify.Logging.forNamespace("auth-hostedui:SignInActivity")
    }
}