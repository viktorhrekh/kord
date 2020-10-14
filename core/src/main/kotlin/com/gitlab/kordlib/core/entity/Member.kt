package com.gitlab.kordlib.core.entity

import com.gitlab.kordlib.common.entity.Permission
import com.gitlab.kordlib.common.entity.Permissions
import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.common.exception.RequestException
import com.gitlab.kordlib.core.Kord
import com.gitlab.kordlib.core.behavior.MemberBehavior
import com.gitlab.kordlib.core.behavior.RoleBehavior
import com.gitlab.kordlib.core.behavior.UserBehavior
import com.gitlab.kordlib.core.cache.data.MemberData
import com.gitlab.kordlib.core.cache.data.UserData
import com.gitlab.kordlib.core.supplier.EntitySupplier
import com.gitlab.kordlib.core.supplier.EntitySupplyStrategy
import com.gitlab.kordlib.core.toInstant
import kotlinx.coroutines.flow.*
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * An instance of a [Discord Member](https://discord.com/developers/docs/resources/guild#guild-member-object).
 */
class Member(
        val memberData: MemberData,
        userData: UserData,
        kord: Kord,
        supplier: EntitySupplier = kord.defaultSupplier
) : User(userData, kord, supplier), MemberBehavior {

    override val guildId: Snowflake
        get() = Snowflake(memberData.guildId)

    /**
     * The name as shown in the discord client, prioritizing the [nickname] over the [use].
     */
    val displayName: String get() = nickname ?: username

    /**
     * When the user joined this [guild].
     */
    val joinedAt: Instant get() = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(memberData.joinedAt, Instant::from)

    /**
     * The guild-specific nickname of the user, if present.
     */
    val nickname: String? get() = memberData.nick

    /**
     * When the user used their Nitro boost on the server.
     */
    val premiumSince: Instant? get() = memberData.premiumSince?.toInstant()

    /**
     * The ids of the [roles][Role] that apply to this user.
     */
    val roleIds: Set<Snowflake> get() = memberData.roles.asSequence().map { Snowflake(it) }.toSet()

    /**
     * The behaviors of the [roles][Role] that apply to this user.
     */
    val roleBehaviors: Set<RoleBehavior>
        get() = memberData.roles.asSequence().map { RoleBehavior(guildId = guildId, id = Snowflake(it), kord = kord) }.toSet()

    /**
     * The [roles][Role] that apply to this user.
     *
     * This request uses state [data] to resolve the entities belonging to the flow,
     * as such it can't guarantee an up to date representation if the [data] is outdated.
     *
     * The returned flow is lazily executed, any [RequestException] will be thrown on
     * [terminal operators](https://kotlinlang.org/docs/reference/coroutines/flow.html#terminal-flow-operators) instead.
     */
    val roles: Flow<Role>
        get() = if (roleIds.isEmpty()) emptyFlow()
        else supplier.getGuildRoles(guildId).filter { it.id in roleIds }

    /**
     * Whether this member's [id] equals the [Guild.ownerId].
     *
     * @throws [RequestException] if something went wrong during the request.
     */
    suspend fun isOwner(): Boolean = getGuild().ownerId == id

    /**
     * Requests to calculate a summation of the permissions of this member's [roles].
     *
     * @throws [RequestException] if something went wrong during the request.
     */
    suspend fun getPermissions(): Permissions {
        val guild = getGuild()
        val owner = guild.ownerId == this.id
        if (owner) return Permissions {
            +Permission.All
        }

        val everyone = guild.getEveryoneRole().permissions
        val roles = roles.map { it.permissions }.toList()

        return Permissions {
            +everyone
            roles.forEach { +it }
        }
    }

    /**
     * Returns a new [Member] with the given [strategy].
     */
    override fun withStrategy(strategy: EntitySupplyStrategy<*>): Member = Member(memberData, data, kord, strategy.supply(kord))


    override fun hashCode(): Int = Objects.hash(id, guildId)

    override fun equals(other: Any?): Boolean = when(other) {
        is MemberBehavior -> other.id == id && other.guildId == guildId
        is UserBehavior -> other.id == id
        else -> false
    }

    override fun toString(): String {
        return "Member(memberData=$memberData)"
    }

}
