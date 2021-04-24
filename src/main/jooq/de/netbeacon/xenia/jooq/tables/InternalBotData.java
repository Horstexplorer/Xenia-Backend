/*
 * This file is generated by jOOQ.
 */
package de.netbeacon.xenia.jooq.tables;


import de.netbeacon.xenia.jooq.Keys;
import de.netbeacon.xenia.jooq.Public;
import de.netbeacon.xenia.jooq.tables.records.InternalBotDataRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import java.util.Arrays;
import java.util.List;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class InternalBotData extends TableImpl<InternalBotDataRecord>{

	private static final long serialVersionUID = 1L;

	/**
	 * The reference instance of <code>public.internal_bot_data</code>
	 */
	public static final InternalBotData INTERNAL_BOT_DATA = new InternalBotData();

	/**
	 * The class holding records for this type
	 */
	@Override
	public Class<InternalBotDataRecord> getRecordType(){
		return InternalBotDataRecord.class;
	}

	/**
	 * The column <code>public.internal_bot_data.client_id</code>.
	 */
	public final TableField<InternalBotDataRecord, Long> CLIENT_ID = createField(DSL.name("client_id"), SQLDataType.BIGINT.nullable(false), this, "");

	/**
	 * The column <code>public.internal_bot_data.client_name</code>.
	 */
	public final TableField<InternalBotDataRecord, String> CLIENT_NAME = createField(DSL.name("client_name"), SQLDataType.VARCHAR(32).nullable(false).defaultValue(DSL.field("'no_name_specified'::character varying", SQLDataType.VARCHAR)), this, "");

	/**
	 * The column <code>public.internal_bot_data.client_info</code>.
	 */
	public final TableField<InternalBotDataRecord, String> CLIENT_INFO = createField(DSL.name("client_info"), SQLDataType.VARCHAR(255).nullable(false).defaultValue(DSL.field("'no_description_specified'::character varying", SQLDataType.VARCHAR)), this, "");

	/**
	 * The column <code>public.internal_bot_data.discord_token</code>.
	 */
	public final TableField<InternalBotDataRecord, String> DISCORD_TOKEN = createField(DSL.name("discord_token"), SQLDataType.VARCHAR(64).nullable(false).defaultValue(DSL.field("'no_token_specified'::character varying", SQLDataType.VARCHAR)), this, "");

	/**
	 * The column <code>public.internal_bot_data.message_crypt_hash</code>.
	 */
	public final TableField<InternalBotDataRecord, String> MESSAGE_CRYPT_HASH = createField(DSL.name("message_crypt_hash"), SQLDataType.CLOB.nullable(false).defaultValue(DSL.field("''::text", SQLDataType.CLOB)), this, "");

	/**
	 * The column <code>public.internal_bot_data.client_location</code>.
	 */
	public final TableField<InternalBotDataRecord, String> CLIENT_LOCATION = createField(DSL.name("client_location"), SQLDataType.VARCHAR(8).nullable(false).defaultValue(DSL.field("'UNKNOWN'::character varying", SQLDataType.VARCHAR)), this, "");

	private InternalBotData(Name alias, Table<InternalBotDataRecord> aliased){
		this(alias, aliased, null);
	}

	private InternalBotData(Name alias, Table<InternalBotDataRecord> aliased, Field<?>[] parameters){
		super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
	}

	/**
	 * Create an aliased <code>public.internal_bot_data</code> table reference
	 */
	public InternalBotData(String alias){
		this(DSL.name(alias), INTERNAL_BOT_DATA);
	}

	/**
	 * Create an aliased <code>public.internal_bot_data</code> table reference
	 */
	public InternalBotData(Name alias){
		this(alias, INTERNAL_BOT_DATA);
	}

	/**
	 * Create a <code>public.internal_bot_data</code> table reference
	 */
	public InternalBotData(){
		this(DSL.name("internal_bot_data"), null);
	}

	public <O extends Record> InternalBotData(Table<O> child, ForeignKey<O, InternalBotDataRecord> key){
		super(child, key, INTERNAL_BOT_DATA);
	}

	@Override
	public Schema getSchema(){
		return Public.PUBLIC;
	}

	@Override
	public UniqueKey<InternalBotDataRecord> getPrimaryKey(){
		return Keys.INTERNAL_BOT_DATA_CLIENT_ID;
	}

	@Override
	public List<UniqueKey<InternalBotDataRecord>> getKeys(){
		return Arrays.<UniqueKey<InternalBotDataRecord>>asList(Keys.INTERNAL_BOT_DATA_CLIENT_ID);
	}

	@Override
	public InternalBotData as(String alias){
		return new InternalBotData(DSL.name(alias), this);
	}

	@Override
	public InternalBotData as(Name alias){
		return new InternalBotData(alias, this);
	}

	/**
	 * Rename this table
	 */
	@Override
	public InternalBotData rename(String name){
		return new InternalBotData(DSL.name(name), null);
	}

	/**
	 * Rename this table
	 */
	@Override
	public InternalBotData rename(Name name){
		return new InternalBotData(name, null);
	}

	// -------------------------------------------------------------------------
	// Row6 type methods
	// -------------------------------------------------------------------------

	@Override
	public Row6<Long, String, String, String, String, String> fieldsRow(){
		return (Row6) super.fieldsRow();
	}

}
