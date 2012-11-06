# --- !Ups
alter table comment add private_comment boolean;

# --- !Downs
alter table comment delete private_comment;
