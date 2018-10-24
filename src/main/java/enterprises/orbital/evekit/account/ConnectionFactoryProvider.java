package enterprises.orbital.evekit.account;

import enterprises.orbital.db.ConnectionFactory;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ConnectionFactoryProvider {
  private static final Logger log       = Logger.getLogger(ConnectionFactoryProvider.class.getName());

  protected static ConnectionFactory getFactory(String factoryName) {
    return ConnectionFactory.getFactory(factoryName);
  }

  protected static <T> T update(String factoryName, T tracked) throws IOException {
    try {
      ConnectionFactory factory = getFactory(factoryName);
      return factory.runTransaction(() -> factory.getEntityManager().merge(tracked));
    } catch (Exception e) {
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

}
