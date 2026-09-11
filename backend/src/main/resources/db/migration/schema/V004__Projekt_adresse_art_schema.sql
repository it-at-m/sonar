alter table projekt_adresse
    add column art varchar(50),
    add column adresse varchar(255),
    add column hausnummer_von varchar(20),
    add column hausnummer_bis varchar(20),
    add column flurstueck varchar(255),
    add column gemarkung varchar(255),
    add column nutzung varchar(50);

-- A free-text bezeichnung may have held a Flurstück. Which one it was is not knowable, so every
-- carried row becomes an Adresse. A free-text baunutzung has no counterpart among the Nutzung
-- constants and is dropped with the column.
update projekt_adresse
   set art = 'ADRESSE',
       adresse = bezeichnung;

alter table projekt_adresse
    alter column art set not null;

alter table projekt_adresse
    drop column bezeichnung,
    drop column baunutzung;
