package enterprises.orbital.evekit.model;

import enterprises.orbital.base.OrbitalProperties;
import enterprises.orbital.evekit.TestBase;
import enterprises.orbital.evekit.account.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ESIEndpointSyncTrackerTest extends TestBase {

  public EveKitUserAccount userAccount;
  public SynchronizedEveAccount testAccount;

  @Override
  @Before
  public void setUp() throws IOException {
    super.setUp();
    userAccount = EveKitUserAccount.createNewUserAccount(true, true);
    try {
      testAccount = SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount", true);
    } catch (AccountCreationException e) {
      throw new IOException(e);
    }
  }

  @Override
  @After
  public void tearDown() throws IOException {
    // Remove all trackers after each test
    try {
      EveKitRefDataProvider.getFactory()
                           .runTransaction(() -> {
                             EveKitRefDataProvider.getFactory()
                                                  .getEntityManager()
                                                  .createQuery("DELETE FROM ESIEndpointSyncTracker")
                                                  .executeUpdate();
                           });
    } catch (ExecutionException e) {
      throw new IOException(e);
    }
    super.tearDown();
  }

  protected ESIEndpointSyncTracker createUnfinishedTracker() {
    ESIEndpointSyncTracker temp = new ESIEndpointSyncTracker();
    temp.account = testAccount;
    temp.endpoint = ESISyncEndpoint.CHAR_BLUEPRINTS;
    temp.syncStart = OrbitalProperties.getCurrentTime();
    temp.scheduled = temp.syncStart;
    return temp;
  }


  @Test
  public void testCreateNewUnfinishedTracker() throws IOException, ExecutionException {
    ESIEndpointSyncTracker existing, result;

    // Populate an existing finished tracker. Creator should NOT return this tracker.
    existing = EveKitUserAccountProvider.getFactory()
                                        .runTransaction(() -> {
                                          ESIEndpointSyncTracker temp = createUnfinishedTracker();
                                          temp.setSyncEnd(temp.getSyncStart() + TimeUnit.MINUTES.toMillis(1));
                                          return EveKitUserAccountProvider.getFactory()
                                                                          .getEntityManager()
                                                                          .merge(temp);
                                        });

    // Get the tracker and check.
    result = ESIEndpointSyncTracker.getOrCreateUnfinishedTracker(testAccount, ESISyncEndpoint.CHAR_BLUEPRINTS, 1234L, null);
    Assert.assertNotSame(existing, result);
    Assert.assertEquals(testAccount, result.getAccount());
    Assert.assertEquals(ESISyncEndpoint.CHAR_BLUEPRINTS, result.getEndpoint());
    Assert.assertEquals(1234L, result.getScheduled());
    Assert.assertEquals(-1L, result.getSyncStart());
    Assert.assertEquals(-1L, result.getSyncEnd());
  }

  @Test
  public void testCreateGetExistingUnfinishedTracker() throws IOException, ExecutionException {
    ESIEndpointSyncTracker existing, result;

    // Populate an existing unfinished tracker. Creator should return this tracker.
    existing = EveKitUserAccountProvider.getFactory()
                                        .runTransaction(() -> {
                                          ESIEndpointSyncTracker temp = createUnfinishedTracker();
                                          temp.scheduled = 1234L;
                                          return EveKitUserAccountProvider.getFactory()
                                                                          .getEntityManager()
                                                                          .merge(temp);
                                        });

    // Get the tracker and check.
    result = ESIEndpointSyncTracker.getOrCreateUnfinishedTracker(testAccount, ESISyncEndpoint.CHAR_BLUEPRINTS, 1234L, null);
    Assert.assertEquals(existing, result);
  }

  @Test
  public void testGetExistingTracker() throws IOException, ExecutionException, TrackerNotFoundException {
    ESIEndpointSyncTracker existing, result;

    // Populate an existing unfinished tracker. Getter should find this one.
    existing = EveKitUserAccountProvider.getFactory()
                                        .runTransaction(() -> {
                                          ESIEndpointSyncTracker temp = createUnfinishedTracker();
                                          temp.scheduled = temp.syncStart;
                                          return EveKitUserAccountProvider.getFactory()
                                                                          .getEntityManager()
                                                                          .merge(temp);
                                        });

    // Get the tracker and check.
    result = ESIEndpointSyncTracker.getUnfinishedTracker(testAccount, ESISyncEndpoint.CHAR_BLUEPRINTS);
    Assert.assertEquals(existing, result);
  }

  @Test(expected = TrackerNotFoundException.class)
  public void testGetMissingTracker() throws IOException, ExecutionException, TrackerNotFoundException {
    // Populate an existing finished tracker. Getter should NOT find this one!
    EveKitUserAccountProvider.getFactory()
                             .runTransaction(() -> {
                               ESIEndpointSyncTracker temp = createUnfinishedTracker();
                               temp.setSyncEnd(temp.getSyncStart() + TimeUnit.MINUTES.toMillis(1));
                               return EveKitUserAccountProvider.getFactory()
                                                               .getEntityManager()
                                                               .merge(temp);
                             });

    // Get the tracker, should throw exception.
    ESIEndpointSyncTracker.getUnfinishedTracker(testAccount, ESISyncEndpoint.CHAR_BLUEPRINTS);
  }

  @Test
  public void testUpdateTracker() throws IOException, ExecutionException, TrackerNotFoundException {
    ESIEndpointSyncTracker existing;

    // Populate an existing unfinished tracker.
    existing = EveKitUserAccountProvider.getFactory().runTransaction(() -> EveKitUserAccountProvider.getFactory().getEntityManager().merge(createUnfinishedTracker())
      );

    // Update the tracker.
    existing.setStatus(ESISyncState.FINISHED);
    existing.setDetail("test detail");
    existing = EveKitUserAccountProvider.update(existing);

    // Verify update
    ESIEndpointSyncTracker result = ESIEndpointSyncTracker.getUnfinishedTracker(testAccount, ESISyncEndpoint.CHAR_BLUEPRINTS);
    Assert.assertEquals(existing, result);
  }

  @Test(expected = TrackerNotFoundException.class)
  public void testFinishTracker() throws IOException, ExecutionException, TrackerNotFoundException {
    ESIEndpointSyncTracker existing;

    // Populate an existing unfinished tracker.
    existing = EveKitUserAccountProvider.getFactory().runTransaction(() -> EveKitUserAccountProvider.getFactory().getEntityManager().merge(createUnfinishedTracker()));

    // Get the tracker and check.
    ESIEndpointSyncTracker.finishTracker(existing);

    // Verify no unfinished trackers
    ESIEndpointSyncTracker.getUnfinishedTracker(testAccount, ESISyncEndpoint.CHAR_BLUEPRINTS);
  }
}
