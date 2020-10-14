package com.gitlab.kordlib.core.event.channel

import com.gitlab.kordlib.core.Kord
import com.gitlab.kordlib.core.entity.channel.*
import com.gitlab.kordlib.core.event.Event

interface ChannelUpdateEvent : Event {
    val channel: Channel
    override val kord: Kord
        get() = channel.kord
}

class CategoryUpdateEvent(override val channel: Category, override val shard: Int) : ChannelUpdateEvent {
    override fun toString(): String {
        return "CategoryUpdateEvent(channel=$channel, shard=$shard)"
    }
}

class DMChannelUpdateEvent(override val channel: DmChannel, override val shard: Int) : ChannelUpdateEvent {
    override fun toString(): String {
        return "DMChannelUpdateEvent(channel=$channel, shard=$shard)"
    }
}

class NewsChannelUpdateEvent(override val channel: NewsChannel, override val shard: Int) : ChannelUpdateEvent {
    override fun toString(): String {
        return "NewsChannelUpdateEvent(channel=$channel, shard=$shard)"
    }
}

class StoreChannelUpdateEvent(override val channel: StoreChannel, override val shard: Int) : ChannelUpdateEvent {
    override fun toString(): String {
        return "StoreChannelUpdateEvent(channel=$channel, shard=$shard)"
    }
}

class TextChannelUpdateEvent(override val channel: TextChannel, override val shard: Int) : ChannelUpdateEvent {
    override fun toString(): String {
        return "TextChannelUpdateEvent(channel=$channel, shard=$shard)"
    }
}

class VoiceChannelUpdateEvent(override val channel: VoiceChannel, override val shard: Int) : ChannelUpdateEvent {
    override fun toString(): String {
        return "VoiceChannelUpdateEvent(channel=$channel, shard=$shard)"
    }
}
