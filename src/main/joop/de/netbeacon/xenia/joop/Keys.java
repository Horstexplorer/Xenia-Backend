/*
 * This file is generated by jOOQ.
 */
package de.netbeacon.xenia.joop;


import de.netbeacon.xenia.joop.tables.*;
import de.netbeacon.xenia.joop.tables.records.*;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.Internal;


/**
 * A class modelling foreign key relationships and constraints of tables of 
 * the <code>public</code> schema.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // IDENTITY definitions
    // -------------------------------------------------------------------------

    public static final Identity<LicensesRecord, Integer> IDENTITY_LICENSES = Identities0.IDENTITY_LICENSES;
    public static final Identity<PermissionRecord, Integer> IDENTITY_PERMISSION = Identities0.IDENTITY_PERMISSION;
    public static final Identity<RolesRecord, Long> IDENTITY_ROLES = Identities0.IDENTITY_ROLES;

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<ChannelsRecord> CHANNELS_CHANNEL_ID = UniqueKeys0.CHANNELS_CHANNEL_ID;
    public static final UniqueKey<GuildsRecord> GUILDS_GUILD_ID = UniqueKeys0.GUILDS_GUILD_ID;
    public static final UniqueKey<GuildsRecord> GUILDS_LICENSE_ID = UniqueKeys0.GUILDS_LICENSE_ID;
    public static final UniqueKey<InternalBotDataRecord> INTERNAL_BOT_DATA_CLIENT_ID = UniqueKeys0.INTERNAL_BOT_DATA_CLIENT_ID;
    public static final UniqueKey<InternalBotShardsRecord> INTERNAL_BOT_SHARDS_SHARD_ID = UniqueKeys0.INTERNAL_BOT_SHARDS_SHARD_ID;
    public static final UniqueKey<LicenseTypesRecord> LICENSE_TYPES_LICENSE_TYPE_ID = UniqueKeys0.LICENSE_TYPES_LICENSE_TYPE_ID;
    public static final UniqueKey<LicensesRecord> LICENSES_LICENSE_ID = UniqueKeys0.LICENSES_LICENSE_ID;
    public static final UniqueKey<LicensesRecord> LICENSES_LICENSE_KEY = UniqueKeys0.LICENSES_LICENSE_KEY;
    public static final UniqueKey<MembersRecord> MEMBERS_GUILD_ID_USER_ID = UniqueKeys0.MEMBERS_GUILD_ID_USER_ID;
    public static final UniqueKey<MembersRecord> MEMBERS_GUILD_ID_USER_ID_UNIQUE = UniqueKeys0.MEMBERS_GUILD_ID_USER_ID_UNIQUE;
    public static final UniqueKey<MembersRolesRecord> MEMBERS_ROLES_USER_ID_ROLE_ID = UniqueKeys0.MEMBERS_ROLES_USER_ID_ROLE_ID;
    public static final UniqueKey<MessagesRecord> MESSAGES_MESSAGE_ID = UniqueKeys0.MESSAGES_MESSAGE_ID;
    public static final UniqueKey<PermissionRecord> PERMISSION_PERMISSION_ID = UniqueKeys0.PERMISSION_PERMISSION_ID;
    public static final UniqueKey<RolesRecord> ROLES_ROLE_ID = UniqueKeys0.ROLES_ROLE_ID;
    public static final UniqueKey<RolesRecord> ROLES_GUILD_ID_ROLE_NAME = UniqueKeys0.ROLES_GUILD_ID_ROLE_NAME;
    public static final UniqueKey<RolesPermissionRecord> ROLE_PERMISSION_ROLE_ID_PERMISSION_ID = UniqueKeys0.ROLE_PERMISSION_ROLE_ID_PERMISSION_ID;
    public static final UniqueKey<TagsRecord> TAGS_TAG_NAME_GUILD_ID = UniqueKeys0.TAGS_TAG_NAME_GUILD_ID;
    public static final UniqueKey<UsersRecord> USERS_USER_ID = UniqueKeys0.USERS_USER_ID;

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------

    public static final ForeignKey<ChannelsRecord, GuildsRecord> CHANNELS__CHANNELS_GUILD_ID_FKEY = ForeignKeys0.CHANNELS__CHANNELS_GUILD_ID_FKEY;
    public static final ForeignKey<GuildsRecord, LicensesRecord> GUILDS__GUILDS_LICENSE_ID_FKEY = ForeignKeys0.GUILDS__GUILDS_LICENSE_ID_FKEY;
    public static final ForeignKey<InternalBotShardsRecord, InternalBotDataRecord> INTERNAL_BOT_SHARDS__INTERNAL_BOT_SHARDS_CLIENT_ID_FKEY = ForeignKeys0.INTERNAL_BOT_SHARDS__INTERNAL_BOT_SHARDS_CLIENT_ID_FKEY;
    public static final ForeignKey<LicensesRecord, LicenseTypesRecord> LICENSES__LICENSES_LICENSE_TYPE_FKEY = ForeignKeys0.LICENSES__LICENSES_LICENSE_TYPE_FKEY;
    public static final ForeignKey<MembersRecord, GuildsRecord> MEMBERS__MEMBERS_GUILD_ID_FKEY = ForeignKeys0.MEMBERS__MEMBERS_GUILD_ID_FKEY;
    public static final ForeignKey<MembersRecord, UsersRecord> MEMBERS__MEMBERS_USER_ID_FKEY = ForeignKeys0.MEMBERS__MEMBERS_USER_ID_FKEY;
    public static final ForeignKey<MembersRolesRecord, GuildsRecord> MEMBERS_ROLES__MEMBERS_ROLES_GUILD_ID_FKEY = ForeignKeys0.MEMBERS_ROLES__MEMBERS_ROLES_GUILD_ID_FKEY;
    public static final ForeignKey<MembersRolesRecord, MembersRecord> MEMBERS_ROLES__MEMBERS_ROLES_GUILD_ID_USER_ID_FKEY = ForeignKeys0.MEMBERS_ROLES__MEMBERS_ROLES_GUILD_ID_USER_ID_FKEY;
    public static final ForeignKey<MembersRolesRecord, RolesRecord> MEMBERS_ROLES__MEMBERS_ROLES_ROLE_ID_FKEY = ForeignKeys0.MEMBERS_ROLES__MEMBERS_ROLES_ROLE_ID_FKEY;
    public static final ForeignKey<MessagesRecord, GuildsRecord> MESSAGES__MESSAGES_GUILD_ID_FKEY = ForeignKeys0.MESSAGES__MESSAGES_GUILD_ID_FKEY;
    public static final ForeignKey<MessagesRecord, ChannelsRecord> MESSAGES__MESSAGES_CHANNEL_ID_FKEY = ForeignKeys0.MESSAGES__MESSAGES_CHANNEL_ID_FKEY;
    public static final ForeignKey<MessagesRecord, UsersRecord> MESSAGES__MESSAGES_USER_ID_FKEY = ForeignKeys0.MESSAGES__MESSAGES_USER_ID_FKEY;
    public static final ForeignKey<RolesPermissionRecord, RolesRecord> ROLES_PERMISSION__ROLE_PERMISSION_ROLE_ID_FKEY = ForeignKeys0.ROLES_PERMISSION__ROLE_PERMISSION_ROLE_ID_FKEY;
    public static final ForeignKey<RolesPermissionRecord, PermissionRecord> ROLES_PERMISSION__ROLE_PERMISSION_PERMISSION_ID_FKEY = ForeignKeys0.ROLES_PERMISSION__ROLE_PERMISSION_PERMISSION_ID_FKEY;
    public static final ForeignKey<TagsRecord, MembersRecord> TAGS__TAGS_GUILD_ID_USER_ID_FKEY = ForeignKeys0.TAGS__TAGS_GUILD_ID_USER_ID_FKEY;

    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class Identities0 {
        public static Identity<LicensesRecord, Integer> IDENTITY_LICENSES = Internal.createIdentity(Licenses.LICENSES, Licenses.LICENSES.LICENSE_ID);
        public static Identity<PermissionRecord, Integer> IDENTITY_PERMISSION = Internal.createIdentity(Permission.PERMISSION, Permission.PERMISSION.PERMISSION_ID);
        public static Identity<RolesRecord, Long> IDENTITY_ROLES = Internal.createIdentity(Roles.ROLES, Roles.ROLES.ROLE_ID);
    }

    private static class UniqueKeys0 {
        public static final UniqueKey<ChannelsRecord> CHANNELS_CHANNEL_ID = Internal.createUniqueKey(Channels.CHANNELS, "channels_channel_id", new TableField[] { Channels.CHANNELS.CHANNEL_ID }, true);
        public static final UniqueKey<GuildsRecord> GUILDS_GUILD_ID = Internal.createUniqueKey(Guilds.GUILDS, "guilds_guild_id", new TableField[] { Guilds.GUILDS.GUILD_ID }, true);
        public static final UniqueKey<GuildsRecord> GUILDS_LICENSE_ID = Internal.createUniqueKey(Guilds.GUILDS, "guilds_license_id", new TableField[] { Guilds.GUILDS.LICENSE_ID }, true);
        public static final UniqueKey<InternalBotDataRecord> INTERNAL_BOT_DATA_CLIENT_ID = Internal.createUniqueKey(InternalBotData.INTERNAL_BOT_DATA, "internal_bot_data_client_id", new TableField[] { InternalBotData.INTERNAL_BOT_DATA.CLIENT_ID }, true);
        public static final UniqueKey<InternalBotShardsRecord> INTERNAL_BOT_SHARDS_SHARD_ID = Internal.createUniqueKey(InternalBotShards.INTERNAL_BOT_SHARDS, "internal_bot_shards_shard_id", new TableField[] { InternalBotShards.INTERNAL_BOT_SHARDS.SHARD_ID }, true);
        public static final UniqueKey<LicenseTypesRecord> LICENSE_TYPES_LICENSE_TYPE_ID = Internal.createUniqueKey(LicenseTypes.LICENSE_TYPES, "license_types_license_type_id", new TableField[] { LicenseTypes.LICENSE_TYPES.LICENSE_TYPE_ID }, true);
        public static final UniqueKey<LicensesRecord> LICENSES_LICENSE_ID = Internal.createUniqueKey(Licenses.LICENSES, "licenses_license_id", new TableField[] { Licenses.LICENSES.LICENSE_ID }, true);
        public static final UniqueKey<LicensesRecord> LICENSES_LICENSE_KEY = Internal.createUniqueKey(Licenses.LICENSES, "licenses_license_key", new TableField[] { Licenses.LICENSES.LICENSE_KEY }, true);
        public static final UniqueKey<MembersRecord> MEMBERS_GUILD_ID_USER_ID = Internal.createUniqueKey(Members.MEMBERS, "members_guild_id_user_id", new TableField[] { Members.MEMBERS.GUILD_ID, Members.MEMBERS.USER_ID }, true);
        public static final UniqueKey<MembersRecord> MEMBERS_GUILD_ID_USER_ID_UNIQUE = Internal.createUniqueKey(Members.MEMBERS, "members_guild_id_user_id_unique", new TableField[] { Members.MEMBERS.GUILD_ID, Members.MEMBERS.USER_ID }, true);
        public static final UniqueKey<MembersRolesRecord> MEMBERS_ROLES_USER_ID_ROLE_ID = Internal.createUniqueKey(MembersRoles.MEMBERS_ROLES, "members_roles_user_id_role_id", new TableField[] { MembersRoles.MEMBERS_ROLES.USER_ID, MembersRoles.MEMBERS_ROLES.ROLE_ID }, true);
        public static final UniqueKey<MessagesRecord> MESSAGES_MESSAGE_ID = Internal.createUniqueKey(Messages.MESSAGES, "messages_message_id", new TableField[] { Messages.MESSAGES.MESSAGE_ID }, true);
        public static final UniqueKey<PermissionRecord> PERMISSION_PERMISSION_ID = Internal.createUniqueKey(Permission.PERMISSION, "permission_permission_id", new TableField[] { Permission.PERMISSION.PERMISSION_ID }, true);
        public static final UniqueKey<RolesRecord> ROLES_ROLE_ID = Internal.createUniqueKey(Roles.ROLES, "roles_role_id", new TableField[] { Roles.ROLES.ROLE_ID }, true);
        public static final UniqueKey<RolesRecord> ROLES_GUILD_ID_ROLE_NAME = Internal.createUniqueKey(Roles.ROLES, "roles_guild_id_role_name", new TableField[] { Roles.ROLES.GUILD_ID, Roles.ROLES.ROLE_NAME }, true);
        public static final UniqueKey<RolesPermissionRecord> ROLE_PERMISSION_ROLE_ID_PERMISSION_ID = Internal.createUniqueKey(RolesPermission.ROLES_PERMISSION, "role_permission_role_id_permission_id", new TableField[] { RolesPermission.ROLES_PERMISSION.ROLE_ID, RolesPermission.ROLES_PERMISSION.PERMISSION_ID }, true);
        public static final UniqueKey<TagsRecord> TAGS_TAG_NAME_GUILD_ID = Internal.createUniqueKey(Tags.TAGS, "tags_tag_name_guild_id", new TableField[] { Tags.TAGS.TAG_NAME, Tags.TAGS.GUILD_ID }, true);
        public static final UniqueKey<UsersRecord> USERS_USER_ID = Internal.createUniqueKey(Users.USERS, "users_user_id", new TableField[] { Users.USERS.USER_ID }, true);
    }

    private static class ForeignKeys0 {
        public static final ForeignKey<ChannelsRecord, GuildsRecord> CHANNELS__CHANNELS_GUILD_ID_FKEY = Internal.createForeignKey(Keys.GUILDS_GUILD_ID, Channels.CHANNELS, "channels_guild_id_fkey", new TableField[] { Channels.CHANNELS.GUILD_ID }, true);
        public static final ForeignKey<GuildsRecord, LicensesRecord> GUILDS__GUILDS_LICENSE_ID_FKEY = Internal.createForeignKey(Keys.LICENSES_LICENSE_ID, Guilds.GUILDS, "guilds_license_id_fkey", new TableField[] { Guilds.GUILDS.LICENSE_ID }, true);
        public static final ForeignKey<InternalBotShardsRecord, InternalBotDataRecord> INTERNAL_BOT_SHARDS__INTERNAL_BOT_SHARDS_CLIENT_ID_FKEY = Internal.createForeignKey(Keys.INTERNAL_BOT_DATA_CLIENT_ID, InternalBotShards.INTERNAL_BOT_SHARDS, "internal_bot_shards_client_id_fkey", new TableField[] { InternalBotShards.INTERNAL_BOT_SHARDS.CLIENT_ID }, true);
        public static final ForeignKey<LicensesRecord, LicenseTypesRecord> LICENSES__LICENSES_LICENSE_TYPE_FKEY = Internal.createForeignKey(Keys.LICENSE_TYPES_LICENSE_TYPE_ID, Licenses.LICENSES, "licenses_license_type_fkey", new TableField[] { Licenses.LICENSES.LICENSE_TYPE }, true);
        public static final ForeignKey<MembersRecord, GuildsRecord> MEMBERS__MEMBERS_GUILD_ID_FKEY = Internal.createForeignKey(Keys.GUILDS_GUILD_ID, Members.MEMBERS, "members_guild_id_fkey", new TableField[] { Members.MEMBERS.GUILD_ID }, true);
        public static final ForeignKey<MembersRecord, UsersRecord> MEMBERS__MEMBERS_USER_ID_FKEY = Internal.createForeignKey(Keys.USERS_USER_ID, Members.MEMBERS, "members_user_id_fkey", new TableField[] { Members.MEMBERS.USER_ID }, true);
        public static final ForeignKey<MembersRolesRecord, GuildsRecord> MEMBERS_ROLES__MEMBERS_ROLES_GUILD_ID_FKEY = Internal.createForeignKey(Keys.GUILDS_GUILD_ID, MembersRoles.MEMBERS_ROLES, "members_roles_guild_id_fkey", new TableField[] { MembersRoles.MEMBERS_ROLES.GUILD_ID }, true);
        public static final ForeignKey<MembersRolesRecord, MembersRecord> MEMBERS_ROLES__MEMBERS_ROLES_GUILD_ID_USER_ID_FKEY = Internal.createForeignKey(Keys.MEMBERS_GUILD_ID_USER_ID, MembersRoles.MEMBERS_ROLES, "members_roles_guild_id_user_id_fkey", new TableField[] { MembersRoles.MEMBERS_ROLES.GUILD_ID, MembersRoles.MEMBERS_ROLES.USER_ID }, true);
        public static final ForeignKey<MembersRolesRecord, RolesRecord> MEMBERS_ROLES__MEMBERS_ROLES_ROLE_ID_FKEY = Internal.createForeignKey(Keys.ROLES_ROLE_ID, MembersRoles.MEMBERS_ROLES, "members_roles_role_id_fkey", new TableField[] { MembersRoles.MEMBERS_ROLES.ROLE_ID }, true);
        public static final ForeignKey<MessagesRecord, GuildsRecord> MESSAGES__MESSAGES_GUILD_ID_FKEY = Internal.createForeignKey(Keys.GUILDS_GUILD_ID, Messages.MESSAGES, "messages_guild_id_fkey", new TableField[] { Messages.MESSAGES.GUILD_ID }, true);
        public static final ForeignKey<MessagesRecord, ChannelsRecord> MESSAGES__MESSAGES_CHANNEL_ID_FKEY = Internal.createForeignKey(Keys.CHANNELS_CHANNEL_ID, Messages.MESSAGES, "messages_channel_id_fkey", new TableField[] { Messages.MESSAGES.CHANNEL_ID }, true);
        public static final ForeignKey<MessagesRecord, UsersRecord> MESSAGES__MESSAGES_USER_ID_FKEY = Internal.createForeignKey(Keys.USERS_USER_ID, Messages.MESSAGES, "messages_user_id_fkey", new TableField[] { Messages.MESSAGES.USER_ID }, true);
        public static final ForeignKey<RolesPermissionRecord, RolesRecord> ROLES_PERMISSION__ROLE_PERMISSION_ROLE_ID_FKEY = Internal.createForeignKey(Keys.ROLES_ROLE_ID, RolesPermission.ROLES_PERMISSION, "role_permission_role_id_fkey", new TableField[] { RolesPermission.ROLES_PERMISSION.ROLE_ID }, true);
        public static final ForeignKey<RolesPermissionRecord, PermissionRecord> ROLES_PERMISSION__ROLE_PERMISSION_PERMISSION_ID_FKEY = Internal.createForeignKey(Keys.PERMISSION_PERMISSION_ID, RolesPermission.ROLES_PERMISSION, "role_permission_permission_id_fkey", new TableField[] { RolesPermission.ROLES_PERMISSION.PERMISSION_ID }, true);
        public static final ForeignKey<TagsRecord, MembersRecord> TAGS__TAGS_GUILD_ID_USER_ID_FKEY = Internal.createForeignKey(Keys.MEMBERS_GUILD_ID_USER_ID, Tags.TAGS, "tags_guild_id_user_id_fkey", new TableField[] { Tags.TAGS.GUILD_ID, Tags.TAGS.USER_ID }, true);
    }
}
