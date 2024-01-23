# openCypher_ersp

<!-- TABLE OF CONTENTS -->
<details open="open">
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#summary">Summary</a>
    </li>
    <li><a href="#instructions">Instructions</a></li>
    <li><a href="#credits">Credits</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
  </ol>
</details>

<!-- ABOUT THE PROJECT -->

## Summary

The original codebase is sourced from openCypher.

We are looking into a way to incorporate queries related to graph embeddings. 

For example (note: this is not specific to Cypher):
```
FOR researcher IN ‘academics’
	FILTER researcher.department == ‘computer science’

	FOR collaborator IN 1 . . 3 OUTBOUND researcher GRAPH ‘collaborationGraph’

	FILTER SIMILAR_TO(collaborator, ‘researchInterests’, researcher, 10)

	RETURN { researcherName: researcher.name, collaboratorName: collaborator.name }
```
<!-- Instructions -->
## Instructions
### For Reproduction:
1. Begin on local machine (or CSIL). 
2. If Docker is installed, continue to step 2. If not, please install [Docker](https://www.docker.com/) locally.
3. If you have an ARM64 architecture:
   1. From the directory, use the command ```docker build -t oc -f Dockerfile.arm64 .```
   - Do not be alarmed if this takes 2-3 minutes to initialize.
4. If you have an Intel (x86_64) architecture:
   1. From the directory, use the command ```docker build -t oc -f Dockerfile.amd64 .```
   - Do not be alarmed if this takes 2-3 minutes to initialize.
5. Then, ```docker run -it oc```. You will open in an Ubuntu terminal in Linux.
6. Next, we need to build the **maven** project. This is done using the command ```build```. Do not be surprised if this takes several minutes to run.
7. Test the build using ```tests```.
8. For more information, see [Neo4j](https://neo4j.com/developer/contributing-code/).


<!-- Credits -->
## Credits
This work is purely research and built on top of [openCypher](https://github.com/opencypher/openCypher).

<!-- LICENSE -->
## License

Distributed under the Apache License 2.0. See `LICENSE` for more information.

<!-- CONTACT -->
## Contact

Will Corcoran - wcorcoran@ucsb.edu

Wyatt Hamabe - whamabe@ucsb.edu

Niyati Mummidivarapu - niyati@ucsb.edu

Danish Ebadulla - danish_ebadulla (at) umail (dot) ucsb (dot) edu
