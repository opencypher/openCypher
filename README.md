# The Cypher Query Language

This repository holds the specification of the Cypher graph database query language.
Its purpose is to be central to the process of evolving the specification of the language that is Cypher.

## Overview of the process

It is our aim to make the process of specifying and evolving the Cypher query language as open as possible. "We" are the Cypher Language Group, put together by Neo Technology to govern the Cypher query language.

While we aim for the process to be open, this does not mean a public democracy, as all decisions are made by the Cypher Language Group. That said, we are of course extremely grateful for comments and suggestions on how to improve the Cypher query language.

### The Cypher Language Group

The Cypher Language Group consists of:

* [Andrés Taylor](https://github.com/systay)
* [Petra Selmer](https://github.com/petraselmer)
* [Stefan Plantikow](https://github.com/boggle)
* [Tobias Lindaaker](https://github.com/thobe)
* [Nigel Small](https://github.com/nigelsmall)

Andrés, in capacity of being the "father of Cypher", has ultimate say, should that ever be necessary.
Petra acts as PM for the group, calling meetings and ensuring that notes get taken at said meetings.

### Improvement Proposals

Ultimately, proposals for improving the Cypher query language should take the form of a Cypher Improvement Proposal (CIP). These should typically be proposed as pull requests against this repository, proposing the addition of a document following a naming scheme of `CIP{year}-{month}-{day}-{name}.asciidoc`, where "`{name}`" is a very short name of the proposed changed; for instance, the name of the keyword the proposal intends to add or modify, and the date portion of the name is the the initial date of proposing the CIP (typically the same date as the submission of the pull request).

CIPs that additionally come with a reference implementation as a pull request submitted towards [Neo4j](https://github.com/neo4j/neo4j) are very much appreciated, as the latter would clarify the workings of the proposed improvement. This would help make the handling of the proposal quicker, or perhaps even make it more likely to be accepted.

Suggestions and ideas that are smaller or less articulated than a CIP are also welcome and much appreciated; these would best be submitted as "issues" in this repository on GitHub.

### Meeting notes

The Cypher Language Group will publish meeting notes in this repository in order to maintain an open communication channel for the ongoing development of the Cypher query language.

## Copyright

All material in this repository is the exclusive property of Neo Technology.
