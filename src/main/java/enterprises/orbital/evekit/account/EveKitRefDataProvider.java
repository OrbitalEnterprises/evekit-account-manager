package enterprises.orbital.evekit.account;

import enterprises.orbital.base.OrbitalProperties;
import enterprises.orbital.db.ConnectionFactory;

/**
 * Provider for XML API reference data sync.
 */
public class EveKitRefDataProvider {
  public static final String REF_DATA_PU_PROP    = "enterprises.orbital.evekit.ref.persistence_unit";
  public static final String REF_DATA_PU_DEFAULT = "evekit-ref";

  public static ConnectionFactory getFactory() {
    return ConnectionFactory.getFactory(OrbitalProperties.getGlobalProperty(REF_DATA_PU_PROP, REF_DATA_PU_DEFAULT));
  }

}
