# --- !Ups
create table vote_status (
  id                        bigint not null,
  status                    integer,
  constraint pk_vote_status primary key (id),
  constraint ck_vote_status_status check (status in (0,1,2)));

create table vote (
  id                        bigint not null,
  note                      integer,
  user_id                   bigint,
  talk_id                   bigint,
  constraint pk_vote        primary key (id)
);

create sequence vote_status_seq;

create sequence vote_seq;

alter table vote
  add constraint fk_vote_user
  foreign key (user_id)
  references user (id)
  on delete restrict
  on update restrict;

create index ix_vote_user on vote (user_id);

alter table vote
  add constraint fk_vote_talk
  foreign key (talk_id)
  references talk (id)
  on delete restrict
  on update restrict;

create index ix_vote_talk on vote (talk_id);

# --- !Downs
drop table if exists vote_status;

drop table if exists vote;

drop sequence if exists vote_status_seq;

drop sequence if exists vote_seq;
