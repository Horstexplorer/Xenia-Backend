/*
 * This file is generated by jOOQ.
 */
package de.netbeacon.xenia.jooq.tables.records;


import de.netbeacon.xenia.jooq.tables.Oauth;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record6;
import org.jooq.Row6;
import org.jooq.impl.UpdatableRecordImpl;

import java.time.LocalDateTime;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class OauthRecord extends UpdatableRecordImpl<OauthRecord> implements Record6<Long, String, String, String, LocalDateTime, String> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.oauth.user_id</code>.
     */
    public OauthRecord setUserId(Long value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>public.oauth.user_id</code>.
     */
    public Long getUserId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>public.oauth.local_auth_secret</code>.
     */
    public OauthRecord setLocalAuthSecret(String value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>public.oauth.local_auth_secret</code>.
     */
    public String getLocalAuthSecret() {
        return (String) get(1);
    }

    /**
     * Setter for <code>public.oauth.discord_access_token</code>.
     */
    public OauthRecord setDiscordAccessToken(String value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>public.oauth.discord_access_token</code>.
     */
    public String getDiscordAccessToken() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.oauth.discord_refresh_token</code>.
     */
    public OauthRecord setDiscordRefreshToken(String value) {
        set(3, value);
        return this;
    }

    /**
     * Getter for <code>public.oauth.discord_refresh_token</code>.
     */
    public String getDiscordRefreshToken() {
        return (String) get(3);
    }

    /**
     * Setter for <code>public.oauth.discord_invalidation_time</code>.
     */
    public OauthRecord setDiscordInvalidationTime(LocalDateTime value) {
        set(4, value);
        return this;
    }

    /**
     * Getter for <code>public.oauth.discord_invalidation_time</code>.
     */
    public LocalDateTime getDiscordInvalidationTime() {
        return (LocalDateTime) get(4);
    }

    /**
     * Setter for <code>public.oauth.discord_scopes</code>.
     */
    public OauthRecord setDiscordScopes(String value) {
        set(5, value);
        return this;
    }

    /**
     * Getter for <code>public.oauth.discord_scopes</code>.
     */
    public String getDiscordScopes() {
        return (String) get(5);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record6 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row6<Long, String, String, String, LocalDateTime, String> fieldsRow() {
        return (Row6) super.fieldsRow();
    }

    @Override
    public Row6<Long, String, String, String, LocalDateTime, String> valuesRow() {
        return (Row6) super.valuesRow();
    }

    @Override
    public Field<Long> field1() {
        return Oauth.OAUTH.USER_ID;
    }

    @Override
    public Field<String> field2() {
        return Oauth.OAUTH.LOCAL_AUTH_SECRET;
    }

    @Override
    public Field<String> field3() {
        return Oauth.OAUTH.DISCORD_ACCESS_TOKEN;
    }

    @Override
    public Field<String> field4() {
        return Oauth.OAUTH.DISCORD_REFRESH_TOKEN;
    }

    @Override
    public Field<LocalDateTime> field5() {
        return Oauth.OAUTH.DISCORD_INVALIDATION_TIME;
    }

    @Override
    public Field<String> field6() {
        return Oauth.OAUTH.DISCORD_SCOPES;
    }

    @Override
    public Long component1() {
        return getUserId();
    }

    @Override
    public String component2() {
        return getLocalAuthSecret();
    }

    @Override
    public String component3() {
        return getDiscordAccessToken();
    }

    @Override
    public String component4() {
        return getDiscordRefreshToken();
    }

    @Override
    public LocalDateTime component5() {
        return getDiscordInvalidationTime();
    }

    @Override
    public String component6() {
        return getDiscordScopes();
    }

    @Override
    public Long value1() {
        return getUserId();
    }

    @Override
    public String value2() {
        return getLocalAuthSecret();
    }

    @Override
    public String value3() {
        return getDiscordAccessToken();
    }

    @Override
    public String value4() {
        return getDiscordRefreshToken();
    }

    @Override
    public LocalDateTime value5() {
        return getDiscordInvalidationTime();
    }

    @Override
    public String value6() {
        return getDiscordScopes();
    }

    @Override
    public OauthRecord value1(Long value) {
        setUserId(value);
        return this;
    }

    @Override
    public OauthRecord value2(String value) {
        setLocalAuthSecret(value);
        return this;
    }

    @Override
    public OauthRecord value3(String value) {
        setDiscordAccessToken(value);
        return this;
    }

    @Override
    public OauthRecord value4(String value) {
        setDiscordRefreshToken(value);
        return this;
    }

    @Override
    public OauthRecord value5(LocalDateTime value) {
        setDiscordInvalidationTime(value);
        return this;
    }

    @Override
    public OauthRecord value6(String value) {
        setDiscordScopes(value);
        return this;
    }

    @Override
    public OauthRecord values(Long value1, String value2, String value3, String value4, LocalDateTime value5, String value6) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached OauthRecord
     */
    public OauthRecord() {
        super(Oauth.OAUTH);
    }

    /**
     * Create a detached, initialised OauthRecord
     */
    public OauthRecord(Long userId, String localAuthSecret, String discordAccessToken, String discordRefreshToken, LocalDateTime discordInvalidationTime, String discordScopes) {
        super(Oauth.OAUTH);

        setUserId(userId);
        setLocalAuthSecret(localAuthSecret);
        setDiscordAccessToken(discordAccessToken);
        setDiscordRefreshToken(discordRefreshToken);
        setDiscordInvalidationTime(discordInvalidationTime);
        setDiscordScopes(discordScopes);
    }
}
