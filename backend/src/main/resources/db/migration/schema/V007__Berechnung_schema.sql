alter table projekt
    add column projektname varchar(255);

alter table abrechnung
    add column verwaltungsgebuehr numeric(12, 2);

alter table abrechnung_position
    add column bezeichnung varchar(255);
