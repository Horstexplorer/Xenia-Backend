/*
 * This file is generated by jOOQ.
 */
package de.netbeacon.xenia.jooq.tables.records;


import de.netbeacon.xenia.jooq.tables.Vroles;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record5;
import org.jooq.Row5;
import org.jooq.impl.UpdatableRecordImpl;

import java.time.LocalDateTime;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class VrolesRecord extends UpdatableRecordImpl<VrolesRecord> implements Record5<Long, Long, LocalDateTime, String, Long> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.vroles.vrole_id</code>.
     */
    public VrolesRecord setVroleId(Long value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>public.vroles.vrole_id</code>.
     */
    public Long getVroleId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>public.vroles.guild_id</code>.
     */
    public VrolesRecord setGuildId(Long value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>public.vroles.guild_id</code>.
     */
    public Long getGuildId() {
        return (Long) get(1);
    }

    /**
     * Setter for <code>public.vroles.creation_timestamp</code>.
     */
    public VrolesRecord setCreationTimestamp(LocalDateTime value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>public.vroles.creation_timestamp</code>.
     */
    public LocalDateTime getCreationTimestamp() {
        return (LocalDateTime) get(2);
    }

    /**
     * Setter for <code>public.vroles.vrole_name</code>.
     */
    public VrolesRecord setVroleName(String value) {
        set(3, value);
        return this;
    }

    /**
     * Getter for <code>public.vroles.vrole_name</code>.
     */
    public String getVroleName() {
        return (String) get(3);
    }

    /**
     * Setter for <code>public.vroles.vrole_permission</code>.
     */
    public VrolesRecord setVrolePermission(Long value) {
        set(4, value);
        return this;
    }

    /**
     * Getter for <code>public.vroles.vrole_permission</code>.
     */
    public Long getVrolePermission() {
        return (Long) get(4);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record5 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row5<Long, Long, LocalDateTime, String, Long> fieldsRow() {
        return (Row5) super.fieldsRow();
    }

    @Override
    public Row5<Long, Long, LocalDateTime, String, Long> valuesRow() {
        return (Row5) super.valuesRow();
    }

    @Override
    public Field<Long> field1() {
        return Vroles.VROLES.VROLE_ID;
    }

    @Override
    public Field<Long> field2() {
        return Vroles.VROLES.GUILD_ID;
    }

    @Override
    public Field<LocalDateTime> field3() {
        return Vroles.VROLES.CREATION_TIMESTAMP;
    }

    @Override
    public Field<String> field4() {
        return Vroles.VROLES.VROLE_NAME;
    }

    @Override
    public Field<Long> field5() {
        return Vroles.VROLES.VROLE_PERMISSION;
    }

    @Override
    public Long component1() {
        return getVroleId();
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
    public String component4() {
        return getVroleName();
    }

    @Override
    public Long component5() {
        return getVrolePermission();
    }

    @Override
    public Long value1() {
        return getVroleId();
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
    public String value4() {
        return getVroleName();
    }

    @Override
    public Long value5() {
        return getVrolePermission();
    }

    @Override
    public VrolesRecord value1(Long value) {
        setVroleId(value);
        return this;
    }

    @Override
    public VrolesRecord value2(Long value) {
        setGuildId(value);
        return this;
    }

    @Override
    public VrolesRecord value3(LocalDateTime value) {
        setCreationTimestamp(value);
        return this;
    }

    @Override
    public VrolesRecord value4(String value) {
        setVroleName(value);
        return this;
    }

    @Override
    public VrolesRecord value5(Long value) {
        setVrolePermission(value);
        return this;
    }

    @Override
    public VrolesRecord values(Long value1, Long value2, LocalDateTime value3, String value4, Long value5) {
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
     * Create a detached VrolesRecord
     */
    public VrolesRecord() {
        super(Vroles.VROLES);
    }

    /**
     * Create a detached, initialised VrolesRecord
     */
    public VrolesRecord(Long vroleId, Long guildId, LocalDateTime creationTimestamp, String vroleName, Long vrolePermission) {
        super(Vroles.VROLES);

        setVroleId(vroleId);
        setGuildId(guildId);
        setCreationTimestamp(creationTimestamp);
        setVroleName(vroleName);
        setVrolePermission(vrolePermission);
    }
}
