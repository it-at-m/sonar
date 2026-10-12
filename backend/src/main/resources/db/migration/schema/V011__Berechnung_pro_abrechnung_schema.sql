delete from berechnung_bescheiddaten_ueberspannung
where ueberspannung_id in (
    select ueberspannung.id
    from berechnung_ueberspannung ueberspannung
    join berechnung veraltet on veraltet.id = ueberspannung.berechnung_id
    where veraltet.lfd_nr < (select max(neuer.lfd_nr) from berechnung neuer where neuer.abrechnung_id = veraltet.abrechnung_id)
);

delete from berechnung_ueberspannung
where berechnung_id in (
    select veraltet.id
    from berechnung veraltet
    where veraltet.lfd_nr < (select max(neuer.lfd_nr) from berechnung neuer where neuer.abrechnung_id = veraltet.abrechnung_id)
);

delete from berechnung_bescheiddaten_flaeche
where berechnung_id in (
    select veraltet.id
    from berechnung veraltet
    where veraltet.lfd_nr < (select max(neuer.lfd_nr) from berechnung neuer where neuer.abrechnung_id = veraltet.abrechnung_id)
);

delete from berechnung veraltet
where veraltet.lfd_nr < (select max(neuer.lfd_nr) from berechnung neuer where neuer.abrechnung_id = veraltet.abrechnung_id);

alter table berechnung
    drop constraint uq_berechnung__abrechnung_id__lfd_nr;

alter table berechnung
    add constraint uq_berechnung__abrechnung_id
    unique (abrechnung_id);
