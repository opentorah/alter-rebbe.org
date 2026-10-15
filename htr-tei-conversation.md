# Conversation: reliable HTR of archival manuscripts into TEI

Leonid Dubinsky and Grok Bot (Chief of Staff), Oct 14, 2026 (ET). Condensed record. The full plan is in `htr-tei-pipeline-plan.md`, and the original is [in Google Docs](https://docs.google.com/document/d/19I4KRrs1wqFrR0qRnhb49CvC5e0adRtTgfMedp0A4B0/edit).

## 1. The problem

**Leonid:** I use Grok Build on my desktop (Aluminum) to transcribe handwritten archival documents from the 18th–19th century into TEI. The originals are in Russian, Polish, French, German and Hebrew. Grok Build is having a hard time, especially with Hebrew. The tools it tries hallucinate or auto-correct, but I need an exact transcription of the original. It ends up cutting the page up and grouping individual strokes, and the results aren't impressive. How can I improve reliability, and how should I change my workflow?

**Grok Bot:**
- Stop using a general AI model as the reader. General vision models predict plausible text, which causes the auto-correction. Splitting the page into strokes makes it worse, because handwriting recognition needs a whole line for context.
- Use a dedicated handwritten-text recognition (HTR) tool. Transkribus is hosted. eScriptorium with the Kraken engine is open source and can be self-hosted. Both segment lines, then recognize each one with a model that keeps the original spelling.
- Sort pages by script and use a separate model for each script.
- Fine-tune on your own pages. Twenty to fifty corrected pages per script take a model from barely usable to only a few percent of characters wrong.
- Export PAGE XML or ALTO, which keep line coordinates. Use those for facsimile links in TEI.
- Use Grok Build only to write a deterministic PAGE/ALTO-to-TEI converter that never touches the characters. Verify that the text is identical before and after conversion.
- Mark doubt explicitly with `<unclear>` and `<choice>` instead of silently correcting.
- If a general model is used at all, give it one line at a time with a strict diplomatic prompt, and check it against the HTR output.

## 2. Which tool, given many scribes, and how to automate

**Leonid:** The facsimiles of my sources are on alter-rebbe.org. They're written by different scribes, so training is going to be a problem. Which tool works best, and how do I set up an automated pipeline?

**Grok Bot:**
- Use Kraken as the engine and eScriptorium as the review and correction interface, self-hosted on Proxmox. Kraken handles right-to-left Hebrew well and has a command-line tool that's easy to script. Transkribus is quicker to start with, but it costs per page and is harder to fit into a TEI pipeline you own.
- Having many scribes is fine. Train one model per script on many hands, with a few pages from each scribe. That generalizes better than many pages from one hand. Fine-tune from existing public models, and add a page or two whenever a new hand appears.
- Pipeline: fetch the images and metadata, `kraken segment`, route each page to its script's model, `kraken ocr` to ALTO/PAGE with confidences, review low-confidence lines in eScriptorium, retrain with `ketos train` while tracking CER on held-out data, then run a deterministic TEI conversion.

## 3. Constraints Leonid added

- **The facsimiles are stored locally on the Proxmox server.** He asked for a plan.
- **No discrete GPU.** Grok Bot: it works. Kraken runs fine on CPU for recognition, at seconds to about a minute per page, so batches run overnight. Training is slower, hours to a day, so always fine-tune rather than train from scratch. A cloud GPU can optionally be rented for an hour or two per training round. The plan was made CPU-first.
- **There are high-quality hand-made transcriptions of many documents, already encoded in TEI.** Grok Bot: these are ready-made training data. The plan was revised to extract diplomatic text from the TEI, align it to the segmented lines, filter by CER, hold out some scribes for validation, and bootstrap over 2–3 rounds. Hand correction is limited to about 3–5 pages for each scribe the TEI doesn't cover.

## 4. Result

The plan was delivered as a Google Doc and saved alongside this file as `htr-tei-pipeline-plan.md`. Key points:

- Kraken 7.1 and eScriptorium 26.07, run with Docker Compose in a Debian LXC on Proxmox. The facsimiles are bind-mounted read-only, and access is over Tailscale.
- Starting models were verified on Zenodo: PP-OCRv6 multilingual base models, Hebrew BiblIA and Ashkenazi, McCATMuS, and German handwriting. No public model was found for handwritten Russian or Polish, so those come from his own TEI.
- Throughput and training times are estimates. A 50-page pilot is the first step to benchmark them.
- The plan includes 8 copy-paste prompts for Grok Build, one per component.
