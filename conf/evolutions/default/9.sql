# --- !Ups
alter table talk add duree_preferee_id bigint;
# --- !Downs
alter table talk delete duree_preferee_id;

