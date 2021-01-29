/*
 * This file is generated by jOOQ.
 */
package de.netbeacon.xenia.jooq.tables.records;


import de.netbeacon.xenia.jooq.tables.TranslationLanguages;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class TranslationLanguagesRecord extends UpdatableRecordImpl<TranslationLanguagesRecord> implements Record2<String, String> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.translation_languages.language_id</code>.
     */
    public TranslationLanguagesRecord setLanguageId(String value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>public.translation_languages.language_id</code>.
     */
    public String getLanguageId() {
        return (String) get(0);
    }

    /**
     * Setter for <code>public.translation_languages.language_name</code>.
     */
    public TranslationLanguagesRecord setLanguageName(String value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>public.translation_languages.language_name</code>.
     */
    public String getLanguageName() {
        return (String) get(1);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<String> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record2 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row2<String, String> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    @Override
    public Row2<String, String> valuesRow() {
        return (Row2) super.valuesRow();
    }

    @Override
    public Field<String> field1() {
        return TranslationLanguages.TRANSLATION_LANGUAGES.LANGUAGE_ID;
    }

    @Override
    public Field<String> field2() {
        return TranslationLanguages.TRANSLATION_LANGUAGES.LANGUAGE_NAME;
    }

    @Override
    public String component1() {
        return getLanguageId();
    }

    @Override
    public String component2() {
        return getLanguageName();
    }

    @Override
    public String value1() {
        return getLanguageId();
    }

    @Override
    public String value2() {
        return getLanguageName();
    }

    @Override
    public TranslationLanguagesRecord value1(String value) {
        setLanguageId(value);
        return this;
    }

    @Override
    public TranslationLanguagesRecord value2(String value) {
        setLanguageName(value);
        return this;
    }

    @Override
    public TranslationLanguagesRecord values(String value1, String value2) {
        value1(value1);
        value2(value2);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached TranslationLanguagesRecord
     */
    public TranslationLanguagesRecord() {
        super(TranslationLanguages.TRANSLATION_LANGUAGES);
    }

    /**
     * Create a detached, initialised TranslationLanguagesRecord
     */
    public TranslationLanguagesRecord(String languageId, String languageName) {
        super(TranslationLanguages.TRANSLATION_LANGUAGES);

        setLanguageId(languageId);
        setLanguageName(languageName);
    }
}
