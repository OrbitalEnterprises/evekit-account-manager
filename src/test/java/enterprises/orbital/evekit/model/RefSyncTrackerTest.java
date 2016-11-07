package enterprises.orbital.evekit.model;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import enterprises.orbital.base.OrbitalProperties;
import enterprises.orbital.db.ConnectionFactory.RunInTransaction;
import enterprises.orbital.db.ConnectionFactory.RunInVoidTransaction;
import enterprises.orbital.evekit.TestBase;
import enterprises.orbital.evekit.account.EveKitRefDataProvider;
import enterprises.orbital.evekit.model.SyncTracker.SyncState;

public class RefSyncTrackerTest extends TestBase {

  @Override
  @After
  public void tearDown() throws IOException {
    // Remove all trackers after each test
    try {
      EveKitRefDataProvider.getFactory().runTransaction(new RunInVoidTransaction() {
        @Override
        public void run() throws Exception {
          EveKitRefDataProvider.getFactory().getEntityManager().createQuery("DELETE FROM RefSyncTracker").executeUpdate();
        }
      });
    } catch (ExecutionException e) {
      throw new IOException(e);
    }
    super.tearDown();
  }

  @Test
  public void testCreateNewUnfinishedTracker() throws IOException, ExecutionException {
    RefSyncTracker existing, result;

    // Populate an existing finished tracker. Creator should NOT return this tracker.
    existing = EveKitRefDataProvider.getFactory().runTransaction(new RunInTransaction<RefSyncTracker>() {
      @Override
      public RefSyncTracker run() throws Exception {
        RefSyncTracker result = new RefSyncTracker();
        result.syncStart = OrbitalProperties.getCurrentTime();
        result.setSyncEnd(result.getSyncStart() + TimeUnit.MINUTES.toMillis(1));
        result.setFinished(true);
        return EveKitRefDataProvider.getFactory().getEntityManager().merge(result);
      }
    });

    // Get the tracker and check.
    result = RefSyncTracker.createOrGetUnfinishedTracker();
    Assert.assertNotSame(existing, result);
    Assert.assertFalse(result.getFinished());
    Assert.assertNotNull(result.getSyncStart());
    Assert.assertEquals(-1, result.getSyncEnd());
  }

  @Test
  public void testCreateGetExistingUnfinishedTracker() throws IOException, ExecutionException {
    RefSyncTracker existing, result;

    // Populate an existing unfinished tracker. Creator should return this tracker.
    existing = EveKitRefDataProvider.getFactory().runTransaction(new RunInTransaction<RefSyncTracker>() {
      @Override
      public RefSyncTracker run() throws Exception {
        RefSyncTracker result = new RefSyncTracker();
        result.syncStart = OrbitalProperties.getCurrentTime();
        result.setFinished(false);
        return EveKitRefDataProvider.getFactory().getEntityManager().merge(result);
      }
    });

    // Get the tracker and check.
    result = RefSyncTracker.createOrGetUnfinishedTracker();
    Assert.assertEquals(existing, result);
  }

  @Test
  public void testGetExistingTracker() throws IOException, ExecutionException {
    RefSyncTracker existing, result;

    // Populate an existing unfinished tracker. Getter should find this one.
    existing = EveKitRefDataProvider.getFactory().runTransaction(new RunInTransaction<RefSyncTracker>() {
      @Override
      public RefSyncTracker run() throws Exception {
        RefSyncTracker result = new RefSyncTracker();
        result.syncStart = OrbitalProperties.getCurrentTime();
        result.setFinished(false);
        return EveKitRefDataProvider.getFactory().getEntityManager().merge(result);
      }
    });

    // Get the tracker and check.
    result = RefSyncTracker.getUnfinishedTracker();
    Assert.assertEquals(existing, result);
  }

  @Test
  public void testGetMissingTracker() throws IOException, ExecutionException {
    RefSyncTracker result;

    // Populate an existing finished tracker. Getter should NOT find this one!
    EveKitRefDataProvider.getFactory().runTransaction(new RunInTransaction<RefSyncTracker>() {
      @Override
      public RefSyncTracker run() throws Exception {
        RefSyncTracker result = new RefSyncTracker();
        result.syncStart = OrbitalProperties.getCurrentTime();
        result.setSyncEnd(result.getSyncStart() + TimeUnit.MINUTES.toMillis(1));
        result.setFinished(true);
        return EveKitRefDataProvider.getFactory().getEntityManager().merge(result);
      }
    });

    // Get the tracker and check.
    result = RefSyncTracker.getUnfinishedTracker();
    Assert.assertNull(result);
  }

  @Test
  public void testUpdateTracker() throws IOException, ExecutionException {
    RefSyncTracker existing;

    // Populate an existing unfinished tracker.
    existing = EveKitRefDataProvider.getFactory().runTransaction(new RunInTransaction<RefSyncTracker>() {
      @Override
      public RefSyncTracker run() throws Exception {
        RefSyncTracker result = new RefSyncTracker();
        result.syncStart = OrbitalProperties.getCurrentTime();
        result.setFinished(false);
        return EveKitRefDataProvider.getFactory().getEntityManager().merge(result);
      }
    });

    // Update the tracker.
    existing.setServerStatusStatus(SyncState.UPDATED);
    existing.setServerStatusDetail("test detail");
    existing = RefSyncTracker.updateTracker(existing);

    // If joined, the cache should be empty but a get should retrieve the proper value.
    RefSyncTracker result = RefSyncTracker.getUnfinishedTracker();
    Assert.assertEquals(existing, result);
  }

  @Test
  public void testFinishTracker() throws IOException, ExecutionException {
    RefSyncTracker existing;

    // Populate an existing unfinished tracker.
    existing = EveKitRefDataProvider.getFactory().runTransaction(new RunInTransaction<RefSyncTracker>() {
      @Override
      public RefSyncTracker run() throws Exception {
        RefSyncTracker result = new RefSyncTracker();
        result.syncStart = OrbitalProperties.getCurrentTime();
        result.setFinished(false);
        return EveKitRefDataProvider.getFactory().getEntityManager().merge(result);
      }
    });

    // Get the tracker and check.
    RefSyncTracker.finishTracker(existing);

    // Verify no unfinished trackers
    List<RefSyncTracker> results = RefSyncTracker.getAllUnfinishedTrackers();
    Assert.assertEquals(0, results.size());
  }
}
