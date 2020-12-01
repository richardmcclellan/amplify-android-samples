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

package com.amplifyframework.app.datastore

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amplifyframework.core.Amplify

class SettingsActivity : AppCompatActivity() {

    private val LOG = Amplify.Logging.forNamespace("app-datastore:ProfileActivity")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        fetchUserAttributes()
        fetchAuthSession()
    }

    private fun fetchAuthSession() {
        Amplify.Auth.fetchAuthSession(
            { LOG.debug("Auth session: " + it) },
            { LOG.error("Error retrieving AuthSession: ", it) }
        )
    }

    private fun fetchUserAttributes() {
        Amplify.Auth.fetchUserAttributes(
        { runOnUiThread({
            Toast.makeText(this, "Got user attributes!", Toast.LENGTH_SHORT).show()
            LOG.debug("User attributes: " + it)
        })},
        { runOnUiThread({
            Toast.makeText(this, "Failed to sign in", Toast.LENGTH_SHORT).show()
            LOG.error("Failed to sign in: " + it.message, it)
        })})
        }
}
