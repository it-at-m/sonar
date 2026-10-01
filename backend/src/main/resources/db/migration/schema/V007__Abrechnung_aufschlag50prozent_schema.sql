alter table abrechnung_nutzungsobjekt
    add column aufschlag50prozent boolean not null;

alter table abrechnung_position
    drop column haelfte;
