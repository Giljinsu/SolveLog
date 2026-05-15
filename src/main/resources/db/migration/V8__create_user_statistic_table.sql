CREATE TABLE public.user_statistic (
    user_statistic_id bigint NOT NULL,
    user_id bigint NOT NULL,
    statistic_type character varying(255) not null,
    statistic_date timestamp(6) without time zone,
    category_name character varying(255),
    tag_name character varying(255),
    count bigint
);

ALTER TABLE public.user_statistic OWNER TO solvelog_admin;

CREATE SEQUENCE public.user_statistic_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.user_statistic_seq OWNER TO solvelog_admin;

ALTER TABLE ONLY public.user_statistic
    ADD CONSTRAINT user_statistic_pkey PRIMARY KEY (user_statistic_id);

ALTER TABLE ONLY public.user_statistic
    ADD CONSTRAINT fk_user_statistic_user FOREIGN KEY (user_id) REFERENCES public.users(user_id);