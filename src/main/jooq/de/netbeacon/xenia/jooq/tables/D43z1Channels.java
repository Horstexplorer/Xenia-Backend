/*
 * This file is generated by jOOQ.
 */
package de.netbeacon.xenia.jooq.tables;


import de.netbeacon.xenia.jooq.Keys;
import de.netbeacon.xenia.jooq.Public;
import de.netbeacon.xenia.jooq.tables.records.D43z1ChannelsRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class D43z1Channels extends TableImpl<D43z1ChannelsRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.d43z1_channels</code>
     */
    public static final D43z1Channels D43Z1_CHANNELS = new D43z1Channels();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<D43z1ChannelsRecord> getRecordType() {
        return D43z1ChannelsRecord.class;
    }

    /**
     * The column <code>public.d43z1_channels.guild_id</code>.
     */
    public final TableField<D43z1ChannelsRecord, Long> GUILD_ID = createField(DSL.name("guild_id"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.d43z1_channels.channel_id</code>.
     */
    public final TableField<D43z1ChannelsRecord, Long> CHANNEL_ID = createField(DSL.name("channel_id"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.d43z1_channels.context_pool_uuid</code>.
     */
    public final TableField<D43z1ChannelsRecord, UUID> CONTEXT_POOL_UUID = createField(DSL.name("context_pool_uuid"), SQLDataType.UUID.nullable(false), this, "");

    /**
     * The column <code>public.d43z1_channels.self_learning</code>.
     */
    public final TableField<D43z1ChannelsRecord, Boolean> SELF_LEARNING = createField(DSL.name("self_learning"), SQLDataType.BOOLEAN.nullable(false).defaultValue(DSL.field("false", SQLDataType.BOOLEAN)), this, "");

    private D43z1Channels(Name alias, Table<D43z1ChannelsRecord> aliased) {
        this(alias, aliased, null);
    }

    private D43z1Channels(Name alias, Table<D43z1ChannelsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.d43z1_channels</code> table reference
     */
    public D43z1Channels(String alias) {
        this(DSL.name(alias), D43Z1_CHANNELS);
    }

    /**
     * Create an aliased <code>public.d43z1_channels</code> table reference
     */
    public D43z1Channels(Name alias) {
        this(alias, D43Z1_CHANNELS);
    }

    /**
     * Create a <code>public.d43z1_channels</code> table reference
     */
    public D43z1Channels() {
        this(DSL.name("d43z1_channels"), null);
    }

    public <O extends Record> D43z1Channels(Table<O> child, ForeignKey<O, D43z1ChannelsRecord> key) {
        super(child, key, D43Z1_CHANNELS);
    }

    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public UniqueKey<D43z1ChannelsRecord> getPrimaryKey() {
        return Keys.D43Z1_CHANNELS_GUILD_ID_CHANNEL_ID;
    }

    @Override
    public List<UniqueKey<D43z1ChannelsRecord>> getKeys() {
        return Arrays.<UniqueKey<D43z1ChannelsRecord>>asList(Keys.D43Z1_CHANNELS_GUILD_ID_CHANNEL_ID);
    }

    @Override
    public List<ForeignKey<D43z1ChannelsRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<D43z1ChannelsRecord, ?>>asList(Keys.D43Z1_CHANNELS__D43Z1_CHANNELS_GUILD_ID_FKEY, Keys.D43Z1_CHANNELS__D43Z1_CHANNELS_CHANNEL_ID_FKEY);
    }

    public Guilds guilds() {
        return new Guilds(this, Keys.D43Z1_CHANNELS__D43Z1_CHANNELS_GUILD_ID_FKEY);
    }

    public Channels channels() {
        return new Channels(this, Keys.D43Z1_CHANNELS__D43Z1_CHANNELS_CHANNEL_ID_FKEY);
    }

    @Override
    public D43z1Channels as(String alias) {
        return new D43z1Channels(DSL.name(alias), this);
    }

    @Override
    public D43z1Channels as(Name alias) {
        return new D43z1Channels(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public D43z1Channels rename(String name) {
        return new D43z1Channels(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public D43z1Channels rename(Name name) {
        return new D43z1Channels(name, null);
    }

    // -------------------------------------------------------------------------
    // Row4 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row4<Long, Long, UUID, Boolean> fieldsRow() {
        return (Row4) super.fieldsRow();
    }
}
