/*
 * This file is generated by jOOQ.
 */
package de.netbeacon.xenia.joop.tables;


import de.netbeacon.xenia.joop.Keys;
import de.netbeacon.xenia.joop.Public;
import de.netbeacon.xenia.joop.tables.records.InternalBotShardsRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

import java.util.Arrays;
import java.util.List;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class InternalBotShards extends TableImpl<InternalBotShardsRecord> {

    private static final long serialVersionUID = -591805645;

    /**
     * The reference instance of <code>public.internal_bot_shards</code>
     */
    public static final InternalBotShards INTERNAL_BOT_SHARDS = new InternalBotShards();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<InternalBotShardsRecord> getRecordType() {
        return InternalBotShardsRecord.class;
    }

    /**
     * The column <code>public.internal_bot_shards.shard_id</code>.
     */
    public final TableField<InternalBotShardsRecord, Integer> SHARD_ID = createField(DSL.name("shard_id"), org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>public.internal_bot_shards.client_id</code>.
     */
    public final TableField<InternalBotShardsRecord, Long> CLIENT_ID = createField(DSL.name("client_id"), org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * Create a <code>public.internal_bot_shards</code> table reference
     */
    public InternalBotShards() {
        this(DSL.name("internal_bot_shards"), null);
    }

    /**
     * Create an aliased <code>public.internal_bot_shards</code> table reference
     */
    public InternalBotShards(String alias) {
        this(DSL.name(alias), INTERNAL_BOT_SHARDS);
    }

    /**
     * Create an aliased <code>public.internal_bot_shards</code> table reference
     */
    public InternalBotShards(Name alias) {
        this(alias, INTERNAL_BOT_SHARDS);
    }

    private InternalBotShards(Name alias, Table<InternalBotShardsRecord> aliased) {
        this(alias, aliased, null);
    }

    private InternalBotShards(Name alias, Table<InternalBotShardsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    public <O extends Record> InternalBotShards(Table<O> child, ForeignKey<O, InternalBotShardsRecord> key) {
        super(child, key, INTERNAL_BOT_SHARDS);
    }

    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public UniqueKey<InternalBotShardsRecord> getPrimaryKey() {
        return Keys.INTERNAL_BOT_SHARDS_SHARD_ID;
    }

    @Override
    public List<UniqueKey<InternalBotShardsRecord>> getKeys() {
        return Arrays.<UniqueKey<InternalBotShardsRecord>>asList(Keys.INTERNAL_BOT_SHARDS_SHARD_ID);
    }

    @Override
    public List<ForeignKey<InternalBotShardsRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<InternalBotShardsRecord, ?>>asList(Keys.INTERNAL_BOT_SHARDS__INTERNAL_BOT_SHARDS_CLIENT_ID_FKEY);
    }

    public InternalBotData internalBotData() {
        return new InternalBotData(this, Keys.INTERNAL_BOT_SHARDS__INTERNAL_BOT_SHARDS_CLIENT_ID_FKEY);
    }

    @Override
    public InternalBotShards as(String alias) {
        return new InternalBotShards(DSL.name(alias), this);
    }

    @Override
    public InternalBotShards as(Name alias) {
        return new InternalBotShards(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public InternalBotShards rename(String name) {
        return new InternalBotShards(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public InternalBotShards rename(Name name) {
        return new InternalBotShards(name, null);
    }

    // -------------------------------------------------------------------------
    // Row2 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row2<Integer, Long> fieldsRow() {
        return (Row2) super.fieldsRow();
    }
}
