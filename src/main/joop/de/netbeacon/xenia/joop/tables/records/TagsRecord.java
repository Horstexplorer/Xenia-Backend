/*
 * This file is generated by jOOQ.
 */
package de.netbeacon.xenia.joop.tables.records;


import de.netbeacon.xenia.joop.tables.Tags;
import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Record5;
import org.jooq.Row5;
import org.jooq.impl.UpdatableRecordImpl;

import java.time.LocalDateTime;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class TagsRecord extends UpdatableRecordImpl<TagsRecord> implements Record5<String, LocalDateTime, Long, Long, String> {

    private static final long serialVersionUID = 1335698735;

    /**
     * Setter for <code>public.tags.tag_name</code>.
     */
    public TagsRecord setTagName(String value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>public.tags.tag_name</code>.
     */
    public String getTagName() {
        return (String) get(0);
    }

    /**
     * Setter for <code>public.tags.creation_timestamp</code>.
     */
    public TagsRecord setCreationTimestamp(LocalDateTime value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>public.tags.creation_timestamp</code>.
     */
    public LocalDateTime getCreationTimestamp() {
        return (LocalDateTime) get(1);
    }

    /**
     * Setter for <code>public.tags.guild_id</code>.
     */
    public TagsRecord setGuildId(Long value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>public.tags.guild_id</code>.
     */
    public Long getGuildId() {
        return (Long) get(2);
    }

    /**
     * Setter for <code>public.tags.user_id</code>.
     */
    public TagsRecord setUserId(Long value) {
        set(3, value);
        return this;
    }

    /**
     * Getter for <code>public.tags.user_id</code>.
     */
    public Long getUserId() {
        return (Long) get(3);
    }

    /**
     * Setter for <code>public.tags.tag_content</code>.
     */
    public TagsRecord setTagContent(String value) {
        set(4, value);
        return this;
    }

    /**
     * Getter for <code>public.tags.tag_content</code>.
     */
    public String getTagContent() {
        return (String) get(4);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record2<String, Long> key() {
        return (Record2) super.key();
    }

    // -------------------------------------------------------------------------
    // Record5 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row5<String, LocalDateTime, Long, Long, String> fieldsRow() {
        return (Row5) super.fieldsRow();
    }

    @Override
    public Row5<String, LocalDateTime, Long, Long, String> valuesRow() {
        return (Row5) super.valuesRow();
    }

    @Override
    public Field<String> field1() {
        return Tags.TAGS.TAG_NAME;
    }

    @Override
    public Field<LocalDateTime> field2() {
        return Tags.TAGS.CREATION_TIMESTAMP;
    }

    @Override
    public Field<Long> field3() {
        return Tags.TAGS.GUILD_ID;
    }

    @Override
    public Field<Long> field4() {
        return Tags.TAGS.USER_ID;
    }

    @Override
    public Field<String> field5() {
        return Tags.TAGS.TAG_CONTENT;
    }

    @Override
    public String component1() {
        return getTagName();
    }

    @Override
    public LocalDateTime component2() {
        return getCreationTimestamp();
    }

    @Override
    public Long component3() {
        return getGuildId();
    }

    @Override
    public Long component4() {
        return getUserId();
    }

    @Override
    public String component5() {
        return getTagContent();
    }

    @Override
    public String value1() {
        return getTagName();
    }

    @Override
    public LocalDateTime value2() {
        return getCreationTimestamp();
    }

    @Override
    public Long value3() {
        return getGuildId();
    }

    @Override
    public Long value4() {
        return getUserId();
    }

    @Override
    public String value5() {
        return getTagContent();
    }

    @Override
    public TagsRecord value1(String value) {
        setTagName(value);
        return this;
    }

    @Override
    public TagsRecord value2(LocalDateTime value) {
        setCreationTimestamp(value);
        return this;
    }

    @Override
    public TagsRecord value3(Long value) {
        setGuildId(value);
        return this;
    }

    @Override
    public TagsRecord value4(Long value) {
        setUserId(value);
        return this;
    }

    @Override
    public TagsRecord value5(String value) {
        setTagContent(value);
        return this;
    }

    @Override
    public TagsRecord values(String value1, LocalDateTime value2, Long value3, Long value4, String value5) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached TagsRecord
     */
    public TagsRecord() {
        super(Tags.TAGS);
    }

    /**
     * Create a detached, initialised TagsRecord
     */
    public TagsRecord(String tagName, LocalDateTime creationTimestamp, Long guildId, Long userId, String tagContent) {
        super(Tags.TAGS);

        set(0, tagName);
        set(1, creationTimestamp);
        set(2, guildId);
        set(3, userId);
        set(4, tagContent);
    }
}