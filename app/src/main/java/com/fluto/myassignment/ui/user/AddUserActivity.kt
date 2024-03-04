package com.fluto.myassignment.ui.user

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.fluto.myassignment.R
import com.fluto.myassignment.databinding.ActivityAddUserBinding
import com.fluto.myassignment.ui.channel.GroupChannelChatActivity
import com.fluto.myassignment.utils.ToastUtil
import com.sendbird.android.SendbirdChat
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.params.ApplicationUserListQueryParams
import com.sendbird.android.params.GroupChannelCreateParams

class AddUserActivity: AppCompatActivity() {
    private val binding by lazy {
        ActivityAddUserBinding.inflate(layoutInflater)
    }
    private lateinit var adapter: AddUserAdapter
    private var userListQuery = SendbirdChat.createApplicationUserListQuery(
        ApplicationUserListQueryParams()
    )
    private var isCreateMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        init()
        initRecyclerView()
        loadNextUsers()
    }

    private fun init() {
        binding.toolbar.title = getString(R.string.add_user)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        isCreateMode = intent.getBooleanExtra("create_mode", false)
    }

    private fun initRecyclerView() {
        adapter = AddUserAdapter(
            { _, _ -> },
            true,
            intent.getStringArrayListExtra("select_user"),
            intent.getStringArrayListExtra("base_user")
        )
        binding.recyclerview.adapter = adapter
        binding.recyclerview.addItemDecoration(
            DividerItemDecoration(
                this,
                RecyclerView.VERTICAL
            )
        )
        binding.recyclerview.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1)) {
                    loadNextUsers()
                }
            }
        })
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val item = menu.findItem(R.id.select)
        item.title = if (isCreateMode) getString(R.string.opt_add) else getString(R.string.opt_join)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.select_user_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.select -> {
                if (isCreateMode) {
                    createChannel()
                } else {
                    selectUser()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun loadNextUsers() {
        if (userListQuery.hasNext) {
            userListQuery.next { users, e ->
                if (e != null) {
                    ToastUtil.show(this, "${e.message}")
                    return@next
                }
                if (!users.isNullOrEmpty()) {
                    adapter.addUsers(users)
                }
            }
        }
    }

    private fun createChannel() {
        if (adapter.selectUserIdSet.isEmpty()) {
            ToastUtil.show(this, "Select user")
            return
        }
        val params = GroupChannelCreateParams()
            .apply {
                userIds = adapter.selectUserIdSet.toList()
            }
        GroupChannel.createChannel(params) createChannelLabel@{ groupChannel, e ->
            if (e != null) {
                ToastUtil.show(this, "${e.message}")
                return@createChannelLabel
            }
            if (groupChannel != null) {
                val intent = Intent(
                    this@AddUserActivity,
                    GroupChannelChatActivity::class.java
                )
                intent.putExtra("channel_url", groupChannel.url)
                intent.putExtra("channel_title", groupChannel.name)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun selectUser() {
        val intent = intent
        val arrayList = arrayListOf<String>()
        arrayList.addAll(adapter.selectUserIdSet)
        intent.putExtra("select_user", arrayList)
        setResult(RESULT_OK, intent)
        finish()
    }
}