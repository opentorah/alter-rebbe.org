# How to improve alter-rebbe.org

Compared with strong history and documentary-edition sites. Written 11 October 2026 from the live site: the home page, the archive tree, the RGADA file, sample documents (021, 063, 289), the name page for Zalman Borukhovich, the about page, the transcription guide, and the news list.

The site is already a real scholarly archive, not a brochure. It publishes facsimiles and diplomatic transcriptions of the 1798 and 1800 arrests of Rabbi Shneur Zalman of Liadi, and of later Chabad files, in TEI, with archival call numbers, three calendars, named transcribers, and a person-and-place index. The suggestions below keep that purpose. They do not ask the site to become a Chabad magazine or a biography.

## What already matches the best archives

- Archival address is the spine of the site: archive, fond, opis, file, document, folio. That is how RGADA, RGIA, LVIA, NIAB, and CAHJP expect to be cited.
- Transcription keeps old orthography, deletions, hands, unclear words, and abbreviations. The guide at `/note/contributing.html` is as explicit as the Bentham or Newton instructions.
- Each document can name author, addressee, date, language, transcriber, and abstract.
- People and places are authority records, and documents link back to them. That is the same idea as Early Modern Letters Online and London Lives.
- Dates are stored as Julian, Gregorian, and Jewish. Few public archives do this.
- Related copies are noted (French translation, Russian draft, Hebrew original, Dubnow’s print).
- The corpus is open (CC BY-SA), the source is on GitHub, and a reader can send a correction without agreeing with the editors.
- URLs such as `/rgada/063` are short and stable.

## What a first visit shows

The home page is a long list of files. Its title is “Дела | Документы”. There is no sentence that says who was arrested, when, or why these papers matter. A reader who searched for “Alter Rebbe arrest” and landed here has to already know what RGADA, opis 2, delo 3140 means.

Dates in that list sometimes appear as a broken table (`Calendar | Date | Julian | …`) or as the raw words “Calendar Date Julian 1798 август 11”. On a document, the same date is one dense line. The three-calendar work is invisible as a feature.

The name page for Zalman Borukhovich is a flat dump of every snippet that mentions him. It does not say, at the top, “founder of Chabad, arrested in Liozna in 1798 and again in 1800,” and it does not offer a list of documents sorted by date. The variant spellings are there (Зальман, Зельман, Соломон, Шнеур Залман), which is valuable, but they are not labeled as variants.

The news page stops in August 2020. The about page is clear and honest, and it is easy to miss.

Unfinished catalog records are public: “???”, “UNKNOWN”, “ISBN-10(13)”, and an empty RN B file. That is honest, and it also looks abandoned.

## Comparison

| What a reader needs | alter-rebbe.org now | What strong history sites do |
| --- | --- | --- |
| A way in | Archive tree only | Founders Online and Old Bailey Online open with a search box, a short explanation, and a few paths (person, year, topic) |
| Search | No search across the transcriptions | Old Bailey, Founders, EMLO, and JPress are usable because the text is searchable |
| Reading a page | Transcription alone; the scan is a separate link, and only if the page number is a link | Newton Project, Old Bailey, and Transkribus put image and text side by side |
| Old spelling and modern reading | Diplomatic text only. A modern Russian reading is promised, not offered | Newton offers diplomatic and normalized text from the same encoding. The TEI already has `sic` / `corr` and `abbr` / `expan` for this |
| Who this person is | Backlinks | EMLO and London Lives give a short identity, variant names, and a dated list of documents |
| Where it happened | Place names as links | EMLO and Mapping the Republic of Letters put the same places on a map |
| How to cite | The archival path is in the breadcrumb, not in a citation | Documentary editions give a copyable citation: archive, folio, URL, and date viewed |
| Language of the site | Russian interface. Documents are Russian, Hebrew, Polish, and French | The readers of this story search in English and Hebrew as well as Russian. Chabad.org and Mondshine’s *Kerem Chabad* edition meet them there. The edition can stay Russian and still offer an English and a Hebrew front door |
| Status of the work | A reader cannot see what is transcribed, what has a scan, and what is only a title | Transcribe Bentham shows “not started / in progress / complete” |
| Parallel texts | A note says that 063 is a French translation of the Hebrew testimony and that 289 is the Russian draft | Sefaria and Newton show the two texts together |
| Images as research objects | JPEGs in a bucket, opened by exact URL | Libraries now serve images through IIIF, so a page can be zoomed, cited, and compared |
| Editorial silence | The about page refuses critical interpretation. That is a fair choice | The best editions still add a headnote of a few lines: who wrote this, to whom, and which document it answers. That is cataloging, not a commentary |

## Do these first

1. **Replace the home page with an entrance.** Keep the archive tree, and put it one click away. The first screen needs four things: one paragraph (the 1798 and 1800 files, and the later Chabad papers); a search box; three doors (by archive, by person, by year); and links to “About”, “How to read a document”, and “How to help”. The title of the page should name the archive, not the word “Документы”.

2. **Fix the date display.** Show one line: `11 Aug 1798 Julian · 22 Aug 1798 Gregorian · 10 Elul 5558`. Stop printing the words “Calendar Date” and stop dropping Markdown tables into the file list. This is a defect, not a design preference.

3. **Add search over the transcriptions.** Search by word, person, place, year, archive, and language. Search should match the diplomatic text and, later, the normalized reading. Without this, the only way through 376 folios of RGADA 3140 is the table of contents.

4. **Put the scan beside the transcription.** A two-panel page: image, then text, with the folio number in both. If the scan is missing, say “no facsimile” in the margin instead of leaving a dead page number. Credit the photographer on that page (Elena Volk, F. Z. Shvaiger, and the others named in the about page).

5. **Give every document a citation box.** For example: `RGADA, razriad VII, opis 2, delo 3140, listy 63–78. alter-rebbe.org/rgada/063. Transcription by Evgeniia Shlosman.` Add “copy citation”. Offer the TEI file for download from the same page. The repository already has the XML.

6. **Turn the person page into a record.** At the top: the standard name, the variants, one sentence of identity, and links that already exist (Wikipedia, the YIVO-style encyclopedia, Brockhaus). Under that: documents sorted by date, not a pile of snippets. Snippets can stay as a second view. Do the same for a place.

7. **Mark completeness.** On each file, show counts: documents, documents with a transcription, documents with a scan. Hide or badge records that are still `???` and `UNKNOWN`. An empty record should say “not yet catalogued”.

8. **Write a “How to read these documents” page.** One page, not a monograph. Explain Julian dates, pre-reform spelling, the words *karlinisty*, *kagal*, *misnagdim*, and the path from denunciation to the Secret Expedition. Old Bailey’s historical background is the model. Link it from the home page and from the first document of each file.

## Do these next

9. **A calendar of the two arrests.** 1798: denunciation, arrest in Liozna, interrogation, release on 19 Kislev. 1800–1801: Avigdor’s complaint, second arrest, release under Alexander I. Each event links to the document. This is an index, not an interpretation. Dubnow already numbered the second arrest; the site can point at his numbers and at Mondshine’s Hebrew edition without reprinting their arguments.

10. **A document headnote.** Three to eight lines from data the TEI header already has: author, addressee, date, language, what it answers, and where else the same text appears. Document 063 already has this in prose. Make it the same block on every document.

11. **Parallel texts.** When one document is a translation of another (Hebrew testimony, French 063, Russian 289, the modern Russian reading when it exists), show them in two columns. A reader who has only one language can see that the other text exists.

12. **A normalized reading beside the diplomatic one.** Do not replace the old spelling. Add a switch, as the Newton Project does. Use the `corr` and `expan` already in the TEI. Join words broken only by a line ending, so “нахо- дилось” is stored as one word for search and still displayed with the break.

13. **English and Hebrew doors.** Translate the home page, the about page, the how-to-read page, the calendar, and the document headnotes. Leave the diplomatic transcription in its own language. The about page already lists translation as a goal. The interface is the part readers meet first.

14. **A map.** Liozna, Liadi, Vilna, Pinsk, Vitebsk, Mogilev, Petersburg. One point per place, sized by the number of documents. EMLO is the model. A static map is enough.

15. **Show the license and the version.** Put CC BY-SA and a link to the GitHub file in the footer of every document. Scholars need to know which transcription they quoted.

## Do these later

16. **IIIF for the facsimiles.** Serve each folio so it can be zoomed, deep-linked, and reused in another viewer. A private bucket of JPEGs cannot do that.

17. **A public data download.** One ZIP of the TEI, plus a CSV of documents (id, archive, date, author, addressee, language, transcriber). Then other researchers can check the corpus without scraping the HTML.

18. **Transcription status for volunteers.** A page of folios that have a scan and no transcription, with a button that does not require a pull request for the first draft. Keep GitHub for reviewers. The guide already allows email; the missing piece is a list of what to do next. Say, on the site, that the pencil and ink underlining and the `[…]` for gaps appear only in site-publisher newer than 0.4.0. Readers should not have to discover that in the guide.

19. **Names as a network, only after the list is clean.** Who wrote to whom, who denounced whom, who was arrested with whom. EMLO’s correspondence graph is the model. It will mislead if the `ref` on names is still incomplete. The errors page is the right place to finish that first.

20. **Start the news again, or remove the impression that the site froze in 2020.** If work continues, post when a file is finished. If a section is stable, say “RGADA 3140 was published in 2020 and is corrected as errors are found.”

## Do not copy these things

- Do not turn the site into a biography of the Alter Rebbe. Chabad.org and the popular timelines already do that, and they do not publish the files. The value here is the files.
- Do not silently modernize the Russian. A modern reading has to be marked as modern.
- Do not add background music, daily quotes, or a festival calendar. Those belong to a community site. They will make the archive look less reliable.
- Do not merge commentary into the transcription. Keep Dubnow’s notes, the transcriber’s notes, and the editor’s headnote visually separate. The TEI already has `note place="foot"` and `note place="end"` for this.

## A practical order

The date bug and the home page change what every visitor sees, and they do not require new transcription. Search and the side-by-side scan change what a historian can do in one evening. The citation box and the person record make the site citable. English and Hebrew headnotes are what let the archive be found by the people who already read Mondshine, Wilensky, and Dubnow in other languages.

The encoding is ahead of the presentation. Most of the first list can be built from TEI that is already there.
