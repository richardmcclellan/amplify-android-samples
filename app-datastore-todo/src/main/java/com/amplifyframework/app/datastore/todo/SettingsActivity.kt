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

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.amplifyframework.auth.AuthSession
import com.amplifyframework.auth.AuthUserAttribute
import com.amplifyframework.core.Amplify

class SettingsActivity : AppCompatActivity() {
    var authSession: AuthSession? = null
    var userAttributes: List<AuthUserAttribute> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        fetchUserAttributes()
        fetchAuthSession()
    }

    private fun fetchAuthSession() {
        Amplify.Auth.fetchAuthSession({
                authSession = it
                loadContent()
            },
            { LOG.error("Error retrieving AuthSession: ", it) }
        )
    }

    private fun fetchUserAttributes() {
        Amplify.Auth.fetchUserAttributes(
            {
                userAttributes = it
                LOG.debug("User attributes: $it")
                loadContent()
            }, { LOG.error("Failed to sign in: " + it.message, it) }
        )
    }
    private fun loadContent() {
        runOnUiThread {
            val textView = findViewById<TextView>(R.id.textview);
            textView.setText("AuthSessionDetails: \n" + authSession.toString() + " \n UserAttributes: \n " + userAttributes);
        }
    }

    companion object {
        private val LOG = Amplify.Logging.forNamespace("app-datastore-todo:SettingsActivity")
    }
}
