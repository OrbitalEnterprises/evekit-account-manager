package enterprises.orbital.evekit.account;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Mask of values controlling access to data in synchronized accounts.
 */
public enum AccountAccessMask {

  // Common Resources
  ACCESS_ACCOUNT_STATUS(0),
  ACCESS_ACCOUNT_BALANCE(1),
  ACCESS_ASSETS(2),
  ACCESS_CONTACT_LIST(3),
  ACCESS_BLUEPRINTS(38),
  ACCESS_BOOKMARKS(39),
  ACCESS_CONTRACTS(4),
  ACCESS_FAC_WAR_STATS(7),
  ACCESS_INDUSTRY_JOBS(8),
  ACCESS_KILL_LOG(9),
  ACCESS_MARKET_ORDERS(10),
  ACCESS_STANDINGS(11),
  ACCESS_WALLET_JOURNAL(12),
  ACCESS_WALLET_TRANSACTIONS(13),
  ALLOW_METADATA_CHANGES(14),

  // Character Specific Resources
  ACCESS_CALENDAR_EVENT_ATTENDEES(15),
  ACCESS_CHARACTER_SHEET(16),
  ACCESS_CONTACT_NOTIFICATIONS(17),
  ACCESS_MAIL(18),
  ACCESS_MAILING_LISTS(19),
  ACCESS_MEDALS(20),
  ACCESS_NOTIFICATIONS(21),
  ACCESS_RESEARCH(22),
  ACCESS_SKILL_IN_TRAINING(23),
  ACCESS_SKILL_QUEUE(24),
  ACCESS_UPCOMING_CALENDAR_EVENTS(5),

  // Corporation Specific Resources
  ACCESS_CONTAINER_LOG(25),
  ACCESS_CORPORATION_SHEET(26),
  ACCESS_CORPORATION_MEDALS(27),
  ACCESS_MEMBER_MEDALS(28),
  ACCESS_MEMBER_SECURITY(29),
  ACCESS_MEMBER_SECURITY_LOG(30),
  ACCESS_MEMBER_TRACKING(31),
  ACCESS_OUTPOST_LIST(32),
  ACCESS_SHAREHOLDERS(34),
  ACCESS_STARBASE_LIST(35),
  ACCESS_CORPORATION_TITLES(37);

  private int maskPosition;

  private AccountAccessMask(int val) {
    maskPosition = val;
  }

  public int getMaskValue() {
    return maskPosition;
  }

  protected static byte[] extend(byte[] original, int pos) {
    int maxBit = original.length * 8;
    if (pos >= maxBit) {
      int newLen = (pos + 1) / 8 + ((pos + 1) % 8 > 0 ? 1 : 0);
      byte[] copy = new byte[newLen];
      System.arraycopy(original, 0, copy, 0, original.length);
      original = copy;
    }

    return original;
  }

  public static byte[] setMask(byte[] original, AccountAccessMask value) {
    original = extend(original, value.maskPosition);
    int offset = value.maskPosition / 8;
    int bit = value.maskPosition % 8;
    original[offset] |= 1L << bit;

    return original;
  }

  public static byte[] unsetMask(byte[] original, AccountAccessMask value) {
    original = extend(original, value.maskPosition);
    int offset = value.maskPosition / 8;
    int bit = value.maskPosition % 8;
    original[offset] &= ~(1L << bit);

    return original;
  }

  public static boolean isAccessAllowed(byte[] mask, AccountAccessMask test) {
    int offset = test.maskPosition / 8;
    int bit = test.maskPosition % 8;

    return (offset < mask.length) && (mask[offset] & (1L << bit)) != 0;
  }

  public static byte[] createMask(AccountAccessMask singleton) {
    byte[] mask = new byte[1];
    mask = setMask(mask, singleton);

    return mask;
  }

  public static byte[] createMask(Iterable<AccountAccessMask> set) {
    byte[] mask = new byte[1];
    for (AccountAccessMask next : set) {
      mask = setMask(mask, next);
    }

    return mask;
  }

  public static Collection<AccountAccessMask> createMaskSet(byte[] raw) {
    List<AccountAccessMask> result = new ArrayList<AccountAccessMask>();
    for (AccountAccessMask next : AccountAccessMask.values()) {
      if (next.checkAccess(raw)) result.add(next);
    }
    return result;
  }

  public boolean checkAccess(byte[] mask) {
    return isAccessAllowed(mask, this);
  }

  /**
   * Verify that the corresponding mask does not use any undefined bits.
   * 
   * @param mask
   *          the mask to check
   * @return true if valid, false otherwise.
   */
  public static boolean isValidMask(byte[] mask) {
    byte[] test = new byte[mask.length];
    System.arraycopy(mask, 0, test, 0, mask.length);
    for (AccountAccessMask next : AccountAccessMask.values()) {
      test = unsetMask(test, next);
    }
    for (int i = 0; i < test.length; i++) {
      if (test[i] != 0) return false;
    }

    return true;
  }
}
