/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amplifyframework.sample.datastore.blog

import com.amplifyframework.datastore.generated.model.Blog
import com.amplifyframework.datastore.generated.model.Post
import com.amplifyframework.sample.R
import com.amplifyframework.sample.datastore.ListActivity
import com.amplifyframework.sample.datastore.ViewModel
import java.util.*

class PostListActivity : ListActivity<Post>() {
    override fun createModel(): Post {
        return Post.builder()
            .title(applicationContext.resources.getStringArray(R.array.random_blogs).random())
            .build()
    }

    override fun updateModel(model: Post): Post {
        return model.copyOfBuilder()
                .title(UUID.randomUUID().toString())
                .build()
    }

    override fun getModelClass(): Class<out Post> {
        return Post::class.java
    }

    override fun getViewModel(model: Post): ViewModel<Post> {
        return PostViewModel(model)
    }
}