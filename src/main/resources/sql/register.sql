CREATE OR REPLACE PROCEDURE authentication_register(
    IN p_email VARCHAR(255),
    IN p_password VARCHAR(50),
    in p_session_id varchar(64),
    in p_email_token varchar(64),
    in p_reject_token varchar(64),
    INOUT error_type VARCHAR(32),
    inout p_avatar varchar(32),
    inout p_avatar_id bigint,
    in p_discussion_election bigint
)
LANGUAGE PLPGSQL
AS $$
DECLARE
    t_email VARCHAR(255);
    t_token varchar(512);
BEGIN
--    SELECT email
--    FROM users
--    INTO t_email
--    WHERE users.email = p_email;
--
--    if t_email is null then
--
--      select token
--      from sessions
--      into t_token
--      where sessions.session_id = p_session_id;
--
--      if t_token is not null then
--
--        INSERT INTO users("email", "password", discussion_election)
--        VALUES (p_email, p_password, p_discussion_election)
--        RETURNING user_id
--        INTO p_avatar_id;
--
--        update tokens
--        set user_id = p_avatar_id
--        where token = t_token;
--
--        update sessions
--        set user_id = p_avatar_id
--        where token = t_token and
--            session_id = p_session_id;
--
--        p_avatar := 'u' || p_avatar_id;
--
--        insert into avatars_current("user_id", "avatar")
--        values (p_avatar_id, p_avatar);
--
--        insert into email_tokens("token", "user_id", "type")
--        values (p_email_token, p_avatar_id, 'Accept');
--
--        insert into email_tokens("token", "user_id", "type")
--        values (p_reject_token, p_avatar_id, 'Reject');
--
--        error_type := 'NoError';
--
--      else
--        error_type := 'SessionNotFound';
--      end if;
--
--    else
--        error_type := 'DuplicateEmail';
--    end if;

END; $$;