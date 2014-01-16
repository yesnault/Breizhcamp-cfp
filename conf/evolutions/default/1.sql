# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table comment (
  id                        bigint auto_increment not null,
  author_id                 bigint,
  talk_id                   bigint,
  comment                   varchar(140),
  clos                      tinyint(1) default 0,
  private_comment           tinyint(1) default 0,
  question_id               bigint,
  constraint pk_comment primary key (id))
;

create table credentials (
  id                        bigint auto_increment not null,
  user_id                   bigint,
  ext_user_id               varchar(255),
  provider_id               varchar(255),
  first_name                varchar(255),
  last_name                 varchar(255),
  o_auth1token              varchar(255),
  o_auth1secret             varchar(255),
  o_auth2access_token       varchar(255),
  o_auth2token_type         varchar(255),
  o_auth2expires_in         integer,
  o_auth2refresh_token      varchar(255),
  password_hasher           varchar(255),
  password                  varchar(255),
  password_salt             varchar(255),
  constraint pk_credentials primary key (id))
;

create table creneau (
  id                        bigint auto_increment not null,
  libelle                   varchar(50),
  duree_minutes             integer,
  description               varchar(255),
  nb_instance               integer,
  event_id                  bigint,
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
  draft                     tinyint(1) default 0,
  event_id                  bigint,
  duree_preferee_id         bigint,
  duree_approuve_id         bigint,
  constraint ck_talk_status_talk check (status_talk in ('A','W','R')),
  constraint uq_talk_title unique (title),
  constraint pk_talk primary key (id))
;

create table user (
  id                        bigint auto_increment not null,
  email                     varchar(255),
  fullname                  varchar(255),
  authentication_method     varchar(255),
  credentials_id            bigint,
  date_creation             datetime,
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

create table event (
  id                        bigint not null,
  name                      varchar(50),
  description               varchar(1000),
  clos                      tinyint(1) default 0,
  constraint uq_event_name unique (name),
  constraint pk_event primary key (id))
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

create table user_talk (
  user_id                        bigint not null,
  talk_id                        bigint not null,
  constraint pk_user_talk primary key (user_id, talk_id))
;
alter table comment add constraint fk_comment_author_1 foreign key (author_id) references user (id) on delete restrict on update restrict;
create index ix_comment_author_1 on comment (author_id);
alter table comment add constraint fk_comment_talk_2 foreign key (talk_id) references talk (id) on delete restrict on update restrict;
create index ix_comment_talk_2 on comment (talk_id);
alter table comment add constraint fk_comment_question_3 foreign key (question_id) references comment (id) on delete restrict on update restrict;
create index ix_comment_question_3 on comment (question_id);
alter table credentials add constraint fk_credentials_user_4 foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_credentials_user_4 on credentials (user_id);
alter table dynamic_field_value add constraint fk_dynamic_field_value_dynamicField_5 foreign key (dynamic_field_id) references dynamic_field (id) on delete restrict on update restrict;
create index ix_dynamic_field_value_dynamicField_5 on dynamic_field_value (dynamic_field_id);
alter table dynamic_field_value add constraint fk_dynamic_field_value_user_6 foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_dynamic_field_value_user_6 on dynamic_field_value (user_id);
alter table lien add constraint fk_lien_user_7 foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_lien_user_7 on lien (user_id);
alter table talk add constraint fk_talk_speaker_8 foreign key (speaker_id) references user (id) on delete restrict on update restrict;
create index ix_talk_speaker_8 on talk (speaker_id);
alter table talk add constraint fk_talk_dureePreferee_9 foreign key (duree_preferee_id) references creneau (id) on delete restrict on update restrict;
create index ix_talk_dureePreferee_9 on talk (duree_preferee_id);
alter table talk add constraint fk_talk_dureeApprouve_10 foreign key (duree_approuve_id) references creneau (id) on delete restrict on update restrict;
create index ix_talk_dureeApprouve_10 on talk (duree_approuve_id);
alter table user add constraint fk_user_credentials_11 foreign key (credentials_id) references credentials (id) on delete restrict on update restrict;
create index ix_user_credentials_11 on user (credentials_id);
alter table vote add constraint fk_vote_user_12 foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_vote_user_12 on vote (user_id);
alter table vote add constraint fk_vote_talk_13 foreign key (talk_id) references talk (id) on delete restrict on update restrict;
create index ix_vote_talk_13 on vote (talk_id);



alter table creneau_talk add constraint fk_creneau_talk_creneau_01 foreign key (creneau_id) references creneau (id) on delete restrict on update restrict;

alter table creneau_talk add constraint fk_creneau_talk_talk_02 foreign key (talk_id) references talk (id) on delete restrict on update restrict;

alter table tag_talk add constraint fk_tag_talk_tag_01 foreign key (tag_id) references tag (id) on delete restrict on update restrict;

alter table tag_talk add constraint fk_tag_talk_talk_02 foreign key (talk_id) references talk (id) on delete restrict on update restrict;

alter table user_talk add constraint fk_user_talk_user_01 foreign key (user_id) references user (id) on delete restrict on update restrict;

alter table user_talk add constraint fk_user_talk_talk_02 foreign key (talk_id) references talk (id) on delete restrict on update restrict;

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table comment;

drop table credentials;

drop table creneau;

drop table creneau_talk;

drop table dynamic_field;

drop table dynamic_field_value;

drop table lien;

drop table tag;

drop table if exists event;

drop table tag_talk;

drop table talk;

drop table user_talk;

drop table user;

drop table vote;

drop table vote_status;

SET FOREIGN_KEY_CHECKS=1;

