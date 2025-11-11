--
-- PostgreSQL database dump
--

\restrict ODTRzKP2C72Yc2Zw35X8keFzfB54HcGTFurWJcWa9DfTjpZuRVFgxjTXOgWLLBZ

-- Dumped from database version 16.10 (Debian 16.10-1.pgdg13+1)
-- Dumped by pg_dump version 16.10 (Debian 16.10-1.pgdg13+1)

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
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: mangauser
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE public.flyway_schema_history OWNER TO mangauser;

--
-- Name: manga; Type: TABLE; Schema: public; Owner: mangauser
--

CREATE TABLE public.manga (
    id bigint NOT NULL,
    author character varying(255),
    cover_url character varying(255),
    description text,
    rating double precision,
    status character varying(255),
    title character varying(255),
    release_year integer,
    dex_id character varying(255)
);


ALTER TABLE public.manga OWNER TO mangauser;

--
-- Name: manga_alt_titles; Type: TABLE; Schema: public; Owner: mangauser
--

CREATE TABLE public.manga_alt_titles (
    manga_id bigint NOT NULL,
    alt_title text NOT NULL
);


ALTER TABLE public.manga_alt_titles OWNER TO mangauser;

--
-- Name: manga_genres; Type: TABLE; Schema: public; Owner: mangauser
--

CREATE TABLE public.manga_genres (
    manga_id bigint NOT NULL,
    genre character varying(255) NOT NULL
);


ALTER TABLE public.manga_genres OWNER TO mangauser;

--
-- Name: manga_id_seq; Type: SEQUENCE; Schema: public; Owner: mangauser
--

ALTER TABLE public.manga ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.manga_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: user_library; Type: TABLE; Schema: public; Owner: mangauser
--

CREATE TABLE public.user_library (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    manga_id bigint NOT NULL,
    reading_status character varying(32) DEFAULT 'Plan to Read'::character varying NOT NULL,
    rating integer,
    review text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.user_library OWNER TO mangauser;

--
-- Name: user_library_id_seq; Type: SEQUENCE; Schema: public; Owner: mangauser
--

CREATE SEQUENCE public.user_library_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.user_library_id_seq OWNER TO mangauser;

--
-- Name: user_library_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: mangauser
--

ALTER SEQUENCE public.user_library_id_seq OWNED BY public.user_library.id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: mangauser
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    username character varying(50) NOT NULL,
    email character varying(255) NOT NULL,
    password_hash character varying(255) NOT NULL,
    display_name character varying(100),
    role character varying(50) DEFAULT 'USER'::character varying,
    enabled boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.users OWNER TO mangauser;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: mangauser
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.users_id_seq OWNER TO mangauser;

--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: mangauser
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: user_library id; Type: DEFAULT; Schema: public; Owner: mangauser
--

ALTER TABLE ONLY public.user_library ALTER COLUMN id SET DEFAULT nextval('public.user_library_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: mangauser
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: mangauser
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: manga manga_dex_id_key; Type: CONSTRAINT; Schema: public; Owner: mangauser
--

ALTER TABLE ONLY public.manga
    ADD CONSTRAINT manga_dex_id_key UNIQUE (dex_id);


--
-- Name: manga manga_pkey; Type: CONSTRAINT; Schema: public; Owner: mangauser
--

ALTER TABLE ONLY public.manga
    ADD CONSTRAINT manga_pkey PRIMARY KEY (id);


--
-- Name: user_library user_library_pkey; Type: CONSTRAINT; Schema: public; Owner: mangauser
--

ALTER TABLE ONLY public.user_library
    ADD CONSTRAINT user_library_pkey PRIMARY KEY (id);


--
-- Name: user_library user_library_user_id_manga_id_key; Type: CONSTRAINT; Schema: public; Owner: mangauser
--

ALTER TABLE ONLY public.user_library
    ADD CONSTRAINT user_library_user_id_manga_id_key UNIQUE (user_id, manga_id);


--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: mangauser
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: mangauser
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: users users_username_key; Type: CONSTRAINT; Schema: public; Owner: mangauser
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_key UNIQUE (username);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: mangauser
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: idx_manga_author; Type: INDEX; Schema: public; Owner: mangauser
--

CREATE INDEX idx_manga_author ON public.manga USING btree (lower((author)::text));


--
-- Name: idx_manga_genres_genre; Type: INDEX; Schema: public; Owner: mangauser
--

CREATE INDEX idx_manga_genres_genre ON public.manga_genres USING btree (genre);


--
-- Name: idx_manga_genres_manga_genre; Type: INDEX; Schema: public; Owner: mangauser
--

CREATE INDEX idx_manga_genres_manga_genre ON public.manga_genres USING btree (manga_id, genre);


--
-- Name: idx_manga_genres_manga_id; Type: INDEX; Schema: public; Owner: mangauser
--

CREATE INDEX idx_manga_genres_manga_id ON public.manga_genres USING btree (manga_id);


--
-- Name: idx_manga_status; Type: INDEX; Schema: public; Owner: mangauser
--

CREATE INDEX idx_manga_status ON public.manga USING btree (status);


--
-- Name: idx_manga_title; Type: INDEX; Schema: public; Owner: mangauser
--

CREATE INDEX idx_manga_title ON public.manga USING btree (lower((title)::text));


--
-- Name: idx_manga_year; Type: INDEX; Schema: public; Owner: mangauser
--

CREATE INDEX idx_manga_year ON public.manga USING btree (release_year);


--
-- Name: manga_alt_titles fk_manga_alt; Type: FK CONSTRAINT; Schema: public; Owner: mangauser
--

ALTER TABLE ONLY public.manga_alt_titles
    ADD CONSTRAINT fk_manga_alt FOREIGN KEY (manga_id) REFERENCES public.manga(id) ON DELETE CASCADE;


--
-- Name: manga_genres fk_manga_gen; Type: FK CONSTRAINT; Schema: public; Owner: mangauser
--

ALTER TABLE ONLY public.manga_genres
    ADD CONSTRAINT fk_manga_gen FOREIGN KEY (manga_id) REFERENCES public.manga(id) ON DELETE CASCADE;


--
-- Name: user_library user_library_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mangauser
--

ALTER TABLE ONLY public.user_library
    ADD CONSTRAINT user_library_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

\unrestrict ODTRzKP2C72Yc2Zw35X8keFzfB54HcGTFurWJcWa9DfTjpZuRVFgxjTXOgWLLBZ

