How to load OSM data into SOLR
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

1. start with OSM data, so download them and place them somewhere

2. load those data to PostgreSQL using osm2pgrouting

3. load Municipality shape file:
        shp2pgsql -s 3003:4326 -c -I GIA_COMUNI.shp

4. extract features from ways:
        create table features as 
        select 'OSM_' || coalesce(codcom, 'OUTSIDE') || '_' || osm_id id, 
            trim(replace(replace(replace(name, '&', '&amp;'), '>', '&gt;'), '<', '&lt;'))::character varying as name, 
            st_collect(the_geom) centerline, 
            st_pointonsurface(st_collect(the_geom)) centroid, 
            st_envelope(st_collect(the_geom)) bounding_box,
            coalesce(c.nome, 'UNKNOWN') municipality, CASE 
                WHEN codprov = '045' THEN 'MS'
                WHEN codprov = '046' THEN 'LU'
                WHEN codprov = '048' THEN 'FI'
                WHEN codprov = '047' THEN 'PT'
                WHEN codprov = '100' THEN 'PO'
                WHEN codprov = '051' THEN 'AR'
                WHEN codprov = '050' THEN 'PI'
                WHEN codprov = '052' THEN 'SI'
                WHEN codprov = '049' THEN 'LI'
                WHEN codprov = '053' THEN 'GR'
                ELSE 'XX'
            END country_subdivision
        from ways w LEFT JOIN am_com_region_am_com c ON w.the_geom && c.geom and st_intersects(w.the_geom, c.geom) = 't'
        where name is not null and name <> ''
        group by id, name, municipality, country_subdivision;
        
        create table dug (text character varying(50));
        INSERT INTO dug (text) VALUES ('NUOVA');
        INSERT INTO dug (text) VALUES ('STRADELLO');
        INSERT INTO dug (text) VALUES ('RIPA');
        INSERT INTO dug (text) VALUES ('CANTO');
        INSERT INTO dug (text) VALUES ('SOBBORGO');
        INSERT INTO dug (text) VALUES ('CORSO');
        INSERT INTO dug (text) VALUES ('PONTE');
        INSERT INTO dug (text) VALUES ('S.V.');
        INSERT INTO dug (text) VALUES ('CHIASSO');
        INSERT INTO dug (text) VALUES ('PIAZZETTA');
        INSERT INTO dug (text) VALUES ('PIAZZALE');
        INSERT INTO dug (text) VALUES ('SENZA');
        INSERT INTO dug (text) VALUES ('LARGO');
        INSERT INTO dug (text) VALUES ('VIOTTOLO');
        INSERT INTO dug (text) VALUES ('VOLTA');
        INSERT INTO dug (text) VALUES ('VIADOTTO');
        INSERT INTO dug (text) VALUES ('ERTA');
        INSERT INTO dug (text) VALUES ('VIUZZO');
        INSERT INTO dug (text) VALUES ('VIA');
        INSERT INTO dug (text) VALUES ('BORGO');
        INSERT INTO dug (text) VALUES ('PIAZZA');
        INSERT INTO dug (text) VALUES ('LOCALITA''');
        INSERT INTO dug (text) VALUES ('RAMPA');
        INSERT INTO dug (text) VALUES ('LUNGARNO');
        INSERT INTO dug (text) VALUES ('VIALETTO');
        INSERT INTO dug (text) VALUES ('VICOLO');
        INSERT INTO dug (text) VALUES ('VIALE');
        INSERT INTO dug (text) VALUES ('COSTA');
        INSERT INTO dug (text) VALUES ('SALITA');
        INSERT INTO dug (text) VALUES ('C.S.');
        INSERT INTO dug (text) VALUES ('C.S.O.');
        INSERT INTO dug (text) VALUES ('CAVALCAFERROVIA');
        INSERT INTO dug (text) VALUES ('S.G.C.');
        INSERT INTO dug (text) VALUES ('RACCORDO AUTOSTRADALE');
        INSERT INTO dug (text) VALUES ('PARCO');
        INSERT INTO dug (text) VALUES ('NUCLEO');
        INSERT INTO dug (text) VALUES ('GALLERIA');
        INSERT INTO dug (text) VALUES ('SDRUCCIOLO');
        INSERT INTO dug (text) VALUES ('SENTIERO');
        INSERT INTO dug (text) VALUES ('EX S.V.');
        INSERT INTO dug (text) VALUES ('S.C.');
        INSERT INTO dug (text) VALUES ('S.S.');
        INSERT INTO dug (text) VALUES ('C.A.');
        INSERT INTO dug (text) VALUES ('C.S.T.');
        INSERT INTO dug (text) VALUES ('S.V');
        INSERT INTO dug (text) VALUES ('CIRCONVALLAZIONE');
        INSERT INTO dug (text) VALUES ('GIARDINO PUBBLICO');
        INSERT INTO dug (text) VALUES ('STRETTO');
        INSERT INTO dug (text) VALUES ('TRAVERSA VIA');
        INSERT INTO dug (text) VALUES ('EX S.S.');
        INSERT INTO dug (text) VALUES ('EX S.R.');
        INSERT INTO dug (text) VALUES ('S.D.');
        INSERT INTO dug (text) VALUES ('SCALE');
        INSERT INTO dug (text) VALUES ('AUTOSTRADA');
        INSERT INTO dug (text) VALUES ('PIAGGIA');
        INSERT INTO dug (text) VALUES ('S.R.');
        INSERT INTO dug (text) VALUES ('RACCORDO');
        INSERT INTO dug (text) VALUES ('VILLAGGIO');
        INSERT INTO dug (text) VALUES ('EX S.P.');
        INSERT INTO dug (text) VALUES ('PORTA');
        INSERT INTO dug (text) VALUES ('EX S.C.');
        INSERT INTO dug (text) VALUES ('STRADA');
        INSERT INTO dug (text) VALUES ('SPIAZZO');
        INSERT INTO dug (text) VALUES ('N.A.');
        INSERT INTO dug (text) VALUES ('C.S.R.');
        INSERT INTO dug (text) VALUES ('TRAVERSA');
        INSERT INTO dug (text) VALUES ('TANGENZIALE');
        INSERT INTO dug (text) VALUES ('ARCO');
        INSERT INTO dug (text) VALUES ('LOCALITA');
        INSERT INTO dug (text) VALUES ('S.P.');
        INSERT INTO dug (text) VALUES ('FRAZIONE');
        INSERT INTO dug (text) VALUES ('PODERE');
        INSERT INTO dug (text) VALUES ('PASSEGGIO');
        INSERT INTO dug (text) VALUES ('SCALA');
        INSERT INTO dug (text) VALUES ('STAZIONE');
        INSERT INTO dug (text) VALUES ('PRATO');
        INSERT INTO dug (text) VALUES ('PIAZZOLA');
        INSERT INTO dug (text) VALUES ('PASSAGGIO');
        INSERT INTO dug (text) VALUES ('CHIASSINO');
        INSERT INTO dug (text) VALUES ('STRADA VICINALE');
        INSERT INTO dug (text) VALUES ('VICINALE');
        INSERT INTO dug (text) VALUES ('MULATTIERA');
        INSERT INTO dug (text) VALUES ('CAVALCAVIA');
        INSERT INTO dug (text) VALUES ('PASSERELLA');
        INSERT INTO dug (text) VALUES ('PASSO');
        INSERT INTO dug (text) VALUES ('PIAZZALETTO');
        INSERT INTO dug (text) VALUES ('PRATELLO');
        INSERT INTO dug (text) VALUES ('SCALO');
        INSERT INTO dug (text) VALUES ('STRADONE');
        INSERT INTO dug (text) VALUES ('SUPERSTRADA');
        INSERT INTO dug (text) VALUES ('VARCO');
        INSERT INTO dug (text) VALUES ('VIOTTOLA');
        INSERT INTO dug (text) VALUES ('VIOTTOLONE');
        INSERT INTO dug (text) VALUES ('CA');
        INSERT INTO dug (text) VALUES ('LUNGO');
        INSERT INTO dug (text) VALUES ('STRADA COMUNALE');
        INSERT INTO dug (text) VALUES ('PASSEGGIATA');
        INSERT INTO dug (text) VALUES ('LOGGE');
        INSERT INTO dug (text) VALUES ('EMICICLO');
        INSERT INTO dug (text) VALUES ('VIUCOLA');
        INSERT INTO dug (text) VALUES ('SORTITA');
        INSERT INTO dug (text) VALUES ('CORTE');
        INSERT INTO dug (text) VALUES ('GIARDINO');
        INSERT INTO dug (text) VALUES ('LOGGIATI');
        INSERT INTO dug (text) VALUES ('ROTONDA');
        INSERT INTO dug (text) VALUES ('VIUCCIOLO');
        INSERT INTO dug (text) VALUES ('VIUZZA');
        INSERT INTO dug (text) VALUES ('BORGATA');
        INSERT INTO dug (text) VALUES ('LUNGOMARE');
        
        alter table features add column street_type character varying(200);
        alter table features add column street_name character varying(200);
        
        CREATE OR REPLACE FUNCTION normalize()
          RETURNS void AS
        $BODY$
        DECLARE
            c CURSOR FOR select text from dug;
            t character varying(50);
        BEGIN
            open c;
            loop
                fetch c into t;
                exit when NOT FOUND;
        
                update features set street_type = t, street_name = trim(substring(name, length(t) + 1)) where upper(name) like t || ' %';
            END LOOP;
            CLOSE c;
        END;
        $BODY$
        LANGUAGE plpgsql;
        
        select normalize();

        create table features2 as 
        select min(id) id, name, st_union(centerline) centerline, 
        st_centroid(st_union(centerline)) centroid, st_envelope(st_collect(centerline)) bounding_box, 
        municipality, country_subdivision, street_type, street_name 
        from features 
        group by name, municipality, country_subdivision, street_type, street_name;
        
        select '<doc><field name="id">' || id || '</field><field name="is_building">false</field><field name="name">' || 
                name || '</field><field name="street_type">' || street_type || '</field><field name="street_name">' || 
                street_name || '</field><field name="municipality">' || municipality || '</field><field name="country_subdivision">' || 
                country_subdivision || '</field><field name="centerline">' || st_astext(centerline) || '</field><field name="centroid">' ||
                st_astext(centroid) || '</field><field name="bounding_box">' ||
                st_xmin(bounding_box) || ' ' || st_ymin(bounding_box) || ' ' || st_xmax(bounding_box) || ' ' || st_ymax(bounding_box) ||
            '</field></doc>' doc
        from features2 where street_type is not null and street_name is not null
        union all
        select '<doc><field name="id">' || id || '</field><field name="is_building">false</field><field name="name">' || 
                name || '</field><field name="municipality">' || municipality || '</field><field name="country_subdivision">' || 
                country_subdivision || '</field><field name="centerline">' || st_astext(centerline) || '</field><field name="centroid">' ||
                st_astext(centroid) || '</field><field name="bounding_box">' ||
                st_xmin(bounding_box) || ' ' || st_ymin(bounding_box) || ' ' || st_xmax(bounding_box) || ' ' || st_ymax(bounding_box) ||
            '</field></doc>'
        from features2 where street_type is null and street_name is null

How to load iter.net data into SOLR
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

1. load iter.net data into loadshp schema of iter.GIS

2. create feature table:
    create index el_st_cod_top_idx on loadshp.elementi_stradali(cod_top);
    create index el_st_cod_top2_idx on loadshp.elementi_stradali(cod_top2);
    
    create table features as
    select 'INET_' || c.cod_istat || '_' || ts.cod_top id, 
        trim(replace(replace(replace(ts.cod_dug || ' ' || ts.den_uff, '&', '&amp;'), '>', '&gt;'), '<', '&lt;'))::character varying as name, 
        trim(replace(replace(replace(ts.cod_dug , '&', '&amp;'), '>', '&gt;'), '<', '&lt;'))::character varying as street_type, 
        trim(replace(replace(replace(ts.den_uff , '&', '&amp;'), '>', '&gt;'), '<', '&lt;'))::character varying as street_name, 
        ST_Transform(st_linemerge(st_union(es.the_geom)), 4326) centerline, 
            ST_Transform(st_pointonsurface(st_collect(es.the_geom)), 4326) centroid, 
            ST_Transform(st_envelope(st_collect(es.the_geom)), 4326) bounding_box,
            c.den_uff municipality, 
        CASE 
            WHEN id_prov = 4 THEN 'MS'
            WHEN id_prov = 5 THEN 'LU'
            WHEN id_prov = 6 THEN 'FI'
            WHEN id_prov = 8 THEN 'PT'
            WHEN id_prov = 9 THEN 'PO'
            WHEN id_prov = 10 THEN 'AR'
            WHEN id_prov = 11 THEN 'PI'
            WHEN id_prov = 12 THEN 'SI'
            WHEN id_prov = 13 THEN 'LI'
            WHEN id_prov = 14 THEN 'GR'
            ELSE 'XX'
            END country_subdivision 
    from loadshp.toponimi_stradali ts 
    join loadshp.comuni c on ts.cod_com = c.cod_com
    join loadshp.elementi_stradali es on ts.cod_top = es.cod_top or ts.cod_top = es.cod_top2
    where ts.cod_dug <> 'SENZA' or ts.den_uff <> 'STRADA SENZA NOME'
    group by id, name, street_type, street_name, municipality, country_subdivision;
    
    select '<doc><field name="id">' || id || '</field><field name="is_building">false</field><field name="name">' || 
                name || '</field><field name="street_type">' || street_type || '</field><field name="street_name">' || 
                street_name || '</field><field name="municipality">' || municipality || '</field><field name="country_subdivision">' || 
                country_subdivision || '</field><field name="centerline">' || st_astext(centerline) || '</field><field name="centroid">' ||
                st_astext(centroid) || '</field><field name="bounding_box">' ||
                st_xmin(bounding_box) || ' ' || st_ymin(bounding_box) || ' ' || st_xmax(bounding_box) || ' ' || st_ymax(bounding_box) ||
            '</field></doc>' doc
        from features;
    
    create table features_cv as
    select 'INET_' || c.cod_istat || '_' || nc.cod_civ id, 
        trim(replace(replace(replace(ts.cod_dug || ' ' || ts.den_uff, '&', '&amp;'), '>', '&gt;'), '<', '&lt;'))::character varying || ', ' || CASE
            WHEN esp_civ is not null THEN num_civ || '/' || esp_civ
            ELSE trim(to_char(num_civ, '999999'))
        END as name, 
        trim(replace(replace(replace(ts.cod_dug , '&', '&amp;'), '>', '&gt;'), '<', '&lt;'))::character varying as street_type, 
        trim(replace(replace(replace(ts.den_uff , '&', '&amp;'), '>', '&gt;'), '<', '&lt;'))::character varying as street_name, 
        ST_Transform(a.the_geom, 4326) centroid,
        c.den_uff municipality, 
        CASE 
            WHEN id_prov = 4 THEN 'MS'
            WHEN id_prov = 5 THEN 'LU'
            WHEN id_prov = 6 THEN 'FI'
            WHEN id_prov = 8 THEN 'PT'
            WHEN id_prov = 9 THEN 'PO'
            WHEN id_prov = 10 THEN 'AR'
            WHEN id_prov = 11 THEN 'PI'
            WHEN id_prov = 12 THEN 'SI'
            WHEN id_prov = 13 THEN 'LI'
            WHEN id_prov = 14 THEN 'GR'
            ELSE 'XX'
        END country_subdivision,
        CASE
            WHEN esp_civ is not null THEN num_civ || '/' || esp_civ
            ELSE trim(to_char(num_civ, '999999'))
        END building_number,
        num_civ number,
        esp_civ number_extension
    from loadshp.numeri_civici nc
    join loadshp.accessi a on coalesce(nc.cod_acc_in, nc.cod_acc_es) = a.cod_acc
    join loadshp.toponimi_stradali ts on nc.cod_top = ts.cod_top
    join loadshp.comuni c on ts.cod_com = c.cod_com
    where (ts.cod_dug <> 'SENZA' or ts.den_uff <> 'STRADA SENZA NOME')
    and num_civ <> 0;

    
    select '<doc><field name="id">' || id || '</field><field name="is_building">true</field><field name="name">' || 
                name || '</field><field name="street_type">' || street_type || '</field><field name="street_name">' || 
                street_name || '</field><field name="municipality">' || municipality || '</field><field name="country_subdivision">' || 
                country_subdivision || '</field><field name="building_number">' || building_number || 
                '</field><field name="number">' || number || 
                '</field><field name="centroid">' ||
                st_astext(centroid) || '</field></doc>' doc
        from features_cv where number_extension is null
    union all
    select '<doc><field name="id">' || id || '</field><field name="is_building">true</field><field name="name">' || 
                name || '</field><field name="street_type">' || street_type || '</field><field name="street_name">' || 
                street_name || '</field><field name="municipality">' || municipality || '</field><field name="country_subdivision">' || 
                country_subdivision || '</field><field name="building_number">' || building_number || 
                '</field><field name="number">' || number || 
                '</field><field name="number_extension">' || number_extension || 
                '</field><field name="centroid">' ||
                st_astext(centroid) || '</field></doc>' doc
        from features_cv where number_extension is not null;
    