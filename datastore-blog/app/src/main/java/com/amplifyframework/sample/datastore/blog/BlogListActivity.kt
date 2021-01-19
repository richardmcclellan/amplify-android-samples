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
import com.amplifyframework.sample.datastore.ListActivity
import com.amplifyframework.sample.datastore.ViewModel
import java.util.*

class BlogListActivity : ListActivity<Blog>() {
    override fun createModel(): Blog {
        return Blog.builder()
//                .name(applicationContext.resources.getStringArray(R.array.random_blogs).random())
                .name("a name")
                .description("a description")
                .build()
    }

    override fun updateModel(model: Blog): Blog {
        return model.copyOfBuilder()
                .name(UUID.randomUUID().toString())
                .build()
    }

    override fun getModelClass(): Class<out Blog> {
        return Blog::class.java
    }

    override fun getViewModel(model: Blog): ViewModel<Blog> {
        return BlogViewModel(model)
    }
}