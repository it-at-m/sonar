create table abrechnung (
    id uuid not null,
    projekt_id uuid not null,
    geschaeftspartner_id varchar(10) not null,
    zustellungsbevollmaechtigter_genutzt boolean not null,
    zustellungsbevollmaechtigter_id varchar(10),
    zustellungsbevollmaechtigter_typ varchar(50),
    zeitraum_von date not null,
    zeitraum_bis date not null,
    abrechnungs_art varchar(50) not null,
    primary key (id)
);

create table abrechnung_nutzungsobjekt (
    id uuid not null,
    abrechnung_id uuid not null,
    sort_order integer not null,
    art varchar(50) not null,
    adresse varchar(255),
    hausnummer_von varchar(20),
    hausnummer_bis varchar(20),
    flurstueck varchar(255),
    gemarkung varchar(255),
    nutzung varchar(50),
    unerlaubte_nutzung_von date,
    unerlaubte_nutzung_bis date,
    tage_unerlaubte_nutzung integer,
    bemerkung varchar(10000),
    primary key (id)
);

create table abrechnung_position (
    id uuid not null,
    nutzungsobjekt_id uuid not null,
    sort_order integer not null,
    beginn date not null,
    ende date not null,
    laenge numeric(12, 2) not null,
    breite numeric(12, 2) not null,
    flaeche numeric(12, 2) not null,
    haelfte boolean not null,
    anteil_an_flaeche numeric(12, 2) not null,
    primary key (id)
);

alter table abrechnung
    add constraint fk_abrechnung__projekt_id
    foreign key (projekt_id) references projekt (id);

alter table abrechnung_nutzungsobjekt
    add constraint fk_abrechnung_nutzungsobjekt__abrechnung_id
    foreign key (abrechnung_id) references abrechnung (id);

alter table abrechnung_position
    add constraint fk_abrechnung_position__nutzungsobjekt_id
    foreign key (nutzungsobjekt_id) references abrechnung_nutzungsobjekt (id);
