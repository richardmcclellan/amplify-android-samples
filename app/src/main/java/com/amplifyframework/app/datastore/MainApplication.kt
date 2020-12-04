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

import android.app.Application
import com.amplifyframework.AmplifyException
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.AmplifyConfiguration
import com.amplifyframework.datastore.AWSDataStorePlugin
import com.amplifyframework.datastore.DataStoreConfiguration
import com.amplifyframework.logging.AndroidLoggingPlugin
import com.amplifyframework.logging.LogLevel

@Suppress("unused") // It is referenced in AndroidManifest.xml.
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            Amplify.addPlugin(AndroidLoggingPlugin(LogLevel.VERBOSE))
            Amplify.addPlugin(AWSApiPlugin())
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.addPlugin(
                AWSDataStorePlugin(DataStoreConfiguration.builder()
                    .syncPageSize(100)
                    .syncMaxRecords(5000)
                    .build())
            )
            Amplify.configure(
                AmplifyConfiguration.builder(applicationContext)
                    .devMenuEnabled(false)
                    .build(),
                applicationContext)
            LOG.info("All set and ready to go!")
        } catch (exception: AmplifyException) {
            LOG.error("Configure failed: " + exception.message, exception)
        }
    }

    companion object {
        private val LOG = Amplify.Logging.forNamespace("app-datastore:mainapplication")
    }
}