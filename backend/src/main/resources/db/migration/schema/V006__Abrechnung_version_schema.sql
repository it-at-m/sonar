alter table abrechnung
    add column versionsnummer integer,
    add column vorgaenger_abrechnung_id uuid;

update abrechnung
   set versionsnummer = 1;

alter table abrechnung
    alter column versionsnummer set not null;

alter table abrechnung
    add constraint fk_abrechnung__vorgaenger_abrechnung_id
    foreign key (vorgaenger_abrechnung_id) references abrechnung (id);

alter table abrechnung
    add constraint uq_abrechnung__vorgaenger_abrechnung_id
    unique (vorgaenger_abrechnung_id);
