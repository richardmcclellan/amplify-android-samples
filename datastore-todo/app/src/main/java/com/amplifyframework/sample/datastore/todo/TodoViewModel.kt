package com.amplifyframework.sample.datastore.todo

import com.amplifyframework.datastore.generated.model.Todo
import com.amplifyframework.sample.datastore.ViewModel

class TodoViewModel(private val todo: Todo): ViewModel<Todo>(todo) {
    override fun getTitle(): String {
        return todo.name
    }
}