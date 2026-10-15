# Archival HTR → TEI pipeline plan (eScriptorium + Kraken on Proxmox)

Prepared 14 Oct 2026 for Leonid Dubinsky.
Grok Build implements the code on Aluminum.
The plan assumes a CPU-only Proxmox host and treats the existing hand-made TEI transcriptions as the main source of
training data.

## 0. Summary of decisions

1. **Engine:** Kraken 7.1 (current release; it adds the PP-OCRv6 recognition architecture and multilingual base models).
   **Review UI:** eScriptorium 26.07, which bundles Kraken 7.1.
   Transkribus is used only for comparison.
2. **No LLM ever touches transcribed characters.** LLMs (Grok Build) write glue code only.
   Every transcription comes from CTC models (Kraken) or from Leonid.
   CTC models emit only characters that appear in their codec and cannot "auto-correct" into fluent text.
3. **Ground truth (GT) comes from the human TEI checked in before any Grok transcription.** See §4.0.
   The pipeline extracts the diplomatic text from that snapshot, aligns it to Kraken-segmented lines, filters it by CER,
   and writes PAGE XML for ketos.
   Hand correction is limited to scribes or scripts that the TEI doesn't cover.
4. **One recognition model per script:** Hebrew, Cyrillic, and Latin-script.
   The Latin-script model may be split into German Kurrent versus French/Polish if a single model plateaus.
   Each model is fine-tuned from a public base model and trained on many hands.
5. **CPU-first.** Inference runs as overnight batches on the Proxmox host.
   Fine-tuning runs on CPU over weekends, or on a short-lived rented cloud GPU, after which the weights are copied back.
6. **A deterministic converter** turns PAGE (and the eScriptorium archive) into TEI.
   A text-identity check runs before and after conversion and fails the build on any difference.

## 1. Infrastructure (Proxmox, CPU-only)

### 1.1 LXC or VM

- **Default: a Debian 12 or 13 unprivileged LXC running Docker**, with `features: nesting=1,keyctl=1`.
  It is lighter than a VM and bind mounts are trivial.
- **Alternative: a Debian VM.** This is the setup Proxmox officially recommends for Docker.
  Use it if Docker-in-LXC causes trouble (overlayfs or AppArmor).
  Share the facsimiles into it with virtiofs or NFS.

### 1.2 Sizing (CPU-only)

| Resource | Minimum | Recommended | Notes |
| :---- | :---- | :---- | :---- |
| vCPU | 8 | 12–16 | Inference scales with cores; run several kraken processes at about 4 threads each |
| RAM | 16 GB | 32 GB | Postgres, Redis, Celery workers; training on CPU needs 8–16 GB |
| Disk (Docker volumes) | 100 GB | 200 GB+ | eScriptorium **copies** uploaded images into its `media` volume, so plan for roughly 1.2× the size of the facsimile set |
| Work dir | 50 GB | 100 GB | Kraken outputs, GT, datasets, checkpoints |

### 1.3 Storage: bind-mount the facsimiles read-only

1. On the host: `pct set <CTID> -mp0 /tank/archive/facsimiles,mp=/srv/facsimiles,ro=1` (replace with the real path).
2. Add a second, read-write mount for pipeline work: `-mp1 /tank/htr-work,mp=/srv/htr`.
3. Unprivileged container IDs are shifted (root in the container is host UID 100000).
   Make the host files world-readable, or `chown` the work dir to `100000:100000`.
4. The pipeline never writes into `/srv/facsimiles`.
   Everything derived goes to `/srv/htr`.

### 1.4 Install eScriptorium (Docker Compose v2)

Steps from the official wiki (gitlab.com/scripta/escriptorium → Wiki → Install with Docker):

1. `apt install git docker.io docker-compose-plugin` (or Docker CE).
   Check with `docker compose version`.
   The old standalone `docker-compose` is end-of-life.
2. `git clone https://gitlab.com/scripta/escriptorium.git && cd escriptorium`
3. `cp variables.env_example variables.env`.
   Set `SECRET_KEY` (unique), `DJANGO_SU_NAME`, `DJANGO_SU_PASSWORD`, `DJANGO_SU_EMAIL`, `DOMAIN`, and
   `CSRF_TRUSTED_ORIGINS` (must include the Tailscale URL, see 1.5).
4. Don't edit `docker-compose.yml`.
   Put all changes in `docker-compose.override.yml` (start from `docker-compose.override.yml_example`, and drop any
   `version:` key or `runtime: nvidia` lines from it).
5. In the override:
   - add CPU and memory limits for the `celery-*` workers;
   - **remove the published Flower port 5555** (it has no authentication);
   - set `KRAKEN_TRAINING_DEVICE=cpu` and `KRAKEN_INFERENCE_DEVICE=cpu`;
   - set `KRAKEN_TRAINING_LOAD_THREADS` to leave cores free.
6. `docker compose pull && docker compose up -d`.
   The site comes up at `http://localhost:8080`.
   On first start, migration-race errors ("relation already exists") are harmless.
7. Enable text alignment, needed for TEI bootstrapping of paragraph-only texts (§4.4): set `TEXT_ALIGNMENT=True` and add
   the `celery-jvm` worker from the override example.
8. Back up before every update: `docker exec -u postgres escriptorium-db-1 pg_dump -Fc escriptorium > backup.dump`, then
   `git pull && docker compose pull && docker compose up -d`.
9. **Security:** eScriptorium registered five CVEs in 2026.
   Subscribe to the instance-owner list or OpenCVE and keep the instance updated.
   Don't expose it outside the tailnet.

### 1.5 Access over Tailscale

1. To run Tailscale inside an LXC, it needs TUN.
   Add these lines to `/etc/pve/lxc/<CTID>.conf`:
   - `lxc.cgroup2.devices.allow: c 10:200 rwm`
   - `lxc.mount.entry: /dev/net/tun dev/net/tun none bind,create=file`
2. In the container: install Tailscale, then `tailscale up --hostname escriptorium`.
3. HTTPS with no port juggling: `tailscale serve --bg 8080` serves `https://escriptorium.<tailnet>.ts.net`.
   Add that exact origin to `CSRF_TRUSTED_ORIGINS`, otherwise login fails with a CSRF 403.
4. Aluminum and Grok Build reach the API at `https://escriptorium.<tailnet>.ts.net/api/`.
   The Swagger docs are at `/api/swagger/` and the schema at `/api/schema/` (new in 26.07).
   **Generate the API client from that schema rather than guessing payloads.**

### 1.6 Kraken CLI (pipeline host)

- In the same LXC (or a sibling one with the same mounts): `pipx install kraken` (Python 3.10–3.13; pin `kraken==7.1.*`).
- Note: 7.0.2 pinned `lightning` after the compromised lightning 2.6.2/2.6.3 releases (Apr 2026).
  Use a lockfile and don't upgrade lightning blindly.

### 1.7 Images to skip

Ingest keeps an image only when both are true.
The path is under a `case/` directory or under `books/`.
The file stem is a document page id: digits, an optional letter, then up to two `-digits` groups, as in `000-1`,
`000a-1`, `138-143-1`, or `749`.

On 2026-10-14 that rule kept 2,964 of 3,035 images and skipped 71.
Images outside `case/` and `books/` are scans of an opis, a fund title, or an inventory volume.
Some of those files are named like page ids (`001-1.jpg` under an inventory directory) and some are named `p01.jpg` or
`t1-000-1.jpg`.
Inside a case, the same rule drops the RGADA 3140 forgery photographs `*.now.jpg` and the comparison crops `scraps.jpg`,
`363-edge.jpg`, `363-sup-1.jpg`, `363-sup-9.jpg`, `061-1-seakir.jpg`, and `329-line1.jpg`.
The TEI cites those crops and `.now` files with `<ref>`.
The document page is the earlier scan.
Inserted leaves such as `000a-1.jpg` stay.
So does `138-143-1.jpg`, which `137.xml` uses as a facsimile.

*Footnote on GPUs:* the host has no discrete GPU.
If one is added later, passthrough to an LXC means NVIDIA host drivers, the device nodes in the LXC config, and the
NVIDIA Container Toolkit, plus a `celery-gpu` stanza in the override (see the wiki).
Nothing else in this plan changes.

## 2. CPU-first performance plan

### 2.1 Inference settings

- Global options go **before** the subcommands: `kraken --device cpu --threads 4 -i … segment … ocr …`.
- Recognition throughput flags: `ocr -B 8 --num-line-workers 2`.
  Kraken 7.0 added batched recognition (about 80% faster on CPU, according to the release notes).
- Run **N = cores ÷ 4 parallel kraken processes**, each with `--threads 4`, and set `OMP_NUM_THREADS=4` in each
  process's environment.
  Don't run one process with 16 threads; it scales poorly.
- Process many pages per invocation (`-I 'glob'` or several `-i in out` pairs) so model loading is amortized.

### 2.2 Throughput estimates (my estimates, unverified: benchmark in Phase 0)

| Stage | Per page (about 25–35 lines), 4 threads | Notes |
| :---- | :---- | :---- |
| Baseline segmentation (blla) | 10–40 s | Depends on image size; downscale huge TIFFs to about 2500–3500 px on the long side for segmentation only |
| Recognition, PP-OCRv6 small | 10–30 s | Default for production |
| Recognition, PP-OCRv6 medium | 30–90 s | Best accuracy; use for the hard Hebrew hands |

On a 12–16 core host with 3–4 parallel workers, expect roughly **400–1,500 pages per 10-hour overnight run**.
Schedule it with a systemd timer from 22:00 to 07:00 and `nice -n 10`, so that eScriptorium stays responsive.

### 2.3 Training on CPU without pain

1. **Always fine-tune (`ketos train -i base.safetensors --resize union`), never train from scratch.** Base models
   already know the scripts.
2. Train the **small** PP-OCRv6 variant (3.2M parameters) on CPU, and the medium one (15.9M) on a rented GPU.
3. Compile the line data once: `ketos compile -f page -o train.arrow …`.
   Training then reads pre-extracted lines and doesn't re-crop images every epoch.
4. Use small batches (`-B 8`–`16`), `--workers 4`, `--threads 8`, early stopping (`--lag 5`–`10`, `--min-epochs 5`), and
   `--resume` for multi-night runs.
   Kraken 7 writes checkpoints and an abort checkpoint.
5. Rough estimate (unverified): fine-tuning `small` on about 5k lines on 12 cores takes 15–40 minutes per epoch and
   20–40 epochs, so plan **one overnight-to-weekend run per script per iteration**.
6. Put the settings in a ketos YAML experiment file (`ketos --config exp.yml train`) so runs are reproducible.

### 2.4 Optional: rent a cloud GPU for training rounds only

1. Pick an hourly GPU provider (for example RunPod, Vast.ai, Lambda).
   A 24 GB-class GPU is plenty.
   Prices change, so check them at the time.
2. **Ship only compiled line datasets** (`*.arrow` from `ketos compile`), the base weights, and the YAML file.
   Don't ship full facsimiles.
   That keeps the transfer small and limits how much of the archive leaves the house.
3. On the instance:
   - `pip install "kraken==7.1.*"`;
   - `ketos --device cuda:0 --precision bf16-mixed --config exp.yml train`;
   - optionally join the tailnet with an **ephemeral** auth key, and `rsync` over Tailscale.
4. Copy back the converted `.safetensors` file and the metrics log.
   **Don't copy back `.ckpt` files**: checkpoints can execute code when loaded.
5. Destroy the instance and delete its volume, then run `ketos test` locally on CPU to confirm the CER.
6. Expected cost: a few GPU-hours per script per round.

## 3. Starting models

Status legend: ✅ = record checked on Zenodo or HTRMoPo on 14 Oct 2026.
⚠️ = exists but caveats apply.
❓ = not verified, or not a Kraken model.

| Script | Model | Where | Status / notes |
| :---- | :---- | :---- | :---- |
| All (multilingual) | **PP-OCRv6 medium / small / tiny** (Kiessling, Aug 2026) | Zenodo 10.5281/zenodo.21788410 (medium), .21788405 (small), .21788403 (tiny) | ✅ Apache-2.0, needs Kraken ≥ 7.1. Covers Hebrew (5.1% CER, medium), Yiddish, Russian (12.3%), Church Slavonic, Polish (0.4%, mostly synthetic data), German (1.6%), French (7.6%). Trained with NFD. **Recommended base for all scripts.** |
| Hebrew | **BiblIA_01** (generalized medieval Hebrew) | `kraken get 10.5281/zenodo.5468286` | ✅ CC-BY-SA-4.0. Medieval bookhands; legacy `.mlmodel` |
| Hebrew | **Ashkenazi_01** (also Italian_01, Sephardi_01) | 10.5281/zenodo.5468478 (Ashkenazi), .5468573, .5468665 | ✅ CC-BY-SA. Ashkenazi bookhand is the closest relative of 18th–19th c. Ashkenazi semi-cursive, but still a domain gap |
| Hebrew (segmentation) | **Sofer Mahir** seg models: `BiblIAlong02_se3_2_tl.mlmodel` (main text), `SoferMahirCleanFL06Eb_83_tl.mlmodel` (Main/Margin/Paratext + interlinear "Correction") | github.com/dstoekl/sofer_mahir | ✅ CC-BY-NC-SA. Useful for margin and interlinear handling |
| Hebrew | **MiDRASH Geniza 01** | v1: 10.5281/zenodo.17931017 (has `MiDRASH_Gen_01.mlmodel`) | ⚠️ CC-BY-NC-SA. Geniza material, NFKD, MiDRASH bracket conventions. The v2 record (.18732245) currently lists only metadata |
| Hebrew | Tikkoun Sofrim models | none found | ❓ No public Kraken model located. Treat as unverified |
| Latin-script (FR, DE, PL) | **McCATMuS** (16th–21st c., handwritten + print) | `kraken get 10.5281/zenodo.13788177` | ✅ CC-BY-4.0, NFD. Mostly French; good alternative base for French |
| German | **german_handwriting** (UB Mannheim, S. Weil) | 10.5281/zenodo.7933463 | ✅ CC-BY-SA-4.0. Includes some 19th c. Kurrent GT |
| German Kurrent | **german_handwriting_kurrent_v3** (J. Alze, Aug 2026) | `kraken get 10.5281/zenodo.21747522` | ✅ CC-BY-4.0. Only 12 pages and a single hand; fine-tuned from Weil's model. Use for comparison, not as the base |
| German Kurrent GT | 19th c. Kurrent lines (9,317 lines) | Zenodo 10.5281/zenodo.17252677 | ✅ Dataset. Can be mixed into Kurrent training |
| Russian (pre-reform) | none as a Kraken model; PP-OCRv6 includes Russian | — | ⚠️ Fine-tune PP-OCRv6. The YeniseiGovReports-HWR dataset (Hugging Face, 19th c. pre-reform letters ѣ ѳ і) is possible extra GT; licence not checked ❓. HTR_ru (GitHub) is not Kraken ❓ |
| Polish | no dedicated public Kraken model found | — | ⚠️ Use PP-OCRv6. Transkribus "Polski Wieszcz" (17th–19th c.) is for comparison only (PyLaia, inside Transkribus) |

**Bake-off (Phase 2):** on held-out TEI-aligned lines per script, compare PP-OCRv6 small/medium against the
script-specific model with `ketos test`, then pick the best base per script.
Expect PP-OCRv6 medium to win in most cases, and BiblIA or Ashkenazi_01 to be competitive on Hebrew.

**Unicode note:** the base models are NFD (MiDRASH: NFKD).
Store GT and output in **NFC** for TEI, but train and test with `-u NFD` consistently.
NFC and NFD are canonically equivalent, so nothing diplomatic is lost.
**Never use NFKC or NFKD on the corpus**: they fold compatibility characters such as ligatures and presentation forms.

## 4. Ground truth from the existing TEI (core of training)

### 4.0 Training corpus

Training text is the human TEI at commit `4dcc22dff` (2026-10-09, "fix: new entities and references").
That commit is the parent of `cda36bdb9`, the first transcription made with Grok (NIAB 1781/088).
Do not train on transcriptions added or revised after that commit.
Use the pre-Grok text of a file when a later commit revised it.
There is no per-document training order.
Each script model is trained on the lines of that script from this snapshot, in the splits of §4.7.
Phase 2 pilots about 50 pages (about 20 Hebrew, 10 Russian, 8 German, 6 French, 6 Polish).
Phase 3 runs the same extraction over every TEI document in the snapshot.
Pages with no TEI are not used until those iterations have finished.

### 4.1 Inventory

1. Build a script that lists every TEI file and records:
   - `<pb facs>` and `<lb>` counts;
   - whether `<facsimile>/<surface>/<zone>` coordinates exist;
   - `xml:lang` values;
   - `@hand` / `<handNote>`;
   - which editorial elements are used, and how often.
2. Classify each document into one of three alignment cases:
   - **A:** zones with coordinates;
   - **B:** `lb`/`pb` but no zones (the expected majority);
   - **C:** paragraph text only, without `lb`.
3. Write a one-page **encoding profile**: how Leonid encoded abbreviations, deletions, additions, and so on.
   The extractor follows it.

### 4.2 Diplomatic text extraction rules (a reading of the TEI that matches what is visible on the line)

| TEI construct | GT reading | Line flag |
| :---- | :---- | :---- |
| `choice/abbr + expan` | **abbr** (as written, including `am` abbreviation marks) | — |
| `expan` with `ex` but no `abbr` | drop the `ex` content and keep the rest | `normalized` if `ex` couldn't be removed cleanly |
| `choice/sic + corr` | **sic** | — |
| `choice/orig + reg` | **orig** | — |
| `reg`, `corr`, or `expan` with **no** diplomatic sibling | the text is not diplomatic | `normalized` → **exclude** |
| `unclear` | keep the text (it is the editor's reading) | `unclear` → exclude the whole line from training in every iteration. The model's reading of that line is kept in §4.9 |
| `gap`, `damage` without text | — | `gap` → **exclude the line** |
| `supplied` | drop it (not on the page) | `supplied` |
| `del` (in-line strike-through) | keep the deleted letters if legible: they are in the line image | `del` |
| `add place="inline"` | keep | — |
| `add` whose `place` is not `inline` (`above`, `below`, `margin`, `left`, `right`) | **not part of this line**; its own record. Route it onto the marginal line if segmentation has one | `add-displaced` |
| `hi`, `seg`, `foreign`, `persName`, etc. | keep the text, ignore the markup | — |
| `note` with `place` `end` or `foot`, or whose text starts with `(` | drop. Editorial commentary, `(sic)`, `(?)` | — |
| other `note` | its own line, not part of the host line. Keep the characters, including insertion delimiters | `gloss` → exclude until routed onto a marginal line |
| `fw` inside `l` | keep it in that line. Inline document numbers are on the line | — |
| `fw` not inside `l` | its own line (page numbers, marginal notes, catchwords) | — |
| `l`, `lb` | `l` is one line. `lb` splits it. `break="no"` does not insert a space | — |
| `pb` | page boundary. The page id is `@n`. `@facs`, when present, is the image | — |
| Whitespace | collapse XML whitespace, but preserve meaningful spaces between words | — |

- Every line record holds: `doc`, `page`, `line_n`, `text_diplomatic`, `xml:lang` (inherited from the nearest ancestor),
  `hand`, `flags[]`, the TEI XPath, and `unclear_spans` (character offsets of each `<unclear>` in the diplomatic text,
  for §4.9).
- Lines flagged `normalized`, `gap`, `unclear`, `add-displaced`, or `gloss` are **never used for training** until a
  later segmentation routes an `add-displaced` or `gloss` line onto its marginal line.
  The characters, including insertion delimiters, stay on the record.
- **The same extractor is reused for the TEI identity check (§6.4).**
- The Phase 1 profile and the flag counts are in `htrpipe/docs/encoding-profile.md`, from commit `4dcc22dff`.

### 4.3 Case A (zones exist)

Generate PAGE directly from the TEI zone polygons.
Kraken needs **baselines**, so either derive a baseline from each zone (the bottom spline of the polygon), or run kraken
segmentation and match its lines to zones by IoU ≥ 0.5.

### 4.4 Case B (lb but no zones): segment, then align

1. **Segment** each page with blla.
   For Hebrew pages, compare the default model against the Sofer Mahir segmentation models; use `-d horizontal-rl` for
   Hebrew-majority pages.
2. **Recognize** with the current best base model to get rough hypotheses.
   The CER can be 30–60% at iteration 0 and alignment still works.
3. **Align lines with dynamic programming.** Use Needleman–Wunsch between the segmented lines (in reading order) and the
   TEI `lb` lines:
   - cost = normalized edit distance between hypothesis and TEI line (`rapidfuzz.distance.Levenshtein.normalized_distance`);
   - allow 1:1 matches, 1:2 and 2:1 merges and splits (handles over- or under-segmentation), and skips (marginalia, `fw`,
     noise lines).
4. **Optional character-level check:** `kraken.align.forced_align()` (Python API, `kraken/align.py`) runs CTC forced
   alignment of the TEI line text against the line image.
   Use the score as a second filter and to get character cuts.
5. Write PAGE XML per page:
   - Kraken's baseline and polygon geometry;
   - `TextEquiv/Unicode` = TEI diplomatic text (NFC);
   - `custom="structure {type:…}; lang {…}; hand {…}"`;
   - the TEI XPath, for traceability.

### 4.5 Case C (no lb)

- Import the page into eScriptorium, run segmentation and transcription, then use **Text alignment** (passim-based;
  needs `TEXT_ALIGNMENT=True` and the `celery-jvm` worker) with the TEI-extracted text as the witness.
- Alternative: chunk the text by expected line length and use the same DP aligner at word level.
- Treat this as lowest priority.
  Case B will provide most of the GT.

### 4.6 Quality filtering

1. Per aligned line, compute CER(hypothesis, GT):
   - iteration 0: keep if ≤ 0.40;
   - iteration 1: ≤ 0.25;
   - iteration 2 onward: ≤ 0.15.
   - Also require a length ratio between 0.6 and 1.6.
2. Drop pages where fewer than 70% of lines align (likely the wrong image or wrong page order).
   Report them for manual checking.
3. Keep a `rejected.jsonl` with the reason for each rejection.
   Sample 20 rejected lines per iteration by eye: they are either alignment bugs or TEI–image mismatches.
4. Lines that keep failing after two model iterations go to the eScriptorium review queue as "hard" lines.

### 4.7 Splits: by script and by scribe

- **Script routing:** route by `xml:lang` (he, yi, arc → Hebrew; ru → Cyrillic; pl, fr, de, la → Latin).
  Lines from mixed-script pages go to their own script's dataset.
- **Scribe:** take it from `@hand` / `<handNote>`, or from the manifest column `scribe_id`.
- **Splits per script:**
  - `train`;
  - `val-in` (5% of lines from training scribes);
  - **`val-heldout`**: 2–3 entire scribes never seen in training;
  - `test`: frozen and never trained on, for example 1 more held-out scribe plus 3% of lines.
- Report the CER on both validation sets.
  `val-heldout` is the honest number for new scribes.
- Pass the manifests to ketos with `-t train.lst -e val-in.lst` (the evaluation data override `-p`) and run `ketos test`
  on the held-out and test sets.

### 4.8 Bootstrapping loop

1. **Iteration 0:** base-model hypotheses, relaxed alignment threshold, fine-tune → model v1.
2. **Iteration 1:** re-recognize all TEI pages with v1, re-align at a tighter threshold (more lines pass and the
   alignments are cleaner), fine-tune → v2.
3. **Iteration 2:** same again.
   It usually converges within 2–3 rounds.
4. **Only then** use the model on pages that have no TEI.
5. Optional: fine-tune a **segmentation** model (`ketos segtrain`) from the corrected PAGE files once 50–100 pages have
   reviewed segmentation, especially for margins and interlinear lines.

### 4.9 Readings of unclear lines

Exclusion from training is not exclusion from recognition.
After each promoted model, recognize every line flagged `unclear`, using the line image from the segmentation already
aligned to that TEI line.
The PAGE file from §4.4 still stores the TEI diplomatic text.
The hypothesis is written beside it.
One JSONL record per line goes to `/srv/htr/unclears/<script>/<model-version>.jsonl`: document, page, line number, TEI
XPath, diplomatic text, the `<unclear>` spans, the hypothesis, the model id, and the mean glyph confidence.
Import that hypothesis into eScriptorium as a transcription named for the model, on the page that holds the line.
Do not write the hypothesis into the TEI.
Accepting a reading is a hand edit of the TEI.

## 5. Production pipeline stages (pages without TEI)

1. **Ingest:** walk `/srv/facsimiles` and keep the images that pass §1.7.
   For each image, compute its sha256 and record its path, size, and DPI.
2. **Manifest:** one SQLite table,
   `pages(sha256 PK, path, doc_id, page_n, scripts, langs, scribe_id, tei_ref, stage, model_ids, mean_conf, min_line_conf, escr_doc_pk, escr_part_pk, updated_at)`,
   with editable CSV import and export.
3. **Routing:** script and language come from the manifest (a manual, per-document default), with an optional per-line
   override after a first pass.
   For mixed pages, run every relevant script model and keep, per line, the hypothesis with the highest mean confidence.
   Record the losing hypothesis too.
4. **Segmentation:**
   `kraken --device cpu --threads 4 -f image -a -i page.tif page.seg.xml segment -bl -i seg.safetensors [-d horizontal-rl]`
5. **Recognition:**
   `kraken -f alto -a -i page.seg.xml page.alto.xml ocr -m he_v2.safetensors --base-dir R -B 8 --num-line-workers 2`
   - ALTO output carries `String/@WC` (word confidence) and `Glyph/@GC` (per-character confidence).
     PAGE output (`-x`) carries `Glyph/TextEquiv/@conf`.
   - Keep subline segmentation on (the default) so per-character data is written.
   - The XML stores logical (reading) order.
     Don't apply display reordering to stored text.
6. **Import into eScriptorium** via `/api/`:
   - create a document per archival unit (main script and read direction, RTL for Hebrew);
   - `POST /api/documents/{pk}/parts/` (image);
   - `POST /api/documents/{pk}/imports/` (ALTO/PAGE, with the transcription name set to the model version).
   - Check the field names in `/api/swagger/`.
7. **Prioritize review:** score lines by min(GC), mean(GC), and the count of characters with GC < 0.5.
   Build a review queue of pages sorted by worst lines.
   Output an HTML or Markdown list of direct editor links and, optionally, tag the pages in eScriptorium.
8. **Export:** reviewed documents are exported as PAGE (or with the new **Download Archive**, whose `parts.jsonl` and
   `annotations.jsonl` include the annotations) to `/srv/htr/exports/<doc>/<timestamp>/`.
9. **Retrain loop:** reviewed lines are appended to that script's GT (provenance `reviewed`).
   Retrain when there are more than 2,000 new lines or a new scribe has more than 3 pages.
   Promote a model only if the CER on `val-heldout` and `test` improves.
   Record the model version on every page.
10. **TEI conversion:** see §6.

## 6. TEI conversion (deterministic)

1. **Input:** reviewed PAGE plus annotations.
   **Output:** a TEI P5 document per archival unit.
   It must validate against Leonid's existing customization (RNG/ODD) if there is one; otherwise use `tei_all`.
2. **Facsimile:**
   `<facsimile><surface xml:id="s_p12" ulx="0" uly="0" lrx="W" lry="H"><graphic url="relative/path.tif" width="Wpx" height="Hpx"/><zone xml:id="z_p12_r1" type="region" points="…"/><zone xml:id="z_p12_l7" type="line" points="…"/>…</surface></facsimile>`.
   Coordinates are copied verbatim from PAGE.
3. **Text:** `<pb n="12" facs="#s_p12"/>`, then one `<ab>` (or `<div>`) per region with `facs="#z_…"` and `xml:lang`,
   and `<lb n="7" facs="#z_p12_l7"/>` before each line.
   - Marginal regions become `<note place="margin">`; interlinear lines become `<add place="above">` (mapped from the
     region or line type).
   - `xml:lang` per line or segment, taken from the routing result or a reviewer override.
4. **unclear / choice:** these are editorial decisions, so they are never generated from confidences.
   Reviewers mark them in eScriptorium **text annotations** (taxonomy: unclear, abbr, del, add, sic), and the converter
   maps them to `<unclear>`, `<choice><abbr>…</abbr></choice>`, `<del>`, `<add>`, `<sic>`.
   Expansions (`<expan>`) are added later by hand, outside the pipeline.
5. **Text-identity check (blocking):**
   - **Before:** concatenate the PAGE line texts (NFC).
   - **After:** run the §4.2 extractor in diplomatic mode on the generated TEI.
   - **Assert per-line equality**, so a diff names the page and line.
     Any mismatch fails the job, and nothing is written to the corpus.
   - Also assert counts: lines = `lb`, pages = `pb`, and every `@facs` resolves.
6. **Round-trip test:** take an existing hand-made TEI, generate PAGE GT from it (§4), convert back (§6), and check that
   the diplomatic text is identical.

## 7. Orchestration

- **Python package `htrpipe` plus a Makefile.**
  - Targets: `make ingest`, `segment`, `recognize`, `import`, `review-queue`, `export`, `tei`, `gt`, `train SCRIPT=he`,
    `test`.
  - Optional later: a small queue, `huey` with a SQLite backend.
    Avoid heavy orchestrators.
- **Idempotency:** each stage's output path is derived from `(image sha256, stage, model sha256, params hash)`.
  Skip the stage if the output exists and validates.
  Write to a temp file and `rename` atomically.
  Stages are restartable after a crash.
- **Logging:** structured JSON lines (`/srv/htr/logs/YYYY-MM-DD.jsonl`) with page sha, stage, duration, model, mean
  confidence, and error.
  Add a nightly summary in Markdown.
  ketos metrics go to TensorBoard logs (`--logger tensorboard --log-dir`).
- **Scheduling:** systemd timers inside the LXC, for the nightly inference batch and weekend training.
- **Versioning:**
  - Git for code, encoding profile, YAML experiments, and manifests (CSV exports).
  - Model registry: `models/<script>/<version>/{weights.safetensors, metrics.json, train.lst.sha}`.
  - Keep large files out of Git (git-annex or plain directories with checksums).

## 8. Phased rollout (TEI-first)

| Phase | When | Work | Exit criteria |
| :---- | :---- | :---- | :---- |
| 0. Infra | Week 1 | LXC, eScriptorium, Tailscale serve, kraken CLI, mounts; CPU benchmark on 20 pages | eScriptorium reachable over the tailnet; s/page measured; backups run |
| 1. TEI extraction | Weeks 1–2 | Inventory, encoding profile, diplomatic extractor with unit tests on Leonid's edge cases | 100% of TEI files parse; flag statistics reviewed by Leonid |
| 2. Alignment pilot (50 pages) | Weeks 2–3 | 50 TEI-backed pages across all 5 languages (about 20 Hebrew, 10 Russian, 8 German, 6 French, 6 Polish); segment, recognize, align, filter; base-model bake-off | ≥ 80% of lines aligned at iteration 0; base CER per script recorded on `val-heldout` |
| 3. Bootstrap GT | Weeks 3–6 | Run §4 over all TEI documents; 2–3 fine-tune iterations per script (CPU weekends or cloud GPU) | GT targets reached (below) |
| 4. Production + light review | Week 6 on | Overnight inference on pages without TEI; eScriptorium review of low-confidence lines; **hand-correct only 3–5 pages per scribe not covered by the TEI** | Review throughput measured; CER targets met |
| 5. TEI out + retrain loop | Week 7 on | PAGE→TEI with identity check; retrain monthly or when the threshold is reached | Zero identity-check failures; each retrain improves the held-out CER |

**GT targets (aligned + filtered lines, mostly from TEI):**

- Hebrew: 15–25k lines, ≥ 15 scribes;
- Russian: 5–8k;
- German Kurrent: 3–5k (plus the public Kurrent dataset);
- French: 2–4k;
- Polish: 2–4k.

Hand-corrected pages overall: roughly 50–150, used only for scribes the TEI doesn't cover.

**CER goals (`ketos test`, NFD):**

| Script | Seen scribes | Held-out scribes | After v2 |
| :---- | :---- | :---- | :---- |
| Hebrew | ≤ 5% | ≤ 10% | ≤ 7% held-out |
| Russian, German | ≤ 3% | ≤ 7% | — |
| French, Polish | ≤ 3% | ≤ 5% | — |

Review effort should drop below about 1 correction per line.

## 9. Grok Build prompts (copy and paste, one per component)

> Common preamble to prepend to each: "Repo `htrpipe` (Python 3.12, uv, ruff, pytest, type hints).
> Hard rule: no code may modify transcription characters except through explicitly specified, tested mappings; never
> call an LLM at runtime; all Unicode is stored NFC.
> Paths: facsimiles read-only at /srv/facsimiles, work dir /srv/htr.
> Kraken pinned to 7.1.*.
> Write tests first for edge cases."
> 

1. **Infra:** "Write `infra/` for a Proxmox unprivileged Debian LXC running eScriptorium via Docker Compose v2,
   CPU-only:
   - a README with the `pct` commands (nesting, keyctl, mp0 read-only bind of the facsimiles, mp1 work dir, /dev/net/tun
     lines for Tailscale);
   - a `docker-compose.override.yml` (no `version:` key, worker CPU and memory limits, Flower port removed,
     `KRAKEN_TRAINING_DEVICE=cpu`, `KRAKEN_INFERENCE_DEVICE=cpu`, `TEXT_ALIGNMENT=True` plus a `celery-jvm` worker);
   - a `variables.env` template with `CSRF_TRUSTED_ORIGINS` for `https://escriptorium.<tailnet>.ts.net`;
   - a `tailscale serve --bg 8080` setup;
   - a nightly `pg_dump` backup systemd timer."
2. **Manifest/ingest:** "Implement `htrpipe ingest`: walk /srv/facsimiles, compute sha256, record path, size, DPI and
   page order into SQLite (the schema in plan §5.2), and support CSV import/export for manual script/lang/scribe edits.
   Must be idempotent and handle renames by sha."
3. **TEI → ground truth (key component):** "Implement `htrpipe gt`.
   - (a) `tei_inventory`: report pb/lb/zone/hand/lang and element usage per file; classify A/B/C.
   - (b) `diplomatic.extract(tei) -> list[LineRecord]`, following the rules table in plan §4.2 exactly: abbr over expan,
     sic over corr, orig over reg; drop ex and supplied; keep unclear and in-line del with flags; displaced add gets its
     own record; `l` is a line and `lb` splits it, with no space inserted when `break='no'`; `fw` inside `l` stays in
     that line and any other `fw` is its own line; editorial notes are dropped and any other note is a `gloss` line;
     inherited xml:lang and `@hand`.
     Flag lines containing un-paired reg/corr/expan as `normalized`.
     Add property tests: extraction is a pure function, NFC-stable.
   - (c) For case B: run kraken segmentation (subprocess, `--device cpu --threads 4`) and recognition with a
     configurable base model; align the hypothesis lines to TEI lines with a Needleman–Wunsch aligner supporting 1:1,
     1:2, 2:1 and skips, cost = rapidfuzz normalized Levenshtein; optional `kraken.align.forced_align` score.
   - (d) Case A: map zones to lines by IoU.
   - (e) Filter by CER threshold and length ratio (configurable per iteration); write rejected.jsonl with reasons.
   - (f) Write PAGE 2019 XML per page with Kraken geometry, TEI text, and custom attributes (lang, hand, TEI XPath).
   - (g) Split per script and scribe into train / val-in / val-heldout / test manifests, with held-out scribes given in
     config.
   - Include a round-trip test against a sample TEI file."
4. **Batch inference:** "Implement `htrpipe segment|recognize`: a worker pool of N processes, each calling the kraken
   CLI with `--device cpu --threads 4`, `-B 8 --num-line-workers 2`, ALTO output.
   Model choice comes from the manifest script routing; mixed-script pages run all candidate models and keep the best
   line by mean GC.
   Outputs keyed by (sha, model sha, params hash), with atomic writes, JSONL logs, and a `--until 07:00` deadline for
   overnight runs."
5. **eScriptorium client:** "Generate a typed client from `https://escriptorium.<tailnet>.ts.net/api/schema/`
   (drf-spectacular), with token auth.
   Implement: create document (RTL for Hebrew), upload parts, import ALTO/PAGE with the transcription name set to the
   model version, poll task status, export PAGE / Download Archive, and fetch line transcriptions.
   Build `review-queue`: rank pages by min and mean Glyph GC from our ALTO and output Markdown with editor deep links.
   Store PKs in the manifest; no duplicate uploads."
6. **Training:** "Implement `htrpipe train SCRIPT`: `ketos compile` the manifests to .arrow; generate a ketos YAML
   experiment (fine-tune with `--load` base, `--resize union`, `-u NFD`, small batches, lag/min-epochs, tensorboard);
   run on CPU with `--resume` support; or `--cloud` mode, which packages only the .arrow files, base weights and YAML
   into a tarball and writes a shell script for a rented GPU (pip install kraken==7.1.*,
   `ketos --device cuda:0 --precision bf16-mixed`).
   After copy-back, accept only .safetensors, run `ketos test` on val-heldout and test, and promote into models/
7. **PAGE → TEI converter:** "Implement `htrpipe tei`: deterministic PAGE plus eScriptorium annotations → TEI P5 with
   facsimile/surface/zone (verbatim coordinates), pb/@facs, region ab with xml:lang, lb/@facs, margin note / interlinear
   add by region type, and annotations mapped to unclear/abbr/del/add/sic.
   Then run the blocking identity check: per-line equality between the PAGE text and `diplomatic.extract()` of the
   output; check lb/pb counts; every `@facs` resolves; validate against the RNG.
   Fail with a page/line diff."
8. **Orchestration:** "Add a Makefile and systemd timers wiring ingest → segment → recognize → import nightly, a weekly
   train, and on-demand tei.
   Write a nightly Markdown summary (pages processed, failures, mean confidence per script)."

## 10. Risks and mitigations

| Risk | Mitigation |
| :---- | :---- |
| **Marginalia and interlinear segmentation errors** | Use the Sofer Mahir Main/Margin/Paratext + "Correction" segmentation model for Hebrew. Fine-tune `segtrain` on 50–100 reviewed pages. The DP aligner allows skips. Region locking in eScriptorium 26.07 speeds up fixes. |
| **Mixed-language pages** (Hebrew + Yiddish/Russian/German) | Route per line using the max-confidence model. Keep `xml:lang` per line in PAGE `custom`. Reviewers can override. The TEI-derived GT already carries `xml:lang`. |
| **Hebrew abbreviations** (gershayim, ״ ׳, abbreviation dots, final forms) and ligatures (e.g. אל ligature, ﭏ) | Transcribe the abbreviation as written, never expanded. Keep U+05F3/U+05F4 distinct from ASCII ' and ". Write a **codec whitelist** per script and reject GT lines with out-of-list characters. Never use NFKC (it would decompose ﭏ). |
| **Diacritics** (Polish ą ę ł ż, French accents, pre-reform ѣ і ѳ ѵ ъ, Hebrew points if present) | NFD training plus NFC storage. Check the codec for missing characters before training (`--resize union` adds them, but they need examples). Run a per-character confusion report from `ketos test`. |
| **TEI–image mismatches** (wrong pb/facs, missing pages, TEI that silently normalizes) | Page-level alignment rate threshold, `normalized` flags, and a sample review of rejected lines every iteration. |
| **Model reproduces a TEI convention that isn't visible** (e.g. expansions) | Diplomatic extraction rules plus exclusion of `normalized` lines. Spot-check model output for expansion patterns. |
| **CPU too slow** | Small variant for inference, parallel 4-thread workers, overnight windows, `ketos compile`; cloud GPU for medium-model training. |
| **eScriptorium security / upgrades** | Tailnet only, no Flower port, `pg_dump` before every `git pull`, follow the CVE feeds. |
| **Licence** (NC-SA models such as Sofer Mahir and MiDRASH) | Fine for private research use. If models trained on them are published, carry the same licence, or base them on Apache-2.0 PP-OCRv6. |
| **Kraken 7 API changes and supply chain** | Pin kraken and lightning with a lockfile. Generate the eScriptorium client from the schema. |

## 11. Unverified items

- Throughput and training-time numbers are estimates; measure them in Phase 0.
- Exact eScriptorium import/export field names and line-confidence fields: check them in `/api/swagger/`.
- Whether eScriptorium accepts `.safetensors` uploads in the models UI: expected with Kraken 7.1, not confirmed.
- No public Kraken models were found for Tikkoun Sofrim, pre-reform Russian handwriting, or Polish handwriting.
- The YeniseiGovReports-HWR licence was not checked.