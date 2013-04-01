# --- !Ups
alter table talk add duree_approuve_id bigint;


# --- !Downs

ALTER TABLE talk DROP COLUMN duree_approuve_id;