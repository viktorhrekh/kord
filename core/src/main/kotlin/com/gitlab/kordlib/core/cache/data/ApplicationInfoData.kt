package com.gitlab.kordlib.core.cache.data

import com.gitlab.kordlib.common.entity.DiscordTeam
import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.common.entity.optional.Optional
import com.gitlab.kordlib.common.entity.optional.OptionalSnowflake
import com.gitlab.kordlib.rest.json.response.ApplicationInfoResponse

data class ApplicationInfoData(
        val id: Snowflake,
        val name: String,
        val icon: String? = null,
        val description: String,
        val rpcOrigins: List<String>? = null,
        val botPublic: Boolean,
        val botRequireCodeGrant: Boolean,
        val ownerId: Snowflake,
        val summary: String,
        val verifyKey: String,
        val team: DiscordTeam?= null,
        val guildId: OptionalSnowflake = OptionalSnowflake.Missing,
        val primarySkuId: OptionalSnowflake = OptionalSnowflake.Missing,
        val slug: Optional<String> = Optional.Missing(),
        val coverImage: Optional<String> = Optional.Missing(),
) {
    companion object {

        fun from(entity: ApplicationInfoResponse) = with(entity) {
            ApplicationInfoData(
                    id,
                    name,
                    icon,
                    description,
                    rpcOrigins,
                    botPublic,
                    botRequireCodeGrant,
                    owner.id,
                    summary,
                    verifyKey,
                    team,
                    guildId,
                    primarySkuId,
                    slug,
                    coverImage,
            )
        }

    }
}