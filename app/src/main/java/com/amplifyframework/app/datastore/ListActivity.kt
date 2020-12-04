/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amplifyframework.app.datastore

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.Todo
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ListActivity : AppCompatActivity() {
    private val itemAdapter: SimpleItemRecyclerViewAdapter = SimpleItemRecyclerViewAdapter(listOf(), this)
    private var itemMap = linkedMapOf<String, Todo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            save(Dummy.todo())
        }

        val recyclerView = findViewById<RecyclerView>(R.id.item_list)
        recyclerView.adapter =  itemAdapter

        query()
    }

    private fun query() {
        Amplify.DataStore.query(Todo::class.java,
            { results ->
                LOG.debug("query succeeded")
                observe()
                itemMap = linkedMapOf(*results
                    .iterator()
                    .asSequence()
                    .toList()
                    .map { it.id to it }
                    .toTypedArray())
                reloadData()
            },
            { LOG.error("query failed: " + it.message, it) }
        )
    }

    private fun observe() {
        Amplify.DataStore.observe(Todo::class.java,
            {
                LOG.debug("observe started.")
            },
            {
                LOG.debug("observe item changed: " + it.item())
                query()
            },
            { LOG.error("observe failed: " + it.message, it) },
            { LOG.debug("observe completed.") })
    }

    private fun save(item: Todo) {
        Amplify.DataStore.save(item,
            {
                LOG.debug("save succeeded: " + it.item())
                itemMap[it.item().id] = it.item()
                reloadData()
            },
            { LOG.error("save failed: " + it.message, it) })
    }

    fun delete(item: Todo) {
        Amplify.DataStore.delete(item,
            {
                LOG.debug("deleted succeeded: " + it.item())
                itemMap.remove(it.item().id)
                reloadData()
            },
            { LOG.error("delete failed:" + it.message, it) })
    }

    private fun reloadData() {
        runOnUiThread {
            itemAdapter.values = itemMap.values.toList()
            itemAdapter.notifyDataSetChanged()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_items, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sign_out -> {
                signOut()
                true
            }
            R.id.settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun signOut() {
        Amplify.Auth.signOut(
            {
                LOG.debug("sign out succeeded")
                Amplify.DataStore.clear(
                    {
                        LOG.debug("clear succeeded.")
                        finish()
                    },
                    { LOG.error("clear failed: ", it) }
                )
            },
            { LOG.error("sign out failed:", it) }
        )
    }

    class SimpleItemRecyclerViewAdapter(var values: List<Todo>, private val delegate: ListActivity) :
            RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = values[position]
            holder.contentView.text = item.name

            with(holder.itemView) {
                tag = item
                setOnClickListener { v ->
                    delegate.delete(v.tag as Todo)
                }
            }
        }

        override fun getItemCount() = values.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val contentView: TextView = view.findViewById(R.id.content)
        }
    }

    companion object {
        private val LOG = Amplify.Logging.forNamespace("app-datastore:ListActivity")
    }
}