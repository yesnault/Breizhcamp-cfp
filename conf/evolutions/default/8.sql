# --- !Ups
alter table comment add question_id bigint;
alter table comment add clos boolean;
# --- !Downs
alter table comment delete question_id;
alter table comment delete boolean;

