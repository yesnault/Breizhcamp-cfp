# --- !Ups
create table dynamic_field (
  id                        bigint not null,
  name                      varchar(50),
  constraint pk_dynamic_field primary key (id),
  constraint uq_dynamic_field_name unique (name))
;

create table dynamic_field_value (
  id                        bigint not null,
  value                     varchar(255),
  dynamic_field_id          bigint,
  user_id                   bigint,
  constraint pk_dynamic_field_value primary key (id))
;

create sequence dynamic_field_seq;

create sequence dynamic_field_value_seq;

alter table dynamic_field_value add constraint fk_dynamic_field_value_dynamic_3 foreign key (dynamic_field_id) references dynamic_field (id) on delete restrict on update restrict;
create index ix_dynamic_field_value_dynamic_3 on dynamic_field_value (dynamic_field_id);
alter table dynamic_field_value add constraint fk_dynamic_field_value_user_4 foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_dynamic_field_value_user_4 on dynamic_field_value (user_id);

# --- !Downs
drop table if exists dynamic_field;

drop table if exists dynamic_field_value;

drop sequence if exists dynamic_field_seq;

drop sequence if exists dynamic_field_value_seq;

