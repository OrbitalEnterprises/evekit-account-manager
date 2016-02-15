package enterprises.orbital.evekit.account;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.persistence.TypedQuery;

import org.junit.Assert;
import org.junit.Test;

import enterprises.orbital.base.OrbitalProperties;
import enterprises.orbital.db.ConnectionFactory.RunInTransaction;
import enterprises.orbital.evekit.TestBase;
import enterprises.orbital.evekit.model.CapsuleerSyncTracker;
import enterprises.orbital.evekit.model.CorporationSyncTracker;

public class SynchronizedEveAccountTest extends TestBase {

  @Test
  public void testCreateAccount() throws AccountCreationException {
    EveKitUserAccount userAccount = EveKitUserAccount.createNewUserAccount(true, true);
    SynchronizedEveAccount out = SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount", true, true, 1234, "abcd", 5678, "charname",
                                                                                     8765, "corpname");
    Assert.assertEquals(userAccount, out.getUserAccount());
    Assert.assertEquals("testaccount", out.getName());
    Assert.assertTrue(out.isCharacterType());
    Assert.assertTrue(out.isAutoSynchronized());
    Assert.assertEquals(1234, out.getEveKey());
    Assert.assertEquals("abcd", out.getEveVCode());
    Assert.assertEquals(5678, out.getEveCharacterID());
    Assert.assertEquals("charname", out.getEveCharacterName());
    Assert.assertEquals(8765, out.getEveCorporationID());
    Assert.assertEquals("corpname", out.getEveCorporationName());
  }

  @Test(expected = AccountCreationException.class)
  public void testCreateAccountExists() throws AccountCreationException {
    EveKitUserAccount userAccount = EveKitUserAccount.createNewUserAccount(true, true);
    SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount", true, true, 1234, "abcd", 5678, "charname", 8765, "corpname");
    SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount", true, true, 1234, "abcd", 5678, "charname", 8765, "corpname");
  }

  @Test
  public void testGetExistingAccount() throws IOException {
    EveKitUserAccount userAccount = EveKitUserAccount.createNewUserAccount(true, true);
    SynchronizedEveAccount existing = null, out;
    String name = null;
    try {
      existing = SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount", true, true, 1234, "abcd", 5678, "charname", 8765, "corpname");
      name = existing.getName();
    } catch (Exception e) {
      Assert.fail("Unexpected exception");
    }

    // This should retrieve the cached version
    out = SynchronizedEveAccount.getSynchronizedAccount(userAccount, name, false);
    Assert.assertNotNull(out);
    Assert.assertEquals(existing, out);
  }

  @Test
  public void testGetMissingAccount() throws IOException {
    EveKitUserAccount userAccount = EveKitUserAccount.createNewUserAccount(true, true);
    EveKitUserAccount userAccount2 = EveKitUserAccount.createNewUserAccount(true, true);
    try {
      SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount", true, true, 1234, "abcd", 5678, "charname", 8765, "corpname");
    } catch (Exception e) {
      Assert.assertTrue(false);
    }

    Assert.assertNull(SynchronizedEveAccount.getSynchronizedAccount(userAccount, "12345", false));
    Assert.assertNull(SynchronizedEveAccount.getSynchronizedAccount(userAccount2, "6789", false));
  }

  @Test
  public void testGetAllAccounts() throws IOException, AccountCreationException {
    EveKitUserAccount userAccount = EveKitUserAccount.createNewUserAccount(true, true);
    SynchronizedEveAccount out1 = SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount1", true, true, 1234, "abcd", 5678, "charname",
                                                                                      8765, "corpname");
    SynchronizedEveAccount out2 = SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount2", true, true, 4321, "abcd", 8765, "charname1",
                                                                                      5678, "corpname2");
    Assert.assertNotNull(out1);
    Assert.assertNotNull(out2);
    List<SynchronizedEveAccount> allAccounts = SynchronizedEveAccount.getAllAccounts(userAccount, false);

    org.junit.Assert.assertThat(allAccounts, org.hamcrest.CoreMatchers.hasItems(out1, out2));
  }

  @Test
  public void testRestoreAccount() throws IOException, AccountCreationException {
    EveKitUserAccount userAccount = EveKitUserAccount.createNewUserAccount(true, true);
    long id = SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount1", true, true, 1234, "abcd", 5678, "charname", 8765, "corpname")
        .getAid();
    SynchronizedEveAccount.deleteAccount(userAccount, id);
    Assert.assertNull(SynchronizedEveAccount.getSynchronizedAccount(userAccount, id, false));
    SynchronizedEveAccount.restoreAccount(userAccount, id);
    Assert.assertNotNull(SynchronizedEveAccount.getSynchronizedAccount(userAccount, id, false));
  }

  @Test
  public void testRestoreAccountMissing() throws IOException, AccountCreationException {
    EveKitUserAccount userAccount = EveKitUserAccount.createNewUserAccount(true, true);
    long id = SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount1", true, true, 1234, "abcd", 5678, "charname", 8765, "corpname")
        .getAid();
    SynchronizedEveAccount.restoreAccount(userAccount, 12345L);
    Assert.assertNotNull(SynchronizedEveAccount.getSynchronizedAccount(userAccount, id, false));
  }

  @Test
  public void testDeleteAccount() throws IOException, AccountCreationException {
    EveKitUserAccount userAccount = EveKitUserAccount.createNewUserAccount(true, true);
    long id = SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount1", true, true, 1234, "abcd", 5678, "charname", 8765, "corpname")
        .getAid();
    SynchronizedEveAccount.deleteAccount(userAccount, id);
    Assert.assertNull(SynchronizedEveAccount.getSynchronizedAccount(userAccount, id, false));
    Assert.assertNotNull(SynchronizedEveAccount.getSynchronizedAccount(userAccount, id, true));
  }

  @Test
  public void testDeleteAccountMissing() throws IOException, AccountCreationException {
    EveKitUserAccount userAccount = EveKitUserAccount.createNewUserAccount(true, true);
    SynchronizedEveAccount tst = SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount1", true, true, 1234, "abcd", 5678, "charname",
                                                                                     8765, "corpname");
    SynchronizedEveAccount.deleteAccount(userAccount, 12345L);
    Assert.assertNotNull(SynchronizedEveAccount.getSynchronizedAccount(userAccount, tst.getName(), false));
  }

  @Test
  public void testUpdateAccount() throws IOException, AccountCreationException {
    EveKitUserAccount userAccount = EveKitUserAccount.createNewUserAccount(true, true);
    SynchronizedEveAccount tst = SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount", true, true, 1234, "abcd", 5678, "charname",
                                                                                     8765, "corpname");
    SynchronizedEveAccount.updateAccount(userAccount, tst.getAid(), "testaccount", false, false, 5678, "dbca", 8765, "newcharname", 5678, "newcorpname");
    SynchronizedEveAccount out = SynchronizedEveAccount.getSynchronizedAccount(userAccount, tst.getName(), false);

    Assert.assertEquals(userAccount, out.getUserAccount());
    Assert.assertEquals("testaccount", out.getName());
    Assert.assertEquals(tst.getName(), out.getName());
    Assert.assertFalse(out.isCharacterType());
    Assert.assertFalse(out.isAutoSynchronized());
    Assert.assertEquals(5678, out.getEveKey());
    Assert.assertEquals("dbca", out.getEveVCode());
    Assert.assertEquals(8765, out.getEveCharacterID());
    Assert.assertEquals("newcharname", out.getEveCharacterName());
    Assert.assertEquals(5678, out.getEveCorporationID());
    Assert.assertEquals("newcorpname", out.getEveCorporationName());
  }

  @Test
  public void testUpdateAccountMissing() throws IOException, AccountCreationException {
    EveKitUserAccount userAccount = EveKitUserAccount.createNewUserAccount(true, true);
    SynchronizedEveAccount tst = SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount", true, true, 1234, "abcd", 5678, "charname",
                                                                                     8765, "corpname");
    try {
      SynchronizedEveAccount.updateAccount(userAccount, 12345L, "testaccount", false, false, 5678, "dbca", 8765, "newcharname", 5678, "newcorpname");
    } catch (AccountCreationException e) {
      // expected.
    }
    SynchronizedEveAccount out = SynchronizedEveAccount.getSynchronizedAccount(userAccount, tst.getName(), false);

    Assert.assertEquals(userAccount, out.getUserAccount());
    Assert.assertEquals(tst.getName(), out.getName());
    Assert.assertTrue(out.isCharacterType());
    Assert.assertTrue(out.isAutoSynchronized());
    Assert.assertEquals(1234, out.getEveKey());
    Assert.assertEquals("abcd", out.getEveVCode());
    Assert.assertEquals(5678, out.getEveCharacterID());
    Assert.assertEquals("charname", out.getEveCharacterName());
    Assert.assertEquals(8765, out.getEveCorporationID());
    Assert.assertEquals("corpname", out.getEveCorporationName());
  }

  @Test
  public void testRemoveAccount() throws IOException, AccountCreationException, AccessKeyCreationException, ExecutionException {
    if (Boolean.valueOf(System.getProperty("enterprises.orbtial.evekit.model.unittest.skipbig", "false"))) { return; }

    // Setup account
    EveKitUserAccount userAccount = EveKitUserAccount.createNewUserAccount(true, true);
    SynchronizedEveAccount testAccount = SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount", true, true, 1234, "abcd", 5678,
                                                                                             "charname", 8765, "corpname");

    // Create items typically attached to a sync account: access keys and sync trackers
    int count = TestBase.getRandomInt(2000) + 2000;
    for (int i = 0; i < count; i++) {
      CorporationSyncTracker next = CorporationSyncTracker.createOrGetUnfinishedTracker(testAccount);
      CorporationSyncTracker.finishTracker(next);
    }
    System.out.println("Created CorporationSyncTrackers");

    count = TestBase.getRandomInt(2000) + 2000;
    for (int i = 0; i < count; i++) {
      CapsuleerSyncTracker next = CapsuleerSyncTracker.createOrGetUnfinishedTracker(testAccount);
      CapsuleerSyncTracker.finishTracker(next);
    }
    System.out.println("Created CapsuleerSyncTrackers");

    count = TestBase.getRandomInt(50) + 10;
    for (int i = 0; i < count; i++) {
      SynchronizedAccountAccessKey.createKey(testAccount, TestBase.getRandomText(30), OrbitalProperties.getCurrentTime(), OrbitalProperties.getCurrentTime(),
                                             new byte[] {
                                                 1
      });
    }
    System.out.println("Created AccessKeys");

    // Remove and verify proper cleanup
    SynchronizedEveAccount.remove(testAccount);

    // Account and anything associated with it should be removed
    Assert.assertNull(SynchronizedEveAccount.getSynchronizedAccount(testAccount.getUserAccount(), testAccount.getAid(), true));
    final SynchronizedEveAccount check = testAccount;
    long remaining = EveKitUserAccountProvider.getFactory().runTransaction(new RunInTransaction<Long>() {
      @Override
      public Long run() throws Exception {
        TypedQuery<Long> query = EveKitUserAccountProvider.getFactory().getEntityManager()
            .createQuery("SELECT count(c) FROM SyncTracker c where c.account= :acct", Long.class);
        query.setParameter("acct", check);
        return query.getSingleResult();
      }
    });
    Assert.assertEquals(0, remaining);
    remaining = EveKitUserAccountProvider.getFactory().runTransaction(new RunInTransaction<Long>() {
      @Override
      public Long run() throws Exception {
        TypedQuery<Long> query = EveKitUserAccountProvider.getFactory().getEntityManager()
            .createQuery("SELECT count(c) FROM SynchronizedAccountAccessKey c where c.account= :acct", Long.class);
        query.setParameter("acct", check);
        return query.getSingleResult();
      }
    });
    Assert.assertEquals(0, remaining);
  }

}
