/*
 * This file is generated by jOOQ.
 */
package de.netbeacon.xenia.jooq;


import de.netbeacon.xenia.jooq.tables.*;
import de.netbeacon.xenia.jooq.tables.records.*;
import org.jooq.ForeignKey;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;


/**
 * A class modelling foreign key relationships and constraints of tables in 
 * public.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<ChannelsRecord> CHANNELS_CHANNEL_ID = Internal.createUniqueKey(Channels.CHANNELS, DSL.name("channels_channel_id"), new TableField[] { Channels.CHANNELS.CHANNEL_ID }, true);
    public static final UniqueKey<GuildsRecord> GUILDS_GUILD_ID = Internal.createUniqueKey(Guilds.GUILDS, DSL.name("guilds_guild_id"), new TableField[] { Guilds.GUILDS.GUILD_ID }, true);
    public static final UniqueKey<GuildsRecord> GUILDS_LICENSE_ID = Internal.createUniqueKey(Guilds.GUILDS, DSL.name("guilds_license_id"), new TableField[] { Guilds.GUILDS.LICENSE_ID }, true);
    public static final UniqueKey<InternalBotDataRecord> INTERNAL_BOT_DATA_CLIENT_ID = Internal.createUniqueKey(InternalBotData.INTERNAL_BOT_DATA, DSL.name("internal_bot_data_client_id"), new TableField[] { InternalBotData.INTERNAL_BOT_DATA.CLIENT_ID }, true);
    public static final UniqueKey<InternalBotShardsRecord> INTERNAL_BOT_SHARDS_SHARD_ID = Internal.createUniqueKey(InternalBotShards.INTERNAL_BOT_SHARDS, DSL.name("internal_bot_shards_shard_id"), new TableField[] { InternalBotShards.INTERNAL_BOT_SHARDS.SHARD_ID }, true);
    public static final UniqueKey<LicenseTypesRecord> LICENSE_TYPES_LICENSE_TYPE_ID = Internal.createUniqueKey(LicenseTypes.LICENSE_TYPES, DSL.name("license_types_license_type_id"), new TableField[] { LicenseTypes.LICENSE_TYPES.LICENSE_TYPE_ID }, true);
    public static final UniqueKey<LicensesRecord> LICENSES_LICENSE_ID = Internal.createUniqueKey(Licenses.LICENSES, DSL.name("licenses_license_id"), new TableField[] { Licenses.LICENSES.LICENSE_ID }, true);
    public static final UniqueKey<LicensesRecord> LICENSES_LICENSE_KEY = Internal.createUniqueKey(Licenses.LICENSES, DSL.name("licenses_license_key"), new TableField[] { Licenses.LICENSES.LICENSE_KEY }, true);
    public static final UniqueKey<MembersRecord> MEMBERS_GUILD_ID_USER_ID = Internal.createUniqueKey(Members.MEMBERS, DSL.name("members_guild_id_user_id"), new TableField[] { Members.MEMBERS.GUILD_ID, Members.MEMBERS.USER_ID }, true);
    public static final UniqueKey<MembersRolesRecord> MEMBERS_ROLES_USER_ID_ROLE_ID = Internal.createUniqueKey(MembersRoles.MEMBERS_ROLES, DSL.name("members_roles_user_id_role_id"), new TableField[] { MembersRoles.MEMBERS_ROLES.USER_ID, MembersRoles.MEMBERS_ROLES.ROLE_ID }, true);
    public static final UniqueKey<MessageAttachmentsRecord> MESSAGE_ATTACHMENTS_PK = Internal.createUniqueKey(MessageAttachments.MESSAGE_ATTACHMENTS, DSL.name("message_attachments_pk"), new TableField[] { MessageAttachments.MESSAGE_ATTACHMENTS.ATTACHMENT_ID }, true);
    public static final UniqueKey<MessagesRecord> MESSAGES_MESSAGE_ID = Internal.createUniqueKey(Messages.MESSAGES, DSL.name("messages_message_id"), new TableField[] { Messages.MESSAGES.MESSAGE_ID }, true);
    public static final UniqueKey<NotificationsRecord> NOTIFICATION_NOTIFICATION_ID = Internal.createUniqueKey(Notifications.NOTIFICATIONS, DSL.name("notification_notification_id"), new TableField[] { Notifications.NOTIFICATIONS.NOTIFICATION_ID }, true);
    public static final UniqueKey<OauthRecord> OAUTH_USER_ID = Internal.createUniqueKey(Oauth.OAUTH, DSL.name("oauth_user_id"), new TableField[] { Oauth.OAUTH.USER_ID }, true);
    public static final UniqueKey<TagsRecord> TAGS_TAG_NAME_GUILD_ID = Internal.createUniqueKey(Tags.TAGS, DSL.name("tags_tag_name_guild_id"), new TableField[] { Tags.TAGS.TAG_NAME, Tags.TAGS.GUILD_ID }, true);
    public static final UniqueKey<TwitchnotificationsRecord> TWITCHNOTIFICATIONS_TWITCHNOTIFICATION_ID = Internal.createUniqueKey(Twitchnotifications.TWITCHNOTIFICATIONS, DSL.name("twitchnotifications_twitchnotification_id"), new TableField[] { Twitchnotifications.TWITCHNOTIFICATIONS.TWITCHNOTIFICATION_ID }, true);
    public static final UniqueKey<UsersRecord> USERS_USER_ID = Internal.createUniqueKey(Users.USERS, DSL.name("users_user_id"), new TableField[] { Users.USERS.USER_ID }, true);
    public static final UniqueKey<VrolesRecord> ROLES_GUILD_ID_ROLE_NAME = Internal.createUniqueKey(Vroles.VROLES, DSL.name("roles_guild_id_role_name"), new TableField[] { Vroles.VROLES.GUILD_ID, Vroles.VROLES.VROLE_NAME }, true);
    public static final UniqueKey<VrolesRecord> ROLES_ROLE_ID = Internal.createUniqueKey(Vroles.VROLES, DSL.name("roles_role_id"), new TableField[] { Vroles.VROLES.VROLE_ID }, true);

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------

    public static final ForeignKey<ChannelsRecord, GuildsRecord> CHANNELS__CHANNELS_GUILD_ID_FKEY = Internal.createForeignKey(Channels.CHANNELS, DSL.name("channels_guild_id_fkey"), new TableField[] { Channels.CHANNELS.GUILD_ID }, Keys.GUILDS_GUILD_ID, new TableField[] { Guilds.GUILDS.GUILD_ID }, true);
    public static final ForeignKey<GuildsRecord, LicensesRecord> GUILDS__GUILDS_LICENSE_ID_FKEY = Internal.createForeignKey(Guilds.GUILDS, DSL.name("guilds_license_id_fkey"), new TableField[] { Guilds.GUILDS.LICENSE_ID }, Keys.LICENSES_LICENSE_ID, new TableField[] { Licenses.LICENSES.LICENSE_ID }, true);
    public static final ForeignKey<InternalBotShardsRecord, InternalBotDataRecord> INTERNAL_BOT_SHARDS__INTERNAL_BOT_SHARDS_CLIENT_ID_FKEY = Internal.createForeignKey(InternalBotShards.INTERNAL_BOT_SHARDS, DSL.name("internal_bot_shards_client_id_fkey"), new TableField[] { InternalBotShards.INTERNAL_BOT_SHARDS.CLIENT_ID }, Keys.INTERNAL_BOT_DATA_CLIENT_ID, new TableField[] { InternalBotData.INTERNAL_BOT_DATA.CLIENT_ID }, true);
    public static final ForeignKey<LicensesRecord, LicenseTypesRecord> LICENSES__LICENSES_LICENSE_TYPE_FKEY = Internal.createForeignKey(Licenses.LICENSES, DSL.name("licenses_license_type_fkey"), new TableField[] { Licenses.LICENSES.LICENSE_TYPE }, Keys.LICENSE_TYPES_LICENSE_TYPE_ID, new TableField[] { LicenseTypes.LICENSE_TYPES.LICENSE_TYPE_ID }, true);
    public static final ForeignKey<MembersRecord, GuildsRecord> MEMBERS__MEMBERS_GUILD_ID_FKEY = Internal.createForeignKey(Members.MEMBERS, DSL.name("members_guild_id_fkey"), new TableField[] { Members.MEMBERS.GUILD_ID }, Keys.GUILDS_GUILD_ID, new TableField[] { Guilds.GUILDS.GUILD_ID }, true);
    public static final ForeignKey<MembersRecord, UsersRecord> MEMBERS__MEMBERS_USER_ID_FKEY = Internal.createForeignKey(Members.MEMBERS, DSL.name("members_user_id_fkey"), new TableField[] { Members.MEMBERS.USER_ID }, Keys.USERS_USER_ID, new TableField[] { Users.USERS.USER_ID }, true);
    public static final ForeignKey<MembersRolesRecord, GuildsRecord> MEMBERS_ROLES__MEMBERS_ROLES_GUILD_ID_FKEY = Internal.createForeignKey(MembersRoles.MEMBERS_ROLES, DSL.name("members_roles_guild_id_fkey"), new TableField[] { MembersRoles.MEMBERS_ROLES.GUILD_ID }, Keys.GUILDS_GUILD_ID, new TableField[] { Guilds.GUILDS.GUILD_ID }, true);
    public static final ForeignKey<MembersRolesRecord, MembersRecord> MEMBERS_ROLES__MEMBERS_ROLES_GUILD_ID_USER_ID_FKEY = Internal.createForeignKey(MembersRoles.MEMBERS_ROLES, DSL.name("members_roles_guild_id_user_id_fkey"), new TableField[] { MembersRoles.MEMBERS_ROLES.GUILD_ID, MembersRoles.MEMBERS_ROLES.USER_ID }, Keys.MEMBERS_GUILD_ID_USER_ID, new TableField[] { Members.MEMBERS.GUILD_ID, Members.MEMBERS.USER_ID }, true);
    public static final ForeignKey<MembersRolesRecord, VrolesRecord> MEMBERS_ROLES__MEMBERS_ROLES_ROLE_ID_FKEY = Internal.createForeignKey(MembersRoles.MEMBERS_ROLES, DSL.name("members_roles_role_id_fkey"), new TableField[] { MembersRoles.MEMBERS_ROLES.ROLE_ID }, Keys.ROLES_ROLE_ID, new TableField[] { Vroles.VROLES.VROLE_ID }, true);
    public static final ForeignKey<MessageAttachmentsRecord, MessagesRecord> MESSAGE_ATTACHMENTS__MESSAGE_ATTACHMENTS_MESSAGES_MESSAGE_ID_FK = Internal.createForeignKey(MessageAttachments.MESSAGE_ATTACHMENTS, DSL.name("message_attachments_messages_message_id_fk"), new TableField[] { MessageAttachments.MESSAGE_ATTACHMENTS.MESSAGE_ID }, Keys.MESSAGES_MESSAGE_ID, new TableField[] { Messages.MESSAGES.MESSAGE_ID }, true);
    public static final ForeignKey<MessagesRecord, ChannelsRecord> MESSAGES__MESSAGES_CHANNEL_ID_FKEY = Internal.createForeignKey(Messages.MESSAGES, DSL.name("messages_channel_id_fkey"), new TableField[] { Messages.MESSAGES.CHANNEL_ID }, Keys.CHANNELS_CHANNEL_ID, new TableField[] { Channels.CHANNELS.CHANNEL_ID }, true);
    public static final ForeignKey<MessagesRecord, GuildsRecord> MESSAGES__MESSAGES_GUILD_ID_FKEY = Internal.createForeignKey(Messages.MESSAGES, DSL.name("messages_guild_id_fkey"), new TableField[] { Messages.MESSAGES.GUILD_ID }, Keys.GUILDS_GUILD_ID, new TableField[] { Guilds.GUILDS.GUILD_ID }, true);
    public static final ForeignKey<MessagesRecord, UsersRecord> MESSAGES__MESSAGES_USER_ID_FKEY = Internal.createForeignKey(Messages.MESSAGES, DSL.name("messages_user_id_fkey"), new TableField[] { Messages.MESSAGES.USER_ID }, Keys.USERS_USER_ID, new TableField[] { Users.USERS.USER_ID }, true);
    public static final ForeignKey<NotificationsRecord, ChannelsRecord> NOTIFICATIONS__NOTIFICATION_CHANNEL_ID_FKEY = Internal.createForeignKey(Notifications.NOTIFICATIONS, DSL.name("notification_channel_id_fkey"), new TableField[] { Notifications.NOTIFICATIONS.CHANNEL_ID }, Keys.CHANNELS_CHANNEL_ID, new TableField[] { Channels.CHANNELS.CHANNEL_ID }, true);
    public static final ForeignKey<NotificationsRecord, GuildsRecord> NOTIFICATIONS__NOTIFICATION_GUILD_ID_FKEY = Internal.createForeignKey(Notifications.NOTIFICATIONS, DSL.name("notification_guild_id_fkey"), new TableField[] { Notifications.NOTIFICATIONS.GUILD_ID }, Keys.GUILDS_GUILD_ID, new TableField[] { Guilds.GUILDS.GUILD_ID }, true);
    public static final ForeignKey<NotificationsRecord, MembersRecord> NOTIFICATIONS__NOTIFICATION_GUILD_ID_USER_ID_FKEY = Internal.createForeignKey(Notifications.NOTIFICATIONS, DSL.name("notification_guild_id_user_id_fkey"), new TableField[] { Notifications.NOTIFICATIONS.GUILD_ID, Notifications.NOTIFICATIONS.USER_ID }, Keys.MEMBERS_GUILD_ID_USER_ID, new TableField[] { Members.MEMBERS.GUILD_ID, Members.MEMBERS.USER_ID }, true);
    public static final ForeignKey<OauthRecord, UsersRecord> OAUTH__OAUTH_USER_ID_FKEY = Internal.createForeignKey(Oauth.OAUTH, DSL.name("oauth_user_id_fkey"), new TableField[] { Oauth.OAUTH.USER_ID }, Keys.USERS_USER_ID, new TableField[] { Users.USERS.USER_ID }, true);
    public static final ForeignKey<TagsRecord, MembersRecord> TAGS__TAGS_GUILD_ID_USER_ID_FKEY = Internal.createForeignKey(Tags.TAGS, DSL.name("tags_guild_id_user_id_fkey"), new TableField[] { Tags.TAGS.GUILD_ID, Tags.TAGS.USER_ID }, Keys.MEMBERS_GUILD_ID_USER_ID, new TableField[] { Members.MEMBERS.GUILD_ID, Members.MEMBERS.USER_ID }, true);
    public static final ForeignKey<TwitchnotificationsRecord, ChannelsRecord> TWITCHNOTIFICATIONS__TWITCHNOTIFICATIONS_CHANNEL_ID_FKEY = Internal.createForeignKey(Twitchnotifications.TWITCHNOTIFICATIONS, DSL.name("twitchnotifications_channel_id_fkey"), new TableField[] { Twitchnotifications.TWITCHNOTIFICATIONS.CHANNEL_ID }, Keys.CHANNELS_CHANNEL_ID, new TableField[] { Channels.CHANNELS.CHANNEL_ID }, true);
    public static final ForeignKey<TwitchnotificationsRecord, GuildsRecord> TWITCHNOTIFICATIONS__TWITCHNOTIFICATIONS_GUILD_ID_FKEY = Internal.createForeignKey(Twitchnotifications.TWITCHNOTIFICATIONS, DSL.name("twitchnotifications_guild_id_fkey"), new TableField[] { Twitchnotifications.TWITCHNOTIFICATIONS.GUILD_ID }, Keys.GUILDS_GUILD_ID, new TableField[] { Guilds.GUILDS.GUILD_ID }, true);
}
