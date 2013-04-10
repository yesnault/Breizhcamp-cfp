# --- !Ups
create table user_talk (
  user_id                        bigint not null,
  talk_id                        bigint not null,
  constraint pk_user_talk primary key (user_id, talk_id))
;

alter table user_talk add constraint fk_user_talk_user_01 foreign key (user_id) references user (id) on delete restrict on update restrict;

alter table user_talk add constraint fk_user_talk_talk_02 foreign key (talk_id) references talk (id) on delete restrict on update restrict;


# --- !Downs

drop table if exists user_talk;