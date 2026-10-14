# How to improve alter-rebbe.org

## What already matches the best archives

- The guide at `/note/contributing.html` is as explicit as the Bentham or Newton instructions.
- People and places are authority records, and documents link back to them.
  That is the same idea as Early Modern Letters Online and London Lives.

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
