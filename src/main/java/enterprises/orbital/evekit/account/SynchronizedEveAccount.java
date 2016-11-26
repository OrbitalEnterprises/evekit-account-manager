package enterprises.orbital.evekit.account;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NoResultException;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;

import com.fasterxml.jackson.annotation.JsonProperty;

import enterprises.orbital.base.OrbitalProperties;
import enterprises.orbital.db.ConnectionFactory.RunInTransaction;
import enterprises.orbital.db.ConnectionFactory.RunInVoidTransaction;
import enterprises.orbital.evekit.model.SyncTracker;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Synchronized EVE account.
 */
@Entity
@Table(
    name = "evekit_sync_accounts",
    indexes = {
        @Index(
            name = "accountIndex",
            columnList = "uid",
            unique = false),
        @Index(
            name = "nameIndex",
            columnList = "name",
            unique = false),
        @Index(
            name = "autoIndex",
            columnList = "autoSynchronized",
            unique = false),
        @Index(
            name = "deleteableIndex",
            columnList = "markedForDelete",
            unique = false),
    })
@NamedQueries({
    @NamedQuery(
        name = "SynchronizedEveAccount.findByAcctAndName",
        query = "SELECT c FROM SynchronizedEveAccount c where c.userAccount = :account and c.name = :name and c.markedForDelete = -1"),
    @NamedQuery(
        name = "SynchronizedEveAccount.findByAcctAndNameIncludeMarked",
        query = "SELECT c FROM SynchronizedEveAccount c where c.userAccount = :account and c.name = :name"),
    @NamedQuery(
        name = "SynchronizedEveAccount.findByAcctAndId",
        query = "SELECT c FROM SynchronizedEveAccount c where c.userAccount = :account and c.aid = :aid and c.markedForDelete = -1"),
    @NamedQuery(
        name = "SynchronizedEveAccount.findByAcctAndIdIncludeMarked",
        query = "SELECT c FROM SynchronizedEveAccount c where c.userAccount = :account and c.aid = :aid"),
    @NamedQuery(
        name = "SynchronizedEveAccount.findByAcct",
        query = "SELECT c FROM SynchronizedEveAccount c where c.userAccount = :account and c.markedForDelete = -1"),
    @NamedQuery(
        name = "SynchronizedEveAccount.findByAcctIncludeMarked",
        query = "SELECT c FROM SynchronizedEveAccount c where c.userAccount = :account"),
    @NamedQuery(
        name = "SynchronizedEveAccount.findAllMarkedForDelete",
        query = "SELECT c FROM SynchronizedEveAccount c where c.markedForDelete > -1"),
    @NamedQuery(
        name = "SynchronizedEveAccount.findAllAutoSync",
        query = "SELECT c FROM SynchronizedEveAccount c where c.autoSynchronized = true and c.markedForDelete = -1"),
    @NamedQuery(
        name = "SynchronizedEveAccount.findAllAutoSyncIncludeMarked",
        query = "SELECT c FROM SynchronizedEveAccount c where c.autoSynchronized = true"),
})
@ApiModel(
    description = "EveKit synchronized account")
public class SynchronizedEveAccount {
  private static final Logger log              = Logger.getLogger(SynchronizedEveAccount.class.getName());

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
      value = "Unique account ID")
  @JsonProperty("aid")
  protected long              aid;
  @ManyToOne
  @JoinColumn(
      name = "uid",
      referencedColumnName = "uid")
  @JsonProperty("userAccount")
  private EveKitUserAccount   userAccount;
  @ApiModelProperty(
      value = "Date (milliseconds UTC) when this account was created")
  @JsonProperty("created")
  private long                created          = -1;
  @ApiModelProperty(
      value = "Account name")
  @JsonProperty("name")
  private String              name;
  @ApiModelProperty(
      value = "True if this is a character account, false for a corporation account")
  @JsonProperty("characterType")
  private boolean             characterType;
  @ApiModelProperty(
      value = "True if this account will auto-synchronize")
  @JsonProperty("autoSynchronized")
  private boolean             autoSynchronized;
  @ApiModelProperty(
      value = "EVE XML API access key")
  @JsonProperty("eveKey")
  private int                 eveKey;
  @ApiModelProperty(
      value = "EVE XML API access vcode")
  @JsonProperty("eveVCode")
  private String              eveVCode;
  @ApiModelProperty(
      value = "Character ID to use for accessing the EVE XML API")
  @JsonProperty("eveCharacterID")
  private long                eveCharacterID;
  @ApiModelProperty(
      value = "Character name of character used for access")
  @JsonProperty("eveCharacterName")
  private String              eveCharacterName;
  @ApiModelProperty(
      value = "Corporation ID of character used for access")
  @JsonProperty("eveCorporationID")
  private long                eveCorporationID;
  @ApiModelProperty(
      value = "Corporation Name of character used for access")
  @JsonProperty("eveCorporationName")
  private String              eveCorporationName;
  @Transient
  @ApiModelProperty(
      value = "Date (milliseconds UTC) when this account was last synchronized")
  @JsonProperty("lastSynchronized")
  private long                lastSynchronized = -1;
  // -1 if not marked for delete, otherwise expected delete time
  @ApiModelProperty(
      value = "If greater than 0, then the date (milliseconds UTC) when this account was marked for deletion")
  @JsonProperty("markedForDelete")
  private long                markedForDelete  = -1;

  public SynchronizedEveAccount() {}

  public SynchronizedEveAccount(String name, boolean ischar, boolean autoSynchronized, int eveKey, String eveVCode, long eveCharacterID,
                                String eveCharacterName, long eveCorporationID, String eveCorporationName) {
    this.name = name;
    this.characterType = ischar;
    this.autoSynchronized = autoSynchronized;
    this.eveKey = eveKey;
    this.eveVCode = eveVCode;
    this.eveCharacterID = eveCharacterID;
    this.eveCharacterName = eveCharacterName;
    this.eveCorporationID = eveCorporationID;
    this.eveCorporationName = eveCorporationName;
  }

  public EveKitUserAccount getUserAccount() {
    return userAccount;
  }

  public void setUserAccount(
                             EveKitUserAccount o) {
    userAccount = o;
  }

  public long getAid() {
    return aid;
  }

  public long getCreated() {
    return created;
  }

  public String getName() {
    return name;
  }

  public void setName(
                      String name) {
    this.name = name;
  }

  public boolean isCharacterType() {
    return characterType;
  }

  public void setCharacterType(
                               boolean characterType) {
    this.characterType = characterType;
  }

  public boolean isAutoSynchronized() {
    return autoSynchronized;
  }

  public void setAutoSynchronized(
                                  boolean autoSynchronized) {
    this.autoSynchronized = autoSynchronized;
  }

  public int getEveKey() {
    return eveKey;
  }

  public void setEveKey(
                        int eveKey) {
    this.eveKey = eveKey;
  }

  public String getEveVCode() {
    return eveVCode;
  }

  public void setEveVCode(
                          String eveVCode) {
    this.eveVCode = eveVCode;
  }

  public long getEveCharacterID() {
    return eveCharacterID;
  }

  public void setEveCharacterID(
                                long eveCharacterID) {
    this.eveCharacterID = eveCharacterID;
  }

  public String getEveCharacterName() {
    return eveCharacterName;
  }

  public void setEveCharacterName(
                                  String eveCharacterName) {
    this.eveCharacterName = eveCharacterName;
  }

  public long getEveCorporationID() {
    return eveCorporationID;
  }

  public void setEveCorporationID(
                                  long eveCorporationID) {
    this.eveCorporationID = eveCorporationID;
  }

  public String getEveCorporationName() {
    return eveCorporationName;
  }

  public void setEveCorporationName(
                                    String eveCorporationName) {
    this.eveCorporationName = eveCorporationName;
  }

  public long getLastSynchronized() {
    return lastSynchronized;
  }

  public void setLastSynchronized(
                                  long lastSynchronized) {
    this.lastSynchronized = lastSynchronized;
  }

  public long getMarkedForDelete() {
    return markedForDelete;
  }

  public void setMarkedForDelete(
                                 long markedForDelete) {
    this.markedForDelete = markedForDelete;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (aid ^ (aid >>> 32));
    result = prime * result + (autoSynchronized ? 1231 : 1237);
    result = prime * result + (characterType ? 1231 : 1237);
    result = prime * result + (int) (created ^ (created >>> 32));
    result = prime * result + (int) (eveCharacterID ^ (eveCharacterID >>> 32));
    result = prime * result + ((eveCharacterName == null) ? 0 : eveCharacterName.hashCode());
    result = prime * result + (int) (eveCorporationID ^ (eveCorporationID >>> 32));
    result = prime * result + ((eveCorporationName == null) ? 0 : eveCorporationName.hashCode());
    result = prime * result + eveKey;
    result = prime * result + ((eveVCode == null) ? 0 : eveVCode.hashCode());
    result = prime * result + (int) (markedForDelete ^ (markedForDelete >>> 32));
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((userAccount == null) ? 0 : userAccount.hashCode());
    return result;
  }

  @Override
  public boolean equals(
                        Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    SynchronizedEveAccount other = (SynchronizedEveAccount) obj;
    if (aid != other.aid) return false;
    if (autoSynchronized != other.autoSynchronized) return false;
    if (characterType != other.characterType) return false;
    if (created != other.created) return false;
    if (eveCharacterID != other.eveCharacterID) return false;
    if (eveCharacterName == null) {
      if (other.eveCharacterName != null) return false;
    } else if (!eveCharacterName.equals(other.eveCharacterName)) return false;
    if (eveCorporationID != other.eveCorporationID) return false;
    if (eveCorporationName == null) {
      if (other.eveCorporationName != null) return false;
    } else if (!eveCorporationName.equals(other.eveCorporationName)) return false;
    if (eveKey != other.eveKey) return false;
    if (eveVCode == null) {
      if (other.eveVCode != null) return false;
    } else if (!eveVCode.equals(other.eveVCode)) return false;
    if (markedForDelete != other.markedForDelete) return false;
    if (name == null) {
      if (other.name != null) return false;
    } else if (!name.equals(other.name)) return false;
    if (userAccount == null) {
      if (other.userAccount != null) return false;
    } else if (!userAccount.equals(other.userAccount)) return false;
    return true;
  }

  @Override
  public String toString() {
    return "SynchronizedEveAccount [aid=" + aid + ", userAccount=" + userAccount + ", created=" + created + ", name=" + name + ", characterType="
        + characterType + ", autoSynchronized=" + autoSynchronized + ", eveKey=" + eveKey + ", eveVCode=" + eveVCode + ", eveCharacterID=" + eveCharacterID
        + ", eveCharacterName=" + eveCharacterName + ", eveCorporationID=" + eveCorporationID + ", eveCorporationName=" + eveCorporationName
        + ", lastSynchronized=" + lastSynchronized + ", markedForDelete=" + markedForDelete + "]";
  }

  public static SynchronizedEveAccount createSynchronizedEveAccount(
                                                                    final EveKitUserAccount userAccount,
                                                                    final String name,
                                                                    final boolean isChar,
                                                                    final boolean autoSync,
                                                                    final int key,
                                                                    final String vCode,
                                                                    final long charID,
                                                                    final String charName,
                                                                    final long corpID,
                                                                    final String corpName)
    throws AccountCreationException {
    SynchronizedEveAccount newAccount = null;
    try {
      newAccount = EveKitUserAccountProvider.getFactory().runTransaction(new RunInTransaction<SynchronizedEveAccount>() {
        @Override
        public SynchronizedEveAccount run() throws Exception {
          // Throw exception if account with given name already exists
          SynchronizedEveAccount result = getSynchronizedAccount(userAccount, name, true);
          if (result != null) return null;
          result = new SynchronizedEveAccount(name, isChar, autoSync, key, vCode, charID, charName, corpID, corpName);
          result.userAccount = userAccount;
          result.created = OrbitalProperties.getCurrentTime();
          return EveKitUserAccountProvider.getFactory().getEntityManager().merge(result);
        }
      });
    } catch (Exception e) {
      log.log(Level.SEVERE, "query error", e);
    }
    if (newAccount == null) throw new AccountCreationException("Account with name " + name + " already exists");
    return newAccount;
  }

  public static SynchronizedEveAccount getSynchronizedAccount(
                                                              final EveKitUserAccount owner,
                                                              final String name,
                                                              final boolean includeMarkedForDelete) {
    try {
      return EveKitUserAccountProvider.getFactory().runTransaction(new RunInTransaction<SynchronizedEveAccount>() {
        @Override
        public SynchronizedEveAccount run() throws Exception {
          TypedQuery<SynchronizedEveAccount> getter = EveKitUserAccountProvider.getFactory().getEntityManager()
              .createNamedQuery(includeMarkedForDelete ? "SynchronizedEveAccount.findByAcctAndNameIncludeMarked" : "SynchronizedEveAccount.findByAcctAndName",
                                SynchronizedEveAccount.class);
          getter.setParameter("account", owner);
          getter.setParameter("name", name);
          try {
            return getter.getSingleResult();
          } catch (NoResultException e) {
            return null;
          }
        }
      });
    } catch (Exception e) {
      log.log(Level.SEVERE, "query error", e);
    }
    return null;
  }

  public static SynchronizedEveAccount getSynchronizedAccount(
                                                              final EveKitUserAccount owner,
                                                              final long id,
                                                              final boolean includeMarkedForDelete) {
    try {
      return EveKitUserAccountProvider.getFactory().runTransaction(new RunInTransaction<SynchronizedEveAccount>() {
        @Override
        public SynchronizedEveAccount run() throws Exception {
          TypedQuery<SynchronizedEveAccount> getter = EveKitUserAccountProvider.getFactory().getEntityManager()
              .createNamedQuery(includeMarkedForDelete ? "SynchronizedEveAccount.findByAcctAndIdIncludeMarked" : "SynchronizedEveAccount.findByAcctAndId",
                                SynchronizedEveAccount.class);
          getter.setParameter("account", owner);
          getter.setParameter("aid", id);
          try {
            return getter.getSingleResult();
          } catch (NoResultException e) {
            return null;
          }
        }
      });
    } catch (Exception e) {
      log.log(Level.SEVERE, "query error", e);
    }
    return null;
  }

  public static List<SynchronizedEveAccount> getAllAccounts(
                                                            final EveKitUserAccount owner,
                                                            final boolean includeMarkedForDelete) {
    try {
      return EveKitUserAccountProvider.getFactory().runTransaction(new RunInTransaction<List<SynchronizedEveAccount>>() {
        @Override
        public List<SynchronizedEveAccount> run() throws Exception {
          TypedQuery<SynchronizedEveAccount> getter = EveKitUserAccountProvider.getFactory().getEntityManager()
              .createNamedQuery(includeMarkedForDelete ? "SynchronizedEveAccount.findByAcctIncludeMarked" : "SynchronizedEveAccount.findByAcct",
                                SynchronizedEveAccount.class);
          getter.setParameter("account", owner);
          return getter.getResultList();
        }
      });
    } catch (Exception e) {
      log.log(Level.SEVERE, "query error", e);
    }
    return null;
  }

  public static SynchronizedEveAccount deleteAccount(
                                                     final EveKitUserAccount owner,
                                                     final long id) {
    try {
      return EveKitUserAccountProvider.getFactory().runTransaction(new RunInTransaction<SynchronizedEveAccount>() {
        @Override
        public SynchronizedEveAccount run() throws Exception {
          SynchronizedEveAccount acct = getSynchronizedAccount(owner, id, false);
          if (acct == null) {
            log.warning("Account not found for marking, ignoring: owner=" + owner + " id=" + id);
            return null;
          }
          acct.setMarkedForDelete(OrbitalProperties.getCurrentTime());
          return EveKitUserAccountProvider.getFactory().getEntityManager().merge(acct);
        }
      });
    } catch (Exception e) {
      log.log(Level.SEVERE, "query error", e);
    }
    return null;
  }

  public static SynchronizedEveAccount restoreAccount(
                                                      final EveKitUserAccount owner,
                                                      final long id) {
    try {
      return EveKitUserAccountProvider.getFactory().runTransaction(new RunInTransaction<SynchronizedEveAccount>() {
        @Override
        public SynchronizedEveAccount run() throws Exception {
          SynchronizedEveAccount acct = getSynchronizedAccount(owner, id, true);
          if (acct == null) {
            log.warning("Account not found for restoring, ignoring: owner=" + owner + " id=" + id);
            return null;
          }
          acct.setMarkedForDelete(-1);
          return EveKitUserAccountProvider.getFactory().getEntityManager().merge(acct);
        }
      });
    } catch (Exception e) {
      log.log(Level.SEVERE, "query error", e);
    }
    return null;
  }

  public static void updateAccount(
                                   final EveKitUserAccount owner,
                                   final long id,
                                   final String name,
                                   final boolean isChar,
                                   final boolean autoSync,
                                   final int key,
                                   final String vCode,
                                   final long charID,
                                   final String charName,
                                   final long corpID,
                                   final String corpName)
    throws AccountCreationException {
    AccountCreationException result = null;
    try {
      result = EveKitUserAccountProvider.getFactory().runTransaction(new RunInTransaction<AccountCreationException>() {
        @Override
        public AccountCreationException run() throws Exception {
          // No change if account with requested name does not exist
          SynchronizedEveAccount result = getSynchronizedAccount(owner, id, false);
          if (result == null) return null;
          if (!name.equals(result.getName())) {
            // If account name is changing, then verify account with new name does not already exist
            SynchronizedEveAccount check = getSynchronizedAccount(owner, name, true);
            if (check != null) return new AccountCreationException("Account with target name \"" + name + "\" already exists");
            result.setName(name);
          }
          result.setCharacterType(isChar);
          result.setAutoSynchronized(autoSync);
          result.setEveKey(key);
          result.setEveVCode(vCode);
          result.setEveCharacterID(charID);
          result.setEveCharacterName(charName);
          result.setEveCorporationID(corpID);
          result.setEveCorporationName(corpName);
          EveKitUserAccountProvider.getFactory().getEntityManager().merge(result);
          return null;
        }
      });
    } catch (Exception e) {
      log.log(Level.SEVERE, "query error", e);
    }
    if (result != null) throw result;
  }

  public static List<SynchronizedEveAccount> getAllAutoSyncAccounts(
                                                                    final boolean includeMarkedForDelete) {
    try {
      return EveKitUserAccountProvider.getFactory().runTransaction(new RunInTransaction<List<SynchronizedEveAccount>>() {
        @Override
        public List<SynchronizedEveAccount> run() throws Exception {
          TypedQuery<SynchronizedEveAccount> getter = EveKitUserAccountProvider.getFactory().getEntityManager()
              .createNamedQuery(includeMarkedForDelete ? "SynchronizedEveAccount.findAllAutoSyncIncludeMarked" : "SynchronizedEveAccount.findAllAutoSync",
                                SynchronizedEveAccount.class);
          return getter.getResultList();
        }
      });
    } catch (Exception e) {
      log.log(Level.SEVERE, "query error", e);
    }
    return null;
  }

  public static List<SynchronizedEveAccount> getAllMarkedForDelete() {
    try {
      return EveKitUserAccountProvider.getFactory().runTransaction(new RunInTransaction<List<SynchronizedEveAccount>>() {
        @Override
        public List<SynchronizedEveAccount> run() throws Exception {
          TypedQuery<SynchronizedEveAccount> getter = EveKitUserAccountProvider.getFactory().getEntityManager()
              .createNamedQuery("SynchronizedEveAccount.findAllMarkedForDelete", SynchronizedEveAccount.class);
          return getter.getResultList();
        }
      });
    } catch (Exception e) {
      log.log(Level.SEVERE, "query error", e);
    }
    return null;
  }

  public static void remove(
                            final SynchronizedEveAccount toRemove) {
    try {
      // Remove Sync Trackers
      // Set of sync trackers could be quite large so we remove those in batches
      long lastRemoved = 0;
      do {
        lastRemoved = EveKitUserAccountProvider.getFactory().runTransaction(new RunInTransaction<Long>() {
          @Override
          public Long run() throws Exception {
            long removed = 0;
            TypedQuery<SyncTracker> query = EveKitUserAccountProvider.getFactory().getEntityManager()
                .createQuery("SELECT c FROM SyncTracker c where c.account = :account", SyncTracker.class);
            query.setParameter("account", toRemove);
            query.setMaxResults(1000);
            for (SyncTracker next : query.getResultList()) {
              EveKitUserAccountProvider.getFactory().getEntityManager().remove(next);
              removed++;
            }
            return removed;
          }
        });
      } while (lastRemoved > 0);
      // Remove Access Keys
      EveKitUserAccountProvider.getFactory().runTransaction(new RunInVoidTransaction() {
        @Override
        public void run() throws Exception {
          TypedQuery<SynchronizedAccountAccessKey> query = EveKitUserAccountProvider.getFactory().getEntityManager()
              .createQuery("SELECT c FROM SynchronizedAccountAccessKey c where c.account = :account", SynchronizedAccountAccessKey.class);
          query.setParameter("account", toRemove);
          for (SynchronizedAccountAccessKey next : query.getResultList()) {
            EveKitUserAccountProvider.getFactory().getEntityManager().remove(next);
          }
        }
      });
      // Remove account
      EveKitUserAccountProvider.getFactory().runTransaction(new RunInVoidTransaction() {
        @Override
        public void run() throws Exception {
          // Refetch the account so we remove an attached instance
          EveKitUserAccountProvider.getFactory().getEntityManager()
              .remove(SynchronizedEveAccount.getSynchronizedAccount(toRemove.getUserAccount(), toRemove.getAid(), true));
        }
      });

    } catch (Exception e) {
      log.log(Level.SEVERE, "query error", e);
    }

  }

  public static SynchronizedEveAccount update(
                                              final SynchronizedEveAccount data) {
    try {
      return EveKitUserAccountProvider.getFactory().runTransaction(new RunInTransaction<SynchronizedEveAccount>() {
        @Override
        public SynchronizedEveAccount run() throws Exception {
          return EveKitUserAccountProvider.getFactory().getEntityManager().merge(data);
        }
      });
    } catch (Exception e) {
      log.log(Level.SEVERE, "query error", e);
    }
    return null;
  }

}
