package com.amplifyframework.sample.appsync.todo;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.amplify.generated.graphql.CreateTodoMutation;
import com.amazonaws.amplify.generated.graphql.ListTodosQuery;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.UUID;
import javax.annotation.Nonnull;

import type.CreateTodoInput;

public class AppSyncMainActivity extends AppCompatActivity {
    private final static String TAG = "AppSyncMainActivity";

    private AWSAppSyncClient mAWSAppSyncClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");
        mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                // If you are using complex objects (S3) then uncomment
                //.s3ObjectManager(new S3ObjectManagerImplementation(new AmazonS3Client(AWSMobileClient.getInstance())))
                .build();
        create();
    }

    public void create() {
        Log.d(TAG, "creating a todo...");
        CreateTodoMutation createTodoMutation = CreateTodoMutation.builder()
                .input(CreateTodoInput.builder()
                        .name("Mow the grass")
                        .description("Front and back")
                        .id(UUID.randomUUID().toString())
                        .build())
                .build();
        mAWSAppSyncClient.mutate(createTodoMutation)
                .enqueue(new GraphQLCall.Callback<CreateTodoMutation.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<CreateTodoMutation.Data> response) {
                        Log.i(TAG, "Created todo: " + response.data().toString());
                        query();
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        Log.e(TAG, "Failed to create Todo", e);
                    }
                });
    }

    public void query() {
        ListTodosQuery query = ListTodosQuery.builder().build();
        mAWSAppSyncClient.query(query)
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(new GraphQLCall.Callback<ListTodosQuery.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<ListTodosQuery.Data> response) {
                        Log.i(TAG, "list todo Results: " + response.data().listTodos().items().toString());
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        Log.e(TAG, e.toString(), e);
                    }
                });
    }
}
