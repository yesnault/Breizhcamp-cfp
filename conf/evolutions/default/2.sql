# --- !Ups
alter table creneau add nb_instance integer;


# --- !Downs

ALTER TABLE creneau DROP COLUMN nb_instance;