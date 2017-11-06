package enterprises.orbital.evekit.account;

import enterprises.orbital.base.OrbitalProperties;
import enterprises.orbital.db.ConnectionFactory.RunInTransaction;
import enterprises.orbital.evekit.TestBase;
import enterprises.orbital.evekit.model.CapsuleerSyncTracker;
import enterprises.orbital.evekit.model.CorporationSyncTracker;
import org.junit.Assert;
import org.junit.Test;

import javax.persistence.TypedQuery;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SynchronizedEveAccountTest extends TestBase {

  @Test
  public void testCreateAccount() throws AccountCreationException, IOException {
    EveKitUserAccount userAccount = EveKitUserAccount.createNewUserAccount(true, true);
    SynchronizedEveAccount out = SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount", true, true);
    Assert.assertEquals(userAccount, out.getUserAccount());
    Assert.assertEquals("testaccount", out.getName());
    Assert.assertTrue(out.isCharacterType());
    Assert.assertTrue(out.isAutoSynchronized());
  }

  @Test
  public void testSetXMLCredential() throws AccountCreationException, AccountUpdateException, AccountNotFoundException, IOException {
    // NONE -> XML
    EveKitUserAccount userAccount = EveKitUserAccount.createNewUserAccount(true, true);
    SynchronizedEveAccount out = SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount", true, true);
    out = SynchronizedEveAccount.setXMLCredential(userAccount, out.getAid(), 1234, "abcd", 5678, "charname", 8765, "corpname");
    Assert.assertEquals(userAccount, out.getUserAccount());
    Assert.assertEquals("testaccount", out.getName());
    Assert.assertTrue(out.isCharacterType());
    Assert.assertTrue(out.isAutoSynchronized());
    Assert.assertEquals(1234, out.getEveKey());
    Assert.assertEquals("abcd", out.getEveVCode());
    Assert.assertNull(out.getAccessToken());
    Assert.assertEquals(-1, out.getAccessTokenExpiry());
    Assert.assertNull(out.getRefreshToken());
    Assert.assertNull(out.getScopes());
    Assert.assertEquals(5678, out.getEveCharacterID());
    Assert.assertEquals("charname", out.getEveCharacterName());
    Assert.assertEquals(8765, out.getEveCorporationID());
    Assert.assertEquals("corpname", out.getEveCorporationName());
  }

  @Test
  public void testClearXMLCredential() throws AccountCreationException, AccountUpdateException, AccountNotFoundException, IOException {
    // XML -> NONE
    EveKitUserAccount userAccount = EveKitUserAccount.createNewUserAccount(true, true);
    SynchronizedEveAccount out = SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount", true, true);
    out = SynchronizedEveAccount.setXMLCredential(userAccount, out.getAid(), 1234, "abcd", 5678, "charname", 8765, "corpname");
    out = SynchronizedEveAccount.clearXMLCredential(userAccount, out.getAid());
    Assert.assertEquals(userAccount, out.getUserAccount());
    Assert.assertEquals("testaccount", out.getName());
    Assert.assertTrue(out.isCharacterType());
    Assert.assertTrue(out.isAutoSynchronized());
    Assert.assertEquals(-1, out.getEveKey());
    Assert.assertNull(out.getEveVCode());
    Assert.assertNull(out.getAccessToken());
    Assert.assertEquals(-1, out.getAccessTokenExpiry());
    Assert.assertNull(out.getRefreshToken());
    Assert.assertNull(out.getScopes());
    Assert.assertEquals(-1, out.getEveCharacterID());
    Assert.assertNull(out.getEveCharacterName());
    Assert.assertEquals(-1, out.getEveCorporationID());
    Assert.assertNull(out.getEveCorporationName());
  }

  @Test
  public void testClearXMLWithESICredential() throws AccountCreationException, AccountUpdateException, AccountNotFoundException, IOException {
    // BOTH -> ESI
    EveKitUserAccount userAccount = EveKitUserAccount.createNewUserAccount(true, true);
    SynchronizedEveAccount out = SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount", true, true);
    out = SynchronizedEveAccount.setXMLCredential(userAccount, out.getAid(), 1234, "abcd", 5678, "charname", 8765, "corpname");
    out = SynchronizedEveAccount.setESICredential(userAccount, out.getAid(), "aaaa", 1111, "bbbb", "scope_list", 5678, "charname", 8765, "corpname");
    out = SynchronizedEveAccount.clearXMLCredential(userAccount, out.getAid());
    Assert.assertEquals(userAccount, out.getUserAccount());
    Assert.assertEquals("testaccount", out.getName());
    Assert.assertTrue(out.isCharacterType());
    Assert.assertTrue(out.isAutoSynchronized());
    Assert.assertEquals(-1, out.getEveKey());
    Assert.assertNull(out.getEveVCode());
    Assert.assertEquals("aaaa", out.getAccessToken());
    Assert.assertEquals(1111, out.getAccessTokenExpiry());
    Assert.assertEquals("bbbb", out.getRefreshToken());
    Assert.assertEquals("scope_list", out.getScopes());
    Assert.assertEquals(5678, out.getEveCharacterID());
    Assert.assertEquals("charname", out.getEveCharacterName());
    Assert.assertEquals(8765, out.getEveCorporationID());
    Assert.assertEquals("corpname", out.getEveCorporationName());
  }

  @Test(expected = AccountUpdateException.class)
  public void testSetConflictingXMLCredential() throws AccountCreationException, AccountUpdateException, AccountNotFoundException, IOException {
    // ESI -> BOTH bad!
    EveKitUserAccount userAccount = EveKitUserAccount.createNewUserAccount(true, true);
    SynchronizedEveAccount out = SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount", true, true);
    out = SynchronizedEveAccount.setESICredential(userAccount, out.getAid(), "abcd", 1234, "efgh", "scope_list", 1111, "aaaa", 2222,"bbbb");
    SynchronizedEveAccount.setXMLCredential(userAccount, out.getAid(), 1234, "abcd", 5678, "charname", 8765, "corpname");
  }

  @Test
  public void testSetNonConflictingXMLCredential() throws AccountCreationException, AccountUpdateException, AccountNotFoundException, IOException {
    // ESI -> BOTH
    EveKitUserAccount userAccount = EveKitUserAccount.createNewUserAccount(true, true);
    SynchronizedEveAccount out = SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount", true, true);
    out = SynchronizedEveAccount.setESICredential(userAccount, out.getAid(), "abcd", 1234, "efgh", "scope_list", 5678, "charname", 8765, "corpname");
    out = SynchronizedEveAccount.setXMLCredential(userAccount, out.getAid(), 1234, "abcd", 5678, "charname", 8765, "corpname");
    Assert.assertEquals(userAccount, out.getUserAccount());
    Assert.assertEquals("testaccount", out.getName());
    Assert.assertTrue(out.isCharacterType());
    Assert.assertTrue(out.isAutoSynchronized());
    Assert.assertEquals(1234, out.getEveKey());
    Assert.assertEquals("abcd", out.getEveVCode());
    Assert.assertEquals("abcd", out.getAccessToken());
    Assert.assertEquals(1234, out.getAccessTokenExpiry());
    Assert.assertEquals("efgh", out.getRefreshToken());
    Assert.assertEquals("scope_list", out.getScopes());
    Assert.assertEquals(5678, out.getEveCharacterID());
    Assert.assertEquals("charname", out.getEveCharacterName());
    Assert.assertEquals(8765, out.getEveCorporationID());
    Assert.assertEquals("corpname", out.getEveCorporationName());
  }

  @Test
  public void testSetESICredential() throws AccountCreationException, AccountUpdateException, AccountNotFoundException, IOException {
    // NONE -> ESI
    EveKitUserAccount userAccount = EveKitUserAccount.createNewUserAccount(true, true);
    SynchronizedEveAccount out = SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount", true, true);
    out = SynchronizedEveAccount.setESICredential(userAccount, out.getAid(), "abcd", 1234, "efgh", "scope_list", 5678, "charname", 8765, "corpname");
    Assert.assertEquals(userAccount, out.getUserAccount());
    Assert.assertEquals("testaccount", out.getName());
    Assert.assertTrue(out.isCharacterType());
    Assert.assertTrue(out.isAutoSynchronized());
    Assert.assertEquals("abcd", out.getAccessToken());
    Assert.assertEquals(1234, out.getAccessTokenExpiry());
    Assert.assertEquals("efgh", out.getRefreshToken());
    Assert.assertEquals("scope_list", out.getScopes());
    Assert.assertEquals(-1, out.getEveKey());
    Assert.assertNull(out.getEveVCode());
    Assert.assertEquals(5678, out.getEveCharacterID());
    Assert.assertEquals("charname", out.getEveCharacterName());
    Assert.assertEquals(8765, out.getEveCorporationID());
    Assert.assertEquals("corpname", out.getEveCorporationName());
  }

  @Test
  public void testClearESICredential() throws AccountCreationException, AccountUpdateException, AccountNotFoundException, IOException {
    // ESI -> NONE
    EveKitUserAccount userAccount = EveKitUserAccount.createNewUserAccount(true, true);
    SynchronizedEveAccount out = SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount", true, true);
    out = SynchronizedEveAccount.setESICredential(userAccount, out.getAid(), "abcd", 1234, "efgh", "scope_list", 8765, "charname", 8765, "corpname");
    out = SynchronizedEveAccount.clearESICredential(userAccount, out.getAid());
    Assert.assertEquals(userAccount, out.getUserAccount());
    Assert.assertEquals("testaccount", out.getName());
    Assert.assertTrue(out.isCharacterType());
    Assert.assertTrue(out.isAutoSynchronized());
    Assert.assertEquals(-1, out.getEveKey());
    Assert.assertNull(out.getEveVCode());
    Assert.assertNull(out.getAccessToken());
    Assert.assertEquals(-1, out.getAccessTokenExpiry());
    Assert.assertNull(out.getRefreshToken());
    Assert.assertNull(out.getScopes());
    Assert.assertEquals(-1, out.getEveCharacterID());
    Assert.assertNull(out.getEveCharacterName());
    Assert.assertEquals(-1, out.getEveCorporationID());
    Assert.assertNull(out.getEveCorporationName());
  }

  @Test
  public void testClearESIWithXMLCredential() throws AccountCreationException, AccountUpdateException, AccountNotFoundException, IOException {
    // BOTH -> XML
    EveKitUserAccount userAccount = EveKitUserAccount.createNewUserAccount(true, true);
    SynchronizedEveAccount out = SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount", true, true);
    out = SynchronizedEveAccount.setXMLCredential(userAccount, out.getAid(), 1234, "abcd", 5678, "charname", 8765, "corpname");
    out = SynchronizedEveAccount.setESICredential(userAccount, out.getAid(), "aaaa", 1111, "bbbb", "scope_list", 5678, "charname", 8765, "corpname");
    out = SynchronizedEveAccount.clearESICredential(userAccount, out.getAid());
    Assert.assertEquals(userAccount, out.getUserAccount());
    Assert.assertEquals("testaccount", out.getName());
    Assert.assertTrue(out.isCharacterType());
    Assert.assertTrue(out.isAutoSynchronized());
    Assert.assertEquals(1234, out.getEveKey());
    Assert.assertEquals("abcd", out.getEveVCode());
    Assert.assertNull(out.getAccessToken());
    Assert.assertEquals(-1, out.getAccessTokenExpiry());
    Assert.assertNull(out.getRefreshToken());
    Assert.assertNull(out.getScopes());
    Assert.assertEquals(5678, out.getEveCharacterID());
    Assert.assertEquals("charname", out.getEveCharacterName());
    Assert.assertEquals(8765, out.getEveCorporationID());
    Assert.assertEquals("corpname", out.getEveCorporationName());
  }

  @Test(expected = AccountUpdateException.class)
  public void testSetConflictingESICredential() throws AccountCreationException, AccountUpdateException, AccountNotFoundException, IOException {
    // XML -> BOTH bad!
    EveKitUserAccount userAccount = EveKitUserAccount.createNewUserAccount(true, true);
    SynchronizedEveAccount out = SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount", true, true);
    out = SynchronizedEveAccount.setXMLCredential(userAccount, out.getAid(), 1234, "abcd", 5678, "charname", 8765, "corpName");
    SynchronizedEveAccount.setESICredential(userAccount, out.getAid(), "aaaa", 1111, "bbbb", "scope_list", 3333, "cccc", 4444, "dddd");
  }

  @Test
  public void testSetNonConflictingESICredential() throws AccountCreationException, AccountUpdateException, AccountNotFoundException, IOException {
    // XML -> BOTH
    EveKitUserAccount userAccount = EveKitUserAccount.createNewUserAccount(true, true);
    SynchronizedEveAccount out = SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount", true, true);
    out = SynchronizedEveAccount.setXMLCredential(userAccount, out.getAid(), 1234, "abcd", 5678, "charname", 8765, "corpname");
    out = SynchronizedEveAccount.setESICredential(userAccount, out.getAid(), "abcd", 1234, "efgh", "scope_list", 5678, "charname", 8765, "corpname");
    Assert.assertEquals(userAccount, out.getUserAccount());
    Assert.assertEquals("testaccount", out.getName());
    Assert.assertTrue(out.isCharacterType());
    Assert.assertTrue(out.isAutoSynchronized());
    Assert.assertEquals(1234, out.getEveKey());
    Assert.assertEquals("abcd", out.getEveVCode());
    Assert.assertEquals("abcd", out.getAccessToken());
    Assert.assertEquals(1234, out.getAccessTokenExpiry());
    Assert.assertEquals("efgh", out.getRefreshToken());
    Assert.assertEquals("scope_list", out.getScopes());
    Assert.assertEquals(5678, out.getEveCharacterID());
    Assert.assertEquals("charname", out.getEveCharacterName());
    Assert.assertEquals(8765, out.getEveCorporationID());
    Assert.assertEquals("corpname", out.getEveCorporationName());
  }

  @Test(expected = AccountCreationException.class)
  public void testCreateAccountExists() throws AccountCreationException, IOException {
    EveKitUserAccount userAccount = EveKitUserAccount.createNewUserAccount(true, true);
    SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount", true, true);
    SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount", true, true);
  }

  @Test
  public void testGetExistingAccount() throws AccountCreationException, AccountNotFoundException, IOException {
    EveKitUserAccount userAccount = EveKitUserAccount.createNewUserAccount(true, true);
    SynchronizedEveAccount existing = null, out;
    existing = SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount", true, true);
    String name = existing.getName();

    // This should retrieve the cached version
    out = SynchronizedEveAccount.getSynchronizedAccount(userAccount, name, false);
    Assert.assertNotNull(out);
    Assert.assertEquals(existing, out);
  }

  @Test(expected = AccountNotFoundException.class)
  public void testGetMissingAccountSameUser() throws AccountCreationException, AccountNotFoundException, IOException {
    EveKitUserAccount userAccount = EveKitUserAccount.createNewUserAccount(true, true);
    SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount", true, true);
    SynchronizedEveAccount.getSynchronizedAccount(userAccount, "12345", false);
  }

  @Test(expected = AccountNotFoundException.class)
  public void testGetMissingAccountDifferentUser() throws AccountCreationException, AccountNotFoundException, IOException {
    EveKitUserAccount userAccount = EveKitUserAccount.createNewUserAccount(true, true);
    EveKitUserAccount userAccount2 = EveKitUserAccount.createNewUserAccount(true, true);
    SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount", true, true);
    SynchronizedEveAccount.getSynchronizedAccount(userAccount2, "testaccount", false);
  }

  @Test
  public void testGetAllAccounts() throws IOException, AccountCreationException {
    EveKitUserAccount userAccount = EveKitUserAccount.createNewUserAccount(true, true);
    SynchronizedEveAccount out1 = SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount1", true, true);
    SynchronizedEveAccount out2 = SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount2", true, true);
    List<SynchronizedEveAccount> allAccounts = SynchronizedEveAccount.getAllAccounts(userAccount, false);
    Assert.assertThat(allAccounts, org.hamcrest.CoreMatchers.hasItems(out1, out2));
  }

  @Test
  public void testRestoreAccount() throws IOException, AccountNotFoundException, AccountCreationException {
    EveKitUserAccount userAccount = EveKitUserAccount.createNewUserAccount(true, true);
    long id = SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount1", true, true).getAid();
    SynchronizedEveAccount.deleteAccount(userAccount, id);
    try {
      SynchronizedEveAccount.getSynchronizedAccount(userAccount, id, false);
      Assert.fail("previous call should have thrown exception");
    } catch (AccountNotFoundException e) {
      // Expected
    }
    SynchronizedEveAccount.restoreAccount(userAccount, id);
    SynchronizedEveAccount.getSynchronizedAccount(userAccount, id, false);
  }

  @Test
  public void testRestoreAccountMissing() throws IOException, AccountCreationException, AccountNotFoundException {
    EveKitUserAccount userAccount = EveKitUserAccount.createNewUserAccount(true, true);
    long id = SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount1", true, true).getAid();
    try {
      SynchronizedEveAccount.restoreAccount(userAccount, 12345L);
      Assert.fail("previous call should have thrown exception");
    } catch (AccountNotFoundException e) {
      // Expected
    }
    SynchronizedEveAccount.getSynchronizedAccount(userAccount, id, false);
  }

  @Test
  public void testDeleteAccount() throws IOException, AccountCreationException, AccountNotFoundException {
    EveKitUserAccount userAccount = EveKitUserAccount.createNewUserAccount(true, true);
    long id = SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount1", true, true).getAid();
    SynchronizedEveAccount.deleteAccount(userAccount, id);
    try {
    SynchronizedEveAccount.getSynchronizedAccount(userAccount, id, false);
      Assert.fail("previous call should have thrown exception");
    } catch (AccountNotFoundException e) {
      // Expected
    }
    SynchronizedEveAccount.getSynchronizedAccount(userAccount, id, true);
  }

  @Test
  public void testDeleteAccountMissing() throws IOException, AccountCreationException, AccountNotFoundException {
    EveKitUserAccount userAccount = EveKitUserAccount.createNewUserAccount(true, true);
    SynchronizedEveAccount tst = SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount1", true, true);
    try {
      SynchronizedEveAccount.deleteAccount(userAccount, 12345L);
      Assert.fail("previous call should have thrown exception");
    } catch (AccountNotFoundException e) {
      // Expected
    }
    SynchronizedEveAccount.getSynchronizedAccount(userAccount, tst.getName(), false);
  }

  @Test
  public void testUpdateAccount() throws IOException, AccountCreationException, AccountNotFoundException, AccountUpdateException {
    EveKitUserAccount userAccount = EveKitUserAccount.createNewUserAccount(true, true);
    SynchronizedEveAccount tst = SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount", true, true);
    SynchronizedEveAccount.updateAccount(userAccount, tst.getAid(), "testaccount", false);
    SynchronizedEveAccount out = SynchronizedEveAccount.getSynchronizedAccount(userAccount, tst.getName(), false);
    Assert.assertEquals(userAccount, out.getUserAccount());
    Assert.assertEquals("testaccount", out.getName());
    Assert.assertEquals(tst.getName(), out.getName());
    Assert.assertTrue(out.isCharacterType());
    Assert.assertFalse(out.isAutoSynchronized());
  }

  @Test
  public void testUpdateAccountMissing() throws IOException, AccountCreationException, AccountUpdateException, AccountNotFoundException {
    EveKitUserAccount userAccount = EveKitUserAccount.createNewUserAccount(true, true);
    SynchronizedEveAccount tst = SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount", true, true);
    try {
      SynchronizedEveAccount.updateAccount(userAccount, 12345L, "testaccount", false);
      Assert.fail("previous call should have thrown exception");
    } catch (AccountNotFoundException e) {
      // expected.
    }
    SynchronizedEveAccount out = SynchronizedEveAccount.getSynchronizedAccount(userAccount, tst.getName(), false);
    Assert.assertEquals(userAccount, out.getUserAccount());
    Assert.assertEquals(tst.getName(), out.getName());
    Assert.assertTrue(out.isCharacterType());
    Assert.assertTrue(out.isAutoSynchronized());
  }

  @Test
  public void testRemoveAccount() throws IOException, AccountCreationException, AccessKeyCreationException, ExecutionException {
    if (Boolean.valueOf(System.getProperty("enterprises.orbtial.evekit.model.unittest.skipbig", "false"))) { return; }

    // Setup account
    EveKitUserAccount userAccount = EveKitUserAccount.createNewUserAccount(true, true);
    SynchronizedEveAccount testAccount = SynchronizedEveAccount.createSynchronizedEveAccount(userAccount, "testaccount", true, true);

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
    try {
      SynchronizedEveAccount.getSynchronizedAccount(testAccount.getUserAccount(), testAccount.getAid(), true);
      Assert.fail("previous call should have thrown exception");
    } catch (AccountNotFoundException e) {
      // expected.
    }
    final SynchronizedEveAccount check = testAccount;
    long remaining = EveKitUserAccountProvider.getFactory().runTransaction(() -> {
        TypedQuery<Long> query = EveKitUserAccountProvider.getFactory().getEntityManager()
            .createQuery("SELECT count(c) FROM SyncTracker c where c.account= :acct", Long.class);
        query.setParameter("acct", check);
        return query.getSingleResult();
      });
    Assert.assertEquals(0, remaining);
    remaining = EveKitUserAccountProvider.getFactory().runTransaction(() -> {
        TypedQuery<Long> query = EveKitUserAccountProvider.getFactory().getEntityManager()
            .createQuery("SELECT count(c) FROM SynchronizedAccountAccessKey c where c.account= :acct", Long.class);
        query.setParameter("acct", check);
        return query.getSingleResult();
      });
    Assert.assertEquals(0, remaining);
  }

}
