# Tracker-Gyro

Tracker-Gyro is a production-grade, highly optimized Android application showcasing multi-sensor data processing and background hardware system services using Jetpack Compose. This project strictly complies with academic requirements by integrating multiple sensor categories, rendering direct real-time visual feedback, and enforcing rigid Android lifecycle states.

## Architecture & Sensor Modules

The application architecture utilizes an Unidirectional Data Flow (UDF) pattern. Each feature relies purely on Coroutine `StateFlow` streams exposed by clean ViewModels, tracking low-latency real-time hardware data.

### 1. Metal & Compass Mode (Position & Motion Category)
A dual-function feature utilizing `Sensor.TYPE_MAGNETIC_FIELD` and `Sensor.TYPE_ACCELEROMETER`. 
- **Data Flow:** The application cross-references geomagnetic vectors and gravity arrays through `SensorManager.getRotationMatrix` to yield device Azimuth heading.
- **Metal Detector:** The absolute magnitude of the magnetic flux is calculated via `sqrt(x^2 + y^2 + z^2)`. It alerts the user of ferromagnetic proximity hazard (> 100 µT) utilizing system-level Haptics (`Vibrator`).
- **UI:** Rendered as a multi-layered rotating compass dial, alongside a dynamic gauge mode toggled by segmented frosted UI controls.

### 2. Precision Bubble Level (Motion Category)
A dual-axis (pitch/roll) stabilized level tracker.
- **Data Flow:** Driven directly by `Sensor.TYPE_ROTATION_VECTOR` representing a fused high-precision 9-axis spatial rotation state.
- **UI:** A concentric circular frosted ring rendering smooth floating bubble indications dampened by Jetpack Compose's native spring physics (alpha 0.15 low-pass smoothing applied at the ViewModel level).
- **Feedback:** Snaps to an illuminated emerald glow border alongside a satisfying haptic tick when calibrated exactly within ±0.5°.

### 3. Smart Ambient Torch (Environment Category)
Tracks ambient illuminance dynamically manipulating hardware.
- **Data Flow:** Reads `Sensor.TYPE_LIGHT` capturing surrounding light intensity (lux) across a rolling average filter to combat instantaneous flicker or interference. 
- **Hardware Integration:** Employs bounded hysteresis (triggers below 10 lux, shuts off above 25 lux) to autonomously manage the system flashlight using `CameraManager.setTorchMode()`.

### 4. Live Telemetry Dashboard
An aggregated diagnostic view presenting real-time scalar values across all actively tracked sensors instantly evaluating the hardware health.

## Sensor Categorization Table

| Feature Mode | Hardware Constant | Category | Direct UI Manifestation |
|---|---|---|---|
| Auto Torch | `Sensor.TYPE_LIGHT` | Environment | Dynamic glassmorphism dimming and numeric lux readout. |
| Compass | `Sensor.TYPE_MAGNETIC_FIELD`, `Sensor.TYPE_ACCELEROMETER` | Position, Motion | Real-time cardinal navigation dial and heading degrees. |
| Metal Detector | `Sensor.TYPE_MAGNETIC_FIELD` | Position, Environment | Pulsing arc gauge visualizing total magnetic magnitude (µT). |
| Bubble Level | `Sensor.TYPE_ROTATION_VECTOR` | Motion | Interactive 2D coordinate bubble animating spatial pitch and roll. |

## Lifecycle Management Strategy

Zero background battery drain is strictly enforced. All underlying physical hardware monitors (`SensorTracker`) seamlessly integrate with `DisposableEffect` mapping to modern Jetpack Compose environments.
- **ON_RESUME:** Hooks trigger `sensorManager.registerListener` only when the active tab is in primary focus and fully rendered.
- **ON_PAUSE:** Hardware releases locks immediately overriding the UI loop with `sensorManager.unregisterListener`, freeing system hardware threads.

## UI Design System
The visual language strictly abides by neutral frosted glassmorphism rules. Utilizes absolute `#0F172A` deep slates, translucent micro-borders via `Brush.verticalGradient`, avoiding high-contrast AI gradients. All navigations rest neatly behind a floating capsule tab row aligned perfectly inside standard window insets padding.

## Installation & Setup

1. **Gradle Compilation:** Project is compiled against SDK 37 utilizing Jetpack Compose Material 3 and Jetpack Navigation. Build using Android Studio 2024+.
2. **Execution:** Assemble using `assembleDebug` locally over ADB to physical hardware. Virtual emulators do not fully support robust 9-axis rotation configurations.