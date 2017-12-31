package enterprises.orbital.evekit.model;

import enterprises.orbital.base.OrbitalProperties;
import enterprises.orbital.evekit.TestBase;
import enterprises.orbital.evekit.account.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ESIRefEndpointSyncTrackerTest extends TestBase {

  @Override
  @Before
  public void setUp() throws IOException {
    super.setUp();
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
                                                  .createQuery("DELETE FROM ESIRefEndpointSyncTracker")
                                                  .executeUpdate();
                           });
    } catch (ExecutionException e) {
      throw new IOException(e);
    }
    super.tearDown();
  }

  protected ESIRefEndpointSyncTracker createUnfinishedTracker() {
    ESIRefEndpointSyncTracker temp = new ESIRefEndpointSyncTracker();
    temp.endpoint = ESIRefSyncEndpoint.REF_SERVER_STATUS;
    temp.syncStart = OrbitalProperties.getCurrentTime();
    temp.scheduled = temp.syncStart;
    return temp;
  }


  @Test
  public void testCreateNewUnfinishedTracker() throws IOException, ExecutionException {
    ESIRefEndpointSyncTracker existing, result;

    // Populate an existing finished tracker. Creator should NOT return this tracker.
    existing = EveKitRefDataProvider.getFactory()
                                        .runTransaction(() -> {
                                          ESIRefEndpointSyncTracker temp = createUnfinishedTracker();
                                          temp.setSyncEnd(temp.getSyncStart() + TimeUnit.MINUTES.toMillis(1));
                                          return EveKitRefDataProvider.getFactory()
                                                                          .getEntityManager()
                                                                          .merge(temp);
                                        });

    // Get the tracker and check.
    result = ESIRefEndpointSyncTracker.getOrCreateUnfinishedTracker(ESIRefSyncEndpoint.REF_SERVER_STATUS, 1234L);
    Assert.assertNotSame(existing, result);
    Assert.assertEquals(ESIRefSyncEndpoint.REF_SERVER_STATUS, result.getEndpoint());
    Assert.assertEquals(1234L, result.getScheduled());
    Assert.assertEquals(-1L, result.getSyncStart());
    Assert.assertEquals(-1L, result.getSyncEnd());
  }

  @Test
  public void testCreateGetExistingUnfinishedTracker() throws IOException, ExecutionException {
    ESIRefEndpointSyncTracker existing, result;

    // Populate an existing unfinished tracker. Creator should return this tracker.
    existing = EveKitRefDataProvider.getFactory()
                                        .runTransaction(() -> {
                                          ESIRefEndpointSyncTracker temp = createUnfinishedTracker();
                                          temp.scheduled = 1234L;
                                          return EveKitRefDataProvider.getFactory()
                                                                          .getEntityManager()
                                                                          .merge(temp);
                                        });

    // Get the tracker and check.
    result = ESIRefEndpointSyncTracker.getOrCreateUnfinishedTracker(ESIRefSyncEndpoint.REF_SERVER_STATUS, 1234L);
    Assert.assertEquals(existing, result);
  }

  @Test
  public void testGetExistingTracker() throws IOException, ExecutionException, TrackerNotFoundException {
    ESIRefEndpointSyncTracker existing, result;

    // Populate an existing unfinished tracker. Getter should find this one.
    existing = EveKitRefDataProvider.getFactory()
                                        .runTransaction(() -> {
                                          ESIRefEndpointSyncTracker temp = createUnfinishedTracker();
                                          temp.scheduled = temp.syncStart;
                                          return EveKitRefDataProvider.getFactory()
                                                                          .getEntityManager()
                                                                          .merge(temp);
                                        });

    // Get the tracker and check.
    result = ESIRefEndpointSyncTracker.getUnfinishedTracker(ESIRefSyncEndpoint.REF_SERVER_STATUS);
    Assert.assertEquals(existing, result);
  }

  @Test(expected = TrackerNotFoundException.class)
  public void testGetMissingTracker() throws IOException, ExecutionException, TrackerNotFoundException {
    // Populate an existing finished tracker. Getter should NOT find this one!
    EveKitRefDataProvider.getFactory()
                             .runTransaction(() -> {
                               ESIRefEndpointSyncTracker temp = createUnfinishedTracker();
                               temp.setSyncEnd(temp.getSyncStart() + TimeUnit.MINUTES.toMillis(1));
                               return EveKitRefDataProvider.getFactory()
                                                               .getEntityManager()
                                                               .merge(temp);
                             });

    // Get the tracker, should throw exception.
    ESIRefEndpointSyncTracker.getUnfinishedTracker(ESIRefSyncEndpoint.REF_SERVER_STATUS);
  }

  @Test
  public void testUpdateTracker() throws IOException, ExecutionException, TrackerNotFoundException {
    ESIRefEndpointSyncTracker existing;

    // Populate an existing unfinished tracker.
    existing = EveKitRefDataProvider.getFactory().runTransaction(() -> EveKitRefDataProvider.getFactory().getEntityManager().merge(createUnfinishedTracker())
      );

    // Update the tracker.
    existing.setStatus(ESISyncState.FINISHED);
    existing.setDetail("test detail");
    existing = EveKitRefDataProvider.update(existing);

    // Verify update
    ESIRefEndpointSyncTracker result = ESIRefEndpointSyncTracker.getUnfinishedTracker(ESIRefSyncEndpoint.REF_SERVER_STATUS);
    Assert.assertEquals(existing, result);
  }

  @Test(expected = TrackerNotFoundException.class)
  public void testFinishTracker() throws IOException, ExecutionException, TrackerNotFoundException {
    ESIRefEndpointSyncTracker existing;

    // Populate an existing unfinished tracker.
    existing = EveKitRefDataProvider.getFactory().runTransaction(() -> EveKitRefDataProvider.getFactory().getEntityManager().merge(createUnfinishedTracker()));

    // Get the tracker and check.
    ESIRefEndpointSyncTracker.finishTracker(existing);

    // Verify no unfinished trackers
    ESIRefEndpointSyncTracker.getUnfinishedTracker(ESIRefSyncEndpoint.REF_SERVER_STATUS);
  }
}
