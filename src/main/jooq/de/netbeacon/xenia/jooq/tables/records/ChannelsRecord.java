/*
 * This file is generated by jOOQ.
 */
package de.netbeacon.xenia.jooq.tables.records;


import de.netbeacon.xenia.jooq.tables.Channels;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record8;
import org.jooq.Row8;
import org.jooq.impl.UpdatableRecordImpl;

import java.time.LocalDateTime;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ChannelsRecord extends UpdatableRecordImpl<ChannelsRecord> implements Record8<Long, Long, LocalDateTime, Boolean, String, String, Boolean, Long> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.channels.channel_id</code>.
     */
    public ChannelsRecord setChannelId(Long value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>public.channels.channel_id</code>.
     */
    public Long getChannelId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>public.channels.guild_id</code>.
     */
    public ChannelsRecord setGuildId(Long value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>public.channels.guild_id</code>.
     */
    public Long getGuildId() {
        return (Long) get(1);
    }

    /**
     * Setter for <code>public.channels.creation_timestamp</code>.
     */
    public ChannelsRecord setCreationTimestamp(LocalDateTime value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>public.channels.creation_timestamp</code>.
     */
    public LocalDateTime getCreationTimestamp() {
        return (LocalDateTime) get(2);
    }

    /**
     * Setter for <code>public.channels.access_restriction</code>.
     */
    public ChannelsRecord setAccessRestriction(Boolean value) {
        set(3, value);
        return this;
    }

    /**
     * Getter for <code>public.channels.access_restriction</code>.
     */
    public Boolean getAccessRestriction() {
        return (Boolean) get(3);
    }

    /**
     * Setter for <code>public.channels.channel_type</code>.
     */
    public ChannelsRecord setChannelType(String value) {
        set(4, value);
        return this;
    }

    /**
     * Getter for <code>public.channels.channel_type</code>.
     */
    public String getChannelType() {
        return (String) get(4);
    }

    /**
     * Setter for <code>public.channels.channel_mode</code>.
     */
    public ChannelsRecord setChannelMode(String value) {
        set(5, value);
        return this;
    }

    /**
     * Getter for <code>public.channels.channel_mode</code>.
     */
    public String getChannelMode() {
        return (String) get(5);
    }

    /**
     * Setter for <code>public.channels.tmp_logging_active</code>.
     */
    public ChannelsRecord setTmpLoggingActive(Boolean value) {
        set(6, value);
        return this;
    }

    /**
     * Getter for <code>public.channels.tmp_logging_active</code>.
     */
    public Boolean getTmpLoggingActive() {
        return (Boolean) get(6);
    }

    /**
     * Setter for <code>public.channels.tmp_logging_channel_id</code>.
     */
    public ChannelsRecord setTmpLoggingChannelId(Long value) {
        set(7, value);
        return this;
    }

    /**
     * Getter for <code>public.channels.tmp_logging_channel_id</code>.
     */
    public Long getTmpLoggingChannelId() {
        return (Long) get(7);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record8 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row8<Long, Long, LocalDateTime, Boolean, String, String, Boolean, Long> fieldsRow() {
        return (Row8) super.fieldsRow();
    }

    @Override
    public Row8<Long, Long, LocalDateTime, Boolean, String, String, Boolean, Long> valuesRow() {
        return (Row8) super.valuesRow();
    }

    @Override
    public Field<Long> field1() {
        return Channels.CHANNELS.CHANNEL_ID;
    }

    @Override
    public Field<Long> field2() {
        return Channels.CHANNELS.GUILD_ID;
    }

    @Override
    public Field<LocalDateTime> field3() {
        return Channels.CHANNELS.CREATION_TIMESTAMP;
    }

    @Override
    public Field<Boolean> field4() {
        return Channels.CHANNELS.ACCESS_RESTRICTION;
    }

    @Override
    public Field<String> field5() {
        return Channels.CHANNELS.CHANNEL_TYPE;
    }

    @Override
    public Field<String> field6() {
        return Channels.CHANNELS.CHANNEL_MODE;
    }

    @Override
    public Field<Boolean> field7() {
        return Channels.CHANNELS.TMP_LOGGING_ACTIVE;
    }

    @Override
    public Field<Long> field8() {
        return Channels.CHANNELS.TMP_LOGGING_CHANNEL_ID;
    }

    @Override
    public Long component1() {
        return getChannelId();
    }

    @Override
    public Long component2() {
        return getGuildId();
    }

    @Override
    public LocalDateTime component3() {
        return getCreationTimestamp();
    }

    @Override
    public Boolean component4() {
        return getAccessRestriction();
    }

    @Override
    public String component5() {
        return getChannelType();
    }

    @Override
    public String component6() {
        return getChannelMode();
    }

    @Override
    public Boolean component7() {
        return getTmpLoggingActive();
    }

    @Override
    public Long component8() {
        return getTmpLoggingChannelId();
    }

    @Override
    public Long value1() {
        return getChannelId();
    }

    @Override
    public Long value2() {
        return getGuildId();
    }

    @Override
    public LocalDateTime value3() {
        return getCreationTimestamp();
    }

    @Override
    public Boolean value4() {
        return getAccessRestriction();
    }

    @Override
    public String value5() {
        return getChannelType();
    }

    @Override
    public String value6() {
        return getChannelMode();
    }

    @Override
    public Boolean value7() {
        return getTmpLoggingActive();
    }

    @Override
    public Long value8() {
        return getTmpLoggingChannelId();
    }

    @Override
    public ChannelsRecord value1(Long value) {
        setChannelId(value);
        return this;
    }

    @Override
    public ChannelsRecord value2(Long value) {
        setGuildId(value);
        return this;
    }

    @Override
    public ChannelsRecord value3(LocalDateTime value) {
        setCreationTimestamp(value);
        return this;
    }

    @Override
    public ChannelsRecord value4(Boolean value) {
        setAccessRestriction(value);
        return this;
    }

    @Override
    public ChannelsRecord value5(String value) {
        setChannelType(value);
        return this;
    }

    @Override
    public ChannelsRecord value6(String value) {
        setChannelMode(value);
        return this;
    }

    @Override
    public ChannelsRecord value7(Boolean value) {
        setTmpLoggingActive(value);
        return this;
    }

    @Override
    public ChannelsRecord value8(Long value) {
        setTmpLoggingChannelId(value);
        return this;
    }

    @Override
    public ChannelsRecord values(Long value1, Long value2, LocalDateTime value3, Boolean value4, String value5, String value6, Boolean value7, Long value8) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached ChannelsRecord
     */
    public ChannelsRecord() {
        super(Channels.CHANNELS);
    }

    /**
     * Create a detached, initialised ChannelsRecord
     */
    public ChannelsRecord(Long channelId, Long guildId, LocalDateTime creationTimestamp, Boolean accessRestriction, String channelType, String channelMode, Boolean tmpLoggingActive, Long tmpLoggingChannelId) {
        super(Channels.CHANNELS);

        setChannelId(channelId);
        setGuildId(guildId);
        setCreationTimestamp(creationTimestamp);
        setAccessRestriction(accessRestriction);
        setChannelType(channelType);
        setChannelMode(channelMode);
        setTmpLoggingActive(tmpLoggingActive);
        setTmpLoggingChannelId(tmpLoggingChannelId);
    }
}