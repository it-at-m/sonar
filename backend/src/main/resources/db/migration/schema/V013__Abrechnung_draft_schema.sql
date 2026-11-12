alter table abrechnung
    add column draft boolean;

update abrechnung
   set draft = false;

alter table abrechnung
    alter column draft set not null;
