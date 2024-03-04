package com.fluto.myassignment.ui.channellist

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fluto.myassignment.databinding.FragmentGroupChannelListBinding
import com.fluto.myassignment.ui.channel.GroupChannelChatActivity
import com.fluto.myassignment.ui.user.AddUserActivity
import com.fluto.myassignment.utils.ToastUtil
import com.sendbird.android.SendbirdChat
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.channel.query.GroupChannelListQueryOrder
import com.sendbird.android.channel.query.MyMemberStateFilter
import com.sendbird.android.collection.GroupChannelCollection
import com.sendbird.android.collection.GroupChannelContext
import com.sendbird.android.handler.GroupChannelCollectionHandler
import com.sendbird.android.params.GroupChannelCollectionCreateParams
import com.sendbird.android.params.GroupChannelListQueryParams

class GroupChannelListFragment : Fragment() {
    private val binding by lazy {
        FragmentGroupChannelListBinding.inflate(layoutInflater)
    }
    private lateinit var recAdapter: GroupChannelListAdapter
    private var groupChannelCollection: GroupChannelCollection? = null
    private val linearLayoutManager =
        LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    private var bindingState = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bindingState = true
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun onResume() {
        super.onResume()
        createCollection()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.root.removeAllViews()
        bindingState = false
    }

    private fun initView() {
        binding.fabCreateChannel.setOnClickListener {
            // TODO: 사용자 추가 화면으로 이동
        }

        recAdapter = GroupChannelListAdapter { groupChannel ->
            Intent(activity, GroupChannelChatActivity::class.java).apply {
                putExtra("channel_url", groupChannel.url)
                putExtra("channel_title", groupChannel.name)
                startActivity(this)
            }
        }
        binding.recyclerView.apply {
            layoutManager = linearLayoutManager
            this.adapter = recAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (!recyclerView.canScrollVertically(1)) {
                        loadMore()
                    }
                }
            })
            addItemDecoration(
                DividerItemDecoration(context, RecyclerView.VERTICAL)
            )
        }
        binding.fabCreateChannel.setOnClickListener {
            Intent(activity, AddUserActivity::class.java).apply {
                val currentUser = SendbirdChat.currentUser
                if (currentUser != null) {
                    putExtra("base_user", arrayListOf(currentUser.userId))
                }
                putExtra("create_mode", true)
                startActivity(this)
            }
        }

        recAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount)
                recyclerViewMoveTop()
                recAdapter.notifyItemChanged(fromPosition)
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                recyclerViewMoveTop()
            }
        })
    }

    private fun createCollection() {
        val listQuery = GroupChannel.createMyGroupChannelListQuery(
            GroupChannelListQueryParams(
                order = GroupChannelListQueryOrder.LATEST_LAST_MESSAGE,
                myMemberStateFilter = MyMemberStateFilter.ALL
            )
        )
        val params = GroupChannelCollectionCreateParams(listQuery)
        groupChannelCollection = SendbirdChat.createGroupChannelCollection(params).apply {
            groupChannelCollectionHandler = (object : GroupChannelCollectionHandler {
                override fun onChannelsAdded(
                    context: GroupChannelContext,
                    channels: List<GroupChannel>
                ) {
                    recAdapter.updateChannels(channels)
                }

                override fun onChannelsDeleted(
                    context: GroupChannelContext,
                    deletedChannelUrls: List<String>
                ) {
                    recAdapter.deleteChannels(deletedChannelUrls)
                }

                override fun onChannelsUpdated(
                    context: GroupChannelContext,
                    channels: List<GroupChannel>
                ) {
                    recAdapter.updateChannels(channels)
                }
            })
        }
        loadMore(true)
    }

    private fun loadMore(isRefreshing: Boolean = false) {
        val collection = groupChannelCollection ?: return
        if (collection.hasMore) {
            collection.loadMore loadMoreLabel@{ channelList, e ->
                if (e != null || channelList == null) {
                    ToastUtil.show(activity, "${e?.message}")
                    return@loadMoreLabel
                }
                if (channelList.isNotEmpty()) {
                    if (isRefreshing) {
                        recAdapter.addChannels(emptyList())
                    }
                    recAdapter.addChannels(channelList)
                }

            }
        }
    }

    private fun recyclerViewMoveTop() {
        if (bindingState) {
            val firstVisiblePosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition()
            if (firstVisiblePosition == 0) {
                binding.recyclerView.scrollToPosition(0)
            }
        }
    }

}