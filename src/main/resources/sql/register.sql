create or replace procedure authentication_register(
  in p_email varchar(255),
  in p_avatar varchar(64),
  in p_password varchar(50),
  in p_token text,
  inout p_user_id bigint,
  inout p_error_type varchar(32)
)
language plpgsql
as $$
declare
  v_email varchar(255);
  v_avatar varchar(64);
  v_token varchar(512);
begin
  select avatar
  from avatars
  into v_avatar
  where avatar = p_avatar;

  if found then
      p_error_type := 'AvatarUnavailable';
      return;
  end if;

  select user_id
  from users
  into p_user_id
  where email = p_email;

  if found then
    p_error_type := 'EmailUnavailable';
    return;
  end if;

  insert into users(email, password)
  values (p_email, p_password)
  returning user_id
  into p_user_id;

  insert into tokens(user_id, token)
  values (p_user_id, p_token);

  insert into avatars(user_id, avatar)
  values (p_user_id, p_avatar);

  p_error_type := 'NoError';

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

end; $$;