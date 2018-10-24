package enterprises.orbital.evekit.account;

import enterprises.orbital.base.OrbitalProperties;
import enterprises.orbital.db.ConnectionFactory;

import java.io.IOException;

/**
 * Provider for ESI reference data sync.  These are mainly endpoints that do not require
 * authentication.
 */
@SuppressWarnings("WeakerAccess")
public class EveKitRefDataProvider extends ConnectionFactoryProvider {
  public static final String REF_DATA_PU_PROP    = "enterprises.orbital.evekit.ref.persistence_unit";
  public static final String REF_DATA_PU_DEFAULT = "evekit-ref";

  public static String factoryName() {
    return OrbitalProperties.getGlobalProperty(REF_DATA_PU_PROP, REF_DATA_PU_DEFAULT);
  }

  public static ConnectionFactory getFactory() {
    return getFactory(factoryName());
  }

  public static <T> T update(T tracked) throws IOException {
    return update(factoryName(), tracked);
  }

}
