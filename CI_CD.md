# 🚀 CI/CD Pipeline - Event Hive

This project uses **GitHub Actions** to automate testing and building. This ensures that every change is verified against our 23+ unit tests and that the application still compiles.

---

## 🏗 Workflow: `android-ci.yml`

The pipeline is triggered on every **Push** and **Pull Request** to the `main` or `master` branches.

### 🔹 Job 1: `test`
- **Environment**: Ubuntu Latest
- **Action**: 
  1. Sets up JDK 17.
  2. Creates a dummy `google-services.json` to satisfy the Google Services plugin.
  3. Runs all unit tests via `./gradlew :app:testDebugUnitTest`.
  4. **Artifact**: Uploads the HTML test report so you can see failures in the GitHub UI.

### 🔹 Job 2: `build` (Depends on `test`)
- **Action**:
  1. Builds the Debug APK via `./gradlew assembleDebug`.
  2. **Artifact**: Uploads the generated `app-debug.apk`.

---

## 🔒 Handling Secrets

Currently, the CI uses a **dummy** `google-services.json`. This is enough to run **Unit Tests** (since they use Mocks) and verify the **Build** structure.

### For Production Releases:
To build a production APK in CI, you should:
1.  **Encode your real `google-services.json`**:
    ```bash
    base64 -i google-services.json
    ```
2.  **Add it to GitHub Secrets**: Save the output as a secret named `GOOGLE_SERVICES_JSON`.
3.  **Update the workflow**:
    ```yaml
    - name: Decode google-services.json
      run: echo "${{ secrets.GOOGLE_SERVICES_JSON }}" | base64 -d > app/google-services.json
    ```

---

## ✅ Best Practices Implemented
- **Caching**: Uses `actions/setup-java` built-in Gradle caching to speed up builds by ~50%.
- **Separation of Concerns**: Tests run before the build to fail fast and save compute minutes.
- **Reporting**: Always uploads reports even if tests fail, aiding in quick debugging.
