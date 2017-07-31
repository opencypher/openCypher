/*
This graph is based upon YAGO, which is derived from Wikipedia.
The idea is to enlarge it over time.
http://www.mpi-inf.mpg.de/departments/databases-and-information-systems/research/yago-naga/yago/
*/

CREATE (rachel:Person:Actor {name: 'Rachel Kempson'})
CREATE (michael:Person:Actor {name: 'Michael Redgrave'})
CREATE (vanessa:Person:Actor {name: 'Vanessa Redgrave'})
CREATE (corin:Person:Actor {name: 'Corin Redgrave'})
CREATE (liam:Person:Actor {name: 'Liam Neeson'})
CREATE (natasha:Person:Actor {name: 'Natasha Richardson'})
CREATE (richard:Person:Actor {name: 'Richard Harris'})
CREATE (dennis:Person:Actor {name: 'Dennis Quaid'})
CREATE (lindsay:Person:Actor {name: 'Lindsay Lohan'})
CREATE (jemma:Person:Actor {name: 'Jemma Redgrave'})
CREATE (roy:Person:Actor {name: 'Roy Redgrave'})

CREATE (john:Person {name: 'John Williams'})
CREATE (christopher:Person {name: 'Christopher Nolan'})

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
