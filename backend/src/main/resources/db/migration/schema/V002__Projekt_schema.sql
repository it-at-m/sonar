create table projekt (
    id uuid not null,
    projektnummer varchar(20) not null,
    abrechnung_beginn date not null,
    abrechnung_ende date not null,
    primary key (id)
);

create table projekt_adresse (
    id uuid not null,
    projekt_id uuid not null,
    sort_order integer not null,
    bezeichnung varchar(255) not null,
    baunutzung varchar(255),
    unerlaubte_nutzung_von date,
    unerlaubte_nutzung_bis date,
    tage_unerlaubte_nutzung integer,
    anzahl_mahnungen integer not null,
    sondernutzung_erlaubt boolean not null,
    primary key (id)
);

alter table projekt_adresse
    add constraint fk_projekt_adresse__projekt_id
    foreign key (projekt_id) references projekt (id);
