# --- !Ups

create table comment (
  id                        bigint not null,
  author_id                 bigint,
  talk_id                   bigint,
  comment                   varchar(140),
  constraint pk_comment primary key (id))
;

create table lien (
  id                        bigint not null,
  user_id                   bigint not null,
  label                     varchar(50),
  url                       varchar(200),
  constraint pk_lien primary key (id))
;

create table talk (
  id                        bigint not null,
  title                     varchar(50),
  description               varchar(2000),
  speaker_id                bigint,
  constraint uq_talk_title unique (title),
  constraint pk_talk primary key (id))
;

create table token (
  token                     varchar(255) not null,
  user_id                   bigint,
  type                      varchar(8),
  date_creation             timestamp,
  email                     varchar(255),
  constraint ck_token_type check (type in ('password','email')),
  constraint pk_token primary key (token))
;

create table user (
  id                        bigint not null,
  email                     varchar(255),
  fullname                  varchar(255),
  confirmation_token        varchar(255),
  password_hash             varchar(255),
  date_creation             timestamp,
  validated                 boolean,
  admin                     boolean,
  notif_on_my_talk          boolean,
  notif_admin_on_all_talk   boolean,
  notif_admin_on_talk_with_comment boolean,
  description               varchar(2000),
  constraint uq_user_email unique (email),
  constraint uq_user_fullname unique (fullname),
  constraint pk_user primary key (id))
;

create sequence comment_seq;

create sequence lien_seq;

create sequence talk_seq;

create sequence token_seq;

create sequence user_seq;

alter table comment add constraint fk_comment_author_1 foreign key (author_id) references user (id) on delete restrict on update restrict;
create index ix_comment_author_1 on comment (author_id);
alter table comment add constraint fk_comment_talk_2 foreign key (talk_id) references talk (id) on delete restrict on update restrict;
create index ix_comment_talk_2 on comment (talk_id);
alter table lien add constraint fk_lien_user_3 foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_lien_user_3 on lien (user_id);
alter table talk add constraint fk_talk_speaker_4 foreign key (speaker_id) references user (id) on delete restrict on update restrict;
create index ix_talk_speaker_4 on talk (speaker_id);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists comment;

drop table if exists lien;

drop table if exists talk;

drop table if exists token;

drop table if exists user;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists comment_seq;

drop sequence if exists lien_seq;

drop sequence if exists talk_seq;

drop sequence if exists token_seq;

drop sequence if exists user_seq;

