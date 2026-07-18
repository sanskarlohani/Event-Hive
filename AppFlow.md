# 🌊 App Flow & User Journeys - Event Hive

## 1. Onboarding & Auth Flow
1.  **Splash**: Check auth status.
2.  **Login/Signup**: Email + Password or Google Sign-in.
3.  **Verification**: Required for college email access.
4.  **College Assignment**: 
    *   *Path A*: Domain matches ➔ Auto-joined.
    *   *Path B*: No match ➔ Enter College Code.

## 2. Event Registration Flow
1.  **Event Detail**: User views event details.
2.  **Mode Selection**: User chooses Solo or Group (if permitted).
3.  **Team Formation**: Enter Team Name + Search/Select members.
4.  **Additional Info**: User answers event-specific questions.
5.  **Confirmation**: `IssueTicketUseCase` runs ➔ QR generated ➔ Added to "My Tickets".

## 3. Administrator Flow (College Admin)
1.  **Dashboard**: Overview of college metrics.
2.  **Role Manager**: Create/Edit custom roles (e.g., "Event Coordinator").
3.  **Member List**: Select user ➔ Assign Role ➔ Trigger Permission Recalculation.
4.  **Report Generation**: Export attendance/participation data.

## 4. Chat Flow
1.  **Home**: View list of active threads (DMs, Clubs, Events).
2.  **Enter Room**: Metadata and history loaded in parallel.
3.  **Send Message**: Verified by Security Rules ➔ Real-time broadcast.
4.  **Moderation**: Admins can delete messages (Permission: `canModerateChat`).
