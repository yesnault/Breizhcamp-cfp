# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table agenda (
  id                        bigint auto_increment not null,
  debut_cfp                 datetime,
  fin_cfp                   datetime,
  fin_vote                  datetime,
  constraint pk_agenda primary key (id))
;

create table comment (
  id                        bigint auto_increment not null,
  author_id                 bigint,
  date_creation             datetime,
  proposal_id               bigint,
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

create table event (
  id                        bigint auto_increment not null,
  name                      varchar(50),
  short_name                varchar(5),
  url                       varchar(200),
  description               varchar(1000),
  cgu                       varchar(1000),
  agenda_id                 bigint,
  constraint uq_event_name unique (name),
  constraint uq_event_short_name unique (short_name),
  constraint pk_event primary key (id))
;

create table link (
  id                        bigint auto_increment not null,
  user_id                   bigint not null,
  label                     varchar(50),
  url                       varchar(200),
  link_type                 integer,
  constraint ck_link_link_type check (link_type in (0,1,2,3,4,5,6,7)),
  constraint pk_link primary key (id))
;

create table proposal (
  id                        bigint auto_increment not null,
  title                     varchar(50),
  description               varchar(2000),
  indications_organisateurs varchar(1000),
  speaker_id                bigint,
  event_id                  bigint,
  status                    varchar(1),
  audience                  integer,
  format_id                 bigint,
  track_id                  bigint,
  constraint ck_proposal_status check (status in ('R','W','D','A','S')),
  constraint ck_proposal_audience check (audience in (0,1,2)),
  constraint uq_proposal_title unique (title),
  constraint pk_proposal primary key (id))
;

create table tag (
  id                        bigint auto_increment not null,
  nom                       varchar(255),
  constraint uq_tag_nom unique (nom),
  constraint pk_tag primary key (id))
;

create table talk_format (
  id                        bigint auto_increment not null,
  libelle                   varchar(50),
  duree_minutes             integer,
  description               varchar(255),
  nb_instance               integer,
  event_id                  bigint,
  constraint pk_talk_format primary key (id))
;

create table track (
  id                        bigint auto_increment not null,
  title                     varchar(50),
  short_title               varchar(5),
  description               varchar(1000),
  event_id                  bigint,
  constraint pk_track primary key (id))
;

create table user (
  id                        bigint auto_increment not null,
  email                     varchar(255),
  full_name                 varchar(255),
  date_creation             datetime,
  admin                     tinyint(1) default 0,
  notif_on_my_proposal      tinyint(1) default 0,
  notif_admin_on_all_proposal tinyint(1) default 0,
  notif_admin_on_proposal_with_comment tinyint(1) default 0,
  adresse_mac               varchar(255),
  description               varchar(2000),
  avatar                    varchar(255),
  constraint uq_user_email unique (email),
  constraint pk_user primary key (id))
;

create table vote (
  id                        bigint auto_increment not null,
  user_id                   bigint,
  proposal_id               bigint,
  note                      integer,
  constraint pk_vote primary key (id))
;

create table vote_status (
  id                        bigint auto_increment not null,
  status                    integer,
  constraint ck_vote_status_status check (status in (0,1,2)),
  constraint pk_vote_status primary key (id))
;


create table tag_proposal (
  tag_id                         bigint not null,
  proposal_id                    bigint not null,
  constraint pk_tag_proposal primary key (tag_id, proposal_id))
;

create table user_proposal (
  user_id                        bigint not null,
  proposal_id                    bigint not null,
  constraint pk_user_proposal primary key (user_id, proposal_id))
;

create table user_track (
  user_id                        bigint not null,
  track_id                       bigint not null,
  constraint pk_user_track primary key (user_id, track_id))
;

create table user_event (
  user_id                        bigint not null,
  event_id                       bigint not null,
  constraint pk_user_event primary key (user_id, event_id))
;
alter table comment add constraint fk_comment_author_1 foreign key (author_id) references user (id) on delete restrict on update restrict;
create index ix_comment_author_1 on comment (author_id);
alter table comment add constraint fk_comment_proposal_2 foreign key (proposal_id) references proposal (id) on delete restrict on update restrict;
create index ix_comment_proposal_2 on comment (proposal_id);
alter table comment add constraint fk_comment_question_3 foreign key (question_id) references comment (id) on delete restrict on update restrict;
create index ix_comment_question_3 on comment (question_id);
alter table credentials add constraint fk_credentials_user_4 foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_credentials_user_4 on credentials (user_id);
alter table dynamic_field_value add constraint fk_dynamic_field_value_dynamicField_5 foreign key (dynamic_field_id) references dynamic_field (id) on delete restrict on update restrict;
create index ix_dynamic_field_value_dynamicField_5 on dynamic_field_value (dynamic_field_id);
alter table dynamic_field_value add constraint fk_dynamic_field_value_user_6 foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_dynamic_field_value_user_6 on dynamic_field_value (user_id);
alter table event add constraint fk_event_agenda_7 foreign key (agenda_id) references agenda (id) on delete restrict on update restrict;
create index ix_event_agenda_7 on event (agenda_id);
alter table link add constraint fk_link_user_8 foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_link_user_8 on link (user_id);
alter table proposal add constraint fk_proposal_speaker_9 foreign key (speaker_id) references user (id) on delete restrict on update restrict;
create index ix_proposal_speaker_9 on proposal (speaker_id);
alter table proposal add constraint fk_proposal_event_10 foreign key (event_id) references event (id) on delete restrict on update restrict;
create index ix_proposal_event_10 on proposal (event_id);
alter table proposal add constraint fk_proposal_format_11 foreign key (format_id) references talk_format (id) on delete restrict on update restrict;
create index ix_proposal_format_11 on proposal (format_id);
alter table proposal add constraint fk_proposal_track_12 foreign key (track_id) references track (id) on delete restrict on update restrict;
create index ix_proposal_track_12 on proposal (track_id);
alter table talk_format add constraint fk_talk_format_event_13 foreign key (event_id) references event (id) on delete restrict on update restrict;
create index ix_talk_format_event_13 on talk_format (event_id);
alter table track add constraint fk_track_event_14 foreign key (event_id) references event (id) on delete restrict on update restrict;
create index ix_track_event_14 on track (event_id);
alter table vote add constraint fk_vote_user_15 foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_vote_user_15 on vote (user_id);
alter table vote add constraint fk_vote_proposal_16 foreign key (proposal_id) references proposal (id) on delete restrict on update restrict;
create index ix_vote_proposal_16 on vote (proposal_id);



alter table tag_proposal add constraint fk_tag_proposal_tag_01 foreign key (tag_id) references tag (id) on delete restrict on update restrict;

alter table tag_proposal add constraint fk_tag_proposal_proposal_02 foreign key (proposal_id) references proposal (id) on delete restrict on update restrict;

alter table user_proposal add constraint fk_user_proposal_user_01 foreign key (user_id) references user (id) on delete restrict on update restrict;

alter table user_proposal add constraint fk_user_proposal_proposal_02 foreign key (proposal_id) references proposal (id) on delete restrict on update restrict;

alter table user_track add constraint fk_user_track_user_01 foreign key (user_id) references user (id) on delete restrict on update restrict;

alter table user_track add constraint fk_user_track_track_02 foreign key (track_id) references track (id) on delete restrict on update restrict;

alter table user_event add constraint fk_user_event_user_01 foreign key (user_id) references user (id) on delete restrict on update restrict;

alter table user_event add constraint fk_user_event_event_02 foreign key (event_id) references event (id) on delete restrict on update restrict;

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table agenda;

drop table comment;

drop table credentials;

drop table dynamic_field;

drop table dynamic_field_value;

drop table event;

drop table user_event;

drop table link;

drop table proposal;

drop table user_proposal;

drop table tag_proposal;

drop table tag;

drop table talk_format;

drop table track;

drop table user_track;

drop table user;

drop table vote;

drop table vote_status;

SET FOREIGN_KEY_CHECKS=1;

