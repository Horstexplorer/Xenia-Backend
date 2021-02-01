/*
 * This file is generated by jOOQ.
 */
package de.netbeacon.xenia.jooq.tables.records;


import de.netbeacon.xenia.jooq.tables.Notifications;

import java.time.LocalDateTime;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record7;
import org.jooq.Row7;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class NotificationsRecord extends UpdatableRecordImpl<NotificationsRecord> implements Record7<Long, LocalDateTime, Long, Long, Long, LocalDateTime, String> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.notifications.notification_id</code>.
     */
    public NotificationsRecord setNotificationId(Long value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>public.notifications.notification_id</code>.
     */
    public Long getNotificationId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>public.notifications.creation_timestamp</code>.
     */
    public NotificationsRecord setCreationTimestamp(LocalDateTime value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>public.notifications.creation_timestamp</code>.
     */
    public LocalDateTime getCreationTimestamp() {
        return (LocalDateTime) get(1);
    }

    /**
     * Setter for <code>public.notifications.guild_id</code>.
     */
    public NotificationsRecord setGuildId(Long value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>public.notifications.guild_id</code>.
     */
    public Long getGuildId() {
        return (Long) get(2);
    }

    /**
     * Setter for <code>public.notifications.channel_id</code>.
     */
    public NotificationsRecord setChannelId(Long value) {
        set(3, value);
        return this;
    }

    /**
     * Getter for <code>public.notifications.channel_id</code>.
     */
    public Long getChannelId() {
        return (Long) get(3);
    }

    /**
     * Setter for <code>public.notifications.user_id</code>.
     */
    public NotificationsRecord setUserId(Long value) {
        set(4, value);
        return this;
    }

    /**
     * Getter for <code>public.notifications.user_id</code>.
     */
    public Long getUserId() {
        return (Long) get(4);
    }

    /**
     * Setter for <code>public.notifications.notification_target</code>.
     */
    public NotificationsRecord setNotificationTarget(LocalDateTime value) {
        set(5, value);
        return this;
    }

    /**
     * Getter for <code>public.notifications.notification_target</code>.
     */
    public LocalDateTime getNotificationTarget() {
        return (LocalDateTime) get(5);
    }

    /**
     * Setter for <code>public.notifications.notification_message</code>.
     */
    public NotificationsRecord setNotificationMessage(String value) {
        set(6, value);
        return this;
    }

    /**
     * Getter for <code>public.notifications.notification_message</code>.
     */
    public String getNotificationMessage() {
        return (String) get(6);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record7 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row7<Long, LocalDateTime, Long, Long, Long, LocalDateTime, String> fieldsRow() {
        return (Row7) super.fieldsRow();
    }

    @Override
    public Row7<Long, LocalDateTime, Long, Long, Long, LocalDateTime, String> valuesRow() {
        return (Row7) super.valuesRow();
    }

    @Override
    public Field<Long> field1() {
        return Notifications.NOTIFICATIONS.NOTIFICATION_ID;
    }

    @Override
    public Field<LocalDateTime> field2() {
        return Notifications.NOTIFICATIONS.CREATION_TIMESTAMP;
    }

    @Override
    public Field<Long> field3() {
        return Notifications.NOTIFICATIONS.GUILD_ID;
    }

    @Override
    public Field<Long> field4() {
        return Notifications.NOTIFICATIONS.CHANNEL_ID;
    }

    @Override
    public Field<Long> field5() {
        return Notifications.NOTIFICATIONS.USER_ID;
    }

    @Override
    public Field<LocalDateTime> field6() {
        return Notifications.NOTIFICATIONS.NOTIFICATION_TARGET;
    }

    @Override
    public Field<String> field7() {
        return Notifications.NOTIFICATIONS.NOTIFICATION_MESSAGE;
    }

    @Override
    public Long component1() {
        return getNotificationId();
    }

    @Override
    public LocalDateTime component2() {
        return getCreationTimestamp();
    }

    @Override
    public Long component3() {
        return getGuildId();
    }

    @Override
    public Long component4() {
        return getChannelId();
    }

    @Override
    public Long component5() {
        return getUserId();
    }

    @Override
    public LocalDateTime component6() {
        return getNotificationTarget();
    }

    @Override
    public String component7() {
        return getNotificationMessage();
    }

    @Override
    public Long value1() {
        return getNotificationId();
    }

    @Override
    public LocalDateTime value2() {
        return getCreationTimestamp();
    }

    @Override
    public Long value3() {
        return getGuildId();
    }

    @Override
    public Long value4() {
        return getChannelId();
    }

    @Override
    public Long value5() {
        return getUserId();
    }

    @Override
    public LocalDateTime value6() {
        return getNotificationTarget();
    }

    @Override
    public String value7() {
        return getNotificationMessage();
    }

    @Override
    public NotificationsRecord value1(Long value) {
        setNotificationId(value);
        return this;
    }

    @Override
    public NotificationsRecord value2(LocalDateTime value) {
        setCreationTimestamp(value);
        return this;
    }

    @Override
    public NotificationsRecord value3(Long value) {
        setGuildId(value);
        return this;
    }

    @Override
    public NotificationsRecord value4(Long value) {
        setChannelId(value);
        return this;
    }

    @Override
    public NotificationsRecord value5(Long value) {
        setUserId(value);
        return this;
    }

    @Override
    public NotificationsRecord value6(LocalDateTime value) {
        setNotificationTarget(value);
        return this;
    }

    @Override
    public NotificationsRecord value7(String value) {
        setNotificationMessage(value);
        return this;
    }

    @Override
    public NotificationsRecord values(Long value1, LocalDateTime value2, Long value3, Long value4, Long value5, LocalDateTime value6, String value7) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached NotificationsRecord
     */
    public NotificationsRecord() {
        super(Notifications.NOTIFICATIONS);
    }

    /**
     * Create a detached, initialised NotificationsRecord
     */
    public NotificationsRecord(Long notificationId, LocalDateTime creationTimestamp, Long guildId, Long channelId, Long userId, LocalDateTime notificationTarget, String notificationMessage) {
        super(Notifications.NOTIFICATIONS);

        setNotificationId(notificationId);
        setCreationTimestamp(creationTimestamp);
        setGuildId(guildId);
        setChannelId(channelId);
        setUserId(userId);
        setNotificationTarget(notificationTarget);
        setNotificationMessage(notificationMessage);
    }
}
