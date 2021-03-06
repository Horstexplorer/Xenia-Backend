/*
 * This file is generated by jOOQ.
 */
package de.netbeacon.xenia.jooq.tables.records;


import de.netbeacon.xenia.jooq.tables.MessageAttachments;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class MessageAttachmentsRecord extends UpdatableRecordImpl<MessageAttachmentsRecord> implements Record3<Long, Long, String> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.message_attachments.attachment_id</code>.
     */
    public MessageAttachmentsRecord setAttachmentId(Long value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>public.message_attachments.attachment_id</code>.
     */
    public Long getAttachmentId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>public.message_attachments.message_id</code>.
     */
    public MessageAttachmentsRecord setMessageId(Long value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>public.message_attachments.message_id</code>.
     */
    public Long getMessageId() {
        return (Long) get(1);
    }

    /**
     * Setter for <code>public.message_attachments.attachment_url</code>.
     */
    public MessageAttachmentsRecord setAttachmentUrl(String value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>public.message_attachments.attachment_url</code>.
     */
    public String getAttachmentUrl() {
        return (String) get(2);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row3<Long, Long, String> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    @Override
    public Row3<Long, Long, String> valuesRow() {
        return (Row3) super.valuesRow();
    }

    @Override
    public Field<Long> field1() {
        return MessageAttachments.MESSAGE_ATTACHMENTS.ATTACHMENT_ID;
    }

    @Override
    public Field<Long> field2() {
        return MessageAttachments.MESSAGE_ATTACHMENTS.MESSAGE_ID;
    }

    @Override
    public Field<String> field3() {
        return MessageAttachments.MESSAGE_ATTACHMENTS.ATTACHMENT_URL;
    }

    @Override
    public Long component1() {
        return getAttachmentId();
    }

    @Override
    public Long component2() {
        return getMessageId();
    }

    @Override
    public String component3() {
        return getAttachmentUrl();
    }

    @Override
    public Long value1() {
        return getAttachmentId();
    }

    @Override
    public Long value2() {
        return getMessageId();
    }

    @Override
    public String value3() {
        return getAttachmentUrl();
    }

    @Override
    public MessageAttachmentsRecord value1(Long value) {
        setAttachmentId(value);
        return this;
    }

    @Override
    public MessageAttachmentsRecord value2(Long value) {
        setMessageId(value);
        return this;
    }

    @Override
    public MessageAttachmentsRecord value3(String value) {
        setAttachmentUrl(value);
        return this;
    }

    @Override
    public MessageAttachmentsRecord values(Long value1, Long value2, String value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached MessageAttachmentsRecord
     */
    public MessageAttachmentsRecord() {
        super(MessageAttachments.MESSAGE_ATTACHMENTS);
    }

    /**
     * Create a detached, initialised MessageAttachmentsRecord
     */
    public MessageAttachmentsRecord(Long attachmentId, Long messageId, String attachmentUrl) {
        super(MessageAttachments.MESSAGE_ATTACHMENTS);

        setAttachmentId(attachmentId);
        setMessageId(messageId);
        setAttachmentUrl(attachmentUrl);
    }
}
