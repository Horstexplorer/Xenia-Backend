/*
 * This file is generated by jOOQ.
 */
package de.netbeacon.xenia.jooq.tables;


import de.netbeacon.xenia.jooq.Keys;
import de.netbeacon.xenia.jooq.Public;
import de.netbeacon.xenia.jooq.tables.records.MessageAttachmentsRecord;
import org.jooq.Record;
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
public class MessageAttachments extends TableImpl<MessageAttachmentsRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.message_attachments</code>
     */
    public static final MessageAttachments MESSAGE_ATTACHMENTS = new MessageAttachments();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<MessageAttachmentsRecord> getRecordType() {
        return MessageAttachmentsRecord.class;
    }

    /**
     * The column <code>public.message_attachments.attachment_id</code>.
     */
    public final TableField<MessageAttachmentsRecord, Long> ATTACHMENT_ID = createField(DSL.name("attachment_id"), SQLDataType.BIGINT.nullable(false).identity(true), this, "");

    /**
     * The column <code>public.message_attachments.message_id</code>.
     */
    public final TableField<MessageAttachmentsRecord, Long> MESSAGE_ID = createField(DSL.name("message_id"), SQLDataType.BIGINT, this, "");

    /**
     * The column <code>public.message_attachments.attachment_url</code>.
     */
    public final TableField<MessageAttachmentsRecord, String> ATTACHMENT_URL = createField(DSL.name("attachment_url"), SQLDataType.CLOB, this, "");

    private MessageAttachments(Name alias, Table<MessageAttachmentsRecord> aliased) {
        this(alias, aliased, null);
    }

    private MessageAttachments(Name alias, Table<MessageAttachmentsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.message_attachments</code> table reference
     */
    public MessageAttachments(String alias) {
        this(DSL.name(alias), MESSAGE_ATTACHMENTS);
    }

    /**
     * Create an aliased <code>public.message_attachments</code> table reference
     */
    public MessageAttachments(Name alias) {
        this(alias, MESSAGE_ATTACHMENTS);
    }

    /**
     * Create a <code>public.message_attachments</code> table reference
     */
    public MessageAttachments() {
        this(DSL.name("message_attachments"), null);
    }

    public <O extends Record> MessageAttachments(Table<O> child, ForeignKey<O, MessageAttachmentsRecord> key) {
        super(child, key, MESSAGE_ATTACHMENTS);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Public.PUBLIC;
    }

    @Override
    public Identity<MessageAttachmentsRecord, Long> getIdentity() {
        return (Identity<MessageAttachmentsRecord, Long>) super.getIdentity();
    }

    @Override
    public UniqueKey<MessageAttachmentsRecord> getPrimaryKey() {
        return Keys.MESSAGE_ATTACHMENTS_PK;
    }

    @Override
    public List<ForeignKey<MessageAttachmentsRecord, ?>> getReferences() {
        return Arrays.asList(Keys.MESSAGE_ATTACHMENTS__MESSAGE_ATTACHMENTS_MESSAGES_MESSAGE_ID_FK);
    }

    private transient Messages _messages;

    public Messages messages() {
        if (_messages == null)
            _messages = new Messages(this, Keys.MESSAGE_ATTACHMENTS__MESSAGE_ATTACHMENTS_MESSAGES_MESSAGE_ID_FK);

        return _messages;
    }

    @Override
    public MessageAttachments as(String alias) {
        return new MessageAttachments(DSL.name(alias), this);
    }

    @Override
    public MessageAttachments as(Name alias) {
        return new MessageAttachments(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public MessageAttachments rename(String name) {
        return new MessageAttachments(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public MessageAttachments rename(Name name) {
        return new MessageAttachments(name, null);
    }

    // -------------------------------------------------------------------------
    // Row3 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row3<Long, Long, String> fieldsRow() {
        return (Row3) super.fieldsRow();
    }
}
