package enterprises.orbital.evekit.account;

import enterprises.orbital.base.OrbitalProperties;
import enterprises.orbital.db.ConnectionFactory;
import enterprises.orbital.oauth.UserAccount;
import enterprises.orbital.oauth.UserAccountProvider;
import enterprises.orbital.oauth.UserAuthSource;

public class EveKitUserAccountProvider implements UserAccountProvider {
  public static final String USER_ACCOUNT_PU_PROP    = "enterprises.orbital.evekit.account.persistence_unit";
  public static final String USER_ACCOUNT_PU_DEFAULT = "evekit-production";

  public static ConnectionFactory getFactory() {
    return ConnectionFactory.getFactory(OrbitalProperties.getGlobalProperty(USER_ACCOUNT_PU_PROP, USER_ACCOUNT_PU_DEFAULT));
  }

  @Override
  public UserAccount getAccount(String uid) {
    long user_id = 0;
    try {
      user_id = Long.valueOf(uid);
    } catch (NumberFormatException e) {
      user_id = 0;
    }
    return EveKitUserAccount.getAccount(user_id);
  }

  @Override
  public UserAuthSource getSource(UserAccount acct, String source) {
    assert acct instanceof EveKitUserAccount;
    return EveKitUserAuthSource.getSource((EveKitUserAccount) acct, source);
  }

  @Override
  public void removeSourceIfExists(UserAccount acct, String source) {
    assert acct instanceof EveKitUserAccount;
    EveKitUserAuthSource.removeSourceIfExists((EveKitUserAccount) acct, source);
  }

  @Override
  public UserAuthSource getBySourceScreenname(String source, String screenName) {
    return EveKitUserAuthSource.getBySourceScreenname(source, screenName);
  }

  @Override
  public UserAuthSource createSource(UserAccount newUser, String source, String screenName, String body) {
    assert newUser instanceof EveKitUserAccount;
    return EveKitUserAuthSource.createSource((EveKitUserAccount) newUser, source, screenName, body);
  }

  @Override
  public UserAccount createNewUserAccount(boolean disabled) {
    return EveKitUserAccount.createNewUserAccount(false, !disabled);
  }

}
