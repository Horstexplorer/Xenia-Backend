/*
 * This file is generated by jOOQ.
 */
package de.netbeacon.xenia.joop.tables;


import de.netbeacon.xenia.joop.Keys;
import de.netbeacon.xenia.joop.Public;
import de.netbeacon.xenia.joop.tables.records.MessagesRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Messages extends TableImpl<MessagesRecord> {

    private static final long serialVersionUID = -2029722210;

    /**
     * The reference instance of <code>public.messages</code>
     */
    public static final Messages MESSAGES = new Messages();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<MessagesRecord> getRecordType() {
        return MessagesRecord.class;
    }

    /**
     * The column <code>public.messages.message_id</code>.
     */
    public final TableField<MessagesRecord, Long> MESSAGE_ID = createField(DSL.name("message_id"), org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.messages.guild_id</code>.
     */
    public final TableField<MessagesRecord, Long> GUILD_ID = createField(DSL.name("guild_id"), org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.messages.channel_id</code>.
     */
    public final TableField<MessagesRecord, Long> CHANNEL_ID = createField(DSL.name("channel_id"), org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.messages.user_id</code>.
     */
    public final TableField<MessagesRecord, Long> USER_ID = createField(DSL.name("user_id"), org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.messages.creation_timestamp</code>.
     */
    public final TableField<MessagesRecord, LocalDateTime> CREATION_TIMESTAMP = createField(DSL.name("creation_timestamp"), org.jooq.impl.SQLDataType.LOCALDATETIME.nullable(false).defaultValue(org.jooq.impl.DSL.field("now()", org.jooq.impl.SQLDataType.LOCALDATETIME)), this, "");

    /**
     * The column <code>public.messages.creation_timestamp_discord</code>.
     */
    public final TableField<MessagesRecord, LocalDateTime> CREATION_TIMESTAMP_DISCORD = createField(DSL.name("creation_timestamp_discord"), org.jooq.impl.SQLDataType.LOCALDATETIME.nullable(false), this, "");

    /**
     * The column <code>public.messages.message_salt</code>.
     */
    public final TableField<MessagesRecord, String> MESSAGE_SALT = createField(DSL.name("message_salt"), org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "");

    /**
     * The column <code>public.messages.message_content</code>.
     */
    public final TableField<MessagesRecord, String> MESSAGE_CONTENT = createField(DSL.name("message_content"), org.jooq.impl.SQLDataType.CLOB.nullable(false), this, "");

    /**
     * Create a <code>public.messages</code> table reference
     */
    public Messages() {
        this(DSL.name("messages"), null);
    }

    /**
     * Create an aliased <code>public.messages</code> table reference
     */
    public Messages(String alias) {
        this(DSL.name(alias), MESSAGES);
    }

    /**
     * Create an aliased <code>public.messages</code> table reference
     */
    public Messages(Name alias) {
        this(alias, MESSAGES);
    }

    private Messages(Name alias, Table<MessagesRecord> aliased) {
        this(alias, aliased, null);
    }

    private Messages(Name alias, Table<MessagesRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    public <O extends Record> Messages(Table<O> child, ForeignKey<O, MessagesRecord> key) {
        super(child, key, MESSAGES);
    }

    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public UniqueKey<MessagesRecord> getPrimaryKey() {
        return Keys.MESSAGES_MESSAGE_ID;
    }

    @Override
    public List<UniqueKey<MessagesRecord>> getKeys() {
        return Arrays.<UniqueKey<MessagesRecord>>asList(Keys.MESSAGES_MESSAGE_ID);
    }

    @Override
    public List<ForeignKey<MessagesRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<MessagesRecord, ?>>asList(Keys.MESSAGES__MESSAGES_GUILD_ID_FKEY, Keys.MESSAGES__MESSAGES_CHANNEL_ID_FKEY, Keys.MESSAGES__MESSAGES_USER_ID_FKEY);
    }

    public Guilds guilds() {
        return new Guilds(this, Keys.MESSAGES__MESSAGES_GUILD_ID_FKEY);
    }

    public Channels channels() {
        return new Channels(this, Keys.MESSAGES__MESSAGES_CHANNEL_ID_FKEY);
    }

    public Users users() {
        return new Users(this, Keys.MESSAGES__MESSAGES_USER_ID_FKEY);
    }

    @Override
    public Messages as(String alias) {
        return new Messages(DSL.name(alias), this);
    }

    @Override
    public Messages as(Name alias) {
        return new Messages(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Messages rename(String name) {
        return new Messages(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Messages rename(Name name) {
        return new Messages(name, null);
    }

    // -------------------------------------------------------------------------
    // Row8 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row8<Long, Long, Long, Long, LocalDateTime, LocalDateTime, String, String> fieldsRow() {
        return (Row8) super.fieldsRow();
    }
}
