package enterprises.orbital.evekit.account;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import enterprises.orbital.evekit.TestBase;

public class UserAccountTest extends TestBase {

  @Test
  public void testGetOrCreateFirstTime() throws IOException {
    EveKitUserAccount second;
    String screenName = "evekit2";
    String details = "this is a test";
    second = EveKitUserAccount.createNewUserAccount(true, true);
    EveKitUserAuthSource.createSource(second, "google", screenName, details);
    Assert.assertTrue(second.isAdmin());
    Assert.assertTrue(second.isActive());
    Assert.assertNotNull(second.getCreated());
    Assert.assertNotNull(second.getLast());
    Assert.assertNotNull(second.getUid());
    EveKitUserAuthSource source = EveKitUserAuthSource.getBySourceScreenname("google", screenName);
    Assert.assertEquals(second, source.getOwner());
    Assert.assertEquals("google", source.getSource());
    Assert.assertEquals(screenName, source.getScreenName());
    Assert.assertEquals(details, source.getDetails());
  }

  @Test
  public void testGetOrCreateExisting() throws IOException, UserNotFoundException {
    EveKitUserAccount original = EveKitUserAccount.createNewUserAccount(true, true);
    EveKitUserAccount out = EveKitUserAccount.getAccount(Long.valueOf(original.getUid()));
    Assert.assertEquals(original, out);
  }

}
