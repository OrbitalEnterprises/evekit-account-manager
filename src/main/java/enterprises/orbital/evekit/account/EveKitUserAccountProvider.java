package enterprises.orbital.evekit.account;

import enterprises.orbital.base.OrbitalProperties;
import enterprises.orbital.db.ConnectionFactory;
import enterprises.orbital.evekit.model.ESIEndpointSyncTracker;
import enterprises.orbital.oauth.UserAccount;
import enterprises.orbital.oauth.UserAccountProvider;
import enterprises.orbital.oauth.UserAuthSource;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EveKitUserAccountProvider implements UserAccountProvider {
  private static final Logger log       = Logger.getLogger(EveKitUserAccountProvider.class.getName());

  public static final String USER_ACCOUNT_PU_PROP    = "enterprises.orbital.evekit.account.persistence_unit";
  public static final String USER_ACCOUNT_PU_DEFAULT = "evekit-production";

  public static ConnectionFactory getFactory() {
    return ConnectionFactory.getFactory(OrbitalProperties.getGlobalProperty(USER_ACCOUNT_PU_PROP, USER_ACCOUNT_PU_DEFAULT));
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

  @Override
  public UserAccount getAccount(String uid) {
    long user_id = 0;
    try {
      user_id = Long.valueOf(uid);
    } catch (NumberFormatException e) {
      user_id = 0;
    }
    try {
      return EveKitUserAccount.getAccount(user_id);
    } catch (UserNotFoundException | IOException e) {
      return null;
    }
  }

  @Override
  public UserAuthSource getSource(UserAccount acct, String source) {
    assert acct instanceof EveKitUserAccount;
    try {
      return EveKitUserAuthSource.getSource((EveKitUserAccount) acct, source);
    } catch (AuthSourceNotFoundException | IOException e) {
      return null;
    }
  }

  @Override
  public void removeSourceIfExists(UserAccount acct, String source) {
    assert acct instanceof EveKitUserAccount;
    try {
      EveKitUserAuthSource.removeSourceIfExists((EveKitUserAccount) acct, source);
    } catch (IOException e) {
      // ignore
    }
  }

  @Override
  public UserAuthSource getBySourceScreenname(String source, String screenName) {
    try {
      return EveKitUserAuthSource.getBySourceScreenname(source, screenName);
    } catch (IOException e) {
      return null;
    }
  }

  @Override
  public UserAuthSource createSource(UserAccount newUser, String source, String screenName, String body) {
    assert newUser instanceof EveKitUserAccount;
    try {
      return EveKitUserAuthSource.createSource((EveKitUserAccount) newUser, source, screenName, body);
    } catch (IOException e) {
      return null;
    }
  }

  @Override
  public UserAccount createNewUserAccount(boolean disabled) {
    try {
      return EveKitUserAccount.createNewUserAccount(false, !disabled);
    } catch (IOException e) {
      return null;
    }
  }

}
