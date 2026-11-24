# Photo Studio Feature Matrix

| Feature ID | Visible Name | UI Route | State Model | Render Implementation | Preview Implementation | Export Implementation | Undo/Redo | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **FEAT_TUNE** | Tune Image | Photo Studio -> Tools -> Tune | `ImageAdjustmentState` | `ColorMatrixProcessor` | 60fps Compose `ColorFilter` | `BitmapProcessor` full-res | Yes | VERIFIED |
| **FEAT_CURVES** | Curves | Photo Studio -> Tools -> Curves | `CurvesState` (RGB, R, G, B) | `CurvesProcessor` Monotonic Cubic Spline | 256-entry LUT | `BitmapProcessor` full-res | Yes | VERIFIED |
| **FEAT_HSL** | HSL Mix | Photo Studio -> Tools -> HSL | `HslState` (8 color bands) | `HslProcessor` | HSV circular blend | `BitmapProcessor` full-res | Yes | VERIFIED |
| **FEAT_WB** | White Balance | Photo Studio -> Tools -> WB | `ImageAdjustmentState.warmth/tint` | `ColorMatrixProcessor` | Temperature/Tint matrix | `BitmapProcessor` full-res | Yes | VERIFIED |
| **FEAT_DETAILS** | Details | Photo Studio -> Tools -> Details | `DetailsState` | `DetailsProcessor` / Convolution | Fast unsharp preview | Unsharp mask + structure | Yes | VERIFIED |
| **FEAT_HIST** | Histogram | Photo Studio -> Tools -> Histogram | `HistogramData` | `HistogramProcessor` | Real-time 256-bin sub-sampled | N/A (Analytical) | N/A | VERIFIED |
| **FEAT_LOOKS** | Looks (34 Presets) | Photo Studio -> Looks | `FilterPreset` + `filterIntensity` | `ColorMatrixProcessor` | Dynamic live thumbnail carousel | Exact lerped preset matrix | Yes | VERIFIED |
| **FEAT_CROP** | Crop & Rotate | Photo Studio -> Tools -> Crop | `CropTransformState` | Interactive Crop Canvas + `BitmapProcessor` | Real boundary-constrained window | Exact pixel crop without stretch | Yes | VERIFIED |
| **FEAT_COMPARE** | Compare Original | Header action | `isComparingOriginal` | Press-and-hold original bitmap | Instantaneous bypass of color filter | N/A | N/A | VERIFIED |
| **FEAT_EXPORT** | Export Studio | Bottom right tab only | `ExportConfig` | `StorageManager` + `BitmapProcessor` | Exact resolution & size estimate | MediaStore `Pictures/Creators Club` | N/A | VERIFIED |
| **FEAT_PROJECT** | Durable Projects | Auto-create on import | `ProjectEntity` (Room) | SQLite persistence + recipe JSON | Restores exact edits & crop | Linked export records | Yes | VERIFIED |
| **FEAT_REMBG** | Remove BG | Quick Creative Tools -> Remove BG | `RemoveBgState` | On-device segmentation + manual brush | Checkerboard transparent canvas | Lossless PNG with alpha | Yes | VERIFIED |
