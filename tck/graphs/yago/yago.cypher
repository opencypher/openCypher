CREATE (rachel:Person:Actor {firstname: 'Rachel', surname: 'Kempson'})
CREATE (michael:Person:Actor {firstname: 'Michael', surname: 'Redgrave'})
CREATE (vanessa:Person:Actor {firstname: 'Vanessa', surname: 'Redgrave'})
CREATE (corin:Person:Actor {firstname: 'Corin', surname: 'Redgrave'})
CREATE (liam:Person:Actor {firstname: 'Liam', surname: 'Neeson'})
CREATE (natasha:Person:Actor {firstname: 'Natasha', surname: 'Richardson'})
CREATE (richard:Person:Actor {firstname: 'Richard', surname: 'Harris'})
CREATE (dennis:Person:Actor {firstname: 'Dennis', surname: 'Quaid'})
CREATE (lindsay:Person:Actor {firstname: 'Lindsay', surname: 'Lohan'})
CREATE (jemma:Person:Actor {firstname: 'Jemma', surname: 'Redgrave'})
CREATE (roy:Person:Actor {firstname: 'Roy', surname: 'Redgrave'})

CREATE (john:Person {firstname: 'John', surname: 'Williams'})
CREATE (christopher:Person {firstname: 'Christopher', surname: 'Nolan'})

CREATE (newyork:City {name: 'New York'})
CREATE (london:City {name: 'London'})
CREATE (houston:City {name: 'Houston'})

CREATE (mrchips:Film {title: 'Goodbye, Mr. Chips'})
CREATE (darkknight:Film {title: 'The Dark Knight Rises'})
CREATE (harrypotter:Film {title: 'Harry Potter and the Sorcerer\'s Stone'})
CREATE (parent:Film {title: 'The Parent Trap'})
CREATE (camelot:Film {title: 'Camelot'})

CREATE (rachel)-[:HAS_CHILD]->(vanessa),
       (rachel)-[:HAS_CHILD]->(corin),
       (michael)-[:HAS_CHILD]->(vanessa),
       (michael)-[:HAS_CHILD]->(corin),
       (corin)-[:HAS_CHILD]->(jemma),
       (vanessa)-[:HAS_CHILD]->(natasha),
       (roy)-[:HAS_CHILD]->(michael),

       (rachel)-[:MARRIED]->(michael),
       (michael)-[:MARRIED]->(rachel),
       (natasha)-[:MARRIED]->(liam),
       (liam)-[:MARRIED]->(natasha),

       (vanessa)-[:BORN_IN]->(london),
       (natasha)-[:BORN_IN]->(london),
       (christopher)-[:BORN_IN]->(london),
       (dennis)-[:BORN_IN]->(houston),
       (lindsay)-[:BORN_IN]->(newyork),
       (john)-[:BORN_IN]->(newyork),

       (christopher)-[:DIRECTED]->(darkknight),

       (john)-[:WROTE_MUSIC_FOR]->(harrypotter),
       (john)-[:WROTE_MUSIC_FOR]->(mrchips),

       (michael)-[:ACTED_IN {charactername: 'The Headmaster'}]->(mrchips),
       (vanessa)-[:ACTED_IN {charactername: 'Guenevere'}]->(camelot),
       (richard)-[:ACTED_IN {charactername: 'King Arthur'}]->(camelot),
       (richard)-[:ACTED_IN {charactername: 'Albus Dumbledore'}]->(harrypotter),
       (natasha)-[:ACTED_IN {charactername: 'Liz James'}]->(parent),
       (dennis)-[:ACTED_IN {charactername: 'Nick Parker'}]->(parent),
       (lindsay)-[:ACTED_IN {charactername: 'Halle/Annie'}]->(parent),
       (liam)-[:ACTED_IN {charactername: 'Henri Ducard'}]->(darkknight)
