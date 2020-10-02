/*
 * This file is generated by jOOQ.
 */
package de.netbeacon.xenia.joop.tables.records;


import de.netbeacon.xenia.joop.tables.InternalBotData;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record4;
import org.jooq.Row4;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class InternalBotDataRecord extends UpdatableRecordImpl<InternalBotDataRecord> implements Record4<Long, String, String, String> {

    private static final long serialVersionUID = 296889560;

    /**
     * Setter for <code>public.internal_bot_data.client_id</code>.
     */
    public InternalBotDataRecord setClientId(Long value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>public.internal_bot_data.client_id</code>.
     */
    public Long getClientId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>public.internal_bot_data.client_name</code>.
     */
    public InternalBotDataRecord setClientName(String value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>public.internal_bot_data.client_name</code>.
     */
    public String getClientName() {
        return (String) get(1);
    }

    /**
     * Setter for <code>public.internal_bot_data.client_info</code>.
     */
    public InternalBotDataRecord setClientInfo(String value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>public.internal_bot_data.client_info</code>.
     */
    public String getClientInfo() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.internal_bot_data.discord_token</code>.
     */
    public InternalBotDataRecord setDiscordToken(String value) {
        set(3, value);
        return this;
    }

    /**
     * Getter for <code>public.internal_bot_data.discord_token</code>.
     */
    public String getDiscordToken() {
        return (String) get(3);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record4 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row4<Long, String, String, String> fieldsRow() {
        return (Row4) super.fieldsRow();
    }

    @Override
    public Row4<Long, String, String, String> valuesRow() {
        return (Row4) super.valuesRow();
    }

    @Override
    public Field<Long> field1() {
        return InternalBotData.INTERNAL_BOT_DATA.CLIENT_ID;
    }

    @Override
    public Field<String> field2() {
        return InternalBotData.INTERNAL_BOT_DATA.CLIENT_NAME;
    }

    @Override
    public Field<String> field3() {
        return InternalBotData.INTERNAL_BOT_DATA.CLIENT_INFO;
    }

    @Override
    public Field<String> field4() {
        return InternalBotData.INTERNAL_BOT_DATA.DISCORD_TOKEN;
    }

    @Override
    public Long component1() {
        return getClientId();
    }

    @Override
    public String component2() {
        return getClientName();
    }

    @Override
    public String component3() {
        return getClientInfo();
    }

    @Override
    public String component4() {
        return getDiscordToken();
    }

    @Override
    public Long value1() {
        return getClientId();
    }

    @Override
    public String value2() {
        return getClientName();
    }

    @Override
    public String value3() {
        return getClientInfo();
    }

    @Override
    public String value4() {
        return getDiscordToken();
    }

    @Override
    public InternalBotDataRecord value1(Long value) {
        setClientId(value);
        return this;
    }

    @Override
    public InternalBotDataRecord value2(String value) {
        setClientName(value);
        return this;
    }

    @Override
    public InternalBotDataRecord value3(String value) {
        setClientInfo(value);
        return this;
    }

    @Override
    public InternalBotDataRecord value4(String value) {
        setDiscordToken(value);
        return this;
    }

    @Override
    public InternalBotDataRecord values(Long value1, String value2, String value3, String value4) {
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
     * Create a detached InternalBotDataRecord
     */
    public InternalBotDataRecord() {
        super(InternalBotData.INTERNAL_BOT_DATA);
    }

    /**
     * Create a detached, initialised InternalBotDataRecord
     */
    public InternalBotDataRecord(Long clientId, String clientName, String clientInfo, String discordToken) {
        super(InternalBotData.INTERNAL_BOT_DATA);

        set(0, clientId);
        set(1, clientName);
        set(2, clientInfo);
        set(3, discordToken);
    }
}
