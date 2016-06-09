package enterprises.orbital.evekit.account;

import org.junit.Assert;
import org.junit.Test;

import enterprises.orbital.evekit.model.SynchronizationState;

public class SynchronizationStateTest {
  @Test
  public void testSetMask() {
    long bigMask = 2147483648L;
    Assert.assertTrue(SynchronizationState.SYNC_CHAR_PARTIALCHARACTERSHEET.isAllowed(bigMask));
  }

}
