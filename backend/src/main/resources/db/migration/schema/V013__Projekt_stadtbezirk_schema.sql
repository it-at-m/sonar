alter table projekt
    add column stadtbezirk varchar(100);

update projekt
   set stadtbezirk = 'BEZIRK_01_ALTSTADT_LEHEL';

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
    (gen_random_uuid(), 'BEZIRK_01_ALTSTADT_LEHEL', 'SB_Mitte'),
    (gen_random_uuid(), 'BEZIRK_02_LUDWIGSVORSTADT_ISARVORSTADT', 'SB_Mitte'),
    (gen_random_uuid(), 'BEZIRK_03_MAXVORSTADT', 'SB_Mitte'),
    (gen_random_uuid(), 'BEZIRK_04_SCHWABING_WEST', 'SB_Nord'),
    (gen_random_uuid(), 'BEZIRK_05_AU_HAIDHAUSEN', 'SB_Mitte'),
    (gen_random_uuid(), 'BEZIRK_06_SENDLING', 'SB_Sued'),
    (gen_random_uuid(), 'BEZIRK_07_SENDLING_WESTPARK', 'SB_Sued'),
    (gen_random_uuid(), 'BEZIRK_08_SCHWANTHALERHOEHE', 'SB_Mitte'),
    (gen_random_uuid(), 'BEZIRK_09_NEUHAUSEN_NYMPHENBURG', 'SB_West'),
    (gen_random_uuid(), 'BEZIRK_10_MOOSACH', 'SB_Nord'),
    (gen_random_uuid(), 'BEZIRK_11_MILBERTSHOFEN_AM_HART', 'SB_Nord'),
    (gen_random_uuid(), 'BEZIRK_12_SCHWABING_FREIMANN', 'SB_Nord'),
    (gen_random_uuid(), 'BEZIRK_13_BOGENHAUSEN', 'SB_Ost'),
    (gen_random_uuid(), 'BEZIRK_14_BERG_AM_LAIM', 'SB_Ost'),
    (gen_random_uuid(), 'BEZIRK_15_TRUDERING_RIEM', 'SB_Ost'),
    (gen_random_uuid(), 'BEZIRK_16_RAMERSDORF_PERLACH', 'SB_Ost'),
    (gen_random_uuid(), 'BEZIRK_17_OBERGIESING_FASANGARTEN', 'SB_Ost'),
    (gen_random_uuid(), 'BEZIRK_18_UNTERGIESING_HARLACHING', 'SB_Sued'),
    (gen_random_uuid(), 'BEZIRK_19_THALKIRCHEN_OBERSENDLING_FORSTENRIED_FUERSTENRIED_SOLLN', 'SB_Sued'),
    (gen_random_uuid(), 'BEZIRK_20_HADERN', 'SB_Sued'),
    (gen_random_uuid(), 'BEZIRK_21_PASING_OBERMENZING', 'SB_West'),
    (gen_random_uuid(), 'BEZIRK_22_AUBING_LOCHHAUSEN_LANGWIED', 'SB_West'),
    (gen_random_uuid(), 'BEZIRK_23_ALLACH_UNTERMENZING', 'SB_West'),
    (gen_random_uuid(), 'BEZIRK_24_FELDMOCHING_HASENBERGL', 'SB_Nord'),
    (gen_random_uuid(), 'BEZIRK_25_LAIM', 'SB_West');
