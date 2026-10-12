# How to improve alter-rebbe.org

Compared with strong history and documentary-edition sites.
First written 11 October 2026 from the live site, then cut the same day against the static generator.

The site is already a real scholarly archive, not a brochure.
It publishes facsimiles and diplomatic transcriptions of the 1798 and 1800 arrests of Rabbi Shneur Zalman of Liadi, and
of later Chabad files, in TEI, with archival call numbers, three calendars, named transcribers, and a person-and-place
index.
The suggestions below keep that purpose.
They do not ask the site to become a Chabad magazine or a biography.

## What already matches the best archives

- Archival address is the spine of the site: archive, fond, opis, file, document, folio.
  That is how RGADA, RGIA, LVIA, NIAB, and CAHJP expect to be cited.
- Transcription keeps old orthography, deletions, hands, unclear words, and abbreviations.
  The guide at `/note/contributing.html` is as explicit as the Bentham or Newton instructions.
- Each document can name author, addressee, date, language, transcriber, and abstract.
- People and places are authority records, and documents link back to them.
  That is the same idea as Early Modern Letters Online and London Lives.
- Dates are stored as Julian, Gregorian, and Jewish.
  Few public archives do this.
  The page shows the words written in the document.
  The other two calendars are a hover, not the raw words “Calendar Date” in the file list.
- Related copies are noted (French translation, Russian draft, Hebrew original, Dubnow’s print).
- The corpus is open (CC BY-SA), the source is on GitHub, and a reader can send a correction without agreeing with the
  editors.
  The footer shows the holder and a license line.
- URLs such as `/rgada/063` are short and stable.
- The home page is an entrance.
  One paragraph names the 1798 and 1800 files and the later Chabad papers.
  Doors lead to the archive, to people and places, to the year list, and to the full file list, with About and How to
  help.
  The title is “Архив раннего Хабада”.
  A search box sits in the header.
- Search covers the diplomatic transcription, with year, archive, and language filters.
- Every document carries the same header block: description, date, author, addressee, transcriber, and translator, when
  those values exist.
- The news list includes posts from 2026.

## What a first visit shows

The name page for Zalman Borukhovich puts the spellings, the identity sentence, and the encyclopedia links at the top,
then a flat dump of every snippet that mentions him.
The spellings are not labeled as variants, and the documents are not a list sorted by date.

Unfinished catalog records are public: “???”, “UNKNOWN”, “ISBN-10(13)”, and an empty RNB file.
That is honest, and it also looks abandoned.

## Comparison

| What a reader needs | alter-rebbe.org now | What strong history sites do |
| --- | --- | --- |
| A way in | Entrance, then archive, person, year, and the file list. No “how to read” page yet | Founders Online and Old Bailey Online open with a search box, a short explanation, and a few paths (person, year, topic) |
| Search | Diplomatic text from the header, plus year, archive, and language. No normalized spelling | Old Bailey, Founders, EMLO, and JPress are usable because the text is searchable |
| Reading a page | Transcription alone; the scan is a separate link, and only if the page number is a link | Newton Project, Old Bailey, and Transkribus put image and text side by side |
| Old spelling and modern reading | Diplomatic text only. A modern Russian reading is promised, not offered | Newton offers diplomatic and normalized text from the same encoding. The TEI already has `sic` / `corr` and `abbr` / `expan` for this |
| Who this person is | Identity sentence, encyclopedia links, then snippets | EMLO and London Lives give a short identity, variant names, and a dated list of documents |
| Where it happened | Place names as links | EMLO and Mapping the Republic of Letters put the same places on a map |
| How to cite | The archival path is in the breadcrumb, not in a citation | Documentary editions give a copyable citation: archive, folio, URL, and date viewed |
| Language of the site | Russian interface. Documents are Russian, Hebrew, Polish, and French | The readers of this story search in English and Hebrew as well as Russian. Chabad.org and Mondshine’s *Kerem Chabad* edition meet them there. The edition can stay Russian and still offer an English and a Hebrew front door |
| Status of the work | A reader cannot see what is transcribed, what has a scan, and what is only a title | Transcribe Bentham shows “not started / in progress / complete” |
| Parallel texts | A note says that 063 is a French translation of the Hebrew testimony and that 289 is the Russian draft | Sefaria and Newton show the two texts together |
| Images as research objects | JPEGs in a bucket, opened by exact URL | Libraries now serve images through IIIF, so a page can be zoomed, cited, and compared |
| Editorial silence | The about page refuses critical interpretation. The document header is cataloging (who, to whom, when, and the description), not a commentary | The best editions keep that line. A short headnote is cataloging, not a commentary |

## Do these first

1. **Put the scan beside the transcription.** A two-panel page: image, then text, with the folio number in both.
   If the scan is missing, say “no facsimile” in the margin instead of leaving a dead page number.
   Credit the photographer on that page (Elena Volk, F. Z. Shvaiger, and the others named in the about page).

2. **Give every document a citation box.** For example:
   `RGADA, razriad VII, opis 2, delo 3140, listy 63–78. alter-rebbe.org/rgada/063. Transcription by Evgeniia Shlosman.`
   Add “copy citation”.
   Offer the TEI file for download from the same page.
   The repository already has the XML.

3. **Turn the person page into a record.** The standard name, the spellings, one sentence of identity, and the
   encyclopedia links are already at the top.
   Label the spellings as variants.
   Under that, list documents by date.
   Keep the snippets as a second view.
   Do the same for a place.

4. **Mark completeness.** On each file, show counts: documents, documents with a transcription, documents with a scan.
   Hide or badge records that are still `???` and `UNKNOWN`.
   An empty record should say “not yet catalogued”.

5. **Write a “How to read these documents” page.** One page, not a monograph.
   Explain Julian dates, pre-reform spelling, the words *karlinisty*, *kagal*, *misnagdim*, and the path from
   denunciation to the Secret Expedition.
   Old Bailey’s historical background is the model.
   Link it from the home page and from the first document of each file.

## Do these next

6. **A calendar of the two arrests.** 1798: denunciation, arrest in Liozna, interrogation, release on 19 Kislev.
   1800–1801: Avigdor’s complaint, second arrest, release under Alexander I.
   Each event links to the document.
   This is an index, not an interpretation.
   Dubnow already numbered the second arrest; the site can point at his numbers and at Mondshine’s Hebrew edition
   without reprinting their arguments.

7. **Parallel texts.** When one document is a translation of another (Hebrew testimony, French 063, Russian 289, the
   modern Russian reading when it exists), show them in two columns.
   A reader who has only one language can see that the other text exists.

8. **A normalized reading beside the diplomatic one.** Do not replace the old spelling.
   Add a switch, as the Newton Project does.
   Use the `corr` and `expan` already in the TEI.
   Join words broken only by a line ending, so “нахо- дилось” is stored as one word for search and still displayed with
   the break.

9. **English and Hebrew doors.** Translate the home page, the about page, the how-to-read page, the calendar, and the
   document headnotes.
   Leave the diplomatic transcription in its own language.
   The about page already lists translation as a goal.
   The interface is the part readers meet first.

10. **A map.** Liozna, Liadi, Vilna, Pinsk, Vitebsk, Mogilev, Petersburg.
    One point per place, sized by the number of documents.
    EMLO is the model.
    A static map is enough.

11. **Link the transcription’s file and version.** The footer already shows the holder and a license line (CC BY 4.0;
    the repository LICENSE is CC BY-SA).
    Still missing, on each document: a link to that file on GitHub, and a way to see which version was quoted.

## Do these later

12. **IIIF for the facsimiles.** Serve each folio so it can be zoomed, deep-linked, and reused in another viewer.
    A private bucket of JPEGs cannot do that.

13. **A public data download.** One ZIP of the TEI, plus a CSV of documents (id, archive, date, author, addressee,
    language, transcriber).
    Then other researchers can check the corpus without scraping the HTML.

14. **Transcription status for volunteers.** A page of folios that have a scan and no transcription, with a button that
    does not require a pull request for the first draft.
    Keep GitHub for reviewers.
    The guide already allows email; the missing piece is a list of what to do next.

15. **Names as a network, only after the list is clean.** Who wrote to whom, who denounced whom, who was arrested with
    whom.
    EMLO’s correspondence graph is the model.
    It will mislead if the `ref` on names is still incomplete.
    The errors page is the right place to finish that first.

## Do not copy these things

- Do not turn the site into a biography of the Alter Rebbe.
  Chabad.org and the popular timelines already do that, and they do not publish the files.
  The value here is the files.
- Do not silently modernize the Russian.
  A modern reading has to be marked as modern.
- Do not add background music, daily quotes, or a festival calendar.
  Those belong to a community site.
  They will make the archive look less reliable.
- Do not merge commentary into the transcription.
  Keep Dubnow’s notes, the transcriber’s notes, and the editor’s headnote visually separate.
  The TEI already has `note place="foot"` and `note place="end"` for this.

## A practical order

The scan beside the transcription changes what a historian can do in one evening.
The citation box and the person record make the site citable.
English and Hebrew headnotes are what let the archive be found by the people who already read Mondshine, Wilensky, and
Dubnow in other languages.

The encoding is ahead of the presentation.
Most of the first list can be built from TEI that is already there.
