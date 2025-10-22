--
-- PostgreSQL database dump
--

--\restrict n46PHs0UZBgPwG2XDRmEC6GUjsM7bLkbdxRE2zHTNJG0h67Rd0ktAhJECyapyUa

-- Dumped from database version 16.10 (Homebrew)
-- Dumped by pg_dump version 16.10 (Homebrew)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: alarm; Type: TABLE; Schema: public; Owner: solvelog_admin
--

CREATE TABLE public.alarm (
    is_viewed boolean,
    alarm_id bigint NOT NULL,
    alarm_type_id bigint,
    user_id bigint,
    metadata character varying(255)
);


ALTER TABLE public.alarm OWNER TO solvelog_admin;

--
-- Name: alarm_seq; Type: SEQUENCE; Schema: public; Owner: solvelog_admin
--

CREATE SEQUENCE public.alarm_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alarm_seq OWNER TO solvelog_admin;

--
-- Name: alarm_type; Type: TABLE; Schema: public; Owner: solvelog_admin
--

CREATE TABLE public.alarm_type (
    alarm_type_id bigint NOT NULL,
    template character varying(255),
    type character varying(255),
    CONSTRAINT alarm_type_type_check CHECK (((type)::text = ANY ((ARRAY['COMMENT'::character varying, 'REPLY'::character varying, 'LIKE'::character varying])::text[])))
);


ALTER TABLE public.alarm_type OWNER TO solvelog_admin;

--
-- Name: alarm_type_seq; Type: SEQUENCE; Schema: public; Owner: solvelog_admin
--

CREATE SEQUENCE public.alarm_type_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alarm_type_seq OWNER TO solvelog_admin;

--
-- Name: category; Type: TABLE; Schema: public; Owner: solvelog_admin
--

CREATE TABLE public.category (
    id bigint NOT NULL,
    parent_category_id bigint,
    type character varying(50) NOT NULL
);


ALTER TABLE public.category OWNER TO solvelog_admin;

--
-- Name: category_seq; Type: SEQUENCE; Schema: public; Owner: solvelog_admin
--

CREATE SEQUENCE public.category_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.category_seq OWNER TO solvelog_admin;

--
-- Name: comment; Type: TABLE; Schema: public; Owner: solvelog_admin
--

CREATE TABLE public.comment (
    comment_id bigint NOT NULL,
    created_date timestamp(6) without time zone,
    last_modified_date timestamp(6) without time zone,
    parent_comment_id bigint,
    post_id bigint,
    user_id bigint,
    comment character varying(255)
);


ALTER TABLE public.comment OWNER TO solvelog_admin;

--
-- Name: comment_seq; Type: SEQUENCE; Schema: public; Owner: solvelog_admin
--

CREATE SEQUENCE public.comment_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.comment_seq OWNER TO solvelog_admin;

--
-- Name: file; Type: TABLE; Schema: public; Owner: solvelog_admin
--

CREATE TABLE public.file (
    is_thumbnail boolean,
    is_user_img boolean,
    id bigint NOT NULL,
    post_id bigint,
    size bigint,
    upload_date timestamp(6) without time zone,
    original_file_name character varying(255) NOT NULL,
    path character varying(255) NOT NULL,
    type character varying(255),
    username character varying(255) NOT NULL,
    CONSTRAINT file_type_check CHECK (((type)::text = ANY ((ARRAY['JPG'::character varying, 'JPEG'::character varying, 'PNG'::character varying, 'GIF'::character varying, 'BMP'::character varying, 'WEBP'::character varying, 'PDF'::character varying, 'DOC'::character varying, 'DOCX'::character varying, 'PPT'::character varying, 'PPTX'::character varying, 'XLS'::character varying, 'XLSX'::character varying, 'TXT'::character varying, 'HWP'::character varying, 'ZIP'::character varying, 'RAR'::character varying, 'MP4'::character varying, 'MP3'::character varying])::text[])))
);


ALTER TABLE public.file OWNER TO solvelog_admin;

--
-- Name: file_seq; Type: SEQUENCE; Schema: public; Owner: solvelog_admin
--

CREATE SEQUENCE public.file_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.file_seq OWNER TO solvelog_admin;

--
-- Name: likes; Type: TABLE; Schema: public; Owner: solvelog_admin
--

CREATE TABLE public.likes (
    like_id bigint NOT NULL,
    post_id bigint,
    user_id bigint
);


ALTER TABLE public.likes OWNER TO solvelog_admin;

--
-- Name: likes_seq; Type: SEQUENCE; Schema: public; Owner: solvelog_admin
--

CREATE SEQUENCE public.likes_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.likes_seq OWNER TO solvelog_admin;

--
-- Name: post; Type: TABLE; Schema: public; Owner: solvelog_admin
--

CREATE TABLE public.post (
    is_temp boolean,
    view_count integer NOT NULL,
    category_id bigint,
    created_date timestamp(6) without time zone,
    last_modified_date timestamp(6) without time zone,
    post_id bigint NOT NULL,
    user_id bigint,
    content text,
    summary character varying(255),
    tags character varying(255),
    title character varying(255) NOT NULL
);


ALTER TABLE public.post OWNER TO solvelog_admin;

--
-- Name: post_seq; Type: SEQUENCE; Schema: public; Owner: solvelog_admin
--

CREATE SEQUENCE public.post_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.post_seq OWNER TO solvelog_admin;

--
-- Name: post_tag; Type: TABLE; Schema: public; Owner: solvelog_admin
--

CREATE TABLE public.post_tag (
    post_id bigint,
    post_tag_id bigint NOT NULL,
    tag_id bigint
);


ALTER TABLE public.post_tag OWNER TO solvelog_admin;

--
-- Name: post_tag_seq; Type: SEQUENCE; Schema: public; Owner: solvelog_admin
--

CREATE SEQUENCE public.post_tag_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.post_tag_seq OWNER TO solvelog_admin;

--
-- Name: tag; Type: TABLE; Schema: public; Owner: solvelog_admin
--

CREATE TABLE public.tag (
    tag_id bigint NOT NULL,
    name character varying(255)
);


ALTER TABLE public.tag OWNER TO solvelog_admin;

--
-- Name: tag_seq; Type: SEQUENCE; Schema: public; Owner: solvelog_admin
--

CREATE SEQUENCE public.tag_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.tag_seq OWNER TO solvelog_admin;

--
-- Name: users; Type: TABLE; Schema: public; Owner: solvelog_admin
--

CREATE TABLE public.users (
    created_date timestamp(6) without time zone,
    user_id bigint NOT NULL,
    user_img_id bigint,
    nickname character varying(255) NOT NULL,
    password character varying(255) NOT NULL,
    role character varying(255),
    username character varying(255) NOT NULL,
    CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['USER'::character varying, 'ADMIN'::character varying])::text[])))
);


ALTER TABLE public.users OWNER TO solvelog_admin;

--
-- Name: users_seq; Type: SEQUENCE; Schema: public; Owner: solvelog_admin
--

CREATE SEQUENCE public.users_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.users_seq OWNER TO solvelog_admin;

--
-- Name: alarm alarm_pkey; Type: CONSTRAINT; Schema: public; Owner: solvelog_admin
--

ALTER TABLE ONLY public.alarm
    ADD CONSTRAINT alarm_pkey PRIMARY KEY (alarm_id);


--
-- Name: alarm_type alarm_type_pkey; Type: CONSTRAINT; Schema: public; Owner: solvelog_admin
--

ALTER TABLE ONLY public.alarm_type
    ADD CONSTRAINT alarm_type_pkey PRIMARY KEY (alarm_type_id);


--
-- Name: alarm_type alarm_type_type_key; Type: CONSTRAINT; Schema: public; Owner: solvelog_admin
--

ALTER TABLE ONLY public.alarm_type
    ADD CONSTRAINT alarm_type_type_key UNIQUE (type);


--
-- Name: category category_pkey; Type: CONSTRAINT; Schema: public; Owner: solvelog_admin
--

ALTER TABLE ONLY public.category
    ADD CONSTRAINT category_pkey PRIMARY KEY (id);


--
-- Name: category category_type_key; Type: CONSTRAINT; Schema: public; Owner: solvelog_admin
--

ALTER TABLE ONLY public.category
    ADD CONSTRAINT category_type_key UNIQUE (type);


--
-- Name: comment comment_pkey; Type: CONSTRAINT; Schema: public; Owner: solvelog_admin
--

ALTER TABLE ONLY public.comment
    ADD CONSTRAINT comment_pkey PRIMARY KEY (comment_id);


--
-- Name: file file_pkey; Type: CONSTRAINT; Schema: public; Owner: solvelog_admin
--

ALTER TABLE ONLY public.file
    ADD CONSTRAINT file_pkey PRIMARY KEY (id);


--
-- Name: likes likes_pkey; Type: CONSTRAINT; Schema: public; Owner: solvelog_admin
--

ALTER TABLE ONLY public.likes
    ADD CONSTRAINT likes_pkey PRIMARY KEY (like_id);


--
-- Name: likes post_id_user_id_unique; Type: CONSTRAINT; Schema: public; Owner: solvelog_admin
--

ALTER TABLE ONLY public.likes
    ADD CONSTRAINT post_id_user_id_unique UNIQUE (user_id, post_id);


--
-- Name: post post_pkey; Type: CONSTRAINT; Schema: public; Owner: solvelog_admin
--

ALTER TABLE ONLY public.post
    ADD CONSTRAINT post_pkey PRIMARY KEY (post_id);


--
-- Name: post_tag post_tag_pkey; Type: CONSTRAINT; Schema: public; Owner: solvelog_admin
--

ALTER TABLE ONLY public.post_tag
    ADD CONSTRAINT post_tag_pkey PRIMARY KEY (post_tag_id);


--
-- Name: tag tag_name_key; Type: CONSTRAINT; Schema: public; Owner: solvelog_admin
--

ALTER TABLE ONLY public.tag
    ADD CONSTRAINT tag_name_key UNIQUE (name);


--
-- Name: tag tag_pkey; Type: CONSTRAINT; Schema: public; Owner: solvelog_admin
--

ALTER TABLE ONLY public.tag
    ADD CONSTRAINT tag_pkey PRIMARY KEY (tag_id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: solvelog_admin
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);


--
-- Name: users users_username_key; Type: CONSTRAINT; Schema: public; Owner: solvelog_admin
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_key UNIQUE (username);


--
-- Name: alarm fk29nfm0duhkp2co2jgymbxqf7b; Type: FK CONSTRAINT; Schema: public; Owner: solvelog_admin
--

ALTER TABLE ONLY public.alarm
    ADD CONSTRAINT fk29nfm0duhkp2co2jgymbxqf7b FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- Name: post fk7ky67sgi7k0ayf22652f7763r; Type: FK CONSTRAINT; Schema: public; Owner: solvelog_admin
--

ALTER TABLE ONLY public.post
    ADD CONSTRAINT fk7ky67sgi7k0ayf22652f7763r FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- Name: post_tag fkac1wdchd2pnur3fl225obmlg0; Type: FK CONSTRAINT; Schema: public; Owner: solvelog_admin
--

ALTER TABLE ONLY public.post_tag
    ADD CONSTRAINT fkac1wdchd2pnur3fl225obmlg0 FOREIGN KEY (tag_id) REFERENCES public.tag(tag_id);


--
-- Name: post_tag fkc2auetuvsec0k566l0eyvr9cs; Type: FK CONSTRAINT; Schema: public; Owner: solvelog_admin
--

ALTER TABLE ONLY public.post_tag
    ADD CONSTRAINT fkc2auetuvsec0k566l0eyvr9cs FOREIGN KEY (post_id) REFERENCES public.post(post_id);


--
-- Name: post fkg6l1ydp1pwkmyj166teiuov1b; Type: FK CONSTRAINT; Schema: public; Owner: solvelog_admin
--

ALTER TABLE ONLY public.post
    ADD CONSTRAINT fkg6l1ydp1pwkmyj166teiuov1b FOREIGN KEY (category_id) REFERENCES public.category(id);


--
-- Name: comment fkhvh0e2ybgg16bpu229a5teje7; Type: FK CONSTRAINT; Schema: public; Owner: solvelog_admin
--

ALTER TABLE ONLY public.comment
    ADD CONSTRAINT fkhvh0e2ybgg16bpu229a5teje7 FOREIGN KEY (parent_comment_id) REFERENCES public.comment(comment_id);


--
-- Name: alarm fkilooyt7m4lc9n7u0bxbs2td00; Type: FK CONSTRAINT; Schema: public; Owner: solvelog_admin
--

ALTER TABLE ONLY public.alarm
    ADD CONSTRAINT fkilooyt7m4lc9n7u0bxbs2td00 FOREIGN KEY (alarm_type_id) REFERENCES public.alarm_type(alarm_type_id);


--
-- Name: likes fknvx9seeqqyy71bij291pwiwrg; Type: FK CONSTRAINT; Schema: public; Owner: solvelog_admin
--

ALTER TABLE ONLY public.likes
    ADD CONSTRAINT fknvx9seeqqyy71bij291pwiwrg FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- Name: likes fkowd6f4s7x9f3w50pvlo6x3b41; Type: FK CONSTRAINT; Schema: public; Owner: solvelog_admin
--

ALTER TABLE ONLY public.likes
    ADD CONSTRAINT fkowd6f4s7x9f3w50pvlo6x3b41 FOREIGN KEY (post_id) REFERENCES public.post(post_id);


--
-- Name: comment fkqm52p1v3o13hy268he0wcngr5; Type: FK CONSTRAINT; Schema: public; Owner: solvelog_admin
--

ALTER TABLE ONLY public.comment
    ADD CONSTRAINT fkqm52p1v3o13hy268he0wcngr5 FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- Name: comment fks1slvnkuemjsq2kj4h3vhx7i1; Type: FK CONSTRAINT; Schema: public; Owner: solvelog_admin
--

ALTER TABLE ONLY public.comment
    ADD CONSTRAINT fks1slvnkuemjsq2kj4h3vhx7i1 FOREIGN KEY (post_id) REFERENCES public.post(post_id);


--
-- Name: category fks2ride9gvilxy2tcuv7witnxc; Type: FK CONSTRAINT; Schema: public; Owner: solvelog_admin
--

ALTER TABLE ONLY public.category
    ADD CONSTRAINT fks2ride9gvilxy2tcuv7witnxc FOREIGN KEY (parent_category_id) REFERENCES public.category(id);


--
-- PostgreSQL database dump complete
--

