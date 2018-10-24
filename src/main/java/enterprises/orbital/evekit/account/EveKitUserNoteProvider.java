package enterprises.orbital.evekit.account;

import enterprises.orbital.base.OrbitalProperties;
import enterprises.orbital.db.ConnectionFactory;

import java.io.IOException;

/**
 * This provider is only used for EveKitUserNotification methods.  We use a separate provider
 * so that processes which need only read access to model data can still create user notifications.
 * At present, the process which uses this feature most often is the snapshot generator.
 */
@SuppressWarnings("WeakerAccess")
public class EveKitUserNoteProvider extends ConnectionFactoryProvider {
  public static final String REF_DATA_PU_PROP    = "enterprises.orbital.evekit.user_note.persistence_unit";
  public static final String REF_DATA_PU_DEFAULT = "evekit-production";

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
