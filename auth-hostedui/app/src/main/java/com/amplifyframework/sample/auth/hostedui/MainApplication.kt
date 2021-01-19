package com.amplifyframework.sample.auth.hostedui

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
        LOG.info("MainApplication onCreate")
        try {
            Amplify.addPlugin(AndroidLoggingPlugin(LogLevel.VERBOSE))
            Amplify.addPlugin(AWSCognitoAuthPlugin())
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
        private val LOG = Amplify.Logging.forNamespace("auth-hostedui:mainapplication")
    }
}