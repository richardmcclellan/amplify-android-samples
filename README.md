# DataStore Sample App

The purpose of this application is to test the Amplify DataStore.

<img src="./screenshot.png" width="300px"/>

## Installation
To use, install, and evaluate this application, please do the following:

2. Import this current project into Android studio. Try to build it.

3. Using the [`schema.graphql`](./schema.graphql) as below,
   generate models and deploy an AppSync backend. Make sure that this
   step updates your local configuration.

```graphql
type Todo @model
    @auth(rules: [{ allow: owner }]) {
  id: ID!
  name: String!
  description: String
  status: Status!
  dueDate: AWSDateTime
}

enum Status {
    NOT_STARTED
    IN_PROGRESS
    DONE
}

type Blog @model
    @auth(rules: [{ allow: owner, operations: [create, update, delete] }]) {
  id: ID!
  name: String!
  posts: [Post] @connection(keyName: "byBlog", fields: ["id"])
}

type Post @model
    @key(name: "byBlog", fields: ["blogID"])
    @auth(rules: [{ allow: owner, operations: [create, update, delete] }]) {
  id: ID!
  title: String!
  blogID: ID!
  blog: Blog @connection(fields: ["blogID"])
  comments: [Comment] @connection(keyName: "byPost", fields: ["id"])
  publishDate: AWSDateTime
}

type Comment @model
    @key(name: "byPost", fields: ["postID", "content"])
    @auth(rules: [{ allow: owner, operations: [create, update, delete] }]) {
  id: ID!
  postID: ID!
  post: Post @connection(fields: ["postID"])
  content: String!
}
```

Use `amplify init`, `amplify add api`. Follow the [guide to add a DataStore endpoint](https://docs.amplify.aws/lib/datastore/getting-started/q/platform/android#option-2-use-amplify-cli) --
except choose "Cognito User Pools" as the auth type, instead of API key. Run `amplify push` when done, and wait.

4. Create a valid Cognito user, in your the user pool you just created.
```sh
aws cognito-idp admin-create-user \
    --user-pool-id <pool_id_that_amplify_created> \
    --username <some_username>

aws cognito-idp admin-set-user-password \
    --user-pool-id <pool_id_that_amplify_created> \
    --username <some_username> \
    --password <some_password> \
    --permanent
```

Add the `<some_username>` and `<some_password>` values into
`app/src/main/res/values/sign_in.xml`. For example, your `sign_in.xml`
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

1. Create a symlink from amplify-android/app to app-datastore/app

```
ln -s ~/workspace/app-datastore/app ~/workspace/amplify-android/app
```

2. In amplify-android/settings.gradle, add the new module:

```
include ':app'
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


4. In app/build.gradle, modify the Amplify library references to reference the local modules.

```
implementation project(':aws-api')
implementation project(':aws-datastore')
implementation project(':aws-auth-cognito')
```

