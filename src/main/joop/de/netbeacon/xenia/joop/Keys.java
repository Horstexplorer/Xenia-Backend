/*
 * This file is generated by jOOQ.
 */
package de.netbeacon.xenia.joop;


import de.netbeacon.xenia.joop.tables.Channels;
import de.netbeacon.xenia.joop.tables.ClientSettings;
import de.netbeacon.xenia.joop.tables.ClientShards;
import de.netbeacon.xenia.joop.tables.Guilds;
import de.netbeacon.xenia.joop.tables.Members;
import de.netbeacon.xenia.joop.tables.Permission;
import de.netbeacon.xenia.joop.tables.Roles;
import de.netbeacon.xenia.joop.tables.RolesPermission;
import de.netbeacon.xenia.joop.tables.RolesUser;
import de.netbeacon.xenia.joop.tables.Users;
import de.netbeacon.xenia.joop.tables.records.ChannelsRecord;
import de.netbeacon.xenia.joop.tables.records.ClientSettingsRecord;
import de.netbeacon.xenia.joop.tables.records.ClientShardsRecord;
import de.netbeacon.xenia.joop.tables.records.GuildsRecord;
import de.netbeacon.xenia.joop.tables.records.MembersRecord;
import de.netbeacon.xenia.joop.tables.records.PermissionRecord;
import de.netbeacon.xenia.joop.tables.records.RolesPermissionRecord;
import de.netbeacon.xenia.joop.tables.records.RolesRecord;
import de.netbeacon.xenia.joop.tables.records.RolesUserRecord;
import de.netbeacon.xenia.joop.tables.records.UsersRecord;

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

    public static final Identity<ClientSettingsRecord, Integer> IDENTITY_CLIENT_SETTINGS = Identities0.IDENTITY_CLIENT_SETTINGS;
    public static final Identity<PermissionRecord, Integer> IDENTITY_PERMISSION = Identities0.IDENTITY_PERMISSION;
    public static final Identity<RolesRecord, Integer> IDENTITY_ROLES = Identities0.IDENTITY_ROLES;

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<ChannelsRecord> CHANNELS_CHANNEL_ID = UniqueKeys0.CHANNELS_CHANNEL_ID;
    public static final UniqueKey<ClientSettingsRecord> CLIENT_BOT_SETTINGS_BOT_ID = UniqueKeys0.CLIENT_BOT_SETTINGS_BOT_ID;
    public static final UniqueKey<ClientShardsRecord> CLIENT_BOT_SHARDS_PKEY = UniqueKeys0.CLIENT_BOT_SHARDS_PKEY;
    public static final UniqueKey<GuildsRecord> GUILDS_GUILD_ID = UniqueKeys0.GUILDS_GUILD_ID;
    public static final UniqueKey<MembersRecord> MEMBERS_GUILD_ID_USER_ID = UniqueKeys0.MEMBERS_GUILD_ID_USER_ID;
    public static final UniqueKey<PermissionRecord> PERMISSION_DEF_PERMISSION_ID = UniqueKeys0.PERMISSION_DEF_PERMISSION_ID;
    public static final UniqueKey<RolesRecord> ROLES_ROLE_ID = UniqueKeys0.ROLES_ROLE_ID;
    public static final UniqueKey<RolesPermissionRecord> ROLES_PERMISSION_ROLE_ID_PERMISSION_ID = UniqueKeys0.ROLES_PERMISSION_ROLE_ID_PERMISSION_ID;
    public static final UniqueKey<RolesUserRecord> ROLES_USER_ROLE_ID_USER_ID = UniqueKeys0.ROLES_USER_ROLE_ID_USER_ID;
    public static final UniqueKey<UsersRecord> USERS_USER_ID = UniqueKeys0.USERS_USER_ID;

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------

    public static final ForeignKey<ChannelsRecord, GuildsRecord> CHANNELS__CHANNELS_GUILD_ID_FKEY = ForeignKeys0.CHANNELS__CHANNELS_GUILD_ID_FKEY;
    public static final ForeignKey<ClientShardsRecord, ClientSettingsRecord> CLIENT_SHARDS__CLIENT_BOT_SHARDS_CLIENT_BOT_ID_FKEY = ForeignKeys0.CLIENT_SHARDS__CLIENT_BOT_SHARDS_CLIENT_BOT_ID_FKEY;
    public static final ForeignKey<MembersRecord, GuildsRecord> MEMBERS__MEMBERS_GUILD_ID_FKEY = ForeignKeys0.MEMBERS__MEMBERS_GUILD_ID_FKEY;
    public static final ForeignKey<MembersRecord, UsersRecord> MEMBERS__MEMBERS_USER_ID_FKEY = ForeignKeys0.MEMBERS__MEMBERS_USER_ID_FKEY;
    public static final ForeignKey<RolesRecord, GuildsRecord> ROLES__ROLES_GUILD_ID_FKEY = ForeignKeys0.ROLES__ROLES_GUILD_ID_FKEY;
    public static final ForeignKey<RolesPermissionRecord, RolesRecord> ROLES_PERMISSION__ROLES_PERMISSION_ROLE_ID_FKEY = ForeignKeys0.ROLES_PERMISSION__ROLES_PERMISSION_ROLE_ID_FKEY;
    public static final ForeignKey<RolesPermissionRecord, PermissionRecord> ROLES_PERMISSION__ROLES_PERMISSION_PERMISSION_ID_FKEY = ForeignKeys0.ROLES_PERMISSION__ROLES_PERMISSION_PERMISSION_ID_FKEY;
    public static final ForeignKey<RolesUserRecord, RolesRecord> ROLES_USER__ROLES_USER_ROLE_ID_FKEY = ForeignKeys0.ROLES_USER__ROLES_USER_ROLE_ID_FKEY;
    public static final ForeignKey<RolesUserRecord, UsersRecord> ROLES_USER__ROLES_USER_USER_ID_FKEY = ForeignKeys0.ROLES_USER__ROLES_USER_USER_ID_FKEY;

    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class Identities0 {
        public static Identity<ClientSettingsRecord, Integer> IDENTITY_CLIENT_SETTINGS = Internal.createIdentity(ClientSettings.CLIENT_SETTINGS, ClientSettings.CLIENT_SETTINGS.CLIENT_ID);
        public static Identity<PermissionRecord, Integer> IDENTITY_PERMISSION = Internal.createIdentity(Permission.PERMISSION, Permission.PERMISSION.PERMISSION_ID);
        public static Identity<RolesRecord, Integer> IDENTITY_ROLES = Internal.createIdentity(Roles.ROLES, Roles.ROLES.ROLE_ID);
    }

    private static class UniqueKeys0 {
        public static final UniqueKey<ChannelsRecord> CHANNELS_CHANNEL_ID = Internal.createUniqueKey(Channels.CHANNELS, "channels_channel_id", new TableField[] { Channels.CHANNELS.CHANNEL_ID }, true);
        public static final UniqueKey<ClientSettingsRecord> CLIENT_BOT_SETTINGS_BOT_ID = Internal.createUniqueKey(ClientSettings.CLIENT_SETTINGS, "client_bot_settings_bot_id", new TableField[] { ClientSettings.CLIENT_SETTINGS.CLIENT_ID }, true);
        public static final UniqueKey<ClientShardsRecord> CLIENT_BOT_SHARDS_PKEY = Internal.createUniqueKey(ClientShards.CLIENT_SHARDS, "client_bot_shards_pkey", new TableField[] { ClientShards.CLIENT_SHARDS.SHARD_ID }, true);
        public static final UniqueKey<GuildsRecord> GUILDS_GUILD_ID = Internal.createUniqueKey(Guilds.GUILDS, "guilds_guild_id", new TableField[] { Guilds.GUILDS.GUILD_ID }, true);
        public static final UniqueKey<MembersRecord> MEMBERS_GUILD_ID_USER_ID = Internal.createUniqueKey(Members.MEMBERS, "members_guild_id_user_id", new TableField[] { Members.MEMBERS.GUILD_ID, Members.MEMBERS.USER_ID }, true);
        public static final UniqueKey<PermissionRecord> PERMISSION_DEF_PERMISSION_ID = Internal.createUniqueKey(Permission.PERMISSION, "permission_def_permission_id", new TableField[] { Permission.PERMISSION.PERMISSION_ID }, true);
        public static final UniqueKey<RolesRecord> ROLES_ROLE_ID = Internal.createUniqueKey(Roles.ROLES, "roles_role_id", new TableField[] { Roles.ROLES.ROLE_ID }, true);
        public static final UniqueKey<RolesPermissionRecord> ROLES_PERMISSION_ROLE_ID_PERMISSION_ID = Internal.createUniqueKey(RolesPermission.ROLES_PERMISSION, "roles_permission_role_id_permission_id", new TableField[] { RolesPermission.ROLES_PERMISSION.ROLE_ID, RolesPermission.ROLES_PERMISSION.PERMISSION_ID }, true);
        public static final UniqueKey<RolesUserRecord> ROLES_USER_ROLE_ID_USER_ID = Internal.createUniqueKey(RolesUser.ROLES_USER, "roles_user_role_id_user_id", new TableField[] { RolesUser.ROLES_USER.ROLE_ID, RolesUser.ROLES_USER.USER_ID }, true);
        public static final UniqueKey<UsersRecord> USERS_USER_ID = Internal.createUniqueKey(Users.USERS, "users_user_id", new TableField[] { Users.USERS.USER_ID }, true);
    }

    private static class ForeignKeys0 {
        public static final ForeignKey<ChannelsRecord, GuildsRecord> CHANNELS__CHANNELS_GUILD_ID_FKEY = Internal.createForeignKey(Keys.GUILDS_GUILD_ID, Channels.CHANNELS, "channels_guild_id_fkey", new TableField[] { Channels.CHANNELS.GUILD_ID }, true);
        public static final ForeignKey<ClientShardsRecord, ClientSettingsRecord> CLIENT_SHARDS__CLIENT_BOT_SHARDS_CLIENT_BOT_ID_FKEY = Internal.createForeignKey(Keys.CLIENT_BOT_SETTINGS_BOT_ID, ClientShards.CLIENT_SHARDS, "client_bot_shards_client_bot_id_fkey", new TableField[] { ClientShards.CLIENT_SHARDS.BOT_ID }, true);
        public static final ForeignKey<MembersRecord, GuildsRecord> MEMBERS__MEMBERS_GUILD_ID_FKEY = Internal.createForeignKey(Keys.GUILDS_GUILD_ID, Members.MEMBERS, "members_guild_id_fkey", new TableField[] { Members.MEMBERS.GUILD_ID }, true);
        public static final ForeignKey<MembersRecord, UsersRecord> MEMBERS__MEMBERS_USER_ID_FKEY = Internal.createForeignKey(Keys.USERS_USER_ID, Members.MEMBERS, "members_user_id_fkey", new TableField[] { Members.MEMBERS.USER_ID }, true);
        public static final ForeignKey<RolesRecord, GuildsRecord> ROLES__ROLES_GUILD_ID_FKEY = Internal.createForeignKey(Keys.GUILDS_GUILD_ID, Roles.ROLES, "roles_guild_id_fkey", new TableField[] { Roles.ROLES.GUILD_ID }, true);
        public static final ForeignKey<RolesPermissionRecord, RolesRecord> ROLES_PERMISSION__ROLES_PERMISSION_ROLE_ID_FKEY = Internal.createForeignKey(Keys.ROLES_ROLE_ID, RolesPermission.ROLES_PERMISSION, "roles_permission_role_id_fkey", new TableField[] { RolesPermission.ROLES_PERMISSION.ROLE_ID }, true);
        public static final ForeignKey<RolesPermissionRecord, PermissionRecord> ROLES_PERMISSION__ROLES_PERMISSION_PERMISSION_ID_FKEY = Internal.createForeignKey(Keys.PERMISSION_DEF_PERMISSION_ID, RolesPermission.ROLES_PERMISSION, "roles_permission_permission_id_fkey", new TableField[] { RolesPermission.ROLES_PERMISSION.PERMISSION_ID }, true);
        public static final ForeignKey<RolesUserRecord, RolesRecord> ROLES_USER__ROLES_USER_ROLE_ID_FKEY = Internal.createForeignKey(Keys.ROLES_ROLE_ID, RolesUser.ROLES_USER, "roles_user_role_id_fkey", new TableField[] { RolesUser.ROLES_USER.ROLE_ID }, true);
        public static final ForeignKey<RolesUserRecord, UsersRecord> ROLES_USER__ROLES_USER_USER_ID_FKEY = Internal.createForeignKey(Keys.USERS_USER_ID, RolesUser.ROLES_USER, "roles_user_user_id_fkey", new TableField[] { RolesUser.ROLES_USER.USER_ID }, true);
    }
}
