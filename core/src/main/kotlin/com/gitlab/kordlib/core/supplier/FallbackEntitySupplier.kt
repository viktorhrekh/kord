package com.gitlab.kordlib.core.supplier

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.entity.*
import com.gitlab.kordlib.core.entity.channel.Channel
import com.gitlab.kordlib.core.entity.channel.GuildChannel
import com.gitlab.kordlib.core.supplier.EntitySupplyStrategy.Companion.cache
import com.gitlab.kordlib.core.supplier.EntitySupplyStrategy.Companion.rest
import com.gitlab.kordlib.core.switchIfEmpty
import kotlinx.coroutines.flow.Flow

/**
 * Creates supplier providing a strategy which will first operate on this supplier. When an entity
 * is not present from the first supplier it will be fetched from [other] instead. Operations that return flows
 * will only fall back to [other] when the returned flow contained no elements.
 */
infix fun EntitySupplier.withFallback(other: EntitySupplier): EntitySupplier = FallbackEntitySupplier(this, other)

private class FallbackEntitySupplier(val first: EntitySupplier, val second: EntitySupplier) : EntitySupplier {

    override val guilds: Flow<Guild>
        get() = first.guilds.switchIfEmpty(second.guilds)

    override val regions: Flow<Region>
        get() = first.regions.switchIfEmpty(second.regions)

    override suspend fun getGuildOrNull(id: Snowflake): Guild? =
            first.getGuildOrNull(id) ?: second.getGuildOrNull(id)

    override suspend fun getChannelOrNull(id: Snowflake): Channel? =
            first.getChannelOrNull(id) ?: second.getChannelOrNull(id)

    override fun getGuildChannels(guildId: Snowflake): Flow<GuildChannel> =
            first.getGuildChannels(guildId).switchIfEmpty(second.getGuildChannels(guildId))

    override fun getChannelPins(channelId: Snowflake): Flow<Message> =
            first.getChannelPins(channelId).switchIfEmpty(second.getChannelPins(channelId))

    override suspend fun getMemberOrNull(guildId: Snowflake, userId: Snowflake): Member? =
            first.getMemberOrNull(guildId, userId) ?: second.getMemberOrNull(guildId, userId)

    override suspend fun getMessageOrNull(channelId: Snowflake, messageId: Snowflake): Message? =
            first.getMessageOrNull(channelId, messageId) ?: second.getMessageOrNull(channelId, messageId)

    override suspend fun getMember(guildId: Snowflake, userId: Snowflake): Member =
            getMemberOrNull(guildId, userId)!!

    override fun getMessagesAfter(messageId: Snowflake, channelId: Snowflake, limit: Int): Flow<Message> =
            first.getMessagesAfter(messageId, channelId, limit).switchIfEmpty(second.getMessagesAfter(messageId, channelId, limit))

    override fun getMessagesBefore(messageId: Snowflake, channelId: Snowflake, limit: Int): Flow<Message> =
            first.getMessagesBefore(messageId, channelId, limit).switchIfEmpty(second.getMessagesBefore(messageId, channelId, limit))

    override fun getMessagesAround(messageId: Snowflake, channelId: Snowflake, limit: Int): Flow<Message> =
            first.getMessagesAround(messageId, channelId, limit).switchIfEmpty(second.getMessagesAround(messageId, channelId, limit))

    override suspend fun getSelfOrNull(): User? =
            first.getSelfOrNull() ?: second.getSelfOrNull()

    override suspend fun getUserOrNull(id: Snowflake): User? =
            first.getUserOrNull(id) ?: second.getUserOrNull(id)

    override suspend fun getRoleOrNull(guildId: Snowflake, roleId: Snowflake): Role? =
            first.getRoleOrNull(guildId, roleId) ?: second.getRoleOrNull(guildId, roleId)


    override fun getGuildRoles(guildId: Snowflake): Flow<Role> =
            first.getGuildRoles(guildId).switchIfEmpty(second.getGuildRoles(guildId))

    override suspend fun getGuildBanOrNull(guildId: Snowflake, userId: Snowflake): Ban? =
            first.getGuildBanOrNull(guildId, userId) ?: second.getGuildBanOrNull(guildId, userId)

    override fun getGuildBans(guildId: Snowflake): Flow<Ban> =
            first.getGuildBans(guildId).switchIfEmpty(second.getGuildBans(guildId))

    override fun getGuildMembers(guildId: Snowflake, limit: Int): Flow<Member> =
            first.getGuildMembers(guildId, limit).switchIfEmpty(second.getGuildMembers(guildId, limit))

    override fun getGuildVoiceRegions(guildId: Snowflake): Flow<Region> =
            first.getGuildVoiceRegions(guildId).switchIfEmpty(second.getGuildVoiceRegions(guildId))

    override suspend fun getEmojiOrNull(guildId: Snowflake, emojiId: Snowflake): GuildEmoji? =
            first.getEmojiOrNull(guildId, emojiId) ?: second.getEmojiOrNull(guildId, emojiId)

    override fun getEmojis(guildId: Snowflake): Flow<GuildEmoji> =
            first.getEmojis(guildId).switchIfEmpty(second.getEmojis(guildId))

    override fun getCurrentUserGuilds(limit: Int): Flow<Guild> =
            first.getCurrentUserGuilds(limit).switchIfEmpty(second.getCurrentUserGuilds(limit))

    override fun getChannelWebhooks(channelId: Snowflake): Flow<Webhook> =
            first.getChannelWebhooks(channelId).switchIfEmpty(second.getChannelWebhooks(channelId))

    override fun getGuildWebhooks(guildId: Snowflake): Flow<Webhook> =
            first.getGuildWebhooks(guildId).switchIfEmpty(second.getGuildWebhooks(guildId))

    override suspend fun getWebhookOrNull(id: Snowflake): Webhook? =
            first.getWebhookOrNull(id) ?: second.getWebhookOrNull(id)

    override suspend fun getWebhookWithTokenOrNull(id: Snowflake, token: String): Webhook? =
            first.getWebhookWithTokenOrNull(id, token) ?: second.getWebhookWithTokenOrNull(id, token)

    override fun toString(): String = buildToString("FallbackEntitySupplier"){
        property("first", first)
        property("second", second)
    }
}

