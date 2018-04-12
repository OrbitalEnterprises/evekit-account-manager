package enterprises.orbital.evekit.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.scribejava.core.model.OAuth2AccessToken;
import enterprises.orbital.base.OrbitalProperties;
import enterprises.orbital.base.PersistentPropertyKey;
import enterprises.orbital.evekit.model.SyncTracker;
import enterprises.orbital.oauth.EVEAuthHandler;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Synchronized EVE account (SEV).
 * <p>
 * A SEV can be in one of several states as determined by which credentials are associated with the account:
 * <p>
 * NONE - no credentials have been assigned.  Character and corporation information is undefined.
 * XML - only an XML credential is assigned.  Character and corporation information reflects the XML credential.
 * ESI - only an ESI credential is assigned.  Character and corporation information reflects the ESI credential.
 * BOTH - both an XML and ESI credential are assigned.  Character and corporation information must agree
 * with both credentials.
 * <p>
 * The operations below allow the following transitions:
 * <p>
 * NONE -&gt; XML - add XML credential
 * NONE -&gt; ESI - add ESI credential
 * XML -&gt; BOTH - add ESI credential where an XML credential already exists
 * ESI -&gt; BOTH - add XML credential where an ESI credential already exists
 * BOTH -&gt; XML - remove ESI credential where an XML credential already exists
 * BOTH -&gt; ESI - remove XML credential where an ESI credential already exists
 * XML -&gt; NONE - remove XML credential
 * ESI -&gt; NONE - remove ESI credential
 */
@Entity
@Table(
    name = "evekit_sync_accounts",
    indexes = {
        @Index(
            name = "accountIndex",
            columnList = "uid"),
        @Index(
            name = "nameIndex",
            columnList = "name"),
        @Index(
            name = "deleteableIndex",
            columnList = "markedForDelete"),
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
        name = "SynchronizedEveAccount.findAll",
        query = "SELECT c FROM SynchronizedEveAccount c where c.markedForDelete = -1"),
    @NamedQuery(
        name = "SynchronizedEveAccount.findAllIncludeMarked",
        query = "SELECT c FROM SynchronizedEveAccount c"),
})
@ApiModel(
    description = "EveKit synchronized account")
public class SynchronizedEveAccount implements PersistentPropertyKey<String> {
  private static final Logger log = Logger.getLogger(SynchronizedEveAccount.class.getName());

  // Not configurable for now.
  private static final int TOKEN_LOCK_RETRY_ATTEMPTS = 3;

  // Unique account ID
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
  protected long aid;

  // User which owns this account
  @ManyToOne
  @JoinColumn(
      name = "uid",
      referencedColumnName = "uid")
  @JsonProperty("userAccount")
  private EveKitUserAccount userAccount;

  // Date when this account was created
  @ApiModelProperty(
      value = "Date (milliseconds UTC) when this account was created")
  @JsonProperty("created")
  private long created = -1;

  // Account name
  @ApiModelProperty(
      value = "Account name")
  @JsonProperty("name")
  private String name;

  // True if account synchronizes characters, false otherwise
  @ApiModelProperty(
      value = "True if this is a character account, false for a corporation account")
  @JsonProperty("characterType")
  private boolean characterType;

  // Account character and corporation information.  This information will not be set until a valid
  // credential has been added for this account.
  @ApiModelProperty(
      value = "Character ID to use for accessing the EVE XML API")
  @JsonProperty("eveCharacterID")
  private long eveCharacterID = -1;
  @ApiModelProperty(
      value = "Character name of character used for access")
  @JsonProperty("eveCharacterName")
  private String eveCharacterName;
  @ApiModelProperty(
      value = "Corporation ID of character used for access")
  @JsonProperty("eveCorporationID")
  private long eveCorporationID = -1;
  @ApiModelProperty(
      value = "Corporation Name of character used for access")
  @JsonProperty("eveCorporationName")
  private String eveCorporationName;

  // The last time this account was synchronized at the server.
  @Transient
  @ApiModelProperty(
      value = "Date (milliseconds UTC) when this account was last synchronized")
  @JsonProperty("lastSynchronized")
  private long lastSynchronized = -1;

  // -1 if not marked for delete, otherwise expected delete time
  @ApiModelProperty(
      value = "If greater than 0, then the date (milliseconds UTC) when this account was marked for deletion")
  @JsonProperty("markedForDelete")
  private long markedForDelete = -1;

  // Credentials.  Until mid 2018, a sync account may have both an XML and ESI credential.  When added, we
  // verify that credentials are consistent and refer to the same character ID.  After mid 2018, XML credentials
  // will be retired as the servers will be shut down.

  //////////////////////////////
  // XML API credentials
  //////////////////////////////

  // XML API key
  @ApiModelProperty(
      value = "EVE XML API access key")
  @JsonProperty("eveKey")
  private int eveKey = -1;

  // XML API VCode
  @ApiModelProperty(
      value = "EVE XML API access vcode")
  @JsonProperty("eveVCode")
  private String eveVCode;

  //////////////////////////////
  // ESI credentials
  //////////////////////////////

  // Space delimited list of scopes attached to this key when it was created
  @Lob
  @Column(
      length = 102400)
  @JsonProperty("scopes")
  private String scopes;

  // Latest access token
  private String accessToken;

  // Expiry date (millis UTC) of access token
  @JsonProperty("accessTokenExpiry")
  private long accessTokenExpiry = -1;

  // Latest refresh token
  private String refreshToken;

  // True if refresh token is non-null and non-empty, false otherwise.
  // Set before returning token data to web client.
  @Transient
  @ApiModelProperty(
      value = "Valid"
  )
  @JsonProperty("valid")
  private boolean valid;

  @Transient
  private Set<String> scopeSet;

  /**
   * No argument constructor sometimes required for Hibernate.
   */
  public SynchronizedEveAccount() {}

  /**
   * Create a new account.
   *
   * @param user   owner of this account (this can never be changed).
   * @param name   name of this account (may be changed later).
   * @param ischar whether this account will a character or corporation (this can never be changed).
   */
  public SynchronizedEveAccount(EveKitUserAccount user, String name, boolean ischar) {
    this.userAccount = user;
    this.name = name;
    this.characterType = ischar;
  }

  public EveKitUserAccount getUserAccount() {
    return userAccount;
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

  public long getEveCharacterID() {
    return eveCharacterID;
  }

  public String getEveCharacterName() {
    return eveCharacterName;
  }

  public long getEveCorporationID() {
    return eveCorporationID;
  }

  public String getEveCorporationName() {
    return eveCorporationName;
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

  public int getEveKey() {
    return eveKey;
  }

  public String getEveVCode() {
    return eveVCode;
  }

  public String getScopes() {
    return scopes;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public long getAccessTokenExpiry() {
    return accessTokenExpiry;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public boolean isValid() {
    return valid;
  }

  public boolean hasScope(String scopeName) {
    // False if we've never set a scope (e.g. because we lack an actual ESI key)
    if (scopes == null) return false;
    // Don't allow scopes if we can't refresh to create a valid token
    if (refreshToken == null) return false;
    if (scopeSet == null) {
      synchronized (this) {
        if (scopeSet == null) {
          scopeSet = new HashSet<>();
          for (String scopeEntry : scopes.split(" ")) {
            scopeSet.add(scopeEntry);
          }
        }
      }
    }
    return scopeSet.contains(scopeName);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SynchronizedEveAccount that = (SynchronizedEveAccount) o;

    if (aid != that.aid) return false;
    if (created != that.created) return false;
    if (characterType != that.characterType) return false;
    return userAccount.equals(that.userAccount);
  }

  @Override
  public int hashCode() {
    int result = (int) (aid ^ (aid >>> 32));
    result = 31 * result + userAccount.hashCode();
    result = 31 * result + (int) (created ^ (created >>> 32));
    result = 31 * result + (characterType ? 1 : 0);
    return result;
  }

  @Override
  public String toString() {
    return "SynchronizedEveAccount{" +
        "aid=" + aid +
        ", userAccount=" + userAccount +
        ", created=" + created +
        ", name='" + name + '\'' +
        ", characterType=" + characterType +
        ", eveCharacterID=" + eveCharacterID +
        ", eveCharacterName='" + eveCharacterName + '\'' +
        ", eveCorporationID=" + eveCorporationID +
        ", eveCorporationName='" + eveCorporationName + '\'' +
        ", lastSynchronized=" + lastSynchronized +
        ", markedForDelete=" + markedForDelete +
        ", eveKey=" + eveKey +
        ", eveVCode='" + eveVCode + '\'' +
        ", scopes='" + scopes + '\'' +
        ", accessToken='" + accessToken + '\'' +
        ", accessTokenExpiry=" + accessTokenExpiry +
        ", refreshToken='" + refreshToken + '\'' +
        ", valid=" + valid +
        '}';
  }

  public boolean hasXMLKey() {
    return eveKey != -1;
  }

  public boolean hasESIKey() {
    return accessToken != null;
  }

  /**
   * Update the valid state of an ESI credential.  This method is normally called
   * before returning an account to a web client so that proper ESI status can
   * be displayed.
   */
  public void updateValid() {
    valid = refreshToken != null && !refreshToken.isEmpty();
  }

  /**
   * Create and commit a new synchronized account.
   *
   * @param userAccount account owner
   * @param name        name of new account (must be unique for this owner)
   * @param isChar      true if this account will sync a character, false otherwise
   * @return the new committed account
   * @throws AccountCreationException if the selected name is already in use.
   * @throws IOException              on any other error (including database errors)
   */
  public static SynchronizedEveAccount createSynchronizedEveAccount(final EveKitUserAccount userAccount,
                                                                    final String name, final boolean isChar)
      throws AccountCreationException, IOException {
    try {
      return EveKitUserAccountProvider.getFactory()
                                      .runTransaction(() -> {
                                        try {
                                          getSynchronizedAccount(userAccount, name, true);
                                          // If we get here then account already exists, throw exception
                                          throw new AccountCreationException(
                                              "Account with name " + String.valueOf(name) + " already exists");
                                        } catch (AccountNotFoundException e) {
                                          // Proceed with account creation
                                          SynchronizedEveAccount result = new SynchronizedEveAccount(userAccount, name,
                                                                                                     isChar);
                                          result.created = OrbitalProperties.getCurrentTime();
                                          return EveKitUserAccountProvider.getFactory()
                                                                          .getEntityManager()
                                                                          .merge(result);
                                        }
                                      });
    } catch (Exception e) {
      if (e.getCause() instanceof AccountCreationException) throw (AccountCreationException) e.getCause();
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  /**
   * Get a synchronized account by name.
   *
   * @param owner                  account owner
   * @param name                   name of account to retrieve
   * @param includeMarkedForDelete if true, also search accounts that are marked for deletion
   * @return the named account for the named owner, or null if the account can not be found.
   * @throws AccountNotFoundException if the specified account could not be found
   * @throws IOException              on any database error
   */
  public static SynchronizedEveAccount getSynchronizedAccount(final EveKitUserAccount owner,
                                                              final String name,
                                                              final boolean includeMarkedForDelete)
      throws AccountNotFoundException, IOException {
    try {
      return EveKitUserAccountProvider.getFactory()
                                      .runTransaction(() -> {
                                        TypedQuery<SynchronizedEveAccount> getter = EveKitUserAccountProvider.getFactory()
                                                                                                             .getEntityManager()
                                                                                                             .createNamedQuery(
                                                                                                                 includeMarkedForDelete ? "SynchronizedEveAccount.findByAcctAndNameIncludeMarked" : "SynchronizedEveAccount.findByAcctAndName",
                                                                                                                 SynchronizedEveAccount.class);
                                        getter.setParameter("account", owner);
                                        getter.setParameter("name", name);
                                        try {
                                          return getter.getSingleResult();
                                        } catch (NoResultException e) {
                                          throw new AccountNotFoundException();
                                        }
                                      });
    } catch (Exception e) {
      if (e.getCause() instanceof AccountNotFoundException) throw (AccountNotFoundException) e.getCause();
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  /**
   * Get a synchronized account by ID.
   *
   * @param owner                  account owner
   * @param id                     account ID
   * @param includeMarkedForDelete if true, also search accounts that are marked for deletion
   * @return the account with the specified ID and owner, or null if the account can not be found.
   * @throws AccountNotFoundException if the specified account could not be found
   * @throws IOException              on any database error
   */
  public static SynchronizedEveAccount getSynchronizedAccount(final EveKitUserAccount owner,
                                                              final long id,
                                                              final boolean includeMarkedForDelete)
      throws AccountNotFoundException, IOException {
    try {
      return EveKitUserAccountProvider.getFactory()
                                      .runTransaction(() -> {
                                        TypedQuery<SynchronizedEveAccount> getter = EveKitUserAccountProvider.getFactory()
                                                                                                             .getEntityManager()
                                                                                                             .createNamedQuery(
                                                                                                                 includeMarkedForDelete ? "SynchronizedEveAccount.findByAcctAndIdIncludeMarked" : "SynchronizedEveAccount.findByAcctAndId",
                                                                                                                 SynchronizedEveAccount.class);
                                        getter.setParameter("account", owner);
                                        getter.setParameter("aid", id);
                                        try {
                                          return getter.getSingleResult();
                                        } catch (NoResultException e) {
                                          throw new AccountNotFoundException();
                                        }
                                      });
    } catch (Exception e) {
      if (e.getCause() instanceof AccountNotFoundException) throw (AccountNotFoundException) e.getCause();
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  /**
   * Get all accounts for a given owner.
   *
   * @param owner                  Accounts owner
   * @param includeMarkedForDelete if true, also search accounts that are marked for deletion
   * @return the list of accounts associated with the given user
   * @throws IOException on any database error
   */
  public static List<SynchronizedEveAccount> getAllAccounts(final EveKitUserAccount owner,
                                                            final boolean includeMarkedForDelete)
      throws IOException {
    try {
      return EveKitUserAccountProvider.getFactory()
                                      .runTransaction(() -> {
                                        TypedQuery<SynchronizedEveAccount> getter = EveKitUserAccountProvider.getFactory()
                                                                                                             .getEntityManager()
                                                                                                             .createNamedQuery(
                                                                                                                 includeMarkedForDelete ? "SynchronizedEveAccount.findByAcctIncludeMarked" : "SynchronizedEveAccount.findByAcct",
                                                                                                                 SynchronizedEveAccount.class);
                                        getter.setParameter("account", owner);
                                        return getter.getResultList();
                                      });
    } catch (Exception e) {
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  /**
   * Mark an account for deletion.
   *
   * @param owner account owner
   * @param id    account ID
   * @return the account after being marked
   * @throws AccountNotFoundException if the specified account could not be found
   * @throws IOException              on any database error
   */
  public static SynchronizedEveAccount deleteAccount(final EveKitUserAccount owner,
                                                     final long id)
      throws AccountNotFoundException, IOException {
    try {
      return EveKitUserAccountProvider.getFactory()
                                      .runTransaction(() -> {
                                        SynchronizedEveAccount acct = getSynchronizedAccount(owner, id, false);
                                        if (acct == null)
                                          throw new AccountNotFoundException(
                                              "Account not found for deletion: owner=" + String.valueOf(
                                                  owner) + " id=" + id);
                                        // If already marked for delete, don't remark
                                        if (acct.getMarkedForDelete() > 0)
                                          return acct;
                                        acct.setMarkedForDelete(OrbitalProperties.getCurrentTime());
                                        return EveKitUserAccountProvider.getFactory()
                                                                        .getEntityManager()
                                                                        .merge(acct);
                                      });
    } catch (Exception e) {
      if (e.getCause() instanceof AccountNotFoundException)
        throw (AccountNotFoundException) e.getCause();
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  /**
   * Unmark an account for deletion.
   *
   * @param owner account owner
   * @param id    account ID
   * @return the account after being unmarked
   * @throws AccountNotFoundException if the specified account could not be found
   * @throws IOException              on any database error
   */
  public static SynchronizedEveAccount restoreAccount(final EveKitUserAccount owner,
                                                      final long id)
      throws AccountNotFoundException, IOException {
    try {
      return EveKitUserAccountProvider.getFactory()
                                      .runTransaction(() -> {
                                        SynchronizedEveAccount acct = getSynchronizedAccount(owner, id, true);
                                        if (acct == null)
                                          throw new AccountNotFoundException(
                                              "Account not found for restoring: owner=" + String.valueOf(
                                                  owner) + " id=" + id);
                                        acct.setMarkedForDelete(-1);
                                        return EveKitUserAccountProvider.getFactory()
                                                                        .getEntityManager()
                                                                        .merge(acct);
                                      });
    } catch (Exception e) {
      if (e.getCause() instanceof AccountNotFoundException)
        throw (AccountNotFoundException) e.getCause();
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  /**
   * Update existing account information.
   *
   * @param owner account owner
   * @param id    account ID
   * @param name  new account name
   * @return account after updates
   * @throws AccountUpdateException   if the new name conflicts with the name of another existing account for the same user
   * @throws AccountNotFoundException if the target account can not be found
   * @throws IOException              on any database error
   */
  @SuppressWarnings("Duplicates")
  public static SynchronizedEveAccount updateAccount(final EveKitUserAccount owner, final long id, final String name)
      throws AccountUpdateException, AccountNotFoundException, IOException {
    try {
      return EveKitUserAccountProvider.getFactory()
                                      .runTransaction(() -> {
                                        // No change if account with requested name does not exist
                                        SynchronizedEveAccount result = getSynchronizedAccount(owner, id, false);
                                        if (result == null)
                                          throw new AccountNotFoundException(
                                              "No account owned by " + String.valueOf(owner) + " with id: " + id);
                                        if (!name.equals(result.getName())) {
                                          // If account name is changing, then verify account with new name does not already exist
                                          try {
                                            getSynchronizedAccount(owner, name, true);
                                            // If no exception is thrown then this name exists so we can't use it
                                            throw new AccountUpdateException(
                                                "Account with target name \"" + String.valueOf(
                                                    name) + "\" already exists");
                                          } catch (AccountNotFoundException e) {
                                            // Name not in use, proceed
                                            result.setName(name);
                                          }
                                        }
                                        return update(result);
                                      });
    } catch (Exception e) {
      if (e.getCause() instanceof AccountUpdateException)
        throw (AccountUpdateException) e.getCause();
      if (e.getCause() instanceof AccountNotFoundException)
        throw (AccountNotFoundException) e.getCause();
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  /**
   * Remove XML credential from a synchronized account.  If this operation leaves the account with no credentials,
   * then character and corporation information is also cleared.
   *
   * @param owner account owner
   * @param id    account ID
   * @return the account after removal, if successful.  Otherwise, null.
   * @throws AccountNotFoundException if the target account can not be found
   * @throws IOException              on any database error
   */
  public static SynchronizedEveAccount clearXMLCredential(final EveKitUserAccount owner, final long id)
      throws AccountNotFoundException, IOException {
    // Covers transitions:
    // XML -> NONE
    // BOTH -> ESI
    try {
      return EveKitUserAccountProvider.getFactory()
                                      .runTransaction(() -> {
                                        // No change if account with requested name does not exist
                                        SynchronizedEveAccount result = getSynchronizedAccount(owner, id, false);
                                        if (result == null)
                                          throw new AccountNotFoundException(
                                              "No account owned by " + String.valueOf(owner) + " with id: " + id);
                                        result.eveKey = -1;
                                        result.eveVCode = null;
                                        if (!result.hasESIKey()) {
                                          result.eveCharacterID = -1;
                                          result.eveCharacterName = null;
                                          result.eveCorporationID = -1;
                                          result.eveCorporationName = null;
                                        }
                                        return update(result);
                                      });
    } catch (Exception e) {
      if (e.getCause() instanceof AccountNotFoundException)
        throw (AccountNotFoundException) e.getCause();
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  /**
   * Remove ESI credential from a synchronized account.  If this operation leaves the account with no credentials,
   * then character and corporation information is also cleared.
   *
   * @param owner account owner
   * @param id    account ID
   * @return the account after removal, if successful.  Otherwise, null.
   * @throws AccountNotFoundException if the target account can not be found
   * @throws IOException              on any database error
   */
  public static SynchronizedEveAccount clearESICredential(final EveKitUserAccount owner, final long id)
      throws AccountNotFoundException, IOException {
    // Covers transitions:
    // ESI -> NONE
    // BOTH -> XML
    try {
      return EveKitUserAccountProvider.getFactory()
                                      .runTransaction(() -> {
                                        // No change if account with requested name does not exist
                                        SynchronizedEveAccount result = getSynchronizedAccount(owner, id, false);
                                        if (result == null)
                                          throw new AccountNotFoundException(
                                              "No account owned by " + String.valueOf(owner) + " with id: " + id);
                                        result.accessToken = null;
                                        result.accessTokenExpiry = -1;
                                        result.refreshToken = null;
                                        result.scopes = null;
                                        if (!result.hasXMLKey()) {
                                          result.eveCharacterID = -1;
                                          result.eveCharacterName = null;
                                          result.eveCorporationID = -1;
                                          result.eveCorporationName = null;
                                        }
                                        return update(result);
                                      });
    } catch (Exception e) {
      if (e.getCause() instanceof AccountNotFoundException)
        throw (AccountNotFoundException) e.getCause();
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  /**
   * Set XML credential on an account.
   *
   * @param owner           account owner
   * @param id              account ID
   * @param key             XML API access key
   * @param vcode           XML API access verification code
   * @param characterID     character ID associated with credential
   * @param characterName   character name associated with credential
   * @param corporationID   corporation ID associated with credential
   * @param corporationName corporation name associated with credential
   * @return the account after modification, if successful.  Otherwise, null.
   * @throws AccountUpdateException   if the new char/corp information does not agree with an existing credential.
   * @throws AccountNotFoundException if the target account can not be found
   * @throws IOException              on any database error
   */
  @SuppressWarnings("Duplicates")
  public static SynchronizedEveAccount setXMLCredential(final EveKitUserAccount owner, final long id,
                                                        final int key, final String vcode,
                                                        final long characterID, final String characterName,
                                                        final long corporationID, final String corporationName)
      throws AccountUpdateException, AccountNotFoundException, IOException {
    // Covers transitions:
    // NONE -> XML
    // XML -> XML
    // ESI -> BOTH
    try {
      return EveKitUserAccountProvider.getFactory()
                                      .runTransaction(() -> {
                                        // No change if account with requested name does not exist
                                        SynchronizedEveAccount result = getSynchronizedAccount(owner, id, false);
                                        if (result == null)
                                          throw new AccountNotFoundException(
                                              "No account owned by " + String.valueOf(owner) + " with id: " + id);
                                        if (result.hasESIKey() || result.hasXMLKey()) {
                                          // Verify character and corporation does not conflict with existing credential.
                                          // Note that if an XML credential already exists, we'll allow the update as long
                                          // as the character and corporation are identical.
                                          if (characterID != result.eveCharacterID ||
                                              !characterName.equals(result.eveCharacterName) ||
                                              corporationID != result.eveCorporationID ||
                                              !corporationName.equals(result.eveCorporationName))
                                            throw new AccountUpdateException(
                                                "New char/corp information inconsistent with existing ESI credential");
                                        }
                                        result.eveKey = key;
                                        result.eveVCode = vcode;
                                        result.eveCharacterID = characterID;
                                        result.eveCharacterName = characterName;
                                        result.eveCorporationID = corporationID;
                                        result.eveCorporationName = corporationName;
                                        return update(result);
                                      });
    } catch (Exception e) {
      if (e.getCause() instanceof AccountUpdateException)
        throw (AccountUpdateException) e.getCause();
      if (e.getCause() instanceof AccountNotFoundException)
        throw (AccountNotFoundException) e.getCause();
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  /**
   * Set ESI credential on an account.
   *
   * @param owner             account owner
   * @param id                account ID
   * @param accessToken       ESI access token
   * @param accessTokenExpiry ESI access token expiry (milliseconds UTC)
   * @param refreshToken      ESI refresh token
   * @param scopes            desired scopes for token
   * @param characterID       character ID associated with credential
   * @param characterName     character name associated with credential
   * @param corporationID     corporation ID associated with credential
   * @param corporationName   corporation name associated with credential
   * @return the account after modification, if successful.  Otherwise, null.
   * @throws AccountUpdateException   if the new char/corp information does not agree with an existing credential.
   * @throws AccountNotFoundException if the target account can not be found
   * @throws IOException              on any database error
   */
  @SuppressWarnings("Duplicates")
  public static SynchronizedEveAccount setESICredential(final EveKitUserAccount owner, final long id,
                                                        final String accessToken, final long accessTokenExpiry,
                                                        final String refreshToken, final String scopes,
                                                        final long characterID, final String characterName,
                                                        final long corporationID, final String corporationName)
      throws AccountUpdateException, AccountNotFoundException, IOException {
    // Covers transitions:
    // NONE -> ESI
    // ESI -> ESI
    // XML -> BOTH
    try {
      return EveKitUserAccountProvider.getFactory()
                                      .runTransaction(() -> {
                                        // No change if account with requested name does not exist
                                        // Note that if an ESI credential already exists, we'll allow the update as long
                                        // as the character and corporation are identical.
                                        SynchronizedEveAccount result = getSynchronizedAccount(owner, id, false);
                                        if (result == null)
                                          throw new AccountNotFoundException(
                                              "No account owned by " + String.valueOf(owner) + " with id: " + id);
                                        if (result.hasXMLKey() || result.hasESIKey()) {
                                          // Verify character and corporation does not conflict
                                          if (characterID != result.eveCharacterID ||
                                              !characterName.equals(result.eveCharacterName) ||
                                              corporationID != result.eveCorporationID ||
                                              !corporationName.equals(result.eveCorporationName))
                                            throw new AccountUpdateException(
                                                "New char/corp information inconsistent with existing XML credential");
                                        }
                                        result.accessToken = accessToken;
                                        result.accessTokenExpiry = accessTokenExpiry;
                                        result.refreshToken = refreshToken;
                                        result.scopes = scopes;
                                        result.eveCharacterID = characterID;
                                        result.eveCharacterName = characterName;
                                        result.eveCorporationID = corporationID;
                                        result.eveCorporationName = corporationName;
                                        return update(result);
                                      });
    } catch (Exception e) {
      if (e.getCause() instanceof AccountUpdateException)
        throw (AccountUpdateException) e.getCause();
      if (e.getCause() instanceof AccountNotFoundException)
        throw (AccountNotFoundException) e.getCause();
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  /**
   * Get all accounts.
   *
   * @param includeMarkedForDelete if true, include accounts that are marked for deletion.
   * @return the list of marked accounts
   * @throws IOException on any database error
   */
  public static List<SynchronizedEveAccount> getAllSyncAccounts(
      final boolean includeMarkedForDelete) throws IOException {
    try {
      return EveKitUserAccountProvider.getFactory()
                                      .runTransaction(() -> {
                                        TypedQuery<SynchronizedEveAccount> getter = EveKitUserAccountProvider.getFactory()
                                                                                                             .getEntityManager()
                                                                                                             .createNamedQuery(
                                                                                                                 includeMarkedForDelete ? "SynchronizedEveAccount.findAllIncludeMarked" : "SynchronizedEveAccount.findAll",
                                                                                                                 SynchronizedEveAccount.class);
                                        return getter.getResultList();
                                      });
    } catch (Exception e) {
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  /**
   * Get all accounts marked for deletion.
   *
   * @return the list of marked accounts
   * @throws IOException on any database error
   */
  public static List<SynchronizedEveAccount> getAllMarkedForDelete() throws IOException {
    try {
      return EveKitUserAccountProvider.getFactory()
                                      .runTransaction(() -> {
                                        TypedQuery<SynchronizedEveAccount> getter = EveKitUserAccountProvider.getFactory()
                                                                                                             .getEntityManager()
                                                                                                             .createNamedQuery(
                                                                                                                 "SynchronizedEveAccount.findAllMarkedForDelete",
                                                                                                                 SynchronizedEveAccount.class);
                                        return getter.getResultList();
                                      });
    } catch (Exception e) {
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  /**
   * Remove a synchronized account including any linked account access keys.
   * If this call returns without throwing an exception, then the account was successfully removed.
   *
   * @param toRemove the account to remove
   * @throws IOException on any database error.
   */
  public static void remove(final SynchronizedEveAccount toRemove) throws IOException {
    try {
      // Remove Sync Trackers
      // Set of sync trackers could be quite large so we remove those in batches
      long lastRemoved = 0;
      do {
        lastRemoved = EveKitUserAccountProvider.getFactory()
                                               .runTransaction(() -> {
                                                 long removed = 0;
                                                 TypedQuery<SyncTracker> query = EveKitUserAccountProvider.getFactory()
                                                                                                          .getEntityManager()
                                                                                                          .createQuery(
                                                                                                              "SELECT c FROM SyncTracker c where c.account = :account",
                                                                                                              SyncTracker.class);
                                                 query.setParameter("account", toRemove);
                                                 query.setMaxResults(1000);
                                                 for (SyncTracker next : query.getResultList()) {
                                                   EveKitUserAccountProvider.getFactory()
                                                                            .getEntityManager()
                                                                            .remove(next);
                                                   removed++;
                                                 }
                                                 return removed;
                                               });
      } while (lastRemoved > 0);
      // Remove Access Keys
      EveKitUserAccountProvider.getFactory()
                               .runTransaction(() -> {
                                 TypedQuery<SynchronizedAccountAccessKey> query = EveKitUserAccountProvider.getFactory()
                                                                                                           .getEntityManager()
                                                                                                           .createQuery(
                                                                                                               "SELECT c FROM SynchronizedAccountAccessKey c where c.account = :account",
                                                                                                               SynchronizedAccountAccessKey.class);
                                 query.setParameter("account", toRemove);
                                 for (SynchronizedAccountAccessKey next : query.getResultList()) {
                                   EveKitUserAccountProvider.getFactory()
                                                            .getEntityManager()
                                                            .remove(next);
                                 }
                               });
      // Remove account
      EveKitUserAccountProvider.getFactory()
                               .runTransaction(() -> {
                                 // Refetch the account so we remove an attached instance
                                 EveKitUserAccountProvider.getFactory()
                                                          .getEntityManager()
                                                          .remove(SynchronizedEveAccount.getSynchronizedAccount(
                                                              toRemove.getUserAccount(), toRemove.getAid(), true));
                               });

    } catch (Exception e) {
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }

  }

  /**
   * Merge an account to the database.
   *
   * @param data the account to merge
   * @return the merged account
   * @throws IOException on any database error.
   */
  @SuppressWarnings("Duplicates")
  public static SynchronizedEveAccount update(final SynchronizedEveAccount data) throws IOException {
    try {
      return EveKitUserAccountProvider.getFactory()
                                      .runTransaction(() ->
                                                          EveKitUserAccountProvider.getFactory()
                                                                                   .getEntityManager()
                                                                                   .merge(data));
    } catch (Exception e) {
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      throw new IOException(e.getCause());
    }
  }

  private boolean hasLockAcquisitionException(Throwable t) {
    while (t != null) {
      if (t instanceof org.hibernate.exception.LockAcquisitionException)
        return true;
      t = t.getCause();
    }
    return false;
  }

  /**
   * Refresh the access token for this account.
   *
   * @param expiryWindow expiry window in milliseconds.  If the access token will expire within this many
   *                     milliseconds, then refresh it even if it's not expired yet.  In other words,
   *                     guarantee the access token will be valid for this many milliseconds.
   * @param eveClientID  EVE SSO authentication client ID.
   * @param eveSecretKey EVE SSO authentication secret key
   * @return an access token valid for at least "expiryWindow" milliseconds.
   * @throws IOException if the access token could not be refreshed, or a database error occurred.
   */
  @SuppressWarnings("UnnecessaryLocalVariable")
  public String refreshToken(long expiryWindow, String eveClientID, String eveSecretKey)
      throws IOException {
    // Synchronize to reduce contention when token must be refreshed
    synchronized (SynchronizedEveAccount.class) {
      SynchronizedEveAccount account = this;
      // Ensure the access token is valid, if not attempt to renew it
      if (getAccessTokenExpiry() - OrbitalProperties.getCurrentTime() < expiryWindow) {
        // Key within expiry window, refresh
        String rToken = getRefreshToken();
        if (rToken == null) throw new IOException("No valid refresh token for account: " + getAid());
        OAuth2AccessToken newToken = EVEAuthHandler.doRefresh(eveClientID, eveSecretKey, rToken);
        if (newToken == null) {
          // Invalidate refresh token.
          refreshToken = null;
          update(this);
          throw new IOException("Failed to refresh token for credential: " + getAid());
        }
        accessToken = newToken.getAccessToken();
        accessTokenExpiry = OrbitalProperties.getCurrentTime() +
            TimeUnit.MILLISECONDS.convert(newToken.getExpiresIn(), TimeUnit.SECONDS);
        refreshToken = newToken.getRefreshToken();

        // Update new refresh token.  Use retries as this lock is occasionally heavily
        // contested.
        int retries = TOKEN_LOCK_RETRY_ATTEMPTS;
        while (retries > 0) {
          try {
            retries--;
            SynchronizedEveAccount updated = update(account);
            account = updated;
            break;
          } catch (IOException x) {
            if (retries == 0 || !hasLockAcquisitionException(x)) {
              log.log(Level.SEVERE, "failed to update refresh token with retries", x);
              throw x;
            }
            log.log(Level.WARNING, "retrying lock timeout on refresh token for account: " + this);
          }
        }
      }
      return account.getAccessToken();
    }
  }

  @Override
  public String getPeristentPropertyKey(String field) {
    // Key scheme: SyncAccount.<UID>.<AID>.<field>
    return "SyncAccount." + String.valueOf(userAccount.getID()) + "." + String.valueOf(aid) + "." + field;
  }
}
