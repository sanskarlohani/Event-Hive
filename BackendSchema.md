# 🗄 Backend Schema & Data Architecture - Event Hive

## 1. Firestore Collections

### `/users/{uid}`
*   `systemRole`: String (student, studentGuide, superAdmin)
*   `collegeId`: String
*   `effectivePermissions`: Map<String, Boolean>
*   `customRoleIds`: List<String>

### `/Categories/{catId}`
*   `title`: String
*   `Clubs/`: Sub-collection
    *   `title`, `description`, `bannerUrl`
    *   `Members/`: Sub-collection (userId, role: ADMIN/MEMBER)
    *   `Events/`: Sub-collection
        *   `title`, `startTime`, `endTime`
        *   `mode`: Enum (SINGLE, GROUP, BOTH)
        *   `participantsIds`: List<String> (for fast membership checks)

### `/colleges/{collegeId}`
*   `name`, `collegeCode`, `emailDomain`
*   `roles/`: Sub-collection (permissions mapping)

## 2. Realtime Database (RTDB) Tree

### `chats/{roomId}/`
*   `metadata/`: (type, collegeId, relatedId, readOnly)
*   `messages/{msgId}/`:
    *   `senderId`: String (uid or "anon")
    *   `displayName`: String (Real name or Alias)
    *   `text`, `timestamp`, `deletedAt`

### `chatAccess/{uid}/{roomId}`
*   Value: `Boolean` (Determines if user can read/write to room)

## 3. Storage Paths
*   `/event_posters/{catId}/{clubId}/{eventId}`
*   `/profile_images/{uid}`
*   `/tickets/{userId}/{ticketId}.png` (QR Codes)

## 4. Cloud Function Triggers
*   `computeEffectivePermissions`: Triggered on `users` update; flattens role perms.
*   `onEventEnd`: Scheduled; locks chats and marks room `readOnly`.
*   `generateAnonAlias`: On-call; creates daily user aliases.
