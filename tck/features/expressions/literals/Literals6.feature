#
# Copyright (c) 2015-2023 "Neo Technology,"
# Network Engine for Objects in Lund AB [http://neotechnology.com]
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# Attribution Notice under the terms of the Apache License 2.0
#
# This work was created by the collective efforts of the openCypher community.
# Without limiting the terms of Section 6, any Derivative Work that is not
# approved by the public consensus process of the openCypher Implementers Group
# should not be described as “Cypher” (and Cypher® is a registered trademark of
# Neo4j Inc.) or as "openCypher". Extensions by implementers or prototypes or
# proposals for change that have been documented or implemented should only be
# described as "implementation extensions to Cypher" or as "proposed changes to
# Cypher that are not yet approved by the openCypher community".
#

#encoding: utf-8

Feature: Literals6 - String

  Scenario: [1] Return a single-quoted empty string
    Given any graph
    When executing query:
      """
      RETURN '' AS literal
      """
    Then the result should be, in any order:
      | literal |
      | ''      |
    And no side effects

  Scenario: [2] Return a single-quoted string with one character
    Given any graph
    When executing query:
      """
      RETURN 'a' AS literal
      """
    Then the result should be, in any order:
      | literal |
      | 'a'     |
    And no side effects

  Scenario: [3] Return a single-quoted string with uft-8 characters
    Given any graph
    When executing query:
      """
      RETURN '🧐🍌❖⋙⚐' AS literal
      """
    Then the result should be, in any order:
      | literal   |
      | '🧐🍌❖⋙⚐' |
    And no side effects

  Scenario: [4] Return a single-quoted string with escaped single-quoted
    Given any graph
    When executing query:
      """
      RETURN '\'' AS literal
      """
    Then the result should be, in any order:
      | literal |
      | '\''    |
    And no side effects

  @skipStyleCheck
  Scenario: [5] Return a single-quoted string with escaped characters
    Given any graph
    When executing query:
      """
      RETURN 'a\\bcn5t\'"\\//\\"\'' AS literal
      """
    # Note that backslashes are an escape character for vertical bar and need to be escaped themselves, i.e. \\\\ for \\
    Then the result should be, in any order:
      | literal                      |
      | 'a\\\\bcn5t\'"\\\\//\\\\"\'' |
    And no side effects

  Scenario: [6] Return a single-quoted string with 100 characters
    Given any graph
    When executing query:
      """
      RETURN 'zvhg02LrjXbeIWUue4CzFT1baQ5ZA uP0ur4suuufFWZu3MGLlMUDYdhya1WcV8GcpEa4Pi03YjPieg2hJY3rt4OAQIeBKhpasUd' AS literal
      """
    Then the result should be, in any order:
      | literal                                                                                                |
      | 'zvhg02LrjXbeIWUue4CzFT1baQ5ZA uP0ur4suuufFWZu3MGLlMUDYdhya1WcV8GcpEa4Pi03YjPieg2hJY3rt4OAQIeBKhpasUd' |
    And no side effects

  Scenario: [7] Return a single-quoted string with 1000 characters
    Given any graph
    When executing query:
      """
      RETURN '92WeD0wBWj GWB1Y pUd6ZiCalZR5VJzIxXt6C74 4bfhdEAkXIHccJ4Avce2aWXTBj v22FvYQ4F0R GfPsbTyQYaL6DEHMbKR HlnP3BrpNBSO427Tsayra 950dNriiiRPbfLhV5oNHZl1Lbs44oAl40hU4LTkZkzIzNhwDtnOunSXwHH4FWpoqSP7B8VHz88z7X8BoSCECUIVs T4z5UFT9oPUCIsdTjzOocn8nT0dD7PVwRzsO2a4R5sNyYe6R4TdBqIWELcIiKhTpaMQsfuEPuzFnwCV1L g zZhhR7yNIo14oupUUD0V0oIHIRvtM0MITOkSiTTmO68ROtezWPfdJQq9pQ6gdcPsy YAU0wMs dVFBTyTzPml55k VOgY4dEuHUC5BkDGwCm8BTvls07JdY4cwm1zsLq1xGuQfVYmr62WF7VeVVIKFX3FuAIOyFqIshJxA8rTnEtzL1eSxrVcabZ0j24i1Zv2D6SDvsbs45pPHNollnZJmKUkLfrldZzlNEuy4JkJa2ahzizZW72f5m2xiwDKgM3 g7nrbYLgIKUtXOdoJeKgUl2cN7j4Xd30dajZpcIDBqsZ LwmRYQlvRXFafWBMD3yQfU4GEzbWQlxV6iBidK83UVdyyvMKaqPvdqovPVQzhIK Xfs yVwnSHDXpjUonwsOFeykee9TcixuxkbYp3Md EBk4LcBDn4zFR3JSmz3FGfP1llIGL ZYWHrzjugMbxPXU02OrqExStd X1ALxTJq2W6mO4kQig4ZQFKHIs66EVWf6HG3SKAxzPAmmf4DZmlZGawG agiO2PrNnWyifOau4em ozqdkAbxu6mCbMEjMri7dkzpjtYFwkxUGpgSjfDm481Eby3SKvwNybwvqfj5CXHWSjGpk8YtJV0T3jzNd731Wb3SWQrVyIy2Wz1UntzYJ33O W9cFnumIVZK1Sj0pQwWoxktNdyknjXiL5COyZiZDBJOcNtIXoklXdBDy' AS literal
      """
    Then the result should be, in any order:
      | literal                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
      | '92WeD0wBWj GWB1Y pUd6ZiCalZR5VJzIxXt6C74 4bfhdEAkXIHccJ4Avce2aWXTBj v22FvYQ4F0R GfPsbTyQYaL6DEHMbKR HlnP3BrpNBSO427Tsayra 950dNriiiRPbfLhV5oNHZl1Lbs44oAl40hU4LTkZkzIzNhwDtnOunSXwHH4FWpoqSP7B8VHz88z7X8BoSCECUIVs T4z5UFT9oPUCIsdTjzOocn8nT0dD7PVwRzsO2a4R5sNyYe6R4TdBqIWELcIiKhTpaMQsfuEPuzFnwCV1L g zZhhR7yNIo14oupUUD0V0oIHIRvtM0MITOkSiTTmO68ROtezWPfdJQq9pQ6gdcPsy YAU0wMs dVFBTyTzPml55k VOgY4dEuHUC5BkDGwCm8BTvls07JdY4cwm1zsLq1xGuQfVYmr62WF7VeVVIKFX3FuAIOyFqIshJxA8rTnEtzL1eSxrVcabZ0j24i1Zv2D6SDvsbs45pPHNollnZJmKUkLfrldZzlNEuy4JkJa2ahzizZW72f5m2xiwDKgM3 g7nrbYLgIKUtXOdoJeKgUl2cN7j4Xd30dajZpcIDBqsZ LwmRYQlvRXFafWBMD3yQfU4GEzbWQlxV6iBidK83UVdyyvMKaqPvdqovPVQzhIK Xfs yVwnSHDXpjUonwsOFeykee9TcixuxkbYp3Md EBk4LcBDn4zFR3JSmz3FGfP1llIGL ZYWHrzjugMbxPXU02OrqExStd X1ALxTJq2W6mO4kQig4ZQFKHIs66EVWf6HG3SKAxzPAmmf4DZmlZGawG agiO2PrNnWyifOau4em ozqdkAbxu6mCbMEjMri7dkzpjtYFwkxUGpgSjfDm481Eby3SKvwNybwvqfj5CXHWSjGpk8YtJV0T3jzNd731Wb3SWQrVyIy2Wz1UntzYJ33O W9cFnumIVZK1Sj0pQwWoxktNdyknjXiL5COyZiZDBJOcNtIXoklXdBDy' |
    And no side effects

  Scenario: [8] Return a single-quoted string with 10000 characters
    Given any graph
    When executing query:
      """
      RETURN 'Qu7cFy732T2KJBCJzyY2xP7fWr4bhg7mdQALjUcVNa2nW2vIfAYMDxd4 ZGSe8g52kVWAiYI5K9SnVH2lMc7Uvh4M9hrvBUs5CPrAIjq9OwgxbVtZcfSrQgRe7hbkx162n0SNvY3KvqBBT5gyhTe4cG2BwJjFx8y11zpf0zyLpnYeQtd6V5maSx9tBigoLnjWdu9pjZ3aycAY8ZpzzOoBniPWThl1ydWyA8E4blXlzkeXnR9GY2UCpHpdmsg5u0GkF4phyqPt61 QRUiJBFXIHDx0zljppa vNLVbIaz8AqM7CGXU5796XKbiCX6uM9WRJXtUooJBJv0uHowr1tey4GQEL4t7j0tE4MznU9X7gRx7BMQGREyCBl5yR6qstIuMKug95TsVxUK3uE1oE5VsS68GlnL6IBAeNhsNMTA4kEflKNI2XKYGf4aDBLABvRa5Qbm12JpccslBbaILFQgQkPBy5nPRfh9Brjpyif1fPPkFB1rJIn 2z4G4irjFafOMuB 4JFTJnvj3 65yEbX7bNtgEF4oB7b7On8DVUAfFQfSz6T1SAFnOatwsNTts6dcH5JewU3jkS4TihfDUvAvw sjo0qoNxowKCoOtOUybt31Xg2mpeV5y5lyxZCSBkqjADNwLglwVcFa08Go3gU qP xs Hrw7ZmQ6vcy6oS6UH R3cJBUKWslkZKEYhXct3duSSWsnn8QFzKm6B4U6dmYXttjjVED0tqPXQ2vwp9eN8jJPebjZfT453810lZM9cQlfOhLdgsSaNaszT8t9pbPC5SrPPPIaXKF2IwRY3uMqAtTJD03bW o8dA3ZqT9igCrKRRfVo5j82HfUzjm2kBh4VT3UXfLGyTnnWqBqQ5WUbmdQQNfiMqGpBIcktEhov1XlJ6DyAzrn 1s yDyQS4Pjqg6y7NHl09nnJ3aMOxdDE7BHv4HVethC3Db32LHv6ZW9zotdOZ8tSH2AGKwhND6cfum67hSXu5OsAGeLZxrrMIn9ml9VWZj8Qxar 3lw3OM2jeUB62REWg7lxTJp3zVuaCQgejCGh40wOPR4vYtyzLdFxsxZ2qwn3XvnO2Xw25KckV8dstFfv4w9NFe03VTBWhoYkuSl0j3eCB1absxURBvss7ReatCgqonoVtkwD5RgknklJg12R56ikPOa9akQwEY ri5X8xDrKyqo2FXrj Np8AmXc4nx0yxydL4yF6WVk J9HmgHjGP0M3dMFOl0n15BUPyTAQNQhAHhDcGjt3jvTqKDW A4GG6gK2xn7hfdgAuoDj4h1lMZsSyYIGTnV6Zig8Nlmtwtss9kjCx 234UQbVuBD96JXbrjmY5jHd7c10KRvUFFzlGcdTscUUi38q6f0czcpoeT8MFBgEbrAw2b50fzz5tLhBGJGeKE0ndK64LOWP0olrS0voljEXYRiLMEArn1bkNUcaOgtQHzoV1Pqp6CR4suZxza66QcNOPH CoSuReOfjYOs1f0hWQ2RU2BUg1vJ5OyRPxAZ81195eJg82WgMFxIo 3EwNLUH6j3D41mu9G2L4ckbETdQRy8PEeM1KSIIjEBLD7xJdXFneolAbsv81mKzrWYRXw0pA8hTI4aIFFQSE8aaUkPUmCE0hzUENcHeNNHMK2UqsClOAdxRiz58hrzdUROac 7UM97kncRVWBSuW4GtISDrgBoEAJQqR2IFIh93W9wKCrESYtjf5uGLzEsGn3l0b2B0jXBoTkbd05jweOTk9LUOgpeNGBNWlpinKda9ny3OfjjCIZx3NnVqsxYiFeV0r4EgE4Vd5QypPNSoQN7rNx2aGufdT52tf1tGeK2d9uVgjDKIjJjZsDJhmnaOUbT5KPYb7fDJ4FJUcl22SMtXAkmQZTbXxGAkyve2SD6pyNB6ShBJ9LkeJPKDWQybSdRD tlQnHVqboE9iYdYOQSblltZwiQHMZcy4eiUHqW7uJ3Mve7bwRZLXYgJEoHeR7E8MXc0SpbVLpbEKEItiqFoi0XEhPGrRvE1PUhphlwiTJBXoLdGO02G97kpy2E8AZtFwboyuW0TXMyEg3bgAP TvGBrbtHyuYfbX6TC1meqTQOGTEMUBjz2VzRB ouL nUpSH7DojvQdxGi8F13xP12K 3IDVZX3UkPAsDgdChHvG5mFiSAaOWBZzUGbGTBkW52NtUQCMkzwYoCNwooNh5Ewk9rNafQQCsrmwaZQGrV pl4u9dBgedBtOeVF7SbxDdOewY uOb1TxLPn9CLwY7KY7igUGZ1prFMUqQ6IsmDLebpOIlG uKI7Xkar6hoRj1Xm8yWPf9o5qkGk agGuD4HrZOA2CtNVsWKiWnV09NLSBd5LdVkhjDbCFGRevIHO1aPCHTPpkml0EStzJdDHVtmGt6EYkbTXUZz7UZs8gKxNs950gEG 4Vtj98io9N0xNbO8FjLL lIqo4LunkmUs0otjT3gmshVAVTwQ0SjCRhqBs10NqVHAT9jCv J3s4mRSoirWeWw7UtzqRc bYtZrpvzmKvP 9lVvuOlEvWhcufv2VUQniDZFYE2EDtNCWrAqiodSAeX5eHEbfbQ5CwJjDjpBHJwoa7lPcZpt43nsXDLvZoIJZPzRPWOzDbt5u3loDI8aYrF2HOmpZ1Lrei XVV3DGYok8M5cWFgfaDILw8sa3kmDDJ2erUPblmMJZZB9eEOLnvEl5O9ALYbBBpnVTnLJvedw9uPVr1HXDmNWgAVpUFYXxKeQVReEFkHT29vENZGi3g7Bv2VUgEx5BxTlHGa13Kmge9QliYARWNfhBPjWQoP2ZRoKCDalsOCeohq2pNOKvkgZOy3AwfpFykBoUjtsvI7NAg6zVhCtCSo6PHcryDgAYYRF737e82qLpjkbCpMozebQRoGrZ7deTFTy TZCiOP2nGOKWiMnGq1daw3uAOx3ntthuZR1viQ8qmyXiIaBwJF5REqFJbZdPvRTpXns8vsG9PsXu DkPiWh3LieaiMGM3zyBsdFheatoBnj0ccBSsiKSDH SmVyBPw8K5vAeVA5WQy8LXX27mzhA7rlrXdWH8kMmtK15lR2AHE7XmSrzGaUbqWGRzmfrTDM vJPKZ8y73x8jhCvVK34nqFZbvlIRdYaUfWjQIGhdJ60V0JMJsh3bvYMDOlDnviPgT5MoAP6LszNwTp4O4yzdxgmq7CY48bQigcLRYEmg8ZWBU6ekc0Gk8Uuj3qC2Oy4DviJoC5Sy68xnl762KjXseDWuO0US6k5NCcztEWuB41AhFLjT Xlfv7dJNvDvyrTwYbnapgnqRTq2fD0NlkKq 0Wmjgv8HRMAUOU4Sfh2PNem 4BK4fBQKbzZWjK8Mjh4quPQr23P4K3qfVfyqGU9Y7HWPRiaz 86zjtl0Gu6DGo92GqPEGNBs RVMTebDPNWQWZju4bqF01z9jnsyzLbG1PD5bqdZccxHK9E bD9AM0KjsT3bSvhG4wCqIUOH9VBFKARnrscsgtF7sbmiBwtt3RfX9cddLMWn8lxh6swaE1pFyN8sg4qRhjVBHv0viacoxg7glAHAowSaqJXKRUWO0wBLz7esMhv9H44d6ztNLrgfays65REWjKWuMe4RsSP7VLGrQRvG6QKZ2GyI5K3WdQRRsPl2QrSxzCEHR1feQLSkngRpWAi4Gwt0ZUHzTGLMZeDQpG9fYWjSRfuPBWm4rHYyI0ny6WmqZa3yi3zeaHXKsNMMxV5RhI3wcY3UdgRBNTG1 yogATPH JYM5tSqE3M6tPgUumwH3qba 7a9XZcAJF7MYjb214yDndl8CYcQiJ9xUnyta9DToaXdLDFMOxIWdv4Oc Ae 092ASura8P5qig9RUZAwUpWiJTnCz6fSEkb1XHzAgW4HwrczuFFGsRNAUY5cReitkmwpFhf4Jz8KHHbUj8fbDROSfdsmjInlHnwLsB1sjfvZG6vk3LffL78GSIZ5fPfDnFm3rc2A0AWP0Abu539HMhSFd967byWCgpKqWCyMBjW1b6ool1XPus5gM0hx10WdSbMsEpYRR2SwicTxN18oIR4pJaQkE6or9TX6rz9vV6ZEyb4 ud wHyp1I227JdmFLT79kilRqj9K9xWnDR7SlCYSrIVavAnAa1vp4OF4fIQv5ER0Yj61PgmVQQWorwnGK4B9ArBshfyu CTzvR2isHgEpXVRg q2c4c4u7S19M 2PlDrcryc1M0HR1oBmdAsy mIV0E8BR 5E4xi5ZmrKMCXnpH7jURkiDLcu6bsOBufpLbEhKCaFJoC5r3nKY59nohuSWOigeOkEIcdCJt3VaQdwL1doyWzdpG0lUsCP9ZzzIB5oOp5RGgkoGiAh 5WSB5gHlpeK7lDPm2JEulXLeh97fRmSxe4nOVgyGscjoFfi9PgFqDuntZZwsNLiiMfsX8W 97fDeOT0TWvHw7JuioLjxDtOOOBrnZlKkUZQ7CRy7ch38tA1DzJOcCb178efuhtH91QrhoHJn6csVBRrg0DL98BGshITV Rojhsgq7j4NSLircpRgENiVRh49HigUtgwH5AK7xIAjMpD1ky gLFMqpfp4l9vlNrBhTpPDCI1R9UQMeCpiSXnJ9UjtL4uoXfmraI9xY4yVxVZFBXyhhk BaCRXp92qhUege4cIsMfK47FVJLIXzqn3Nu1TPmVyxQmmqXw7NLvVVu12x3DRrsi8ouiedz1KwDXmDhR4cLlnnHSei62MXC0elxELoUAooeyWnLPj6irfATHZ2BvdHUHNXLMq0xqqwzWDsQPklXiI5UPrCi6LfKDvwa38SAyF460vkacS92lPRdrh9S7xjhUOVN7mvjRYdnCU5I5sNiBsQqiuo8aA3GjQkXO0zBnddviQinlSjDEqB97aqZlviAgLTYtM8nbN1tWUH8gayIEPcpC4GyC37WCRiRg0hgyeXbs9sA1nHm5pIZ6sWY33A849nLfYF28C1TB27YPGTlrbCGIZEB4j62BvYUUAxmVo8VXS3hqegl2NPEKX8viEqv qwJZn1YBNjXRlJ1CHd6kqi48 udquQQT4XJTCMpfzbS9HOpXq4SRZmJDrqgXSsY4HPGc xk8p2ZRBodSSpKH3z6YOJ6tdOJ8BRqrymXoIsE1YK63BLSSyD437qwJedJzpHUMiLRZWJ 5FTcYrdWUIh4d I98rGjwjmlAdzEKMtXl0aimE 3hQ2T14pGWF2BlIKQPiX Q2FlSssswVhXtfdUdaBSlBXSk1e2JXVh4a2X5F ENUoTSbAgRHm 0jeYe9Mgw7BAOv1IXWzqfEpBgca0DnbIaDhYGojuvYb3ZKygKzsEXWF9ybgSNdMXARHYfNru2MoI9EKQHEcAHwwBWWKevcr92SnF83UyNyoyATmfb76bqggDHg0e4OD7FYyQ16VhLFowFGew7OhN16urh5 SU9JxECvjmbpe3mY83MOtZR65FRq3FaxYSsEDgI41Ce3wsNgkUXaxmiUw8M6FUFwihz8ZEihfxMb41EAnafjOUo66tfs1bzzWFvGuuEXfLeHOs07YF7YSmwhs6smrP3SkWXJCQfEjr9kn8sGB2VBpmO7aTiIdGHBa2u hyjkJrTu64n54dknHBPMl2Yc nyEoHucwalDRjPBhPNTAenytix29MsVEFvnaEqgxkB1DbdbifGvkWAt9t86BWvbgE2hIPAGA6zcm43Wzg8ENZCLqVoGSAFe ZjpptB4c84l a1XxUUxo7fmmDdkFNaTZP6UFmkzFnhDt3NB Dzom5Px h5CEHIvdgRSbdBr9tlLkm9gBTbS3fTYjPTPBnnGyUZnOhLMS8CExBvaAdxh6lmprWxyfaLOfi4uqmDQ5VGmjexWZin2Q7QQBSDZaLoSImoZ0TytdMvwpdIHQysLtvdLUJ9Jmklz4C cwZM538cCfD97iMjkZ sGB95sShsGhgNCUwR35cmjMJfVuFtppu4iU3AZkXs0OyKFUxBMhLEHQYBM0U9H rV0rHJDW0LirrncRqtLBOvcj bC4jKiSN3slzd v2XbmKBd4tWKKLcgMZmtF99WcteKyYMCWkF62nBVTyZZsyxUWETHOB9O2B7dukuQuGFz28pQhR Qsf7xKo8cwjc66YYWj61OFt4qFO9miVOojp8MR2qhCXdl1tVVHoUPh8WnrEnPWT C9u5co4NUhSAUHwyPuMKbr jhx9u34vJNaAScYvGDKy3wmxB3ogzfWE7n yqN1RvxJl9 mc0vk3ObjaGUYidas4nK2fQaVeNvwebbr dHeLJF0f qHWUoJmBKg6d7owotrQ7beZcYO7J7vZRZv0P26JuM3he8Q hl2Lak9ViLes59a4zfOn rzS9swYagFbPhwll44Q7lfRQzbjs7OO6viaC3aCYPv5BAPB8F9k W6sKpfuY52rpez5W4LoBBmjYMz8j 9Sc5WPXj32Zic fCaM65d eFACBAwnQeJKohksmmx9GPBKEZScTHe0gVqOfKklUv7OITLOVFIXD311e8KoWg2L7RZgiWz1JHNPI1BL9jkY3aQW52b6OGDX LR HQf7WoT3lQF85ICLNVKbjzWUDEL2AOIWK0jxvTnFiDBH7y2b4MpfmAfWBXtUsJJfgUGG2VW3pTFOqQS6rWir6jfvQs43ohSyt68RiZ1CfbR0Y9xY04fWPVsLKRlo9KM4JllXAwwKuSbvRpT4amOtbdkdKEKDPvmA6FQ61cSWayEADwjN8lbpUELdl150T9MjcDDdWZxv7nZ XAj493l8tUZlVGNXZ7OxOyoTf3PyIDCdtN9ut7TDBzpIFlDQhSBAHDY5cs5ct9nLzA6s1DGqdBj4NJPeRiKsPYGHnyqK5CE8S9IAJ 0XIfiJR so8fY9iySAKKECppnRk4hcdoVQhevjFBqAbSG02X1zkaKRXpvGxdWryFYL6TA9fVvRNpwi3JVSnhLslULMTcsnZeIkwN7QHWLDWh29DPXX31g7lLYdYnkiA53ZCCN0EKuwEpToy84vh3Gu8sO6Kv k6tHynKAVz0SentHsh 0LV387w8PQHYdYn7PzsQJ1sNmqIOyTn4Te7z1ElCSgqU0I0ImflD ilxsSUrsqaqhofXMyDkb5ZAaYGtFrhn Ea6 qw5ZCkbws8N8aY4gW90e90k9Rhhg0vE5nD74Rg5awiOA7vtmjn9LOKdLF67j1nVrpIZU4ADStXLwHWX0yCRFdw sfEKYuIrnFOc1sSjOKx fvHOSVGlYqaBv1yKqRBheU hsYupfxA3zzrlsYD71qZ4TmlqayGtK8p5SELT1mD0YG0v9VYPQrSqkrk V4kcPKckonY7zPZKkYbf6b5e22XVE0AWokBiYQwNuyIqEifpkhlc9PrUp13cwWncTlnMWyRDQrlW2i6oRJbMZJoE2Bcy72YMzbqvbcrmXnemI9tUDiHRZi0V1gbtxxvEjw 0 Z5UjDGk0jua35FOBRL4DdYRIawvkbzo7Lr 4PymJ0DrUu3k5IvBhQthdDJG7Dpf8Q4AiyUsZKkied3d7CFLKcpAmZ7up8J0pOcGEN3q0HsIUJ m1oW3acBCBXiYJ2 n JKAteFJPTgCqQzDhNOootC6BJXq4Ju4VUSdfD8poERjuadKYrInUCTKqRgU6H7N8B2lILyF GKnUT4mrxGxDduPrMIKE1wIdCOwAlD7H5V BYKZDF3GGwxsRU9Ktctq3tgatYQyB40VkWSftduesDqH118 2MhhZqYFwq8stqRqhFpYsjHwqY1owy yPnApsBOt7F7P9Y2NPCBziPywkY7nZiRhf2UtSLpWGPWlegIlkMCYtOB fNnPpxotXpOyUiNWcF TpwXxXrUG2PTnHouO2vtQOSS5OkbpDYPMgCNZI Pvc6WAV8H61FnNOaGJHYY8zmKGMNaqZg4XRpbDZKCd34aFJDmu6rXwzOf4LqagfuR6S3shK82phsJvJXpho6pkugIfCiai0Xw9qkUW2NT4DMiomcJmWEwUCnTEsZCUSN0Lxlz6Cm49 Jc8OBtlCYqGwOtQkK2Uqz0CYGxX9zUcu BYH2I00luXU6seC2vcn2ouX3oBmOkfg5GW4whSQJd0ahBvsRAvHMj2YAixGkZM9XE FgJqJYl98YoIUQtH7aOXkZfcgWsojqGo0v8DdZNjYuXJzUEgDzIbD xWwxjf2S1LeLieYDcqgnu6I6WpMlwaCAtReo tY7mLd5r2oxLABi7epYW6oZZrYxwhjZZNw1FgOo1OEWfwKn ApeXjiXDrQZb5rhwEjKGOE5uzI6Qohv3LIQgbBUL8rFU3g9FmkmmfdVtMGPpolkueiFzm4maKb8X4LLGiZ PeQfMGFQBW7UzH9PJFsVHecq96W6MVn6xbIiRItnuce61JXf7YWslpM1ktrFVzEF2hyEJSoMAec1Z3z2rEm33CBtOF9snfBky2ePmnioOm1yE8FpkyK7DVXGQEER2Zpz4nBGUalgPCNTQcOf34D4IY2Ucbn5 qMJzF5ibH0ogr6QmeSyRMQ3gWRp92RVpxD5sWQwKoCIagfhxevuLhz5k59zJqW5p82zcGiC3hcf3mMuJJ0IVibzNgepksfKRz19wGpOnnCKJW10jI7eW8EpF1pWdhTdcxZ7IGhMCFwj7ZHCmqNZLArfBI2gZYcKqR6hBDZYyzFj6SZ6J2X74JtFtIdWVasiyZ8gKviEAajZXIO2dn7cwwk17BWuFsP5NZ8l v07haNR0dcYwa9V4Nt3t8o7ZJSlXwELzODYA3WPsq4pUaof2dz8bsB1Fv2Hbe0VarRC9uqkthty1MImPBG5tDNbXZlTU4dh9Ph WIPtudfX3BRmptNHhJ5vPn2NJN41UIj70c0tgwNALFOgzk8NynQ5cGdz7CD8sQufqZPtlaDBV4ndTAgRpIg79DSA8SxN8eDQP4YrT6wDxJMxA9Aaerojes3EiQFc PVjqyqJ0oUDQvNK9rJ1ANrgJrcF jyk8BZtH Dipxg6HXKlDdLB5Tb8NObOnOBesJYHMY2iPQWKHhJc7g1hxJy9aUfdo5J4d9AyNDo83kPbNgqhsJO5tu7ZBaZVsJsV19H26SkHY8Z1vZOlQac7uKnqBZpp5OFwyHMOqIfw2Nf B6pmiF2lE1AlkMdICL2Nqh N8I54R918QZNNXDNtHnZWeLaGRqmS9DZBIwGkMm2COY3naU1IoF6yQY1MccPmebAdTNAmey1ArqvZCek5EXCJOoasrRE3qBIUSZXlU87odvxNCKJ78pZeP7U8Ed7RrnN3SbiDyEiY c7eDjdF4AAzcEr2 UlGGznQxBDriVuWBRWugpdIufzu5rk9KUe13Sa 5fPTAoHNXyjRIDObArGnjBHjPHPFM4nxyhk6mm2JCCYfNhKUmL5CBEf9jImdwRpu3KxQ1mv7bH9vKUWPcLMpVoX5P5gXvN1eOI0ZYyPoMDLd7UvcOrnjXL  2t4E0GG8TBRqLfbCLqyuBaePrnA0lIPHGQLMDoPe3IBidztyAhR KwoCWrwt2QbmvYs3KRaidfYuvMQ2 IlxUazVSZgJnc4PIpg cZkIWaTuQakpDyvJozz3yL2F4RIv14GovVvTq9QTpYkOvqHZxolngw0qpGbMeALhwFlWGpot5jgqeQjA VYA72jb2fxoWBl45AnqdW1czHYXG46kdRnUzrCenkF0mAkDuV0gRPY222BC7uWHAn6PTEWgDB3HyoBqPvanbc6s2ccdzSHJ4YJQWfAX td7UqFApODVkTbW6G7mjzuCeSpMoULyouH q1s0LjyECDXokV1Kri KhWGJUugEuxquue vh9AVw09QW fhya0F8ZmKVqD78G9EFbpMQjvOvgPlmCcvUmnxi3PXFDNkJG8WRPzocUVe3PTw0E3eEHghOKiEB4u0Xvt2Hb2esODlsJ5Uajn7B46Bq0w3W55MDUw0U5i8CP6QDrizWsQOYQOCF3vpLGOCVIyeleOWkVPz51u30XZCD7jKlRYvYOw2Rxocfq2YdbPZcvhPN7iRT ToHlNUY' AS literal
      """
    Then the result should be, in any order:
      | literal                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
      | 'Qu7cFy732T2KJBCJzyY2xP7fWr4bhg7mdQALjUcVNa2nW2vIfAYMDxd4 ZGSe8g52kVWAiYI5K9SnVH2lMc7Uvh4M9hrvBUs5CPrAIjq9OwgxbVtZcfSrQgRe7hbkx162n0SNvY3KvqBBT5gyhTe4cG2BwJjFx8y11zpf0zyLpnYeQtd6V5maSx9tBigoLnjWdu9pjZ3aycAY8ZpzzOoBniPWThl1ydWyA8E4blXlzkeXnR9GY2UCpHpdmsg5u0GkF4phyqPt61 QRUiJBFXIHDx0zljppa vNLVbIaz8AqM7CGXU5796XKbiCX6uM9WRJXtUooJBJv0uHowr1tey4GQEL4t7j0tE4MznU9X7gRx7BMQGREyCBl5yR6qstIuMKug95TsVxUK3uE1oE5VsS68GlnL6IBAeNhsNMTA4kEflKNI2XKYGf4aDBLABvRa5Qbm12JpccslBbaILFQgQkPBy5nPRfh9Brjpyif1fPPkFB1rJIn 2z4G4irjFafOMuB 4JFTJnvj3 65yEbX7bNtgEF4oB7b7On8DVUAfFQfSz6T1SAFnOatwsNTts6dcH5JewU3jkS4TihfDUvAvw sjo0qoNxowKCoOtOUybt31Xg2mpeV5y5lyxZCSBkqjADNwLglwVcFa08Go3gU qP xs Hrw7ZmQ6vcy6oS6UH R3cJBUKWslkZKEYhXct3duSSWsnn8QFzKm6B4U6dmYXttjjVED0tqPXQ2vwp9eN8jJPebjZfT453810lZM9cQlfOhLdgsSaNaszT8t9pbPC5SrPPPIaXKF2IwRY3uMqAtTJD03bW o8dA3ZqT9igCrKRRfVo5j82HfUzjm2kBh4VT3UXfLGyTnnWqBqQ5WUbmdQQNfiMqGpBIcktEhov1XlJ6DyAzrn 1s yDyQS4Pjqg6y7NHl09nnJ3aMOxdDE7BHv4HVethC3Db32LHv6ZW9zotdOZ8tSH2AGKwhND6cfum67hSXu5OsAGeLZxrrMIn9ml9VWZj8Qxar 3lw3OM2jeUB62REWg7lxTJp3zVuaCQgejCGh40wOPR4vYtyzLdFxsxZ2qwn3XvnO2Xw25KckV8dstFfv4w9NFe03VTBWhoYkuSl0j3eCB1absxURBvss7ReatCgqonoVtkwD5RgknklJg12R56ikPOa9akQwEY ri5X8xDrKyqo2FXrj Np8AmXc4nx0yxydL4yF6WVk J9HmgHjGP0M3dMFOl0n15BUPyTAQNQhAHhDcGjt3jvTqKDW A4GG6gK2xn7hfdgAuoDj4h1lMZsSyYIGTnV6Zig8Nlmtwtss9kjCx 234UQbVuBD96JXbrjmY5jHd7c10KRvUFFzlGcdTscUUi38q6f0czcpoeT8MFBgEbrAw2b50fzz5tLhBGJGeKE0ndK64LOWP0olrS0voljEXYRiLMEArn1bkNUcaOgtQHzoV1Pqp6CR4suZxza66QcNOPH CoSuReOfjYOs1f0hWQ2RU2BUg1vJ5OyRPxAZ81195eJg82WgMFxIo 3EwNLUH6j3D41mu9G2L4ckbETdQRy8PEeM1KSIIjEBLD7xJdXFneolAbsv81mKzrWYRXw0pA8hTI4aIFFQSE8aaUkPUmCE0hzUENcHeNNHMK2UqsClOAdxRiz58hrzdUROac 7UM97kncRVWBSuW4GtISDrgBoEAJQqR2IFIh93W9wKCrESYtjf5uGLzEsGn3l0b2B0jXBoTkbd05jweOTk9LUOgpeNGBNWlpinKda9ny3OfjjCIZx3NnVqsxYiFeV0r4EgE4Vd5QypPNSoQN7rNx2aGufdT52tf1tGeK2d9uVgjDKIjJjZsDJhmnaOUbT5KPYb7fDJ4FJUcl22SMtXAkmQZTbXxGAkyve2SD6pyNB6ShBJ9LkeJPKDWQybSdRD tlQnHVqboE9iYdYOQSblltZwiQHMZcy4eiUHqW7uJ3Mve7bwRZLXYgJEoHeR7E8MXc0SpbVLpbEKEItiqFoi0XEhPGrRvE1PUhphlwiTJBXoLdGO02G97kpy2E8AZtFwboyuW0TXMyEg3bgAP TvGBrbtHyuYfbX6TC1meqTQOGTEMUBjz2VzRB ouL nUpSH7DojvQdxGi8F13xP12K 3IDVZX3UkPAsDgdChHvG5mFiSAaOWBZzUGbGTBkW52NtUQCMkzwYoCNwooNh5Ewk9rNafQQCsrmwaZQGrV pl4u9dBgedBtOeVF7SbxDdOewY uOb1TxLPn9CLwY7KY7igUGZ1prFMUqQ6IsmDLebpOIlG uKI7Xkar6hoRj1Xm8yWPf9o5qkGk agGuD4HrZOA2CtNVsWKiWnV09NLSBd5LdVkhjDbCFGRevIHO1aPCHTPpkml0EStzJdDHVtmGt6EYkbTXUZz7UZs8gKxNs950gEG 4Vtj98io9N0xNbO8FjLL lIqo4LunkmUs0otjT3gmshVAVTwQ0SjCRhqBs10NqVHAT9jCv J3s4mRSoirWeWw7UtzqRc bYtZrpvzmKvP 9lVvuOlEvWhcufv2VUQniDZFYE2EDtNCWrAqiodSAeX5eHEbfbQ5CwJjDjpBHJwoa7lPcZpt43nsXDLvZoIJZPzRPWOzDbt5u3loDI8aYrF2HOmpZ1Lrei XVV3DGYok8M5cWFgfaDILw8sa3kmDDJ2erUPblmMJZZB9eEOLnvEl5O9ALYbBBpnVTnLJvedw9uPVr1HXDmNWgAVpUFYXxKeQVReEFkHT29vENZGi3g7Bv2VUgEx5BxTlHGa13Kmge9QliYARWNfhBPjWQoP2ZRoKCDalsOCeohq2pNOKvkgZOy3AwfpFykBoUjtsvI7NAg6zVhCtCSo6PHcryDgAYYRF737e82qLpjkbCpMozebQRoGrZ7deTFTy TZCiOP2nGOKWiMnGq1daw3uAOx3ntthuZR1viQ8qmyXiIaBwJF5REqFJbZdPvRTpXns8vsG9PsXu DkPiWh3LieaiMGM3zyBsdFheatoBnj0ccBSsiKSDH SmVyBPw8K5vAeVA5WQy8LXX27mzhA7rlrXdWH8kMmtK15lR2AHE7XmSrzGaUbqWGRzmfrTDM vJPKZ8y73x8jhCvVK34nqFZbvlIRdYaUfWjQIGhdJ60V0JMJsh3bvYMDOlDnviPgT5MoAP6LszNwTp4O4yzdxgmq7CY48bQigcLRYEmg8ZWBU6ekc0Gk8Uuj3qC2Oy4DviJoC5Sy68xnl762KjXseDWuO0US6k5NCcztEWuB41AhFLjT Xlfv7dJNvDvyrTwYbnapgnqRTq2fD0NlkKq 0Wmjgv8HRMAUOU4Sfh2PNem 4BK4fBQKbzZWjK8Mjh4quPQr23P4K3qfVfyqGU9Y7HWPRiaz 86zjtl0Gu6DGo92GqPEGNBs RVMTebDPNWQWZju4bqF01z9jnsyzLbG1PD5bqdZccxHK9E bD9AM0KjsT3bSvhG4wCqIUOH9VBFKARnrscsgtF7sbmiBwtt3RfX9cddLMWn8lxh6swaE1pFyN8sg4qRhjVBHv0viacoxg7glAHAowSaqJXKRUWO0wBLz7esMhv9H44d6ztNLrgfays65REWjKWuMe4RsSP7VLGrQRvG6QKZ2GyI5K3WdQRRsPl2QrSxzCEHR1feQLSkngRpWAi4Gwt0ZUHzTGLMZeDQpG9fYWjSRfuPBWm4rHYyI0ny6WmqZa3yi3zeaHXKsNMMxV5RhI3wcY3UdgRBNTG1 yogATPH JYM5tSqE3M6tPgUumwH3qba 7a9XZcAJF7MYjb214yDndl8CYcQiJ9xUnyta9DToaXdLDFMOxIWdv4Oc Ae 092ASura8P5qig9RUZAwUpWiJTnCz6fSEkb1XHzAgW4HwrczuFFGsRNAUY5cReitkmwpFhf4Jz8KHHbUj8fbDROSfdsmjInlHnwLsB1sjfvZG6vk3LffL78GSIZ5fPfDnFm3rc2A0AWP0Abu539HMhSFd967byWCgpKqWCyMBjW1b6ool1XPus5gM0hx10WdSbMsEpYRR2SwicTxN18oIR4pJaQkE6or9TX6rz9vV6ZEyb4 ud wHyp1I227JdmFLT79kilRqj9K9xWnDR7SlCYSrIVavAnAa1vp4OF4fIQv5ER0Yj61PgmVQQWorwnGK4B9ArBshfyu CTzvR2isHgEpXVRg q2c4c4u7S19M 2PlDrcryc1M0HR1oBmdAsy mIV0E8BR 5E4xi5ZmrKMCXnpH7jURkiDLcu6bsOBufpLbEhKCaFJoC5r3nKY59nohuSWOigeOkEIcdCJt3VaQdwL1doyWzdpG0lUsCP9ZzzIB5oOp5RGgkoGiAh 5WSB5gHlpeK7lDPm2JEulXLeh97fRmSxe4nOVgyGscjoFfi9PgFqDuntZZwsNLiiMfsX8W 97fDeOT0TWvHw7JuioLjxDtOOOBrnZlKkUZQ7CRy7ch38tA1DzJOcCb178efuhtH91QrhoHJn6csVBRrg0DL98BGshITV Rojhsgq7j4NSLircpRgENiVRh49HigUtgwH5AK7xIAjMpD1ky gLFMqpfp4l9vlNrBhTpPDCI1R9UQMeCpiSXnJ9UjtL4uoXfmraI9xY4yVxVZFBXyhhk BaCRXp92qhUege4cIsMfK47FVJLIXzqn3Nu1TPmVyxQmmqXw7NLvVVu12x3DRrsi8ouiedz1KwDXmDhR4cLlnnHSei62MXC0elxELoUAooeyWnLPj6irfATHZ2BvdHUHNXLMq0xqqwzWDsQPklXiI5UPrCi6LfKDvwa38SAyF460vkacS92lPRdrh9S7xjhUOVN7mvjRYdnCU5I5sNiBsQqiuo8aA3GjQkXO0zBnddviQinlSjDEqB97aqZlviAgLTYtM8nbN1tWUH8gayIEPcpC4GyC37WCRiRg0hgyeXbs9sA1nHm5pIZ6sWY33A849nLfYF28C1TB27YPGTlrbCGIZEB4j62BvYUUAxmVo8VXS3hqegl2NPEKX8viEqv qwJZn1YBNjXRlJ1CHd6kqi48 udquQQT4XJTCMpfzbS9HOpXq4SRZmJDrqgXSsY4HPGc xk8p2ZRBodSSpKH3z6YOJ6tdOJ8BRqrymXoIsE1YK63BLSSyD437qwJedJzpHUMiLRZWJ 5FTcYrdWUIh4d I98rGjwjmlAdzEKMtXl0aimE 3hQ2T14pGWF2BlIKQPiX Q2FlSssswVhXtfdUdaBSlBXSk1e2JXVh4a2X5F ENUoTSbAgRHm 0jeYe9Mgw7BAOv1IXWzqfEpBgca0DnbIaDhYGojuvYb3ZKygKzsEXWF9ybgSNdMXARHYfNru2MoI9EKQHEcAHwwBWWKevcr92SnF83UyNyoyATmfb76bqggDHg0e4OD7FYyQ16VhLFowFGew7OhN16urh5 SU9JxECvjmbpe3mY83MOtZR65FRq3FaxYSsEDgI41Ce3wsNgkUXaxmiUw8M6FUFwihz8ZEihfxMb41EAnafjOUo66tfs1bzzWFvGuuEXfLeHOs07YF7YSmwhs6smrP3SkWXJCQfEjr9kn8sGB2VBpmO7aTiIdGHBa2u hyjkJrTu64n54dknHBPMl2Yc nyEoHucwalDRjPBhPNTAenytix29MsVEFvnaEqgxkB1DbdbifGvkWAt9t86BWvbgE2hIPAGA6zcm43Wzg8ENZCLqVoGSAFe ZjpptB4c84l a1XxUUxo7fmmDdkFNaTZP6UFmkzFnhDt3NB Dzom5Px h5CEHIvdgRSbdBr9tlLkm9gBTbS3fTYjPTPBnnGyUZnOhLMS8CExBvaAdxh6lmprWxyfaLOfi4uqmDQ5VGmjexWZin2Q7QQBSDZaLoSImoZ0TytdMvwpdIHQysLtvdLUJ9Jmklz4C cwZM538cCfD97iMjkZ sGB95sShsGhgNCUwR35cmjMJfVuFtppu4iU3AZkXs0OyKFUxBMhLEHQYBM0U9H rV0rHJDW0LirrncRqtLBOvcj bC4jKiSN3slzd v2XbmKBd4tWKKLcgMZmtF99WcteKyYMCWkF62nBVTyZZsyxUWETHOB9O2B7dukuQuGFz28pQhR Qsf7xKo8cwjc66YYWj61OFt4qFO9miVOojp8MR2qhCXdl1tVVHoUPh8WnrEnPWT C9u5co4NUhSAUHwyPuMKbr jhx9u34vJNaAScYvGDKy3wmxB3ogzfWE7n yqN1RvxJl9 mc0vk3ObjaGUYidas4nK2fQaVeNvwebbr dHeLJF0f qHWUoJmBKg6d7owotrQ7beZcYO7J7vZRZv0P26JuM3he8Q hl2Lak9ViLes59a4zfOn rzS9swYagFbPhwll44Q7lfRQzbjs7OO6viaC3aCYPv5BAPB8F9k W6sKpfuY52rpez5W4LoBBmjYMz8j 9Sc5WPXj32Zic fCaM65d eFACBAwnQeJKohksmmx9GPBKEZScTHe0gVqOfKklUv7OITLOVFIXD311e8KoWg2L7RZgiWz1JHNPI1BL9jkY3aQW52b6OGDX LR HQf7WoT3lQF85ICLNVKbjzWUDEL2AOIWK0jxvTnFiDBH7y2b4MpfmAfWBXtUsJJfgUGG2VW3pTFOqQS6rWir6jfvQs43ohSyt68RiZ1CfbR0Y9xY04fWPVsLKRlo9KM4JllXAwwKuSbvRpT4amOtbdkdKEKDPvmA6FQ61cSWayEADwjN8lbpUELdl150T9MjcDDdWZxv7nZ XAj493l8tUZlVGNXZ7OxOyoTf3PyIDCdtN9ut7TDBzpIFlDQhSBAHDY5cs5ct9nLzA6s1DGqdBj4NJPeRiKsPYGHnyqK5CE8S9IAJ 0XIfiJR so8fY9iySAKKECppnRk4hcdoVQhevjFBqAbSG02X1zkaKRXpvGxdWryFYL6TA9fVvRNpwi3JVSnhLslULMTcsnZeIkwN7QHWLDWh29DPXX31g7lLYdYnkiA53ZCCN0EKuwEpToy84vh3Gu8sO6Kv k6tHynKAVz0SentHsh 0LV387w8PQHYdYn7PzsQJ1sNmqIOyTn4Te7z1ElCSgqU0I0ImflD ilxsSUrsqaqhofXMyDkb5ZAaYGtFrhn Ea6 qw5ZCkbws8N8aY4gW90e90k9Rhhg0vE5nD74Rg5awiOA7vtmjn9LOKdLF67j1nVrpIZU4ADStXLwHWX0yCRFdw sfEKYuIrnFOc1sSjOKx fvHOSVGlYqaBv1yKqRBheU hsYupfxA3zzrlsYD71qZ4TmlqayGtK8p5SELT1mD0YG0v9VYPQrSqkrk V4kcPKckonY7zPZKkYbf6b5e22XVE0AWokBiYQwNuyIqEifpkhlc9PrUp13cwWncTlnMWyRDQrlW2i6oRJbMZJoE2Bcy72YMzbqvbcrmXnemI9tUDiHRZi0V1gbtxxvEjw 0 Z5UjDGk0jua35FOBRL4DdYRIawvkbzo7Lr 4PymJ0DrUu3k5IvBhQthdDJG7Dpf8Q4AiyUsZKkied3d7CFLKcpAmZ7up8J0pOcGEN3q0HsIUJ m1oW3acBCBXiYJ2 n JKAteFJPTgCqQzDhNOootC6BJXq4Ju4VUSdfD8poERjuadKYrInUCTKqRgU6H7N8B2lILyF GKnUT4mrxGxDduPrMIKE1wIdCOwAlD7H5V BYKZDF3GGwxsRU9Ktctq3tgatYQyB40VkWSftduesDqH118 2MhhZqYFwq8stqRqhFpYsjHwqY1owy yPnApsBOt7F7P9Y2NPCBziPywkY7nZiRhf2UtSLpWGPWlegIlkMCYtOB fNnPpxotXpOyUiNWcF TpwXxXrUG2PTnHouO2vtQOSS5OkbpDYPMgCNZI Pvc6WAV8H61FnNOaGJHYY8zmKGMNaqZg4XRpbDZKCd34aFJDmu6rXwzOf4LqagfuR6S3shK82phsJvJXpho6pkugIfCiai0Xw9qkUW2NT4DMiomcJmWEwUCnTEsZCUSN0Lxlz6Cm49 Jc8OBtlCYqGwOtQkK2Uqz0CYGxX9zUcu BYH2I00luXU6seC2vcn2ouX3oBmOkfg5GW4whSQJd0ahBvsRAvHMj2YAixGkZM9XE FgJqJYl98YoIUQtH7aOXkZfcgWsojqGo0v8DdZNjYuXJzUEgDzIbD xWwxjf2S1LeLieYDcqgnu6I6WpMlwaCAtReo tY7mLd5r2oxLABi7epYW6oZZrYxwhjZZNw1FgOo1OEWfwKn ApeXjiXDrQZb5rhwEjKGOE5uzI6Qohv3LIQgbBUL8rFU3g9FmkmmfdVtMGPpolkueiFzm4maKb8X4LLGiZ PeQfMGFQBW7UzH9PJFsVHecq96W6MVn6xbIiRItnuce61JXf7YWslpM1ktrFVzEF2hyEJSoMAec1Z3z2rEm33CBtOF9snfBky2ePmnioOm1yE8FpkyK7DVXGQEER2Zpz4nBGUalgPCNTQcOf34D4IY2Ucbn5 qMJzF5ibH0ogr6QmeSyRMQ3gWRp92RVpxD5sWQwKoCIagfhxevuLhz5k59zJqW5p82zcGiC3hcf3mMuJJ0IVibzNgepksfKRz19wGpOnnCKJW10jI7eW8EpF1pWdhTdcxZ7IGhMCFwj7ZHCmqNZLArfBI2gZYcKqR6hBDZYyzFj6SZ6J2X74JtFtIdWVasiyZ8gKviEAajZXIO2dn7cwwk17BWuFsP5NZ8l v07haNR0dcYwa9V4Nt3t8o7ZJSlXwELzODYA3WPsq4pUaof2dz8bsB1Fv2Hbe0VarRC9uqkthty1MImPBG5tDNbXZlTU4dh9Ph WIPtudfX3BRmptNHhJ5vPn2NJN41UIj70c0tgwNALFOgzk8NynQ5cGdz7CD8sQufqZPtlaDBV4ndTAgRpIg79DSA8SxN8eDQP4YrT6wDxJMxA9Aaerojes3EiQFc PVjqyqJ0oUDQvNK9rJ1ANrgJrcF jyk8BZtH Dipxg6HXKlDdLB5Tb8NObOnOBesJYHMY2iPQWKHhJc7g1hxJy9aUfdo5J4d9AyNDo83kPbNgqhsJO5tu7ZBaZVsJsV19H26SkHY8Z1vZOlQac7uKnqBZpp5OFwyHMOqIfw2Nf B6pmiF2lE1AlkMdICL2Nqh N8I54R918QZNNXDNtHnZWeLaGRqmS9DZBIwGkMm2COY3naU1IoF6yQY1MccPmebAdTNAmey1ArqvZCek5EXCJOoasrRE3qBIUSZXlU87odvxNCKJ78pZeP7U8Ed7RrnN3SbiDyEiY c7eDjdF4AAzcEr2 UlGGznQxBDriVuWBRWugpdIufzu5rk9KUe13Sa 5fPTAoHNXyjRIDObArGnjBHjPHPFM4nxyhk6mm2JCCYfNhKUmL5CBEf9jImdwRpu3KxQ1mv7bH9vKUWPcLMpVoX5P5gXvN1eOI0ZYyPoMDLd7UvcOrnjXL  2t4E0GG8TBRqLfbCLqyuBaePrnA0lIPHGQLMDoPe3IBidztyAhR KwoCWrwt2QbmvYs3KRaidfYuvMQ2 IlxUazVSZgJnc4PIpg cZkIWaTuQakpDyvJozz3yL2F4RIv14GovVvTq9QTpYkOvqHZxolngw0qpGbMeALhwFlWGpot5jgqeQjA VYA72jb2fxoWBl45AnqdW1czHYXG46kdRnUzrCenkF0mAkDuV0gRPY222BC7uWHAn6PTEWgDB3HyoBqPvanbc6s2ccdzSHJ4YJQWfAX td7UqFApODVkTbW6G7mjzuCeSpMoULyouH q1s0LjyECDXokV1Kri KhWGJUugEuxquue vh9AVw09QW fhya0F8ZmKVqD78G9EFbpMQjvOvgPlmCcvUmnxi3PXFDNkJG8WRPzocUVe3PTw0E3eEHghOKiEB4u0Xvt2Hb2esODlsJ5Uajn7B46Bq0w3W55MDUw0U5i8CP6QDrizWsQOYQOCF3vpLGOCVIyeleOWkVPz51u30XZCD7jKlRYvYOw2Rxocfq2YdbPZcvhPN7iRT ToHlNUY' |
    And no side effects

  Scenario: [9] Return a double-quoted empty string
    Given any graph
    When executing query:
      """
      RETURN "" AS literal
      """
    Then the result should be, in any order:
      | literal |
      | ''      |
    And no side effects

  Scenario: [10] Accept valid Unicode literal
    Given any graph
    When executing query:
      """
      RETURN '\u01FF' AS a
      """
    Then the result should be, in any order:
      | a   |
      | 'ǿ' |
    And no side effects

  @skipStyleCheck
  Scenario: [11] Return a double-quoted string with one character
    Given any graph
    When executing query:
      """
      RETURN "a" AS literal
      """
    Then the result should be, in any order:
      | literal |
      | 'a'     |
    And no side effects

  @skipStyleCheck
  Scenario: [12] Return a double-quoted string with uft-8 characters
    Given any graph
    When executing query:
      """
      RETURN "🧐🍌❖⋙⚐" AS literal
      """
    Then the result should be, in any order:
      | literal   |
      | '🧐🍌❖⋙⚐' |
    And no side effects

  @skipGrammarCheck
  Scenario: [13] Failing on incorrect unicode literal
    Given any graph
    When executing query:
      """
      RETURN '\uH'
      """
    Then a SyntaxError should be raised at compile time: InvalidUnicodeLiteral
