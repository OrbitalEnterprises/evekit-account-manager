package enterprises.orbital.evekit.account;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import enterprises.orbital.base.OrbitalProperties;
import enterprises.orbital.base.Stamper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Instances of this data object control REST access to synchronized data. There can be many keys associated with a given synchronized account. Each key may
 * differ in features like access mask and expiry.
 * <p>
 * Access model:
 * <ol>
 * <li>Caller (via REST) passes a key ID and hash along with the requested operation.
 * <li>Server looks up key ID and hash and resolves the appropriate SynchronizedAccount and key access mask.
 * <li>Requested operation is checked against access mask.
 * <li>If operation denied, caller receives error.
 * <li>Else, operation performed and data returned.
 * </ol>
 */
@Entity
@Table(
    name = "evekit_access_keys",
    indexes = {
        @Index(
            name = "accountIndex",
            columnList = "aid",
            unique = false),
        @Index(
            name = "accessKeyIndex",
            columnList = "accessKey",
            unique = true),
        @Index(
            name = "keyNameIndex",
            columnList = "keyName",
            unique = false)
    })
@NamedQueries({
    @NamedQuery(
        name = "SynchronizedAccountAccessKey.findByAcctAndName",
        query = "SELECT c FROM SynchronizedAccountAccessKey c where c.account = :account and c.keyName = :name"),
    @NamedQuery(
        name = "SynchronizedAccountAccessKey.findByAcctAndID",
        query = "SELECT c FROM SynchronizedAccountAccessKey c where c.account = :account and c.kid = :kid"),
    @NamedQuery(
        name = "SynchronizedAccountAccessKey.findAllByAcct",
        query = "SELECT c FROM SynchronizedAccountAccessKey c where c.account = :account"),
    @NamedQuery(
        name = "SynchronizedAccountAccessKey.findByAccessKey",
        query = "SELECT c FROM SynchronizedAccountAccessKey c where c.accessKey.value = :accesskey"),
})
@ApiModel(
    description = "EveKit synchronized account access key")
@JsonIgnoreProperties({
    "randomSeed", "accessMask"
})
public class SynchronizedAccountAccessKey {
  protected static final Logger            log      = Logger.getLogger(SynchronizedAccountAccessKey.class.getName());

  protected static ThreadLocal<ByteBuffer> assembly = new ThreadLocal<ByteBuffer>() {
                                                      @Override
                                                      protected ByteBuffer initialValue() {
                                                        // Since we use the user's account name in the hash we need to
                                                        // allocate to the largest possible size allowed for a data store
                                                        // string (which is currently 500 bytes).
                                                        return ByteBuffer.allocate(550);
                                                      }
                                                    };

  public static class BigIntegerSerializer extends JsonSerializer<BigInteger> {
    @Override
    public void serialize(
                          BigInteger value,
                          JsonGenerator jgen,
                          SerializerProvider provider)
      throws IOException {
      jgen.writeString(value.toString());
    }
  };

  // Unique key ID
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
      value = "Unique key ID")
  @JsonProperty("kid")
  protected long                 kid;

  // Account which owns this key
  @ManyToOne
  @JoinColumn(
      name = "aid",
      referencedColumnName = "aid")
  @ApiModelProperty(
      value = "Key owner")
  @JsonProperty("account")
  private SynchronizedEveAccount account;

  // User readable name of this key. This should be unique for all keys on the same SynchronizedEveAccount.
  @ApiModelProperty(
      value = "Key name")
  @JsonProperty("keyName")
  private String                 keyName;

  // Integer key ID. This is unique across all keys maintained by EveKit.
  @OneToOne
  @JoinColumn(
      name = "accessKey",
      referencedColumnName = "value")
  @ApiModelProperty(
      value = "Access key")
  @JsonProperty("accessKey")
  private GeneralSequenceNumber  accessKey;

  // Fixed at the time this key is created, we use this field to randomize the hash.
  private long                   randomSeed;

  // -1 for a key which never expires, otherwise this is the time in UTC when the given key will expire.
  @ApiModelProperty(
      value = "-1 if this key never expires, otherwise the date (milliseconds UTC) when this key expires")
  @JsonProperty("expiry")
  private long                   expiry = -1;

  // -1 for an unlimited key, otherwise this is limit time in UTC for historic queries. That is, records (e.g. wallet transactions) with a date before this
  // time are not accessible by this key. Note that limit is a reserved word, so we need to escape it in the column
  // definition.
  @ApiModelProperty(
      value = "-1 if this key is unlimited, otherwise the date (milliseconds UTC) before which data may not be accessed")
  @JsonProperty("limit")
  @Column(
      name = "\"limit\"")
  private long                   limit  = -1;

  // Mask which controls access for this key.
  @Lob
  private byte[]                 accessMask;

  // Hash computed when needed for communication to owning user.
  @Transient
  @ApiModelProperty(
      value = "Access credential")
  @JsonProperty("credential")
  private String                 credential;

  // Integer value of mask. Used for display purposes.
  @Transient
  @ApiModelProperty(
      value = "Access key mask")
  @JsonProperty("maskValue")
  @JsonSerialize(
      using = BigIntegerSerializer.class)
  private BigInteger             maskValue;

  // String value of mask. Also used for display purposes.
  @Transient
  @ApiModelProperty(
      value = "String value of access key mask")
  @JsonProperty("maskValueString")
  private String                 maskValueString;

  public SynchronizedEveAccount getSyncAccount() {
    return account;
  }

  public void setSyncAccount(
                             SynchronizedEveAccount o) {
    account = o;
  }

  public long getAccessKey() {
    return accessKey.getValue();
  }

  public String getKeyName() {
    return keyName;
  }

  public void setKeyName(
                         String keyName) {
    this.keyName = keyName;
  }

  public long getRandomSeed() {
    return randomSeed;
  }

  public void setRandomSeed(
                            long randomSeed) {
    this.randomSeed = randomSeed;
  }

  public long getExpiry() {
    return expiry;
  }

  public void setExpiry(
                        long expiry) {
    this.expiry = expiry;
  }

  public long getLimit() {
    return limit;
  }

  public void setLimit(
                       long limit) {
    this.limit = limit;
  }

  public byte[] getAccessMask() {
    return accessMask;
  }

  public void setAccessMask(
                            byte[] accessMask) {
    this.accessMask = accessMask;
  }

  public String getCredential() {
    return credential;
  }

  public void setCredential(
                            String credential) {
    this.credential = credential;
  }

  public BigInteger getMaskValue() {
    return maskValue;
  }

  public void setMaskValue(
                           BigInteger maskValue) {
    this.maskValue = maskValue;
  }

  public String getMaskValueString() {
    return maskValueString;
  }

  public void setMaskValueString(
                                 String maskValueString) {
    this.maskValueString = maskValueString;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((accessKey == null) ? 0 : accessKey.hashCode());
    result = prime * result + Arrays.hashCode(accessMask);
    result = prime * result + ((account == null) ? 0 : account.hashCode());
    result = prime * result + ((credential == null) ? 0 : credential.hashCode());
    result = prime * result + (int) (expiry ^ (expiry >>> 32));
    result = prime * result + ((keyName == null) ? 0 : keyName.hashCode());
    result = prime * result + (int) (kid ^ (kid >>> 32));
    result = prime * result + (int) (limit ^ (limit >>> 32));
    result = prime * result + (int) (randomSeed ^ (randomSeed >>> 32));
    return result;
  }

  @Override
  public boolean equals(
                        Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    SynchronizedAccountAccessKey other = (SynchronizedAccountAccessKey) obj;
    if (accessKey == null) {
      if (other.accessKey != null) return false;
    } else if (!accessKey.equals(other.accessKey)) return false;
    if (!Arrays.equals(accessMask, other.accessMask)) return false;
    if (account == null) {
      if (other.account != null) return false;
    } else if (!account.equals(other.account)) return false;
    if (credential == null) {
      if (other.credential != null) return false;
    } else if (!credential.equals(other.credential)) return false;
    if (expiry != other.expiry) return false;
    if (keyName == null) {
      if (other.keyName != null) return false;
    } else if (!keyName.equals(other.keyName)) return false;
    if (kid != other.kid) return false;
    if (limit != other.limit) return false;
    if (randomSeed != other.randomSeed) return false;
    return true;
  }

  @Override
  public String toString() {
    return "SynchronizedAccountAccessKey [kid=" + kid + ", account=" + account + ", keyName=" + keyName + ", accessKey=" + accessKey + ", randomSeed="
        + randomSeed + ", expiry=" + expiry + ", limit=" + limit + ", accessMask=" + Arrays.toString(accessMask) + ", credential=" + credential + ", maskValue="
        + maskValue + ", maskValueString=" + maskValueString + "]";
  }

  /**
   * Create a new account access key. The key is created in a transaction and will succeed as long as a key with the same name does not already exist for the
   * given SynchronizedEveAccount.
   *
   * @param account the parent for the new key.
   * @param keyName    the name of the new key.
   * @param expiry     the time, in UTC, when the key expires, or NULL for a key which never expires.
   * @param limit      the time, in UTC, which marks the oldest records available to this key, or NUL for an unlimited key.
   * @param accessMask the access mask for this key.
   * @return the newly created key
   * @throws AccessKeyCreationException if a key with the given name already exists for this account.
   */
  public static SynchronizedAccountAccessKey createKey(
      final SynchronizedEveAccount account,
      final String keyName,
      final long expiry,
      final long limit,
      final byte[] accessMask)
      throws AccessKeyCreationException, IOException {
    try {
      return EveKitUserAccountProvider.getFactory()
                                      .runTransaction(() -> {
                                        try {
                                          getKeyByOwnerAndName(account, keyName);
                                          // if no exception is thrown, then key already exists, throw exception
                                          throw new AccessKeyCreationException("Access key with name " + keyName + " already exists");
                                        } catch (AccessKeyNotFoundException e) {
                                          // Key does not exit, creat it
                                          long seed = new Random(OrbitalProperties.getCurrentTime()).nextLong();
                                          SynchronizedAccountAccessKey result = new SynchronizedAccountAccessKey();
                                          result.account = account;
                                          result.keyName = keyName;
                                          result.expiry = expiry;
                                          result.limit = limit;
                                          result.accessMask = accessMask;
                                          result.randomSeed = seed;
                                          result.accessKey = GeneralSequenceNumber.create();
                                          return EveKitUserAccountProvider.getFactory()
                                                                          .getEntityManager()
                                                                          .merge(result);
                                        }
                                      });
    } catch (Exception e) {
      if (e.getCause() instanceof AccountNotFoundException) throw (AccessKeyCreationException) e.getCause();
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  /**
   * Retrieve access key by name.
   *
   * @param owner account which owns key.
   * @param name  name of key to retrieve.
   * @return the requested key.
   * @throws AccessKeyNotFoundException if the requested key can not be found.
   * @throws IOException                on any database error.
   */
  public static SynchronizedAccountAccessKey getKeyByOwnerAndName(
      final SynchronizedEveAccount owner,
      final String name)
      throws AccessKeyNotFoundException, IOException {
    try {
      return EveKitUserAccountProvider.getFactory()
                                      .runTransaction(() -> {
                                        TypedQuery<SynchronizedAccountAccessKey> getter = EveKitUserAccountProvider.getFactory()
                                                                                                                   .getEntityManager()
                                                                                                                   .createNamedQuery("SynchronizedAccountAccessKey.findByAcctAndName", SynchronizedAccountAccessKey.class);
                                        getter.setParameter("account", owner);
                                        getter.setParameter("name", name);
                                        try {
                                          return getter.getSingleResult();
                                        } catch (NoResultException e) {
                                          throw new AccessKeyNotFoundException();
                                        }
                                      });
    } catch (Exception e) {
      if (e.getCause() instanceof AccessKeyNotFoundException) throw (AccessKeyNotFoundException) e.getCause();
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  /**
   * Retrieve access key by ID.
   *
   * @param owner account which owns key.
   * @param kid   ID of key to retrieve.
   * @return the requested key.
   * @throws AccessKeyNotFoundException if the requested key can not be found.
   * @throws IOException                on any database error.
   */
  public static SynchronizedAccountAccessKey getKeyByOwnerAndID(
      final SynchronizedEveAccount owner,
      final long kid)
      throws AccessKeyNotFoundException, IOException {
    try {
      return EveKitUserAccountProvider.getFactory()
                                      .runTransaction(() -> {
                                        TypedQuery<SynchronizedAccountAccessKey> getter = EveKitUserAccountProvider.getFactory()
                                                                                                                   .getEntityManager()
                                                                                                                   .createNamedQuery("SynchronizedAccountAccessKey.findByAcctAndID", SynchronizedAccountAccessKey.class);
                                        getter.setParameter("account", owner);
                                        getter.setParameter("kid", kid);
                                        try {
                                          return getter.getSingleResult();
                                        } catch (NoResultException e) {
                                          throw new AccessKeyNotFoundException();
                                        }
                                      });
    } catch (Exception e) {
      if (e.getCause() instanceof AccessKeyNotFoundException) throw (AccessKeyNotFoundException) e.getCause();
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  /**
   * Retrieve access key by key ID.
   *
   * @param accessKey the key ID for the key to retrieve.
   * @return the requested key.
   * @throws AccessKeyNotFoundException if the requested key can not be found.
   * @throws IOException                on any database error.
   */
  public static SynchronizedAccountAccessKey getKeyByAccessKey(
      final long accessKey)
      throws AccessKeyNotFoundException, IOException {
    try {
      return EveKitUserAccountProvider.getFactory()
                                      .runTransaction(() -> {
                                        TypedQuery<SynchronizedAccountAccessKey> getter = EveKitUserAccountProvider.getFactory()
                                                                                                                   .getEntityManager()
                                                                                                                   .createNamedQuery("SynchronizedAccountAccessKey.findByAccessKey", SynchronizedAccountAccessKey.class);
                                        getter.setParameter("accesskey", accessKey);
                                        try {
                                          return getter.getSingleResult();
                                        } catch (NoResultException e) {
                                          throw new AccessKeyNotFoundException();
                                        }
                                      });
    } catch (Exception e) {
      if (e.getCause() instanceof AccessKeyNotFoundException) throw (AccessKeyNotFoundException) e.getCause();
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  /**
   * Retrieve all access keys for the given account.
   *
   * @param owner account for which keys will be retrieved.
   * @return the list of requested keys.
   * @throws IOException on any database error.
   */
  public static List<SynchronizedAccountAccessKey> getAllKeys(
      final SynchronizedEveAccount owner)
      throws IOException {
    try {
      return EveKitUserAccountProvider.getFactory()
                                      .runTransaction(() -> {
                                        TypedQuery<SynchronizedAccountAccessKey> getter = EveKitUserAccountProvider.getFactory()
                                                                                                                   .getEntityManager()
                                                                                                                   .createNamedQuery("SynchronizedAccountAccessKey.findAllByAcct", SynchronizedAccountAccessKey.class);
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
   * Delete access key.
   *
   * @param owner owner of key to delete.
   * @param kid   ID of key to delete
   * @throws AccessKeyNotFoundException if the key to delete can not be found.
   * @throws IOException                on any database error.
   */
  public static void deleteKey(
      final SynchronizedEveAccount owner,
      final long kid)
      throws AccessKeyNotFoundException, IOException {
    try {
      EveKitUserAccountProvider.getFactory()
                               .runTransaction(() -> {
                                 SynchronizedAccountAccessKey key = getKeyByOwnerAndID(owner, kid);
                                 EveKitUserAccountProvider.getFactory()
                                                          .getEntityManager()
                                                          .remove(key);
                               });
    } catch (Exception e) {
      if (e.getCause() instanceof AccessKeyNotFoundException) throw (AccessKeyNotFoundException) e.getCause();
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  /**
   * Update access key settings.
   *
   * @param owner      account which owns key to be updated.
   * @param keyName    name of key to be updated.
   * @param newKeyName new name of key
   * @param expiry     new expiry for key
   * @param limit      new limit for key
   * @param accessMask new access mask for key
   * @throws AccessKeyNotFoundException if key to update can not be found.
   * @throws AccessKeyUpdateException   if key name is changed to a name already in use.
   * @throws IOException                on any database error
   */
  public static SynchronizedAccountAccessKey updateKey(
      final SynchronizedEveAccount owner,
      final String keyName,
      final String newKeyName,
      final long expiry,
      final long limit,
      final byte[] accessMask)
      throws AccessKeyNotFoundException, AccessKeyUpdateException, IOException {
    try {
      return EveKitUserAccountProvider.getFactory()
                                      .runTransaction(() -> {
                                        // Retrieve key to update
                                        SynchronizedAccountAccessKey key = getKeyByOwnerAndName(owner, keyName);
                                        if (!keyName.equals(newKeyName)) {
                                          // We're changing key name, make sure the new name is not already in use
                                          try {
                                            getKeyByOwnerAndName(owner, newKeyName);
                                            throw new AccessKeyUpdateException("Key already exists with new name: " + newKeyName);
                                          } catch (AccessKeyNotFoundException e) {
                                            // Key not in use - continue
                                          }
                                          key.keyName = newKeyName;
                                        }
                                        // Make other key changes
                                        key.setExpiry(expiry);
                                        key.setLimit(limit);
                                        key.setAccessMask(accessMask);
                                        return EveKitUserAccountProvider.getFactory()
                                                                        .getEntityManager()
                                                                        .merge(key);
                                      });
    } catch (Exception e) {
      if (e.getCause() instanceof AccessKeyNotFoundException) throw (AccessKeyNotFoundException) e.getCause();
      if (e.getCause() instanceof AccessKeyUpdateException) throw (AccessKeyUpdateException) e.getCause();
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  public void generateCredential() {
    setCredential(generateHash(this));
  }

  public void generateMaskValue() {
    BigInteger value = new BigInteger("0");

    for (AccountAccessMask next : AccountAccessMask.values()) {
      if (AccountAccessMask.isAccessAllowed(accessMask, next)) {
        value = value.flipBit(next.getMaskValue());
      }
    }

    setMaskValue(value);
  }

  public void generateMaskValueString() {
    setMaskValueString(AccountAccessMask.stringifyMask(accessMask));
  }

  public static String generateHash(
                                    SynchronizedAccountAccessKey ref) {
    ByteBuffer assemble = assembly.get();
    assemble.clear();

    // Assemble contents. The hash consists of:
    // - access key ID
    // - user unique ID
    // - random seed
    // Note that things like synchronized account name and access key name can be changed.
    // It should be possible to change these things without changing the hash, so we don't include
    // those items in the computation of the hash.
    assemble.putLong(ref.getAccessKey());
    assemble.put(ref.getSyncAccount().getUserAccount().getUid().getBytes());
    assemble.putLong(ref.getRandomSeed());
    assemble.limit(assemble.position());
    assemble.rewind();

    return Stamper.digest(assemble);
  }

  /**
   * Verify hash and return associated access key, or null if the has is incorrect.
   *
   * @param keyID         submitted key ID
   * @param submittedHash submitted hash
   * @return the associated access key if the hash is verified, otherwise null
   * @throws AccessKeyNotFoundException if no key could be found with the given key ID.
   * @throws IOException                on any database error.
   */
  public static SynchronizedAccountAccessKey checkHash(
      long keyID,
      String submittedHash)
      throws AccessKeyNotFoundException, IOException {
    SynchronizedAccountAccessKey accessKey = getKeyByAccessKey(keyID);
    return generateHash(accessKey).equals(submittedHash) ? accessKey : null;
  }

  /**
   * Remove all access keys for a synchronized account.
   *
   * @param acct account from which keys will be removed.
   * @throws IOException on any database error.
   */
  public static void remove(
      final SynchronizedEveAccount acct)
      throws IOException {
    try {
      EveKitUserAccountProvider.getFactory()
                               .runTransaction(() -> {
                                 for (SynchronizedAccountAccessKey next : getAllKeys(acct)) {
                                   EveKitUserAccountProvider.getFactory()
                                                            .getEntityManager()
                                                            .remove(next);
                                 }
                               });
    } catch (Exception e) {
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

}
