/*
 * This file is generated by jOOQ.
 */
package de.netbeacon.xenia.jooq.tables.records;


import de.netbeacon.xenia.jooq.tables.Translations;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record4;
import org.jooq.Row4;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class TranslationsRecord extends UpdatableRecordImpl<TranslationsRecord> implements Record4<Integer, String, String, String> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.translations.translation_internal_id</code>.
     */
    public TranslationsRecord setTranslationInternalId(Integer value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>public.translations.translation_internal_id</code>.
     */
    public Integer getTranslationInternalId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>public.translations.translation_language_id</code>.
     */
    public TranslationsRecord setTranslationLanguageId(String value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>public.translations.translation_language_id</code>.
     */
    public String getTranslationLanguageId() {
        return (String) get(1);
    }

    /**
     * Setter for <code>public.translations.translation_key</code>.
     */
    public TranslationsRecord setTranslationKey(String value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>public.translations.translation_key</code>.
     */
    public String getTranslationKey() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.translations.translation</code>.
     */
    public TranslationsRecord setTranslation(String value) {
        set(3, value);
        return this;
    }

    /**
     * Getter for <code>public.translations.translation</code>.
     */
    public String getTranslation() {
        return (String) get(3);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Integer> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record4 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row4<Integer, String, String, String> fieldsRow() {
        return (Row4) super.fieldsRow();
    }

    @Override
    public Row4<Integer, String, String, String> valuesRow() {
        return (Row4) super.valuesRow();
    }

    @Override
    public Field<Integer> field1() {
        return Translations.TRANSLATIONS.TRANSLATION_INTERNAL_ID;
    }

    @Override
    public Field<String> field2() {
        return Translations.TRANSLATIONS.TRANSLATION_LANGUAGE_ID;
    }

    @Override
    public Field<String> field3() {
        return Translations.TRANSLATIONS.TRANSLATION_KEY;
    }

    @Override
    public Field<String> field4() {
        return Translations.TRANSLATIONS.TRANSLATION;
    }

    @Override
    public Integer component1() {
        return getTranslationInternalId();
    }

    @Override
    public String component2() {
        return getTranslationLanguageId();
    }

    @Override
    public String component3() {
        return getTranslationKey();
    }

    @Override
    public String component4() {
        return getTranslation();
    }

    @Override
    public Integer value1() {
        return getTranslationInternalId();
    }

    @Override
    public String value2() {
        return getTranslationLanguageId();
    }

    @Override
    public String value3() {
        return getTranslationKey();
    }

    @Override
    public String value4() {
        return getTranslation();
    }

    @Override
    public TranslationsRecord value1(Integer value) {
        setTranslationInternalId(value);
        return this;
    }

    @Override
    public TranslationsRecord value2(String value) {
        setTranslationLanguageId(value);
        return this;
    }

    @Override
    public TranslationsRecord value3(String value) {
        setTranslationKey(value);
        return this;
    }

    @Override
    public TranslationsRecord value4(String value) {
        setTranslation(value);
        return this;
    }

    @Override
    public TranslationsRecord values(Integer value1, String value2, String value3, String value4) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached TranslationsRecord
     */
    public TranslationsRecord() {
        super(Translations.TRANSLATIONS);
    }

    /**
     * Create a detached, initialised TranslationsRecord
     */
    public TranslationsRecord(Integer translationInternalId, String translationLanguageId, String translationKey, String translation) {
        super(Translations.TRANSLATIONS);

        setTranslationInternalId(translationInternalId);
        setTranslationLanguageId(translationLanguageId);
        setTranslationKey(translationKey);
        setTranslation(translation);
    }
}