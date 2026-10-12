create table abrechnung_mast (
    id uuid not null,
    nutzungsobjekt_id uuid not null,
    sort_order integer not null,
    beginn date not null,
    ende date not null,
    primary key (id)
);

alter table abrechnung_mast
    add constraint fk_abrechnung_mast__nutzungsobjekt_id
    foreign key (nutzungsobjekt_id) references abrechnung_nutzungsobjekt (id);
