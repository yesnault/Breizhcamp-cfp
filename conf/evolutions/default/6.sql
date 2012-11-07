# --- !Ups
create table creneau (
  id                        bigint not null,
  libelle                   varchar(50),
  duree_minutes             integer,
  constraint pk_creneau primary key (id),
  constraint uq_creneau_libelle unique (libelle)
  );

create table creneau_talk (
  creneau_id                     bigint not null,
  talk_id                        bigint not null,
  constraint pk_creneau_talk primary key (creneau_id, talk_id))
;

create sequence creneau_seq;

alter table creneau_talk add constraint fk_creneau_talk_creneau_01 foreign key (creneau_id) references creneau (id) on delete restrict on update restrict;

alter table creneau_talk add constraint fk_creneau_talk_talk_02 foreign key (talk_id) references talk (id) on delete restrict on update restrict;

# --- !Downs
drop table if exists creneau;

drop table if exists creneau_talk;

drop sequence if exists creneau_seq;

