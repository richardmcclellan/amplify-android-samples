package com.amplifyframework.sample.datastore.todo

import com.amplifyframework.datastore.generated.model.Todo
import com.amplifyframework.sample.R
import com.amplifyframework.sample.datastore.ListActivity
import com.amplifyframework.sample.datastore.ViewModel

class TodoListActivity: ListActivity<Todo>() {
    override fun createModel(): Todo {
        return Todo.builder()
            .name(applicationContext.resources.getStringArray(R.array.random_todos).random())
            .build()
    }

    override fun updateModel(model: Todo): Todo {
        return model.copyOfBuilder()
            .name(applicationContext.resources.getStringArray(R.array.random_todos).random())
            .build()
    }

    override fun getModelClass(): Class<out Todo> {
        return Todo::class.java
    }

    override fun getViewModel(model: Todo): ViewModel<Todo> {
        return TodoViewModel(model)
    }
}