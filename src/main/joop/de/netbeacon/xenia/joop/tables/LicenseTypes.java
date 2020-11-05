/*
 * This file is generated by jOOQ.
 */
package de.netbeacon.xenia.joop.tables;


import de.netbeacon.xenia.joop.Keys;
import de.netbeacon.xenia.joop.Public;
import de.netbeacon.xenia.joop.tables.records.LicenseTypesRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

import java.util.Arrays;
import java.util.List;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class LicenseTypes extends TableImpl<LicenseTypesRecord> {

    private static final long serialVersionUID = -480578252;

    /**
     * The reference instance of <code>public.license_types</code>
     */
    public static final LicenseTypes LICENSE_TYPES = new LicenseTypes();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<LicenseTypesRecord> getRecordType() {
        return LicenseTypesRecord.class;
    }

    /**
     * The column <code>public.license_types.license_type_id</code>.
     */
    public final TableField<LicenseTypesRecord, Integer> LICENSE_TYPE_ID = createField(DSL.name("license_type_id"), org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>public.license_types.license_name</code>.
     */
    public final TableField<LicenseTypesRecord, String> LICENSE_NAME = createField(DSL.name("license_name"), org.jooq.impl.SQLDataType.CHAR(32).nullable(false), this, "");

    /**
     * The column <code>public.license_types.license_description</code>.
     */
    public final TableField<LicenseTypesRecord, String> LICENSE_DESCRIPTION = createField(DSL.name("license_description"), org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false).defaultValue(org.jooq.impl.DSL.field("'No Description Set'::character varying", org.jooq.impl.SQLDataType.VARCHAR)), this, "");

    /**
     * The column <code>public.license_types.perk_channel_logging_c</code>.
     */
    public final TableField<LicenseTypesRecord, Integer> PERK_CHANNEL_LOGGING_C = createField(DSL.name("perk_channel_logging_c"), org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>public.license_types.perk_guild_roles_c</code>.
     */
    public final TableField<LicenseTypesRecord, Integer> PERK_GUILD_ROLES_C = createField(DSL.name("perk_guild_roles_c"), org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>public.license_types.perk_misc_tags_c</code>.
     */
    public final TableField<LicenseTypesRecord, Integer> PERK_MISC_TAGS_C = createField(DSL.name("perk_misc_tags_c"), org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>public.license_types.perk_misc_notifications_c</code>.
     */
    public final TableField<LicenseTypesRecord, Integer> PERK_MISC_NOTIFICATIONS_C = createField(DSL.name("perk_misc_notifications_c"), org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>public.license_types.perk_misc_polls_c</code>.
     */
    public final TableField<LicenseTypesRecord, Integer> PERK_MISC_POLLS_C = createField(DSL.name("perk_misc_polls_c"), org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * Create a <code>public.license_types</code> table reference
     */
    public LicenseTypes() {
        this(DSL.name("license_types"), null);
    }

    /**
     * Create an aliased <code>public.license_types</code> table reference
     */
    public LicenseTypes(String alias) {
        this(DSL.name(alias), LICENSE_TYPES);
    }

    /**
     * Create an aliased <code>public.license_types</code> table reference
     */
    public LicenseTypes(Name alias) {
        this(alias, LICENSE_TYPES);
    }

    private LicenseTypes(Name alias, Table<LicenseTypesRecord> aliased) {
        this(alias, aliased, null);
    }

    private LicenseTypes(Name alias, Table<LicenseTypesRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    public <O extends Record> LicenseTypes(Table<O> child, ForeignKey<O, LicenseTypesRecord> key) {
        super(child, key, LICENSE_TYPES);
    }

    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public UniqueKey<LicenseTypesRecord> getPrimaryKey() {
        return Keys.LICENSE_TYPES_LICENSE_TYPE_ID;
    }

    @Override
    public List<UniqueKey<LicenseTypesRecord>> getKeys() {
        return Arrays.<UniqueKey<LicenseTypesRecord>>asList(Keys.LICENSE_TYPES_LICENSE_TYPE_ID);
    }

    @Override
    public LicenseTypes as(String alias) {
        return new LicenseTypes(DSL.name(alias), this);
    }

    @Override
    public LicenseTypes as(Name alias) {
        return new LicenseTypes(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public LicenseTypes rename(String name) {
        return new LicenseTypes(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public LicenseTypes rename(Name name) {
        return new LicenseTypes(name, null);
    }

    // -------------------------------------------------------------------------
    // Row8 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row8<Integer, String, String, Integer, Integer, Integer, Integer, Integer> fieldsRow() {
        return (Row8) super.fieldsRow();
    }
}
