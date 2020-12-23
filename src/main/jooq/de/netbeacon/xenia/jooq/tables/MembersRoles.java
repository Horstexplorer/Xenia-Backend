/*
 * This file is generated by jOOQ.
 */
package de.netbeacon.xenia.jooq.tables;


import de.netbeacon.xenia.jooq.Keys;
import de.netbeacon.xenia.jooq.Public;
import de.netbeacon.xenia.jooq.tables.records.MembersRolesRecord;
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
public class MembersRoles extends TableImpl<MembersRolesRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.members_roles</code>
     */
    public static final MembersRoles MEMBERS_ROLES = new MembersRoles();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<MembersRolesRecord> getRecordType() {
        return MembersRolesRecord.class;
    }

    /**
     * The column <code>public.members_roles.guild_id</code>.
     */
    public final TableField<MembersRolesRecord, Long> GUILD_ID = createField(DSL.name("guild_id"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.members_roles.user_id</code>.
     */
    public final TableField<MembersRolesRecord, Long> USER_ID = createField(DSL.name("user_id"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.members_roles.role_id</code>.
     */
    public final TableField<MembersRolesRecord, Long> ROLE_ID = createField(DSL.name("role_id"), SQLDataType.BIGINT.nullable(false), this, "");

    private MembersRoles(Name alias, Table<MembersRolesRecord> aliased) {
        this(alias, aliased, null);
    }

    private MembersRoles(Name alias, Table<MembersRolesRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.members_roles</code> table reference
     */
    public MembersRoles(String alias) {
        this(DSL.name(alias), MEMBERS_ROLES);
    }

    /**
     * Create an aliased <code>public.members_roles</code> table reference
     */
    public MembersRoles(Name alias) {
        this(alias, MEMBERS_ROLES);
    }

    /**
     * Create a <code>public.members_roles</code> table reference
     */
    public MembersRoles() {
        this(DSL.name("members_roles"), null);
    }

    public <O extends Record> MembersRoles(Table<O> child, ForeignKey<O, MembersRolesRecord> key) {
        super(child, key, MEMBERS_ROLES);
    }

    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public UniqueKey<MembersRolesRecord> getPrimaryKey() {
        return Keys.MEMBERS_ROLES_USER_ID_ROLE_ID;
    }

    @Override
    public List<UniqueKey<MembersRolesRecord>> getKeys() {
        return Arrays.<UniqueKey<MembersRolesRecord>>asList(Keys.MEMBERS_ROLES_USER_ID_ROLE_ID);
    }

    @Override
    public List<ForeignKey<MembersRolesRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<MembersRolesRecord, ?>>asList(Keys.MEMBERS_ROLES__MEMBERS_ROLES_GUILD_ID_FKEY, Keys.MEMBERS_ROLES__MEMBERS_ROLES_GUILD_ID_USER_ID_FKEY, Keys.MEMBERS_ROLES__MEMBERS_ROLES_ROLE_ID_FKEY);
    }

    public Guilds guilds() {
        return new Guilds(this, Keys.MEMBERS_ROLES__MEMBERS_ROLES_GUILD_ID_FKEY);
    }

    public Members members() {
        return new Members(this, Keys.MEMBERS_ROLES__MEMBERS_ROLES_GUILD_ID_USER_ID_FKEY);
    }

    public Vroles vroles() {
        return new Vroles(this, Keys.MEMBERS_ROLES__MEMBERS_ROLES_ROLE_ID_FKEY);
    }

    @Override
    public MembersRoles as(String alias) {
        return new MembersRoles(DSL.name(alias), this);
    }

    @Override
    public MembersRoles as(Name alias) {
        return new MembersRoles(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public MembersRoles rename(String name) {
        return new MembersRoles(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public MembersRoles rename(Name name) {
        return new MembersRoles(name, null);
    }

    // -------------------------------------------------------------------------
    // Row3 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row3<Long, Long, Long> fieldsRow() {
        return (Row3) super.fieldsRow();
    }
}