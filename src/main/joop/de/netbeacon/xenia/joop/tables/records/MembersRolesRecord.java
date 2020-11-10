/*
 * This file is generated by jOOQ.
 */
package de.netbeacon.xenia.joop.tables.records;


import de.netbeacon.xenia.joop.tables.MembersRoles;
import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class MembersRolesRecord extends UpdatableRecordImpl<MembersRolesRecord> implements Record3<Long, Long, Long> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.members_roles.guild_id</code>.
     */
    public MembersRolesRecord setGuildId(Long value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>public.members_roles.guild_id</code>.
     */
    public Long getGuildId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>public.members_roles.user_id</code>.
     */
    public MembersRolesRecord setUserId(Long value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>public.members_roles.user_id</code>.
     */
    public Long getUserId() {
        return (Long) get(1);
    }

    /**
     * Setter for <code>public.members_roles.role_id</code>.
     */
    public MembersRolesRecord setRoleId(Long value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>public.members_roles.role_id</code>.
     */
    public Long getRoleId() {
        return (Long) get(2);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record2<Long, Long> key() {
        return (Record2) super.key();
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row3<Long, Long, Long> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    @Override
    public Row3<Long, Long, Long> valuesRow() {
        return (Row3) super.valuesRow();
    }

    @Override
    public Field<Long> field1() {
        return MembersRoles.MEMBERS_ROLES.GUILD_ID;
    }

    @Override
    public Field<Long> field2() {
        return MembersRoles.MEMBERS_ROLES.USER_ID;
    }

    @Override
    public Field<Long> field3() {
        return MembersRoles.MEMBERS_ROLES.ROLE_ID;
    }

    @Override
    public Long component1() {
        return getGuildId();
    }

    @Override
    public Long component2() {
        return getUserId();
    }

    @Override
    public Long component3() {
        return getRoleId();
    }

    @Override
    public Long value1() {
        return getGuildId();
    }

    @Override
    public Long value2() {
        return getUserId();
    }

    @Override
    public Long value3() {
        return getRoleId();
    }

    @Override
    public MembersRolesRecord value1(Long value) {
        setGuildId(value);
        return this;
    }

    @Override
    public MembersRolesRecord value2(Long value) {
        setUserId(value);
        return this;
    }

    @Override
    public MembersRolesRecord value3(Long value) {
        setRoleId(value);
        return this;
    }

    @Override
    public MembersRolesRecord values(Long value1, Long value2, Long value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached MembersRolesRecord
     */
    public MembersRolesRecord() {
        super(MembersRoles.MEMBERS_ROLES);
    }

    /**
     * Create a detached, initialised MembersRolesRecord
     */
    public MembersRolesRecord(Long guildId, Long userId, Long roleId) {
        super(MembersRoles.MEMBERS_ROLES);

        setGuildId(guildId);
        setUserId(userId);
        setRoleId(roleId);
    }
}
