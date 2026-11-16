const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_miner_123";
const BOB_UID = "bob_miner_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validEntry(uid, overrides = {}) {
  const now = new Date();
  return {
    userId: uid,
    displayName: "SatoshiMiner",
    totalSatsMined: 594293,
    totalBtcMined: 0.00594293,
    hashRateGh: 125.0,
    activeMiners: 3,
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// --- SECURITY BOUNDS TESTS ---

test("Unauthenticated user: cannot read or list leaderboard", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("leaderboard").doc(ALICE_UID).get());
  await assertFails(
    unauthDb
      .collection("leaderboard")
      .where("totalSatsMined", ">=", 0)
      .orderBy("totalSatsMined", "desc")
      .limit(50)
      .get()
  );
});

test("Unauthenticated user: cannot create leaderboard entry", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("leaderboard").doc(ALICE_UID).set(validEntry(ALICE_UID))
  );
});

test("Authenticated user: can create own valid leaderboard entry", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("leaderboard").doc(ALICE_UID).set(validEntry(ALICE_UID))
  );
});

test("Authenticated user: cannot create or overwrite another user's entry", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("leaderboard").doc(BOB_UID).set(validEntry(BOB_UID))
  );
  await assertFails(
    aliceDb.collection("leaderboard").doc(BOB_UID).set(validEntry(ALICE_UID))
  );
});

test("Authenticated user: cannot create entry with mismatched internal userId", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb
      .collection("leaderboard")
      .doc(ALICE_UID)
      .set(validEntry(BOB_UID))
  );
});

test("Red Team: rejects ghost fields on create and update", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb
      .collection("leaderboard")
      .doc(ALICE_UID)
      .set(validEntry(ALICE_UID, { isAdmin: true }))
  );

  await assertSucceeds(
    aliceDb.collection("leaderboard").doc(ALICE_UID).set(validEntry(ALICE_UID))
  );

  await assertFails(
    aliceDb.collection("leaderboard").doc(ALICE_UID).update({
      totalSatsMined: 999999,
      isAdmin: true,
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: rejects poisoned values and immutable field tampering", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  // Negative sats mined
  await assertFails(
    aliceDb
      .collection("leaderboard")
      .doc(ALICE_UID)
      .set(validEntry(ALICE_UID, { totalSatsMined: -10 }))
  );

  // Empty displayName
  await assertFails(
    aliceDb
      .collection("leaderboard")
      .doc(ALICE_UID)
      .set(validEntry(ALICE_UID, { displayName: "" }))
  );

  // Valid initial create
  const createdTime = new Date(Date.now() - 60000);
  await assertSucceeds(
    aliceDb
      .collection("leaderboard")
      .doc(ALICE_UID)
      .set(validEntry(ALICE_UID, { createdAt: createdTime, updatedAt: createdTime }))
  );

  // Tampering with createdAt on update fails
  await assertFails(
    aliceDb.collection("leaderboard").doc(ALICE_UID).update({
      totalSatsMined: 700000,
      totalBtcMined: 0.007,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  // Valid score update succeeds while preserving createdAt
  await assertSucceeds(
    aliceDb.collection("leaderboard").doc(ALICE_UID).update({
      totalSatsMined: 700000,
      totalBtcMined: 0.007,
      hashRateGh: 250.0,
      activeMiners: 4,
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: cannot delete another user's entry", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx
      .firestore()
      .collection("leaderboard")
      .doc(BOB_UID)
      .set(validEntry(BOB_UID));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("leaderboard").doc(BOB_UID).delete());
});

// --- CLIENT QUERY ALIGNMENT & GLOBAL CATCH-ALL TESTS ---

test("Authenticated user: can list global leaderboard with aligned filter & order", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx
      .firestore()
      .collection("leaderboard")
      .doc(ALICE_UID)
      .set(validEntry(ALICE_UID, { totalSatsMined: 1200 }));
    await ctx
      .firestore()
      .collection("leaderboard")
      .doc(BOB_UID)
      .set(validEntry(BOB_UID, { totalSatsMined: 5000 }));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const snap = await assertSucceeds(
    aliceDb
      .collection("leaderboard")
      .where("totalSatsMined", ">=", 0)
      .orderBy("totalSatsMined", "desc")
      .limit(50)
      .get()
  );
  if (snap.size !== 2 || snap.docs[0].id !== BOB_UID) {
    throw new Error("Expected Bob ranked #1 above Alice");
  }
});

test("Global catch-all denies unknown collections even for authenticated users", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("secrets").doc("doc1").get());
  await assertFails(aliceDb.collection("secrets").doc("doc1").set({ data: "x" }));
});
