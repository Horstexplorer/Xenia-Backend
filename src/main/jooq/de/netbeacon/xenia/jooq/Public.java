/*
 * This file is generated by jOOQ.
 */
package de.netbeacon.xenia.jooq;


import de.netbeacon.xenia.jooq.tables.*;
import org.jooq.Catalog;
import org.jooq.Sequence;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;

import java.util.Arrays;
import java.util.List;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Public extends SchemaImpl {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public</code>
     */
    public static final Public PUBLIC = new Public();

    /**
     * The table <code>public.channels</code>.
     */
    public final Channels CHANNELS = Channels.CHANNELS;

    /**
     * The table <code>public.guilds</code>.
     */
    public final Guilds GUILDS = Guilds.GUILDS;

    /**
     * The table <code>public.internal_bot_data</code>.
     */
    public final InternalBotData INTERNAL_BOT_DATA = InternalBotData.INTERNAL_BOT_DATA;

    /**
     * The table <code>public.internal_bot_shards</code>.
     */
    public final InternalBotShards INTERNAL_BOT_SHARDS = InternalBotShards.INTERNAL_BOT_SHARDS;

    /**
     * The table <code>public.license_types</code>.
     */
    public final LicenseTypes LICENSE_TYPES = LicenseTypes.LICENSE_TYPES;

    /**
     * The table <code>public.licenses</code>.
     */
    public final Licenses LICENSES = Licenses.LICENSES;

    /**
     * The table <code>public.members</code>.
     */
    public final Members MEMBERS = Members.MEMBERS;

    /**
     * The table <code>public.members_roles</code>.
     */
    public final MembersRoles MEMBERS_ROLES = MembersRoles.MEMBERS_ROLES;

    /**
     * The table <code>public.messages</code>.
     */
    public final Messages MESSAGES = Messages.MESSAGES;

    /**
     * The table <code>public.notification</code>.
     */
    public final Notification NOTIFICATION = Notification.NOTIFICATION;

    /**
     * The table <code>public.oauth</code>.
     */
    public final Oauth OAUTH = Oauth.OAUTH;

    /**
     * The table <code>public.oauth_states</code>.
     */
    public final OauthStates OAUTH_STATES = OauthStates.OAUTH_STATES;

    /**
     * The table <code>public.tags</code>.
     */
    public final Tags TAGS = Tags.TAGS;

    /**
     * The table <code>public.users</code>.
     */
    public final Users USERS = Users.USERS;

    /**
     * The table <code>public.vroles</code>.
     */
    public final Vroles VROLES = Vroles.VROLES;

    /**
     * No further instances allowed
     */
    private Public() {
        super("public", null);
    }


    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Sequence<?>> getSequences() {
        return Arrays.<Sequence<?>>asList(
            Sequences.LICENSES_LICENSE_ID_SEQ,
            Sequences.NOTIFICATION_NOTIFICATION_ID_SEQ,
            Sequences.OAUTH_STATES_STATE_ID_SEQ,
            Sequences.ROLES_ROLE_ID_SEQ);
    }

    @Override
    public final List<Table<?>> getTables() {
        return Arrays.<Table<?>>asList(
            Channels.CHANNELS,
            Guilds.GUILDS,
            InternalBotData.INTERNAL_BOT_DATA,
            InternalBotShards.INTERNAL_BOT_SHARDS,
            LicenseTypes.LICENSE_TYPES,
            Licenses.LICENSES,
            Members.MEMBERS,
            MembersRoles.MEMBERS_ROLES,
            Messages.MESSAGES,
            Notification.NOTIFICATION,
            Oauth.OAUTH,
            OauthStates.OAUTH_STATES,
            Tags.TAGS,
            Users.USERS,
            Vroles.VROLES);
    }
}