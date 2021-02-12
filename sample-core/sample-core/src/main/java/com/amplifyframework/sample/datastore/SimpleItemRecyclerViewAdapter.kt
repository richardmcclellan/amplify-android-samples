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

package com.amplifyframework.sample.datastore

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.model.Model
import com.amplifyframework.sample.core.R
import com.amplifyframework.sample.datastore.AdapterDelegate

class SimpleItemRecyclerViewAdapter<T : Model>(var values: List<ViewModel<T>>,
                                               var pendingSaveIds: Set<String>,
                                               var pendingDeleteIds: Set<String>,
                                               private val delegate: AdapterDelegate<T>
) : RecyclerView.Adapter<SimpleItemRecyclerViewAdapter<T>.ViewHolder>() {
    private val LOG = Amplify.Logging.forNamespace("app-datastore:SimpleItemRecyclerViewAdapter")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_content, parent, false)
        return ViewHolder(view)
    }

    @SuppressWarnings("unchecked")  // cast from Any? to ViewModel<T>
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.contentView.text = item.getTitle()

        // Italize items pending save
        // Bold items pending delete
        // Italize and bold items pending both
        if (pendingDeleteIds.contains(item.getId())) {
            if(pendingSaveIds.contains(item.getId())) {
                holder.contentView.setTypeface(null, Typeface.BOLD_ITALIC)
            } else {
                holder.contentView.setTypeface(null, Typeface.BOLD)
            }
        } else {
            if(pendingSaveIds.contains(item.getId())) {
                holder.contentView.setTypeface(null, Typeface.ITALIC)
            } else {
                holder.contentView.setTypeface(null, Typeface.NORMAL)
            }
        }

        with(holder.itemView) {
            tag = item
            setOnClickListener { v ->
                delegate.onClick((v.tag as ViewModel<T>).model)
            }
            setOnLongClickListener { v ->
                delegate.onLongClick((v.tag as ViewModel<T>).model)
            }
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val contentView: TextView = view.findViewById(R.id.content)
    }
}