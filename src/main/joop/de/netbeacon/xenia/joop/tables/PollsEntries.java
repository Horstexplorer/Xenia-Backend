/*
 * This file is generated by jOOQ.
 */
package de.netbeacon.xenia.joop.tables;


import de.netbeacon.xenia.joop.Keys;
import de.netbeacon.xenia.joop.Public;
import de.netbeacon.xenia.joop.tables.records.PollsEntriesRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import java.util.Arrays;
import java.util.List;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class PollsEntries extends TableImpl<PollsEntriesRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.polls_entries</code>
     */
    public static final PollsEntries POLLS_ENTRIES = new PollsEntries();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<PollsEntriesRecord> getRecordType() {
        return PollsEntriesRecord.class;
    }

    /**
     * The column <code>public.polls_entries.poll_id</code>.
     */
    public final TableField<PollsEntriesRecord, Long> POLL_ID = createField(DSL.name("poll_id"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.polls_entries.poll_option_id</code>.
     */
    public final TableField<PollsEntriesRecord, Integer> POLL_OPTION_ID = createField(DSL.name("poll_option_id"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>public.polls_entries.user_id</code>.
     */
    public final TableField<PollsEntriesRecord, Long> USER_ID = createField(DSL.name("user_id"), SQLDataType.BIGINT.nullable(false), this, "");

    private PollsEntries(Name alias, Table<PollsEntriesRecord> aliased) {
        this(alias, aliased, null);
    }

    private PollsEntries(Name alias, Table<PollsEntriesRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.polls_entries</code> table reference
     */
    public PollsEntries(String alias) {
        this(DSL.name(alias), POLLS_ENTRIES);
    }

    /**
     * Create an aliased <code>public.polls_entries</code> table reference
     */
    public PollsEntries(Name alias) {
        this(alias, POLLS_ENTRIES);
    }

    /**
     * Create a <code>public.polls_entries</code> table reference
     */
    public PollsEntries() {
        this(DSL.name("polls_entries"), null);
    }

    public <O extends Record> PollsEntries(Table<O> child, ForeignKey<O, PollsEntriesRecord> key) {
        super(child, key, POLLS_ENTRIES);
    }

    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public UniqueKey<PollsEntriesRecord> getPrimaryKey() {
        return Keys.POLLS_ENTRIES_POLL_ID_USER_ID;
    }

    @Override
    public List<UniqueKey<PollsEntriesRecord>> getKeys() {
        return Arrays.<UniqueKey<PollsEntriesRecord>>asList(Keys.POLLS_ENTRIES_POLL_ID_USER_ID);
    }

    @Override
    public List<ForeignKey<PollsEntriesRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<PollsEntriesRecord, ?>>asList(Keys.POLLS_ENTRIES__POLLS_ENTRIES_POLL_ID_FKEY, Keys.POLLS_ENTRIES__POLLS_ENTRIES_POLL_ID_POLL_OPTION_ID_FKEY);
    }

    public Polls polls() {
        return new Polls(this, Keys.POLLS_ENTRIES__POLLS_ENTRIES_POLL_ID_FKEY);
    }

    public PollsOptions pollsOptions() {
        return new PollsOptions(this, Keys.POLLS_ENTRIES__POLLS_ENTRIES_POLL_ID_POLL_OPTION_ID_FKEY);
    }

    @Override
    public PollsEntries as(String alias) {
        return new PollsEntries(DSL.name(alias), this);
    }

    @Override
    public PollsEntries as(Name alias) {
        return new PollsEntries(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public PollsEntries rename(String name) {
        return new PollsEntries(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public PollsEntries rename(Name name) {
        return new PollsEntries(name, null);
    }

    // -------------------------------------------------------------------------
    // Row3 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row3<Long, Integer, Long> fieldsRow() {
        return (Row3) super.fieldsRow();
    }
}
