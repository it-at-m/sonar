alter table abrechnung_nutzungsobjekt
    rename to nutzungsobjekt;

alter table nutzungsobjekt
    rename constraint abrechnung_nutzungsobjekt_pkey to nutzungsobjekt_pkey;

create table abrechnung_nutzungsobjekt (
    abrechnung_id uuid not null,
    nutzungsobjekt_id uuid not null,
    sort_order integer not null,
    primary key (abrechnung_id, sort_order)
);

insert into abrechnung_nutzungsobjekt (abrechnung_id, nutzungsobjekt_id, sort_order)
select abrechnung_id, id, sort_order
  from nutzungsobjekt;

alter table nutzungsobjekt
    drop column abrechnung_id,
    drop column sort_order;

alter table abrechnung_nutzungsobjekt
    add constraint fk_abrechnung_nutzungsobjekt__abrechnung_id
    foreign key (abrechnung_id) references abrechnung (id);

alter table abrechnung_nutzungsobjekt
    add constraint fk_abrechnung_nutzungsobjekt__nutzungsobjekt_id
    foreign key (nutzungsobjekt_id) references nutzungsobjekt (id);

alter table abrechnung_nutzungsobjekt
    add constraint uq_abrechnung_nutzungsobjekt__nutzungsobjekt_id
    unique (abrechnung_id, nutzungsobjekt_id);

alter table abrechnung_position
    add column abrechnung_id uuid;

update abrechnung_position
   set abrechnung_id = abrechnung_nutzungsobjekt.abrechnung_id
  from abrechnung_nutzungsobjekt
 where abrechnung_nutzungsobjekt.nutzungsobjekt_id = abrechnung_position.nutzungsobjekt_id;

alter table abrechnung_position
    alter column abrechnung_id set not null;

with reihenfolge_je_abrechnung as (
    select abrechnung_position.id as position_id,
           row_number() over (
               partition by abrechnung_position.abrechnung_id
               order by abrechnung_nutzungsobjekt.sort_order, abrechnung_position.sort_order
           ) - 1 as sort_order
      from abrechnung_position
      join abrechnung_nutzungsobjekt
        on abrechnung_nutzungsobjekt.abrechnung_id = abrechnung_position.abrechnung_id
       and abrechnung_nutzungsobjekt.nutzungsobjekt_id = abrechnung_position.nutzungsobjekt_id
)
update abrechnung_position
   set sort_order = reihenfolge_je_abrechnung.sort_order
  from reihenfolge_je_abrechnung
 where reihenfolge_je_abrechnung.position_id = abrechnung_position.id;

alter table abrechnung_position
    add constraint fk_abrechnung_position__abrechnung_id
    foreign key (abrechnung_id) references abrechnung (id);
