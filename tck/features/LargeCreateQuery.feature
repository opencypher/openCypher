#
# Copyright 2016 "Neo Technology",
# Network Engine for Objects in Lund AB (http://neotechnology.com)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

Feature: LargeCreateQuery

  Scenario: Generate the movie graph correctly
    Given any graph
    When executing query:
      """
      CREATE (TheMatrix:Movie {title: 'The Matrix', released: 1999, tagline: 'Welcome to the Real World'})
      CREATE (Keanu:Person {name: 'Keanu Reeves', born: 1964})
      CREATE (Carrie:Person {name: 'Carrie-Anne Moss', born: 1967})
      CREATE (Laurence:Person {name: 'Laurence Fishburne', born: 1961})
      CREATE (Hugo:Person {name: 'Hugo Weaving', born: 1960})
      CREATE (AndyW:Person {name: 'Andy Wachowski', born: 1967})
      CREATE (LanaW:Person {name: 'Lana Wachowski', born: 1965})
      CREATE (JoelS:Person {name: 'Joel Silver', born: 1952})
      CREATE
        (Keanu)-[:ACTED_IN {roles: ['Neo']}]->(TheMatrix),
        (Carrie)-[:ACTED_IN {roles: ['Trinity']}]->(TheMatrix),
        (Laurence)-[:ACTED_IN {roles: ['Morpheus']}]->(TheMatrix),
        (Hugo)-[:ACTED_IN {roles: ['Agent Smith']}]->(TheMatrix),
        (AndyW)-[:DIRECTED]->(TheMatrix),
        (LanaW)-[:DIRECTED]->(TheMatrix),
        (JoelS)-[:PRODUCED]->(TheMatrix)

      CREATE (Emil:Person {name: 'Emil Eifrem', born: 1978})
      CREATE (Emil)-[:ACTED_IN {roles: ['Emil']}]->(TheMatrix)

      CREATE (TheMatrixReloaded:Movie {title: 'The Matrix Reloaded', released: 2003,
              tagline: 'Free your mind'})
      CREATE
        (Keanu)-[:ACTED_IN {roles: ['Neo'] }]->(TheMatrixReloaded),
        (Carrie)-[:ACTED_IN {roles: ['Trinity']}]->(TheMatrixReloaded),
        (Laurence)-[:ACTED_IN {roles: ['Morpheus']}]->(TheMatrixReloaded),
        (Hugo)-[:ACTED_IN {roles: ['Agent Smith']}]->(TheMatrixReloaded),
        (AndyW)-[:DIRECTED]->(TheMatrixReloaded),
        (LanaW)-[:DIRECTED]->(TheMatrixReloaded),
        (JoelS)-[:PRODUCED]->(TheMatrixReloaded)

      CREATE (TheMatrixRevolutions:Movie {title: 'The Matrix Revolutions', released: 2003,
              tagline: 'Everything that has a beginning has an end'})
      CREATE
        (Keanu)-[:ACTED_IN {roles: ['Neo']}]->(TheMatrixRevolutions),
        (Carrie)-[:ACTED_IN {roles: ['Trinity']}]->(TheMatrixRevolutions),
        (Laurence)-[:ACTED_IN {roles: ['Morpheus']}]->(TheMatrixRevolutions),
        (Hugo)-[:ACTED_IN {roles: ['Agent Smith']}]->(TheMatrixRevolutions),
        (AndyW)-[:DIRECTED]->(TheMatrixRevolutions),
        (LanaW)-[:DIRECTED]->(TheMatrixRevolutions),
        (JoelS)-[:PRODUCED]->(TheMatrixRevolutions)

      CREATE (TheDevilsAdvocate:Movie {title: "The Devil's Advocate", released: 1997,
              tagline: 'Evil has its winning ways'})
      CREATE (Charlize:Person {name: 'Charlize Theron', born: 1975})
      CREATE (Al:Person {name: 'Al Pacino', born: 1940})
      CREATE (Taylor:Person {name: 'Taylor Hackford', born: 1944})
      CREATE
        (Keanu)-[:ACTED_IN {roles: ['Kevin Lomax']}]->(TheDevilsAdvocate),
        (Charlize)-[:ACTED_IN {roles: ['Mary Ann Lomax']}]->(TheDevilsAdvocate),
        (Al)-[:ACTED_IN {roles: ['John Milton']}]->(TheDevilsAdvocate),
        (Taylor)-[:DIRECTED]->(TheDevilsAdvocate)

      CREATE (AFewGoodMen:Movie {title: 'A Few Good Men', released: 1992,
              tagline: "Deep within the heart of the nation's capital, one man will stop at nothing to keep his honor, ..."})
      CREATE (TomC:Person {name: 'Tom Cruise', born: 1962})
      CREATE (JackN:Person {name: 'Jack Nicholson', born: 1937})
      CREATE (DemiM:Person {name: 'Demi Moore', born: 1962})
      CREATE (KevinB:Person {name: 'Kevin Bacon', born: 1958})
      CREATE (KieferS:Person {name: 'Kiefer Sutherland', born: 1966})
      CREATE (NoahW:Person {name: 'Noah Wyle', born: 1971})
      CREATE (CubaG:Person {name: 'Cuba Gooding Jr.', born: 1968})
      CREATE (KevinP:Person {name: 'Kevin Pollak', born: 1957})
      CREATE (JTW:Person {name: 'J.T. Walsh', born: 1943})
      CREATE (JamesM:Person {name: 'James Marshall', born: 1967})
      CREATE (ChristopherG:Person {name: 'Christopher Guest', born: 1948})
      CREATE (RobR:Person {name: 'Rob Reiner', born: 1947})
      CREATE (AaronS:Person {name: 'Aaron Sorkin', born: 1961})
      CREATE
        (TomC)-[:ACTED_IN {roles: ['Lt. Daniel Kaffee']}]->(AFewGoodMen),
        (JackN)-[:ACTED_IN {roles: ['Col. Nathan R. Jessup']}]->(AFewGoodMen),
        (DemiM)-[:ACTED_IN {roles: ['Lt. Cdr. JoAnne Galloway']}]->(AFewGoodMen),
        (KevinB)-[:ACTED_IN {roles: ['Capt. Jack Ross']}]->(AFewGoodMen),
        (KieferS)-[:ACTED_IN {roles: ['Lt. Jonathan Kendrick']}]->(AFewGoodMen),
        (NoahW)-[:ACTED_IN {roles: ['Cpl. Jeffrey Barnes']}]->(AFewGoodMen),
        (CubaG)-[:ACTED_IN {roles: ['Cpl. Carl Hammaker']}]->(AFewGoodMen),
        (KevinP)-[:ACTED_IN {roles: ['Lt. Sam Weinberg']}]->(AFewGoodMen),
        (JTW)-[:ACTED_IN {roles: ['Lt. Col. Matthew Andrew Markinson']}]->(AFewGoodMen),
        (JamesM)-[:ACTED_IN {roles: ['Pfc. Louden Downey']}]->(AFewGoodMen),
        (ChristopherG)-[:ACTED_IN {roles: ['Dr. Stone']}]->(AFewGoodMen),
        (AaronS)-[:ACTED_IN {roles: ['Bar patron']}]->(AFewGoodMen),
        (RobR)-[:DIRECTED]->(AFewGoodMen),
        (AaronS)-[:WROTE]->(AFewGoodMen)

      CREATE (TopGun:Movie {title: 'Top Gun', released: 1986,
          tagline:'I feel the need, the need for speed.'})
      CREATE (KellyM:Person {name: 'Kelly McGillis', born: 1957})
      CREATE (ValK:Person {name: 'Val Kilmer', born: 1959})
      CREATE (AnthonyE:Person {name: 'Anthony Edwards', born: 1962})
      CREATE (TomS:Person {name: 'Tom Skerritt', born: 1933})
      CREATE (MegR:Person {name: 'Meg Ryan', born: 1961})
      CREATE (TonyS:Person {name: 'Tony Scott', born: 1944})
      CREATE (JimC:Person {name: 'Jim Cash', born: 1941})
      CREATE
        (TomC)-[:ACTED_IN {roles: ['Maverick']}]->(TopGun),
        (KellyM)-[:ACTED_IN {roles: ['Charlie']}]->(TopGun),
        (ValK)-[:ACTED_IN {roles: ['Iceman']}]->(TopGun),
        (AnthonyE)-[:ACTED_IN {roles: ['Goose']}]->(TopGun),
        (TomS)-[:ACTED_IN {roles: ['Viper']}]->(TopGun),
        (MegR)-[:ACTED_IN {roles: ['Carole']}]->(TopGun),
        (TonyS)-[:DIRECTED]->(TopGun),
        (JimC)-[:WROTE]->(TopGun)

      CREATE (JerryMaguire:Movie {title: 'Jerry Maguire', released: 2000,
          tagline: 'The rest of his life begins now.'})
      CREATE (ReneeZ:Person {name: 'Renee Zellweger', born: 1969})
      CREATE (KellyP:Person {name: 'Kelly Preston', born: 1962})
      CREATE (JerryO:Person {name: "Jerry O'Connell", born: 1974})
      CREATE (JayM:Person {name: 'Jay Mohr', born: 1970})
      CREATE (BonnieH:Person {name: 'Bonnie Hunt', born: 1961})
      CREATE (ReginaK:Person {name: 'Regina King', born: 1971})
      CREATE (JonathanL:Person {name: 'Jonathan Lipnicki', born: 1996})
      CREATE (CameronC:Person {name: 'Cameron Crowe', born: 1957})
      CREATE
        (TomC)-[:ACTED_IN {roles: ['Jerry Maguire']}]->(JerryMaguire),
        (CubaG)-[:ACTED_IN {roles: ['Rod Tidwell']}]->(JerryMaguire),
        (ReneeZ)-[:ACTED_IN {roles: ['Dorothy Boyd']}]->(JerryMaguire),
        (KellyP)-[:ACTED_IN {roles: ['Avery Bishop']}]->(JerryMaguire),
        (JerryO)-[:ACTED_IN {roles: ['Frank Cushman']}]->(JerryMaguire),
        (JayM)-[:ACTED_IN {roles: ['Bob Sugar']}]->(JerryMaguire),
        (BonnieH)-[:ACTED_IN {roles: ['Laurel Boyd']}]->(JerryMaguire),
        (ReginaK)-[:ACTED_IN {roles: ['Marcee Tidwell']}]->(JerryMaguire),
        (JonathanL)-[:ACTED_IN {roles: ['Ray Boyd']}]->(JerryMaguire),
        (CameronC)-[:DIRECTED]->(JerryMaguire),
        (CameronC)-[:PRODUCED]->(JerryMaguire),
        (CameronC)-[:WROTE]->(JerryMaguire)

      CREATE (StandByMe:Movie {title: 'Stand-By-Me', released: 1986,
          tagline: 'The last real taste of innocence'})
      CREATE (RiverP:Person {name: 'River Phoenix', born: 1970})
      CREATE (CoreyF:Person {name: 'Corey Feldman', born: 1971})
      CREATE (WilW:Person {name: 'Wil Wheaton', born: 1972})
      CREATE (JohnC:Person {name: 'John Cusack', born: 1966})
      CREATE (MarshallB:Person {name: 'Marshall Bell', born: 1942})
      CREATE
        (WilW)-[:ACTED_IN {roles: ['Gordie Lachance']}]->(StandByMe),
        (RiverP)-[:ACTED_IN {roles: ['Chris Chambers']}]->(StandByMe),
        (JerryO)-[:ACTED_IN {roles: ['Vern Tessio']}]->(StandByMe),
        (CoreyF)-[:ACTED_IN {roles: ['Teddy Duchamp']}]->(StandByMe),
        (JohnC)-[:ACTED_IN {roles: ['Denny Lachance']}]->(StandByMe),
        (KieferS)-[:ACTED_IN {roles: ['Ace Merrill']}]->(StandByMe),
        (MarshallB)-[:ACTED_IN {roles: ['Mr. Lachance']}]->(StandByMe),
        (RobR)-[:DIRECTED]->(StandByMe)

      CREATE (AsGoodAsItGets:Movie {title: 'As-good-as-it-gets', released: 1997,
                tagline: 'A comedy from the heart that goes for the throat'})
      CREATE (HelenH:Person {name: 'Helen Hunt', born: 1963})
      CREATE (GregK:Person {name: 'Greg Kinnear', born: 1963})
      CREATE (JamesB:Person {name: 'James L. Brooks', born: 1940})
      CREATE
        (JackN)-[:ACTED_IN {roles: ['Melvin Udall']}]->(AsGoodAsItGets),
        (HelenH)-[:ACTED_IN {roles: ['Carol Connelly']}]->(AsGoodAsItGets),
        (GregK)-[:ACTED_IN {roles: ['Simon Bishop']}]->(AsGoodAsItGets),
        (CubaG)-[:ACTED_IN {roles: ['Frank Sachs']}]->(AsGoodAsItGets),
        (JamesB)-[:DIRECTED]->(AsGoodAsItGets)

      CREATE (WhatDreamsMayCome:Movie {title: 'What Dreams May Come', released: 1998,
          tagline: 'After life there is more. The end is just the beginning.'})
      CREATE (AnnabellaS:Person {name: 'Annabella Sciorra', born: 1960})
      CREATE (MaxS:Person {name: 'Max von Sydow', born: 1929})
      CREATE (WernerH:Person {name: 'Werner Herzog', born: 1942})
      CREATE (Robin:Person {name: 'Robin Williams', born: 1951})
      CREATE (VincentW:Person {name: 'Vincent Ward', born: 1956})
      CREATE
        (Robin)-[:ACTED_IN {roles: ['Chris Nielsen']}]->(WhatDreamsMayCome),
        (CubaG)-[:ACTED_IN {roles: ['Albert Lewis']}]->(WhatDreamsMayCome),
        (AnnabellaS)-[:ACTED_IN {roles: ['Annie Collins-Nielsen']}]->(WhatDreamsMayCome),
        (MaxS)-[:ACTED_IN {roles: ['The Tracker']}]->(WhatDreamsMayCome),
        (WernerH)-[:ACTED_IN {roles: ['The Face']}]->(WhatDreamsMayCome),
        (VincentW)-[:DIRECTED]->(WhatDreamsMayCome)

      CREATE (SnowFallingonCedars:Movie {title: 'Snow-Falling-on-Cedars', released: 1999,
        tagline: 'First loves last. Forever.'})
      CREATE (EthanH:Person {name: 'Ethan Hawke', born: 1970})
      CREATE (RickY:Person {name: 'Rick Yune', born: 1971})
      CREATE (JamesC:Person {name: 'James Cromwell', born: 1940})
      CREATE (ScottH:Person {name: 'Scott Hicks', born: 1953})
      CREATE
        (EthanH)-[:ACTED_IN {roles: ['Ishmael Chambers']}]->(SnowFallingonCedars),
        (RickY)-[:ACTED_IN {roles: ['Kazuo Miyamoto']}]->(SnowFallingonCedars),
        (MaxS)-[:ACTED_IN {roles: ['Nels Gudmundsson']}]->(SnowFallingonCedars),
        (JamesC)-[:ACTED_IN {roles: ['Judge Fielding']}]->(SnowFallingonCedars),
        (ScottH)-[:DIRECTED]->(SnowFallingonCedars)

      CREATE (YouveGotMail:Movie {title: "You've Got Mail", released: 1998,
          tagline: 'At-odds-in-life, in-love-on-line'})
      CREATE (ParkerP:Person {name: 'Parker Posey', born: 1968})
      CREATE (DaveC:Person {name: 'Dave Chappelle', born: 1973})
      CREATE (SteveZ:Person {name: 'Steve Zahn', born: 1967})
      CREATE (TomH:Person {name: 'Tom Hanks', born: 1956})
      CREATE (NoraE:Person {name: 'Nora Ephron', born: 1941})
      CREATE
        (TomH)-[:ACTED_IN {roles: ['Joe Fox']}]->(YouveGotMail),
        (MegR)-[:ACTED_IN {roles: ['Kathleen Kelly']}]->(YouveGotMail),
        (GregK)-[:ACTED_IN {roles: ['Frank Navasky']}]->(YouveGotMail),
        (ParkerP)-[:ACTED_IN {roles: ['Patricia Eden']}]->(YouveGotMail),
        (DaveC)-[:ACTED_IN {roles: ['Kevin Jackson']}]->(YouveGotMail),
        (SteveZ)-[:ACTED_IN {roles: ['George Pappas']}]->(YouveGotMail),
        (NoraE)-[:DIRECTED]->(YouveGotMail)

      CREATE (SleeplessInSeattle:Movie {title: 'Sleepless-in-Seattle', released: 1993,
          tagline: 'What if someone you never met, someone you never saw, someone you never knew was the only someone for you?'})
      CREATE (RitaW:Person {name: 'Rita Wilson', born: 1956})
      CREATE (BillPull:Person {name: 'Bill Pullman', born: 1953})
      CREATE (VictorG:Person {name: 'Victor Garber', born: 1949})
      CREATE (RosieO:Person {name: "Rosie O'Donnell", born: 1962})
      CREATE
        (TomH)-[:ACTED_IN {roles: ['Sam Baldwin']}]->(SleeplessInSeattle),
        (MegR)-[:ACTED_IN {roles: ['Annie Reed']}]->(SleeplessInSeattle),
        (RitaW)-[:ACTED_IN {roles: ['Suzy']}]->(SleeplessInSeattle),
        (BillPull)-[:ACTED_IN {roles: ['Walter']}]->(SleeplessInSeattle),
        (VictorG)-[:ACTED_IN {roles: ['Greg']}]->(SleeplessInSeattle),
        (RosieO)-[:ACTED_IN {roles: ['Becky']}]->(SleeplessInSeattle),
        (NoraE)-[:DIRECTED]->(SleeplessInSeattle)

      CREATE (JoeVersustheVolcano:Movie {title: 'Joe-Versus-the-Volcano', released: 1990,
          tagline: 'A story of love'})
      CREATE (JohnS:Person {name: 'John Patrick Stanley', born: 1950})
      CREATE (Nathan:Person {name: 'Nathan Lane', born: 1956})
      CREATE
        (TomH)-[:ACTED_IN {roles: ['Joe Banks']}]->(JoeVersustheVolcano),
        (MegR)-[:ACTED_IN {roles: ['DeDe', 'Angelica Graynamore', 'Patricia Graynamore']}]->(JoeVersustheVolcano),
        (Nathan)-[:ACTED_IN {roles: ['Baw']}]->(JoeVersustheVolcano),
        (JohnS)-[:DIRECTED]->(JoeVersustheVolcano)

      CREATE (WhenHarryMetSally:Movie {title: 'When-Harry-Met-Sally', released: 1998,
          tagline: 'When-Harry-Met-Sally'})
      CREATE (BillyC:Person {name: 'Billy Crystal', born: 1948})
      CREATE (CarrieF:Person {name: 'Carrie Fisher', born: 1956})
      CREATE (BrunoK:Person {name: 'Bruno Kirby', born: 1949})
      CREATE
        (BillyC)-[:ACTED_IN {roles: ['Harry Burns']}]->(WhenHarryMetSally),
        (MegR)-[:ACTED_IN {roles: ['Sally Albright']}]->(WhenHarryMetSally),
        (CarrieF)-[:ACTED_IN {roles: ['Marie']}]->(WhenHarryMetSally),
        (BrunoK)-[:ACTED_IN {roles: ['Jess']}]->(WhenHarryMetSally),
        (RobR)-[:DIRECTED]->(WhenHarryMetSally),
        (RobR)-[:PRODUCED]->(WhenHarryMetSally),
        (NoraE)-[:PRODUCED]->(WhenHarryMetSally),
        (NoraE)-[:WROTE]->(WhenHarryMetSally)

      CREATE (ThatThingYouDo:Movie {title: 'That-Thing-You-Do', released: 1996,
          tagline:'There comes a time...'})
      CREATE (LivT:Person {name: 'Liv Tyler', born: 1977})
      CREATE
        (TomH)-[:ACTED_IN {roles: ['Mr. White']}]->(ThatThingYouDo),
        (LivT)-[:ACTED_IN {roles: ['Faye Dolan']}]->(ThatThingYouDo),
        (Charlize)-[:ACTED_IN {roles: ['Tina']}]->(ThatThingYouDo),
        (TomH)-[:DIRECTED]->(ThatThingYouDo)

      CREATE (TheReplacements:Movie {title: 'The Replacements', released: 2000,
          tagline: 'Pain heals, Chicks dig scars... Glory lasts forever'})
      CREATE (Brooke:Person {name: 'Brooke Langton', born: 1970})
      CREATE (Gene:Person {name: 'Gene Hackman', born: 1930})
      CREATE (Orlando:Person {name: 'Orlando Jones', born: 1968})
      CREATE (Howard:Person {name: 'Howard Deutch', born: 1950})
      CREATE
        (Keanu)-[:ACTED_IN {roles: ['Shane Falco']}]->(TheReplacements),
        (Brooke)-[:ACTED_IN {roles: ['Annabelle Farrell']}]->(TheReplacements),
        (Gene)-[:ACTED_IN {roles: ['Jimmy McGinty']}]->(TheReplacements),
        (Orlando)-[:ACTED_IN {roles: ['Clifford Franklin']}]->(TheReplacements),
        (Howard)-[:DIRECTED]->(TheReplacements)

      CREATE (RescueDawn:Movie {title: 'RescueDawn', released: 2006,
          tagline: 'The extraordinary true story'})
      CREATE (ChristianB:Person {name: 'Christian Bale', born: 1974})
      CREATE (ZachG:Person {name: 'Zach Grenier', born: 1954})
      CREATE
        (MarshallB)-[:ACTED_IN {roles: ['Admiral']}]->(RescueDawn),
        (ChristianB)-[:ACTED_IN {roles: ['Dieter Dengler']}]->(RescueDawn),
        (ZachG)-[:ACTED_IN {roles: ['Squad Leader']}]->(RescueDawn),
        (SteveZ)-[:ACTED_IN {roles: ['Duane']}]->(RescueDawn),
        (WernerH)-[:DIRECTED]->(RescueDawn)

      CREATE (TheBirdcage:Movie {title: 'The-Birdcage', released: 1996, tagline: 'Come-as-you-are'})
      CREATE (MikeN:Person {name: 'Mike Nichols', born: 1931})
      CREATE
        (Robin)-[:ACTED_IN {roles: ['Armand Goldman']}]->(TheBirdcage),
        (Nathan)-[:ACTED_IN {roles: ['Albert Goldman']}]->(TheBirdcage),
        (Gene)-[:ACTED_IN {roles: ['Sen. Kevin Keeley']}]->(TheBirdcage),
        (MikeN)-[:DIRECTED]->(TheBirdcage)

      CREATE (Unforgiven:Movie {title: 'Unforgiven', released: 1992,
          tagline: "It's a hell of a thing, killing a man"})
      CREATE (RichardH:Person {name: 'Richard Harris', born: 1930})
      CREATE (ClintE:Person {name: 'Clint Eastwood', born: 1930})
      CREATE
        (RichardH)-[:ACTED_IN {roles: ['English Bob']}]->(Unforgiven),
        (ClintE)-[:ACTED_IN {roles: ['Bill Munny']}]->(Unforgiven),
        (Gene)-[:ACTED_IN {roles: ['Little Bill Daggett']}]->(Unforgiven),
        (ClintE)-[:DIRECTED]->(Unforgiven)

      CREATE (JohnnyMnemonic:Movie {title: 'Johnny-Mnemonic', released: 1995,
          tagline: 'The-hottest-data-in-the-coolest-head'})
      CREATE (Takeshi:Person {name: 'Takeshi Kitano', born: 1947})
      CREATE (Dina:Person {name: 'Dina Meyer', born: 1968})
      CREATE (IceT:Person {name: 'Ice-T', born: 1958})
      CREATE (RobertL:Person {name: 'Robert Longo', born: 1953})
      CREATE
        (Keanu)-[:ACTED_IN {roles: ['Johnny Mnemonic']}]->(JohnnyMnemonic),
        (Takeshi)-[:ACTED_IN {roles: ['Takahashi']}]->(JohnnyMnemonic),
        (Dina)-[:ACTED_IN {roles: ['Jane']}]->(JohnnyMnemonic),
        (IceT)-[:ACTED_IN {roles: ['J-Bone']}]->(JohnnyMnemonic),
        (RobertL)-[:DIRECTED]->(JohnnyMnemonic)

      CREATE (CloudAtlas:Movie {title: 'Cloud Atlas', released: 2012, tagline: 'Everything is connected'})
      CREATE (HalleB:Person {name: 'Halle Berry', born: 1966})
      CREATE (JimB:Person {name: 'Jim Broadbent', born: 1949})
      CREATE (TomT:Person {name: 'Tom Tykwer', born: 1965})
      CREATE (DavidMitchell:Person {name: 'David Mitchell', born: 1969})
      CREATE (StefanArndt:Person {name: 'Stefan Arndt', born: 1961})
      CREATE
        (TomH)-[:ACTED_IN {roles: ['Zachry', 'Dr. Henry Goose', 'Isaac Sachs', 'Dermot Hoggins']}]->(CloudAtlas),
        (Hugo)-[:ACTED_IN {roles: ['Bill Smoke', 'Haskell Moore', 'Tadeusz Kesselring', 'Nurse Noakes', 'Boardman Mephi', 'Old Georgie']}]->(CloudAtlas),
        (HalleB)-[:ACTED_IN {roles: ['Luisa Rey', 'Jocasta Ayrs', 'Ovid', 'Meronym']}]->(CloudAtlas),
        (JimB)-[:ACTED_IN {roles: ['Vyvyan Ayrs', 'Captain Molyneux', 'Timothy Cavendish']}]->(CloudAtlas),
        (TomT)-[:DIRECTED]->(CloudAtlas),
        (AndyW)-[:DIRECTED]->(CloudAtlas),
        (LanaW)-[:DIRECTED]->(CloudAtlas),
        (DavidMitchell)-[:WROTE]->(CloudAtlas),
        (StefanArndt)-[:PRODUCED]->(CloudAtlas)

      CREATE (TheDaVinciCode:Movie {title: 'The Da Vinci Code', released: 2006, tagline: 'Break The Codes'})
      CREATE (IanM:Person {name: 'Ian McKellen', born: 1939})
      CREATE (AudreyT:Person {name: 'Audrey Tautou', born: 1976})
      CREATE (PaulB:Person {name: 'Paul Bettany', born: 1971})
      CREATE (RonH:Person {name: 'Ron Howard', born: 1954})
      CREATE
        (TomH)-[:ACTED_IN {roles: ['Dr. Robert Langdon']}]->(TheDaVinciCode),
        (IanM)-[:ACTED_IN {roles: ['Sir Leight Teabing']}]->(TheDaVinciCode),
        (AudreyT)-[:ACTED_IN {roles: ['Sophie Neveu']}]->(TheDaVinciCode),
        (PaulB)-[:ACTED_IN {roles: ['Silas']}]->(TheDaVinciCode),
        (RonH)-[:DIRECTED]->(TheDaVinciCode)

      CREATE (VforVendetta:Movie {title: 'V for Vendetta', released: 2006, tagline: 'Freedom! Forever!'})
      CREATE (NatalieP:Person {name: 'Natalie Portman', born: 1981})
      CREATE (StephenR:Person {name: 'Stephen Rea', born: 1946})
      CREATE (JohnH:Person {name: 'John Hurt', born: 1940})
      CREATE (BenM:Person {name: 'Ben Miles', born: 1967})
      CREATE
        (Hugo)-[:ACTED_IN {roles: ['V']}]->(VforVendetta),
        (NatalieP)-[:ACTED_IN {roles: ['Evey Hammond']}]->(VforVendetta),
        (StephenR)-[:ACTED_IN {roles: ['Eric Finch']}]->(VforVendetta),
        (JohnH)-[:ACTED_IN {roles: ['High Chancellor Adam Sutler']}]->(VforVendetta),
        (BenM)-[:ACTED_IN {roles: ['Dascomb']}]->(VforVendetta),
        (JamesM)-[:DIRECTED]->(VforVendetta),
        (AndyW)-[:PRODUCED]->(VforVendetta),
        (LanaW)-[:PRODUCED]->(VforVendetta),
        (JoelS)-[:PRODUCED]->(VforVendetta),
        (AndyW)-[:WROTE]->(VforVendetta),
        (LanaW)-[:WROTE]->(VforVendetta)

      CREATE (SpeedRacer:Movie {title: 'Speed Racer', released: 2008, tagline: 'Speed has no limits'})
      CREATE (EmileH:Person {name: 'Emile Hirsch', born: 1985})
      CREATE (JohnG:Person {name: 'John Goodman', born: 1960})
      CREATE (SusanS:Person {name: 'Susan Sarandon', born: 1946})
      CREATE (MatthewF:Person {name: 'Matthew Fox', born: 1966})
      CREATE (ChristinaR:Person {name: 'Christina Ricci', born: 1980})
      CREATE (Rain:Person {name: 'Rain', born: 1982})
      CREATE
        (EmileH)-[:ACTED_IN {roles: ['Speed Racer']}]->(SpeedRacer),
        (JohnG)-[:ACTED_IN {roles: ['Pops']}]->(SpeedRacer),
        (SusanS)-[:ACTED_IN {roles: ['Mom']}]->(SpeedRacer),
        (MatthewF)-[:ACTED_IN {roles: ['Racer X']}]->(SpeedRacer),
        (ChristinaR)-[:ACTED_IN {roles: ['Trixie']}]->(SpeedRacer),
        (Rain)-[:ACTED_IN {roles: ['Taejo Togokahn']}]->(SpeedRacer),
        (BenM)-[:ACTED_IN {roles: ['Cass Jones']}]->(SpeedRacer),
        (AndyW)-[:DIRECTED]->(SpeedRacer),
        (LanaW)-[:DIRECTED]->(SpeedRacer),
        (AndyW)-[:WROTE]->(SpeedRacer),
        (LanaW)-[:WROTE]->(SpeedRacer),
        (JoelS)-[:PRODUCED]->(SpeedRacer)

      CREATE (NinjaAssassin:Movie {title: 'Ninja Assassin', released: 2009,
          tagline:'Prepare to enter a secret world of assassins'})
      CREATE (NaomieH:Person {name: 'Naomie Harris'})
      CREATE
        (Rain)-[:ACTED_IN {roles: ['Raizo']}]->(NinjaAssassin),
        (NaomieH)-[:ACTED_IN {roles: ['Mika Coretti']}]->(NinjaAssassin),
        (RickY)-[:ACTED_IN {roles: ['Takeshi']}]->(NinjaAssassin),
        (BenM)-[:ACTED_IN {roles: ['Ryan Maslow']}]->(NinjaAssassin),
        (JamesM)-[:DIRECTED]->(NinjaAssassin),
        (AndyW)-[:PRODUCED]->(NinjaAssassin),
        (LanaW)-[:PRODUCED]->(NinjaAssassin),
        (JoelS)-[:PRODUCED]->(NinjaAssassin)

      CREATE (TheGreenMile:Movie {title: 'The Green Mile', released: 1999,
          tagline: "Walk a mile you'll never forget."})
      CREATE (MichaelD:Person {name: 'Michael Clarke Duncan', born: 1957})
      CREATE (DavidM:Person {name: 'David Morse', born: 1953})
      CREATE (SamR:Person {name: 'Sam Rockwell', born: 1968})
      CREATE (GaryS:Person {name: 'Gary Sinise', born: 1955})
      CREATE (PatriciaC:Person {name: 'Patricia Clarkson', born: 1959})
      CREATE (FrankD:Person {name: 'Frank Darabont', born: 1959})
      CREATE
        (TomH)-[:ACTED_IN {roles: ['Paul Edgecomb']}]->(TheGreenMile),
        (MichaelD)-[:ACTED_IN {roles: ['John Coffey']}]->(TheGreenMile),
        (DavidM)-[:ACTED_IN {roles: ['Brutus "Brutal" Howell']}]->(TheGreenMile),
        (BonnieH)-[:ACTED_IN {roles: ['Jan Edgecomb']}]->(TheGreenMile),
        (JamesC)-[:ACTED_IN {roles: ['Warden Hal Moores']}]->(TheGreenMile),
        (SamR)-[:ACTED_IN {roles: ['"Wild Bill" Wharton']}]->(TheGreenMile),
        (GaryS)-[:ACTED_IN {roles: ['Burt Hammersmith']}]->(TheGreenMile),
        (PatriciaC)-[:ACTED_IN {roles: ['Melinda Moores']}]->(TheGreenMile),
        (FrankD)-[:DIRECTED]->(TheGreenMile)

      CREATE (FrostNixon:Movie {title: 'Frost/Nixon', released: 2008,
          tagline: '400 million people were waiting for the truth.'})
      CREATE (FrankL:Person {name: 'Frank Langella', born: 1938})
      CREATE (MichaelS:Person {name: 'Michael Sheen', born: 1969})
      CREATE (OliverP:Person {name: 'Oliver Platt', born: 1960})
      CREATE
        (FrankL)-[:ACTED_IN {roles: ['Richard Nixon']}]->(FrostNixon),
        (MichaelS)-[:ACTED_IN {roles: ['David Frost']}]->(FrostNixon),
        (KevinB)-[:ACTED_IN {roles: ['Jack Brennan']}]->(FrostNixon),
        (OliverP)-[:ACTED_IN {roles: ['Bob Zelnick']}]->(FrostNixon),
        (SamR)-[:ACTED_IN {roles: ['James Reston, Jr.']}]->(FrostNixon),
        (RonH)-[:DIRECTED]->(FrostNixon)

      CREATE (Hoffa:Movie {title: 'Hoffa', released: 1992, tagline: "He didn't want law. He wanted justice."})
      CREATE (DannyD:Person {name: 'Danny DeVito', born: 1944})
      CREATE (JohnR:Person {name: 'John C. Reilly', born: 1965})
      CREATE
        (JackN)-[:ACTED_IN {roles: ['Hoffa']}]->(Hoffa),
        (DannyD)-[:ACTED_IN {roles: ['Robert "Bobby" Ciaro']}]->(Hoffa),
        (JTW)-[:ACTED_IN {roles: ['Frank Fitzsimmons']}]->(Hoffa),
        (JohnR)-[:ACTED_IN {roles: ['Peter "Pete" Connelly']}]->(Hoffa),
        (DannyD)-[:DIRECTED]->(Hoffa)

      CREATE (Apollo13:Movie {title: 'Apollo 13', released: 1995, tagline: 'Houston, we have a problem.'})
      CREATE (EdH:Person {name: 'Ed Harris', born: 1950})
      CREATE (BillPax:Person {name: 'Bill Paxton', born: 1955})
      CREATE
        (TomH)-[:ACTED_IN {roles: ['Jim Lovell']}]->(Apollo13),
        (KevinB)-[:ACTED_IN {roles: ['Jack Swigert']}]->(Apollo13),
        (EdH)-[:ACTED_IN {roles: ['Gene Kranz']}]->(Apollo13),
        (BillPax)-[:ACTED_IN {roles: ['Fred Haise']}]->(Apollo13),
        (GaryS)-[:ACTED_IN {roles: ['Ken Mattingly']}]->(Apollo13),
        (RonH)-[:DIRECTED]->(Apollo13)

      CREATE (Twister:Movie {title: 'Twister', released: 1996, tagline: "Don't Breathe. Don't Look Back."})
      CREATE (PhilipH:Person {name: 'Philip Seymour Hoffman', born: 1967})
      CREATE (JanB:Person {name: 'Jan de Bont', born: 1943})
      CREATE
        (BillPax)-[:ACTED_IN {roles: ['Bill Harding']}]->(Twister),
        (HelenH)-[:ACTED_IN {roles: ['Dr. Jo Harding']}]->(Twister),
        (ZachG)-[:ACTED_IN {roles: ['Eddie']}]->(Twister),
        (PhilipH)-[:ACTED_IN {roles: ['Dustin "Dusty" Davis']}]->(Twister),
        (JanB)-[:DIRECTED]->(Twister)

      CREATE (CastAway:Movie {title: 'Cast Away', released: 2000,
          tagline: 'At the edge of the world, his journey begins.'})
      CREATE (RobertZ:Person {name: 'Robert Zemeckis', born: 1951})
      CREATE
        (TomH)-[:ACTED_IN {roles: ['Chuck Noland']}]->(CastAway),
        (HelenH)-[:ACTED_IN {roles: ['Kelly Frears']}]->(CastAway),
        (RobertZ)-[:DIRECTED]->(CastAway)

      CREATE (OneFlewOvertheCuckoosNest:Movie {title: "One Flew Over the Cuckoo's Nest", released: 1975,
          tagline: "If he's crazy, what does that make you?"})
      CREATE (MilosF:Person {name: 'Milos Forman', born: 1932})
      CREATE
        (JackN)-[:ACTED_IN {roles: ['Randle McMurphy']}]->(OneFlewOvertheCuckoosNest),
        (DannyD)-[:ACTED_IN {roles: ['Martini']}]->(OneFlewOvertheCuckoosNest),
        (MilosF)-[:DIRECTED]->(OneFlewOvertheCuckoosNest)

      CREATE (SomethingsGottaGive:Movie {title: "Something's Gotta Give", released: 2003})
      CREATE (DianeK:Person {name: 'Diane Keaton', born: 1946})
      CREATE (NancyM:Person {name: 'Nancy Meyers', born: 1949})
      CREATE
        (JackN)-[:ACTED_IN {roles: ['Harry Sanborn']}]->(SomethingsGottaGive),
        (DianeK)-[:ACTED_IN {roles: ['Erica Barry']}]->(SomethingsGottaGive),
        (Keanu)-[:ACTED_IN {roles: ['Julian Mercer']}]->(SomethingsGottaGive),
        (NancyM)-[:DIRECTED]->(SomethingsGottaGive),
        (NancyM)-[:PRODUCED]->(SomethingsGottaGive),
        (NancyM)-[:WROTE]->(SomethingsGottaGive)

      CREATE (BicentennialMan:Movie {title: 'Bicentennial Man', released: 1999,
          tagline: "One robot's 200 year journey to become an ordinary man."})
      CREATE (ChrisC:Person {name: 'Chris Columbus', born: 1958})
      CREATE
        (Robin)-[:ACTED_IN {roles: ['Andrew Marin']}]->(BicentennialMan),
        (OliverP)-[:ACTED_IN {roles: ['Rupert Burns']}]->(BicentennialMan),
        (ChrisC)-[:DIRECTED]->(BicentennialMan)

      CREATE (CharlieWilsonsWar:Movie {title: "Charlie Wilson's War", released: 2007,
          tagline: "A stiff drink. A little mascara. A lot of nerve. Who said they couldn't bring down the Soviet empire."})
      CREATE (JuliaR:Person {name: 'Julia Roberts', born: 1967})
      CREATE
        (TomH)-[:ACTED_IN {roles: ['Rep. Charlie Wilson']}]->(CharlieWilsonsWar),
        (JuliaR)-[:ACTED_IN {roles: ['Joanne Herring']}]->(CharlieWilsonsWar),
        (PhilipH)-[:ACTED_IN {roles: ['Gust Avrakotos']}]->(CharlieWilsonsWar),
        (MikeN)-[:DIRECTED]->(CharlieWilsonsWar)

      CREATE (ThePolarExpress:Movie {title: 'The Polar Express', released: 2004,
          tagline: 'This Holiday Season… Believe'})
      CREATE
        (TomH)-[:ACTED_IN {roles: ['Hero Boy', 'Father', 'Conductor', 'Hobo', 'Scrooge', 'Santa Claus']}]->(ThePolarExpress),
        (RobertZ)-[:DIRECTED]->(ThePolarExpress)

      CREATE (ALeagueofTheirOwn:Movie {title: 'A League of Their Own', released: 1992,
          tagline: 'A league of their own'})
      CREATE (Madonna:Person {name: 'Madonna', born: 1954})
      CREATE (GeenaD:Person {name: 'Geena Davis', born: 1956})
      CREATE (LoriP:Person {name: 'Lori Petty', born: 1963})
      CREATE (PennyM:Person {name: 'Penny Marshall', born: 1943})
      CREATE
        (TomH)-[:ACTED_IN {roles: ['Jimmy Dugan']}]->(ALeagueofTheirOwn),
        (GeenaD)-[:ACTED_IN {roles: ['Dottie Hinson']}]->(ALeagueofTheirOwn),
        (LoriP)-[:ACTED_IN {roles: ['Kit Keller']}]->(ALeagueofTheirOwn),
        (RosieO)-[:ACTED_IN {roles: ['Doris Murphy']}]->(ALeagueofTheirOwn),
        (Madonna)-[:ACTED_IN {roles: ['"All the Way" Mae Mordabito']}]->(ALeagueofTheirOwn),
        (BillPax)-[:ACTED_IN {roles: ['Bob Hinson']}]->(ALeagueofTheirOwn),
        (PennyM)-[:DIRECTED]->(ALeagueofTheirOwn)

      CREATE (PaulBlythe:Person {name: 'Paul Blythe'})
      CREATE (AngelaScope:Person {name: 'Angela Scope'})
      CREATE (JessicaThompson:Person {name: 'Jessica Thompson'})
      CREATE (JamesThompson:Person {name: 'James Thompson'})

      CREATE
        (JamesThompson)-[:FOLLOWS]->(JessicaThompson),
        (AngelaScope)-[:FOLLOWS]->(JessicaThompson),
        (PaulBlythe)-[:FOLLOWS]->(AngelaScope)

      CREATE
        (JessicaThompson)-[:REVIEWED {summary: 'An amazing journey', rating: 95}]->(CloudAtlas),
        (JessicaThompson)-[:REVIEWED {summary: 'Silly, but fun', rating: 65}]->(TheReplacements),
        (JamesThompson)-[:REVIEWED {summary: 'The coolest football movie ever', rating: 100}]->(TheReplacements),
        (AngelaScope)-[:REVIEWED {summary: 'Pretty funny at times', rating: 62}]->(TheReplacements),
        (JessicaThompson)-[:REVIEWED {summary: 'Dark, but compelling', rating: 85}]->(Unforgiven),
        (JessicaThompson)-[:REVIEWED {summary: 'Slapstick', rating: 45}]->(TheBirdcage),
        (JessicaThompson)-[:REVIEWED {summary: 'A solid romp', rating: 68}]->(TheDaVinciCode),
        (JamesThompson)-[:REVIEWED {summary: 'Fun, but a little far fetched', rating: 65}]->(TheDaVinciCode),
        (JessicaThompson)-[:REVIEWED {summary: 'You had me at Jerry', rating: 92}]->(JerryMaguire)
      """
    Then the result should be empty
    And the side effects should be:
      | +nodes         | 171 |
      | +relationships | 253 |
      | +properties    | 564 |
      | +labels        | 171 |

  Scenario: Many CREATE clauses
    Given any graph
    When executing query:
      """
      CREATE (hf:School {name: 'Hilly Fields Technical College'})
      CREATE (hf)-[:STAFF]->(mrb:Teacher {name: 'Mr Balls'})
      CREATE (hf)-[:STAFF]->(mrspb:Teacher {name: 'Ms Packard-Bell'})
      CREATE (hf)-[:STAFF]->(mrs:Teacher {name: 'Mr Smith'})
      CREATE (hf)-[:STAFF]->(mrsa:Teacher {name: 'Mrs Adenough'})
      CREATE (hf)-[:STAFF]->(mrvdg:Teacher {name: 'Mr Van der Graaf'})
      CREATE (hf)-[:STAFF]->(msn:Teacher {name: 'Ms Noethe'})
      CREATE (hf)-[:STAFF]->(mrsn:Teacher {name: 'Mrs Noakes'})
      CREATE (hf)-[:STAFF]->(mrm:Teacher {name: 'Mr Marker'})
      CREATE (hf)-[:STAFF]->(msd:Teacher {name: 'Ms Delgado'})
      CREATE (hf)-[:STAFF]->(mrsg:Teacher {name: 'Mrs Glass'})
      CREATE (hf)-[:STAFF]->(mrf:Teacher {name: 'Mr Flint'})
      CREATE (hf)-[:STAFF]->(mrk:Teacher {name: 'Mr Kearney'})
      CREATE (hf)-[:STAFF]->(msf:Teacher {name: 'Mrs Forrester'})
      CREATE (hf)-[:STAFF]->(mrsf:Teacher {name: 'Mrs Fischer'})
      CREATE (hf)-[:STAFF]->(mrj:Teacher {name: 'Mr Jameson'})

      CREATE (hf)-[:STUDENT]->(_001:Student {name:'Portia Vasquez'})
      CREATE (hf)-[:STUDENT]->(_002:Student {name:'Andrew Parks'})
      CREATE (hf)-[:STUDENT]->(_003:Student {name:'Germane Frye'})
      CREATE (hf)-[:STUDENT]->(_004:Student {name:'Yuli Gutierrez'})
      CREATE (hf)-[:STUDENT]->(_005:Student {name:'Kamal Solomon'})
      CREATE (hf)-[:STUDENT]->(_006:Student {name:'Lysandra Porter'})
      CREATE (hf)-[:STUDENT]->(_007:Student {name:'Stella Santiago'})
      CREATE (hf)-[:STUDENT]->(_008:Student {name:'Brenda Torres'})
      CREATE (hf)-[:STUDENT]->(_009:Student {name:'Heidi Dunlap'})

      CREATE (hf)-[:STUDENT]->(_010:Student {name:'Halee Taylor'})
      CREATE (hf)-[:STUDENT]->(_011:Student {name:'Brennan Crosby'})
      CREATE (hf)-[:STUDENT]->(_012:Student {name:'Rooney Cook'})
      CREATE (hf)-[:STUDENT]->(_013:Student {name:'Xavier Morrison'})
      CREATE (hf)-[:STUDENT]->(_014:Student {name:'Zelenia Santana'})
      CREATE (hf)-[:STUDENT]->(_015:Student {name:'Eaton Bonner'})
      CREATE (hf)-[:STUDENT]->(_016:Student {name:'Leilani Bishop'})
      CREATE (hf)-[:STUDENT]->(_017:Student {name:'Jamalia Pickett'})
      CREATE (hf)-[:STUDENT]->(_018:Student {name:'Wynter Russell'})
      CREATE (hf)-[:STUDENT]->(_019:Student {name:'Liberty Melton'})

      CREATE (hf)-[:STUDENT]->(_020:Student {name:'MacKensie Obrien'})
      CREATE (hf)-[:STUDENT]->(_021:Student {name:'Oprah Maynard'})
      CREATE (hf)-[:STUDENT]->(_022:Student {name:'Lyle Parks'})
      CREATE (hf)-[:STUDENT]->(_023:Student {name:'Madonna Justice'})
      CREATE (hf)-[:STUDENT]->(_024:Student {name:'Herman Frederick'})
      CREATE (hf)-[:STUDENT]->(_025:Student {name:'Preston Stevenson'})
      CREATE (hf)-[:STUDENT]->(_026:Student {name:'Drew Carrillo'})
      CREATE (hf)-[:STUDENT]->(_027:Student {name:'Hamilton Woodward'})
      CREATE (hf)-[:STUDENT]->(_028:Student {name:'Buckminster Bradley'})
      CREATE (hf)-[:STUDENT]->(_029:Student {name:'Shea Cote'})

      CREATE (hf)-[:STUDENT]->(_030:Student {name:'Raymond Leonard'})
      CREATE (hf)-[:STUDENT]->(_031:Student {name:'Gavin Branch'})
      CREATE (hf)-[:STUDENT]->(_032:Student {name:'Kylan Powers'})
      CREATE (hf)-[:STUDENT]->(_033:Student {name:'Hedy Bowers'})
      CREATE (hf)-[:STUDENT]->(_034:Student {name:'Derek Church'})
      CREATE (hf)-[:STUDENT]->(_035:Student {name:'Silas Santiago'})
      CREATE (hf)-[:STUDENT]->(_036:Student {name:'Elton Bright'})
      CREATE (hf)-[:STUDENT]->(_037:Student {name:'Dora Schmidt'})
      CREATE (hf)-[:STUDENT]->(_038:Student {name:'Julian Sullivan'})
      CREATE (hf)-[:STUDENT]->(_039:Student {name:'Willow Morton'})

      CREATE (hf)-[:STUDENT]->(_040:Student {name:'Blaze Hines'})
      CREATE (hf)-[:STUDENT]->(_041:Student {name:'Felicia Tillman'})
      CREATE (hf)-[:STUDENT]->(_042:Student {name:'Ralph Webb'})
      CREATE (hf)-[:STUDENT]->(_043:Student {name:'Roth Gilmore'})
      CREATE (hf)-[:STUDENT]->(_044:Student {name:'Dorothy Burgess'})
      CREATE (hf)-[:STUDENT]->(_045:Student {name:'Lana Sandoval'})
      CREATE (hf)-[:STUDENT]->(_046:Student {name:'Nevada Strickland'})
      CREATE (hf)-[:STUDENT]->(_047:Student {name:'Lucian Franco'})
      CREATE (hf)-[:STUDENT]->(_048:Student {name:'Jasper Talley'})
      CREATE (hf)-[:STUDENT]->(_049:Student {name:'Madaline Spears'})

      CREATE (hf)-[:STUDENT]->(_050:Student {name:'Upton Browning'})
      CREATE (hf)-[:STUDENT]->(_051:Student {name:'Cooper Leon'})
      CREATE (hf)-[:STUDENT]->(_052:Student {name:'Celeste Ortega'})
      CREATE (hf)-[:STUDENT]->(_053:Student {name:'Willa Hewitt'})
      CREATE (hf)-[:STUDENT]->(_054:Student {name:'Rooney Bryan'})
      CREATE (hf)-[:STUDENT]->(_055:Student {name:'Nayda Hays'})
      CREATE (hf)-[:STUDENT]->(_056:Student {name:'Kadeem Salazar'})
      CREATE (hf)-[:STUDENT]->(_057:Student {name:'Halee Allen'})
      CREATE (hf)-[:STUDENT]->(_058:Student {name:'Odysseus Mayo'})
      CREATE (hf)-[:STUDENT]->(_059:Student {name:'Kato Merrill'})

      CREATE (hf)-[:STUDENT]->(_060:Student {name:'Halee Juarez'})
      CREATE (hf)-[:STUDENT]->(_061:Student {name:'Chloe Charles'})
      CREATE (hf)-[:STUDENT]->(_062:Student {name:'Abel Montoya'})
      CREATE (hf)-[:STUDENT]->(_063:Student {name:'Hilda Welch'})
      CREATE (hf)-[:STUDENT]->(_064:Student {name:'Britanni Bean'})
      CREATE (hf)-[:STUDENT]->(_065:Student {name:'Joelle Beach'})
      CREATE (hf)-[:STUDENT]->(_066:Student {name:'Ciara Odom'})
      CREATE (hf)-[:STUDENT]->(_067:Student {name:'Zia Williams'})
      CREATE (hf)-[:STUDENT]->(_068:Student {name:'Darrel Bailey'})
      CREATE (hf)-[:STUDENT]->(_069:Student {name:'Lance Mcdowell'})

      CREATE (hf)-[:STUDENT]->(_070:Student {name:'Clayton Bullock'})
      CREATE (hf)-[:STUDENT]->(_071:Student {name:'Roanna Mosley'})
      CREATE (hf)-[:STUDENT]->(_072:Student {name:'Amethyst Mcclure'})
      CREATE (hf)-[:STUDENT]->(_073:Student {name:'Hanae Mann'})
      CREATE (hf)-[:STUDENT]->(_074:Student {name:'Graiden Haynes'})
      CREATE (hf)-[:STUDENT]->(_075:Student {name:'Marcia Byrd'})
      CREATE (hf)-[:STUDENT]->(_076:Student {name:'Yoshi Joyce'})
      CREATE (hf)-[:STUDENT]->(_077:Student {name:'Gregory Sexton'})
      CREATE (hf)-[:STUDENT]->(_078:Student {name:'Nash Carey'})
      CREATE (hf)-[:STUDENT]->(_079:Student {name:'Rae Stevens'})

      CREATE (hf)-[:STUDENT]->(_080:Student {name:'Blossom Fulton'})
      CREATE (hf)-[:STUDENT]->(_081:Student {name:'Lev Curry'})
      CREATE (hf)-[:STUDENT]->(_082:Student {name:'Margaret Gamble'})
      CREATE (hf)-[:STUDENT]->(_083:Student {name:'Rylee Patterson'})
      CREATE (hf)-[:STUDENT]->(_084:Student {name:'Harper Perkins'})
      CREATE (hf)-[:STUDENT]->(_085:Student {name:'Kennan Murphy'})
      CREATE (hf)-[:STUDENT]->(_086:Student {name:'Hilda Coffey'})
      CREATE (hf)-[:STUDENT]->(_087:Student {name:'Marah Reed'})
      CREATE (hf)-[:STUDENT]->(_088:Student {name:'Blaine Wade'})
      CREATE (hf)-[:STUDENT]->(_089:Student {name:'Geraldine Sanders'})

      CREATE (hf)-[:STUDENT]->(_090:Student {name:'Kerry Rollins'})
      CREATE (hf)-[:STUDENT]->(_091:Student {name:'Virginia Sweet'})
      CREATE (hf)-[:STUDENT]->(_092:Student {name:'Sophia Merrill'})
      CREATE (hf)-[:STUDENT]->(_093:Student {name:'Hedda Carson'})
      CREATE (hf)-[:STUDENT]->(_094:Student {name:'Tamekah Charles'})
      CREATE (hf)-[:STUDENT]->(_095:Student {name:'Knox Barton'})
      CREATE (hf)-[:STUDENT]->(_096:Student {name:'Ariel Porter'})
      CREATE (hf)-[:STUDENT]->(_097:Student {name:'Berk Wooten'})
      CREATE (hf)-[:STUDENT]->(_098:Student {name:'Galena Glenn'})
      CREATE (hf)-[:STUDENT]->(_099:Student {name:'Jolene Anderson'})

      CREATE (hf)-[:STUDENT]->(_100:Student {name:'Leonard Hewitt'})
      CREATE (hf)-[:STUDENT]->(_101:Student {name:'Maris Salazar'})
      CREATE (hf)-[:STUDENT]->(_102:Student {name:'Brian Frost'})
      CREATE (hf)-[:STUDENT]->(_103:Student {name:'Zane Moses'})
      CREATE (hf)-[:STUDENT]->(_104:Student {name:'Serina Finch'})
      CREATE (hf)-[:STUDENT]->(_105:Student {name:'Anastasia Fletcher'})
      CREATE (hf)-[:STUDENT]->(_106:Student {name:'Glenna Chapman'})
      CREATE (hf)-[:STUDENT]->(_107:Student {name:'Mufutau Gillespie'})
      CREATE (hf)-[:STUDENT]->(_108:Student {name:'Basil Guthrie'})
      CREATE (hf)-[:STUDENT]->(_109:Student {name:'Theodore Marsh'})

      CREATE (hf)-[:STUDENT]->(_110:Student {name:'Jaime Contreras'})
      CREATE (hf)-[:STUDENT]->(_111:Student {name:'Irma Poole'})
      CREATE (hf)-[:STUDENT]->(_112:Student {name:'Buckminster Bender'})
      CREATE (hf)-[:STUDENT]->(_113:Student {name:'Elton Morris'})
      CREATE (hf)-[:STUDENT]->(_114:Student {name:'Barbara Nguyen'})
      CREATE (hf)-[:STUDENT]->(_115:Student {name:'Tanya Kidd'})
      CREATE (hf)-[:STUDENT]->(_116:Student {name:'Kaden Hoover'})
      CREATE (hf)-[:STUDENT]->(_117:Student {name:'Christopher Bean'})
      CREATE (hf)-[:STUDENT]->(_118:Student {name:'Trevor Daugherty'})
      CREATE (hf)-[:STUDENT]->(_119:Student {name:'Rudyard Bates'})

      CREATE (hf)-[:STUDENT]->(_120:Student {name:'Stacy Monroe'})
      CREATE (hf)-[:STUDENT]->(_121:Student {name:'Kieran Keller'})
      CREATE (hf)-[:STUDENT]->(_122:Student {name:'Ivy Garrison'})
      CREATE (hf)-[:STUDENT]->(_123:Student {name:'Miranda Haynes'})
      CREATE (hf)-[:STUDENT]->(_124:Student {name:'Abigail Heath'})
      CREATE (hf)-[:STUDENT]->(_125:Student {name:'Margaret Santiago'})
      CREATE (hf)-[:STUDENT]->(_126:Student {name:'Cade Floyd'})
      CREATE (hf)-[:STUDENT]->(_127:Student {name:'Allen Crane'})
      CREATE (hf)-[:STUDENT]->(_128:Student {name:'Stella Gilliam'})
      CREATE (hf)-[:STUDENT]->(_129:Student {name:'Rashad Miller'})

      CREATE (hf)-[:STUDENT]->(_130:Student {name:'Francis Cox'})
      CREATE (hf)-[:STUDENT]->(_131:Student {name:'Darryl Rosario'})
      CREATE (hf)-[:STUDENT]->(_132:Student {name:'Michael Daniels'})
      CREATE (hf)-[:STUDENT]->(_133:Student {name:'Aretha Henderson'})
      CREATE (hf)-[:STUDENT]->(_134:Student {name:'Roth Barrera'})
      CREATE (hf)-[:STUDENT]->(_135:Student {name:'Yael Day'})
      CREATE (hf)-[:STUDENT]->(_136:Student {name:'Wynter Richmond'})
      CREATE (hf)-[:STUDENT]->(_137:Student {name:'Quyn Flowers'})
      CREATE (hf)-[:STUDENT]->(_138:Student {name:'Yvette Marquez'})
      CREATE (hf)-[:STUDENT]->(_139:Student {name:'Teagan Curry'})

      CREATE (hf)-[:STUDENT]->(_140:Student {name:'Brenden Bishop'})
      CREATE (hf)-[:STUDENT]->(_141:Student {name:'Montana Black'})
      CREATE (hf)-[:STUDENT]->(_142:Student {name:'Ramona Parker'})
      CREATE (hf)-[:STUDENT]->(_143:Student {name:'Merritt Hansen'})
      CREATE (hf)-[:STUDENT]->(_144:Student {name:'Melvin Vang'})
      CREATE (hf)-[:STUDENT]->(_145:Student {name:'Samantha Perez'})
      CREATE (hf)-[:STUDENT]->(_146:Student {name:'Thane Porter'})
      CREATE (hf)-[:STUDENT]->(_147:Student {name:'Vaughan Haynes'})
      CREATE (hf)-[:STUDENT]->(_148:Student {name:'Irma Miles'})
      CREATE (hf)-[:STUDENT]->(_149:Student {name:'Amery Jensen'})

      CREATE (hf)-[:STUDENT]->(_150:Student {name:'Montana Holman'})
      CREATE (hf)-[:STUDENT]->(_151:Student {name:'Kimberly Langley'})
      CREATE (hf)-[:STUDENT]->(_152:Student {name:'Ebony Bray'})
      CREATE (hf)-[:STUDENT]->(_153:Student {name:'Ishmael Pollard'})
      CREATE (hf)-[:STUDENT]->(_154:Student {name:'Illana Thompson'})
      CREATE (hf)-[:STUDENT]->(_155:Student {name:'Rhona Bowers'})
      CREATE (hf)-[:STUDENT]->(_156:Student {name:'Lilah Dotson'})
      CREATE (hf)-[:STUDENT]->(_157:Student {name:'Shelly Roach'})
      CREATE (hf)-[:STUDENT]->(_158:Student {name:'Celeste Woodward'})
      CREATE (hf)-[:STUDENT]->(_159:Student {name:'Christen Lynn'})

      CREATE (hf)-[:STUDENT]->(_160:Student {name:'Miranda Slater'})
      CREATE (hf)-[:STUDENT]->(_161:Student {name:'Lunea Clements'})
      CREATE (hf)-[:STUDENT]->(_162:Student {name:'Lester Francis'})
      CREATE (hf)-[:STUDENT]->(_163:Student {name:'David Fischer'})
      CREATE (hf)-[:STUDENT]->(_164:Student {name:'Kyra Bean'})
      CREATE (hf)-[:STUDENT]->(_165:Student {name:'Imelda Alston'})
      CREATE (hf)-[:STUDENT]->(_166:Student {name:'Finn Farrell'})
      CREATE (hf)-[:STUDENT]->(_167:Student {name:'Kirby House'})
      CREATE (hf)-[:STUDENT]->(_168:Student {name:'Amanda Zamora'})
      CREATE (hf)-[:STUDENT]->(_169:Student {name:'Rina Franco'})

      CREATE (hf)-[:STUDENT]->(_170:Student {name:'Sonia Lane'})
      CREATE (hf)-[:STUDENT]->(_171:Student {name:'Nora Jefferson'})
      CREATE (hf)-[:STUDENT]->(_172:Student {name:'Colton Ortiz'})
      CREATE (hf)-[:STUDENT]->(_173:Student {name:'Alden Munoz'})
      CREATE (hf)-[:STUDENT]->(_174:Student {name:'Ferdinand Cline'})
      CREATE (hf)-[:STUDENT]->(_175:Student {name:'Cynthia Prince'})
      CREATE (hf)-[:STUDENT]->(_176:Student {name:'Asher Hurst'})
      CREATE (hf)-[:STUDENT]->(_177:Student {name:'MacKensie Stevenson'})
      CREATE (hf)-[:STUDENT]->(_178:Student {name:'Sydnee Sosa'})
      CREATE (hf)-[:STUDENT]->(_179:Student {name:'Dante Callahan'})

      CREATE (hf)-[:STUDENT]->(_180:Student {name:'Isabella Santana'})
      CREATE (hf)-[:STUDENT]->(_181:Student {name:'Raven Bowman'})
      CREATE (hf)-[:STUDENT]->(_182:Student {name:'Kirby Bolton'})
      CREATE (hf)-[:STUDENT]->(_183:Student {name:'Peter Shaffer'})
      CREATE (hf)-[:STUDENT]->(_184:Student {name:'Fletcher Beard'})
      CREATE (hf)-[:STUDENT]->(_185:Student {name:'Irene Lowe'})
      CREATE (hf)-[:STUDENT]->(_186:Student {name:'Ella Talley'})
      CREATE (hf)-[:STUDENT]->(_187:Student {name:'Jorden Kerr'})
      CREATE (hf)-[:STUDENT]->(_188:Student {name:'Macey Delgado'})
      CREATE (hf)-[:STUDENT]->(_189:Student {name:'Ulysses Graves'})

      CREATE (hf)-[:STUDENT]->(_190:Student {name:'Declan Blake'})
      CREATE (hf)-[:STUDENT]->(_191:Student {name:'Lila Hurst'})
      CREATE (hf)-[:STUDENT]->(_192:Student {name:'David Rasmussen'})
      CREATE (hf)-[:STUDENT]->(_193:Student {name:'Desiree Cortez'})
      CREATE (hf)-[:STUDENT]->(_194:Student {name:'Myles Horton'})
      CREATE (hf)-[:STUDENT]->(_195:Student {name:'Rylee Willis'})
      CREATE (hf)-[:STUDENT]->(_196:Student {name:'Kelsey Yates'})
      CREATE (hf)-[:STUDENT]->(_197:Student {name:'Alika Stanton'})
      CREATE (hf)-[:STUDENT]->(_198:Student {name:'Ria Campos'})
      CREATE (hf)-[:STUDENT]->(_199:Student {name:'Elijah Hendricks'})

      CREATE (hf)-[:STUDENT]->(_200:Student {name:'Hayes House'})

      CREATE (hf)-[:DEPARTMENT]->(md:Department {name: 'Mathematics'})
      CREATE (hf)-[:DEPARTMENT]->(sd:Department {name: 'Science'})
      CREATE (hf)-[:DEPARTMENT]->(ed:Department {name: 'Engineering'})

      CREATE (pm:Subject {name: 'Pure Mathematics'})
      CREATE (am:Subject {name: 'Applied Mathematics'})
      CREATE (ph:Subject {name: 'Physics'})
      CREATE (ch:Subject {name: 'Chemistry'})
      CREATE (bi:Subject {name: 'Biology'})
      CREATE (es:Subject {name: 'Earth Science'})
      CREATE (me:Subject {name: 'Mechanical Engineering'})
      CREATE (ce:Subject {name: 'Chemical Engineering'})
      CREATE (se:Subject {name: 'Systems Engineering'})
      CREATE (ve:Subject {name: 'Civil Engineering'})
      CREATE (ee:Subject {name: 'Electrical Engineering'})

      CREATE (sd)-[:CURRICULUM]->(ph)
      CREATE (sd)-[:CURRICULUM]->(ch)
      CREATE (sd)-[:CURRICULUM]->(bi)
      CREATE (sd)-[:CURRICULUM]->(es)
      CREATE (md)-[:CURRICULUM]->(pm)
      CREATE (md)-[:CURRICULUM]->(am)
      CREATE (ed)-[:CURRICULUM]->(me)
      CREATE (ed)-[:CURRICULUM]->(se)
      CREATE (ed)-[:CURRICULUM]->(ce)
      CREATE (ed)-[:CURRICULUM]->(ee)
      CREATE (ed)-[:CURRICULUM]->(ve)

      CREATE (ph)-[:TAUGHT_BY]->(mrb)
      CREATE (ph)-[:TAUGHT_BY]->(mrk)
      CREATE (ch)-[:TAUGHT_BY]->(mrk)
      CREATE (ch)-[:TAUGHT_BY]->(mrsn)
      CREATE (bi)-[:TAUGHT_BY]->(mrsn)
      CREATE (bi)-[:TAUGHT_BY]->(mrsf)
      CREATE (es)-[:TAUGHT_BY]->(msn)
      CREATE (pm)-[:TAUGHT_BY]->(mrf)
      CREATE (pm)-[:TAUGHT_BY]->(mrm)
      CREATE (pm)-[:TAUGHT_BY]->(mrvdg)
      CREATE (am)-[:TAUGHT_BY]->(mrsg)
      CREATE (am)-[:TAUGHT_BY]->(mrspb)
      CREATE (am)-[:TAUGHT_BY]->(mrvdg)
      CREATE (me)-[:TAUGHT_BY]->(mrj)
      CREATE (ce)-[:TAUGHT_BY]->(mrsa)
      CREATE (se)-[:TAUGHT_BY]->(mrs)
      CREATE (ve)-[:TAUGHT_BY]->(msd)
      CREATE (ee)-[:TAUGHT_BY]->(mrsf)

      CREATE(_001)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_188)
      CREATE(_002)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_198)
      CREATE(_003)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_106)
      CREATE(_004)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_029)
      CREATE(_005)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_153)
      CREATE(_006)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_061)
      CREATE(_007)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_177)
      CREATE(_008)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_115)
      CREATE(_009)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_131)
      CREATE(_010)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_142)
      CREATE(_011)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_043)
      CREATE(_012)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_065)
      CREATE(_013)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_074)
      CREATE(_014)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_165)
      CREATE(_015)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_117)
      CREATE(_016)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_086)
      CREATE(_017)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_062)
      CREATE(_018)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_033)
      CREATE(_019)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_171)
      CREATE(_020)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_117)
      CREATE(_021)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_086)
      CREATE(_022)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_121)
      CREATE(_023)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_049)
      CREATE(_024)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_152)
      CREATE(_025)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_152)
      CREATE(_026)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_085)
      CREATE(_027)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_084)
      CREATE(_028)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_143)
      CREATE(_029)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_099)
      CREATE(_030)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_094)
      CREATE(_031)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_125)
      CREATE(_032)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_024)
      CREATE(_033)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_075)
      CREATE(_034)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_161)
      CREATE(_035)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_197)
      CREATE(_036)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_067)
      CREATE(_037)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_049)
      CREATE(_038)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_038)
      CREATE(_039)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_116)
      CREATE(_040)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_149)
      CREATE(_041)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_044)
      CREATE(_042)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_150)
      CREATE(_043)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_095)
      CREATE(_044)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_016)
      CREATE(_045)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_021)
      CREATE(_046)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_123)
      CREATE(_047)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_189)
      CREATE(_048)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_094)
      CREATE(_049)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_161)
      CREATE(_050)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_098)
      CREATE(_051)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_145)
      CREATE(_052)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_148)
      CREATE(_053)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_123)
      CREATE(_054)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_196)
      CREATE(_055)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_175)
      CREATE(_056)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_010)
      CREATE(_057)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_042)
      CREATE(_058)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_196)
      CREATE(_059)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_067)
      CREATE(_060)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_034)
      CREATE(_061)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_002)
      CREATE(_062)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_088)
      CREATE(_063)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_142)
      CREATE(_064)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_88)
      CREATE(_065)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_099)
      CREATE(_066)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_178)
      CREATE(_067)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_041)
      CREATE(_068)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_022)
      CREATE(_069)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_109)
      CREATE(_070)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_045)
      CREATE(_071)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_182)
      CREATE(_072)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_144)
      CREATE(_073)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_140)
      CREATE(_074)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_128)
      CREATE(_075)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_149)
      CREATE(_076)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_038)
      CREATE(_077)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_104)
      CREATE(_078)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_032)
      CREATE(_079)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_123)
      CREATE(_080)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_117)
      CREATE(_081)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_174)
      CREATE(_082)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_162)
      CREATE(_083)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_011)
      CREATE(_084)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_145)
      CREATE(_085)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_003)
      CREATE(_086)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_067)
      CREATE(_087)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_173)
      CREATE(_088)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_128)
      CREATE(_089)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_177)
      CREATE(_090)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_076)
      CREATE(_091)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_137)
      CREATE(_092)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_024)
      CREATE(_093)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_156)
      CREATE(_094)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_020)
      CREATE(_095)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_112)
      CREATE(_096)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_193)
      CREATE(_097)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_006)
      CREATE(_098)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_117)
      CREATE(_099)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_141)
      CREATE(_100)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_001)
      CREATE(_101)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_169)
      CREATE(_102)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_161)
      CREATE(_103)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_136)
      CREATE(_104)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_125)
      CREATE(_105)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_127)
      CREATE(_106)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_095)
      CREATE(_107)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_036)
      CREATE(_108)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_074)
      CREATE(_109)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_150)
      CREATE(_110)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_191)
      CREATE(_111)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_068)
      CREATE(_112)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_019)
      CREATE(_113)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_035)
      CREATE(_114)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_061)
      CREATE(_115)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_070)
      CREATE(_116)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_069)
      CREATE(_117)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_096)
      CREATE(_118)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_107)
      CREATE(_119)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_140)
      CREATE(_120)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_167)
      CREATE(_121)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_120)
      CREATE(_122)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_090)
      CREATE(_123)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_004)
      CREATE(_124)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_083)
      CREATE(_125)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_094)
      CREATE(_126)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_174)
      CREATE(_127)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_168)
      CREATE(_128)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_084)
      CREATE(_129)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_186)
      CREATE(_130)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_090)
      CREATE(_131)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_010)
      CREATE(_132)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_031)
      CREATE(_133)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_059)
      CREATE(_134)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_037)
      CREATE(_135)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_012)
      CREATE(_136)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_197)
      CREATE(_137)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_059)
      CREATE(_138)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_065)
      CREATE(_139)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_175)
      CREATE(_140)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_170)
      CREATE(_141)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_191)
      CREATE(_142)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_139)
      CREATE(_143)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_054)
      CREATE(_144)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_176)
      CREATE(_145)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_188)
      CREATE(_146)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_072)
      CREATE(_147)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_096)
      CREATE(_148)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_108)
      CREATE(_149)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_155)
      CREATE(_150)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_002)
      CREATE(_151)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_076)
      CREATE(_152)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_169)
      CREATE(_153)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_179)
      CREATE(_154)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_186)
      CREATE(_155)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_058)
      CREATE(_156)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_071)
      CREATE(_157)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_073)
      CREATE(_158)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_003)
      CREATE(_159)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_182)
      CREATE(_160)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_199)
      CREATE(_161)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_072)
      CREATE(_162)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_014)
      CREATE(_163)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_163)
      CREATE(_164)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_038)
      CREATE(_165)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_044)
      CREATE(_166)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_136)
      CREATE(_167)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_038)
      CREATE(_168)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_110)
      CREATE(_169)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_198)
      CREATE(_170)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_178)
      CREATE(_171)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_022)
      CREATE(_172)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_020)
      CREATE(_173)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_164)
      CREATE(_174)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_075)
      CREATE(_175)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_175)
      CREATE(_176)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_003)
      CREATE(_177)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_120)
      CREATE(_178)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_006)
      CREATE(_179)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_057)
      CREATE(_180)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_185)
      CREATE(_181)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_074)
      CREATE(_182)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_120)
      CREATE(_183)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_131)
      CREATE(_184)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_045)
      CREATE(_185)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_200)
      CREATE(_186)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_140)
      CREATE(_187)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_150)
      CREATE(_188)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_014)
      CREATE(_189)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_096)
      CREATE(_190)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_063)
      CREATE(_191)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_079)
      CREATE(_192)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_121)
      CREATE(_193)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_196)
      CREATE(_194)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_029)
      CREATE(_195)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_164)
      CREATE(_196)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_083)
      CREATE(_197)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_101)
      CREATE(_198)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_039)
      CREATE(_199)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_011)
      CREATE(_200)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_073)
      CREATE(_001)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_129)
      CREATE(_002)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_078)
      CREATE(_003)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_181)
      CREATE(_004)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_162)
      CREATE(_005)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_057)
      CREATE(_006)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_111)
      CREATE(_007)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_027)
      CREATE(_008)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_123)
      CREATE(_009)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_132)
      CREATE(_010)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_147)
      CREATE(_011)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_083)
      CREATE(_012)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_118)
      CREATE(_013)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_099)
      CREATE(_014)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_140)
      CREATE(_015)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_107)
      CREATE(_016)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_116)
      CREATE(_017)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_002)
      CREATE(_018)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_069)
      CREATE(_019)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_024)
      CREATE(_020)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_022)
      CREATE(_021)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_184)
      CREATE(_022)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_200)
      CREATE(_023)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_200)
      CREATE(_024)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_075)
      CREATE(_025)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_087)
      CREATE(_026)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_163)
      CREATE(_027)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_115)
      CREATE(_028)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_042)
      CREATE(_029)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_058)
      CREATE(_030)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_188)
      CREATE(_031)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_123)
      CREATE(_032)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_015)
      CREATE(_033)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_130)
      CREATE(_034)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_141)
      CREATE(_035)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_158)
      CREATE(_036)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_020)
      CREATE(_037)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_102)
      CREATE(_038)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_184)
      CREATE(_039)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_196)
      CREATE(_040)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_003)
      CREATE(_041)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_171)
      CREATE(_042)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_050)
      CREATE(_043)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_085)
      CREATE(_044)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_025)
      CREATE(_045)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_084)
      CREATE(_046)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_118)
      CREATE(_047)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_002)
      CREATE(_048)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_099)
      CREATE(_049)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_071)
      CREATE(_050)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_178)
      CREATE(_051)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_200)
      CREATE(_052)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_059)
      CREATE(_053)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_095)
      CREATE(_054)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_185)
      CREATE(_055)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_108)
      CREATE(_056)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_083)
      CREATE(_057)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_031)
      CREATE(_058)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_054)
      CREATE(_059)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_198)
      CREATE(_060)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_138)
      CREATE(_061)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_176)
      CREATE(_062)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_086)
      CREATE(_063)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_032)
      CREATE(_064)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_101)
      CREATE(_065)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_181)
      CREATE(_066)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_153)
      CREATE(_067)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_166)
      CREATE(_068)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_003)
      CREATE(_069)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_027)
      CREATE(_070)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_021)
      CREATE(_071)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_193)
      CREATE(_072)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_022)
      CREATE(_073)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_108)
      CREATE(_074)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_174)
      CREATE(_075)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_019)
      CREATE(_076)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_179)
      CREATE(_077)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_005)
      CREATE(_078)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_014)
      CREATE(_079)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_017)
      CREATE(_080)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_146)
      CREATE(_081)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_098)
      CREATE(_082)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_171)
      CREATE(_083)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_099)
      CREATE(_084)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_161)
      CREATE(_085)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_098)
      CREATE(_086)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_199)
      CREATE(_087)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_057)
      CREATE(_088)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_164)
      CREATE(_089)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_064)
      CREATE(_090)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_109)
      CREATE(_091)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_077)
      CREATE(_092)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_124)
      CREATE(_093)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_181)
      CREATE(_094)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_142)
      CREATE(_095)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_191)
      CREATE(_096)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_093)
      CREATE(_097)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_031)
      CREATE(_098)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_045)
      CREATE(_099)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_182)
      CREATE(_100)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_043)
      CREATE(_101)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_146)
      CREATE(_102)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_141)
      CREATE(_103)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_040)
      CREATE(_104)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_199)
      CREATE(_105)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_063)
      CREATE(_106)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_180)
      CREATE(_107)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_010)
      CREATE(_108)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_122)
      CREATE(_109)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_111)
      CREATE(_110)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_065)
      CREATE(_111)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_199)
      CREATE(_112)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_135)
      CREATE(_113)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_172)
      CREATE(_114)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_096)
      CREATE(_115)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_028)
      CREATE(_116)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_109)
      CREATE(_117)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_191)
      CREATE(_118)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_169)
      CREATE(_119)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_101)
      CREATE(_120)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_184)
      CREATE(_121)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_032)
      CREATE(_122)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_127)
      CREATE(_123)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_129)
      CREATE(_124)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_116)
      CREATE(_125)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_150)
      CREATE(_126)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_175)
      CREATE(_127)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_018)
      CREATE(_128)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_165)
      CREATE(_129)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_117)
      CREATE(_130)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_066)
      CREATE(_131)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_050)
      CREATE(_132)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_197)
      CREATE(_133)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_111)
      CREATE(_134)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_125)
      CREATE(_135)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_112)
      CREATE(_136)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_173)
      CREATE(_137)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_181)
      CREATE(_138)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_072)
      CREATE(_139)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_115)
      CREATE(_140)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_013)
      CREATE(_141)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_140)
      CREATE(_142)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_003)
      CREATE(_143)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_144)
      CREATE(_144)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_002)
      CREATE(_145)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_015)
      CREATE(_146)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_061)
      CREATE(_147)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_009)
      CREATE(_148)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_145)
      CREATE(_149)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_176)
      CREATE(_150)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_152)
      CREATE(_151)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_055)
      CREATE(_152)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_157)
      CREATE(_153)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_090)
      CREATE(_154)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_162)
      CREATE(_155)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_146)
      CREATE(_156)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_073)
      CREATE(_157)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_044)
      CREATE(_158)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_154)
      CREATE(_159)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_123)
      CREATE(_160)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_168)
      CREATE(_161)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_122)
      CREATE(_162)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_015)
      CREATE(_163)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_041)
      CREATE(_164)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_087)
      CREATE(_165)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_104)
      CREATE(_166)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_116)
      CREATE(_167)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_019)
      CREATE(_168)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_021)
      CREATE(_169)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_065)
      CREATE(_170)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_183)
      CREATE(_171)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_147)
      CREATE(_172)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_045)
      CREATE(_173)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_172)
      CREATE(_174)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_137)
      CREATE(_175)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_145)
      CREATE(_176)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_138)
      CREATE(_177)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_078)
      CREATE(_178)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_176)
      CREATE(_179)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_062)
      CREATE(_180)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_145)
      CREATE(_181)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_178)
      CREATE(_182)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_173)
      CREATE(_183)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_107)
      CREATE(_184)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_198)
      CREATE(_185)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_057)
      CREATE(_186)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_041)
      CREATE(_187)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_076)
      CREATE(_188)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_132)
      CREATE(_189)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_093)
      CREATE(_190)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_002)
      CREATE(_191)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_183)
      CREATE(_192)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_140)
      CREATE(_193)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_196)
      CREATE(_194)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_117)
      CREATE(_195)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_054)
      CREATE(_196)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_197)
      CREATE(_197)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_086)
      CREATE(_198)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_190)
      CREATE(_199)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_143)
      CREATE(_200)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_144)
      CREATE(_001)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_050)
      CREATE(_002)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_024)
      CREATE(_003)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_135)
      CREATE(_004)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_094)
      CREATE(_005)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_143)
      CREATE(_006)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_066)
      CREATE(_007)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_193)
      CREATE(_008)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_022)
      CREATE(_009)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_074)
      CREATE(_010)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_166)
      CREATE(_011)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_131)
      CREATE(_012)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_036)
      CREATE(_013)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_016)
      CREATE(_014)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_108)
      CREATE(_015)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_083)
      CREATE(_016)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_120)
      CREATE(_017)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_016)
      CREATE(_018)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_130)
      CREATE(_019)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_013)
      CREATE(_020)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_186)
      CREATE(_021)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_026)
      CREATE(_022)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_040)
      CREATE(_023)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_064)
      CREATE(_024)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_072)
      CREATE(_025)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_017)
      CREATE(_026)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_159)
      CREATE(_027)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_076)
      CREATE(_028)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_014)
      CREATE(_029)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_089)
      CREATE(_030)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_157)
      CREATE(_031)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_029)
      CREATE(_032)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_184)
      CREATE(_033)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_131)
      CREATE(_034)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_171)
      CREATE(_035)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_051)
      CREATE(_036)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_031)
      CREATE(_037)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_200)
      CREATE(_038)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_057)
      CREATE(_039)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_023)
      CREATE(_040)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_109)
      CREATE(_041)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_177)
      CREATE(_042)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_020)
      CREATE(_043)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_069)
      CREATE(_044)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_068)
      CREATE(_045)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_027)
      CREATE(_046)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_018)
      CREATE(_047)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_154)
      CREATE(_048)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_090)
      CREATE(_049)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_166)
      CREATE(_050)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_150)
      CREATE(_051)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_045)
      CREATE(_052)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_123)
      CREATE(_053)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_160)
      CREATE(_054)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_088)
      CREATE(_055)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_196)
      CREATE(_056)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_120)
      CREATE(_057)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_110)
      CREATE(_058)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_060)
      CREATE(_059)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_084)
      CREATE(_060)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_030)
      CREATE(_061)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_170)
      CREATE(_062)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_027)
      CREATE(_063)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_018)
      CREATE(_064)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_004)
      CREATE(_065)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_138)
      CREATE(_066)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_009)
      CREATE(_067)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_172)
      CREATE(_068)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_077)
      CREATE(_069)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_112)
      CREATE(_070)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_069)
      CREATE(_071)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_018)
      CREATE(_072)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_172)
      CREATE(_073)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_053)
      CREATE(_074)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_098)
      CREATE(_075)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_068)
      CREATE(_076)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_132)
      CREATE(_077)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_134)
      CREATE(_078)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_138)
      CREATE(_079)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_002)
      CREATE(_080)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_125)
      CREATE(_081)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_129)
      CREATE(_082)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_048)
      CREATE(_083)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_145)
      CREATE(_084)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_101)
      CREATE(_085)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_131)
      CREATE(_086)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_011)
      CREATE(_087)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_200)
      CREATE(_088)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_070)
      CREATE(_089)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_008)
      CREATE(_090)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_107)
      CREATE(_091)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_002)
      CREATE(_092)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_180)
      CREATE(_093)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_001)
      CREATE(_094)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_120)
      CREATE(_095)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_135)
      CREATE(_096)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_116)
      CREATE(_097)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_171)
      CREATE(_098)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_122)
      CREATE(_099)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_100)
      CREATE(_100)-[:BUDDY]->(:StudyBuddy)<-[:BUDDY]-(_130)
      """
    Then the result should be empty
    And the side effects should be:
      | +nodes         | 731  |
      | +relationships | 1247 |
      | +labels        | 730  |
      | +properties    | 230  |