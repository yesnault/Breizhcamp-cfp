
# --- !Ups
alter table talk add status_talk integer;
alter table talk add constraint ck_talk_status_talk check (status_talk in (0,1,2));


create table tag (
id                        bigint auto_increment not null,
nom                       varchar(255),
constraint uq_tag_nom unique (nom),
constraint pk_tag primary key (id))
;

create table tag_talk (
tag_id                         bigint not null,
talk_id                        bigint not null,
constraint pk_tag_talk primary key (tag_id, talk_id))
;


alter table tag_talk add constraint fk_tag_talk_tag_01 foreign key (tag_id) references tag (id) on delete restrict on update restrict;

alter table tag_talk add constraint fk_tag_talk_talk_02 foreign key (talk_id) references talk (id) on delete restrict on update restrict;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists tag_talk;

drop table if exists tag;


SET REFERENTIAL_INTEGRITY TRUE;

alter table talk delete status_talk;
alter table talk delete constraint ck_talk_status_talk;