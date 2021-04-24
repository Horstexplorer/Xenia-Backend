/*
 * This file is generated by jOOQ.
 */
package de.netbeacon.xenia.jooq.tables.records;


import de.netbeacon.xenia.jooq.tables.Twitchnotifications;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record7;
import org.jooq.Row7;
import org.jooq.impl.UpdatableRecordImpl;

import java.time.LocalDateTime;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class TwitchnotificationsRecord extends UpdatableRecordImpl<TwitchnotificationsRecord> implements Record7<Long, LocalDateTime, Long, Long, String, Long, String>{

	private static final long serialVersionUID = 1L;

	/**
	 * Setter for <code>public.twitchnotifications.twitchnotification_id</code>.
	 */
	public TwitchnotificationsRecord setTwitchnotificationId(Long value){
		set(0, value);
		return this;
	}

	/**
	 * Getter for <code>public.twitchnotifications.twitchnotification_id</code>.
	 */
	public Long getTwitchnotificationId(){
		return (Long) get(0);
	}

	/**
	 * Setter for <code>public.twitchnotifications.creation_timestamp</code>.
	 */
	public TwitchnotificationsRecord setCreationTimestamp(LocalDateTime value){
		set(1, value);
		return this;
	}

	/**
	 * Getter for <code>public.twitchnotifications.creation_timestamp</code>.
	 */
	public LocalDateTime getCreationTimestamp(){
		return (LocalDateTime) get(1);
	}

	/**
	 * Setter for <code>public.twitchnotifications.guild_id</code>.
	 */
	public TwitchnotificationsRecord setGuildId(Long value){
		set(2, value);
		return this;
	}

	/**
	 * Getter for <code>public.twitchnotifications.guild_id</code>.
	 */
	public Long getGuildId(){
		return (Long) get(2);
	}

	/**
	 * Setter for <code>public.twitchnotifications.channel_id</code>.
	 */
	public TwitchnotificationsRecord setChannelId(Long value){
		set(3, value);
		return this;
	}

	/**
	 * Getter for <code>public.twitchnotifications.channel_id</code>.
	 */
	public Long getChannelId(){
		return (Long) get(3);
	}

	/**
	 * Setter for <code>public.twitchnotifications.twitchnotification_twitch_channel_name</code>.
	 */
	public TwitchnotificationsRecord setTwitchnotificationTwitchChannelName(String value){
		set(4, value);
		return this;
	}

	/**
	 * Getter for <code>public.twitchnotifications.twitchnotification_twitch_channel_name</code>.
	 */
	public String getTwitchnotificationTwitchChannelName(){
		return (String) get(4);
	}

	/**
	 * Setter for <code>public.twitchnotifications.twitchnotification_twitch_channel_id</code>.
	 */
	public TwitchnotificationsRecord setTwitchnotificationTwitchChannelId(Long value){
		set(5, value);
		return this;
	}

	/**
	 * Getter for <code>public.twitchnotifications.twitchnotification_twitch_channel_id</code>.
	 */
	public Long getTwitchnotificationTwitchChannelId(){
		return (Long) get(5);
	}

	/**
	 * Setter for <code>public.twitchnotifications.twitchnotification_custom_message</code>.
	 */
	public TwitchnotificationsRecord setTwitchnotificationCustomMessage(String value){
		set(6, value);
		return this;
	}

	/**
	 * Getter for <code>public.twitchnotifications.twitchnotification_custom_message</code>.
	 */
	public String getTwitchnotificationCustomMessage(){
		return (String) get(6);
	}

	// -------------------------------------------------------------------------
	// Primary key information
	// -------------------------------------------------------------------------

	@Override
	public Record1<Long> key(){
		return (Record1) super.key();
	}

	// -------------------------------------------------------------------------
	// Record7 type implementation
	// -------------------------------------------------------------------------

	@Override
	public Row7<Long, LocalDateTime, Long, Long, String, Long, String> fieldsRow(){
		return (Row7) super.fieldsRow();
	}

	@Override
	public Row7<Long, LocalDateTime, Long, Long, String, Long, String> valuesRow(){
		return (Row7) super.valuesRow();
	}

	@Override
	public Field<Long> field1(){
		return Twitchnotifications.TWITCHNOTIFICATIONS.TWITCHNOTIFICATION_ID;
	}

	@Override
	public Field<LocalDateTime> field2(){
		return Twitchnotifications.TWITCHNOTIFICATIONS.CREATION_TIMESTAMP;
	}

	@Override
	public Field<Long> field3(){
		return Twitchnotifications.TWITCHNOTIFICATIONS.GUILD_ID;
	}

	@Override
	public Field<Long> field4(){
		return Twitchnotifications.TWITCHNOTIFICATIONS.CHANNEL_ID;
	}

	@Override
	public Field<String> field5(){
		return Twitchnotifications.TWITCHNOTIFICATIONS.TWITCHNOTIFICATION_TWITCH_CHANNEL_NAME;
	}

	@Override
	public Field<Long> field6(){
		return Twitchnotifications.TWITCHNOTIFICATIONS.TWITCHNOTIFICATION_TWITCH_CHANNEL_ID;
	}

	@Override
	public Field<String> field7(){
		return Twitchnotifications.TWITCHNOTIFICATIONS.TWITCHNOTIFICATION_CUSTOM_MESSAGE;
	}

	@Override
	public Long component1(){
		return getTwitchnotificationId();
	}

	@Override
	public LocalDateTime component2(){
		return getCreationTimestamp();
	}

	@Override
	public Long component3(){
		return getGuildId();
	}

	@Override
	public Long component4(){
		return getChannelId();
	}

	@Override
	public String component5(){
		return getTwitchnotificationTwitchChannelName();
	}

	@Override
	public Long component6(){
		return getTwitchnotificationTwitchChannelId();
	}

	@Override
	public String component7(){
		return getTwitchnotificationCustomMessage();
	}

	@Override
	public Long value1(){
		return getTwitchnotificationId();
	}

	@Override
	public LocalDateTime value2(){
		return getCreationTimestamp();
	}

	@Override
	public Long value3(){
		return getGuildId();
	}

	@Override
	public Long value4(){
		return getChannelId();
	}

	@Override
	public String value5(){
		return getTwitchnotificationTwitchChannelName();
	}

	@Override
	public Long value6(){
		return getTwitchnotificationTwitchChannelId();
	}

	@Override
	public String value7(){
		return getTwitchnotificationCustomMessage();
	}

	@Override
	public TwitchnotificationsRecord value1(Long value){
		setTwitchnotificationId(value);
		return this;
	}

	@Override
	public TwitchnotificationsRecord value2(LocalDateTime value){
		setCreationTimestamp(value);
		return this;
	}

	@Override
	public TwitchnotificationsRecord value3(Long value){
		setGuildId(value);
		return this;
	}

	@Override
	public TwitchnotificationsRecord value4(Long value){
		setChannelId(value);
		return this;
	}

	@Override
	public TwitchnotificationsRecord value5(String value){
		setTwitchnotificationTwitchChannelName(value);
		return this;
	}

	@Override
	public TwitchnotificationsRecord value6(Long value){
		setTwitchnotificationTwitchChannelId(value);
		return this;
	}

	@Override
	public TwitchnotificationsRecord value7(String value){
		setTwitchnotificationCustomMessage(value);
		return this;
	}

	@Override
	public TwitchnotificationsRecord values(Long value1, LocalDateTime value2, Long value3, Long value4, String value5, Long value6, String value7){
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
	 * Create a detached TwitchnotificationsRecord
	 */
	public TwitchnotificationsRecord(){
		super(Twitchnotifications.TWITCHNOTIFICATIONS);
	}

	/**
	 * Create a detached, initialised TwitchnotificationsRecord
	 */
	public TwitchnotificationsRecord(Long twitchnotificationId, LocalDateTime creationTimestamp, Long guildId, Long channelId, String twitchnotificationTwitchChannelName, Long twitchnotificationTwitchChannelId, String twitchnotificationCustomMessage){
		super(Twitchnotifications.TWITCHNOTIFICATIONS);

		setTwitchnotificationId(twitchnotificationId);
		setCreationTimestamp(creationTimestamp);
		setGuildId(guildId);
		setChannelId(channelId);
		setTwitchnotificationTwitchChannelName(twitchnotificationTwitchChannelName);
		setTwitchnotificationTwitchChannelId(twitchnotificationTwitchChannelId);
		setTwitchnotificationCustomMessage(twitchnotificationCustomMessage);
	}

}
