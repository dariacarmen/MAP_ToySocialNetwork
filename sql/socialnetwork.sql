-- Baza de date pentru reteaua de socializare (laboratorul 5).
-- Se creeaza intai baza de date socialnetwork (in pgAdmin: Databases -> Create -> Database),
-- apoi se ruleaza acest script in Query Tool, pe baza de date socialnetwork.
-- Scriptul poate fi rulat de mai multe ori: nu creeaza tabelele a doua oara
-- si nu dubleaza utilizatorii sau prieteniile.

-- id-ul il genereaza baza de date; cu ALWAYS, un INSERT cu id dat explicit este refuzat
CREATE TABLE IF NOT EXISTS users (
                                     id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                     first_name VARCHAR NOT NULL,
                                     last_name  VARCHAR NOT NULL
);

-- o prietenie (id1, id2) se salveaza o singura data, cu id1 < id2;
-- cheile straine nu au ON DELETE CASCADE: prieteniile unui utilizator sters
-- le sterge service-ul (justificarea este in readme.txt)
CREATE TABLE IF NOT EXISTS friendships (
                                           id1          BIGINT    NOT NULL REFERENCES users (id),
    id2          BIGINT    NOT NULL REFERENCES users (id),
    friends_from TIMESTAMP NOT NULL,
    PRIMARY KEY (id1, id2),
    CHECK (id1 < id2)
    );

-- utilizatorii de test, adaugati doar daca nu exista deja (acelasi prenume si nume);
-- coloana nr pastreaza ordinea, ca Ana Pop sa primeasca id-ul 1, Mihai Ionescu 2 etc.
INSERT INTO users (first_name, last_name)
SELECT v.first_name, v.last_name
FROM (VALUES (1, 'Ana', 'Pop'), (2, 'Mihai', 'Ionescu'), (3, 'Ioana', 'Marin'),
             (4, 'Ion', 'Pop'), (5, 'Irina', 'Pop'), (6, 'Dan', 'Matei')) AS v(nr, first_name, last_name)
WHERE NOT EXISTS (SELECT 1 FROM users u
                  WHERE u.first_name = v.first_name AND u.last_name = v.last_name)
ORDER BY v.nr;

-- prieteniile, date prin nume; id-urile se cauta in users,
-- iar LEAST / GREATEST pun id-ul mai mic primul
INSERT INTO friendships (id1, id2, friends_from)
SELECT LEAST(a.id, b.id), GREATEST(a.id, b.id), p.friends_from
FROM (VALUES ('Ana', 'Pop', 'Mihai', 'Ionescu', TIMESTAMP '2026-10-01 12:00'),
             ('Ana', 'Pop', 'Ioana', 'Marin',   TIMESTAMP '2026-10-02 09:30'),
             ('Ana', 'Pop', 'Ion',   'Pop',     TIMESTAMP '2026-10-03 18:15'),
             ('Mihai', 'Ionescu', 'Ioana', 'Marin', TIMESTAMP '2026-10-04 10:00'),
             ('Ion', 'Pop', 'Irina', 'Pop',     TIMESTAMP '2026-10-05 20:00'))
         AS p(f1, l1, f2, l2, friends_from)
         JOIN users a ON a.first_name = p.f1 AND a.last_name = p.l1
         JOIN users b ON b.first_name = p.f2 AND b.last_name = p.l2
    ON CONFLICT DO NOTHING;

SELECT * FROM users ORDER BY id;
SELECT * FROM friendships ORDER BY id1, id2;