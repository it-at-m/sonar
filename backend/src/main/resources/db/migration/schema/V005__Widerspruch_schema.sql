create table widerspruch (
    id uuid not null,
    abrechnung_id uuid not null,
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

alter table widerspruch
    add constraint fk_widerspruch__abrechnung_id
    foreign key (abrechnung_id) references abrechnung (id);

alter table widerspruch
    add constraint uq_widerspruch__abrechnung_id
    unique (abrechnung_id);
