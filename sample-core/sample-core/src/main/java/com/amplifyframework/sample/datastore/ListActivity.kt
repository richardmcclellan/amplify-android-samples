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

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.model.Model
import com.amplifyframework.datastore.DataStoreChannelEventName
import com.amplifyframework.datastore.DataStoreItemChange
import com.amplifyframework.datastore.events.NetworkStatusEvent
import com.amplifyframework.datastore.syncengine.OutboxMutationEvent
import com.amplifyframework.hub.HubChannel
import com.amplifyframework.hub.SubscriptionToken
import com.amplifyframework.sample.core.R
import com.amplifyframework.sample.core.SettingsActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

abstract class ListActivity<T : Model> : AppCompatActivity(), AdapterDelegate<T> {
    private val itemAdapter: SimpleItemRecyclerViewAdapter<T> = SimpleItemRecyclerViewAdapter(listOf(), setOf(), setOf(), this)
    private var itemMap = linkedMapOf<String, T>()
    private var pendingSaveIds = mutableSetOf<String>()
    private var pendingDeleteIds = mutableSetOf<String>()
    private var subscriptionTokens = mutableSetOf<SubscriptionToken>()
    private lateinit var networkStatusBar: Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = getModelClass().simpleName
        setSupportActionBar(toolbar)

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            save(createModel())
        }

        val recyclerView = findViewById<RecyclerView>(R.id.item_list)
        recyclerView.adapter = itemAdapter

        val view = this.findViewById<CoordinatorLayout>(R.id.root_view);
        networkStatusBar = Snackbar.make(view, "DataStore is not in sync.", Snackbar.LENGTH_INDEFINITE)
            .setAction("Retry") { start() }
    }

    override fun onStart() {
        super.onStart()
        query()
        observe()
        subscribe()
    }

    override fun onStop() {
        super.onStop()
        unsubscribe()
    }

    abstract fun createModel(): T
    abstract fun updateModel(model: T): T
    abstract fun getModelClass(): Class<out T>
    abstract fun getViewModel(model: T): ViewModel<T>

    private fun query() {
        LOG.info("query")
        Amplify.DataStore.query(getModelClass(),
//            Where.matches(QueryPredicateOperation.not(Blog.NAME.eq("jhad"))), // Blog.NAME.ascending()),
            { results ->
                val itemsList = results.iterator().asSequence().toList()
                val pairsList = itemsList.map { it.id to it }
                val array = pairsList.toTypedArray()
                itemMap = linkedMapOf(*array)
                LOG.info("query succeeded: ${itemMap.size} Items")
                loadContent()
            },
            { LOG.error("query failed: ${it.message}", it) }
        )
    }

    private fun observe() {
        LOG.info("observe")
        Amplify.DataStore.observe(getModelClass(),
            {
                LOG.info("observe started.")
            },
            { itemChange ->
                if (itemChange.initiator().equals(DataStoreItemChange.Initiator.REMOTE)) {
                    LOG.info("observe remote item changed: ${itemChange.item()}")
                    query()
                } else {
                    LOG.info("observe local item changed: ${itemChange.item()}")
                }
            },
            { LOG.error("observe failed: ${it.message}", it) },
            { LOG.info("observe completed.") })
    }

    private fun subscribe() {
        subscriptionTokens.add(Amplify.Hub.subscribe(HubChannel.DATASTORE) {
            LOG.debug("DataStore event: ${it.name} (${it.id}): ${it.data}")
            when (DataStoreChannelEventName.fromString(it.name)) {
                DataStoreChannelEventName.OUTBOX_MUTATION_PROCESSED -> {
                    (it.data as? OutboxMutationEvent<in Model>)?.let { event ->
                        pendingSaveIds.remove(event.element.model.id)
                        pendingDeleteIds.remove(event.element.model.id)
                    }
                    query()
                }
                DataStoreChannelEventName.NETWORK_STATUS -> {
                    showNetworkStatusIndicator((it.data as NetworkStatusEvent).active)
                }
                else -> { }
            }
        })

        subscriptionTokens.add(Amplify.Hub.subscribe(HubChannel.API) {
            LOG.debug("API event: ${it.name} (${it.id}): ${it.data}")
        })
    }

    private fun unsubscribe() {
        subscriptionTokens.forEach { token -> Amplify.Hub.unsubscribe(token) }
        subscriptionTokens = mutableSetOf()
    }

    private fun start() {
        Amplify.DataStore.start(
            { LOG.info("start succeeded.") },
            { LOG.error("start failed: ${it.message}", it)}
        )
    }
    private fun save(item: T) {
        LOG.info("save")
        Amplify.DataStore.save(item,
            { itemChange ->
                LOG.info("save succeeded: ${itemChange.item()}")
                pendingSaveIds.add(itemChange.item().id)
                itemMap[itemChange.item().id] = itemChange.item()
                loadContent()
            },
            { LOG.error("save failed: ${it.message}", it) })
    }

    override fun onClick(item: T) {
        delete(item)
    }

    override fun onLongClick(item: T): Boolean {
        save(updateModel(item))
        return true
    }

    fun delete(item: T) {
        LOG.info("delete")
        Amplify.DataStore.delete(item,
            { itemChange ->
                LOG.info("deleted succeeded: ${itemChange.item()}")
                pendingDeleteIds.add(itemChange.item().id)
                loadContent()
            },
            { LOG.error("delete failed: ${it.message}", it) })
    }

    private fun loadContent() {
        runOnUiThread {
            itemAdapter.values = itemMap.values.toList().map { getViewModel(it) }
            itemAdapter.pendingSaveIds = pendingSaveIds;
            itemAdapter.pendingDeleteIds = pendingDeleteIds;
            itemAdapter.notifyDataSetChanged()
            val emptyView = findViewById<TextView>(R.id.empty_view)
            emptyView.visibility = if (itemMap.size == 0) View.VISIBLE else View.GONE
            emptyView.setText("No items.  Tap + to create one!")
        }
    }

    private fun showNetworkStatusIndicator(active: Boolean) {
        runOnUiThread {
            if(active) {
                networkStatusBar.dismiss()
            } else {
                networkStatusBar.show()
            }
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
                    { LOG.error("clear failed.", it) }
                )
            },
            { LOG.error("sign out failed.", it) }
        )
    }

    companion object {
        private val LOG = Amplify.Logging.forNamespace("app-datastore:ListActivity")
    }
}
