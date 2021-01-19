# Amplify Android Samples

This repository contains the following sample applications to demonstrate usage of Amplify Android.  
 - `datastore-blog`: Demonstrates DataStore with a Blog / Post / Comment schema.
 - `datastore-todo`: Demonstrates DataStore with a Todo schema, and has @auth restrictions.  
 - `auth-hostedui`: Demonstrates signIn/signOut with webUI

It also contains `sample-core`, a library module, which is a dependency of each sample app.

Below is a screenshot from `datastore-todo`:

<img src="./screenshot.png" width="300px"/>

## Setup
To use, install, and evaluate this application, please do the following:

1. Create an Amplify project within each sample app's folder, and add an API with Cognito auth:
    ```
    cd <sample-app-folder>
    amplify init
    amplify add api

    # Choose "Cognito User Pools" as the auth type
    # Configured advanced settings for the API and make sure conflict detection is set to AutoMerge
    ```

    For `datastore-todo`, use the following `schema.graphql`:
    ```
    type Todo @model
        @auth(rules: [{ allow: owner }]) {
    id: ID!
    name: String!
    description: String
    }
    ```

    For `datastore-blog`, use the following `schema.graphql`:
    ```
    type Blog @model 
        @auth(rules: [{ allow: owner, operations: [create, update, delete]}]) {
    id: ID!
    name: String!
    description: String
    posts: [Post] @connection(keyName: "byBlog", fields: ["id"])
    }

    type Post @model @key(name: "byBlog", fields: ["blogID"]) 
        @auth(rules: [{ allow: owner, operations: [create, update, delete]}]) { 
    id: ID!
    title: String!
    blogID: ID!
    blog: Blog @connection(fields: ["blogID"])
    comments: [Comment] @connection(keyName: "byPost", fields: ["id"])
    }

    type Comment @model @key(name: "byPost", fields: ["postID", "content"])
        @auth(rules: [{ allow: owner, operations: [create, update, delete]}]) {
    id: ID!
    postID: ID!
    post: Post @connection(fields: ["postID"])
    content: String!
    }
    ```

    Now create the backend:

    ```
    amplify push
    ```

2. Create a valid Cognito user, in your the user pool you just created.

    ```
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

 - Tap **Sign In** to sign in with the credentials in your `sign_in.xml`, and query all items
 - Tap the floating action button to save a new random item
 - Tap any item to delete it.  Text will be bold until the mutation has reached the server successfully.
 - Long press any item to update it. Text will be italized until the mutation has reached the server successfully.
 - Tap the Power button icon to signs out and clear DataStore
 - Tap the gear icon to go to a **Settings** page.
 - If DataStore is stopped for any reason (e.g. device put in airplane mode), a Snack bar is shown with a **Retry** button.  Tapping **Retry** will call `DataStore.start`.

## Amplify Library Development

When developing with the library, it is often very useful to be able to make changes to the sample app and the library within the same Android Studio project.  This allows you to test library changes in a sample app, even if the tests aren't passing yet, or the checkstyle isn't satisfied yet, both of which are required for publishing to Maven local.  It also enables stepping through sample app or library code with the debugger in the same session.  This can be setup with the following steps:


1. In `amplify-android/settings.gradle`, add references to each sample app module you want to test, as well as `sample-core` (this is a dependency of each sample app).

    ```
    include ':sample-datastore-blog'
    project(":sample-datastore-blog").projectDir=new File(rootDir, "../amplify-android-samples/datastore-blog/app")
    include ':sample-datastore-todo'
    project(":sample-datastore-todo").projectDir=new File(rootDir, "../amplify-android-samples/datastore-todo/app")
    include ':sample-auth-hostedui'
    project(":sample-auth-hostedui").projectDir=new File(rootDir, "../amplify-android-samples/auth-hostedui/app")
    include ':sample-core'
    project(":sample-core").projectDir=new File(rootDir, "../amplify-android-samples/sample-core/sample-core")
    ```

2. In `amplify-android/build.gradle`, add Kotlin support by replacing the `buildscript` section with:

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

3. In `sample-core/build.gradle`,  `datastore-todo/build.gradle`, and  `datastore-blog/build.gradle`, replace the Maven references to the Amplify library with references to the local modules.

    ```
    implementation project(':aws-api')
    implementation project(':aws-api-appsync')
    implementation project(':aws-auth-cognito')
    implementation project(':aws-datastore')
    implementation project(':core')
    ```