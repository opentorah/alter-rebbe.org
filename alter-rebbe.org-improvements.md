# How to improve alter-rebbe.org

## What already matches the best archives

- Transcription keeps old orthography, deletions, hands, unclear words, and abbreviations.
  The guide at `/note/contributing.html` is as explicit as the Bentham or Newton instructions.
- People and places are authority records, and documents link back to them.
  That is the same idea as Early Modern Letters Online and London Lives.

## What a first visit shows

Unfinished catalog records are public: “???”, “UNKNOWN”, “ISBN-10(13)”, and an empty RNB file.
That is honest, and it also looks abandoned.

## Comparison

| What a reader needs | alter-rebbe.org now | What strong history sites do |
| --- | --- | --- |
| A way in | Entrance, then archive, person, year, and the file list. No “how to read” page yet | Founders Online and Old Bailey Online open with a search box, a short explanation, and a few paths (person, year, topic) |
| Search | Diplomatic text from the header, plus year, archive, and language. No normalized spelling | Old Bailey, Founders, EMLO, and JPress are usable because the text is searchable |
| Reading a page | On a wide window the current scan sits beside the text. A missing page number is still only a link, and the page does not name the photographer | Newton Project, Old Bailey, and Transkribus put image and text side by side |
| Old spelling and modern reading | Diplomatic text only. A modern Russian reading is promised, not offered | Newton offers diplomatic and normalized text from the same encoding. The TEI already has `sic` / `corr` and `abbr` / `expan` for this |
| Who this person is | Identity sentence, encyclopedia links, then snippets | EMLO and London Lives give a short identity, variant names, and a dated list of documents |
| Where it happened | Place names as links | EMLO and Mapping the Republic of Letters put the same places on a map |
| How to cite | Cite copies the archive path, the URL, the transcriber, the license, the git hash, and the GitHub file URL. The words are not shown on the page. The TEI file is linked from the same line | Documentary editions give a copyable citation: archive, folio, URL, and date viewed |
| Language of the site | Russian interface. Documents are Russian, Hebrew, Polish, and French | The readers of this story search in English and Hebrew as well as Russian. Chabad.org and Mondshine’s *Kerem Chabad* edition meet them there. The edition can stay Russian and still offer an English and a Hebrew front door |
| Status of the work | A reader cannot see what is transcribed, what has a scan, and what is only a title | Transcribe Bentham shows “not started / in progress / complete” |
| Parallel texts | A note says that 063 is a French translation of the Hebrew testimony and that 289 is the Russian draft | Sefaria and Newton show the two texts together |
| Images as research objects | JPEGs in a bucket, opened by exact URL. The transcription pane shows one scan at a time | Libraries now serve images through IIIF, so a page can be zoomed, cited, and compared |
| Editorial silence | The about page refuses critical interpretation. The document header is cataloging (who, to whom, when, and the description), not a commentary | The best editions keep that line. A short headnote is cataloging, not a commentary |

## Do these first

1. **Say when the scan is missing, and name the photographer.** The scan already sits beside the text on a wide window.
   If the scan is missing, say “no facsimile” in the margin instead of leaving a dead page number.
   Credit the photographer on that page (Elena Volk, F. Z. Shvaiger, and the others named in the about page).

2. **Turn the person page into a record.** The standard name, the spellings, one sentence of identity, and the
   encyclopedia links are already at the top.
   Label the spellings as variants.
   Under that, list documents by date.
   Keep the snippets as a second view.
   Do the same for a place.

3. **Mark completeness.** On each file, show counts: documents, documents with a transcription, documents with a scan.
   Hide or badge records that are still `???` and `UNKNOWN`.
   An empty record should say “not yet catalogued”.

4. **Write a “How to read these documents” page.** One page, not a monograph.
   Explain Julian dates, pre-reform spelling, the words *karlinisty*, *kagal*, *misnagdim*, and the path from
   denunciation to the Secret Expedition.
   Old Bailey’s historical background is the model.
   Link it from the home page and from the first document of each file.

## Do these next

5. **A calendar of the two arrests.** 1798: denunciation, arrest in Liozna, interrogation, release on 19 Kislev.
   1800–1801: Avigdor’s complaint, second arrest, release under Alexander I.
   Each event links to the document.
   This is an index, not an interpretation.
   Dubnow already numbered the second arrest; the site can point at his numbers and at Mondshine’s Hebrew edition
   without reprinting their arguments.

6. **Parallel texts.** When one document is a translation of another (Hebrew testimony, French 063, Russian 289, the
   modern Russian reading when it exists), show them in two columns.
   A reader who has only one language can see that the other text exists.

7. **A normalized reading beside the diplomatic one.** Do not replace the old spelling.
   Add a switch, as the Newton Project does.
   Use the `corr` and `expan` already in the TEI.
   Join words broken only by a line ending, so “нахо- дилось” is stored as one word for search and still displayed with
   the break.

8. **English and Hebrew doors.** Translate the home page, the about page, the how-to-read page, the calendar, and the
   document headnotes.
   Leave the diplomatic transcription in its own language.
   The about page already lists translation as a goal.
   The interface is the part readers meet first.

9. **A map.** Liozna, Liadi, Vilna, Pinsk, Vitebsk, Mogilev, Petersburg.
   One point per place, sized by the number of documents.
   EMLO is the model.
   A static map is enough.

## Do these later

10. **IIIF for the facsimiles.** Serve each folio so it can be zoomed, deep-linked, and reused in another viewer.
    A private bucket of JPEGs cannot do that.

11. **A public data download.** One ZIP of the TEI, plus a CSV of documents (id, archive, date, author, addressee,
    language, transcriber).
    Each document already links to its own TEI file.
    The ZIP and the CSV are what let other researchers check the corpus without scraping the HTML.

12. **Transcription status for volunteers.** A page of folios that have a scan and no transcription, with a button that
    does not require a pull request for the first draft.
    Keep GitHub for reviewers.
    The guide already allows email; the missing piece is a list of what to do next.

13. **Names as a network, only after the list is clean.** Who wrote to whom, who denounced whom, who was arrested with
    whom.
    EMLO’s correspondence graph is the model.
    It will mislead if the `ref` on names is still incomplete.
    The errors page is the right place to finish that first.
