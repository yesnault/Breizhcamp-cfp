# --- !Ups
create table vote_status (
  id                        bigint not null,
  status                    integer,
  constraint pk_vote_status primary key (id),
  constraint ck_vote_status_status check (status in (0,1,2)));

create sequence vote_status_seq;

# --- !Downs
drop table if exists vote_status;

drop sequence if exists vote_status_seq;
