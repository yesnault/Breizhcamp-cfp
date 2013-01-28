# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table comment (
  id                        bigint auto_increment not null,
  author_id                 bigint,
  talk_id                   bigint,
  comment                   varchar(140),
  private_comment           tinyint(1) default 0,
  constraint pk_comment primary key (id))
;

create table creneau (
  id                        bigint auto_increment not null,
  libelle                   varchar(50),
  duree_minutes             integer,
  constraint uq_creneau_libelle unique (libelle),
  constraint pk_creneau primary key (id))
;

create table dynamic_field (
  id                        bigint auto_increment not null,
  name                      varchar(50),
  constraint uq_dynamic_field_name unique (name),
  constraint pk_dynamic_field primary key (id))
;

create table dynamic_field_value (
  id                        bigint auto_increment not null,
  value                     varchar(255),
  dynamic_field_id          bigint,
  user_id                   bigint,
  constraint pk_dynamic_field_value primary key (id))
;

create table external_user_id (
  id                        bigint auto_increment not null,
  user_id                   bigint,
  provider_uuid             varchar(255),
  provider_id               varchar(255),
  constraint pk_external_user_id primary key (id))
;

create table lien (
  id                        bigint auto_increment not null,
  user_id                   bigint not null,
  label                     varchar(50),
  url                       varchar(200),
  constraint pk_lien primary key (id))
;

create table tag (
  id                        bigint auto_increment not null,
  nom                       varchar(255),
  constraint uq_tag_nom unique (nom),
  constraint pk_tag primary key (id))
;

create table talk (
  id                        bigint auto_increment not null,
  title                     varchar(50),
  description               varchar(2000),
  speaker_id                bigint,
  status_talk               varchar(1),
  constraint ck_talk_status_talk check (status_talk in ('A','W','R')),
  constraint uq_talk_title unique (title),
  constraint pk_talk primary key (id))
;

create table user (
  id                        bigint auto_increment not null,
  email                     varchar(255),
  fullname                  varchar(255),
  sign_up                   tinyint(1) default 0,
  token_uuid                varchar(255),
  token_creation_time       datetime,
  token_modification_time   datetime,
  password_hash             varchar(255),
  date_creation             datetime,
  validated                 tinyint(1) default 0,
  admin                     tinyint(1) default 0,
  notif_on_my_talk          tinyint(1) default 0,
  notif_admin_on_all_talk   tinyint(1) default 0,
  notif_admin_on_talk_with_comment tinyint(1) default 0,
  adresse_mac               varchar(255),
  description               varchar(2000),
  avatar                    varchar(255),
  constraint uq_user_email unique (email),
  constraint pk_user primary key (id))
;

create table vote (
  id                        bigint auto_increment not null,
  user_id                   bigint,
  talk_id                   bigint,
  note                      integer,
  constraint pk_vote primary key (id))
;

create table vote_status (
  id                        bigint auto_increment not null,
  status                    integer,
  constraint ck_vote_status_status check (status in (0,1,2)),
  constraint pk_vote_status primary key (id))
;


create table creneau_talk (
  creneau_id                     bigint not null,
  talk_id                        bigint not null,
  constraint pk_creneau_talk primary key (creneau_id, talk_id))
;

create table tag_talk (
  tag_id                         bigint not null,
  talk_id                        bigint not null,
  constraint pk_tag_talk primary key (tag_id, talk_id))
;
alter table comment add constraint fk_comment_author_1 foreign key (author_id) references user (id) on delete restrict on update restrict;
create index ix_comment_author_1 on comment (author_id);
alter table comment add constraint fk_comment_talk_2 foreign key (talk_id) references talk (id) on delete restrict on update restrict;
create index ix_comment_talk_2 on comment (talk_id);
alter table dynamic_field_value add constraint fk_dynamic_field_value_dynamic_3 foreign key (dynamic_field_id) references dynamic_field (id) on delete restrict on update restrict;
create index ix_dynamic_field_value_dynamic_3 on dynamic_field_value (dynamic_field_id);
alter table dynamic_field_value add constraint fk_dynamic_field_value_user_4 foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_dynamic_field_value_user_4 on dynamic_field_value (user_id);
alter table external_user_id add constraint fk_external_user_id_user_5 foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_external_user_id_user_5 on external_user_id (user_id);
alter table lien add constraint fk_lien_user_6 foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_lien_user_6 on lien (user_id);
alter table talk add constraint fk_talk_speaker_7 foreign key (speaker_id) references user (id) on delete restrict on update restrict;
create index ix_talk_speaker_7 on talk (speaker_id);
alter table vote add constraint fk_vote_user_8 foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_vote_user_8 on vote (user_id);
alter table vote add constraint fk_vote_talk_9 foreign key (talk_id) references talk (id) on delete restrict on update restrict;
create index ix_vote_talk_9 on vote (talk_id);



alter table creneau_talk add constraint fk_creneau_talk_creneau_01 foreign key (creneau_id) references creneau (id) on delete restrict on update restrict;

alter table creneau_talk add constraint fk_creneau_talk_talk_02 foreign key (talk_id) references talk (id) on delete restrict on update restrict;

alter table tag_talk add constraint fk_tag_talk_tag_01 foreign key (tag_id) references tag (id) on delete restrict on update restrict;

alter table tag_talk add constraint fk_tag_talk_talk_02 foreign key (talk_id) references talk (id) on delete restrict on update restrict;

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table comment;

drop table creneau;

drop table creneau_talk;

drop table dynamic_field;

drop table dynamic_field_value;

drop table external_user_id;

drop table lien;

drop table tag;

drop table tag_talk;

drop table talk;

drop table user;

drop table vote;

drop table vote_status;

SET FOREIGN_KEY_CHECKS=1;

