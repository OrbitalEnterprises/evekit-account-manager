package enterprises.orbital.evekit.account;

import enterprises.orbital.base.OrbitalProperties;
import enterprises.orbital.db.ConnectionFactory;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provider for XML API reference data sync.
 */
public class EveKitRefDataProvider {
  private static final Logger log       = Logger.getLogger(EveKitRefDataProvider.class.getName());

  public static final String REF_DATA_PU_PROP    = "enterprises.orbital.evekit.ref.persistence_unit";
  public static final String REF_DATA_PU_DEFAULT = "evekit-ref";

  public static ConnectionFactory getFactory() {
    return ConnectionFactory.getFactory(OrbitalProperties.getGlobalProperty(REF_DATA_PU_PROP, REF_DATA_PU_DEFAULT));
  }

  public static <T> T update(T tracked) throws IOException {
    try {
      return getFactory().runTransaction(() -> getFactory().getEntityManager().merge(tracked));
    } catch (Exception e) {
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

}
