# --- !Ups

create table comment (
  id                        bigint auto_increment not null,
  author_id                 bigint,
  talk_id                   bigint,
  comment                   varchar(140),
  constraint pk_comment primary key (id))
;

create table lien (
  id                        bigint auto_increment not null,
  user_id                   bigint not null,
  label                     varchar(50),
  url                       varchar(200),
  constraint pk_lien primary key (id))
;

create table talk (
  id                        bigint auto_increment not null,
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
  date_creation             datetime,
  email                     varchar(255),
  constraint ck_token_type check (type in ('password','email')),
  constraint pk_token primary key (token))
;

create table user (
  id                        bigint auto_increment not null,
  email                     varchar(255),
  fullname                  varchar(255),
  confirmation_token        varchar(255),
  password_hash             varchar(255),
  date_creation             datetime,
  validated                 tinyint(1) default 0,
  admin                     tinyint(1) default 0,
  notif_on_my_talk          tinyint(1) default 0,
  notif_admin_on_all_talk   tinyint(1) default 0,
  notif_admin_on_talk_with_comment tinyint(1) default 0,
  description               varchar(2000),
  constraint uq_user_email unique (email),
  constraint uq_user_fullname unique (fullname),
  constraint pk_user primary key (id))
;

alter table comment add constraint fk_comment_author_1 foreign key (author_id) references user (id) on delete restrict on update restrict;
create index ix_comment_author_1 on comment (author_id);
alter table comment add constraint fk_comment_talk_2 foreign key (talk_id) references talk (id) on delete restrict on update restrict;
create index ix_comment_talk_2 on comment (talk_id);
alter table lien add constraint fk_lien_user_3 foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_lien_user_3 on lien (user_id);
alter table talk add constraint fk_talk_speaker_4 foreign key (speaker_id) references user (id) on delete restrict on update restrict;
create index ix_talk_speaker_4 on talk (speaker_id);



# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table comment;

drop table lien;

drop table talk;

drop table token;

drop table user;

SET FOREIGN_KEY_CHECKS=1;

