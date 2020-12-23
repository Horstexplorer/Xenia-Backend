/*
 * This file is generated by jOOQ.
 */
package de.netbeacon.xenia.jooq;


import org.jooq.Sequence;
import org.jooq.impl.Internal;
import org.jooq.impl.SQLDataType;


/**
 * Convenience access to all sequences in public.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Sequences {

    /**
     * The sequence <code>public.licenses_license_id_seq</code>
     */
    public static final Sequence<Integer> LICENSES_LICENSE_ID_SEQ = Internal.createSequence("licenses_license_id_seq", Public.PUBLIC, SQLDataType.INTEGER.nullable(false), null, null, null, null, false, null);

    /**
     * The sequence <code>public.notification_notification_id_seq</code>
     */
    public static final Sequence<Integer> NOTIFICATION_NOTIFICATION_ID_SEQ = Internal.createSequence("notification_notification_id_seq", Public.PUBLIC, SQLDataType.INTEGER.nullable(false), null, null, null, null, false, null);

    /**
     * The sequence <code>public.oauth_states_state_id_seq</code>
     */
    public static final Sequence<Integer> OAUTH_STATES_STATE_ID_SEQ = Internal.createSequence("oauth_states_state_id_seq", Public.PUBLIC, SQLDataType.INTEGER.nullable(false), null, null, null, null, false, null);

    /**
     * The sequence <code>public.roles_role_id_seq</code>
     */
    public static final Sequence<Integer> ROLES_ROLE_ID_SEQ = Internal.createSequence("roles_role_id_seq", Public.PUBLIC, SQLDataType.INTEGER.nullable(false), null, null, null, null, false, null);
}