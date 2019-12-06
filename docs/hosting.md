---
title: Hosting
layout: page
---

Static site is hosted on GitHub pages.
Facsimiles are in a Google Cloud Storage bucket `facsimiles.alter-rebbe.org`.

This is what I did:
- bucket `facsimiles.alter-rebbe.org` created;
- CNAME for it pointing to `c.storage.googleapis.com` added;
- member "allUsers" with role "Storag Object Viewer" added to the bucket;
- facsimiles extracted from the GitHub repository uploaded into the bucket;
- Jekyll templates changed to point to a configured facsimiles host;
- facsimiles removed from the repository and its history (using BFG Repocleaner);

Remains to be done:
- fix the code that looks for facsimiles in the repository
  (use Google API to get (and cache?) bucket structure?);

Maybe:
- add link to the full-size facsimiles;
- upload them into the bucket.
