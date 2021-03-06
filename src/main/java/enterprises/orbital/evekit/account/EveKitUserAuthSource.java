package enterprises.orbital.evekit.account;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NoResultException;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TypedQuery;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import enterprises.orbital.base.OrbitalProperties;
import enterprises.orbital.db.ConnectionFactory.RunInTransaction;
import enterprises.orbital.db.ConnectionFactory.RunInVoidTransaction;
import enterprises.orbital.oauth.UserAccount;
import enterprises.orbital.oauth.UserAuthSource;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * User authentication sources. There may be multiple entries associated with a single UserAccount.
 */
@Entity
@Table(
    name = "evekit_auth_source",
    indexes = {
        @Index(
            name = "accountIndex",
            columnList = "uid",
            unique = false),
        @Index(
            name = "sourceAndScreenIndex",
            columnList = "source, screenName",
            unique = false)
    })
@NamedQueries({
    @NamedQuery(
        name = "EveKitUserAuthSource.findByAcctAndSource",
        query = "SELECT c FROM EveKitUserAuthSource c where c.account = :account and c.source = :source"),
    @NamedQuery(
        name = "EveKitUserAuthSource.allSourcesByAcct",
        query = "SELECT c FROM EveKitUserAuthSource c where c.account = :account order by c.last desc"),
    @NamedQuery(
        name = "EveKitUserAuthSource.all",
        query = "SELECT c FROM EveKitUserAuthSource c"),
    @NamedQuery(
        name = "EveKitUserAuthSource.allBySourceAndScreenname",
        query = "SELECT c FROM EveKitUserAuthSource c where c.source = :source and c.screenName = :screenname"),
})
@ApiModel(
    description = "Authentication source for a user")
@JsonSerialize(
    typing = JsonSerialize.Typing.DYNAMIC)
public class EveKitUserAuthSource implements UserAuthSource {
  private static final Logger log  = Logger.getLogger(EveKitUserAuthSource.class.getName());

  // Unique source ID
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
      value = "Unique source ID")
  @JsonProperty("sid")
  protected long              sid;

  // Owner to which this source is attached
  @ManyToOne
  @JoinColumn(
      name = "uid",
      referencedColumnName = "uid")
  @JsonProperty("account")
  private EveKitUserAccount   account;

  // Source name
  @ApiModelProperty(
      value = "Name of authentication source")
  @JsonProperty("source")
  private String              source;

  // "screen" name of user when authenticated using this source
  @ApiModelProperty(
      value = "Screen name for this source")
  @JsonProperty("screenName")
  private String              screenName;

  // authentication details for user when using this rouce
  @ApiModelProperty(
      value = "Source specific authentication details")
  @JsonProperty("details")
  @Lob
  @Column(
      length = 102400)
  private String              details;

  // Date when this source was last used
  @ApiModelProperty(
      value = "Last time (milliseconds UTC) this source was used to authenticate")
  @JsonProperty("last")
  private long                last = -1;

  public EveKitUserAccount getUserAccount() {
    return account;
  }

  @Override
  public EveKitUserAccount getOwner() {
    return account;
  }

  @Override
  public String getSource() {
    return source;
  }

  @Override
  public String getScreenName() {
    return screenName;
  }

  public void setScreenName(
                            String screenName) {
    this.screenName = screenName;
  }

  public String getDetails() {
    return details;
  }

  public void setDetails(
                         String details) {
    this.details = details;
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
    result = prime * result + ((account == null) ? 0 : account.hashCode());
    result = prime * result + ((details == null) ? 0 : details.hashCode());
    result = prime * result + (int) (last ^ (last >>> 32));
    result = prime * result + ((screenName == null) ? 0 : screenName.hashCode());
    result = prime * result + (int) (sid ^ (sid >>> 32));
    result = prime * result + ((source == null) ? 0 : source.hashCode());
    return result;
  }

  @Override
  public boolean equals(
                        Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    EveKitUserAuthSource other = (EveKitUserAuthSource) obj;
    if (account == null) {
      if (other.account != null) return false;
    } else if (!account.equals(other.account)) return false;
    if (details == null) {
      if (other.details != null) return false;
    } else if (!details.equals(other.details)) return false;
    if (last != other.last) return false;
    if (screenName == null) {
      if (other.screenName != null) return false;
    } else if (!screenName.equals(other.screenName)) return false;
    if (sid != other.sid) return false;
    if (source == null) {
      if (other.source != null) return false;
    } else if (!source.equals(other.source)) return false;
    return true;
  }

  @Override
  public String toString() {
    return "EveKitUserAuthSource [sid=" + sid + ", account=" + account + ", source=" + source + ", screenName=" + screenName + ", details=" + details
        + ", last=" + last + "]";
  }

  public static EveKitUserAuthSource getSource(
                                               final EveKitUserAccount acct,
                                               final String source) throws AuthSourceNotFoundException, IOException {
    try {
      return EveKitUserAccountProvider.getFactory().runTransaction(() -> {
          TypedQuery<EveKitUserAuthSource> getter = EveKitUserAccountProvider.getFactory().getEntityManager()
              .createNamedQuery("EveKitUserAuthSource.findByAcctAndSource", EveKitUserAuthSource.class);
          getter.setParameter("account", acct);
          getter.setParameter("source", source);
          try {
            return getter.getSingleResult();
          } catch (NoResultException e) {
            throw new AuthSourceNotFoundException();
          }
        });
    } catch (Exception e) {
      if (e.getCause() instanceof AuthSourceNotFoundException) throw (AuthSourceNotFoundException) e.getCause();
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  public static List<EveKitUserAuthSource> getAllSources(
                                                         final EveKitUserAccount acct) throws IOException {
    try {
      return EveKitUserAccountProvider.getFactory().runTransaction(() -> {
          TypedQuery<EveKitUserAuthSource> getter = EveKitUserAccountProvider.getFactory().getEntityManager()
              .createNamedQuery("EveKitUserAuthSource.allSourcesByAcct", EveKitUserAuthSource.class);
          getter.setParameter("account", acct);
          return getter.getResultList();
        });
    } catch (Exception e) {
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  public static EveKitUserAuthSource getLastUsedSource(
                                                       final EveKitUserAccount acct) throws IOException {
    try {
      return EveKitUserAccountProvider.getFactory().runTransaction(() -> {
          TypedQuery<EveKitUserAuthSource> getter = EveKitUserAccountProvider.getFactory().getEntityManager()
              .createNamedQuery("EveKitUserAuthSource.allSourcesByAcct", EveKitUserAuthSource.class);
          getter.setParameter("account", acct);
          getter.setMaxResults(1);
          List<EveKitUserAuthSource> results = getter.getResultList();
          return results.isEmpty() ? null : results.get(0);
        });
    } catch (Exception e) {
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  public static List<EveKitUserAuthSource> getAll() throws IOException {
    try {
      return EveKitUserAccountProvider.getFactory().runTransaction(() -> {
          TypedQuery<EveKitUserAuthSource> getter = EveKitUserAccountProvider.getFactory().getEntityManager().createNamedQuery("EveKitUserAuthSource.all",
                                                                                                                               EveKitUserAuthSource.class);
          return getter.getResultList();
        });
    } catch (Exception e) {
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  public static EveKitUserAuthSource getBySourceScreenname(
                                                           final String source,
                                                           final String screenName) throws IOException {
    try {
      return EveKitUserAccountProvider.getFactory().runTransaction(() -> {
          TypedQuery<EveKitUserAuthSource> getter = EveKitUserAccountProvider.getFactory().getEntityManager()
              .createNamedQuery("EveKitUserAuthSource.allBySourceAndScreenname", EveKitUserAuthSource.class);
          getter.setParameter("source", source);
          getter.setParameter("screenname", screenName);
          getter.setMaxResults(1);
          List<EveKitUserAuthSource> results = getter.getResultList();
          return results.isEmpty() ? null : results.get(0);
        });
    } catch (Exception e) {
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  public static EveKitUserAuthSource updateAccount(
                                                   final EveKitUserAuthSource src,
                                                   final EveKitUserAccount newAccount) throws IOException {
    try {
      return EveKitUserAccountProvider.getFactory().runTransaction(() -> {
          src.account = newAccount;
          return EveKitUserAccountProvider.getFactory().getEntityManager().merge(src);
        });
    } catch (Exception e) {
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  public static EveKitUserAuthSource createSource(
                                                  final EveKitUserAccount owner,
                                                  final String source,
                                                  final String screenName,
                                                  final String details) throws IOException {
    try {
      return EveKitUserAccountProvider.getFactory().runTransaction(() -> {
          try {
            // Return the source if we already have one
            return getSource(owner, source);
          } catch (AuthSourceNotFoundException e) {
            // Otherwise, create a new source
            EveKitUserAuthSource result = new EveKitUserAuthSource();
            result.account = owner;
            result.source = source;
            result.setScreenName(screenName);
            result.setDetails(details);
            result.setLast(OrbitalProperties.getCurrentTime());
            return EveKitUserAccountProvider.getFactory()
                                            .getEntityManager()
                                            .merge(result);
          }
        });
    } catch (Exception e) {
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  public static void removeSourceIfExists(
                                          final EveKitUserAccount owner,
                                          final String source) throws IOException {
    try {
      EveKitUserAccountProvider.getFactory().runTransaction(() -> {
          try {
            EveKitUserAuthSource result = getSource(owner, source);
            EveKitUserAccountProvider.getFactory()
                                                         .getEntityManager()
                                                         .remove(result);
          } catch (AuthSourceNotFoundException e) {
            // ignore
          }
        });
    } catch (Exception e) {
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  public static EveKitUserAuthSource touch(
                                           final EveKitUserAuthSource source) throws IOException {
    try {
      return EveKitUserAccountProvider.getFactory().runTransaction(() -> {
          source.setLast(OrbitalProperties.getCurrentTime());
          return EveKitUserAccountProvider.getFactory().getEntityManager().merge(source);
        });
    } catch (Exception e) {
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  @Override
  public String getBody() {
    return details;
  }

  @Override
  public void touch() {
    try {
      touch(this);
    } catch (IOException e) {
      // ignore
    }
  }

  @Override
  public void updateAccount(
                            UserAccount existing) {
    assert existing instanceof EveKitUserAccount;
    try {
      updateAccount(this, (EveKitUserAccount) existing);
    } catch (IOException e) {
      // ignore
    }
  }

  @Override
  public Date getLastSignOn() {
    return new Date(last);
  }

}
