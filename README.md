# DataStore Sample App

The purpose of this application is to test the Amplify DataStore.

<img src="./screenshot.png" width="300px"/>

## Setup
To use, install, and evaluate this application, please do the following:

1. Checkout this sample app project
```
git clone https://github.com/richardmcclellan/app-datastore-todo.git
cd app-datastore-todo

```

2. Create an Amplify project, and add an API with Cognito auth:
```
amplify init

# Set the resource directory to `app-datastore-todo/src/main/res` (instead of the default `app/src/main/res`)
 
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

3. Create a valid Cognito user, in your the user pool you just created.
aws cognito-idp admin-create-user \
    --user-pool-id <pool_id_that_amplify_created> \
    --username <some_username>

aws cognito-idp admin-set-user-password \
    --user-pool-id <pool_id_that_amplify_created> \
    --username <some_username> \
    --password <some_password> \
    --permanent
```

4. Add the `<some_username>` and `<some_password>` values into
`app-datastore-todo/src/main/res/values/sign_in.xml`. For example, your `sign_in.xml`
might look as below:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="username" type="id">alice</string>
    <string name="password" type="id">password</string>
</resources>
```

5. Build the app and install it on an Android device.

## Usage

 - Tap "Sign In" to sign in with the credentials in your sign_in.xml, and query all Todos
 - Tap the floating action button to save a new random Todo
 - Tap any Todo to delete it.
 - Tap the Power button icon to signs out and clear DataStore
 - Tap the gear icon to go to a Settings page.

## Amplify Library Development

When developing with the library, it is often very useful to be able to make changes to the sample app and the library within the same Android Studio project.  This allows you to test library changes in a sample app, even if the tests aren't passing yet, or the checkstyle isn't satisfied yet.  It also enables stepping through sample app or library code with the debugger in the same session.  This can be setup with the following steps:

1. Create a symlink in the amplify-android project folder to the app module folder.

```
ln -s ~/workspace/app-datastore-todo/app-datastore-todo ~/workspace/amplify-android/app-datastore-todo
```

2. In amplify-android/settings.gradle, add the new module:

```
include ':app-datastore-todo'
```

3. In amplify-android/build.gradle, add Kotlin support by replacing the `buildscript` section with:

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


4. In app/build.gradle, replace the Amplify library references to reference the local modules.

```
implementation project(':aws-api')
implementation project(':aws-auth-cognito')
implementation project(':aws-datastore')
```

