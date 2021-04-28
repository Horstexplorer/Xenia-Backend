/*
 * This file is generated by jOOQ.
 */
package de.netbeacon.xenia.jooq.tables;


import de.netbeacon.xenia.jooq.Keys;
import de.netbeacon.xenia.jooq.Public;
import de.netbeacon.xenia.jooq.tables.records.TagsRecord;
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
public class Tags extends TableImpl<TagsRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.tags</code>
     */
    public static final Tags TAGS = new Tags();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<TagsRecord> getRecordType() {
        return TagsRecord.class;
    }

    /**
     * The column <code>public.tags.tag_name</code>.
     */
    public final TableField<TagsRecord, String> TAG_NAME = createField(DSL.name("tag_name"), SQLDataType.VARCHAR(32).nullable(false), this, "");

    /**
     * The column <code>public.tags.creation_timestamp</code>.
     */
    public final TableField<TagsRecord, LocalDateTime> CREATION_TIMESTAMP = createField(DSL.name("creation_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false).defaultValue(DSL.field("now()", SQLDataType.LOCALDATETIME)), this, "");

    /**
     * The column <code>public.tags.guild_id</code>.
     */
    public final TableField<TagsRecord, Long> GUILD_ID = createField(DSL.name("guild_id"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.tags.user_id</code>.
     */
    public final TableField<TagsRecord, Long> USER_ID = createField(DSL.name("user_id"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.tags.tag_content</code>.
     */
    public final TableField<TagsRecord, String> TAG_CONTENT = createField(DSL.name("tag_content"), SQLDataType.CLOB.nullable(false), this, "");

    private Tags(Name alias, Table<TagsRecord> aliased) {
        this(alias, aliased, null);
    }

    private Tags(Name alias, Table<TagsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.tags</code> table reference
     */
    public Tags(String alias) {
        this(DSL.name(alias), TAGS);
    }

    /**
     * Create an aliased <code>public.tags</code> table reference
     */
    public Tags(Name alias) {
        this(alias, TAGS);
    }

    /**
     * Create a <code>public.tags</code> table reference
     */
    public Tags() {
        this(DSL.name("tags"), null);
    }

    public <O extends Record> Tags(Table<O> child, ForeignKey<O, TagsRecord> key) {
        super(child, key, TAGS);
    }

    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public UniqueKey<TagsRecord> getPrimaryKey() {
        return Keys.TAGS_TAG_NAME_GUILD_ID;
    }

    @Override
    public List<UniqueKey<TagsRecord>> getKeys() {
        return Arrays.<UniqueKey<TagsRecord>>asList(Keys.TAGS_TAG_NAME_GUILD_ID);
    }

    @Override
    public List<ForeignKey<TagsRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<TagsRecord, ?>>asList(Keys.TAGS__TAGS_GUILD_ID_USER_ID_FKEY);
    }

    public Members members() {
        return new Members(this, Keys.TAGS__TAGS_GUILD_ID_USER_ID_FKEY);
    }

    @Override
    public Tags as(String alias) {
        return new Tags(DSL.name(alias), this);
    }

    @Override
    public Tags as(Name alias) {
        return new Tags(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Tags rename(String name) {
        return new Tags(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Tags rename(Name name) {
        return new Tags(name, null);
    }

    // -------------------------------------------------------------------------
    // Row5 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row5<String, LocalDateTime, Long, Long, String> fieldsRow() {
        return (Row5) super.fieldsRow();
    }
}
