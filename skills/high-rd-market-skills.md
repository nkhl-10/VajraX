# High R&D Market Skills & Product Development Guide

## 1. Existing Skills & Workflows
*The following skills have been cataloged with their respective file names and core content to ensure accurate product development.*

| File Name | Content / Purpose |
| :--- | :--- |
| `AGENTS.md` | Mandatory routing rules for context-mode MCP tools. Protects context window and enforces code-based analysis. |
| `always-use-context-mode.md` | Rules emphasizing the use of MCP tools and sandbox execution over basic shell commands like `curl`. |
| `android-camera-ml-processing.md` | Complete engineering methodology for designing Android Camera and ML processing performance. |
| `build-fast.md` | Fast build and deployment workflow prerequisites and steps. |
| `build.md` | High-level outline for preparing, initializing, validating, and compiling the project. |
| `design-principles-and-patterns.md` | Detailed guide on SOLID principles and standard programming patterns. |
| `fdroid-deployment.md` | Workflow for deploying the FinalBenchmark 2 application to F-Droid. |
| `fdroid-update.md` | Release workflow for preparing and publishing new FinalBenchmark releases. |
| `fix.md` | Quick workflow for identifying and fixing bugs or issues in the codebase. |
| `implement-feature.md` | Detailed workflow for implementing features, ensuring quality and proper review. |
| `instructions.md` | Basic rules for feature request, implementation, review, testing, and approval. |
| `project-info.md` / `project-always-on.md` | Comprehensive overview of the "FinalBenchmark2" Android benchmarking application, tech stack, and structure. |
| `update-version-and-buildlog.md` | Guidelines for updating version numbers and maintaining the build log. |

---

## 2. New High R&D Ideas & Market-Accurate Skills

To position the product (e.g., FinalBenchmark2 and future apps) at the forefront of the market, the following **New Skills** should be developed and added to our repository. These reflect the highest standard of current R&D in mobile and AI technology:

### A. Edge AI & On-Device LLM Optimization (`edge_ai_optimization.md`)
- **Market Context**: Privacy-first, offline-capable AI is dominating the mobile market. 
- **Skill Content**: Guidelines for integrating and profiling local LLMs (like `llama.cpp`), utilizing Android NNAPI, NPUs, and Google Play Services ML Kit. Focus on quantization (INT8/INT4), memory offloading, and inference speed benchmarking.

### B. Thermal Throttling & Power Profiling (`thermal_power_profiling.md`)
- **Market Context**: High-performance apps (gaming, ML) are often gated by device thermals, not just raw compute.
- **Skill Content**: Deep-dive methodologies for measuring sustained performance, utilizing Android Thermal API, battery temperature tracking, and correlating performance drops with thermal states over long durations.

### C. Spatial Computing & AR/VR Metrics (`ar_vr_performance_metrics.md`)
- **Market Context**: With the rise of XR devices, standard 2D benchmarking is insufficient.
- **Skill Content**: Benchmarking frame drops, motion-to-photon latency, and high-refresh-rate (90Hz/120Hz) sustained rendering using OpenGL/Vulkan. 

### D. Kotlin Multiplatform (KMP) Architecture (`kmp_migration_strategy.md`)
- **Market Context**: Cross-platform development is essential for reaching wider markets without duplicating native codebases.
- **Skill Content**: Strategies for sharing benchmarking logic between iOS and Android. Abstracting native system calls (like CPU info and RAM stats) using `expect`/`actual` paradigms.

### E. Advanced Automated Quality Gates (`ci_cd_ml_quality_gates.md`)
- **Market Context**: Continuous delivery requires rigorous, automated performance regression testing.
- **Skill Content**: Setting up GitHub Actions/GitLab CI to automatically run benchmarks on physical device farms (e.g., Firebase Test Lab) on every PR, using statistical analysis to flag performance regressions automatically.

### F. Next-Gen Storage Benchmarking (DirectStorage / NVMe) (`advanced_storage_io.md`)
- **Market Context**: Mobile devices are adopting ultra-fast NVMe storage. Applications need to utilize asynchronous I/O to prevent UI blocking.
- **Skill Content**: Benchmarking random vs. sequential reads at varying block sizes, testing SQLite/Room database performance under high concurrent loads, and optimizing I/O dispatchers.

---
*To implement any of these new skills, simply request the creation of their respective markdown files. They will be generated with full implementation details, architectural patterns, and code examples.*
