# Repository Guidelines

## Project Structure & Module Organization

This is a Kotlin, Jetpack Compose, multi-module Android application. `app/` owns the application entry point, navigation wiring, manifest, and app resources. Shared capabilities live under `core/`: `common`, `designsystem`, `navigation`, `network`, and `analytics`. Product flows are isolated under `feature/`, including `auth`, `onboarding`, `home`, `training`, `progress`, and `profile`.

Place production Kotlin in `<module>/src/main/kotlin`, Android resources in `<module>/src/main/res`, local JVM tests in `<module>/src/test`, and device tests in `<module>/src/androidTest`. Architectural notes and product specifications are stored in `nextrank-agent-pack/`; treat them as reference material, not application source.

## Build, Test, and Development Commands

Run commands from the repository root using the Gradle wrapper:

- `./gradlew assembleDebug` (`.\gradlew.bat assembleDebug` on Windows): builds a debug APK.
- `./gradlew testDebugUnitTest`: runs JVM unit tests for debug variants.
- `./gradlew connectedDebugAndroidTest`: runs instrumentation tests on a connected emulator or device.
- `./gradlew lintDebug`: performs Android lint checks.
- `./gradlew ktlintCheck detekt`: runs the configured Kotlin static-analysis tools.
- `./gradlew clean`: removes generated build outputs.

Use Android Studio to launch the `app` configuration on an API 28+ device.

## Coding Style & Naming Conventions

Follow Kotlin's official style with four-space indentation and trailing commas in multiline declarations. Use `UpperCamelCase` for classes and composables, `lowerCamelCase` for functions and properties, and `UPPER_SNAKE_CASE` for constants. Keep packages under `com.nextrank` and organize feature code by responsibility (`data`, `domain`, `di`, `presentation`). Name Compose screens `*Screen`, state holders `*UiState`, view models `*ViewModel`, and dependency modules `*Module`.

## Testing Guidelines

JUnit is used for local tests, with MockK for doubles and Turbine for Flow assertions; AndroidX JUnit supports instrumentation tests. Name tests after the subject under test (for example, `AuthViewModelTest`) and use descriptive backtick test names. Add unit tests alongside new business logic and device tests for Android or Compose behavior that cannot be validated on the JVM.

## Commit & Pull Request Guidelines

Git history is unavailable in this checkout, so use concise, imperative commit subjects such as `Add training session timer`. Keep each commit focused. Pull requests should explain the change, list verification commands, link relevant issues, and include screenshots or recordings for UI changes. Call out configuration, schema, or architecture changes explicitly.

## Security & Configuration

Copy `local.properties.template` to `local.properties` and provide local Supabase values there. Never commit secrets, generated APKs, IDE state, or machine-specific SDK paths.
