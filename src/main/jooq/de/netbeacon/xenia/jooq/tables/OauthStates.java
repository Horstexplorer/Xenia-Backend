/*
 * This file is generated by jOOQ.
 */
package de.netbeacon.xenia.jooq.tables;


import de.netbeacon.xenia.jooq.Public;
import de.netbeacon.xenia.jooq.tables.records.OauthStatesRecord;
import org.jooq.Record;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import java.time.LocalDateTime;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OauthStates extends TableImpl<OauthStatesRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.oauth_states</code>
     */
    public static final OauthStates OAUTH_STATES = new OauthStates();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<OauthStatesRecord> getRecordType() {
        return OauthStatesRecord.class;
    }

    /**
     * The column <code>public.oauth_states.state_id</code>.
     */
    public final TableField<OauthStatesRecord, Integer> STATE_ID = createField(DSL.name("state_id"), SQLDataType.INTEGER.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.oauth_states.creation_timestamp</code>.
     */
    public final TableField<OauthStatesRecord, LocalDateTime> CREATION_TIMESTAMP = createField(DSL.name("creation_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false).defaultValue(DSL.field("now()", SQLDataType.LOCALDATETIME)), this, "");

    /**
     * The column <code>public.oauth_states.state_owner</code>.
     */
    public final TableField<OauthStatesRecord, String> STATE_OWNER = createField(DSL.name("state_owner"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>public.oauth_states.state</code>.
     */
    public final TableField<OauthStatesRecord, String> STATE = createField(DSL.name("state"), SQLDataType.VARCHAR(32).nullable(false), this, "");

    private OauthStates(Name alias, Table<OauthStatesRecord> aliased) {
        this(alias, aliased, null);
    }

    private OauthStates(Name alias, Table<OauthStatesRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.oauth_states</code> table reference
     */
    public OauthStates(String alias) {
        this(DSL.name(alias), OAUTH_STATES);
    }

    /**
     * Create an aliased <code>public.oauth_states</code> table reference
     */
    public OauthStates(Name alias) {
        this(alias, OAUTH_STATES);
    }

    /**
     * Create a <code>public.oauth_states</code> table reference
     */
    public OauthStates() {
        this(DSL.name("oauth_states"), null);
    }

    public <O extends Record> OauthStates(Table<O> child, ForeignKey<O, OauthStatesRecord> key) {
        super(child, key, OAUTH_STATES);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public Identity<OauthStatesRecord, Integer> getIdentity() {
        return (Identity<OauthStatesRecord, Integer>) super.getIdentity();
    }

    @Override
    public OauthStates as(String alias) {
        return new OauthStates(DSL.name(alias), this);
    }

    @Override
    public OauthStates as(Name alias) {
        return new OauthStates(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public OauthStates rename(String name) {
        return new OauthStates(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public OauthStates rename(Name name) {
        return new OauthStates(name, null);
    }

    // -------------------------------------------------------------------------
    // Row4 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row4<Integer, LocalDateTime, String, String> fieldsRow() {
        return (Row4) super.fieldsRow();
    }
}
