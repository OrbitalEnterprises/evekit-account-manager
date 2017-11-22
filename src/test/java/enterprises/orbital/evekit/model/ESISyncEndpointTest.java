package enterprises.orbital.evekit.model;

import enterprises.orbital.base.OrbitalProperties;
import enterprises.orbital.db.ConnectionFactory.RunInTransaction;
import enterprises.orbital.db.ConnectionFactory.RunInVoidTransaction;
import enterprises.orbital.evekit.TestBase;
import enterprises.orbital.evekit.account.EveKitRefDataProvider;
import enterprises.orbital.evekit.model.SyncTracker.SyncState;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ESISyncEndpointTest extends TestBase {

  @Test
  public void testCharEndpointsMarkedCorrectly() {
    for (ESISyncEndpoint next : ESISyncEndpoint.getCharEndpoints()) {
      Assert.assertTrue(next.isChar);
    }
  }

  @Test
  public void testCorpEndpointsMarkedCorrectly() {
    for (ESISyncEndpoint next : ESISyncEndpoint.getCorpEndpoints()) {
      Assert.assertFalse(next.isChar);
    }
  }

  @Test
  public void testAllEndpointsIncluded() {
    Set<ESISyncEndpoint> all = new HashSet<>();
    all.addAll(Arrays.asList(ESISyncEndpoint.getCharEndpoints()));
    all.addAll(Arrays.asList(ESISyncEndpoint.getCorpEndpoints()));
    for (ESISyncEndpoint next : ESISyncEndpoint.values()) {
      Assert.assertTrue(all.contains(next));
    }
  }

}
