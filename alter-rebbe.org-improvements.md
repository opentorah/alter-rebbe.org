# How to improve alter-rebbe.org

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

## Do these later

10. **IIIF for the facsimiles.** Serve each folio so it can be zoomed, deep-linked, and reused in another viewer.
    A private bucket of JPEGs cannot do that.

11. **Transcription status for volunteers.** A page of folios that have a scan and no transcription, with a button that
    does not require a pull request for the first draft.
    Keep GitHub for reviewers.
    The guide already allows email; the missing piece is a list of what to do next.

12. **Names as a network, only after the list is clean.** Who wrote to whom, who denounced whom, who was arrested with
    whom.
    EMLO’s correspondence graph is the model.
    It will mislead if the `ref` on names is still incomplete.
    The errors page is the right place to finish that first.
