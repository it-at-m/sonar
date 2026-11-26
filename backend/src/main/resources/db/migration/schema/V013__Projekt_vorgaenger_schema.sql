alter table projekt
    add column vorgaenger_projekt_id uuid;

alter table projekt
    add constraint fk_projekt__vorgaenger_projekt_id
    foreign key (vorgaenger_projekt_id) references projekt (id);

alter table projekt
    add constraint uq_projekt__vorgaenger_projekt_id
    unique (vorgaenger_projekt_id);

alter table projekt
    add column verknuepfung_abgelehnt boolean not null default false;
