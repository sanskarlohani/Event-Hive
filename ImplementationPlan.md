# 🚀 Implementation Plan - Event Hive

## Phase 1: Foundation (Weeks 1-2)
*   **Infrastructure**: Setup Hilt, Navigation, and Theme.
*   **Authentication**: Implement email verification and college auto-assignment logic.
*   **Data Models**: Define immutable core models and Firestore/RTDB schema.

## Phase 2: Core Features (Weeks 3-5)
*   **Discovery**: Implement Category and Club listing with parallel image loading (Coil).
*   **Events**: Develop Solo/Group registration flows and `IssueTicketUseCase`.
*   **Messaging**: Build real-time DM and Group Chat using RTDB.

## Phase 3: Administration & Security (Weeks 6-7)
*   **RBAC**: Implement the custom role management system and Cloud Functions for permission flattening.
*   **Hardening**: Apply Firebase Security Rules (Prevents privilege escalation and spoofing).
*   **Anonymous Chat**: Develop the daily-alias seeding logic.

## Phase 4: Polish & Scaling (Weeks 8+)
*   **Optimizations**: Implement parallel fetching for chat previews and UI caching for user metadata.
*   **A11y**: Add semantic accessibility labels and ensure RTL support.
*   **Testing**: 
    *   Unit Testing for all Use Cases and ViewModels.
    *   Instrumented UI tests for the Registration flow.

## Testing Strategy
1.  **Unit Tests**: Verify business logic without external dependencies (MockK).
2.  **Integration Tests**: Test Compose UI logic using instrumented tests.
3.  **Security Testing**: Manual verification of Firebase Rules via authenticated SDK calls.
