/*
 * This file is generated by jOOQ.
 */
package de.netbeacon.xenia.jooq.tables.records;


import de.netbeacon.xenia.jooq.tables.Users;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record6;
import org.jooq.Row6;
import org.jooq.impl.UpdatableRecordImpl;

import java.time.LocalDateTime;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class UsersRecord extends UpdatableRecordImpl<UsersRecord> implements Record6<Long, LocalDateTime, String, String, String, String> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.users.user_id</code>.
     */
    public UsersRecord setUserId(Long value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>public.users.user_id</code>.
     */
    public Long getUserId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>public.users.creation_timestamp</code>.
     */
    public UsersRecord setCreationTimestamp(LocalDateTime value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>public.users.creation_timestamp</code>.
     */
    public LocalDateTime getCreationTimestamp() {
        return (LocalDateTime) get(1);
    }

    /**
     * Setter for <code>public.users.internal_role</code>.
     */
    public UsersRecord setInternalRole(String value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>public.users.internal_role</code>.
     */
    public String getInternalRole() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.users.preferred_language</code>.
     */
    public UsersRecord setPreferredLanguage(String value) {
        set(3, value);
        return this;
    }

    /**
     * Getter for <code>public.users.preferred_language</code>.
     */
    public String getPreferredLanguage() {
        return (String) get(3);
    }

    /**
     * Setter for <code>public.users.meta_username</code>.
     */
    public UsersRecord setMetaUsername(String value) {
        set(4, value);
        return this;
    }

    /**
     * Getter for <code>public.users.meta_username</code>.
     */
    public String getMetaUsername() {
        return (String) get(4);
    }

    /**
     * Setter for <code>public.users.meta_iconurl</code>.
     */
    public UsersRecord setMetaIconurl(String value) {
        set(5, value);
        return this;
    }

    /**
     * Getter for <code>public.users.meta_iconurl</code>.
     */
    public String getMetaIconurl() {
        return (String) get(5);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record6 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row6<Long, LocalDateTime, String, String, String, String> fieldsRow() {
        return (Row6) super.fieldsRow();
    }

    @Override
    public Row6<Long, LocalDateTime, String, String, String, String> valuesRow() {
        return (Row6) super.valuesRow();
    }

    @Override
    public Field<Long> field1() {
        return Users.USERS.USER_ID;
    }

    @Override
    public Field<LocalDateTime> field2() {
        return Users.USERS.CREATION_TIMESTAMP;
    }

    @Override
    public Field<String> field3() {
        return Users.USERS.INTERNAL_ROLE;
    }

    @Override
    public Field<String> field4() {
        return Users.USERS.PREFERRED_LANGUAGE;
    }

    @Override
    public Field<String> field5() {
        return Users.USERS.META_USERNAME;
    }

    @Override
    public Field<String> field6() {
        return Users.USERS.META_ICONURL;
    }

    @Override
    public Long component1() {
        return getUserId();
    }

    @Override
    public LocalDateTime component2() {
        return getCreationTimestamp();
    }

    @Override
    public String component3() {
        return getInternalRole();
    }

    @Override
    public String component4() {
        return getPreferredLanguage();
    }

    @Override
    public String component5() {
        return getMetaUsername();
    }

    @Override
    public String component6() {
        return getMetaIconurl();
    }

    @Override
    public Long value1() {
        return getUserId();
    }

    @Override
    public LocalDateTime value2() {
        return getCreationTimestamp();
    }

    @Override
    public String value3() {
        return getInternalRole();
    }

    @Override
    public String value4() {
        return getPreferredLanguage();
    }

    @Override
    public String value5() {
        return getMetaUsername();
    }

    @Override
    public String value6() {
        return getMetaIconurl();
    }

    @Override
    public UsersRecord value1(Long value) {
        setUserId(value);
        return this;
    }

    @Override
    public UsersRecord value2(LocalDateTime value) {
        setCreationTimestamp(value);
        return this;
    }

    @Override
    public UsersRecord value3(String value) {
        setInternalRole(value);
        return this;
    }

    @Override
    public UsersRecord value4(String value) {
        setPreferredLanguage(value);
        return this;
    }

    @Override
    public UsersRecord value5(String value) {
        setMetaUsername(value);
        return this;
    }

    @Override
    public UsersRecord value6(String value) {
        setMetaIconurl(value);
        return this;
    }

    @Override
    public UsersRecord values(Long value1, LocalDateTime value2, String value3, String value4, String value5, String value6) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached UsersRecord
     */
    public UsersRecord() {
        super(Users.USERS);
    }

    /**
     * Create a detached, initialised UsersRecord
     */
    public UsersRecord(Long userId, LocalDateTime creationTimestamp, String internalRole, String preferredLanguage, String metaUsername, String metaIconurl) {
        super(Users.USERS);

        setUserId(userId);
        setCreationTimestamp(creationTimestamp);
        setInternalRole(internalRole);
        setPreferredLanguage(preferredLanguage);
        setMetaUsername(metaUsername);
        setMetaIconurl(metaIconurl);
    }
}
