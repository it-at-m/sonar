alter table projekt_adresse
    add column nutzung_sonstiges varchar(255);

alter table abrechnung_nutzungsobjekt
    add column nutzung_sonstiges varchar(255);

create table projekt_adresse_nutzung (
    projekt_adresse_id uuid not null,
    nutzung varchar(50) not null,
    primary key (projekt_adresse_id, nutzung)
);

create table abrechnung_nutzungsobjekt_nutzung (
    nutzungsobjekt_id uuid not null,
    nutzung varchar(50) not null,
    primary key (nutzungsobjekt_id, nutzung)
);

alter table projekt_adresse_nutzung
    add constraint fk_projekt_adresse_nutzung__projekt_adresse_id
    foreign key (projekt_adresse_id) references projekt_adresse (id);

alter table abrechnung_nutzungsobjekt_nutzung
    add constraint fk_abrechnung_nutzungsobjekt_nutzung__nutzungsobjekt_id
    foreign key (nutzungsobjekt_id) references abrechnung_nutzungsobjekt (id);

alter table projekt_adresse
    drop column nutzung;

alter table abrechnung_nutzungsobjekt
    drop column nutzung;
