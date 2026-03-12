-- Table: public.sys_document

-- DROP TABLE IF EXISTS public.sys_document;

CREATE TABLE IF NOT EXISTS public.sys_document
(
    id bigint NOT NULL,
    name character varying(255) COLLATE pg_catalog."default" NOT NULL,
    extension character varying(16) COLLATE pg_catalog."default",
    content_type character varying(128) COLLATE pg_catalog."default",
    size integer,
    group_name character varying(255) COLLATE pg_catalog."default" NOT NULL,
    path character varying(255) COLLATE pg_catalog."default" NOT NULL,
    create_time timestamp without time zone NOT NULL,
    create_by bigint,
    update_time timestamp without time zone NOT NULL,
    update_by bigint,
    is_deleted boolean NOT NULL DEFAULT false,
    CONSTRAINT sys_document_pkey PRIMARY KEY (id)
    )

    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.sys_document
    OWNER to postgres;