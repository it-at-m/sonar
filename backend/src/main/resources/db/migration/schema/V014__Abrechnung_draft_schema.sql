alter table abrechnung
    add column draft boolean;

update abrechnung
   set draft = false;

alter table abrechnung
    alter column draft set not null;

alter table abrechnung_nutzungsobjekt
    drop constraint uq_abrechnung_nutzungsobjekt__nutzungsobjekt_id;

alter table abrechnung_nutzungsobjekt
    add constraint uq_abrechnung_nutzungsobjekt__nutzungsobjekt_id
    unique (abrechnung_id, nutzungsobjekt_id) deferrable initially deferred;
