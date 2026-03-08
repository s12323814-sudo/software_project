--
-- PostgreSQL database dump
--

\restrict dL1Qjondd4wyAR0YyIRFbz63nfo2uZZX0jo2ejLZe7oz8V5kIMM94Hesog5NKOf

-- Dumped from database version 18.0
-- Dumped by pg_dump version 18.0

-- Started on 2026-03-01 19:34:11

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
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
-- TOC entry 220 (class 1259 OID 16537)
-- Name: admin; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.admin (
    admin_id integer NOT NULL,
    username character varying(50) NOT NULL,
    password character varying(100) NOT NULL
);


ALTER TABLE public.admin OWNER TO postgres;

--
-- TOC entry 219 (class 1259 OID 16536)
-- Name: admin_admin_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.admin_admin_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.admin_admin_id_seq OWNER TO postgres;

--
-- TOC entry 4980 (class 0 OID 0)
-- Dependencies: 219
-- Name: admin_admin_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.admin_admin_id_seq OWNED BY public.admin.admin_id;


--
-- TOC entry 222 (class 1259 OID 16549)
-- Name: appointment_slot; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.appointment_slot (
    slot_id integer NOT NULL,
    slot_date date NOT NULL,
    slot_time time without time zone NOT NULL,
    max_capacity integer DEFAULT 1,
    booked_count integer DEFAULT 0,
    admin_id integer NOT NULL
);


ALTER TABLE public.appointment_slot OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 16548)
-- Name: appointment_slot_slot_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.appointment_slot ALTER COLUMN slot_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.appointment_slot_slot_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 4814 (class 2604 OID 16540)
-- Name: admin admin_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.admin ALTER COLUMN admin_id SET DEFAULT nextval('public.admin_admin_id_seq'::regclass);


--
-- TOC entry 4972 (class 0 OID 16537)
-- Dependencies: 220
-- Data for Name: admin; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.admin (admin_id, username, password) FROM stdin;
\.


--
-- TOC entry 4974 (class 0 OID 16549)
-- Dependencies: 222
-- Data for Name: appointment_slot; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.appointment_slot (slot_id, slot_date, slot_time, max_capacity, booked_count, admin_id) FROM stdin;
\.


--
-- TOC entry 4981 (class 0 OID 0)
-- Dependencies: 219
-- Name: admin_admin_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.admin_admin_id_seq', 1, false);


--
-- TOC entry 4982 (class 0 OID 0)
-- Dependencies: 221
-- Name: appointment_slot_slot_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.appointment_slot_slot_id_seq', 1, false);


--
-- TOC entry 4818 (class 2606 OID 16545)
-- Name: admin admin_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.admin
    ADD CONSTRAINT admin_pkey PRIMARY KEY (admin_id);


--
-- TOC entry 4820 (class 2606 OID 16547)
-- Name: admin admin_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.admin
    ADD CONSTRAINT admin_username_key UNIQUE (username);


--
-- TOC entry 4822 (class 2606 OID 16558)
-- Name: appointment_slot appointment_slot_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.appointment_slot
    ADD CONSTRAINT appointment_slot_pkey PRIMARY KEY (slot_id);


--
-- TOC entry 4823 (class 2606 OID 16560)
-- Name: appointment_slot fk_admin; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.appointment_slot
    ADD CONSTRAINT fk_admin FOREIGN KEY (admin_id) REFERENCES public.admin(admin_id);


-- Completed on 2026-03-01 19:34:12

--
-- PostgreSQL database dump complete
--

\unrestrict dL1Qjondd4wyAR0YyIRFbz63nfo2uZZX0jo2ejLZe7oz8V5kIMM94Hesog5NKOf
CREATE TABLE appointments (
    appointment_id SERIAL PRIMARY KEY,
    slot_id INT NOT NULL REFERENCES appointment_slots(slot_id) ON DELETE CASCADE,
    user_id INT REFERENCES users(user_id) ON DELETE SET NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    duration INT NOT NULL,
    participants INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED'
);
