/*
 * This file is generated by jOOQ.
 */
package de.netbeacon.xenia.joop.tables;


import de.netbeacon.xenia.joop.Keys;
import de.netbeacon.xenia.joop.Public;
import de.netbeacon.xenia.joop.tables.records.GuildsRecord;
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
public class Guilds extends TableImpl<GuildsRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.guilds</code>
     */
    public static final Guilds GUILDS = new Guilds();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<GuildsRecord> getRecordType() {
        return GuildsRecord.class;
    }

    /**
     * The column <code>public.guilds.guild_id</code>.
     */
    public final TableField<GuildsRecord, Long> GUILD_ID = createField(DSL.name("guild_id"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.guilds.creation_timestamp</code>.
     */
    public final TableField<GuildsRecord, LocalDateTime> CREATION_TIMESTAMP = createField(DSL.name("creation_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false).defaultValue(DSL.field("now()", SQLDataType.LOCALDATETIME)), this, "");

    /**
     * The column <code>public.guilds.preferred_language</code>.
     */
    public final TableField<GuildsRecord, String> PREFERRED_LANGUAGE = createField(DSL.name("preferred_language"), SQLDataType.VARCHAR(16).nullable(false).defaultValue(DSL.field("'undefined'::character varying", SQLDataType.VARCHAR)), this, "");

    /**
     * The column <code>public.guilds.license_id</code>.
     */
    public final TableField<GuildsRecord, Integer> LICENSE_ID = createField(DSL.name("license_id"), SQLDataType.INTEGER, this, "");

    private Guilds(Name alias, Table<GuildsRecord> aliased) {
        this(alias, aliased, null);
    }

    private Guilds(Name alias, Table<GuildsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.guilds</code> table reference
     */
    public Guilds(String alias) {
        this(DSL.name(alias), GUILDS);
    }

    /**
     * Create an aliased <code>public.guilds</code> table reference
     */
    public Guilds(Name alias) {
        this(alias, GUILDS);
    }

    /**
     * Create a <code>public.guilds</code> table reference
     */
    public Guilds() {
        this(DSL.name("guilds"), null);
    }

    public <O extends Record> Guilds(Table<O> child, ForeignKey<O, GuildsRecord> key) {
        super(child, key, GUILDS);
    }

    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public UniqueKey<GuildsRecord> getPrimaryKey() {
        return Keys.GUILDS_GUILD_ID;
    }

    @Override
    public List<UniqueKey<GuildsRecord>> getKeys() {
        return Arrays.<UniqueKey<GuildsRecord>>asList(Keys.GUILDS_GUILD_ID, Keys.GUILDS_LICENSE_ID);
    }

    @Override
    public List<ForeignKey<GuildsRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<GuildsRecord, ?>>asList(Keys.GUILDS__GUILDS_LICENSE_ID_FKEY);
    }

    public Licenses licenses() {
        return new Licenses(this, Keys.GUILDS__GUILDS_LICENSE_ID_FKEY);
    }

    @Override
    public Guilds as(String alias) {
        return new Guilds(DSL.name(alias), this);
    }

    @Override
    public Guilds as(Name alias) {
        return new Guilds(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Guilds rename(String name) {
        return new Guilds(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Guilds rename(Name name) {
        return new Guilds(name, null);
    }

    // -------------------------------------------------------------------------
    // Row4 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row4<Long, LocalDateTime, String, Integer> fieldsRow() {
        return (Row4) super.fieldsRow();
    }
}
