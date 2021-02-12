package com.amplifyframework.sample.appsync.todo;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.api.aws.AWSApiPlugin;
import com.amplifyframework.api.aws.GsonVariablesSerializer;
import com.amplifyframework.api.graphql.GraphQLRequest;
import com.amplifyframework.api.graphql.SimpleGraphQLRequest;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.logging.AndroidLoggingPlugin;
import com.amplifyframework.logging.LogLevel;
import com.amplifyframework.util.GsonObjectConverter;

import com.amazonaws.amplify.generated.graphql.CreateTodoMutation;
import com.amazonaws.amplify.generated.graphql.ListTodosQuery;
import com.apollographql.apollo.api.Operation;
import com.apollographql.apollo.internal.json.InputFieldJsonWriter;
import com.apollographql.apollo.internal.json.JsonWriter;
import com.apollographql.apollo.internal.response.ScalarTypeAdapters;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import okio.Buffer;
import type.CreateTodoInput;
import type.ModelStringInput;
import type.ModelTodoFilterInput;

public class AmplifyMainActivity extends AppCompatActivity {
    private final static String TAG = "AmplifyMainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            Amplify.addPlugin(new AndroidLoggingPlugin(LogLevel.DEBUG));
            Amplify.addPlugin(new AWSApiPlugin());
            Amplify.configure(getApplicationContext());
        } catch (AmplifyException e) {
            e.printStackTrace();
        }
        create();
    }

    public void create() {
        CreateTodoMutation createTodoMutation = CreateTodoMutation.builder()
                .input(CreateTodoInput.builder()
                        .name("Mow the grass")
                        .description("Front and back")
                        .id(UUID.randomUUID().toString())
                        .build())
                .build();

        Amplify.API.mutate(requestFromOperation(createTodoMutation, CreateTodoMutation.Data.class),
                response -> {
                    Log.d(TAG, "CreateTodo response: " + response);
                    query();
                },
                error -> Log.e(TAG, "CreateTodo error: " + error));
    }
    public void query(){
        ListTodosQuery query = ListTodosQuery.builder().build();

        Amplify.API.query(requestFromOperation(query, ListTodosQuery.Data.class),
                response -> Log.d(TAG, "ListTodos response: " + response),
                error -> Log.e(TAG, "ListTodos error: " + error)
        );
    }

    private <D extends Operation.Data, T, V extends Operation.Variables>
            GraphQLRequest<R> requestFromOperation(Operation<D, T, V> operation, Type responseType) {
        String variablesString = getVariablesString(operation);
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(variablesString, JsonObject.class);
        Map<String, Object> variables = GsonObjectConverter.toMap(jsonObject);
        GraphQLRequest<R> request =  new SimpleGraphQLRequest<>(operation.queryDocument(),
            variables,
            responseType,
            new GsonVariablesSerializer());
        Log.d(TAG, "Request string: " + request.getContent());
        return request;
    }

    private <D extends Operation.Data, T, V extends Operation.Variables>
            String getVariablesString(Operation<D, T, V> operation) {
        ScalarTypeAdapters scalarTypeAdapters = new ScalarTypeAdapters(new LinkedHashMap<>());
        Buffer buffer = new Buffer();
        JsonWriter jsonWriter = JsonWriter.of(buffer);
        try {
            jsonWriter.beginObject();
            operation.variables().marshaller().marshal(new InputFieldJsonWriter(jsonWriter, scalarTypeAdapters));
            jsonWriter.endObject();
            jsonWriter.close();
            return new String(buffer.readByteArray(), Charset.defaultCharset());
        } catch (IOException e) {
            return "";
        }
    }
}
