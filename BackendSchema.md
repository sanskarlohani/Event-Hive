# 🗄 Backend Schema & Data Architecture - Event Hive

## 1. Firestore Hierarchy

### `/users/{uid}`
*   `systemRole`: String (student, studentGuide, superAdmin)
*   `collegeId`: String
*   `effectivePermissions`: Map<String, Boolean> (e.g., `canCreateEvent`, `canModerateChat`)
*   `customRoleIds`: List<String>
*   `clubs`: List (Legacy local cache)
*   `events`: List (Legacy local cache)

### `/Categories/{catId}`
*   `name`: String
*   `clubs`: List<String> (Club IDs for navigation)
*   **Sub-collection: `Clubs/`**
    *   `name`, `description`, `logoUrl`, `bannerUrl`, `isPublic`
    *   **Sub-collection: `Members/`** (`userId`, `role`: ADMIN/MEMBER)
    *   **Sub-collection: `Events/`**
        *   `title`, `description`, `startTime`, `endTime`, `mode`
        *   `participantsIds`: List<String> (Used for Real-time Access Sync)

### `/colleges/{collegeId}`
*   `name`, `collegeCode`, `emailDomain`
*   **Sub-collection: `roles/`** (Maps role name to permission set)

## 2. Realtime Database (RTDB) Tree

### `chats/{roomId}/`
*   **`metadata/`**:
    *   `type`: dm | club | event | anonymous
    *   `name`: (Resolved from Firestore)
    *   `relatedId`: (ClubId, EventId, or RoomId)
    *   `categoryId`: (For name resolution path)
    *   `clubId`: (For event-name resolution path)
    *   `readOnly`: Boolean
*   **`messages/{msgId}/`**:
    *   `senderId`: (uid or "anon")
    *   `displayName`: (Real name or daily Alias)
    *   `text`, `timestamp`, `deletedAt` (Null if not moderated)

### `chatAccess/{uid}/{roomId}`
*   `value`: true (Enables Rule-based read/write access)

## 3. Storage Infrastructure
*   `/club_logos/{clubName}/{filename}`: High-res square logos.
*   `/club_banners/{clubName}/{filename}`: Optimized landscape banners.
*   `/profile_images/{uid}/{filename}`: User avatars.
*   `/event_posters/{eventId}.png`: Dynamic marketing material.

## 4. Automation & Logic (Cloud Functions)
*   **Permission Flattener**: Triggered on `customRoleIds` change.
*   **Access Healer**: Automatically populates `chatAccess` nodes based on Firestore membership.
*   **Daily Alias Generator**: Rotates `anonAliases` at 00:00 UTC.
