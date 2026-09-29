create table widerspruch (
    id uuid not null,
    datum_eingang date not null,
    datum_ruecknahme date,
    datum_vorlage_regierung date,
    datum_ablehnung_regierung date,
    entscheidung_durchfuehrung varchar(255),
    soll_abgesetzt boolean not null,
    neue_teilabrechnung_anlegen boolean not null,
    bemerkung varchar(10000),
    primary key (id)
);

alter table abrechnung
    add column widerspruch_id uuid;

alter table abrechnung
    add constraint fk_abrechnung__widerspruch_id
    foreign key (widerspruch_id) references widerspruch (id);

alter table abrechnung
    add constraint uq_abrechnung__widerspruch_id
    unique (widerspruch_id);
