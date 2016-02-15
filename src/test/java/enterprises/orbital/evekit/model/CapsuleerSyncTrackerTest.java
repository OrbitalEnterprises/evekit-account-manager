package enterprises.orbital.evekit.model;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import enterprises.orbital.base.OrbitalProperties;
import enterprises.orbital.db.ConnectionFactory.RunInTransaction;
import enterprises.orbital.evekit.TestBase;
import enterprises.orbital.evekit.account.AccountCreationException;
import enterprises.orbital.evekit.account.EveKitUserAccount;
import enterprises.orbital.evekit.account.EveKitUserAccountProvider;
import enterprises.orbital.evekit.account.SynchronizedEveAccount;
import enterprises.orbital.evekit.model.SyncTracker.SyncState;

public class CapsuleerSyncTrackerTest extends TestBase {

  public EveKitUserAccount      userAccount;
  public SynchronizedEveAccount testAccount;

  @Override
  @Before
  public void setUp() throws IOException {
    super.setUp();
    userAccount = EveKitUserAccount.createNewUserAccount(true, true);
    try {
      testAccount = SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount", true, true, 1234, "abcd", 5678, "charname", 8765,
                                                                        "corpname");
    } catch (AccountCreationException e) {
      throw new IOException(e);
    }
  }

  @Override
  @After
  public void tearDown() throws IOException {
    super.tearDown();
  }

  @Test
  public void testCreateNewUnfinishedTracker() throws IOException, ExecutionException {
    CapsuleerSyncTracker existing, result;

    // Populate an existing finished tracker. Creator should NOT return this tracker.
    existing = EveKitUserAccountProvider.getFactory().runTransaction(new RunInTransaction<CapsuleerSyncTracker>() {
      @Override
      public CapsuleerSyncTracker run() throws Exception {
        CapsuleerSyncTracker result = new CapsuleerSyncTracker();
        result.account = testAccount;
        result.syncStart = OrbitalProperties.getCurrentTime();
        result.setSyncEnd(result.getSyncStart() + TimeUnit.MINUTES.toMillis(1));
        result.setFinished(true);
        return EveKitUserAccountProvider.getFactory().getEntityManager().merge(result);
      }
    });

    // Get the tracker and check.
    result = CapsuleerSyncTracker.createOrGetUnfinishedTracker(testAccount);

    Assert.assertNotSame(existing, result);
    Assert.assertFalse(result.getFinished());
    Assert.assertNotNull(result.getSyncStart());
    Assert.assertEquals(-1, result.getSyncEnd());
  }

  @Test
  public void testCreateGetExistingUnfinishedTracker() throws IOException, ExecutionException {
    CapsuleerSyncTracker existing, result;

    // Populate an existing unfinished tracker. Creator should return this tracker.
    existing = EveKitUserAccountProvider.getFactory().runTransaction(new RunInTransaction<CapsuleerSyncTracker>() {
      @Override
      public CapsuleerSyncTracker run() throws Exception {
        CapsuleerSyncTracker result = new CapsuleerSyncTracker();
        result.account = testAccount;
        result.syncStart = OrbitalProperties.getCurrentTime();
        result.setFinished(false);
        return EveKitUserAccountProvider.getFactory().getEntityManager().merge(result);
      }
    });

    // Get the tracker and check.
    result = CapsuleerSyncTracker.createOrGetUnfinishedTracker(testAccount);
    Assert.assertEquals(existing, result);
  }

  @Test
  public void testGetExistingTracker() throws IOException, ExecutionException {
    CapsuleerSyncTracker existing, result;

    // Populate an existing unfinished tracker. Getter should find this one.
    existing = EveKitUserAccountProvider.getFactory().runTransaction(new RunInTransaction<CapsuleerSyncTracker>() {
      @Override
      public CapsuleerSyncTracker run() throws Exception {
        CapsuleerSyncTracker result = new CapsuleerSyncTracker();
        result.account = testAccount;
        result.syncStart = OrbitalProperties.getCurrentTime();
        result.setFinished(false);
        return EveKitUserAccountProvider.getFactory().getEntityManager().merge(result);
      }
    });

    // Get the tracker and check.
    result = CapsuleerSyncTracker.getUnfinishedTracker(testAccount);
    Assert.assertEquals(existing, result);
  }

  @Test
  public void testGetMissingTracker() throws IOException, ExecutionException {
    CapsuleerSyncTracker result;

    // Populate an existing finished tracker. Getter should NOT find this one!
    EveKitUserAccountProvider.getFactory().runTransaction(new RunInTransaction<CapsuleerSyncTracker>() {
      @Override
      public CapsuleerSyncTracker run() throws Exception {
        CapsuleerSyncTracker result = new CapsuleerSyncTracker();
        result.account = testAccount;
        result.syncStart = OrbitalProperties.getCurrentTime();
        result.setSyncEnd(result.getSyncStart() + TimeUnit.MINUTES.toMillis(1));
        result.setFinished(true);
        return EveKitUserAccountProvider.getFactory().getEntityManager().merge(result);
      }
    });

    // Get the tracker and check.
    result = CapsuleerSyncTracker.getUnfinishedTracker(testAccount);
    Assert.assertNull(result);
  }

  @Test
  public void testUpdateTracker() throws IOException, ExecutionException {
    CapsuleerSyncTracker existing;

    // Populate an existing unfinished tracker.
    existing = EveKitUserAccountProvider.getFactory().runTransaction(new RunInTransaction<CapsuleerSyncTracker>() {
      @Override
      public CapsuleerSyncTracker run() throws Exception {
        CapsuleerSyncTracker result = new CapsuleerSyncTracker();
        result.account = testAccount;
        result.syncStart = OrbitalProperties.getCurrentTime();
        result.setFinished(false);
        return EveKitUserAccountProvider.getFactory().getEntityManager().merge(result);
      }
    });

    // Update the tracker.
    existing.setAccountBalanceStatus(SyncState.UPDATED);
    existing.setAccountBalanceDetail("test detail");
    existing = SyncTracker.updateTracker(existing);

    CapsuleerSyncTracker result = CapsuleerSyncTracker.getUnfinishedTracker(testAccount);
    Assert.assertEquals(existing, result);
  }

  @Test
  public void testFinishTracker() throws IOException, ExecutionException {
    CapsuleerSyncTracker existing;

    // Populate an existing unfinished tracker.
    existing = EveKitUserAccountProvider.getFactory().runTransaction(new RunInTransaction<CapsuleerSyncTracker>() {
      @Override
      public CapsuleerSyncTracker run() throws Exception {
        CapsuleerSyncTracker result = new CapsuleerSyncTracker();
        result.account = testAccount;
        result.syncStart = OrbitalProperties.getCurrentTime();
        result.setFinished(false);
        return EveKitUserAccountProvider.getFactory().getEntityManager().merge(result);
      }
    });

    // Finish this tracker
    SyncTracker.finishTracker(existing);

    // Verify no unfinished trackers
    List<CapsuleerSyncTracker> results = CapsuleerSyncTracker.getAllUnfinishedTrackers();
    Assert.assertEquals(0, results.size());
  }
}
