import { onCall, HttpsError } from "firebase-functions/v2/https";
import { onDocumentWritten } from "firebase-functions/v2/firestore";
import { onSchedule } from "firebase-functions/v2/scheduler";
import * as admin from "firebase-admin";

admin.initializeApp();

const db = admin.firestore();
const rtdb = admin.database();

const PERMISSION_KEYS = [
  "canCreateEvent",
  "canEditEvent",
  "canDeleteEvent",
  "canCreateClub",
  "canManageClubMembers",
  "canViewReports",
  "canModerateChat",
  "canSendAnnouncements",
] as const;

const ADJECTIVES = [
  "Teal",
  "Swift",
  "Quiet",
  "Nova",
  "Bright",
  "Mellow",
  "Crimson",
  "Lunar",
  "Silver",
  "Bold",
];

const ANIMALS = [
  "Otter",
  "Falcon",
  "Panda",
  "Lynx",
  "Fox",
  "Dolphin",
  "Hawk",
  "Tiger",
  "Whale",
  "Koala",
];

function hashString(input: string): number {
  let hash = 0;
  for (let i = 0; i < input.length; i += 1) {
    hash = (hash << 5) - hash + input.charCodeAt(i);
    hash |= 0;
  }
  return Math.abs(hash);
}

function utcDateKey(date = new Date()): string {
  return date.toISOString().slice(0, 10);
}

function emptyPermissions(): Record<string, boolean> {
  const out: Record<string, boolean> = {};
  for (const key of PERMISSION_KEYS) out[key] = false;
  return out;
}

async function getOrCreateAlias(uid: string, collegeId: string): Promise<string> {
  const dateKey = utcDateKey();
  const aliasRef = db
    .collection("anonAliases")
    .doc(collegeId)
    .collection(dateKey)
    .doc(uid);

  const existing = await aliasRef.get();
  if (existing.exists) {
    return existing.get("alias") as string;
  }

  const seed = hashString(`${uid}_${dateKey}`);
  const alias = `${ADJECTIVES[seed % ADJECTIVES.length]}${ANIMALS[seed % ANIMALS.length]}#${(seed % 99) + 1}`;
  await aliasRef.set({
    alias,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
  });
  return alias;
}

export const createStudentGuide = onCall(async (request) => {
  const auth = request.auth;
  if (!auth) throw new HttpsError("unauthenticated", "Authentication required.");

  const callerSnap = await db.collection("users").doc(auth.uid).get();
  if (!callerSnap.exists || callerSnap.get("systemRole") !== "superAdmin") {
    throw new HttpsError("permission-denied", "Only super admins can create student guides.");
  }

  const { name, email, collegeId } = request.data || {};
  if (!name || !email || !collegeId) {
    throw new HttpsError("invalid-argument", "name, email and collegeId are required.");
  }

  const tempPassword = Math.random().toString(36).slice(-10) + "A1!";
  const userRecord = await admin.auth().createUser({
    email,
    password: tempPassword,
    displayName: name,
  });

  const resetLink = await admin.auth().generatePasswordResetLink(email);
  await db.collection("users").doc(userRecord.uid).set({
    userId: userRecord.uid,
    name,
    email,
    collegeId,
    systemRole: "studentGuide",
    customRoleIds: [],
    effectivePermissions: emptyPermissions(),
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    passwordResetLink: resetLink,
  }, { merge: true });

  await db.collection("colleges").doc(collegeId).set({
    studentGuideUid: userRecord.uid,
    studentGuideName: name,
    studentGuideEmail: email,
    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
  }, { merge: true });

  return { uid: userRecord.uid };
});

export const validateCollegeCode = onCall(async (request) => {
  const { code } = request.data || {};
  if (!code || typeof code !== "string") {
    throw new HttpsError("invalid-argument", "code is required.");
  }
  const normalized = code.toUpperCase();
  const snap = await db.collection("colleges")
    .where("collegeCode", "==", normalized)
    .limit(1)
    .get();
  return { available: snap.empty };
});

export const generateAnonAlias = onCall(async (request) => {
  if (!request.auth) throw new HttpsError("unauthenticated", "Authentication required.");
  const uid = request.auth.uid;
  const collegeId = request.data?.collegeId as string | undefined;
  if (!collegeId) throw new HttpsError("invalid-argument", "collegeId is required.");
  const alias = await getOrCreateAlias(uid, collegeId);
  return { alias };
});

export const sendAnonMessage = onCall(async (request) => {
  if (!request.auth) throw new HttpsError("unauthenticated", "Authentication required.");
  const uid = request.auth.uid;
  const roomId = request.data?.roomId as string | undefined;
  const text = (request.data?.text as string | undefined)?.trim();

  if (!roomId || !text) throw new HttpsError("invalid-argument", "roomId and text are required.");

  const metaSnap = await rtdb.ref(`chats/${roomId}/metadata`).get();
  if (!metaSnap.exists() || metaSnap.child("type").val() !== "anonymous") {
    throw new HttpsError("failed-precondition", "Room is not anonymous.");
  }

  const hasAccessSnap = await rtdb.ref(`chatAccess/${uid}/${roomId}`).get();
  if (hasAccessSnap.val() !== true) {
    throw new HttpsError("permission-denied", "No access to this room.");
  }

  const collegeId = metaSnap.child("collegeId").val() as string;
  const alias = await getOrCreateAlias(uid, collegeId);

  const msgRef = rtdb.ref(`chats/${roomId}/messages`).push();
  await msgRef.set({
    senderId: "anon",
    displayName: alias,
    text,
    timestamp: admin.database.ServerValue.TIMESTAMP,
    deletedAt: null,
  });
  return { success: true };
});

export const onEventEnd = onSchedule("every 15 minutes", async () => {
  const now = admin.firestore.Timestamp.now();
  const eventsSnap = await db.collection("events")
    .where("endTime", "<=", now)
    .where("chatLocked", "==", false)
    .limit(200)
    .get();

  const batch = db.batch();
  const updates: Array<Promise<void>> = [];

  eventsSnap.docs.forEach((doc) => {
    batch.update(doc.ref, { chatLocked: true, updatedAt: admin.firestore.FieldValue.serverTimestamp() });
    updates.push(rtdb.ref(`chats/event_${doc.id}/metadata/readOnly`).set(true));
  });

  if (!eventsSnap.empty) await batch.commit();
  await Promise.all(updates);
});

export const computeEffectivePermissions = onDocumentWritten("users/{uid}", async (event) => {
  const before = event.data?.before?.data();
  const after = event.data?.after?.data();
  if (!after) return;

  const beforeIds = JSON.stringify(before?.customRoleIds ?? []);
  const afterIds = JSON.stringify(after.customRoleIds ?? []);
  if (beforeIds === afterIds) return;

  const collegeId = after.collegeId as string | undefined;
  const roleIds = (after.customRoleIds as string[] | undefined) ?? [];
  if (!collegeId) return;

  const effective = emptyPermissions();
  for (const roleId of roleIds) {
    const roleSnap = await db.collection("colleges").doc(collegeId).collection("roles").doc(roleId).get();
    if (!roleSnap.exists) continue;
    const perms = (roleSnap.get("permissions") ?? {}) as Record<string, boolean>;
    for (const key of Object.keys(perms)) {
      if (perms[key] === true) effective[key] = true;
    }
  }

  await event.data?.after?.ref.set({
    effectivePermissions: effective,
    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
  }, { merge: true });
});

export const onCollegeCreated = onDocumentWritten("colleges/{collegeId}", async (event) => {
  if (!event.data?.after.exists || event.data?.before.exists) return;
  const collegeId = event.params.collegeId;

  await rtdb.ref(`chats/anon_${collegeId}/metadata`).set({
    type: "anonymous",
    collegeId,
    relatedId: `anon_${collegeId}`,
    readOnly: false,
    createdAt: Date.now(),
  });

  const usersSnap = await db.collection("users").where("collegeId", "==", collegeId).limit(500).get();
  const writes: Array<Promise<void>> = [];
  usersSnap.docs.forEach((doc) => {
    writes.push(rtdb.ref(`chatAccess/${doc.id}/anon_${collegeId}`).set(true));
  });
  await Promise.all(writes);
});

export const onUserCollegeAssigned = onDocumentWritten("users/{uid}", async (event) => {
  const beforeCollegeId = event.data?.before?.get("collegeId") as string | undefined;
  const afterCollegeId = event.data?.after?.get("collegeId") as string | undefined;
  const uid = event.params.uid;

  if (!afterCollegeId || beforeCollegeId === afterCollegeId) return;
  await rtdb.ref(`chatAccess/${uid}/anon_${afterCollegeId}`).set(true);
});
