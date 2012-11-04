# --- !Ups
alter table user add adresse_mac  varchar(17);

# --- !Downs
alter table user delete adresse_mac;