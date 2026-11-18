=====================================================================
  RETEA DE SOCIALIZARE - LABORATOR 5 (persistenta in baza de date)
  Metode Avansate de Programare, 2026-2027
=====================================================================

Proiectul continua laboratorul 4. Ce se schimba: datele se pastreaza intr-o
baza de date PostgreSQL (repository.type=DATABASE), iar id-ul unui utilizator
nou il genereaza stocarea, nu service-ul. Functionalitatile din laboratorul 2
merg in continuare; la adaugarea unui utilizator se afiseaza id-ul primit de
la baza de date.


=====================================================================
1. PREGATIRE SI RULARE
=====================================================================
 1. PostgreSQL pornit local, portul 5432, utilizatorul postgres.
 2. In pgAdmin: Databases -> Create -> Database..., numele socialnetwork.
 3. Pe baza de date socialnetwork: Tools -> Query Tool, se deschide
    sql/socialnetwork.sql si se ruleaza (F5). Scriptul poate fi rulat de mai
    multe ori: nu recreeaza tabelele si nu dubleaza datele de test.
 4. Se copiaza src/main/resources/config.example.properties in
    config.properties (acelasi folder) si se completeaza db.password.
    config.properties este in .gitignore: parola reala nu ajunge pe GitHub.
 5. Proiectul este proiect Gradle; driverul JDBC
    org.postgresql:postgresql:42.7.7 este declarat in build.gradle.kts.
    Se ruleaza clasa socialnetwork.Main, cu working directory = folderul
    proiectului.

Trecerea intre DATABASE, FILE si MEMORY se face doar din config.properties
(repository.type), fara modificari in cod.


=====================================================================
2. BAZA DE DATE (sql/socialnetwork.sql)
=====================================================================
 users(id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
       first_name VARCHAR NOT NULL, last_name VARCHAR NOT NULL)
 friendships(id1 BIGINT, id2 BIGINT, friends_from TIMESTAMP NOT NULL,
             PRIMARY KEY (id1, id2), CHECK (id1 < id2))
 id1 si id2 sunt chei straine catre users(id).

 - GENERATED ALWAYS: baza de date genereaza id-ul la fiecare INSERT si
   refuza un id dat explicit ("cannot insert a non-DEFAULT value").
 - CHECK (id1 < id2): o prietenie se salveaza o singura data; Prietenie.makeId
   pune deja id-ul mai mic primul.

ALEGERE: ON DELETE CASCADE sau stergere explicita in service?
-------------------------------------------------------------
Am ales stergerea explicita in service (Service.removeUser), iar cheile
straine NU au ON DELETE CASCADE. Motive:
 - regula "un utilizator sters nu mai are prietenii" sta intr-un singur loc
   si merge la fel pentru MEMORY, FILE si DATABASE (fisierele si memoria nu
   au cascade); cu CASCADE, aceeasi regula ar fi fost scrisa de doua ori;
 - cheile straine fara CASCADE raman o protectie: daca cineva sterge direct
   un utilizator care mai are prietenii (de exemplu din pgAdmin), baza de date
   refuza ("violates foreign key constraint") in loc sa stearga in tacere si
   prieteniile.
Dezavantaj: prieteniile si utilizatorul se sterg prin conexiuni separate,
nu intr-o singura tranzactie.


=====================================================================
3. REPOSITORY-URILE JDBC (pachetul repository.database)
=====================================================================
 UtilizatorDbRepository implements Repository<Long, Utilizator>
 PrietenieDbRepository  implements Repository<Tuple<Long, Long>, Prietenie>

 - Conexiunea: DriverManager.getConnection(url, user, password); url, user
   si password vin din config.properties prin RepositoryFactory, care ii da
   repository-ului prin constructor.
 - Fiecare metoda deschide Connection, PreparedStatement si ResultSet in
   try-with-resources (se inchid automat, si la exceptii).
 - Toate interogarile folosesc PreparedStatement cu "?"; valorile se trimit
   cu setString / setLong / setObject si nu se concateneaza in textul SQL.
   De aceea un nume ca  Robert'); DROP TABLE users; --  se salveaza ca text
   obisnuit (SQL injection nu este posibil).
 - Id-ul generat: save face INSERT fara id, cu
   prepareStatement(sql, Statement.RETURN_GENERATED_KEYS), citeste id-ul cu
   getGeneratedKeys() si il seteaza pe utilizator (setId).

Contractul Repository (acelasi ca la laboratoarele 3-4, intoarce null):
 - findOne(id): entitatea sau null; id null -> IllegalArgumentException;
 - save: validare inainte; null daca s-a salvat; pentru o prietenie care
   exista deja (cheie primara duplicata, SQLState 23505) intoarce entitatea;
 - update: validare inainte; daca executeUpdate() intoarce 0 (id inexistent)
   intoarce entitatea, altfel null;
 - delete: intoarce entitatea stearsa (DELETE ... RETURNING) sau null;
 - argument null -> IllegalArgumentException.

Utilizatorii cititi din baza de date vin cu lista de prieteni completata
(findAll citeste users si friendships cu aceeasi conexiune). Motiv: service-ul
(comunitati, cel mai lung drum) si criteriile din CriteriiUtilizator folosesc
getFriends(), deci merg la fel pentru toate tipurile de persistenta.


=====================================================================
4. ID-UL GENERAT DE STOCARE, PENTRU TOATE TIPURILE
=====================================================================
Service.addUser creeaza utilizatorul fara id si apeleaza save; nu mai exista
Service.nextUserId (era in laboratorul 4).
 - DATABASE: GENERATED ALWAYS AS IDENTITY + getGeneratedKeys();
 - MEMORY si FILE: InMemoryRepository primeste in constructor un generator de
   id-uri, Function<Collection<E>, ID>; pentru utilizatori este
   InMemoryRepository::nextLongId (cel mai mare id + 1). Prieteniile au mereu
   id (perechea de id-uri), deci nu au generator.
 - UtilizatorValidator accepta acum un utilizator nou fara id; daca id-ul
   exista, trebuie sa fie pozitiv.


=====================================================================
5. FACTORY SI CONFIGURARE
=====================================================================
 - RepositoryFactory (Map<RepositoryType, Function<...>>, fara switch) creeaza
   pentru DATABASE repository-urile JDBC, cu db.url, db.user, db.password.
 - config.example.properties (pe GitHub, fara parola reala) si
   config.properties (local, in .gitignore).
 - Driverul: implementation("org.postgresql:postgresql:42.7.7") in
   build.gradle.kts.


=====================================================================
6. TRATAREA EXCEPTIILOR
=====================================================================
SQLException se impacheteaza in RepositoryException (cu SQLException drept
cauza), in DbErrors.wrap. Mesajul se alege dupa SQLState:
 08xxx  -> baza de date nu este disponibila (server oprit / url gresit)
 28P01  -> autentificare esuata (parola sau utilizator gresit)
 3D000  -> baza de date nu exista
 42P01  -> tabela nu exista (scriptul nu a fost rulat)
 23505  -> cheie duplicata (la prietenii: save intoarce entitatea, iar UI
           afiseaza "Utilizatorii x si y sunt deja prieteni")
 23503  -> cheie straina incalcata
 23514  -> constrangere CHECK incalcata
In UI (ConsoleUI) si la pornire (Main) se afiseaza doar mesajul, fara stack
trace; nu exista catch gol si nici catch (Exception e). Daca serverul se
opreste in timpul rularii, aplicatia afiseaza mesajul si continua.


=====================================================================
7. ALTE ALEGERI
=====================================================================
 - Bucle pastrate: while (rs.next()) in repository-urile JDBC parcurge
   cursorul unui ResultSet (nu o colectie Java), ca in Seminarul 7; while
   (true) din ConsoleUI citeste comenzi de la tastatura.
 - Regula scrisa ca lambda in Main (compusa cu UtilizatorValidator prin
   andThen) este acum "numele au cel mult 50 de caractere". Regula din
   laboratorul 4 ("doar litere") ar fi respins numele cu '); DROP TABLE
   users; -- cerut la demonstratie.
 - Data unei prietenii noi se salveaza fara fractiuni de secunda.


=====================================================================
8. DEMONSTRATIE
=====================================================================
 - CRUD verificat in pgAdmin: dupa comenzile 1-4, in pgAdmin pe tabela users
   sau friendships: click dreapta -> View/Edit Data -> All Rows.
 - Id generat: comanda 1 afiseaza "Adaugat, cu id-ul primit de la stocare N".
 - Date pastrate dupa repornire: se inchide aplicatia (0), se porneste din
   nou, comanda 5.
 - Schimbarea lui repository.type: DATABASE / FILE / MEMORY in
   config.properties.
 - Server oprit: in Windows, Services -> postgresql-x64-17 -> Stop; aplicatia
   afiseaza "baza de date nu este disponibila"; apoi Start.
 - Nume cu '); DROP TABLE users; -- : comanda 1, prenume
   Robert'); DROP TABLE users; --  si nume Tables; tabela users exista in
   continuare, iar numele apare ca text in comanda 5 si in pgAdmin.