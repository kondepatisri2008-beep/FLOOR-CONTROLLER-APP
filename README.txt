Bluetooth Car Controller (HC-05 + Arduino Nano)
=================================================

Arduino wiring expected by the matching Arduino sketch:
  HC-05 TX  -> Nano D2
  HC-05 RX  -> Nano D3
  L298N IN1 -> Nano D4
  L298N IN2 -> Nano D5
  L298N IN3 -> Nano D6
  L298N IN4 -> Nano D7
  All grounds common.

Commands sent by this Android app:
  F = Forward
  B = Backward
  L = Left
  R = Right
  S = Stop

Behaviour:
  - Pair HC-05 first in Android Settings.
  - Open the app.
  - Allow Nearby devices/Bluetooth permission.
  - Choose HC-05 from the paired-device list and tap Connect.
  - Hold a direction button to move.
  - Releasing a direction button automatically sends S (Stop).
  - STOP also sends S immediately.

Build:
  Open this folder in Android Studio.
  Let Gradle sync.
  Build > Build App Bundles or APKs > Build APKs.
  Install the generated APK on your Android phone.

Note:
  This uses Bluetooth Classic SPP, which is the correct protocol for HC-05.
