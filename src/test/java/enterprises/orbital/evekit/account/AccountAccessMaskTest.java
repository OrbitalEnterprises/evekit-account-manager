package enterprises.orbital.evekit.account;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import enterprises.orbital.evekit.account.AccountAccessMask;

public class AccountAccessMaskTest {

  @Test
  public void testSetMask() {
    byte[] value = AccountAccessMask.setMask(new byte[0], AccountAccessMask.ACCESS_SKILL_QUEUE);
    Assert.assertTrue(AccountAccessMask.isValidMask(value));
    Assert.assertTrue(AccountAccessMask.isAccessAllowed(value, AccountAccessMask.ACCESS_SKILL_QUEUE));
    int val = AccountAccessMask.ACCESS_SKILL_QUEUE.getMaskValue();
    int offset = val / 8;
    int bit = val % 8;
    for (int i = 0; i < value.length; i++) {
      if (i == offset) {
        Assert.assertEquals(1L << bit, value[i]);
      } else {
        Assert.assertEquals(0, value[i]);
      }
    }
  }

  @Test
  public void testUnsetMask() {
    byte[] value = AccountAccessMask.setMask(new byte[0], AccountAccessMask.ACCESS_SKILL_QUEUE);
    value = AccountAccessMask.setMask(value, AccountAccessMask.ACCESS_ACCOUNT_BALANCE);
    Assert.assertTrue(AccountAccessMask.isValidMask(value));
    for (AccountAccessMask next : AccountAccessMask.values()) {
      switch (next) {
      case ACCESS_SKILL_QUEUE:
      case ACCESS_ACCOUNT_BALANCE:
        Assert.assertTrue(AccountAccessMask.isAccessAllowed(value, next));
        break;
      default:
        Assert.assertFalse(AccountAccessMask.isAccessAllowed(value, next));
      }
    }
  }

  @Test
  public void testIsAccessAllowedYes() {
    byte[] mask = AccountAccessMask.createMask(Arrays.asList(new AccountAccessMask[] {
        AccountAccessMask.ACCESS_ACCOUNT_BALANCE, AccountAccessMask.ACCESS_SKILL_QUEUE
    }));
    Assert.assertTrue(AccountAccessMask.isAccessAllowed(mask, AccountAccessMask.ACCESS_SKILL_QUEUE));
  }

  @Test
  public void testIsAccessAllowedNo() {
    byte[] mask = AccountAccessMask.createMask(Arrays.asList(new AccountAccessMask[] {
        AccountAccessMask.ACCESS_ACCOUNT_BALANCE, AccountAccessMask.ACCESS_SKILL_QUEUE
    }));
    Assert.assertFalse(AccountAccessMask.isAccessAllowed(mask, AccountAccessMask.ACCESS_WALLET_JOURNAL));
  }

  @Test
  public void testCheckAccessYes() {
    byte[] mask = AccountAccessMask.createMask(Arrays.asList(new AccountAccessMask[] {
      AccountAccessMask.ACCESS_ACCOUNT_BALANCE
    }));
    Assert.assertTrue(AccountAccessMask.ACCESS_ACCOUNT_BALANCE.checkAccess(mask));
  }

  @Test
  public void testCheckAccessNo() {
    byte[] mask = AccountAccessMask.createMask(Arrays.asList(new AccountAccessMask[] {
      AccountAccessMask.ACCESS_ASSETS
    }));
    Assert.assertFalse(AccountAccessMask.ACCESS_ACCOUNT_BALANCE.checkAccess(mask));
  }

  @Test
  public void testIsValidMaskNo() {
    int max = 0;
    for (AccountAccessMask next : AccountAccessMask.values()) {
      max = Math.max(max, next.getMaskValue());
    }
    max = max + 1;
    byte[] testMask = new byte[((max + 1) / 8) + ((max + 1) % 8 > 0
                                                                   ? 1
                                                                   : 0)];
    int offset = max / 8;
    int bit = max % 8;
    testMask[offset] |= 1L << bit;
    Assert.assertFalse(AccountAccessMask.isValidMask(testMask));
  }
}
