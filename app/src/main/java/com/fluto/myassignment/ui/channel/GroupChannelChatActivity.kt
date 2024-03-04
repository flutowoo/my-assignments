package com.fluto.myassignment.ui.channel

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fluto.myassignment.databinding.ActivityGroupChatBinding
import com.fluto.myassignment.ui.common.TypingChannelHandler
import com.fluto.myassignment.utils.ChatRecyclerDataObserver
import com.fluto.myassignment.utils.MyPreference
import com.fluto.myassignment.utils.TextUtils
import com.fluto.myassignment.utils.ToastUtil
import com.sendbird.android.SendbirdChat
import com.sendbird.android.channel.GroupChannel
import com.sendbird.android.collection.GroupChannelContext
import com.sendbird.android.collection.MessageCollection
import com.sendbird.android.collection.MessageCollectionInitPolicy
import com.sendbird.android.collection.MessageContext
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.handler.MessageCollectionHandler
import com.sendbird.android.handler.MessageCollectionInitHandler
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.SendingStatus
import com.sendbird.android.params.GroupChannelUpdateParams
import com.sendbird.android.params.MessageCollectionCreateParams
import com.sendbird.android.params.MessageListParams
import com.sendbird.android.params.UserMessageCreateParams
import com.sendbird.android.params.UserMessageUpdateParams
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min


class GroupChannelChatActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityGroupChatBinding.inflate(layoutInflater)
    }
    private lateinit var adapter: GroupChannelChatAdapter
    private lateinit var recyclerObserver: ChatRecyclerDataObserver
    private var channelUrl: String = ""
    private var channelTitle: String = ""
    private var currentGroupChannel: GroupChannel? = null
    private var messageCollection: MessageCollection? = null
    private var channelTSHashMap = ConcurrentHashMap<String, Long>()
    private var isCollectionInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val intent = intent
        channelUrl = intent.getStringExtra("channel_url") ?: ""
        channelTitle = intent.getStringExtra("channel_title") ?: ""
        channelTSHashMap = MyPreference.channelTSMap

        getChannel(channelUrl)
        init()
        initRecyclerView()
    }

    private fun init() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.buttonChat.setOnClickListener {
            val message = binding.edittextChat.text
            if (!message.isNullOrBlank()) {
                sendMessage(message.toString())
                binding.edittextChat.setText("")
                hideKeyPadPad()
            }
        }
    }

    private fun setupTypingListener() {
        val groupChannel = currentGroupChannel ?: return
        SendbirdChat?.addChannelHandler(
            groupChannel.hashCode().toString(),
            object : TypingChannelHandler() {

                override fun typingStatusUpdated(channel: GroupChannel) {
                    if (channel != currentGroupChannel) return
                    channel.displayTypingUsers()
                }
            })

        binding.edittextChat.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                currentGroupChannel?.startTyping()
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
    }

    private fun hideKeyPadPad() {
        val input: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        input.hideSoftInputFromWindow(binding.edittextChat.windowToken, 0)
    }

    private fun initRecyclerView() {
        adapter = GroupChannelChatAdapter()
        binding.recyclerviewChat.itemAnimator = null
        binding.recyclerviewChat.adapter = adapter
        recyclerObserver = ChatRecyclerDataObserver(binding.recyclerviewChat, adapter)
        adapter.registerAdapterDataObserver(recyclerObserver)

        binding.recyclerviewChat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(-1)) {
                    loadPreviousMessageItems()
                } else if (!recyclerView.canScrollVertically(1)) {
                    loadNextMessageItems()
                }
            }
        })
    }

    private fun getChannel(channelUrl: String?) {
        if (channelUrl.isNullOrBlank()) {
            ToastUtil.show(this, "error: Channel url null")
            return
        }
        GroupChannel.getChannel(
            channelUrl
        ) getChannelLabel@{ groupChannel, e ->
            if (e != null) {
                ToastUtil.show(this, "${e.message}")
                return@getChannelLabel
            }
            if (groupChannel != null) {
                currentGroupChannel = groupChannel
                setChannelTitle()
                createMessageCollection(channelTSHashMap[channelUrl] ?: Long.MAX_VALUE)
                setupTypingListener()
            }
        }
    }

    private fun setChannelTitle() {
        val currentChannel = currentGroupChannel
        if (channelTitle == TextUtils.CHANNEL_DEFAULT_NAME && currentChannel != null) {
            binding.toolbar.title = TextUtils.getGroupChannelTitle(currentChannel)
        } else {
            binding.toolbar.title = channelTitle
        }
    }

    private fun createMessageCollection(timeStamp: Long) {
        messageCollection?.dispose()
        isCollectionInitialized = false
        val channel = currentGroupChannel
        if (channel == null) {
            ToastUtil.show(this, "error: Channel null")
            finish()
            return
        }

        val messageListParams = MessageListParams().apply {
            reverse = false
            previousResultSize = 20
            nextResultSize = 20
        }
        val messageCollectionCreateParams =
            MessageCollectionCreateParams(channel, messageListParams)
                .apply {
                    startingPoint = timeStamp
                    messageCollectionHandler = collectionHandler
                }
        messageCollection =
            SendbirdChat.createMessageCollection(messageCollectionCreateParams).apply {
                initialize(
                    MessageCollectionInitPolicy.CACHE_AND_REPLACE_BY_API,
                    object : MessageCollectionInitHandler {
                        override fun onCacheResult(
                            cachedList: List<BaseMessage>?,
                            e: SendbirdException?
                        ) {
                            if (e != null) {
                                ToastUtil.show(this@GroupChannelChatActivity, "${e.message}")
                            }
                            adapter.changeMessages(cachedList)
                            adapter.addPendingMessages(this@apply.pendingMessages)

                            binding.recyclerviewChat.scrollToPosition(adapter.itemCount - 1)
                        }

                        override fun onApiResult(
                            apiResultList: List<BaseMessage>?,
                            e: SendbirdException?
                        ) {
                            if (e != null) {
                                ToastUtil.show(this@GroupChannelChatActivity, "${e.message}")
                            }
                            adapter.changeMessages(apiResultList, false)
                            markAsRead()
                            isCollectionInitialized = true

                            binding.recyclerviewChat.scrollToPosition(adapter.itemCount - 1)
                        }
                    }
                )
            }
    }

    private fun loadPreviousMessageItems() {
        val collection = messageCollection ?: return
        if (collection.hasPrevious) {
            collection.loadPrevious { messages, e ->
                if (e != null) {
                    ToastUtil.show(this, "${e.message}")
                    return@loadPrevious
                }
                adapter.addPreviousMessages(messages)
            }
        }
    }

    private fun loadNextMessageItems() {
        val collection = messageCollection ?: return
        if (collection.hasNext) {
            collection.loadNext { messages, e ->
                if (e != null) {
                    ToastUtil.show(this, "${e.message}")
                    return@loadNext
                }
                adapter.addNextMessages(messages)
                markAsRead()
            }
        }
    }

    private fun updateMessage(msg: String, baseMessage: BaseMessage) {
        if (msg.isBlank()) {
            ToastUtil.show(this, "Please enter your message")
            return
        }
        val params = UserMessageUpdateParams().apply {
            message = msg
        }
        currentGroupChannel?.updateUserMessage(
            baseMessage.messageId, params
        ) { _, e ->
            if (e != null) {
                ToastUtil.show(this, "${e.message}")
            }
        }
    }

    private fun inviteUser(selectIds: List<String>?) {
        if (selectIds != null && selectIds.isNotEmpty()) {
            val channel = currentGroupChannel ?: return
            channel.invite(selectIds.toList()) {
                if (it != null) {
                    ToastUtil.show(this, "${it.message}")
                }
            }
        }
    }

    private fun updateChannelView(name: String, channel: GroupChannel) {
        if (name.isBlank()) {
            ToastUtil.show(this, "Please enter your message")
            return
        }
        if (channel.name != name) {
            val params = GroupChannelUpdateParams()
                .apply { this.name = name }
            channel.updateChannel(
                params
            ) { _, e ->
                if (e != null) {
                    ToastUtil.show(this, "${e.message}")
                }
            }
        }
    }

    private fun sendMessage(message: String) {
        if (message.isBlank()) {
            ToastUtil.show(this, "Please enter your message")
            return
        }
        if (!isCollectionInitialized) {
            ToastUtil.show(this, "Message Collection is initializing.")
            return
        }
        val collection = messageCollection ?: return
        val channel = currentGroupChannel ?: return

        val params = UserMessageCreateParams().apply {
            this.message = message.trim()
        }
//        binding.chatInputView.clearText()
        recyclerObserver.scrollToBottom(true)
        channel.sendUserMessage(params, null)
        if (collection.hasNext) {
            createMessageCollection(Long.MAX_VALUE)
        }
    }

    private fun markAsRead() {
//        currentGroupChannel?.markAsRead { e1 -> e1?.printStackTrace() }
        currentGroupChannel?.markAsRead { null }
    }

    private fun updateChannelView(groupChannel: GroupChannel) {
        currentGroupChannel = groupChannel
        binding.toolbar.title =
            if (groupChannel.name.isBlank() || groupChannel.name == TextUtils.CHANNEL_DEFAULT_NAME)
                TextUtils.getGroupChannelTitle(groupChannel)
            else groupChannel.name
    }

    override fun onPause() {
        val lastMessage = adapter.currentList.lastOrNull()
        if (lastMessage != null && channelUrl.isNotBlank()) {
            channelTSHashMap[channelUrl] = lastMessage.createdAt
            MyPreference.channelTSMap = channelTSHashMap
        }
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        messageCollection?.dispose()
        SendbirdChat.autoBackgroundDetection = true
    }

    private val collectionHandler = object : MessageCollectionHandler {
        override fun onMessagesAdded(
            context: MessageContext,
            channel: GroupChannel,
            messages: List<BaseMessage>
        ) {
            when (context.messagesSendingStatus) {
                SendingStatus.SUCCEEDED -> {
                    adapter.addMessages(messages)
                    markAsRead()
                }

                SendingStatus.PENDING -> adapter.addPendingMessages(messages)

                else -> {
                }
            }
        }

        override fun onMessagesUpdated(
            context: MessageContext,
            channel: GroupChannel,
            messages: List<BaseMessage>
        ) {
            when (context.messagesSendingStatus) {
                SendingStatus.SUCCEEDED -> adapter.updateSucceedMessages(messages)

                SendingStatus.PENDING -> adapter.updatePendingMessages(messages)

                SendingStatus.FAILED -> adapter.updatePendingMessages(messages)

                SendingStatus.CANCELED -> adapter.deletePendingMessages(messages)// The cancelled messages in the sample will be deleted

                else -> {
                }
            }
        }

        override fun onMessagesDeleted(
            context: MessageContext,
            channel: GroupChannel,
            messages: List<BaseMessage>
        ) {
            when (context.messagesSendingStatus) {
                SendingStatus.SUCCEEDED -> adapter.deleteMessages(messages)

                SendingStatus.FAILED -> adapter.deletePendingMessages(messages)

                else -> {
                }
            }
        }

        override fun onChannelUpdated(context: GroupChannelContext, channel: GroupChannel) {
            updateChannelView(channel)
        }

        override fun onChannelDeleted(context: GroupChannelContext, channelUrl: String) {
            ToastUtil.show(this@GroupChannelChatActivity, "This channel has been deleted.")
            finish()
        }

        override fun onHugeGapDetected() {
            val collection = messageCollection
            if (collection == null) {
                ToastUtil.show(this@GroupChannelChatActivity, "error: Channel null")
                finish()
                return
            }
            val startingPoint = collection.startingPoint
            collection.dispose()
            val position: Int =
                (binding.recyclerviewChat.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            if (position >= 0) {
                val message: BaseMessage = adapter.currentList[position]
                createMessageCollection(message.createdAt)
            } else {
                createMessageCollection(startingPoint)
            }
        }

    }

    private fun GroupChannel.displayTypingUsers() {
        if (isTyping) {
            val users = typingUsers
            val typingLimit = min(2, users.size)
            val truncatedText = when (typingLimit) {
                2 -> "are typing"
                else -> "and others are typing"
            }

            val typingTitle = if (typingLimit == 1) {
                "${users[0].nickname} is typing"
            } else {
                typingUsers.joinToString(
                    limit = typingLimit,
                    truncated = truncatedText,
                    separator = ","
                ) { it.nickname }
            }
            binding.toolbar.title = typingTitle
        } else {
            setChannelTitle()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            10001 -> {
                ToastUtil.show(this, "Not Supported")
            }
        }
    }
}