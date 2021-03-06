/*
 * This file is generated by jOOQ.
 */
package de.netbeacon.xenia.jooq.tables;


import de.netbeacon.xenia.jooq.Keys;
import de.netbeacon.xenia.jooq.Public;
import de.netbeacon.xenia.jooq.tables.records.NotificationsRecord;
import org.jooq.Record;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Notifications extends TableImpl<NotificationsRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.notifications</code>
     */
    public static final Notifications NOTIFICATIONS = new Notifications();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<NotificationsRecord> getRecordType() {
        return NotificationsRecord.class;
    }

    /**
     * The column <code>public.notifications.notification_id</code>.
     */
    public final TableField<NotificationsRecord, Long> NOTIFICATION_ID = createField(DSL.name("notification_id"), SQLDataType.BIGINT.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.notifications.creation_timestamp</code>.
     */
    public final TableField<NotificationsRecord, LocalDateTime> CREATION_TIMESTAMP = createField(DSL.name("creation_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false).defaultValue(DSL.field("now()", SQLDataType.LOCALDATETIME)), this, "");

    /**
     * The column <code>public.notifications.guild_id</code>.
     */
    public final TableField<NotificationsRecord, Long> GUILD_ID = createField(DSL.name("guild_id"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.notifications.channel_id</code>.
     */
    public final TableField<NotificationsRecord, Long> CHANNEL_ID = createField(DSL.name("channel_id"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.notifications.user_id</code>.
     */
    public final TableField<NotificationsRecord, Long> USER_ID = createField(DSL.name("user_id"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.notifications.notification_target</code>.
     */
    public final TableField<NotificationsRecord, LocalDateTime> NOTIFICATION_TARGET = createField(DSL.name("notification_target"), SQLDataType.LOCALDATETIME(6).nullable(false), this, "");

    /**
     * The column <code>public.notifications.notification_message</code>.
     */
    public final TableField<NotificationsRecord, String> NOTIFICATION_MESSAGE = createField(DSL.name("notification_message"), SQLDataType.CLOB.nullable(false), this, "");

    private Notifications(Name alias, Table<NotificationsRecord> aliased) {
        this(alias, aliased, null);
    }

    private Notifications(Name alias, Table<NotificationsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.notifications</code> table reference
     */
    public Notifications(String alias) {
        this(DSL.name(alias), NOTIFICATIONS);
    }

    /**
     * Create an aliased <code>public.notifications</code> table reference
     */
    public Notifications(Name alias) {
        this(alias, NOTIFICATIONS);
    }

    /**
     * Create a <code>public.notifications</code> table reference
     */
    public Notifications() {
        this(DSL.name("notifications"), null);
    }

    public <O extends Record> Notifications(Table<O> child, ForeignKey<O, NotificationsRecord> key) {
        super(child, key, NOTIFICATIONS);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public Identity<NotificationsRecord, Long> getIdentity() {
        return (Identity<NotificationsRecord, Long>) super.getIdentity();
    }

    @Override
    public UniqueKey<NotificationsRecord> getPrimaryKey() {
        return Keys.NOTIFICATION_NOTIFICATION_ID;
    }

    @Override
    public List<ForeignKey<NotificationsRecord, ?>> getReferences() {
        return Arrays.asList(Keys.NOTIFICATIONS__NOTIFICATION_GUILD_ID_FKEY, Keys.NOTIFICATIONS__NOTIFICATION_GUILD_ID_USER_ID_FKEY, Keys.NOTIFICATIONS__NOTIFICATION_CHANNEL_ID_FKEY);
    }

    private transient Guilds _guilds;
    private transient Members _members;
    private transient Channels _channels;

    public Guilds guilds() {
        if (_guilds == null)
            _guilds = new Guilds(this, Keys.NOTIFICATIONS__NOTIFICATION_GUILD_ID_FKEY);

        return _guilds;
    }

    public Members members() {
        if (_members == null)
            _members = new Members(this, Keys.NOTIFICATIONS__NOTIFICATION_GUILD_ID_USER_ID_FKEY);

        return _members;
    }

    public Channels channels() {
        if (_channels == null)
            _channels = new Channels(this, Keys.NOTIFICATIONS__NOTIFICATION_CHANNEL_ID_FKEY);

        return _channels;
    }

    @Override
    public Notifications as(String alias) {
        return new Notifications(DSL.name(alias), this);
    }

    @Override
    public Notifications as(Name alias) {
        return new Notifications(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Notifications rename(String name) {
        return new Notifications(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Notifications rename(Name name) {
        return new Notifications(name, null);
    }

    // -------------------------------------------------------------------------
    // Row7 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row7<Long, LocalDateTime, Long, Long, Long, LocalDateTime, String> fieldsRow() {
        return (Row7) super.fieldsRow();
    }
}
