alter table berechnung
    add column zusammenfassung text not null default '',
    add column berechnungslog text not null default '';

alter table berechnung
    alter column zusammenfassung drop default,
    alter column berechnungslog drop default;

create table berechnung_bescheiddaten_flaeche (
    id uuid not null,
    berechnung_id uuid not null,
    sort_order integer not null,
    woche_beginn integer not null,
    woche_ende integer not null,
    woche_dauer integer not null,
    flaechen_verteilung text not null,
    grund text not null,
    gebuehrenstufe text not null,
    berechnung text not null,
    gebuehr numeric(12, 2) not null,
    primary key (id)
);

create table berechnung_ueberspannung (
    id uuid not null,
    berechnung_id uuid not null,
    sort_order integer not null,
    adresse text not null,
    berechnungslog text not null,
    gebuehr_ueberspannungen numeric(12, 2) not null,
    primary key (id)
);

create table berechnung_bescheiddaten_ueberspannung (
    id uuid not null,
    ueberspannung_id uuid not null,
    sort_order integer not null,
    monat_beginn integer not null,
    monat_ende integer not null,
    monat_dauer integer not null,
    grund text not null,
    berechnung text not null,
    gebuehr numeric(12, 2) not null,
    primary key (id)
);

alter table berechnung_bescheiddaten_flaeche
    add constraint fk_berechnung_bescheiddaten_flaeche__berechnung_id
    foreign key (berechnung_id) references berechnung (id);

alter table berechnung_ueberspannung
    add constraint fk_berechnung_ueberspannung__berechnung_id
    foreign key (berechnung_id) references berechnung (id);

alter table berechnung_bescheiddaten_ueberspannung
    add constraint fk_berechnung_bescheiddaten_ueberspannung__ueberspannung_id
    foreign key (ueberspannung_id) references berechnung_ueberspannung (id);
