/*
 * This file is generated by jOOQ.
 */
package de.netbeacon.xenia.joop.tables;


import de.netbeacon.xenia.joop.Keys;
import de.netbeacon.xenia.joop.Public;
import de.netbeacon.xenia.joop.tables.records.PollsRecord;
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
public class Polls extends TableImpl<PollsRecord> {

    private static final long serialVersionUID = -12136686;

    /**
     * The reference instance of <code>public.polls</code>
     */
    public static final Polls POLLS = new Polls();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<PollsRecord> getRecordType() {
        return PollsRecord.class;
    }

    /**
     * The column <code>public.polls.poll_id</code>.
     */
    public final TableField<PollsRecord, Long> POLL_ID = createField(DSL.name("poll_id"), org.jooq.impl.SQLDataType.BIGINT.nullable(false).defaultValue(org.jooq.impl.DSL.field("nextval('polls_poll_id_seq'::regclass)", org.jooq.impl.SQLDataType.BIGINT)), this, "");

    /**
     * The column <code>public.polls.creation_timestamp</code>.
     */
    public final TableField<PollsRecord, LocalDateTime> CREATION_TIMESTAMP = createField(DSL.name("creation_timestamp"), org.jooq.impl.SQLDataType.LOCALDATETIME.nullable(false).defaultValue(org.jooq.impl.DSL.field("now()", org.jooq.impl.SQLDataType.LOCALDATETIME)), this, "");

    /**
     * The column <code>public.polls.guild_id</code>.
     */
    public final TableField<PollsRecord, Long> GUILD_ID = createField(DSL.name("guild_id"), org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.polls.channel_id</code>.
     */
    public final TableField<PollsRecord, Long> CHANNEL_ID = createField(DSL.name("channel_id"), org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.polls.user_id</code>.
     */
    public final TableField<PollsRecord, Long> USER_ID = createField(DSL.name("user_id"), org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.polls.poll_close_timestamp</code>.
     */
    public final TableField<PollsRecord, LocalDateTime> POLL_CLOSE_TIMESTAMP = createField(DSL.name("poll_close_timestamp"), org.jooq.impl.SQLDataType.LOCALDATETIME, this, "");

    /**
     * The column <code>public.polls.poll_is_active</code>.
     */
    public final TableField<PollsRecord, Boolean> POLL_IS_ACTIVE = createField(DSL.name("poll_is_active"), org.jooq.impl.SQLDataType.BOOLEAN.nullable(false).defaultValue(org.jooq.impl.DSL.field("true", org.jooq.impl.SQLDataType.BOOLEAN)), this, "");

    /**
     * The column <code>public.polls.poll_description</code>.
     */
    public final TableField<PollsRecord, String> POLL_DESCRIPTION = createField(DSL.name("poll_description"), org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false).defaultValue(org.jooq.impl.DSL.field("'no description set'::character varying", org.jooq.impl.SQLDataType.VARCHAR)), this, "");

    /**
     * Create a <code>public.polls</code> table reference
     */
    public Polls() {
        this(DSL.name("polls"), null);
    }

    /**
     * Create an aliased <code>public.polls</code> table reference
     */
    public Polls(String alias) {
        this(DSL.name(alias), POLLS);
    }

    /**
     * Create an aliased <code>public.polls</code> table reference
     */
    public Polls(Name alias) {
        this(alias, POLLS);
    }

    private Polls(Name alias, Table<PollsRecord> aliased) {
        this(alias, aliased, null);
    }

    private Polls(Name alias, Table<PollsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    public <O extends Record> Polls(Table<O> child, ForeignKey<O, PollsRecord> key) {
        super(child, key, POLLS);
    }

    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public Identity<PollsRecord, Long> getIdentity() {
        return Keys.IDENTITY_POLLS;
    }

    @Override
    public UniqueKey<PollsRecord> getPrimaryKey() {
        return Keys.POLLS_POLL_ID;
    }

    @Override
    public List<UniqueKey<PollsRecord>> getKeys() {
        return Arrays.<UniqueKey<PollsRecord>>asList(Keys.POLLS_POLL_ID);
    }

    @Override
    public List<ForeignKey<PollsRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<PollsRecord, ?>>asList(Keys.POLLS__POLLS_GUILD_ID_FKEY, Keys.POLLS__POLLS_GUILD_ID_USER_ID_FKEY, Keys.POLLS__POLLS_CHANNEL_ID_FKEY, Keys.POLLS__POLLS_USER_ID_FKEY);
    }

    public Guilds guilds() {
        return new Guilds(this, Keys.POLLS__POLLS_GUILD_ID_FKEY);
    }

    public Members members() {
        return new Members(this, Keys.POLLS__POLLS_GUILD_ID_USER_ID_FKEY);
    }

    public Channels channels() {
        return new Channels(this, Keys.POLLS__POLLS_CHANNEL_ID_FKEY);
    }

    public Users users() {
        return new Users(this, Keys.POLLS__POLLS_USER_ID_FKEY);
    }

    @Override
    public Polls as(String alias) {
        return new Polls(DSL.name(alias), this);
    }

    @Override
    public Polls as(Name alias) {
        return new Polls(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Polls rename(String name) {
        return new Polls(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Polls rename(Name name) {
        return new Polls(name, null);
    }

    // -------------------------------------------------------------------------
    // Row8 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row8<Long, LocalDateTime, Long, Long, Long, LocalDateTime, Boolean, String> fieldsRow() {
        return (Row8) super.fieldsRow();
    }
}
