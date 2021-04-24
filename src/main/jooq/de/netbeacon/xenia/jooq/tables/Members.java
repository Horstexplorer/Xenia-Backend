/*
 * This file is generated by jOOQ.
 */
package de.netbeacon.xenia.jooq.tables;


import de.netbeacon.xenia.jooq.Keys;
import de.netbeacon.xenia.jooq.Public;
import de.netbeacon.xenia.jooq.tables.records.MembersRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class Members extends TableImpl<MembersRecord>{

	private static final long serialVersionUID = 1L;

	/**
	 * The reference instance of <code>public.members</code>
	 */
	public static final Members MEMBERS = new Members();

	/**
	 * The class holding records for this type
	 */
	@Override
	public Class<MembersRecord> getRecordType(){
		return MembersRecord.class;
	}

	/**
	 * The column <code>public.members.guild_id</code>.
	 */
	public final TableField<MembersRecord, Long> GUILD_ID = createField(DSL.name("guild_id"), SQLDataType.BIGINT.nullable(false), this, "");

	/**
	 * The column <code>public.members.user_id</code>.
	 */
	public final TableField<MembersRecord, Long> USER_ID = createField(DSL.name("user_id"), SQLDataType.BIGINT.nullable(false), this, "");

	/**
	 * The column <code>public.members.creation_timestamp</code>.
	 */
	public final TableField<MembersRecord, LocalDateTime> CREATION_TIMESTAMP = createField(DSL.name("creation_timestamp"), SQLDataType.LOCALDATETIME(6).nullable(false).defaultValue(DSL.field("now()", SQLDataType.LOCALDATETIME)), this, "");

	/**
	 * The column <code>public.members.meta_nickname</code>.
	 */
	public final TableField<MembersRecord, String> META_NICKNAME = createField(DSL.name("meta_nickname"), SQLDataType.VARCHAR(32).nullable(false).defaultValue(DSL.field("'unknown_nickname'::character varying", SQLDataType.VARCHAR)), this, "");

	/**
	 * The column <code>public.members.meta_is_owner</code>.
	 */
	public final TableField<MembersRecord, Boolean> META_IS_OWNER = createField(DSL.name("meta_is_owner"), SQLDataType.BOOLEAN.nullable(false).defaultValue(DSL.field("false", SQLDataType.BOOLEAN)), this, "");

	/**
	 * The column <code>public.members.meta_is_administrator</code>.
	 */
	public final TableField<MembersRecord, Boolean> META_IS_ADMINISTRATOR = createField(DSL.name("meta_is_administrator"), SQLDataType.BOOLEAN.nullable(false).defaultValue(DSL.field("false", SQLDataType.BOOLEAN)), this, "");

	private Members(Name alias, Table<MembersRecord> aliased){
		this(alias, aliased, null);
	}

	private Members(Name alias, Table<MembersRecord> aliased, Field<?>[] parameters){
		super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
	}

	/**
	 * Create an aliased <code>public.members</code> table reference
	 */
	public Members(String alias){
		this(DSL.name(alias), MEMBERS);
	}

	/**
	 * Create an aliased <code>public.members</code> table reference
	 */
	public Members(Name alias){
		this(alias, MEMBERS);
	}

	/**
	 * Create a <code>public.members</code> table reference
	 */
	public Members(){
		this(DSL.name("members"), null);
	}

	public <O extends Record> Members(Table<O> child, ForeignKey<O, MembersRecord> key){
		super(child, key, MEMBERS);
	}

	@Override
	public Schema getSchema(){
		return Public.PUBLIC;
	}

	@Override
	public UniqueKey<MembersRecord> getPrimaryKey(){
		return Keys.MEMBERS_GUILD_ID_USER_ID;
	}

	@Override
	public List<UniqueKey<MembersRecord>> getKeys(){
		return Arrays.<UniqueKey<MembersRecord>>asList(Keys.MEMBERS_GUILD_ID_USER_ID);
	}

	@Override
	public List<ForeignKey<MembersRecord, ?>> getReferences(){
		return Arrays.<ForeignKey<MembersRecord, ?>>asList(Keys.MEMBERS__MEMBERS_GUILD_ID_FKEY, Keys.MEMBERS__MEMBERS_USER_ID_FKEY);
	}

	public Guilds guilds(){
		return new Guilds(this, Keys.MEMBERS__MEMBERS_GUILD_ID_FKEY);
	}

	public Users users(){
		return new Users(this, Keys.MEMBERS__MEMBERS_USER_ID_FKEY);
	}

	@Override
	public Members as(String alias){
		return new Members(DSL.name(alias), this);
	}

	@Override
	public Members as(Name alias){
		return new Members(alias, this);
	}

	/**
	 * Rename this table
	 */
	@Override
	public Members rename(String name){
		return new Members(DSL.name(name), null);
	}

	/**
	 * Rename this table
	 */
	@Override
	public Members rename(Name name){
		return new Members(name, null);
	}

	// -------------------------------------------------------------------------
	// Row6 type methods
	// -------------------------------------------------------------------------

	@Override
	public Row6<Long, Long, LocalDateTime, String, Boolean, Boolean> fieldsRow(){
		return (Row6) super.fieldsRow();
	}

}
