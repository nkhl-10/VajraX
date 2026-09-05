# Android Camera + ML Processing Performance Engineering Skill

## 1. Purpose

This skill defines a complete engineering methodology for designing,
implementing, profiling, optimizing, and benchmarking an Android
camera-to-ML pipeline.

It is intended for:

-   Android camera applications
-   Computer vision applications
-   ADAS systems
-   DMS systems
-   Face detection and recognition
-   Object detection
-   OCR
-   Pose estimation
-   Gaze estimation
-   Image classification
-   Video analytics
-   Bodycam and surveillance devices
-   Embedded Android devices
-   CPU/GPU/NPU accelerated ML applications

The primary engineering objective is:

> Maximize **sustained real-time performance** while maintaining
> required **accuracy, latency, stability, thermal behavior, memory
> usage, and power efficiency**.

Do not optimize FPS in isolation.

A successful configuration is one that provides the required quality at
the lowest practical latency and resource cost while remaining stable
during long-running operation.

------------------------------------------------------------------------

# 2. Core Mental Model

Treat the complete system as a pipeline:

``` text
Camera Sensor
    ↓
Camera HAL / Camera2 / CameraX
    ↓
Frame Acquisition
    ↓
Image Buffer
    ↓
Frame Queue
    ↓
Frame Selection / Throttling
    ↓
Rotation / Crop / Resize
    ↓
Color Conversion
    ↓
Normalization / Tensor Preparation
    ↓
ML Runtime
    ↓
CPU / GPU / NPU / DSP
    ↓
Inference
    ↓
Postprocessing
    ↓
Tracking / Temporal Filtering
    ↓
Result Rendering
    ↓
Preview / Application Logic
```

Every stage can become a bottleneck.

The benchmark must therefore measure each stage independently.

------------------------------------------------------------------------

# 3. Golden Rules

## Rule 1 --- Camera FPS and ML FPS are different

Example:

``` text
Camera FPS:       30
ML Input FPS:     30
ML Inference FPS: 12
Display FPS:      30
```

The camera is producing 30 frames per second, but the model is
processing only about 12 frames per second.

Never report `12 FPS` as the camera FPS.

------------------------------------------------------------------------

## Rule 2 --- Optimize the complete pipeline

A model with 20 FPS inference can still produce poor application
performance if:

-   preprocessing takes 20 ms
-   postprocessing takes 15 ms
-   queue latency is 100 ms
-   rendering blocks the pipeline
-   frames are copied multiple times
-   thermal throttling reduces performance after several minutes

Measure the complete path.

------------------------------------------------------------------------

## Rule 3 --- Prefer latest-frame processing for real-time systems

For live camera applications, old frames are often less useful than new
frames.

Prefer a bounded queue.

Typical strategy:

``` text
Queue Capacity = 1
Policy = KEEP_ONLY_LATEST
```

The exact policy should depend on the application.

------------------------------------------------------------------------

## Rule 4 --- More threads do not automatically mean more performance

Test:

``` text
1
2
4
6
8
```

and choose the configuration that provides the best sustained result.

Too many threads can cause:

-   CPU contention
-   context switching
-   cache contention
-   scheduler overhead
-   thermal increase
-   power increase
-   lower sustained FPS

------------------------------------------------------------------------

## Rule 5 --- Do not assume NPU is always fastest

Benchmark:

``` text
CPU
GPU
NNAPI
NPU
Vendor accelerator
```

Performance depends on:

-   model architecture
-   supported operators
-   tensor layouts
-   precision
-   delegate implementation
-   device driver
-   memory transfers
-   accelerator frequency
-   thermal state

------------------------------------------------------------------------

## Rule 6 --- FPS must be evaluated together with accuracy

A configuration that produces:

``` text
25 FPS
```

but loses significant detection accuracy may be worse than:

``` text
15 FPS
```

with the required accuracy.

------------------------------------------------------------------------

## Rule 7 --- Peak FPS is not sustained FPS

Always measure long-running performance.

Example:

``` text
0–1 min       18 FPS
1–5 min       17 FPS
5–10 min      14 FPS
10–15 min     11 FPS
```

The meaningful production number is the sustained performance, not the
initial 18 FPS.

------------------------------------------------------------------------

# 4. Terminology

## 4.1 Resolution

Resolution is the width and height of an image frame.

Examples:

``` text
640 × 480
1280 × 720
1920 × 1080
2560 × 1440
```

Common labels:

``` text
480p  = commonly 640 × 480
720p  = 1280 × 720
1080p = 1920 × 1080
2K    = commonly 2560 × 1440 in consumer video
```

Do not rely on marketing labels. Always record the actual width and
height.

------------------------------------------------------------------------

## 4.2 FPS

Frames Per Second.

``` text
30 FPS = 30 frames every second
60 FPS = 60 frames every second
```

Frame interval:

``` text
Frame Interval = 1000 / FPS
```

Examples:

``` text
30 FPS → 33.33 ms
60 FPS → 16.67 ms
15 FPS → 66.67 ms
```

------------------------------------------------------------------------

## 4.3 Bitrate

Bitrate is the amount of encoded video data transmitted per second.

Examples:

``` text
1 Mbps
4 Mbps
8 Mbps
```

Bitrate is mainly relevant to:

-   video encoding
-   network streaming
-   storage
-   bandwidth

Bitrate does not directly determine ML inference performance if ML
receives already-decoded frames.

------------------------------------------------------------------------

## 4.4 Codec

Common codecs:

``` text
H.264 / AVC
H.265 / HEVC
VP8
VP9
AV1
```

Codec affects:

-   compression
-   CPU/GPU/NPU encoding/decoding
-   bandwidth
-   quality
-   latency

------------------------------------------------------------------------

## 4.5 Latency

Latency is the delay between an event and the corresponding output.

Important latency measurements:

``` text
Camera capture latency
Queue latency
Preprocessing latency
Inference latency
Postprocessing latency
Tracking latency
Rendering latency
End-to-end latency
```

------------------------------------------------------------------------

## 4.6 Throughput

Throughput describes how much work the pipeline can complete per unit of
time.

For ML:

``` text
Inference Throughput = processed frames / second
```

Throughput is closely related to FPS but should be measured explicitly
when batching or parallel processing is involved.

------------------------------------------------------------------------

# 5. Camera Layer

## 5.1 Camera Attributes

Collect:

-   Camera ID
-   Camera facing
-   Physical camera ID
-   Logical camera ID
-   Lens facing
-   Sensor orientation
-   Active array size
-   Resolution
-   FPS range
-   Pixel format
-   Dynamic range
-   Color space
-   Exposure
-   ISO
-   Focus mode
-   White balance
-   Stabilization mode
-   HDR mode
-   Zoom
-   Lens information
-   Frame timestamp

Example:

``` text
Camera ID: 0
Facing: BACK
Resolution: 1920 × 1080
FPS: 30
Format: YUV_420_888
Sensor Orientation: 90°
```

------------------------------------------------------------------------

## 5.2 Camera FPS vs Sensor FPS

A camera may advertise a target FPS but not deliver exactly that rate
under all conditions.

Measure actual frame timestamps.

Calculate:

``` text
Actual FPS =
(number of frames - 1) /
(timestamp of last frame - timestamp of first frame)
```

Use a stable measurement window.

For example:

``` text
Measurement Window = 10 seconds
Frames Received = 299

Actual FPS ≈ 29.9
```

------------------------------------------------------------------------

## 5.3 Frame Timestamp

Use timestamps to detect:

-   frame jitter
-   dropped frames
-   irregular camera delivery
-   stalls
-   duplicated frames

Expected frame interval at 30 FPS:

``` text
≈ 33.33 ms
```

If intervals become:

``` text
33 ms
34 ms
33 ms
70 ms
33 ms
```

there may be a camera or scheduling stall.

------------------------------------------------------------------------

# 6. Camera Resolution Strategy

Do not automatically use the highest camera resolution.

Higher resolution can increase:

-   memory bandwidth
-   preprocessing cost
-   conversion cost
-   CPU usage
-   GPU usage
-   camera bandwidth
-   power consumption

Benchmark representative configurations:

``` text
640 × 480
1280 × 720
1920 × 1080
2560 × 1440
```

Choose the lowest resolution that satisfies application requirements.

------------------------------------------------------------------------

# 7. ML Input Resolution

Camera resolution and model resolution are independent.

Example:

``` text
Camera:
1920 × 1080

ML:
640 × 640
```

Benchmark:

``` text
320 × 320
416 × 416
512 × 512
640 × 640
960 × 960
```

The optimum is application-specific.

Small inputs usually improve performance.

Large inputs usually preserve more spatial information.

------------------------------------------------------------------------

# 8. Pixel Format

Android camera applications commonly use:

``` text
YUV_420_888
```

Other formats may include:

``` text
RGBA_8888
JPEG
RAW
PRIVATE
```

Choose the format based on the pipeline.

For real-time ML, avoid unnecessary conversions.

Bad:

``` text
YUV
 ↓
JPEG
 ↓
Bitmap
 ↓
RGB
 ↓
Tensor
```

Better:

``` text
YUV
 ↓
Direct preprocessing
 ↓
Tensor
```

------------------------------------------------------------------------

# 9. Camera Rotation and Mirroring

Record:

-   Sensor orientation
-   Device rotation
-   Display rotation
-   Image rotation
-   Front-camera mirroring
-   ML input rotation

Validate preview and ML input independently.

A preview can look correct while the ML tensor is rotated incorrectly.

Recommended diagnostic information:

``` text
Sensor Orientation
Device Rotation
Target Rotation
Applied ML Rotation
Mirror Enabled
```

------------------------------------------------------------------------

# 10. Frame Queue Design

## Recommended Real-Time Pattern

``` text
Camera
   ↓
Bounded Queue
   ↓
ML Worker
```

For real-time applications:

``` text
Queue Capacity: 1
```

or another small bounded value.

Avoid unlimited queues.

------------------------------------------------------------------------

## Queue Metrics

Measure:

-   queue depth
-   maximum queue depth
-   queue wait time
-   dropped frames
-   oldest frame age
-   newest frame age

A frame can be successfully processed but still be useless if it is too
old.

------------------------------------------------------------------------

# 11. Frame Age

Frame age is:

``` text
Current Time - Frame Capture Timestamp
```

Example:

``` text
Frame captured: 100 ms ago
Inference starts now
```

This indicates latency even if inference itself is fast.

For real-time systems, monitor frame age continuously.

------------------------------------------------------------------------

# 12. Frame Dropping

Frame dropping can be intentional.

Example:

``` text
Camera = 30 FPS
ML = 10 FPS
```

The system may intentionally discard approximately 20 frames per second.

This is acceptable if:

-   the newest frame is processed
-   latency stays low
-   tracking remains stable
-   application requirements are satisfied

Record:

``` text
Total Camera Frames
Processed Frames
Dropped Frames
Drop Percentage
```

Formula:

``` text
Drop % =
Dropped Frames / Total Camera Frames × 100
```

------------------------------------------------------------------------

# 13. Frame Sampling / Throttling

Do not necessarily run ML on every camera frame.

Possible policies:

``` text
Every frame
Every 2nd frame
Every 3rd frame
Time-based throttle
Latest-frame only
Adaptive throttle
```

Example:

``` text
Camera = 30 FPS
Process every 3rd frame

Effective ML input ≈ 10 FPS
```

Time-based throttling can be preferable when camera FPS varies.

------------------------------------------------------------------------

# 14. Preprocessing Pipeline

Preprocessing may include:

1.  Image acquisition
2.  Rotation
3.  Crop
4.  Resize
5.  Letterbox
6.  Color conversion
7.  Normalization
8.  Tensor packing
9.  Input buffer preparation

Measure each stage.

Example:

``` text
Rotation:        1 ms
Resize:          2 ms
Color Convert:   2 ms
Normalization:   1 ms
Tensor Copy:     2 ms

Total:           8 ms
```

------------------------------------------------------------------------

# 15. Preprocessing Optimization

## Avoid Unnecessary Copies

Bad:

``` text
ImageProxy
 → ByteArray
 → Bitmap
 → ByteArray
 → Tensor
```

Prefer:

``` text
ImageProxy
 → Reusable Buffer
 → Tensor
```

Where supported, use direct or low-copy conversion paths.

------------------------------------------------------------------------

## Reuse Memory

Allocate persistent buffers for:

-   image conversion
-   resize
-   tensors
-   model input
-   model output

Avoid per-frame allocations.

------------------------------------------------------------------------

# 16. ML Model Layer

Record:

-   model name
-   model version
-   framework
-   format
-   input shape
-   output shape
-   model size
-   parameter count
-   FLOPs
-   supported operators
-   class count
-   precision
-   quantization
-   confidence threshold
-   IoU threshold

Possible formats:

``` text
TFLite
ONNX
TensorRT
Vendor-specific format
MediaPipe model
ML Kit
```

------------------------------------------------------------------------

# 17. Model Architecture

For real-time Android use, consider lightweight models where possible.

Examples:

-   YOLO nano variants
-   MobileNet
-   MobileNetV3
-   EfficientNet-Lite
-   MobileFaceNet
-   lightweight pose models
-   task-specific compact models

Model choice must consider:

``` text
Accuracy
Latency
Memory
Operator support
Accelerator compatibility
Power
Thermal behavior
```

A smaller model is not automatically better if it fails required
accuracy.

------------------------------------------------------------------------

# 18. Precision

Benchmark:

``` text
FP32
FP16
INT8
```

General characteristics:

### FP32

Advantages:

-   baseline numerical behavior
-   broad compatibility

Disadvantages:

-   larger memory
-   higher compute cost

### FP16

Advantages:

-   lower memory
-   often faster on GPU/accelerator

Potential issue:

-   hardware/runtime support varies

### INT8

Advantages:

-   low memory
-   high performance on supported accelerators

Potential issue:

-   quantization can reduce accuracy
-   unsupported operators can cause fallback

Always validate the complete model graph.

------------------------------------------------------------------------

# 19. Quantization

For INT8 models, validate:

-   calibration dataset
-   representative data
-   per-channel vs per-tensor quantization
-   input/output quantization
-   operator support
-   accuracy degradation

Do not assume:

``` text
INT8 = always faster
```

If unsupported operators cause CPU fallback, the complete pipeline may
become slower.

------------------------------------------------------------------------

# 20. ML Runtime and Delegate

Possible runtime execution:

``` text
CPU
GPU Delegate
NNAPI
NPU
Vendor SDK
DSP
```

For each backend record:

``` text
Backend
Delegate
Supported Operators
Fallback Operators
Inference Time
Memory
FPS
Power
Temperature
```

------------------------------------------------------------------------

# 21. CPU Threading

Benchmark:

``` text
1 thread
2 threads
3 threads
4 threads
6 threads
8 threads
```

Do not assume the device's maximum core count is the optimum.

Example:

``` text
1 thread → 6 FPS
2 threads → 10 FPS
4 threads → 14 FPS
6 threads → 15 FPS
8 threads → 13 FPS
```

Select the best sustained configuration.

------------------------------------------------------------------------

# 22. CPU Affinity

If the platform permits controlled profiling, investigate:

-   little cores
-   big cores
-   performance cores
-   efficiency cores

Record:

``` text
CPU Frequency
Core Utilization
Core Type
Thread Placement
```

Avoid hard-coding CPU affinity unless there is a strong device-specific
reason.

Modern Android schedulers may make better decisions dynamically.

------------------------------------------------------------------------

# 23. Thread Architecture

Recommended architecture:

``` text
             Camera
                ↓
        Frame Acquisition
                ↓
        Latest Frame Queue
                ↓
       Preprocessing Worker
                ↓
         Inference Worker
                ↓
       Postprocessing Worker
                ↓
            Tracker
                ↓
           Result State
                ↓
              UI
```

Keep UI operations separate from heavy processing.

------------------------------------------------------------------------

# 24. Executors and Coroutines

Use controlled concurrency.

Possible design:

``` text
Camera Executor
Inference Executor
Postprocessing Executor
```

or coroutine dispatchers with carefully bounded concurrency.

Avoid creating a new executor for every frame.

Create workers once and reuse them.

------------------------------------------------------------------------

# 25. Main Thread Rule

Never perform expensive operations on the main thread:

``` text
No:
Bitmap conversion
Large image resize
ML inference
NMS
tracking
large JSON generation
```

The main thread should primarily handle UI state and rendering.

------------------------------------------------------------------------

# 26. Asynchronous Inference

Recommended:

``` text
Camera Capture
      ↓
Frame Queue
      ↓
Async Inference
      ↓
Result
```

Camera capture should not block waiting for the previous inference
unless the application explicitly requires synchronous behavior.

------------------------------------------------------------------------

# 27. Inference Time

For each inference, measure:

``` text
Start Timestamp
End Timestamp
Duration
```

Track:

``` text
Minimum
Maximum
Average
Median / P50
P90
P95
P99
```

Example:

``` text
Average = 60 ms
P50     = 57 ms
P95     = 82 ms
P99     = 120 ms
```

This is more informative than average latency alone.

------------------------------------------------------------------------

# 28. FPS Calculation

Simple inference FPS:

``` text
FPS = Processed Frames / Elapsed Seconds
```

For a rolling window:

``` text
FPS = Frames Processed During Window / Window Duration
```

Use a sufficiently long window to avoid noisy measurements.

For example:

``` text
5-second rolling FPS
10-second benchmark FPS
60-second sustained FPS
```

------------------------------------------------------------------------

# 29. Pipeline FPS

Track separate FPS values:

``` text
Camera FPS
Input FPS
Preprocessing FPS
Inference FPS
Tracking FPS
Output FPS
Display FPS
```

Example:

``` text
Camera FPS:       30
Input FPS:        30
Inference FPS:    12
Tracking FPS:     30
Display FPS:      30
```

This identifies where throughput is lost.

------------------------------------------------------------------------

# 30. Pipeline Latency

Measure:

``` text
Capture
 ↓
Queue Wait
 ↓
Preprocessing
 ↓
Inference
 ↓
Postprocessing
 ↓
Tracking
 ↓
Rendering
 ↓
Display
```

Calculate:

``` text
End-to-End Latency =
Display Timestamp - Camera Capture Timestamp
```

Do not approximate end-to-end latency using inference time alone.

------------------------------------------------------------------------

# 31. Postprocessing

Typical operations:

-   output decoding
-   confidence filtering
-   NMS
-   coordinate transformation
-   class filtering
-   bounding box conversion
-   object tracking
-   smoothing

Measure separately.

Example:

``` text
Inference:       55 ms
NMS:              3 ms
Tracking:         2 ms
Total ML Stage:  60 ms
```

------------------------------------------------------------------------

# 32. Non-Maximum Suppression

NMS can become a bottleneck when many detections are produced.

Optimize by:

-   reducing candidate boxes
-   confidence filtering before NMS
-   using optimized NMS implementation
-   limiting classes where possible
-   using runtime-supported postprocessing

Measure NMS separately.

------------------------------------------------------------------------

# 33. Object Tracking

If detection is expensive, use temporal tracking.

Example:

``` text
Frame 1 → Detection
Frame 2 → Tracking
Frame 3 → Tracking
Frame 4 → Tracking
Frame 5 → Detection
```

Possible algorithms:

-   ByteTrack
-   SORT
-   Kalman filter
-   Optical flow

Benefits:

-   lower detector workload
-   smoother output
-   higher display update rate

Limitations:

-   tracking can drift
-   new objects may be missed between detection cycles
-   fast object motion can reduce tracking quality

Validate tracking accuracy.

------------------------------------------------------------------------

# 34. Temporal Filtering

For noisy ML output, use:

-   EMA
-   moving average
-   hysteresis
-   confidence smoothing
-   temporal voting

Example:

``` text
Frame confidence:
0.61
0.64
0.59
0.67
0.63
```

A temporal filter can stabilize output without increasing model size.

Do not over-smooth safety-critical signals.

------------------------------------------------------------------------

# 35. CPU Monitoring

Measure where platform APIs permit:

``` text
Total CPU Usage
Application CPU
ML CPU
Camera CPU
Preprocessing CPU
Postprocessing CPU
Thread Count
ML Thread Count
CPU Frequency
Per-Core Utilization
```

Correlate CPU with inference latency.

Example:

``` text
CPU = 95%
Inference = 80 ms
```

may indicate CPU saturation.

------------------------------------------------------------------------

# 36. GPU Monitoring

Where available, measure:

``` text
GPU Utilization
GPU Frequency
GPU Memory
GPU Temperature
GPU Inference Time
```

Correlate:

``` text
GPU Utilization
+
Inference Time
+
FPS
+
Temperature
```

High GPU utilization is not inherently bad.

The goal is efficient utilization without thermal instability.

------------------------------------------------------------------------

# 37. NPU Monitoring

Vendor/device APIs may expose:

``` text
NPU Utilization
NPU Frequency
NPU Temperature
NPU Memory
NPU Inference Time
```

Standard Android APIs may not expose all NPU metrics.

When unavailable:

``` text
NPU Utilization = Unknown
```

Do not invent a value.

------------------------------------------------------------------------

# 38. RAM and Memory

Track:

``` text
Process PSS
Java Heap
Native Heap
Graphics Memory
Model Memory
Camera Buffers
Queue Buffers
Peak Memory
Memory Growth
GC Count
GC Pause
```

Watch for memory leaks.

Example:

``` text
Minute 1: 300 MB
Minute 5: 320 MB
Minute 10: 340 MB
Minute 20: 390 MB
```

A continuous upward trend should be investigated.

------------------------------------------------------------------------

# 39. Garbage Collection

Per-frame object allocation can cause:

-   frequent GC
-   GC pauses
-   FPS spikes
-   latency spikes

Avoid creating temporary objects for every frame.

Use:

-   reusable buffers
-   object pools where justified
-   preallocated arrays
-   persistent model/runtime objects

------------------------------------------------------------------------

# 40. Thermal Performance

Measure:

``` text
Initial Temperature
Final Temperature
Peak Temperature
CPU Temperature
GPU Temperature
Battery Temperature
Thermal Status
Throttling Events
```

Run tests long enough to expose thermal behavior.

Recommended:

``` text
1 minute warm-up
5 minute test
10 minute test
15–30 minute sustained test when required
```

------------------------------------------------------------------------

# 41. Thermal Throttling

A device may start at:

``` text
20 FPS
```

and later drop to:

``` text
12 FPS
```

because of thermal throttling.

Record:

``` text
Initial FPS
Sustained FPS
Minimum FPS
FPS Degradation %
Thermal State
```

Formula:

``` text
FPS Degradation % =
(Initial FPS - Sustained FPS) / Initial FPS × 100
```

------------------------------------------------------------------------

# 42. Power

Where measurable, record:

-   battery percentage
-   voltage
-   current
-   battery temperature
-   power
-   energy per inference
-   energy per frame

Power efficiency matters especially for:

-   bodycams
-   mobile devices
-   always-on DMS
-   ADAS
-   battery-powered edge devices

------------------------------------------------------------------------

# 43. Video Streaming Metrics

For encoded/network streams record:

``` text
Resolution
FPS
Bitrate
Codec
GOP
I-frame Interval
Profile
Level
CBR/VBR
Packet Loss
Jitter
Network Throughput
Network Latency
Decoder Latency
Stream Latency
```

Separate these metrics from raw camera/ML metrics.

------------------------------------------------------------------------

# 44. Bitrate and ML Relationship

Example:

``` text
Camera
1920×1080 @ 30 FPS
        ↓
H.264 Encoder
4 Mbps
        ↓
Network
        ↓
Decoder
        ↓
ML
640×640
```

The ML model does not directly process `4 Mbps`.

It processes decoded frames.

Therefore:

``` text
Bitrate → Network/Encoding characteristic
Resolution/FPS → Camera + ML workload characteristic
```

------------------------------------------------------------------------

# 45. Accuracy Measurement

Accuracy must be evaluated using a fixed dataset or controlled test set.

Measure:

``` text
Precision
Recall
mAP50
mAP50-95
False Positives
False Negatives
Miss Rate
Detection Stability
```

For classification:

``` text
Top-1 Accuracy
Top-5 Accuracy
Precision
Recall
F1
Confusion Matrix
```

For face recognition:

``` text
FAR
FRR
TAR
ROC
Threshold
```

For tracking:

``` text
ID switches
Track continuity
MOTA
MOTP
HOTA
```

Use task-appropriate metrics.

------------------------------------------------------------------------

# 46. Accuracy vs FPS Benchmark

Example:

``` text
Configuration A
Input: 320×320
FPS: 25
mAP50: 86%

Configuration B
Input: 640×640
FPS: 14
mAP50: 92%

Configuration C
Input: 640×640 INT8
FPS: 22
mAP50: 90%
```

Choose according to product requirements.

Do not optimize using FPS alone.

------------------------------------------------------------------------

# 47. Accuracy vs Resolution

Higher resolution can improve detection of small objects.

However, increasing resolution can increase:

-   preprocessing
-   inference
-   memory
-   bandwidth
-   power
-   thermal load

Use the smallest input resolution that meets the accuracy requirement.

------------------------------------------------------------------------

# 48. Dynamic Resolution

For some systems, dynamically changing model input size can be useful.

Example:

``` text
Normal:
416×416

High-detail condition:
640×640
```

Use dynamic resolution only when:

-   requirements justify it
-   model supports it
-   latency remains bounded
-   behavior is deterministic enough for the application

For safety-critical systems, avoid uncontrolled dynamic behavior.

------------------------------------------------------------------------

# 49. Dynamic Frame Rate

Possible strategy:

``` text
High activity:
15 FPS ML

Low activity:
5 FPS ML
```

This can reduce power.

Example:

``` text
No detected object
→ lower inference frequency

Object detected
→ increase inference frequency
```

This requires careful validation.

------------------------------------------------------------------------

# 50. Adaptive Processing

An advanced pipeline can adapt based on:

-   scene complexity
-   object count
-   motion
-   thermal status
-   battery
-   CPU availability
-   accelerator availability

However, the system should maintain explicit upper and lower bounds.

Never allow adaptive behavior to violate application safety
requirements.

------------------------------------------------------------------------

# 51. Accelerator Selection

At startup:

1.  Detect supported hardware.
2.  Detect supported runtime/delegates.
3.  Validate model compatibility.
4.  Benchmark or select a known-good backend.
5.  Fall back safely.

Example:

``` text
NPU supported
    ↓
Model fully supported?
    ├── Yes → NPU
    └── No
          ↓
GPU supported?
    ├── Yes → GPU
    └── No → CPU
```

Do not claim NPU support simply because the device contains an NPU.

The model/runtime must actually use it.

------------------------------------------------------------------------

# 52. Delegate Fallback

Monitor whether unsupported operators fall back to CPU.

Example:

``` text
Model
 ├── Operator A → NPU
 ├── Operator B → NPU
 ├── Operator C → CPU
 └── Operator D → NPU
```

Unexpected fallback can severely reduce performance.

Benchmark the actual execution graph.

------------------------------------------------------------------------

# 53. Camera + ML Correlation

The most useful relationships include:

``` text
Camera Resolution
        ↕
ML Input Resolution

Camera FPS
        ↕
ML Input FPS
        ↕
Inference FPS

Camera Frame Timestamp
        ↕
Queue Latency
        ↕
Inference Latency
        ↕
End-to-End Latency

Camera Resolution
        ↕
Memory Bandwidth
        ↕
Preprocessing Cost

ML Backend
        ↕
Inference Time
        ↕
FPS
        ↕
Power
        ↕
Temperature
```

------------------------------------------------------------------------

# 54. Complete Benchmark Dashboard

Recommended UI sections:

## Device

``` text
Device Model
Android Version
SoC
CPU
GPU
NPU
RAM
```

## Camera

``` text
Camera ID
Facing
Resolution
FPS
Format
Sensor Orientation
Actual FPS
Frame Drops
```

## ML

``` text
Model
Input Size
Precision
Backend
Threads
Inference FPS
Inference Latency
```

## Pipeline

``` text
Input FPS
Processed FPS
Dropped FPS
Queue Depth
Preprocess
Inference
Postprocess
Tracking
End-to-End Latency
```

## System

``` text
CPU
GPU
NPU
RAM
Temperature
Power
Thermal Status
```

## Accuracy

``` text
Precision
Recall
mAP
False Positive
False Negative
```

------------------------------------------------------------------------

# 55. Recommended Real-Time Dashboard

Show primary metrics prominently:

``` text
┌────────────────────────────────────┐
│ CAMERA                             │
│ 1920×1080    30 FPS                │
│ Actual: 29.8 FPS                   │
│ Dropped: 1.2%                      │
├────────────────────────────────────┤
│ ML                                 │
│ YOLO11n     640×640                │
│ GPU / FP16                         │
│ Inference: 17.5 FPS                │
│ Inference: 57 ms                   │
├────────────────────────────────────┤
│ PIPELINE                            │
│ Input: 29.8 FPS                    │
│ Processed: 17.5 FPS                │
│ Output: 29 FPS                     │
│ E2E Latency: 82 ms                 │
├────────────────────────────────────┤
│ SYSTEM                              │
│ CPU: 42%   GPU: 61%                │
│ RAM: 420 MB                        │
│ Temp: 44°C                         │
├────────────────────────────────────┤
│ STABILITY                           │
│ Thermal: Normal                    │
│ Frame Drop: 1.2%                   │
│ Sustained FPS: 16.9                │
└────────────────────────────────────┘
```

------------------------------------------------------------------------

# 56. Benchmark Test Modes

Implement separate test modes.

## Mode 1 --- Camera Only

Measure:

-   resolution
-   FPS
-   frame interval
-   frame drops
-   camera stability

No ML.

------------------------------------------------------------------------

## Mode 2 --- Camera + Preview

Measure:

-   camera FPS
-   preview FPS
-   rendering latency
-   frame drops

No ML.

------------------------------------------------------------------------

## Mode 3 --- ML Only

Use a fixed input dataset.

Measure:

-   inference time
-   FPS
-   accuracy
-   backend
-   precision

This isolates model performance.

------------------------------------------------------------------------

## Mode 4 --- Camera + ML

Measure:

-   camera FPS
-   ML FPS
-   preprocessing
-   inference
-   postprocessing
-   frame drops
-   end-to-end latency

------------------------------------------------------------------------

## Mode 5 --- Full Pipeline

``` text
Camera
+
ML
+
Tracking
+
Rendering
+
System Monitoring
```

This is the most realistic production test.

------------------------------------------------------------------------

# 57. Baseline Testing

Before optimization, create a baseline.

Record:

``` text
Device
Camera
Model
Input Size
Backend
Threads
FPS
Latency
CPU
GPU
RAM
Temperature
Accuracy
```

Never optimize without a baseline.

------------------------------------------------------------------------

# 58. One Variable at a Time

When investigating performance, change one major variable at a time.

Example:

``` text
Baseline:
640×640
GPU
FP16

Test:
416×416
GPU
FP16
```

Do not simultaneously change:

``` text
resolution
backend
threads
precision
queue
model
```

Otherwise it becomes difficult to determine what caused the improvement.

------------------------------------------------------------------------

# 59. Optimization Matrix

Use a benchmark matrix.

Example:

  -----------------------------------------------------------------------------------------
  Model        Input Backend   Precision     Threads      FPS   Latency   Accuracy     Temp
  --------- -------- --------- ----------- --------- -------- --------- ---------- --------
  YOLO11n        320 CPU       FP32                4       18      55ms        86%     42°C

  YOLO11n        416 GPU       FP16               \-       22      45ms        89%     44°C

  YOLO11n        640 GPU       FP16               \-       14      71ms        92%     46°C

  YOLO11n        640 NPU       INT8               \-       25      40ms        90%     43°C
  -----------------------------------------------------------------------------------------

Select based on requirements.

------------------------------------------------------------------------

# 60. Bottleneck Classification

Classify the bottleneck before optimizing.

## Camera Bottleneck

Symptoms:

-   camera cannot reach target FPS
-   frame interval unstable
-   camera stalls
-   frame drops before ML

Investigate:

-   supported FPS range
-   resolution
-   camera configuration
-   device thermal state
-   camera HAL

------------------------------------------------------------------------

## Preprocessing Bottleneck

Symptoms:

``` text
Camera FPS = 30
Inference = fast
Preprocess = 20 ms
```

Investigate:

-   color conversion
-   bitmap creation
-   memory copies
-   resize
-   rotation
-   allocations

------------------------------------------------------------------------

## Inference Bottleneck

Symptoms:

``` text
Preprocess = 3 ms
Inference = 90 ms
Postprocess = 3 ms
```

Investigate:

-   model size
-   input resolution
-   precision
-   backend
-   thread count
-   operator support

------------------------------------------------------------------------

## Postprocessing Bottleneck

Symptoms:

``` text
Inference = 20 ms
Postprocess = 30 ms
```

Investigate:

-   NMS
-   excessive candidates
-   allocations
-   tracking
-   result transformation

------------------------------------------------------------------------

## Rendering Bottleneck

Symptoms:

``` text
ML Output = 30 FPS
Display = 15 FPS
```

Investigate:

-   UI rendering
-   overlays
-   bitmap rendering
-   Compose recomposition
-   Surface/Texture handling

------------------------------------------------------------------------

## Thermal Bottleneck

Symptoms:

``` text
Initial FPS = 20
Sustained FPS = 12
Temperature increasing
```

Investigate:

-   CPU/GPU load
-   NPU frequency
-   power
-   cooling
-   workload intensity

------------------------------------------------------------------------

# 61. Common Performance Mistakes

## Mistake 1

Using camera resolution directly as model input.

### Better

Use an appropriate model input size.

------------------------------------------------------------------------

## Mistake 2

Processing every frame when the model cannot keep up.

### Better

Use latest-frame processing or controlled frame sampling.

------------------------------------------------------------------------

## Mistake 3

Unlimited queue.

### Better

Use a bounded queue.

------------------------------------------------------------------------

## Mistake 4

Creating Bitmap objects every frame.

### Better

Reuse buffers and minimize copies.

------------------------------------------------------------------------

## Mistake 5

Running inference on UI thread.

### Better

Use a dedicated worker.

------------------------------------------------------------------------

## Mistake 6

Increasing thread count without benchmarking.

### Better

Benchmark thread counts.

------------------------------------------------------------------------

## Mistake 7

Using NPU without checking operator support.

### Better

Verify actual execution and fallback.

------------------------------------------------------------------------

## Mistake 8

Measuring only average inference time.

### Better

Measure P50/P95/P99 and sustained FPS.

------------------------------------------------------------------------

## Mistake 9

Testing for only a few seconds.

### Better

Run sustained tests.

------------------------------------------------------------------------

## Mistake 10

Calling confidence score "accuracy."

### Better

Use a labeled validation dataset.

------------------------------------------------------------------------

# 62. Android-Specific Implementation Guidance

Recommended components may include:

``` text
Camera2
CameraX
ImageAnalysis
ImageReader
SurfaceTexture
SurfaceView
TextureView
TFLite
ONNX Runtime
MediaPipe
ML Kit
NNAPI
Vendor NPU SDK
```

Choose based on project requirements and device support.

------------------------------------------------------------------------

# 63. CameraX Pattern

For CameraX ImageAnalysis, a real-time configuration commonly uses:

``` text
ImageAnalysis
    ↓
KEEP_ONLY_LATEST
    ↓
Analyzer
    ↓
ML Worker
```

Always close each `ImageProxy` after processing or after safely
transferring the required data.

Failure to close frames can stall the camera pipeline.

------------------------------------------------------------------------

# 64. Camera2 Pattern

For Camera2:

``` text
CameraDevice
    ↓
CaptureSession
    ↓
ImageReader
    ↓
OnImageAvailable
    ↓
Frame Queue
    ↓
ML Worker
```

Control:

-   buffer count
-   image format
-   resolution
-   capture request
-   target surfaces
-   timestamps

Avoid blocking the camera callback with heavy processing.

------------------------------------------------------------------------

# 65. Image Lifetime

A camera frame may be backed by native resources.

Do not hold a camera frame longer than necessary.

Preferred:

``` text
Receive
 ↓
Copy required data into reusable buffer
 ↓
Close image
 ↓
Process safely
```

or process synchronously inside a worker if the image lifetime is
correctly managed.

The exact approach depends on the API and ML runtime.

------------------------------------------------------------------------

# 66. Memory Copy Budget

Count how many times a frame is copied.

Ideal:

``` text
Camera Buffer
 ↓
Minimal conversion
 ↓
ML Input
```

Avoid:

``` text
Camera
 ↓
Copy 1
 ↓
Copy 2
 ↓
Bitmap
 ↓
Copy 3
 ↓
Tensor
```

Every copy can cost:

-   CPU time
-   memory bandwidth
-   cache pressure
-   latency
-   power

------------------------------------------------------------------------

# 67. Benchmark Warm-Up

ML runtimes can have initialization overhead.

Separate:

``` text
Cold Start
Warm-Up
Steady State
```

Recommended:

``` text
Application Start
 ↓
Model Load
 ↓
Delegate Initialization
 ↓
Warm-Up Inferences
 ↓
Benchmark
```

Do not mix model initialization time into steady-state inference
latency.

------------------------------------------------------------------------

# 68. Cold Start Metrics

Record separately:

-   model loading time
-   runtime initialization time
-   delegate initialization
-   first inference
-   warm-up inference
-   steady-state inference

Example:

``` text
Model Load:       120 ms
Delegate Init:    250 ms
First Inference:  180 ms
Steady State:      55 ms
```

------------------------------------------------------------------------

# 69. Benchmark Repeatability

Run each configuration multiple times.

Recommended:

``` text
3–5 short runs
+
1 long sustained run
```

Calculate:

-   mean
-   median
-   standard deviation
-   min
-   max
-   P95

Avoid selecting a configuration based on a single lucky run.

------------------------------------------------------------------------

# 70. Environment Control

For reliable comparison, control:

-   screen state
-   brightness
-   background applications
-   network activity
-   battery level
-   charging state
-   device temperature
-   camera scene
-   lighting
-   model version
-   Android version
-   app build

Record all relevant environmental conditions.

------------------------------------------------------------------------

# 71. Camera Scene Control

Accuracy and performance can vary with scene complexity.

Test:

``` text
Static scene
Normal motion
Fast motion
Low light
Bright light
Multiple objects
Small objects
Occlusion
Different distances
```

For face systems:

``` text
Single face
Multiple faces
Near face
Far face
Side face
Low light
Backlight
Motion
```

Use consistent datasets for fair comparisons.

------------------------------------------------------------------------

# 72. Benchmark Profiles

Define standard profiles.

## Profile A --- Low Power

``` text
Camera: 720p
ML: 320/416
Low inference frequency
Low power backend
```

## Profile B --- Balanced

``` text
Camera: 1080p
ML: 416/640
GPU/NPU
Moderate FPS
```

## Profile C --- High Accuracy

``` text
Camera: 1080p/2K
ML: 640+
Higher inference frequency
Accuracy-focused model
```

## Profile D --- Maximum Performance

``` text
Lightweight model
Accelerator
Optimized input
Latest-frame queue
Minimal preprocessing
```

------------------------------------------------------------------------

# 73. Acceptance Criteria

Define target values before optimization.

Example:

``` text
Camera FPS:           ≥ 30
ML FPS:               ≥ 15
End-to-End Latency:   ≤ 100 ms
Frame Drop:           ≤ 5%
RAM:                  ≤ 500 MB
Temperature:          ≤ defined limit
Thermal Throttling:   None during target duration
Accuracy:             ≥ 90% mAP
```

Actual thresholds must be defined by the product.

------------------------------------------------------------------------

# 74. Pass / Fail

A benchmark should produce an explicit result.

Example:

``` text
PASS

Camera FPS: 29.8 / 30
ML FPS: 17.2 / 15
Latency: 78 ms / 100 ms
Frame Drop: 2.1% / 5%
Accuracy: 91.2% / 90%
Thermal: Stable
```

Or:

``` text
FAIL

ML FPS: 11.4 / 15
Reason:
Inference latency too high
```

Do not use a single FPS threshold to determine success.

------------------------------------------------------------------------

# 75. Recommended Final Score

A product-specific weighted score can be used.

Example:

``` text
Performance       30%
Accuracy          30%
Latency           15%
Stability         10%
Thermal           10%
Power              5%
```

Do not use this exact weighting universally.

For safety-critical systems, accuracy and latency may deserve much
higher weighting.

------------------------------------------------------------------------

# 76. Example End-to-End Configuration

``` text
Device:
Android Embedded Device

Camera:
1920×1080
30 FPS
YUV_420_888

Frame Pipeline:
KEEP_ONLY_LATEST
Queue = 1

Preprocessing:
640×640
Letterbox
YUV → RGB
Normalized 0–1

Model:
YOLO11n
TFLite
FP16

Backend:
GPU

Threads:
Not applicable for GPU inference

Performance:
Camera FPS: 29.7
ML Input FPS: 29.7
Inference FPS: 17.4
Output FPS: 29.0

Latency:
Preprocess: 5 ms
Inference: 55 ms
Postprocess: 3 ms
Tracking: 2 ms
End-to-End: 82 ms

System:
CPU: 38%
GPU: 60%
RAM: 410 MB

Thermal:
Start: 39°C
End: 45°C
Status: Normal

Accuracy:
mAP50: 91%
Precision: 93%
Recall: 90%

Frame Drops:
3.2%

Result:
PASS
```

------------------------------------------------------------------------

# 77. Optimization Decision Tree

``` text
Is Camera FPS below target?
        │
       YES
        ↓
Optimize camera configuration
        │
       NO
        ↓
Is preprocessing too slow?
        │
       YES
        ↓
Reduce copies / optimize conversion
        │
       NO
        ↓
Is inference too slow?
        │
       YES
        ↓
Test input size
        ↓
Test model
        ↓
Test precision
        ↓
Test backend
        ↓
Tune threads
        │
       NO
        ↓
Is postprocessing too slow?
        │
       YES
        ↓
Optimize NMS / tracking
        │
       NO
        ↓
Is display slow?
        │
       YES
        ↓
Optimize rendering
        │
       NO
        ↓
Does FPS degrade over time?
        │
       YES
        ↓
Investigate thermal/power
        │
       NO
        ↓
Validate accuracy
        ↓
Finalize configuration
```

------------------------------------------------------------------------

# 78. Recommended Optimization Sequence

Use this order unless profiling indicates another bottleneck:

``` text
1. Establish baseline
2. Verify camera FPS
3. Verify actual frame timestamps
4. Verify frame format
5. Remove unnecessary frame copies
6. Use bounded queue
7. Use latest-frame strategy
8. Optimize rotation
9. Optimize resize/crop
10. Optimize color conversion
11. Optimize tensor preparation
12. Benchmark ML input sizes
13. Benchmark model variants
14. Benchmark FP32/FP16/INT8
15. Benchmark CPU/GPU/NPU
16. Tune thread count
17. Optimize postprocessing
18. Add tracking if appropriate
19. Optimize rendering
20. Measure RAM
21. Measure thermal behavior
22. Measure power
23. Run sustained benchmark
24. Validate accuracy
25. Select final configuration
```

------------------------------------------------------------------------

# 79. Performance Investigation Checklist

``` text
[ ] Camera resolution verified
[ ] Camera FPS verified
[ ] Actual FPS calculated from timestamps
[ ] Pixel format verified
[ ] Sensor rotation verified
[ ] Frame queue bounded
[ ] Latest-frame policy verified
[ ] Frame drops measured
[ ] Frame age measured
[ ] Preprocessing measured
[ ] Memory copies measured
[ ] ML input size verified
[ ] Model version verified
[ ] Precision verified
[ ] Backend verified
[ ] Operator fallback checked
[ ] Thread count measured
[ ] Inference latency measured
[ ] P95/P99 latency measured
[ ] Postprocessing measured
[ ] Tracking measured
[ ] Output FPS measured
[ ] End-to-end latency measured
[ ] CPU measured
[ ] GPU measured
[ ] NPU measured if available
[ ] RAM measured
[ ] GC behavior checked
[ ] Temperature measured
[ ] Thermal throttling checked
[ ] Power measured if available
[ ] Accuracy validated
[ ] Sustained benchmark completed
[ ] Pass/fail criteria evaluated
```

------------------------------------------------------------------------

# 80. Final Engineering Principle

The correct target is not:

``` text
Maximum FPS
```

The correct target is:

``` text
Maximum SUSTAINED FPS
        +
Required ACCURACY
        +
Low END-TO-END LATENCY
        +
Low FRAME DROPS
        +
Stable MEMORY
        +
Stable THERMALS
        +
Acceptable POWER
```

The best Android ML configuration is therefore the configuration that
provides the required product behavior under realistic, sustained
operating conditions.

A benchmark should answer these questions:

1.  Can the camera produce the required FPS?
2.  Can the application acquire frames without blocking?
3.  Are frames being dropped intentionally or unexpectedly?
4.  How old is the frame when inference begins?
5.  How much time is spent in preprocessing?
6.  How much time is spent in inference?
7.  How much time is spent in postprocessing?
8.  What is the actual inference FPS?
9.  What is the end-to-end latency?
10. Which hardware is executing the model?
11. How many CPU threads are being used?
12. Is GPU/NPU acceleration actually active?
13. Is there operator fallback?
14. How much RAM is consumed?
15. Does performance degrade with temperature?
16. What is the sustained FPS?
17. What is the actual accuracy?
18. What is the power cost?
19. Which configuration gives the best overall trade-off?
20. Does the configuration meet the product acceptance criteria?

If these questions are measured, the system can be optimized
scientifically instead of relying on assumptions such as:

> "More threads = more FPS"

or:

> "NPU = always fastest"

or:

> "Higher resolution = better ML"

or:

> "Higher FPS = better system."

The correct engineering decision must come from measured data across the
complete Camera → ML → Result pipeline.
