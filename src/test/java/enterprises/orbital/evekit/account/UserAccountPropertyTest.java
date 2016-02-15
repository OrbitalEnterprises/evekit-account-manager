package enterprises.orbital.evekit.account;

import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import enterprises.orbital.base.NoPersistentPropertyException;
import enterprises.orbital.base.PersistentProperty;
import enterprises.orbital.evekit.TestBase;

public class UserAccountPropertyTest extends TestBase {

  protected EveKitUserAccount testUserAccount;

  @Override
  @Before
  public void setUp() throws IOException {
    super.setUp();
    testUserAccount = EveKitUserAccount.createNewUserAccount(true, true);
  }

  @Override
  @After
  public void tearDown() throws IOException {
    super.tearDown();
  }

  @Test(expected = NoPersistentPropertyException.class)
  public void testGetMissingProperty() throws IOException, NoPersistentPropertyException {
    PersistentProperty.setProperty(testUserAccount, "foo", "test");
    PersistentProperty.getProperty(testUserAccount, "bar");
  }

  @Test
  public void testGetExistingProperty() throws IOException, NoPersistentPropertyException {
    PersistentProperty.setProperty(testUserAccount, "foo", "test");
    PersistentProperty.setProperty(testUserAccount, "bar", "test-value");
    Assert.assertEquals("test-value", PersistentProperty.getProperty(testUserAccount, "bar"));
  }

  @Test
  public void testSetNewProperty() throws IOException, NoPersistentPropertyException {
    PersistentProperty.setProperty(testUserAccount, "foo", "test-value");
    Assert.assertEquals("test-value", PersistentProperty.getProperty(testUserAccount, "foo"));
  }

  @Test
  public void testSetExistingProperty() throws IOException, NoPersistentPropertyException {
    PersistentProperty.setProperty(testUserAccount, "foo", "test");
    PersistentProperty.setProperty(testUserAccount, "bar", "unchanged-value");
    PersistentProperty.setProperty(testUserAccount, "foo", "test-value");
    Assert.assertEquals("test-value", PersistentProperty.getProperty(testUserAccount, "foo"));
    Assert.assertEquals("unchanged-value", PersistentProperty.getProperty(testUserAccount, "bar"));
  }

  @Test
  public void testRemoveMissingProperty() throws IOException, NoPersistentPropertyException {
    PersistentProperty.setProperty(testUserAccount, "foo", "test");
    PersistentProperty.removeProperty(testUserAccount, "bar");
    Assert.assertEquals("test", PersistentProperty.getProperty(testUserAccount, "foo"));
  }

  @Test
  public void testRemoveExistingProperty() throws IOException, NoPersistentPropertyException {
    PersistentProperty.setProperty(testUserAccount, "foo", "test");
    PersistentProperty.setProperty(testUserAccount, "bar", "unchanged-value");
    PersistentProperty.removeProperty(testUserAccount, "bar");
    try {
      PersistentProperty.getProperty(testUserAccount, "bar");
      Assert.fail("Should through NoPersistentPropertyException here");
    } catch (NoPersistentPropertyException e) {
      // expected
    }
    Assert.assertEquals("test", PersistentProperty.getProperty(testUserAccount, "foo"));
  }

}
