create table tweets
(
    id          varchar(255) primary key,
    title       varchar(1024),
    description varchar(1024),
    pubDate     varchar(255),
    link        varchar(255),
    author      varchar(255),
    timestamp   timestamp
);

create table sentiments
(
    id        varchar(255) primary key,
    sentiment varchar(2024)
);

create table endpoints
(
    id   varchar(255) primary key,
    link varchar(2024)
);
