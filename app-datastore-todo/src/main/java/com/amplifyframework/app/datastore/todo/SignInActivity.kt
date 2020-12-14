/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amplifyframework.app.datastore.todo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amplifyframework.auth.cognito.options.AWSCognitoAuthSignOutOptions
import com.amplifyframework.auth.cognito.options.AWSCognitoAuthWebUISignInOptions
import com.amplifyframework.core.Amplify
import com.google.android.material.snackbar.Snackbar

class SignInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LOG.info("SignInActivity onCreate")
        setContentView(R.layout.activity_sign_in)
        findViewById<Button>(R.id.sign_in_button).setOnClickListener { signIn() }
    }

    private fun signIn() {
        Amplify.Auth.signIn(
            applicationContext.resources.getString(R.string.username),
            applicationContext.resources.getString(R.string.password),
            {
                LOG.info("Sign in succeeded: " + it)
                startActivity(Intent(this, ListActivity::class.java))
            },
            { 
                LOG.error("Sign in failed: ${it.message}", it)
                runOnUiThread { Snackbar.make(findViewById(R.id.constraint_layout),"Sign in failed: ${it.message}", Snackbar.LENGTH_SHORT).show() }
            }
        )
    }

    companion object {
        private val LOG = Amplify.Logging.forNamespace("app-datastore-todo:SignInActivity")
    }
}
