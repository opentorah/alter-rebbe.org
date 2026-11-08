# Archival HTR work log

Record of what Grok Build did on Aluminum while following `htr-tei-pipeline-plan.md`.
The decisions that produced that plan are in `htr-tei-conversation.md` (14 Oct 2026).
This file is the implementation log through 5 Nov 2026.
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
On 4 Nov the medium variant was fine-tuned on a Secure Cloud RTX 4090, and the pod was destroyed the same day.
The home `ketos test` that afternoon adopted medium for both Cyrillic and Latin.
Normalization is NFD.
The split seed is 0.
Case C paragraph transcriptions, mostly Dubnov, were not added.
Sofer Mahir was not downloaded.
No facsimiles were sent off the house.
On 4 Nov the compiled Cyrillic and Latin arrows and the PP-OCRv6 medium weights were copied to the rented GPU and
deleted with the pod.

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
The medium bake-off on 4 Nov scored Cyrillic validation character error rate 0.065 at epoch 7, and Latin 0.110 at epoch
1, on the training validation sets.
The home `ketos test` the same day, on the unchanged validation arrows, scored Cyrillic character accuracy 93.51%
(character error rate 0.065, 264 errors in 4,066 characters, word accuracy 68.99%) and Latin character accuracy 88.18%
(character error rate 0.118, 309 errors in 2,614 characters, word accuracy 50.58%).
Both clear the adoption bar, at most 0.080 for Cyrillic and at most 0.139 for Latin, so medium is adopted for both
scripts.
The weights are `/srv/htr/bootstrap/gpu/cyrillic/best_0.9351.safetensors` and
`/srv/htr/bootstrap/gpu/latin/best_0.8902.safetensors`.

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

The v2 fine-tune started at 1:52 PM on 22 Oct from `best_0.7902.safetensors`, into
`/srv/htr/bootstrap/iter1/models/latin`.
Each epoch is 656 steps and took about 3 hours 17 minutes.
It stopped early on 23 Oct at 9:35 AM, after epoch 5, with patience 5/5.
Epoch 0 was the best: validation accuracy 0.851, character error rate 0.149, word accuracy 0.434, training loss 3.462,
on the 62-line set.
Training loss fell at every later epoch, and validation did not beat epoch 0.
Epoch 5 scored validation accuracy 0.827, character error rate 0.173, and word accuracy 0.441.
Weights: `/srv/htr/bootstrap/iter1/models/latin/best_0.8512.safetensors`, converted from `checkpoint_00-0.8512.ckpt`.
The run used 2 days 3 hours of CPU time and peaked at 14.9 GiB.
There was no out-of-memory stop.
This validation score is on the iteration-1 filter of the same three documents.
`ketos test` of `best_0.8512.safetensors` on the unchanged frozen arrow finished at 12:43 PM on 23 Oct.
Wall time was 1:46.89.
On 41,986 characters the character accuracy is 62.81% (character error rate 0.372) and the word accuracy is 18.23%.
The iteration-0 weights on that same arrow scored 70.46% (character error rate 0.295) and word accuracy 28.95%.
The iteration-1 weights are worse on this Kurrent, which neither model saw in training.
The held-out goal for German is 7% or below.
This score does not meet it.

Cyrillic iteration 1 recognition finished at 3:10 PM on 23 Oct.
All 1,687 pages closed, with no skips.
Alignment at the 0.25 ceiling kept 69.8% of the diplomatic lines, and 86.5% aligned.
The training arrow has 1,409 pages and 29,838 lines.
The validation arrow has 9 pages and 123 lines, from the same three seed-0 documents.
On those documents the mean character error rate of the new hypotheses is 0.126, against 0.210 before this fine-tune.
Twelve training documents contributed no page, so the tool's reshuffled score was discarded.
LVIA 178/233 stays out of both arrows.
The fine-tune from `best_0.9097.safetensors` started at 3:29 PM on 23 Oct.
Each epoch is 3,730 steps and took about 18 hours 40 minutes.
It stopped early on 1 Nov at 3:33 AM ET, after epoch 10, with patience 5/5.
Epoch 5 was the best: validation accuracy 0.925, character error rate 0.075, word accuracy 0.644, training loss 1.405,
on the 123-line set.
The monitored score was 0.92541.
Training loss fell at every later epoch, and validation did not beat epoch 5.
Epoch 10 scored validation accuracy 0.921, character error rate 0.079, and word accuracy 0.618.
Weights: `/srv/htr/bootstrap/iter1/cyrillic/models/best_0.9254.safetensors`, converted from `checkpoint_05-0.9254.ckpt`.
The run used 3 weeks 1 day 6 hours 54 minutes of CPU time and peaked at 15 GiB.
There was no out-of-memory stop.
This validation score is on the iteration-1 filter of the same three documents.
The iteration-0 best was 0.9097 on its 125-line set.
`ketos test` of `best_0.9254.safetensors` on the unchanged frozen arrow finished at 11:37 AM ET on 1 Nov.
Wall time was 5.29 seconds.
On 300 characters the character accuracy is 94.00% (character error rate 0.060) and the word accuracy is 72.73%.
There was 1 insertion, 10 deletions, and 7 substitutions.
The iteration-0 weights on that same arrow scored 93.67% (character error rate 0.063) and word accuracy 68.18%.
The held-out goal for Russian is 7% or below.
This one-page score meets it.
The separate Kurrent fine-tune started at 12:36 PM ET on 1 Nov from PP-OCRv6 small.
On the 14 lines of RGIA 2213/035, that base scored character accuracy 92.55% (82 errors on 1,101 characters, word
accuracy 73.33%).
The public Kurrent model `german_handwriting_kurrent_v3` scored 76.29% (261 errors, word accuracy 38.33%) on those same
lines.
The Zenodo set has 9,316 line images on disk, one short of the record's 9,317.
The published validation split, 780 lines, is the early-stop set.
The published test split is in the training set.
Ketos dropped 308 training lines and 19 validation lines whose polygon or baseline fell outside the image.
The compiled arrows have 8,229 training lines, including all 14 lines of RGIA 2213/035, and 761 validation lines.
RGIA 2213/036 stays the frozen test and is in neither arrow.
The Kurrent fine-tune stopped at 6:25 AM ET on 4 Nov, exit 0, with no out-of-memory stop.
Early stopping fired at the end of epoch 12.
The best checkpoint is epoch 7 on the 761-line public validation set: validation accuracy 0.921, character error rate
0.079, word accuracy 0.657.
Weights: `/srv/htr/bootstrap/kurrent/models/best_0.9208.safetensors`, 13,065,380 bytes and 396 tensors, converted from
`checkpoint_07-0.9208.ckpt`.
Epoch 12 scored validation accuracy 0.919, character error rate 0.081, and word accuracy 0.655, and did not beat epoch 7.
The last measured peak was about 19.6 GiB.
Wall time from the 12:36 PM start on 1 Nov was 2 days 17 hours 49 minutes.
`ketos test` of `best_0.9208.safetensors` on the unchanged frozen arrow finished at 11:23 AM ET on 4 Nov.
Wall time was 1:51.57.
The arrow was still the 22 Oct file, 178,674,866 bytes.
On 41,986 characters the character accuracy is 88.02% (character error rate 0.120) and the word accuracy is 63.77%.
There were 332 insertions, 3,079 deletions, and 1,617 substitutions.
The iteration-0 Latin weights on that same arrow scored 70.46% (character error rate 0.295) and word accuracy 28.95%.
The iteration-1 Latin weights scored 62.81% (character error rate 0.372) and word accuracy 18.23%.
The held-out goal for German is 7% or below.
This score does not meet it.
Hebrew has not started.
Segmentation training has not started.
Pages without TEI have not been recognized.
Unclear lines at `4dcc22dff` were written beside the transcription on 5 Nov 2026, not into the TEI.
Of the 1,062 lines flagged `unclear`, the commit has 322 with one Cyrillic line image and 15 with one Latin line image.
The working tree of RGIA 1347/64-1/328/005 adds one Cyrillic line, so that file has 323 records.
The 1:1 hypothesis is the iteration-2 reading of that polygon with the adopted medium weights, `best_0.9351.safetensors`
and `best_0.8902.safetensors`.
The 005 page was not in that page list.
It was segmented with the default baseline segmenter and recognized with `best_0.9351.safetensors` on 5 Nov 2026.
The page has 25 hypothesis lines.
Mean glyph confidence is the mean of the glyph scores on the polygons in the record.
Local files, with the same bytes on the guest under `/srv/htr/unclears/`, are
`htr-work/unclears/cyrillic/best_0.9351.jsonl` (md5 `f24485f0277273057255c194655cdf1c`) and
`htr-work/unclears/latin/best_0.8902.jsonl` (md5 `32ddf9c2c4f757026c44de62e5170180`).
Of the 285 lines that sit under two polygons, 259 have both polygons at least 40px wide.
Those two readings are joined in reading order in `htr-work/unclears/cyrillic/best_0.9351-2to1.jsonl` (239 lines, md5
`04a2026f1c38a26c3a0a99dd99e7c7da`) and `htr-work/unclears/latin/best_0.8902-2to1.jsonl` (20 lines, md5
`920bd9839cc3c79f82438e25eda655d9`).
The other 26 include a polygon under 40px and stay out.
614 lines have no single aligned line image: 285 are one TEI line under two polygons, 270 share one polygon with a
second TEI line, and 59 have no polygon.
Plan §4.8 and §4.9 now send the 270 shared polygons and the 59 missing lines to the segmentation review.
That review has not been started, and a shared hypothesis was not written onto either line.
RGIA 2213/036, sheets 36–46, has 110 of the unclear lines and stays out until the Transkribus export is scored.
The uncommitted edit of `archive/rgia/fund/1347/inventory/64-1/case/328/005.xml` moves `<pb n="005-2"/>` to immediately
after `<pb n="005-1" missing="true"/>`.
The report, page number 89, now sits on `005-2`, and `005-2.jpg` is the scan.
`005-1` stays an empty missing page.
The commit still has the old page break, so a commit-only extract still lists the registrar line as unaligned on `005-1`.
On the working tree that line is page `005-2`, line 21, XPath `/TEI/text[1]/body[1]/closer[1]/l[3]`, diplomatic
`Коллежскій регистраторъ ???`.
It aligns to one polygon, hypothesis index 23, 827px wide.
The hypothesis is `Полиежаий Регисараистъ риенаивъ`, mean glyph confidence 0.708725.
The row is in the Cyrillic JSONL.
It was not written into the TEI.
Six of the Latin hypotheses are empty.
Four of those six are paired with a sliver of blank margin.
The Hebrew signature beside the line sits in another polygon, which the Latin model also left blank.
The other two empty readings are Hebrew ink.
Those six signatures, and the registrar `???` on 005, were read on 5 Nov 2026 with the public PP-OCRv6 medium weights
`medium.safetensors` (md5 `e3411a453ce3b9e9efae3f8631d85762`), base direction R, on LXC 107.
The file is `htr-work/unclears/hebrew/medium.jsonl` (md5 `2f4592898b5861fcd67899168fec7c84`), and the same bytes are at
`/srv/htr/unclears/hebrew/medium.jsonl`.
LVIA 178/072-2, diplomatic `??? ???`, hypothesis `בהה מא`, mean glyph confidence 0.522883.
LVIA 178/261-2, diplomatic `???`, hypothesis `ר' ש זאב`, 0.77055.
The other four use the signature polygon beside the speck: LVIA 178/288-1 `שבי` (0.7413), RGADA 3140/116
`פאום פ בזהורי חיים זל` (0.76529), RGADA 3140/126 `האת אנהר ישה אנשח` (0.501159), and RGADA 3140/130 `נט רות במהט ארחשך`
(0.659959).
On 005 the medium model returned `nla ant` at 0.512171, Latin letters, from a baseline drawn on the `???`.
The adopted Cyrillic weights on that same baseline returned `иоивъ` at 0.38542.
Nothing from this sample was written into the TEI.
These are the seven lines.
The same weights then read two pages on 5 Nov 2026, unit `htr-hebrew-pages`, exit 0: RGADA 3140/150-1 and RGADA
3140/059-1.
Segmentation used the default baseline model with text direction `horizontal-rl`, and recognition used base direction R.
RGADA 3140/150-1 has 35 diplomatic lines in the working tree, 33 Hebrew and two French.
The segmenter produced 51 lines, and the alignment is 21 two-polygon Hebrew lines, 6 shared polygons, and one single
polygon.
Joined Hebrew readings take 2,021 edits on 1,935 characters, character error rate 1.044, mean glyph confidence 0.745.
The file is `htr-work/unclears/hebrew/medium-150.jsonl` (md5 `a16abf7369a00fe4321651908e0c207c`).
The French clerk note is readable: 13 edits on 71 characters, character error rate 0.183.
`Copie du lettre de Salmann Boruch de Lozno.` came back as `Copie du Lettre de Salmann Borach de Loszno`.
RGADA 3140/059-1 has no line transcription.
The paragraph in `059.xml` sits on page 061-1.
The segmenter produced 61 lines, and 43 of width at least 200px are in `htr-work/unclears/hebrew/medium-059-1.jsonl`
(md5 `3fea57221ebd89c84d3075af63625e6a`), mean glyph confidence 0.864.
One of those lines reads `מה שנקרא בליין רוסי' דאדינתינו קאהַא מַאינא ענין זה.` Both files are also at
`/srv/htr/unclears/hebrew/`.
Nothing was written into the TEI.
The 235-page Hebrew pass has not been started.

The other seven sides of RGADA 3140 sheets 59 through 62 were read on 5 Nov 2026 with the same weights and the same
commands.
`060-1`, `061-1`, and `062-1` are upright in the site JPEG.
`059-2`, `060-2`, and `061-2` are upside down there, so those three were recognized again from a 180-degree copy under
`/srv/htr/unclears/hebrew/pages/rotated/`.
The site files were not changed.
`062-2` is a blank verso.
Unit `htr-hebrew-059-rest` read the seven site files and exited 0.
Unit `htr-hebrew-059-verso` replaced the three upside-down readings and exited 0.
A line is kept when its baseline is at least 200px wide, the same filter as `059-1`.
`059-2` keeps 43 lines, mean glyph confidence 0.879846, file `medium-059-2.jsonl`, md5
`3b62891bbcecb240cebe814d517aa7ba`.
One of those lines reads `הכל ישראל איאי עולם להיות בכל עיר ועיר ב' בתים גדולים לתפלה`.
`060-1` keeps 44 lines, confidence 0.875187, md5 `5d0c500a47cf5a7452efadcd3d89003c`.
Segmentation of that side warned that the polygonizer failed on a line, and the kept lines are the 44 above.
`060-2` keeps 41 lines, confidence 0.869961, md5 `2f26eb221a7752f3e66c16390c991ec3`.
`061-1` keeps 47 lines, confidence 0.865069, md5 `a0ea7a28e121976a0a066253d55d08a7`.
`061-2` keeps 51 lines, confidence 0.866731, md5 `8b2589b865d8259b76cdce17ef39bf7f`.
`062-1` keeps 36 lines, confidence 0.853148, md5 `3de92b843a807aaa32c49b88f74eaec1`.
`062-2` has one empty segmented line and no kept line.
`medium-062-2.jsonl` is empty, md5 `d41d8cd98f00b204e9800998ecf8427e`.
The same bytes are at `/srv/htr/unclears/hebrew/`.
`059.xml` has one paragraph of 316 characters between the `061-1` and `061-2` page breaks.
That paragraph is not divided into lines, so `061-1` has no line score.
The kept reading of the side is longer than the paragraph.
Nothing from these sides was written into the TEI.

The comparison script for the Transkribus export is `htr-work/transkribus-rgia-2213-036/score.py`.
That directory still has only the 22 page images.
With no Page XML the script exits 2 and writes nothing.
When all 22 pages are present it writes `score.json` and `lines.jsonl` in that directory.
It reads `archive/rgia/fund/1374/inventory/3/case/2213/036.xml` at `4dcc22dff` without checking the commit out.
A skipped TEI line counts as a deletion, and a skipped hypothesis line counts as an insertion.
The NFD character error rate is the figure compared with 0.120.
That house figure is `ketos test` of `best_0.9208.safetensors` on the frozen arrow's own line cuts, 41,986 characters.
It is not this alignment.
No score has been recorded.

The unclear lines that share a polygon, and the unclear lines with no polygon, are in
`htr-work/unclears/segmentation-review/review.jsonl`.
There are 228 shared polygons, covering 270 unclear lines, and 59 lines with no polygon, on 224 pages.
Forty-two of the polygons contain two unclear lines.
Each shared polygon has a line image, and each page has a preview.
The 110 unclear lines of RGIA 2213/036 are not in the list.
The 2:1 joins already written are not in the list.
No polygon was split and no baseline was drawn.

The hypothesis was not imported into eScriptorium.
No commercial engine has been called.

## Still open

The Latin model may still be split into German Kurrent versus French and Polish.
The plan allows that if one model plateaus, and the frozen German score is the evidence.
The public Kurrent set (Zenodo 10.5281/zenodo.17252677) is on disk and the checksum matches.
The catalog license is CC BY 4.0.
The record's own description marks the Bayerische Schriftkunde transcriptions as CC BY-NC-SA 4.0 and the Deutsches
Textarchiv text as CC BY 4.0.
The small-versus-medium comparison finished on 4 Nov, and the home test adopted medium for both scripts.
Cyrillic `best_0.9351.safetensors` scored character error rate 0.065 on the iteration-0 validation arrow (4,066
characters).
Latin `best_0.8902.safetensors` scored character error rate 0.118 on the iteration-1 validation arrow (2,614
characters).
Plan §12, added 22 Oct 2026, still includes Transkribus on the residue.
Transkribus has not been started.
The scorer for that export is in place, and the export is not on disk.
Iteration-2 recognition finished at 12:56 AM ET on 5 Nov, unit `htr-bootstrap-iter2`, exit 0.
Cyrillic closed all 1,687 pages at 11:55 PM on 4 Nov.
Latin closed all 265 pages at 12:56 AM.
No page was skipped.
Both used the adopted medium weights and the iteration-0 line geometry.
Alignment at the 0.15 ceiling kept the seed-0 roles in `htr-work/iter0/sets/roles.json`.
Cyrillic aligned 86.7% of diplomatic lines and kept 30,255 of 44,393.
On the same three validation documents the mean line character error rate is 0.109, against 0.126 at iteration 1.
The training arrow takes 29,836 lines from 1,411 pages.
Latin aligned 88.7% and kept 5,394 of 8,020.
On its three validation documents the mean line character error rate is 0.178, against 0.149 at iteration 1.
LVIA 178/287 is 0.116, RGADA 3140/144 is 0.217, and RGIA 2213/035 is 0.238.
The training arrow takes 5,256 lines from 222 pages.
LVIA 178/233 and RGIA 2213/036 stay out of the training arrows.
The four arrows were compiled by 1:21 AM ET on 5 Nov, exit 0.
Cyrillic training is 5,017,468,626 bytes and validation is 20,278,978 bytes.
Latin training is 939,180,482 bytes and validation is 8,875,466 bytes.
The first fine-tune attempt did not run.
Pod `8ic91u4s9w1evx` was created at 1:25 AM ET on 5 Nov and destroyed at 7:25 AM ET, DELETE 204.
The arrow upload never started, because the Proxmox connection was down, so the machine sat idle.
No new weights were produced.
RunPod recorded $5.41 for that idle rental.
A second Secure Cloud RTX 4090, pod `fhgto01jp2x10h`, was created at 10:40 AM ET after another $10 deposit.
The arrow upload and the Kraken install started at 10:43 AM, with the Proxmox connection up.
The copy checked out at 11:05 AM.
Cyrillic fine-tuning from `best_0.9351.safetensors` started then, on the 4090, 34.7 M parameters.
It finished at 12:07 PM ET.
The best checkpoint is epoch 1, score 0.9555, and early stopping ended the run at epoch 6.
Latin fine-tuning from `best_0.8902.safetensors` finished at 12:16 PM ET, exit 0.
The best checkpoint is epoch 0, score 0.9122, and early stopping ended the run at epoch 5.
Those scores are the in-training validation accuracy on the new iteration-2 arrows.
They do not adopt the weights.
Both best files and the train log were copied to the desktop and the checksums matched.
The pod was destroyed at about 12:22 PM ET, DELETE 204, after about 1 hour 41 minutes.
The home `ketos test` finished at 12:32 PM ET on 5 Nov, unit `htr-test-iter2`, exit 0.
It scored both weight files on the same iteration-2 validation arrows, which were left unchanged.
Cyrillic medium `best_0.9351.safetensors` scored character accuracy 95.88% (character error rate 0.041, 149 errors in
3,620 characters, word accuracy 77.00%).
Cyrillic iteration-2 `best_0.9555.safetensors` scored 95.64% (character error rate 0.044, 158 errors, word accuracy
75.48%).
Latin medium `best_0.8902.safetensors` scored character accuracy 92.17% (character error rate 0.078, 141 errors in 1,800
characters, word accuracy 61.92%).
Latin iteration-2 `best_0.9122.safetensors` scored 90.56% (character error rate 0.094, 170 errors, word accuracy
59.93%).
Neither new file beat the medium weights, so medium stays the adopted model for both scripts.
Hebrew, segmentation training, and the production pass over untranscribed pages are later steps.
Cyrillic iteration 1 fine-tuning finished on 1 Nov at 3:33 AM ET.
The Kurrent fine-tune finished on 4 Nov at 6:25 AM ET.
The frozen test of `best_0.9208.safetensors` on RGIA 2213/036 scored character error rate 0.120 and does not meet the 7%
goal.
