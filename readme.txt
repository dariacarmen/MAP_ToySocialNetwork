  RETEA DE SOCIALIZARE - LABORATOR 4 (refactorizare in stil Java 8)
  Metode Avansate de Programare, 2026-2027

Rulare: clasa socialnetwork.Main, cu working directory = folderul
proiectului (caile din config.properties si data/ sunt relative la el).

Aplicatia face exact ce facea la laboratorul 3, pe aceleasi fisiere de
date: fisierul lipsa, liniile corupte (raportate cu numarul liniei) si
exceptiile se trateaza la fel. Codul a fost rescris cu lambda, referinte
la metode, stream-uri si Optional.

1. BUCLE INLOCUITE - TABEL INAINTE / DUPA

 Clasa / metoda                     | Inainte (lab 3)               | Dupa (lab 4) - operatii de stream
------------------------------------+-------------------------------+--------------------------------------------
 Service.rebuildFriendLists         | for peste utilizatori +       | stream().forEach; collect(partitioningBy)
                                    | for peste prietenii + if      | pe prietenii valide / invalide; map + forEach
 Service.nextUserId                 | for + Math.max                | map(Utilizator::getId), max, Optional.map,
                                    |                               | orElse
 Service.removeUser                 | for + if + lista exterioara;  | map, filter, collect(toList);
                                    | for pentru stergere           | forEach(friendshipRepo::delete)
 Service.getCommunities             | for peste utilizatori + DFS   | sorted, map(this::componentOf), distinct,
                                    | cu for peste prieteni         | collect(toList); DFS cu filter + forEach
 Service.getNumberOfCommunities     | getCommunities().size()       | map(this::componentOf), distinct, count
 Service.getMostSociableCommunity   | for + comparatie manuala      | max(Comparator.comparingInt(...)) ->
                                    | cu bestLength                 | Optional.orElseThrow; sorted + collect
 Service.longestPath / longestFrom  | for + backtracking cu         | map + reduce(0, Integer::max);
                                    | onPath.add / onPath.remove    | filter + mapToInt + max().orElse(0)
 Service.getFriendsOf (nou)         | -                             | map, filter, flatMap, distinct, map,
                                    |                               | sorted(comparing.thenComparing), collect
 AbstractFileRepository.loadData    | BufferedReader + while        | Files.lines in try-with-resources,
                                    | (readLine) + contor de linii  | collect(toList); IntStream.range + filter
                                    |                               | + forEach (numarul liniei = i + 1)
 AbstractFileRepository.writeToFile | BufferedWriter + for          | stream().map(this::createEntityAsString)
                                    |                               | .collect(toList()); Files.write(path, linii)
 ConsoleUI (afisari)                | for peste liste               | forEach(System.out::println);
                                    |                               | map + collect(joining(", "))

Pipeline de stream (exemplu din Service.getFriendsOf):
  friendshipRepo.stream()          -> sursa
  .map / .filter / .flatMap        -> operatii intermediare (lenese,
  .distinct / .sorted                 intorc tot un Stream)
  .collect(Collectors.toList())    -> operatie terminala (porneste
                                      prelucrarea si produce rezultatul)

2. INTERFETE FUNCTIONALE FOLOSITE SI UNDE

 Interfata                          | Unde
------------------------------------+--------------------------------------------------
 Validator<T> (@FunctionalInterface)| domain.validators.Validator; metoda default andThen.
                                    | In Main, regula noua "doar litere" este o lambda
                                    | compusa cu UtilizatorValidator prin andThen,
                                    | fara clasa noua si fara a modifica UtilizatorValidator.
 Predicate<E>                       | Repository.findAll(Predicate<E>) (metoda default);
                                    | criteriile din domain.CriteriiUtilizator; combinate
                                    | in ConsoleUI cu or, and, negate, Predicate.not.
 Function<T, R>                     | RepositoryFactory: Map<RepositoryType,
                                    | Function<Validator<E>, Repository<ID, E>>>
                                    | (inlocuieste switch-ul); map(...) in stream-uri.
 Comparator<T>                      | Service (BY_ID, sortarea prieteniilor si a prietenilor),
                                    | ConsoleUI (comanda 12): Comparator.comparing,
                                    | comparingInt, thenComparing, reversed.
 Supplier<T>                        | orElseThrow(() -> new ServiceException(...)) si
                                    | orElseThrow(() -> new RepositoryException(...)).
 Consumer<T>                        | forEach(System.out::println), forEach(this::link).
 BinaryOperator<T>                  | reduce(0, Integer::max) in Service.longestPath.
 IntPredicate                       | chars().allMatch(Character::isLetter) in regula din Main.

Referintele la metode (toate cele patru tipuri):
 - metoda statica:                    CriteriiUtilizator::faraPrieteni,
                                      Character::isLetter, Integer::max
 - metoda de instanta a unui obiect:  System.out::println, friendshipRepo::delete,
                                      userRepo::findOne, this::componentOf
 - metoda de instanta a unui obiect
   oarecare:                          Utilizator::getId, Utilizator::getLastName,
                                      Prietenie::getId, String::trim
 - constructor:                       InMemoryRepository::new (in RepositoryFactory)

3. ALEGERI DE PROIECTARE

Optional in interfata Repository

Repository intoarce in continuare null (findOne, save, delete, update),
ca la laboratorul 3. Motive:
 - contractul din Javadoc ramane acelasi, deci InMemoryRepository,
   repository-urile pe fisier si scheletele pentru baza de date nu se
   schimba;
 - Optional este folosit acolo unde este consumat rezultatul: in Service,
   findUser(id) intoarce Optional.ofNullable(userRepo.findOne(id)), iar
   getUser(id) foloseste orElseThrow. Optional mai apare la max (cea mai
   sociabila comunitate, id-ul urmator) si in RepositoryFactory.
 - get() nu este apelat nicaieri; valoarea se extrage cu orElseThrow,
   orElse, map, isPresent.

Template Method vs Function in repository-urile pe fisier

Am pastrat Template Method (AbstractFileRepository cu subclasele
UtilizatorFileRepository si PrietenieFileRepository). Motive:
 - conversia linie <-> entitate este legata de tipul entitatii si sta
   impreuna cu formatul ei (constantele FIELDS, FILE_DATE_FORMAT);
 - factory-ul ramane simplu: creeaza un repository dupa nume, fara sa
   primeasca doua functii de conversie;
 - codul de citire si scriere (Files.lines, Files.write) este scris o
   singura data, in clasa abstracta.
Dezavantaj cunoscut: loadData este apelata din constructor si apeleaza
metode suprascrise. De aceea subclasele folosesc doar constante statice
(Effective Java, Item 19). Varianta cu Function<String, E> si
Function<E, String> ar evita aceasta problema (compozitie in loc de
mostenire).

Factory fara switch

RepositoryFactory tine doua Map-uri, cate unul pentru utilizatori si
prietenii, din RepositoryType in Function<Validator<E>, Repository<ID, E>>.
Functiile sunt lambda-uri, deci repository-ul este creat doar pentru tipul
ales. Pentru DATABASE, repository-urile raman schelete care arunca
UnsupportedOperationException (implementarea este la laboratorul 5).

4. BUCLE PASTRATE SI DE CE

 - ConsoleUI.run: bucla while (true) care citeste comenzi de la tastatura.
   Nu parcurge o colectie (repeta citirea pana la comanda 0), deci nu
   intra in restrictia despre bucle pe colectii.
 - Backtracking-ul pentru cel mai lung drum NU a fost pastrat ca bucla:
   este scris cu stream-uri (filter + mapToInt + max). Fiecare apel lucreaza
   pe propria copie a drumului, deci functiile date stream-ului nu au
   efecte secundare.

5. EXCEPTII SI COMPORTAMENT NESCHIMBAT

 - Fisier lipsa: repository-ul porneste gol, fisierul (si folderul data/)
   se creeaza la prima salvare.
 - Linie corupta: incarcarea se opreste cu un mesaj cu fisierul, numarul
   liniei, continutul ei si motivul (RepositoryException, cu exceptia
   originala drept cauza). Liniile sunt citite intr-o lista cu Files.lines,
   apoi parcurse cu IntStream.range, ca sa fie cunoscut numarul liniei.
 - IOException de la Files.lines si Files.write este impachetata in
   RepositoryException (exception chaining). Nu exista exceptii checked
   aruncate din interiorul unei lambda.
 - Exceptiile se prind in ConsoleUI si, la pornire, in Main.
 - Fisiere de test cu linii corupte: data/utilizatori_corupt.txt si
   data/prietenii_corupt.txt (se folosesc schimband file.utilizatori sau
   file.prietenii in config.properties).

6. FUNCTIONALITATI DE CAUTARE (DEMONSTRATIE)

 9  - prietenii unui utilizator (stream cu flatMap, joining)
 10 - cautare dupa nume sau prenume: numeIncepeCu(t).or(prenumeIncepeCu(t))
 11 - utilizatori fara prieteni: CriteriiUtilizator::faraPrieteni
 12 - cel putin N prieteni: Predicate.not(CriteriiUtilizator::faraPrieteni)
      .and(celPutinPrieteni(n)), sortati cu comparingInt(...).reversed()
      .thenComparing(Utilizator::getLastName)
 13 - nume care NU incepe cu un text: numeIncepeCu(t).negate()

Un criteriu nou de cautare sau o regula noua de validare se adauga doar
printr-o lambda, fara clase noi (exemplu: regula "doar litere" din Main).