# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table comment (
  id                        bigint not null,
  author_id                 bigint,
  proposal_id                   bigint,
  comment                   varchar(140),
  clos                      boolean,
  private_comment           boolean,
  question_id               bigint,
  constraint pk_comment primary key (id))
;

create table credentials (
  id                        bigint not null,
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
  id                        bigint not null,
  libelle                   varchar(50),
  duree_minutes             integer,
  description               varchar(255),
  nb_instance               integer,
  event_id                  bigint,
  constraint uq_creneau_libelle unique (libelle),
  constraint pk_creneau primary key (id))
;

create table dynamic_field (
  id                        bigint not null,
  name                      varchar(50),
  constraint uq_dynamic_field_name unique (name),
  constraint pk_dynamic_field primary key (id))
;

create table dynamic_field_value (
  id                        bigint not null,
  value                     varchar(255),
  dynamic_field_id          bigint,
  user_id                   bigint,
  constraint pk_dynamic_field_value primary key (id))
;

create table event (
  id                        bigint not null,
  name                      varchar(50),
  description               varchar(1000),
  clos                      boolean,
  constraint uq_event_name unique (name),
  constraint pk_event primary key (id))
;

create table lien (
  id                        bigint not null,
  user_id                   bigint not null,
  label                     varchar(50),
  url                       varchar(200),
  constraint pk_lien primary key (id))
;

create table tag (
  id                        bigint not null,
  nom                       varchar(255),
  constraint uq_tag_nom unique (nom),
  constraint pk_tag primary key (id))
;

create table proposal (
  id                        bigint not null,
  title                     varchar(50),
  description               varchar(2000),
  indications_organisateurs varchar(1000),
  speaker_id                bigint,
  draft                     boolean,
  event_id                  bigint,
  status_proposal               varchar(1),
  format_id         bigint,
  duree_approuve_id         bigint,
  constraint ck_proposal_status_proposal check (status_proposal in ('A','W','R')),
  constraint uq_proposal_title unique (title),
  constraint pk_proposal primary key (id))
;

create table user (
  id                        bigint not null,
  email                     varchar(255),
  fullname                  varchar(255),
  authentication_method     varchar(255),
  credentials_id            bigint,
  date_creation             timestamp,
  admin                     boolean,
  notif_on_my_proposal          boolean,
  notif_admin_on_all_proposal   boolean,
  notif_admin_on_proposal_with_comment boolean,
  adresse_mac               varchar(255),
  description               varchar(2000),
  avatar                    varchar(255),
  constraint uq_user_email unique (email),
  constraint pk_user primary key (id))
;

create table vote (
  id                        bigint not null,
  user_id                   bigint,
  proposal_id                   bigint,
  note                      integer,
  constraint pk_vote primary key (id))
;

create table vote_status (
  id                        bigint not null,
  status                    integer,
  constraint ck_vote_status_status check (status in (0,1,2)),
  constraint pk_vote_status primary key (id))
;


create table creneau_proposal (
  creneau_id                     bigint not null,
  proposal_id                        bigint not null,
  constraint pk_creneau_proposal primary key (creneau_id, proposal_id))
;

create table tag_proposal (
  tag_id                         bigint not null,
  proposal_id                        bigint not null,
  constraint pk_tag_proposal primary key (tag_id, proposal_id))
;

create table user_proposal (
  user_id                        bigint not null,
  proposal_id                        bigint not null,
  constraint pk_user_proposal primary key (user_id, proposal_id))
;
create sequence comment_seq;

create sequence credentials_seq;

create sequence creneau_seq;

create sequence dynamic_field_seq;

create sequence dynamic_field_value_seq;

create sequence event_seq;

create sequence lien_seq;

create sequence tag_seq;

create sequence proposal_seq;

create sequence user_seq;

create sequence vote_seq;

create sequence vote_status_seq;

alter table comment add constraint fk_comment_author_1 foreign key (author_id) references user (id) on delete restrict on update restrict;
create index ix_comment_author_1 on comment (author_id);
alter table comment add constraint fk_comment_proposal_2 foreign key (proposal_id) references proposal (id) on delete restrict on update restrict;
create index ix_comment_proposal_2 on comment (proposal_id);
alter table comment add constraint fk_comment_question_3 foreign key (question_id) references comment (id) on delete restrict on update restrict;
create index ix_comment_question_3 on comment (question_id);
alter table credentials add constraint fk_credentials_user_4 foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_credentials_user_4 on credentials (user_id);
alter table creneau add constraint fk_creneau_event_5 foreign key (event_id) references event (id) on delete restrict on update restrict;
create index ix_creneau_event_5 on creneau (event_id);
alter table dynamic_field_value add constraint fk_dynamic_field_value_dynamic_6 foreign key (dynamic_field_id) references dynamic_field (id) on delete restrict on update restrict;
create index ix_dynamic_field_value_dynamic_6 on dynamic_field_value (dynamic_field_id);
alter table dynamic_field_value add constraint fk_dynamic_field_value_user_7 foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_dynamic_field_value_user_7 on dynamic_field_value (user_id);
alter table lien add constraint fk_lien_user_8 foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_lien_user_8 on lien (user_id);
alter table proposal add constraint fk_proposal_speaker_9 foreign key (speaker_id) references user (id) on delete restrict on update restrict;
create index ix_proposal_speaker_9 on proposal (speaker_id);
alter table proposal add constraint fk_proposal_event_10 foreign key (event_id) references event (id) on delete restrict on update restrict;
create index ix_proposal_event_10 on proposal (event_id);
alter table proposal add constraint fk_proposal_format_11 foreign key (format_id) references creneau (id) on delete restrict on update restrict;
create index ix_proposal_format_11 on proposal (format_id);
alter table user add constraint fk_user_credentials_13 foreign key (credentials_id) references credentials (id) on delete restrict on update restrict;
create index ix_user_credentials_13 on user (credentials_id);
alter table vote add constraint fk_vote_user_14 foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_vote_user_14 on vote (user_id);
alter table vote add constraint fk_vote_proposal_15 foreign key (proposal_id) references proposal (id) on delete restrict on update restrict;
create index ix_vote_proposal_15 on vote (proposal_id);



alter table creneau_proposal add constraint fk_creneau_proposal_creneau_01 foreign key (creneau_id) references creneau (id) on delete restrict on update restrict;

alter table creneau_proposal add constraint fk_creneau_proposal_proposal_02 foreign key (proposal_id) references proposal (id) on delete restrict on update restrict;

alter table tag_proposal add constraint fk_tag_proposal_tag_01 foreign key (tag_id) references tag (id) on delete restrict on update restrict;

alter table tag_proposal add constraint fk_tag_proposal_proposal_02 foreign key (proposal_id) references proposal (id) on delete restrict on update restrict;

alter table user_proposal add constraint fk_user_proposal_user_01 foreign key (user_id) references user (id) on delete restrict on update restrict;

alter table user_proposal add constraint fk_user_proposal_proposal_02 foreign key (proposal_id) references proposal (id) on delete restrict on update restrict;

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists comment;

drop table if exists credentials;

drop table if exists creneau;

drop table if exists creneau_proposal;

drop table if exists dynamic_field;

drop table if exists dynamic_field_value;

drop table if exists event;

drop table if exists lien;

drop table if exists tag;

drop table if exists tag_proposal;

drop table if exists proposal;

drop table if exists user_proposal;

drop table if exists user;

drop table if exists vote;

drop table if exists vote_status;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists comment_seq;

drop sequence if exists credentials_seq;

drop sequence if exists creneau_seq;

drop sequence if exists dynamic_field_seq;

drop sequence if exists dynamic_field_value_seq;

drop sequence if exists event_seq;

drop sequence if exists lien_seq;

drop sequence if exists tag_seq;

drop sequence if exists proposal_seq;

drop sequence if exists user_seq;

drop sequence if exists vote_seq;

drop sequence if exists vote_status_seq;

