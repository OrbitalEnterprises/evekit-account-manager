package enterprises.orbital.evekit;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.After;
import org.junit.Before;

import enterprises.orbital.base.OrbitalProperties;
import enterprises.orbital.base.PersistentProperty;
import enterprises.orbital.db.DBPropertyProvider;
import enterprises.orbital.evekit.account.EveKitUserAccountProvider;

public class TestBase {

  @Before
  public void setUp() throws IOException {
    OrbitalProperties.addPropertyFile("AccountManagerTest.properties");
    PersistentProperty.setProvider(new DBPropertyProvider(OrbitalProperties.getGlobalProperty(EveKitUserAccountProvider.USER_ACCOUNT_PU_PROP)));
  }

  @After
  public void tearDown() throws IOException {}

  protected static Random             GEN             = new Random(
                                                          Long.parseLong(System.getProperty("test.seed", "0")) == 0 ? OrbitalProperties.getCurrentTime()
                                                              : Long.parseLong(System.getProperty("test.seed", "0")));
  protected static final int          MAX_RANDOM      = 1 << 20;
  protected static final Set<Long>    UNIQUE_LONGS    = new HashSet<Long>();
  protected static final Set<Integer> UNIQUE_INTEGERS = new HashSet<Integer>();

  public static void changeSeed(long seed) {
    GEN = new Random(seed);
  }

  public static String getRandomText(int length) {
    char[] alpha = "aAbBcCdDeEfFgGhHiIjJkKlLmMnNoOpPqQrRsStTuUvVwWxXyYzZ ".toCharArray();
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < length; i++) {
      builder.append(alpha[GEN.nextInt(alpha.length)]);
    }
    return builder.toString();
  }

  public static int getRandomInt() {
    return GEN.nextInt(MAX_RANDOM) + 1;
  }

  public static int getRandomInt(int max) {
    return GEN.nextInt(max);
  }

  public static long getRandomLong() {
    long val = Math.abs(GEN.nextLong());
    if (val == 0) {
      val++;
    }
    return val;
  }

  public static long getRandomLong(long max) {
    long next = getRandomLong();
    while (next >= max) {
      next = getRandomLong();
    }
    return next;
  }

  public static double getRandomDouble(double max) {
    return GEN.nextDouble() * max;
  }

  public static boolean getRandomBoolean() {
    return GEN.nextBoolean();
  }

  public static long getUniqueRandomLong() {
    long val = getRandomLong();
    while (UNIQUE_LONGS.contains(val)) {
      val = getRandomLong();
    }
    UNIQUE_LONGS.add(val);
    return val;
  }

  public static int getUniqueRandomInteger() {
    int val = getRandomInt();
    while (UNIQUE_INTEGERS.contains(val)) {
      val = getRandomInt();
    }
    UNIQUE_INTEGERS.add(val);
    return val;
  }

  public static BigDecimal getRandomBigDecimal(int i) {
    return (new BigDecimal(Math.abs(GEN.nextGaussian() * i))).setScale(2, RoundingMode.HALF_UP);
  }

}
