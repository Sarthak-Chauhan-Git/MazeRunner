# Technology Stack

## Runtime and language

- Native Android application.
- Java source with Java 11 source and target compatibility (`app/build.gradle:29-30`).
- Gradle build using the Android application plugin.
- Foojay toolchain resolver configured with toolchain version 25 in `gradle/gradle-daemon-jvm.properties`.

## Android configuration

- Namespace/application ID: `com.example.myapplication` (`app/build.gradle:6,12`).
- Compile SDK: 37 (`app/build.gradle:7-9`).
- Minimum SDK: 24 (`app/build.gradle:13`).
- Target SDK: 37 (`app/build.gradle:14`).
- Version: code 1, name `1.0` (`app/build.gradle:15-16`).
- UI uses `AppCompatActivity`, XML layouts, Android Views, and a custom `Canvas` renderer.

## Build tooling

- Android Gradle Plugin 9.4.1 (`gradle/libs.versions.toml:2,21`).
- Gradle wrapper is present through `gradlew` and `gradlew.bat`.
- Configuration cache is enabled in `gradle.properties`.

## Dependencies

Runtime:

- `androidx.activity:activity-ktx:1.13.0`
- `androidx.appcompat:appcompat:1.8.0`
- `androidx.constraintlayout:constraintlayout:2.2.2`
- `com.google.android.material:material:1.14.0`

Testing:

- `junit:junit:4.13.2`
- `androidx.test.ext:junit:1.3.0`
- `androidx.test.espresso:espresso-core:3.7.0`

## Observed legacy/deprecation signals

No deprecated or end-of-life API use was established from the inspected source. The UI is implemented with classic Android Views rather than Compose; this is an existing implementation fact, not a recommendation.
