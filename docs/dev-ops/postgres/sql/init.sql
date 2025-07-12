create database if not exists springai;

use springai;

drop table if exists vector_store;

create table vector_store
(
    id uuid default gen_random_uuid() not null
        constraint vector_store_like_openai_pkey
        primary key,
    content text not null,
    metadata jsonb,
    embedding vector(1024)
);

alter table vector_store
    owner to postgres;