/*
 * This file is generated by jOOQ.
 */
package de.netbeacon.xenia.jooq.tables.records;


import de.netbeacon.xenia.jooq.tables.OauthStates;
import org.jooq.Field;
import org.jooq.Record4;
import org.jooq.Row4;
import org.jooq.impl.TableRecordImpl;

import java.time.LocalDateTime;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OauthStatesRecord extends TableRecordImpl<OauthStatesRecord> implements Record4<Integer, LocalDateTime, String, String> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.oauth_states.state_id</code>.
     */
    public OauthStatesRecord setStateId(Integer value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>public.oauth_states.state_id</code>.
     */
    public Integer getStateId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>public.oauth_states.creation_timestamp</code>.
     */
    public OauthStatesRecord setCreationTimestamp(LocalDateTime value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>public.oauth_states.creation_timestamp</code>.
     */
    public LocalDateTime getCreationTimestamp() {
        return (LocalDateTime) get(1);
    }

    /**
     * Setter for <code>public.oauth_states.state_owner</code>.
     */
    public OauthStatesRecord setStateOwner(String value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>public.oauth_states.state_owner</code>.
     */
    public String getStateOwner() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.oauth_states.state</code>.
     */
    public OauthStatesRecord setState(String value) {
        set(3, value);
        return this;
    }

    /**
     * Getter for <code>public.oauth_states.state</code>.
     */
    public String getState() {
        return (String) get(3);
    }

    // -------------------------------------------------------------------------
    // Record4 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row4<Integer, LocalDateTime, String, String> fieldsRow() {
        return (Row4) super.fieldsRow();
    }

    @Override
    public Row4<Integer, LocalDateTime, String, String> valuesRow() {
        return (Row4) super.valuesRow();
    }

    @Override
    public Field<Integer> field1() {
        return OauthStates.OAUTH_STATES.STATE_ID;
    }

    @Override
    public Field<LocalDateTime> field2() {
        return OauthStates.OAUTH_STATES.CREATION_TIMESTAMP;
    }

    @Override
    public Field<String> field3() {
        return OauthStates.OAUTH_STATES.STATE_OWNER;
    }

    @Override
    public Field<String> field4() {
        return OauthStates.OAUTH_STATES.STATE;
    }

    @Override
    public Integer component1() {
        return getStateId();
    }

    @Override
    public LocalDateTime component2() {
        return getCreationTimestamp();
    }

    @Override
    public String component3() {
        return getStateOwner();
    }

    @Override
    public String component4() {
        return getState();
    }

    @Override
    public Integer value1() {
        return getStateId();
    }

    @Override
    public LocalDateTime value2() {
        return getCreationTimestamp();
    }

    @Override
    public String value3() {
        return getStateOwner();
    }

    @Override
    public String value4() {
        return getState();
    }

    @Override
    public OauthStatesRecord value1(Integer value) {
        setStateId(value);
        return this;
    }

    @Override
    public OauthStatesRecord value2(LocalDateTime value) {
        setCreationTimestamp(value);
        return this;
    }

    @Override
    public OauthStatesRecord value3(String value) {
        setStateOwner(value);
        return this;
    }

    @Override
    public OauthStatesRecord value4(String value) {
        setState(value);
        return this;
    }

    @Override
    public OauthStatesRecord values(Integer value1, LocalDateTime value2, String value3, String value4) {
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
     * Create a detached OauthStatesRecord
     */
    public OauthStatesRecord() {
        super(OauthStates.OAUTH_STATES);
    }

    /**
     * Create a detached, initialised OauthStatesRecord
     */
    public OauthStatesRecord(Integer stateId, LocalDateTime creationTimestamp, String stateOwner, String state) {
        super(OauthStates.OAUTH_STATES);

        setStateId(stateId);
        setCreationTimestamp(creationTimestamp);
        setStateOwner(stateOwner);
        setState(state);
    }
}
