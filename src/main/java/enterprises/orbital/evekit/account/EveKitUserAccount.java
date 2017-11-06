package enterprises.orbital.evekit.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import enterprises.orbital.base.OrbitalProperties;
import enterprises.orbital.base.PersistentPropertyKey;
import enterprises.orbital.oauth.UserAccount;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User account entries
 */
@Entity
@Table(
    name = "evekit_users")
@NamedQueries({
    @NamedQuery(
        name = "EveKitUserAccount.findByUid",
        query = "SELECT c FROM EveKitUserAccount c where c.uid = :uid"),
    @NamedQuery(
        name = "EveKitUserAccount.allAccounts",
        query = "SELECT c FROM EveKitUserAccount c"),
})
@ApiModel(
    description = "User account")
@JsonSerialize(
    typing = JsonSerialize.Typing.DYNAMIC)
public class EveKitUserAccount implements UserAccount, PersistentPropertyKey<String> {
  private static final Logger log = Logger.getLogger(EveKitUserAccount.class.getName());

  // Constants for commonly used properties
  public static final String PROP_STATIC_DB_ACCESS_LIMIT = "StaticDBAccessLimit";

  // Unique user ID
  @Id
  @GeneratedValue(
      strategy = GenerationType.SEQUENCE,
      generator = "ek_seq")
  @SequenceGenerator(
      name = "ek_seq",
      initialValue = 100000,
      allocationSize = 10,
      sequenceName = "account_sequence")
  @ApiModelProperty(
      value = "Unique user ID")
  @JsonProperty("uid")
  protected long uid;

  // True if this user is active
  @ApiModelProperty(
      value = "True if user is active, false otherwise")
  @JsonProperty("active")
  protected boolean active;

  // Date when this user was created
  @ApiModelProperty(
      value = "Date (milliseconds UTC) when account was created")
  @JsonProperty("created")
  protected long created = -1;

  // True if this user is an administrator
  @ApiModelProperty(
      value = "True if user is an admin, false otherwise")
  @JsonProperty("admin")
  protected boolean admin;

  // Date when this user last logged in
  @ApiModelProperty(
      value = "Last time (milliseconds UTC) user logged in")
  @JsonProperty("last")
  protected long last = -1;

  public long getID() {
    return uid;
  }

  @Override
  public String getUid() {
    return String.valueOf(uid);
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(
      boolean active) {
    this.active = active;
  }

  public long getCreated() {
    return created;
  }

  public boolean isAdmin() {
    return admin;
  }

  public void setAdmin(
      boolean admin) {
    this.admin = admin;
  }

  public long getLast() {
    return last;
  }

  public void setLast(
      long last) {
    this.last = last;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (active ? 1231 : 1237);
    result = prime * result + (admin ? 1231 : 1237);
    result = prime * result + (int) (created ^ (created >>> 32));
    result = prime * result + (int) (last ^ (last >>> 32));
    result = prime * result + (int) (uid ^ (uid >>> 32));
    return result;
  }

  @Override
  public boolean equals(
      Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    EveKitUserAccount other = (EveKitUserAccount) obj;
    if (active != other.active) return false;
    if (admin != other.admin) return false;
    if (created != other.created) return false;
    if (last != other.last) return false;
    if (uid != other.uid) return false;
    return true;
  }

  @Override
  public String toString() {
    return "EveKitUserAccount [uid=" + uid + ", active=" + active + ", created=" + created + ", admin=" + admin + ", last=" + last + "]";
  }

  public EveKitUserAccount copy() {
    EveKitUserAccount copy = new EveKitUserAccount();
    copy.uid = uid;
    copy.active = active;
    copy.created = created;
    copy.admin = admin;
    copy.last = last;
    return copy;
  }

  /**
   * Create a new user account.
   *
   * @param admin  true if this user should be created with administrative privileges.
   * @param active true if this user should be initially active.
   * @return the new EveKitUserAccount.
   * @throws IOException on any database error
   */
  public static EveKitUserAccount createNewUserAccount(
      final boolean admin,
      final boolean active) throws IOException {
    try {
      return EveKitUserAccountProvider.getFactory()
                                      .runTransaction(() -> {
                                        EveKitUserAccount result = new EveKitUserAccount();
                                        result.active = active;
                                        result.created = OrbitalProperties.getCurrentTime();
                                        result.admin = admin;
                                        result.last = result.created;
                                        return EveKitUserAccountProvider.getFactory()
                                                                        .getEntityManager()
                                                                        .merge(result);
                                      });
    } catch (Exception e) {
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  /**
   * Retrieve the user account with the given id.
   *
   * @param uid the ID of the user account to retrieve.
   * @return the given UserAccount, or null if no such user exists.
   * @throws UserNotFoundException if no user with the given ID was found
   * @throws IOException           on any database error
   */
  public static EveKitUserAccount getAccount(
      final long uid) throws UserNotFoundException, IOException {
    try {
      return EveKitUserAccountProvider.getFactory()
                                      .runTransaction(() -> {
                                        TypedQuery<EveKitUserAccount> getter = EveKitUserAccountProvider.getFactory()
                                                                                                        .getEntityManager()
                                                                                                        .createNamedQuery("EveKitUserAccount.findByUid",
                                                                                                                          EveKitUserAccount.class);
                                        getter.setParameter("uid", uid);
                                        try {
                                          return getter.getSingleResult();
                                        } catch (NoResultException e) {
                                          throw new UserNotFoundException();
                                        }
                                      });
    } catch (Exception e) {
      if (e.getCause() instanceof UserNotFoundException) throw (UserNotFoundException) e.getCause();
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  /**
   * Update the "last" time for this user to the current time.
   *
   * @param user the UserAccount to update.
   * @return returns the newly persisted user.
   * @throws IOException on any database error
   */
  public static EveKitUserAccount touch(
      final EveKitUserAccount user) throws IOException {
    try {
      return EveKitUserAccountProvider.getFactory()
                                      .runTransaction(() -> {
                                        user.last = OrbitalProperties.getCurrentTime();
                                        return EveKitUserAccountProvider.getFactory()
                                                                        .getEntityManager()
                                                                        .merge(user);
                                      });
    } catch (Exception e) {
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  /**
   * Return list of all user accounts.
   *
   * @return the list of all current user accounts.
   * @throws IOException on any database error;
   */
  public static List<EveKitUserAccount> getAllAccounts() throws IOException {
    try {
      return EveKitUserAccountProvider.getFactory()
                                      .runTransaction(() -> {
                                        TypedQuery<EveKitUserAccount> getter = EveKitUserAccountProvider.getFactory()
                                                                                                        .getEntityManager()
                                                                                                        .createNamedQuery("EveKitUserAccount.allAccounts",
                                                                                                                          EveKitUserAccount.class);
                                        return getter.getResultList();
                                      });
    } catch (Exception e) {
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  @Override
  public boolean isDisabled() {
    return !active;
  }

  @Override
  public void touch() {
    try {
      touch(this);
    } catch (IOException e) {
      // Log but ignore
      log.log(Level.WARNING, "Failed ot update last access time", e);
    }
  }

  @Override
  public Date getJoinTime() {
    return new Date(created);
  }

  @Override
  public Date getLastSignOn() {
    return new Date(last);
  }

  @Override
  public String getPeristentPropertyKey(
      String field) {
    // Key scheme: EveKitUserAccount.<UUID>.<field>
    return "EveKitUserAccount." + String.valueOf(uid) + "." + field;
  }

  public static EveKitUserAccount update(
      final EveKitUserAccount data) throws IOException {
    try {
      return EveKitUserAccountProvider.getFactory()
                                      .runTransaction(() ->
                                                          EveKitUserAccountProvider.getFactory()
                                                                                   .getEntityManager()
                                                                                   .merge(data)
                                                     );
    } catch (Exception e) {
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

}
