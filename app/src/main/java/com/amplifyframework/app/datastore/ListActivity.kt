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
import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.model.query.Where
import com.amplifyframework.datastore.DataStoreChannelEventName
import com.amplifyframework.datastore.generated.model.Status
import com.amplifyframework.datastore.generated.model.Todo
import com.amplifyframework.datastore.syncengine.OutboxMutationEvent
import com.amplifyframework.hub.HubChannel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ListActivity : AppCompatActivity() {
    private val itemAdapter: SimpleItemRecyclerViewAdapter = SimpleItemRecyclerViewAdapter(listOf(), setOf(),this)
    private var itemMap = linkedMapOf<String, Todo>()
    private var idsPendingSync = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            save(Dummy.todo(applicationContext))
        }

        val recyclerView = findViewById<RecyclerView>(R.id.item_list)
        recyclerView.adapter =  itemAdapter

        query()
        observe()
        subscribe()
    }

    private fun query() {
        LOG.info("query")
        Amplify.DataStore.query(Todo::class.java,
//            Where.sorted(Todo.NAME.ascending()),
            { results ->
                val todosList = results.iterator().asSequence().toList()
                val pairsList = todosList.map { it.id to it }
                val array = pairsList.toTypedArray()
                itemMap = linkedMapOf(*array)
                LOG.info("query succeeded: " + itemMap.size + " Todos")
                loadContent()
            },
            { LOG.error("query failed: " + it.message, it) }
        )
    }

    private fun observe() {
        LOG.info("observe")
        Amplify.DataStore.observe(Todo::class.java,
            {
                LOG.info("observe started.")
            },
            {
                LOG.info("observe item changed: " + it.item())
                query()
            },
            { LOG.error("observe failed: " + it.message, it) },
            { LOG.info("observe completed.") })
    }

    private fun subscribe() {
        Amplify.Hub.subscribe(HubChannel.DATASTORE) {
            LOG.debug(it.name + "(" + it.id + "): " + it.data)
            when (DataStoreChannelEventName.fromString(it.name)) {
                DataStoreChannelEventName.OUTBOX_MUTATION_PROCESSED -> {
                    idsPendingSync.remove((it.data as OutboxMutationEvent<Todo>).element.model.id)
                    loadContent()
                }
            }
        }
    }

    private fun save(item: Todo) {
        LOG.info("save")
        Amplify.DataStore.save(item,
            {
                LOG.info("save succeeded: " + it.item())
                idsPendingSync.add(it.item().id)
                itemMap[it.item().id] = it.item()
                loadContent()
            },
            { LOG.error("save failed: " + it.message, it) })
    }

    fun delete(item: Todo) {
        LOG.info("delete")
        Amplify.DataStore.delete(item,
            {
                LOG.info("deleted succeeded: " + it.item())
                idsPendingSync.add(it.item().id)
                itemMap.remove(it.item().id)
                loadContent()
            },
            { LOG.error("delete failed:" + it.message, it) })
    }

    private fun loadContent() {
        runOnUiThread {
            itemAdapter.values = itemMap.values.toList()
            itemAdapter.idsPendingSync = idsPendingSync;
            itemAdapter.notifyDataSetChanged()
            val emptyView = findViewById<TextView>(R.id.empty_view)
            emptyView.visibility = if (itemMap.size == 0) View.VISIBLE else View.GONE
            emptyView.setText("No Todos.  Tap + to create one!")
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
        LOG.info("signOut")
        Amplify.Auth.signOut(
            {
                LOG.info("sign out succeeded")
                Amplify.DataStore.clear(
                    {
                        LOG.info("clear succeeded.")
                        finish()
                    },
                    { LOG.error("clear failed: ", it) }
                )
            },
            { LOG.error("sign out failed:", it) }
        )
    }

    class SimpleItemRecyclerViewAdapter(var values: List<Todo>,
                                        var idsPendingSync: Set<String>,
                                        private val delegate: ListActivity) :
            RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {
        private val LOG = Amplify.Logging.forNamespace("app-datastore:SimpleItemRecyclerViewAdapter")

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = values[position]
            holder.contentView.text = item.name
            val typeface = if (idsPendingSync.contains(item.id)) Typeface.ITALIC else Typeface.NORMAL
            holder.contentView.setTypeface(null, typeface)

            with(holder.itemView) {
                tag = item
                setOnClickListener { v ->
                    delegate.delete(v.tag as Todo)
                }
            }
        }

        override fun getItemCount(): Int = values.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val contentView: TextView = view.findViewById(R.id.content)
        }
    }

    companion object {
        private val LOG = Amplify.Logging.forNamespace("app-datastore:ListActivity")
    }
}
