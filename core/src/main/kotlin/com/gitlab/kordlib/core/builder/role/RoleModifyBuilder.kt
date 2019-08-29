package com.gitlab.kordlib.core.builder.role

import com.gitlab.kordlib.common.entity.Permissions
import com.gitlab.kordlib.core.builder.AuditRequestBuilder
import com.gitlab.kordlib.core.builder.KordBuilder
import com.gitlab.kordlib.rest.json.request.GuildRoleModifyRequest
import java.awt.Color

@KordBuilder
class RoleModifyBuilder : AuditRequestBuilder<GuildRoleModifyRequest> {
    override var reason: String? = null
    var name: String? = null
    var color: Color? = null
    var hoist: Boolean? = null
    var mentionable: Boolean? = null
    var permissions: Permissions? = null

    override fun toRequest(): GuildRoleModifyRequest = GuildRoleModifyRequest(
            name = name,
            color = color?.rgb,
            separate = hoist,
            mentionable = mentionable,
            permissions = permissions
    )
}