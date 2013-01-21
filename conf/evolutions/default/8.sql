# --- !Ups
alter table comment add question_id bigint;
# --- !Downs
alter table comment delete question_id;

