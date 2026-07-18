# ⚙️ Technical Requirement Document (TRD) - Event Hive

## 1. Tech Stack
*   **Language**: Kotlin
*   **UI Framework**: Jetpack Compose (Material 3)
*   **Architecture**: Clean Architecture + MVVM
*   **Dependency Injection**: Hilt
*   **Backend**: 
    *   **Firestore**: Metadata (Users, Clubs, Events).
    *   **Realtime Database (RTDB)**: Chat messages and active states.
    *   **Cloud Functions**: Server-side permission computation and scheduled tasks.
    *   **Storage**: Binary assets (Images, QR Codes).

## 2. Architectural Layers
### A. Presentation Layer
*   **UI**: Declarative Compose screens.
*   **ViewModel**: State management using `StateFlow` and `Resource<T>` wrappers.
*   **Navigation**: Type-safe navigation with deep-link support.

### B. Domain Layer
*   **Use Cases**: Pure Kotlin business logic (e.g., `IssueTicketUseCase`, `JoinClubUseCase`).
*   **Models**: Immutable data classes.

### C. Data Layer
*   **Repositories**: Interface-driven data fetching.
*   **Mappers**: Safe transformation of Firebase snapshots to domain models.

## 3. Security Model
*   **Permissions**: A flattened `effectivePermissions` map is computed server-side in Cloud Functions based on assigned roles.
*   **Client-Side Protection**: `NavGuard` component prevents unauthorized access to UI nodes.
*   **Rules**: 
    *   **Firestore**: Restricted field updates (preventing self-assignment of roles).
    *   **RTDB**: Sender verification ensuring `senderId` matches `auth.uid`.

## 4. Key Performance Optimizations
*   **Parallel Fetching**: Parallel coroutine execution for fetching chat previews.
*   **Caching**: Local ViewModel caching of user profiles to eliminate redundant Firestore reads during high-frequency tasks (chatting).
