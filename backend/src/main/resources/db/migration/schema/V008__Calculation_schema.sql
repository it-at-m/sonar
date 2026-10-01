create table calculation (
    id uuid not null,
    abrechnung_id uuid not null,
    lfd_nr integer not null,
    abrechnungszeitraum_von date not null,
    abrechnungszeitraum_bis date not null,
    gebuehr_flaechen numeric(12, 2) not null,
    gebuehr_ueberspannungen numeric(12, 2) not null,
    gebuehr_verwaltung numeric(12, 2) not null,
    gebuehr_gesamt numeric(12, 2) not null,
    gebuehr_zahlung numeric(12, 2) not null,
    primary key (id)
);

alter table calculation
    add constraint fk_calculation__abrechnung_id
    foreign key (abrechnung_id) references abrechnung (id);

alter table calculation
    add constraint uq_calculation__abrechnung_id__lfd_nr
    unique (abrechnung_id, lfd_nr);
