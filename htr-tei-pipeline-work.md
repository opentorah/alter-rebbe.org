# Archival HTR work log

Record of what Grok Build did on Aluminum while following `htr-tei-pipeline-plan.md`.
The decisions that produced that plan are in `htr-tei-conversation.md` (14 Oct 2026).
This file is the implementation log through the evening of 22 Oct 2026.
Nothing here is a new plan decision.
The pipeline still does not write TEI.

## Where the work runs

eScriptorium 26.07 and Kraken 7.1.1 are on Proxmox LXC 107, `escriptorium`, an unprivileged Debian 13 container at
`192.168.1.238`.
The container has 8 cores and 32 GiB of RAM.
Facsimiles are bind-mounted read-only at `/srv/facsimiles`.
Working files are on `/srv/htr`.
The web UI is on the tailnet only, through `tailscale serve` to port 8080.
Tailscale Funnel is off.
Grok Build does not drive the UI.
It reaches the Proxmox host by SSH as `root@192.168.1.40` and runs commands inside the container with `pct exec`.
Aluminum sends `192.168.1.0/24` through Tailscale.
The subnet router is LXC 106.
SSH to the host uses the YubiKey FIDO key on Aluminum.
The container was not placed on the Frigate VM, and that VM was not shrunk.

## What was held fixed

Training text is the human TEI at commit `4dcc22dff`, checked in before Grok transcription.
There is no per-document order.
Only pages that have a human line transcription and an image are used.
Lines flagged unclear, normalized, gap, add-displaced, or gloss stay out of every training iteration.
Gloss stays on the line record, including the `|:` `:|` marks, until a later segmentation can put it on its own line.
`del` and supplied text are trainable.
`@hand` records the writing medium, not a scribe.
Held-out documents are the scribe proxy.
Script routing is `he`/`yi`/`arc` to Hebrew, `ru` to Cyrillic, and `pl`/`fr`/`de`/`la` to Latin, by `xml:lang` rather
than by the file's main language.
One recognition model is fine-tuned per script, from PP-OCRv6 small, on CPU.
The medium variant and a rented GPU were not used.
Normalization is NFD.
The split seed is 0.
Case C paragraph transcriptions, mostly Dubnov, were not added.
Sofer Mahir was not downloaded.
No weights, arrow files, or facsimiles were sent off the house.

## Counts from that TEI snapshot

1,955 imaged pages have at least one trainable line.
Two pages were dropped because the image file is missing, and one truncated JPEG was left unrepaired.
Kept lines after the iteration-0 alignment, by language:

| Language | Kept lines | Plan band | Result |
|---|---:|---|---|
| Russian | 28,166 | 5–8k | above the band |
| Polish | 4,169 | 2–4k | inside the band |
| French | 828 | 2–4k | short, one file |
| German | 643 | 3–5k | short, two files |
| Hebrew | 0 | 15–25k and at least 15 scribes | cannot be met |

Hebrew is not being trained.
The Hebrew pages were not invented.

## Phase 0 and the pilot

A CPU benchmark lives at `/srv/htr/bench/phase0`.
The alignment library uses Needleman–Wunsch.
Iteration ceilings are 0.40, then 0.25, then 0.15.
A page is dropped when its alignment rate is under 0.70.
The phase-2 pilot was 33 pages: alignment 88.0%, keep 71.6%.
Its held-out character error rate was 0.299 for Cyrillic and 0.211 for Latin.
That pilot directory was not reused for the full run.

## Iteration 0

Recognition of the imaged pages with the public PP-OCRv6 small model finished on 15 Oct 2026.
1,954 non-empty PAGE files were written.
Alignment then kept 34,865 lines, 66.49% of 52,436 diplomatic lines.
Held-out base character error rate, measured by alignment rather than by `ketos test`, was 0.2095 for Cyrillic and
0.2988 for Latin.

Cyrillic fine-tuning ran on CPU from 16 Oct and stopped early on 21 Oct at 3:28 AM.
The best epoch was epoch 2: validation accuracy 0.910, character error rate 0.090, on 125 lines.
Weights: `/srv/htr/bootstrap/iter0/models/cyrillic/best_0.9097.safetensors`.
The frozen test is one page, LVIA 178/233, 9 lines.
`ketos test` on 21 Oct scored character accuracy 93.67% (character error rate 0.063) and word accuracy 68.18%.
The plan's held-out goal for Russian is 7% or below.
This one-page scribe proxy meets it.
Several of its errors are case only.
The small-versus-medium bake-off was not run.

Latin fine-tuning started fresh from PP-OCRv6 small on 21 Oct and stopped early on 22 Oct at 3:27 AM.
Epoch 0 was the best: validation accuracy 0.790, character error rate 0.210, word accuracy 0.369, on 62 lines.
Training loss fell at every later epoch, and validation did not beat epoch 0.
Weights: `/srv/htr/bootstrap/iter0/models/latin/best_0.7902.safetensors`.
Both German files were outside the training pool.
RGIA 2213/035 is in the 62-line validation set.
RGIA 2213/036 is the frozen test, 22 pages and 629 lines of Kurrent the model never saw.
`ketos test` on 22 Oct scored character accuracy 70.46% (character error rate 0.295) and word accuracy 28.95%.
The held-out goal for German is 7% or below.
This score does not meet it.
Another fine-tune on the same seed-0 split still never sees Kurrent.

CPU training used four threads, two workers, batch 8, a constant schedule, and early stopping with patience 5 after at
least five epochs.
`TORCHDYNAMO_DISABLE` is set because Ketos otherwise caches one compiled graph per line width.
The container was raised to 32 GiB after an earlier out-of-memory stop.
The resumed runs did not run out of memory.

## Iteration 1, Latin only

On 22 Oct the Latin v1 weights re-recognized 265 Latin pages on the iteration-0 baselines.
All 265 PAGE files finished, and the geometry was kept.
Alignment at the 0.25 ceiling kept 5,685 of 8,020 diplomatic lines (70.9%).
On the same three seed-0 validation documents, hypothesis-versus-TEI character error rate is 0.202 across 89 aligned
lines.
The iteration-0 figure on those documents was 0.299.
The new training arrow is 220 pages and 5,248 lines.
The new validation arrow is 3 pages and 62 lines, including 4 lines of RGIA 2213/035.
LVIA 178/400 fell under the 70% page rate and contributed no pages.
The seed-0 document roles were not reshuffled.
The frozen German test arrow from iteration 0 was left in place so a later score can be compared with 0.295.

The v2 fine-tune started at 1:52 PM from `best_0.7902.safetensors`, into `/srv/htr/bootstrap/iter1/models/latin`.
Each epoch is 656 steps.
Epoch 0 finished at 5:10 PM in 3:17:21: validation accuracy 0.851, character error rate 0.149, word accuracy 0.434,
training loss 3.462.
That checkpoint is `checkpoint_00-0.8512.ckpt`.
The same process continued into epoch 1.
Early stopping cannot fire before the end of epoch 5.
At this pace, five further epochs are about 16 hours 30 minutes, so the earliest stop is around 9:40 AM on 23 Oct if
epoch 0 remains the best.

Cyrillic iteration 1 has not started.
Hebrew has not started.
Segmentation training has not started.
Pages without TEI have not been recognized.
Unclear lines have not been recognized into the §4.9 JSONL.
No commercial engine has been called.

## Still open

The Latin model may still be split into German Kurrent versus French and Polish.
The plan allows that if one model plateaus, and the frozen German score is the evidence.
A public Kurrent set was not downloaded.
The small-versus-medium comparison in the plan was not run.
Plan §12, added 22 Oct 2026, is the recommendation for one capped GPU bake-off and for Transkribus on the residue.
Neither has been started.
Iteration 2, Cyrillic iteration 1, Hebrew, segmentation training, and the production pass over untranscribed pages are
later steps.
