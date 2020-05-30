package com.gitlab.kordlib.core.cache

import com.gitlab.kordlib.cache.api.DataCache
import com.gitlab.kordlib.cache.api.DataEntryCache
import com.gitlab.kordlib.cache.api.data.DataDescription
import com.gitlab.kordlib.cache.api.delegate.DelegatingDataCache
import com.gitlab.kordlib.cache.api.delegate.EntrySupplier
import com.gitlab.kordlib.cache.api.find
import com.gitlab.kordlib.cache.map.MapLikeCollection
import com.gitlab.kordlib.cache.map.internal.MapEntryCache
import com.gitlab.kordlib.cache.map.lruLinkedHashMap
import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.EntitySupplier
import com.gitlab.kordlib.core.Kord
import com.gitlab.kordlib.core.cache.data.*
import com.gitlab.kordlib.core.entity.*
import com.gitlab.kordlib.core.entity.channel.Channel
import com.gitlab.kordlib.core.entity.channel.GuildChannel
import kotlinx.coroutines.flow.*
import java.util.concurrent.ConcurrentHashMap

typealias Generator<I, T> = (cache: DataCache, description: DataDescription<T, I>) -> DataEntryCache<out T>

class KordCache(val kord: Kord, val cache: DataCache) : DataCache by cache, EntitySupplier {
    /**
     *  Returns a [Flow] of [Channel]s fetched from cache.
     */
    val channels: Flow<Channel>
        get() = find<ChannelData>().asFlow().map { Channel.from(it, kord) }

    /**
     *  fetches all cached [Guild]s
     */
    override val guilds: Flow<Guild>
        get() = find<GuildData>().asFlow().map { Guild(it, kord) }

    /**
     *  fetches all cached [Region]s
     */
    override val regions: Flow<Region>
        get() = find<RegionData>().asFlow().map { Region(it, kord) }

    /**
     *  fetches all cached [Role]s
     */
    val roles: Flow<Role>
        get() = find<RoleData>().asFlow().map { Role(it, kord) }

    /**
     *  fetches all cached [User]s
     */
    val users: Flow<User>
        get() = find<UserData>().asFlow().map { User(it, kord) }

    /**
     *  fetches all cached [Member]s
     */
    @Suppress("EXPERIMENTAL_API_USAGE")
    val members: Flow<Member>
        get() = find<UserData>().asFlow().flatMapConcat { userData ->
            find<MemberData> { MemberData::userId eq userData.id }
                    .asFlow().map { Member(it, userData, kord) }
        }

    /**
     *  fetches a single [Channel]
     */
    override suspend fun getChannelOrNull(id: Snowflake): Channel? {
        val data = find<ChannelData> { ChannelData::id eq id.longValue }.singleOrNull() ?: return null
        return Channel.from(data, kord)
    }

    override suspend fun getGuild(id: Snowflake): Guild = getGuildOrNull(id)!!

    override suspend fun getChannel(id: Snowflake): Channel = getChannelOrNull(id)!!

    override fun getGuildChannels(guildId: Snowflake): Flow<GuildChannel> = find<ChannelData> {
        ChannelData::guildId eq guildId.longValue
    }.asFlow().map { Channel.from(it, kord) as GuildChannel }

    override fun getChannelPins(channelId: Snowflake): Flow<Message> = find<MessageData> {
        MessageData::channelId eq channelId.longValue
        MessageData::pinned eq true
    }.asFlow().map { Message(it, kord) }

    override suspend fun getGuildOrNull(id: Snowflake): Guild? {
        val data = find<GuildData> { GuildData::id eq id.longValue }.singleOrNull() ?: return null
        return Guild(data, kord)
    }

    override suspend fun getMemberOrNull(guildId: Snowflake, userId: Snowflake): Member? {
        val userData = find<UserData> { UserData::id eq userId.longValue }.singleOrNull() ?: return null

        val memberData = find<MemberData> {
            MemberData::userId eq userId.longValue
            MemberData::guildId eq guildId.longValue
        }.singleOrNull() ?: return null

        return Member(memberData, userData, kord)
    }

    override suspend fun getMessageOrNull(channelId: Snowflake, messageId: Snowflake): Message? {
        val data = find<MessageData> { MessageData::id eq messageId.longValue }.singleOrNull() ?: return null

        return Message(data, kord)
    }

    override suspend fun getMember(guildId: Snowflake, userId: Snowflake): Member = getMemberOrNull(guildId, userId)!!

    override suspend fun getMessage(channelId: Snowflake, messageId: Snowflake): Message = getMessageOrNull(channelId, messageId)!!

    override fun getMessagesAfter(messageId: Snowflake, channelId: Snowflake, limit: Int): Flow<Message> {
        require(limit > 0) { "At least 1 item should be requested, but got $limit." }
        return find<MessageData> {
            MessageData::channelId eq channelId.longValue
            MessageData::id gt messageId.longValue
        }.asFlow().map { Message(it, kord) }.take(limit)
    }

    override fun getMessagesBefore(messageId: Snowflake, channelId: Snowflake, limit: Int): Flow<Message> {
        require(limit > 0) { "At least 1 item should be requested, but got $limit." }
        return find<MessageData> {
            MessageData::channelId eq channelId.longValue
            MessageData::id lt messageId.longValue
        }.asFlow().map { Message(it, kord) }.take(limit)
    }


    override fun getMessagesAround(messageId: Snowflake, channelId: Snowflake, limit: Int): Flow<Message> {
        require(limit > 0) { "At least 1 item should be requested, but got $limit." }
        return flow {
            emitAll(getMessagesBefore(messageId, channelId, limit / 2))
            emitAll(getMessagesAfter(messageId, channelId, limit / 2))
        }
    }

    override suspend fun getRoleOrNull(guildId: Snowflake, roleId: Snowflake): Role? {
        val data = find<RoleData> {
            RoleData::id eq roleId.longValue
            RoleData::guildId eq guildId.longValue
        }.singleOrNull() ?: return null

        return Role(data, kord)
    }

    override suspend fun getRole(guildId: Snowflake, roleId: Snowflake): Role {
        TODO("Not yet implemented")
    }

    override fun getGuildRoles(guildId: Snowflake): Flow<Role> = find<RoleData> {
        RoleData::guildId eq guildId.longValue
    }.asFlow().map { Role(it, kord) }

    override suspend fun getGuildBanOrNull(guildId: Snowflake, userId: Snowflake): Ban? {
        val data = find<BanData> {
            BanData::userId eq userId.longValue
            BanData::guildId eq guildId.longValue
        }.singleOrNull() ?: return null
        return Ban(data, kord)
    }

    override suspend fun getGuildBan(guildId: Snowflake, userId: Snowflake): Ban = getGuildBanOrNull(guildId, userId)!!

    override fun getGuildBans(guildId: Snowflake): Flow<Ban> = find<BanData> {
        BanData::guildId eq guildId.longValue
    }.asFlow().map { Ban(it, kord) }

    override fun getGuildMembers(guildId: Snowflake, limit: Int): Flow<Member> {
        return find<UserData>().asFlow().flatMapConcat { userData ->
            find<MemberData> {
                MemberData::userId eq userData.id
                MemberData::guildId eq guildId
            }.asFlow().map { Member(it, userData, kord) }
        }
    }

    override fun getGuildVoiceRegions(guildId: Snowflake): Flow<Region> = find<RegionData> {
        RegionData::guildId eq guildId.longValue
    }.asFlow().map { Region(it, kord) }

    override suspend fun getEmojiOrNull(guildId: Snowflake, emojiId: Snowflake): GuildEmoji? {
        val data = find<EmojiData> {
            EmojiData::guildId eq guildId.longValue
            EmojiData::id eq emojiId.longValue
        }.singleOrNull() ?: return null

        return GuildEmoji(data, kord)
    }

    override suspend fun getEmoji(guildId: Snowflake, emojiId: Snowflake): GuildEmoji = getEmojiOrNull(guildId, emojiId)!!


    override fun getEmojis(guildId: Snowflake): Flow<GuildEmoji> = find<EmojiData> {
        EmojiData::guildId eq guildId.longValue
    }.asFlow().map { GuildEmoji(it, kord) }


    override fun getCurrentUserGuilds(limit: Int): Flow<Guild> {
        return emptyFlow()
    }

    override fun getChannelWebhooks(channelId: Snowflake): Flow<Webhook> = find<WebhookData> {
        WebhookData::channelId eq channelId.longValue
    }.asFlow().map { Webhook(it, kord) }

    override fun getGuildWebhooks(guildId: Snowflake): Flow<Webhook> = find<WebhookData> {
        WebhookData::guildId eq guildId.longValue
    }.asFlow().map { Webhook(it, kord) }


    override suspend fun getCurrentUserOrNull(): User? {
        val data = find<UserData> {
            UserData::id eq kord.selfId.longValue
        }.singleOrNull() ?: return null
        return User(data, kord)
    }

    override suspend fun getSelf(): User = getSelfOrNull()!!

    override suspend fun getUser(id: Snowflake): User = getUserOrNull(id)!!

    override suspend fun getCurrentUser(): User = getCurrentUserOrNull()!!

    override suspend fun getWebhookOrNull(webhookId: Snowflake): Webhook? {
        val data = find<WebhookData> {
            WebhookData::id eq webhookId.longValue
        }.singleOrNull() ?: return null

        return Webhook(data, kord)
    }

    override suspend fun getWebhookWithTokenOrNull(webhookId: Snowflake, token: String): Webhook? {
        val data = find<WebhookData> {
            WebhookData::id eq webhookId.longValue
            WebhookData::token eq token
        }.singleOrNull() ?: return null

        return Webhook(data, kord)
    }

    override suspend fun getWebhook(webhookId: Snowflake): Webhook = getWebhookOrNull(webhookId)!!

    override suspend fun getWebhookWithToken(webhookId: Snowflake, token: String): Webhook = getWebhookWithTokenOrNull(webhookId, token)!!

    suspend fun getRole(id: Snowflake): Role? {
        val data = find<RoleData> { RoleData::id eq id.longValue }.singleOrNull() ?: return null

        return Role(data, kord)
    }

    override suspend fun getSelfOrNull(): User? = getUserOrNull(kord.selfId)

    override suspend fun getUserOrNull(id: Snowflake): User? {
        val data = find<UserData> { UserData::id eq id.longValue }.singleOrNull() ?: return null

        return User(data, kord)
    }

}

class KordCacheBuilder {
    var defaultGenerator: Generator<Any, Any> = { cache, description ->
        MapEntryCache(cache, description, MapLikeCollection.fromThreadSafe(ConcurrentHashMap()))
    }

    private val descriptionGenerators: MutableMap<DataDescription<*, *>, Generator<*, *>> = mutableMapOf()

    fun <T : Any, I : Any> lruCache(size: Int = 100): Generator<T, I> = { cache, description ->
        MapEntryCache(cache, description, MapLikeCollection.lruLinkedHashMap(size))
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any, I : Any> forDescription(description: DataDescription<T, I>, generator: Generator<T, I>?) {
        if (generator == null) {
            descriptionGenerators.remove(description)
            return
        }
        descriptionGenerators[description] = generator as Generator<*, *>
    }

    fun build(): DataCache = DelegatingDataCache(EntrySupplier.invoke { cache, description ->
        val generator = descriptionGenerators[description] ?: defaultGenerator
        generator(cache, description)
    })

}