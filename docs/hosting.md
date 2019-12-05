---
title: Hosting
layout: page
---

Static site is hosted on GitHub pages.

Facsimiles are being migrated to a Google Cloud Storage bucket.
- bucket `facsimiles.alter-rebbe.org` created;
- CNAME for it pointing to `c.storage.googleapis.com` added;
- member "allUsers" with role "Storag Object Viewer" added to the bucket;
- facsimiles extracted from the GitHub repository uploaded into the bucket;

Remains to be done:
- change Jekyll templates to point to a configured facsimiles host;
- fix the code that looks for facsimiles in the repository
  (use Google API to get (and cache?) bucket structure?);
- remove facsimiles from the repository and its history;

Maybe:
- add link to the full-size facsimiles;
- upload them into the bucket.
