# Amplify Android Samples

This repository contains the following sample applications to demonstrate usage of Amplify Android.  
 - `datastore-blog`: uses a Blog / Post / Comment schema.
 - `datastore-todo`: uses a Todo schema, and has @auth restrictions.  


<img src="./screenshot.png" width="300px"/>

## Setup
To use, install, and evaluate this application, please do the following:

1. Create an Amplify project within each sample app's folder, and add an API with Cognito auth:
```
cd datastore-todo
amplify init
amplify add api

# Choose Cognito User Pools" as the auth type
# Configured advanced settings for the API and make sure conflict detection is set to AutoMerge
# Use the following [`schema.graphql`](./schema.graphql):

type Todo @model
    @auth(rules: [{ allow: owner }]) {
  id: ID!
  name: String!
  description: String
}
```

amplify push
```

2. Create a valid Cognito user, in your the user pool you just created.
aws cognito-idp admin-create-user \
    --user-pool-id <pool_id_that_amplify_created> \
    --username <some_username>

aws cognito-idp admin-set-user-password \
    --user-pool-id <pool_id_that_amplify_created> \
    --username <some_username> \
    --password <some_password> \
    --permanent
```

3. Add the `<some_username>` and `<some_password>` values into
`app/src/main/res/values/sign_in.xml`. For example, your `sign_in.xml`
might look as below:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="username" type="id">alice</string>
    <string name="password" type="id">password</string>
</resources>
```

4. Build the app and install it on an Android device.

## Usage

 - Tap "Sign In" to sign in with the credentials in your sign_in.xml, and query all items
 - Tap the floating action button to save a new random item
 - Tap any item to delete it.  Text will be bold until the mutation has reached the server successfully.
 - Long press any item to update it. Text will be italized until the mutation has reached the server successfully.
 - Tap the Power button icon to signs out and clear DataStore
 - Tap the gear icon to go to a Settings page.
 - If DataStore is stopped for any reason (e.g. device put in airplane mode), a Snack bar, with a Retry button.  Retry will call DataStore.start.

## Amplify Library Development

When developing with the library, it is often very useful to be able to make changes to the sample app and the library within the same Android Studio project.  This allows you to test library changes in a sample app, even if the tests aren't passing yet, or the checkstyle isn't satisfied yet, both of which are required for publishing to Maven local.  It also enables stepping through sample app or library code with the debugger in the same session.  This can be setup with the following steps:


1. In amplify-android/settings.gradle, add references to each sample app.  `sample-core` is also needed as a dependency of any of the sample apps.

```
include ':sample-datastore-blog'
project(":sample-datastore-blog").projectDir=new File(rootDir, "../amplify-android-samples/datastore-blog/app")
include ':sample-datastore-todo'
project(":sample-datastore-todo").projectDir=new File(rootDir, "../amplify-android-samples/datastore-todo/app")
include ':sample-core'
project(":sample-core").projectDir=new File(rootDir, "../amplify-android-samples/core")
```

2. In amplify-android/build.gradle, add Kotlin support by replacing the `buildscript` section with:

```
buildscript {
    ext {
        kotlin_version = '1.4.20'
    }
    repositories {
        google()
        jcenter()
    }

    dependencies {
        classpath 'com.android.tools.build:gradle:4.0.1'
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
    }
}
```

3. In `sample-core/build.gradle` and `datastore-todo/build.gradle`, replace the Amplify library Maven references to reference the local modules.

```
implementation project(':aws-api')
implementation project(':aws-api-appsync')
implementation project(':aws-auth-cognito')
implementation project(':aws-datastore')
implementation project(':core')
```