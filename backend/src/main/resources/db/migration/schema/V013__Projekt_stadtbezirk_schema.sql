alter table projekt
    add column stadtbezirk varchar(100);

update projekt
   set stadtbezirk = 'BEZIRK_01_ALTSTADT_LEHEL';

alter table projekt
    alter column stadtbezirk set not null;
