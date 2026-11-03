alter table projekt
    add column stadtbezirk varchar(100);

update projekt
   set stadtbezirk = 'ALTSTADT_LEHEL';

alter table projekt
    alter column stadtbezirk set not null;

create table stadtbezirk_rolle (
    id uuid not null,
    stadtbezirk varchar(100) not null,
    rolle varchar(255) not null,
    primary key (id)
);

alter table stadtbezirk_rolle
    add constraint uq_stadtbezirk_rolle__stadtbezirk_rolle
    unique (stadtbezirk, rolle);

insert into stadtbezirk_rolle (id, stadtbezirk, rolle) values
    (gen_random_uuid(), 'ALTSTADT_LEHEL', 'SB_Mitte'),
    (gen_random_uuid(), 'LUDWIGSVORSTADT_ISARVORSTADT', 'SB_Mitte'),
    (gen_random_uuid(), 'MAXVORSTADT', 'SB_Mitte'),
    (gen_random_uuid(), 'SCHWABING_WEST', 'SB_Nord'),
    (gen_random_uuid(), 'AU_HAIDHAUSEN', 'SB_Mitte'),
    (gen_random_uuid(), 'SENDLING', 'SB_Sued'),
    (gen_random_uuid(), 'SENDLING_WESTPARK', 'SB_Sued'),
    (gen_random_uuid(), 'SCHWANTHALERHOEHE', 'SB_Mitte'),
    (gen_random_uuid(), 'NEUHAUSEN_NYMPHENBURG', 'SB_West'),
    (gen_random_uuid(), 'MOOSACH', 'SB_Nord'),
    (gen_random_uuid(), 'MILBERTSHOFEN_AM_HART', 'SB_Nord'),
    (gen_random_uuid(), 'SCHWABING_FREIMANN', 'SB_Nord'),
    (gen_random_uuid(), 'BOGENHAUSEN', 'SB_Ost'),
    (gen_random_uuid(), 'BERG_AM_LAIM', 'SB_Ost'),
    (gen_random_uuid(), 'TRUDERING_RIEM', 'SB_Ost'),
    (gen_random_uuid(), 'RAMERSDORF_PERLACH', 'SB_Ost'),
    (gen_random_uuid(), 'OBERGIESING_FASANGARTEN', 'SB_Ost'),
    (gen_random_uuid(), 'UNTERGIESING_HARLACHING', 'SB_Sued'),
    (gen_random_uuid(), 'THALKIRCHEN_OBERSENDLING_FORSTENRIED_FUERSTENRIED_SOLLN', 'SB_Sued'),
    (gen_random_uuid(), 'HADERN', 'SB_Sued'),
    (gen_random_uuid(), 'PASING_OBERMENZING', 'SB_West'),
    (gen_random_uuid(), 'AUBING_LOCHHAUSEN_LANGWIED', 'SB_West'),
    (gen_random_uuid(), 'ALLACH_UNTERMENZING', 'SB_West'),
    (gen_random_uuid(), 'FELDMOCHING_HASENBERGL', 'SB_Nord'),
    (gen_random_uuid(), 'LAIM', 'SB_West');
