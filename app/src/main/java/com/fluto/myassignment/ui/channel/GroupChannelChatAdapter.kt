package com.fluto.myassignment.ui.channel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.fluto.myassignment.databinding.ItemChatReceiveListBinding
import com.fluto.myassignment.databinding.ItemChatSendListBinding
import com.fluto.myassignment.utils.equalDate
import com.fluto.myassignment.utils.equalTime
import com.fluto.myassignment.utils.toDate
import com.fluto.myassignment.utils.toTime
import com.sendbird.android.SendbirdChat
import com.sendbird.android.message.BaseMessage
import com.sendbird.android.message.FileMessage
import com.sendbird.android.message.SendingStatus

class GroupChannelChatAdapter(
) : ListAdapter<BaseMessage, RecyclerView.ViewHolder>(diffCallback) {

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<BaseMessage>() {
            override fun areItemsTheSame(oldItem: BaseMessage, newItem: BaseMessage): Boolean {
                return if (oldItem.messageId > 0 && newItem.messageId > 0) {
                    oldItem.messageId == newItem.messageId
                } else {
                    oldItem.requestId == newItem.requestId
                }
            }

            override fun areContentsTheSame(oldItem: BaseMessage, newItem: BaseMessage): Boolean {
                return oldItem.message == newItem.message
                        && oldItem.sender?.nickname == newItem.sender?.nickname
                        && oldItem.sendingStatus == newItem.sendingStatus
                        && oldItem.updatedAt == newItem.updatedAt
            }
        }
        const val VIEW_TYPE_SEND = 0
        const val VIEW_TYPE_RECEIVE = 1
    }

    private val baseMessageList = mutableListOf<BaseMessage>()
    private val pendingMessageList = mutableListOf<BaseMessage>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        when (viewType) {
            VIEW_TYPE_SEND -> return GroupChatSendViewHolder(
                ItemChatSendListBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            VIEW_TYPE_RECEIVE -> return GroupChatReceiveViewHolder(
                ItemChatReceiveListBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            else -> return GroupChatSendViewHolder(
                ItemChatSendListBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var showName = false
        var showDate = false
        var showTime = false

        if (currentList[position].sendingStatus == SendingStatus.SUCCEEDED) {
            showName = true
            showDate = true
            showTime = true
            if (position > 0) {
                showDate =
                    !currentList[position].createdAt.equalDate(currentList[position - 1].createdAt)
                if (currentList[position].sender != null && currentList[position - 1].sender != null) {
                    showName =
                        currentList[position].sender?.userId != currentList[position - 1].sender?.userId
                }
                if (position < currentList.size - 1) {
                    showTime =
                        !(currentList[position].createdAt.equalTime(currentList[position + 1].createdAt))
                    if (!showTime) {
                        if (currentList[position].sender != null && currentList[position + 1].sender != null) {
                            showTime =
                                currentList[position].sender?.userId != currentList[position + 1].sender?.userId
                        }
                    }
                }
            } else {
                if (position < currentList.size - 1) {
                    showTime =
                        !(currentList[position].createdAt.equalTime(currentList[position + 1].createdAt))
                }
            }
        }

        when (holder) {
            is GroupChatSendViewHolder -> {
                holder.bind(getItem(position), showDate, showTime)
            }

            is GroupChatReceiveViewHolder -> {
                holder.bind(getItem(position), showName, showDate, showTime)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {

        val currentUser = SendbirdChat.currentUser
        var viewType = VIEW_TYPE_SEND
        if (currentUser != null) {
            if (getItem(position).sender?.userId == currentUser.userId) {
                if (getItem(position) !is FileMessage) {
                    viewType = VIEW_TYPE_SEND
                }
            } else {
                if (getItem(position) !is FileMessage) {
                    viewType = VIEW_TYPE_RECEIVE
                }
            }
        } else {
            if (getItem(position) !is FileMessage) {
                viewType = VIEW_TYPE_RECEIVE
            }
        }

        return viewType
    }

    fun changeMessages(messages: List<BaseMessage>?, isPendingClear: Boolean = true) {
        baseMessageList.clear()
        if (isPendingClear) {
            pendingMessageList.clear()
        }
        if (messages != null) {
            baseMessageList.addAll(messages)
        }
        mergeList()
    }

    fun addNextMessages(messages: List<BaseMessage>?) {
        if (messages != null) {
            baseMessageList.addAll(messages)
            mergeList()
        }
    }

    fun addPreviousMessages(messages: List<BaseMessage>?) {
        if (messages != null) {
            baseMessageList.addAll(0, messages)
            mergeList()
        }
    }

    fun addPendingMessages(messages: List<BaseMessage>) {
        pendingMessageList.addAll(messages)
        mergeList()
    }

    fun updateSucceedMessages(messages: List<BaseMessage>) {
        val requestIdIndexMap =
            pendingMessageList.mapIndexed { index, pendingMessage ->
                pendingMessage.requestId to index
            }.toMap()
        val messageIdIndexMap =
            baseMessageList.mapIndexed { index, baseMessage ->
                baseMessage.messageId to index
            }.toMap()
        val resultMessageList = mutableListOf<BaseMessage>().apply { addAll(pendingMessageList) }
        messages.forEach {
            val requestIndex = requestIdIndexMap[it.requestId]
            if (requestIndex != null) {
                baseMessageList.add(it)
                resultMessageList.remove(pendingMessageList[requestIndex])
            } else {
                val messageIndex = messageIdIndexMap[it.messageId]
                if (messageIndex != null) {
                    baseMessageList[messageIndex] = it
                }
            }
        }
        pendingMessageList.clear()
        pendingMessageList.addAll(resultMessageList)
        mergeList()
    }

    fun updatePendingMessages(messages: List<BaseMessage>) {
        val requestIdIndexMap =
            pendingMessageList.mapIndexed { index, pendingMessage ->
                pendingMessage.requestId to index
            }.toMap()
        messages.forEach {
            val index = requestIdIndexMap[it.requestId]
            if (index != null) {
                pendingMessageList[index] = it
            }
        }
        mergeList()
    }

    fun deletePendingMessages(messages: List<BaseMessage>) {
        val requestIdIndexMap =
            pendingMessageList.mapIndexed { index, pendingMessage ->
                pendingMessage.requestId to index
            }.toMap()
        val resultMessageList = mutableListOf<BaseMessage>().apply { addAll(pendingMessageList) }
        messages.forEach {
            val index = requestIdIndexMap[it.requestId]
            if (index != null) {
                resultMessageList.remove(pendingMessageList[index])
            }
        }
        pendingMessageList.clear()
        pendingMessageList.addAll(resultMessageList)
        mergeList()
    }

    fun deleteMessages(messages: List<BaseMessage>) {
        val messageIdIndexMap =
            baseMessageList.mapIndexed { index, message ->
                message.messageId to index
            }.toMap()
        val resultMessageList = mutableListOf<BaseMessage>().apply { addAll(baseMessageList) }
        messages.forEach {
            val index = messageIdIndexMap[it.messageId]
            if (index != null) {
                resultMessageList.remove(baseMessageList[index])
            }
        }
        baseMessageList.clear()
        baseMessageList.addAll(resultMessageList)
        mergeList()
    }

    fun addMessages(messages: List<BaseMessage>) {
        messages.forEach {
            findAddMessageIndex(baseMessageList, it).apply {
                if (this > -1) {
                    baseMessageList.add(this, it)
                }
            }
        }
        mergeList()
    }

    private fun mergeList() = submitList(baseMessageList + pendingMessageList)

    open inner class BaseViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
    }

    inner class GroupChatSendViewHolder(private val binding: ItemChatSendListBinding) :
        BaseViewHolder(binding) {
        fun bind(
            message: BaseMessage,
            showDate: Boolean,
            showTime: Boolean
        ) {
            if (message.sendingStatus == SendingStatus.SUCCEEDED) {
                binding.progressSend.visibility = View.GONE
                binding.chatErrorButton.visibility = View.GONE
                if (showDate) {
//                    binding.dateTagView.setMillisecond(message.createdAt)
                    binding.dateTagView.text = (message.createdAt).toDate()
                    binding.dateTagView.visibility = View.VISIBLE
                } else {
                    binding.dateTagView.visibility = View.GONE
                }
                if (showTime) {
                    binding.textviewTime.text = message.createdAt.toTime()
                    binding.textviewTime.visibility = View.VISIBLE
                } else {
                    binding.textviewTime.visibility = View.GONE
                }
            } else {
                binding.dateTagView.visibility = View.GONE
                binding.textviewTime.visibility = View.GONE
                if (message.sendingStatus == SendingStatus.PENDING) {
                    binding.progressSend.visibility = View.VISIBLE
                    binding.chatErrorButton.visibility = View.GONE
                } else {
                    binding.progressSend.visibility = View.GONE
                    binding.chatErrorButton.visibility = View.VISIBLE
                    binding.chatErrorButton.setOnClickListener {
                    }
                }
            }
            binding.chatBubbleSend.setText(message.message)
        }
    }

    inner class GroupChatReceiveViewHolder(private val binding: ItemChatReceiveListBinding) :
        BaseViewHolder(binding) {
        fun bind(
            message: BaseMessage,
            showName: Boolean,
            showDate: Boolean,
            showTime: Boolean
        ) {
            binding.chatBubbleReceive.setText(message.message)
            if (showName) {
                binding.textviewNickname.text = message.sender?.nickname ?: message.sender?.userId
                binding.textviewNickname.visibility = View.VISIBLE
            } else {
                binding.textviewNickname.visibility = View.GONE
            }
            if (showDate) {
                binding.dateTagView.text = (message.createdAt).toDate()
                binding.dateTagView.visibility = View.VISIBLE
            } else {
                binding.dateTagView.visibility = View.GONE
            }
            if (showTime) {
                binding.textviewTime.text = message.createdAt.toTime()
                binding.textviewTime.visibility = View.VISIBLE
            } else {
                binding.textviewTime.visibility = View.GONE
            }
        }
    }

    fun findAddMessageIndex(originMessages: List<BaseMessage>, targetMessage: BaseMessage): Int {
        if (originMessages.isEmpty()) {
            return 0
        }
        if (originMessages.last().createdAt > targetMessage.createdAt) {
            return 0
        }
        if (originMessages.last().createdAt < targetMessage.createdAt) {
            return originMessages.size
        }
        for (i in 0 until originMessages.size - 1) {
            val currentMessage = originMessages[i]
            val nextMessage = originMessages[i + 1]
            if (currentMessage.createdAt < targetMessage.createdAt && targetMessage.createdAt < nextMessage.createdAt) {
                return i + 1
            }
        }
        return originMessages.size
    }
}