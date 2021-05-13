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
    public static final Sequence<Long> LICENSES_LICENSE_ID_SEQ = Internal.createSequence("licenses_license_id_seq", Public.PUBLIC, SQLDataType.BIGINT.nullable(false), null, null, null, 2147483647, false, null);

    /**
     * The sequence <code>public.message_attachments_attachment_id_seq</code>
     */
    public static final Sequence<Long> MESSAGE_ATTACHMENTS_ATTACHMENT_ID_SEQ = Internal.createSequence("message_attachments_attachment_id_seq", Public.PUBLIC, SQLDataType.BIGINT.nullable(false), null, null, null, null, false, null);

    /**
     * The sequence <code>public.notification_notification_id_seq</code>
     */
    public static final Sequence<Long> NOTIFICATION_NOTIFICATION_ID_SEQ = Internal.createSequence("notification_notification_id_seq", Public.PUBLIC, SQLDataType.BIGINT.nullable(false), null, null, null, 2147483647, false, null);

    /**
     * The sequence <code>public.oauth_states_state_id_seq</code>
     */
    public static final Sequence<Long> OAUTH_STATES_STATE_ID_SEQ = Internal.createSequence("oauth_states_state_id_seq", Public.PUBLIC, SQLDataType.BIGINT.nullable(false), null, null, null, 2147483647, false, null);

    /**
     * The sequence <code>public.roles_role_id_seq</code>
     */
    public static final Sequence<Long> ROLES_ROLE_ID_SEQ = Internal.createSequence("roles_role_id_seq", Public.PUBLIC, SQLDataType.BIGINT.nullable(false), null, null, null, 2147483647, false, null);

    /**
     * The sequence <code>public.twitchnotifications_twitchnotification_id_seq</code>
     */
    public static final Sequence<Long> TWITCHNOTIFICATIONS_TWITCHNOTIFICATION_ID_SEQ = Internal.createSequence("twitchnotifications_twitchnotification_id_seq", Public.PUBLIC, SQLDataType.BIGINT.nullable(false), null, null, null, 2147483647, false, null);
}
