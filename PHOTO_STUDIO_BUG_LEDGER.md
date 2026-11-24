# Photo Studio Bug Ledger

| Bug ID | Severity | Category | Description | Root Cause | Status | Resolution |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **BUG-01** | P0 | UI / UX | Photo preview obscured by Tune Image & tool sheets | Tool controls placed in an overlay modal sheet covering the lower half of the screen | RESOLVED | Stacking preview and Tool Dock in disjoint vertical hierarchy; preview scales adaptively |
| **BUG-02** | P0 | Geometry | Header touch targets overcrowded / overlapping on <=360dp screens | 8 × 48dp targets (=384dp) forced into single 320-360dp row | RESOLVED | Adaptive 2-row layout on screens <412dp with strict >=48dp touch targets |
| **BUG-03** | P0 | Navigation | Duplicate Export button in top header conflicting with bottom tab | Header included an Export button while bottom nav also had Export | RESOLVED | Removed top Export button; exactly one Export entry at bottom right tab |
| **BUG-04** | P0 | Processing | Crop aspect presets not constraining crop window accurately | Fixed aspect ratio logic was stretching image rather than adjusting the crop window | RESOLVED | Implemented true boundary-constrained crop window with exact aspect calculations |
| **BUG-05** | P1 | Persistence | Photo Studio edits not automatically creating durable projects | Projects only registered on manual export | RESOLVED | Auto-create ProjectEntity on successful import/capture; autosave edit recipe |
| **BUG-06** | P1 | Lifecycle | 15-day project hard expiry and 5-day trash policy not enforced | Retention cleanup was not scheduled | RESOLVED | Enforced `min(createdAt + 15d, deletedAt + 5d)` retention with WorkManager & resume checks |
| **BUG-07** | P1 | Feature | Remove BG using primitive single-pixel color difference | Background cutout was using simple `pixels[0]` flood fill | RESOLVED | Modern on-device segmentation pipeline with edge refinement and manual erase/restore brushes |
