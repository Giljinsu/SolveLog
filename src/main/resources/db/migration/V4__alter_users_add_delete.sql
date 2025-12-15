-- add users table alter column
ALTER TABLE users ADD is_deleted character varying(1);
ALTER TABLE users ADD deleted_date timestamp(6) without time zone;

update users set is_deleted = 'n' where is_deleted is null;