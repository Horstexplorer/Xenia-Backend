/*
 * This file is generated by jOOQ.
 */
package de.netbeacon.xenia.joop.tables.records;


import de.netbeacon.xenia.joop.tables.LicenseTypes;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record5;
import org.jooq.Row5;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class LicenseTypesRecord extends UpdatableRecordImpl<LicenseTypesRecord> implements Record5<Integer, String, String, Boolean, Integer> {

    private static final long serialVersionUID = -1712806632;

    /**
     * Setter for <code>public.license_types.license_type_id</code>.
     */
    public LicenseTypesRecord setLicenseTypeId(Integer value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>public.license_types.license_type_id</code>.
     */
    public Integer getLicenseTypeId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>public.license_types.license_name</code>.
     */
    public LicenseTypesRecord setLicenseName(String value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>public.license_types.license_name</code>.
     */
    public String getLicenseName() {
        return (String) get(1);
    }

    /**
     * Setter for <code>public.license_types.license_description</code>.
     */
    public LicenseTypesRecord setLicenseDescription(String value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>public.license_types.license_description</code>.
     */
    public String getLicenseDescription() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.license_types.perk_channel_logging_pcb</code>.
     */
    public LicenseTypesRecord setPerkChannelLoggingPcb(Boolean value) {
        set(3, value);
        return this;
    }

    /**
     * Getter for <code>public.license_types.perk_channel_logging_pcb</code>.
     */
    public Boolean getPerkChannelLoggingPcb() {
        return (Boolean) get(3);
    }

    /**
     * Setter for <code>public.license_types.perk_channel_logging_mc</code>.
     */
    public LicenseTypesRecord setPerkChannelLoggingMc(Integer value) {
        set(4, value);
        return this;
    }

    /**
     * Getter for <code>public.license_types.perk_channel_logging_mc</code>.
     */
    public Integer getPerkChannelLoggingMc() {
        return (Integer) get(4);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Integer> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record5 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row5<Integer, String, String, Boolean, Integer> fieldsRow() {
        return (Row5) super.fieldsRow();
    }

    @Override
    public Row5<Integer, String, String, Boolean, Integer> valuesRow() {
        return (Row5) super.valuesRow();
    }

    @Override
    public Field<Integer> field1() {
        return LicenseTypes.LICENSE_TYPES.LICENSE_TYPE_ID;
    }

    @Override
    public Field<String> field2() {
        return LicenseTypes.LICENSE_TYPES.LICENSE_NAME;
    }

    @Override
    public Field<String> field3() {
        return LicenseTypes.LICENSE_TYPES.LICENSE_DESCRIPTION;
    }

    @Override
    public Field<Boolean> field4() {
        return LicenseTypes.LICENSE_TYPES.PERK_CHANNEL_LOGGING_PCB;
    }

    @Override
    public Field<Integer> field5() {
        return LicenseTypes.LICENSE_TYPES.PERK_CHANNEL_LOGGING_MC;
    }

    @Override
    public Integer component1() {
        return getLicenseTypeId();
    }

    @Override
    public String component2() {
        return getLicenseName();
    }

    @Override
    public String component3() {
        return getLicenseDescription();
    }

    @Override
    public Boolean component4() {
        return getPerkChannelLoggingPcb();
    }

    @Override
    public Integer component5() {
        return getPerkChannelLoggingMc();
    }

    @Override
    public Integer value1() {
        return getLicenseTypeId();
    }

    @Override
    public String value2() {
        return getLicenseName();
    }

    @Override
    public String value3() {
        return getLicenseDescription();
    }

    @Override
    public Boolean value4() {
        return getPerkChannelLoggingPcb();
    }

    @Override
    public Integer value5() {
        return getPerkChannelLoggingMc();
    }

    @Override
    public LicenseTypesRecord value1(Integer value) {
        setLicenseTypeId(value);
        return this;
    }

    @Override
    public LicenseTypesRecord value2(String value) {
        setLicenseName(value);
        return this;
    }

    @Override
    public LicenseTypesRecord value3(String value) {
        setLicenseDescription(value);
        return this;
    }

    @Override
    public LicenseTypesRecord value4(Boolean value) {
        setPerkChannelLoggingPcb(value);
        return this;
    }

    @Override
    public LicenseTypesRecord value5(Integer value) {
        setPerkChannelLoggingMc(value);
        return this;
    }

    @Override
    public LicenseTypesRecord values(Integer value1, String value2, String value3, Boolean value4, Integer value5) {
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
     * Create a detached LicenseTypesRecord
     */
    public LicenseTypesRecord() {
        super(LicenseTypes.LICENSE_TYPES);
    }

    /**
     * Create a detached, initialised LicenseTypesRecord
     */
    public LicenseTypesRecord(Integer licenseTypeId, String licenseName, String licenseDescription, Boolean perkChannelLoggingPcb, Integer perkChannelLoggingMc) {
        super(LicenseTypes.LICENSE_TYPES);

        set(0, licenseTypeId);
        set(1, licenseName);
        set(2, licenseDescription);
        set(3, perkChannelLoggingPcb);
        set(4, perkChannelLoggingMc);
    }
}
