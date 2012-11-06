# --- !Ups
create table creneau (
  id                        bigint not null,
  libelle                   varchar(50),
  duree_minutes             integer,
  constraint pk_creneau primary key (id),
  constraint uq_creneau_libelle unique (libelle)
  );

create sequence creneau_seq;

# --- !Downs
drop table if exists creneau;

drop sequence if exists creneau_seq;

