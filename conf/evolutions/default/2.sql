
# --- !Ups

create table tag (
id                        bigint not null,
nom                       varchar(255),
constraint uq_tag_nom unique (nom),
constraint pk_tag primary key (id))
;

create table tag_talk (
tag_id                         bigint not null,
talk_id                        bigint not null,
constraint pk_tag_talk primary key (tag_id, talk_id))
;


create sequence tag_seq;

alter table tag_talk add constraint fk_tag_talk_tag_01 foreign key (tag_id) references tag (id) on delete restrict on update restrict;

alter table tag_talk add constraint fk_tag_talk_talk_02 foreign key (talk_id) references talk (id) on delete restrict on update restrict;


#; --- !Downs