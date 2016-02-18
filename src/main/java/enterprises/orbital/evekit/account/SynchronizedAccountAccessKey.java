package enterprises.orbital.evekit.account;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.TypedQuery;

import enterprises.orbital.base.OrbitalProperties;
import enterprises.orbital.base.Stamper;
import enterprises.orbital.db.ConnectionFactory.RunInTransaction;
import enterprises.orbital.db.ConnectionFactory.RunInVoidTransaction;

/**
 * Instances of this data object control REST access to synchronized data. There can be many keys associated with a given synchronized account. Each key may
 * differ in features like access mask and expiry.
 * 
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
@Table(name = "evekit_access_keys", indexes = {
    @Index(name = "accountIndex", columnList = "aid", unique = false), @Index(name = "accessKeyIndex", columnList = "accessKey", unique = true),
    @Index(name = "keyNameIndex", columnList = "keyName", unique = false)
})
@NamedQueries({
    @NamedQuery(
        name = "SynchronizedAccountAccessKey.findByAcctAndName",
        query = "SELECT c FROM SynchronizedAccountAccessKey c where c.account = :account and c.keyName = :name"),
    @NamedQuery(name = "SynchronizedAccountAccessKey.findAllByAcct", query = "SELECT c FROM SynchronizedAccountAccessKey c where c.account = :account"),
    @NamedQuery(name = "SynchronizedAccountAccessKey.findByAccessKey", query = "SELECT c FROM SynchronizedAccountAccessKey c where c.accessKey = :accesskey"),
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

  // Unique key ID
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ek_seq")
  @SequenceGenerator(name = "ek_seq", initialValue = 100000, allocationSize = 10)
  protected long                           kid;
  // Account which owns this key
  @ManyToOne
  @JoinColumn(name = "aid", referencedColumnName = "aid")
  private SynchronizedEveAccount           account;
  // User readable name of this key. This should be unique for all keys on the same SynchronizedEveAccount.
  private String                           keyName;
  // Integer key ID. This is unique across all keys maintained by EveKit.
  @OneToOne
  @JoinColumn(name = "accessKey", referencedColumnName = "value")
  private GeneralSequenceNumber            accessKey;
  // Fixed at the time this key is created, we use this field to randomize the hash.
  private long                             randomSeed;
  // -1 for a key which never expires, otherwise this is the time in UTC when the given key will expire.
  private long                             expiry   = -1;
  // -1 for an unlimited key, otherwise this is limit time in UTC for historic queries. That is, records (e.g. wallet transactions) with a date before this
  // time are not accessible by this key.
  private long                             limit    = -1;
  // Mask which controls access for this key.
  @Lob
  private byte[]                           accessMask;
  // Hash computed when needed for communication to owning user.
  @Transient
  private String                           credential;
  // Integer value of mask. Used for display purposes.
  @Transient
  private BigInteger                       maskValue;

  public SynchronizedEveAccount getSyncAccount() {
    return account;
  }

  public void setSyncAccount(SynchronizedEveAccount o) {
    account = o;
  }

  public long getAccessKey() {
    return accessKey.getValue();
  }

  public String getKeyName() {
    return keyName;
  }

  public void setKeyName(String keyName) {
    this.keyName = keyName;
  }

  public long getRandomSeed() {
    return randomSeed;
  }

  public void setRandomSeed(long randomSeed) {
    this.randomSeed = randomSeed;
  }

  public long getExpiry() {
    return expiry;
  }

  public void setExpiry(long expiry) {
    this.expiry = expiry;
  }

  public long getLimit() {
    return limit;
  }

  public void setLimit(long limit) {
    this.limit = limit;
  }

  public byte[] getAccessMask() {
    return accessMask;
  }

  public void setAccessMask(byte[] accessMask) {
    this.accessMask = accessMask;
  }

  public String getCredential() {
    return credential;
  }

  public void setCredential(String credential) {
    this.credential = credential;
  }

  public BigInteger getMaskValue() {
    return maskValue;
  }

  public void setMaskValue(BigInteger maskValue) {
    this.maskValue = maskValue;
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
    result = prime * result + ((maskValue == null) ? 0 : maskValue.hashCode());
    result = prime * result + (int) (randomSeed ^ (randomSeed >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
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
    if (maskValue == null) {
      if (other.maskValue != null) return false;
    } else if (!maskValue.equals(other.maskValue)) return false;
    if (randomSeed != other.randomSeed) return false;
    return true;
  }

  @Override
  public String toString() {
    return "SynchronizedAccountAccessKey [kid=" + kid + ", account=" + account + ", keyName=" + keyName + ", accessKey=" + accessKey + ", randomSeed="
        + randomSeed + ", expiry=" + expiry + ", limit=" + limit + ", accessMask=" + Arrays.toString(accessMask) + ", credential=" + credential + ", maskValue="
        + maskValue + "]";
  }

  /**
   * Create a new account access key. The key is created in a transaction and will succeed as long as a key with the same name does not already exist for the
   * given SynchronizedEveAccount.
   * 
   * @param accountKey
   *          the parent for the new key.
   * @param keyName
   *          the name of the new key.
   * @param expiry
   *          the time, in UTC, when the key expires, or NULL for a key which never expires.
   * @param limit
   *          the time, in UTC, which marks the oldest records available to this key, or NUL for an unlimited key.
   * @param accessMask
   *          the access mask for this key.
   * @return the newly created key, or null if a key with the given name already exists.
   */
  public static SynchronizedAccountAccessKey createKey(
                                                       final SynchronizedEveAccount accountKey,
                                                       final String keyName,
                                                       final long expiry,
                                                       final long limit,
                                                       final byte[] accessMask) throws AccessKeyCreationException {
    SynchronizedAccountAccessKey newKey = null;
    try {
      newKey = EveKitUserAccountProvider.getFactory().runTransaction(new RunInTransaction<SynchronizedAccountAccessKey>() {
        @Override
        public SynchronizedAccountAccessKey run() throws Exception {
          // Throw exception if key with given name already exists
          SynchronizedAccountAccessKey result = getKeyByOwnerAndName(accountKey, keyName);
          if (result != null) return null;
          // Looks good, make key
          long seed = new Random(OrbitalProperties.getCurrentTime()).nextLong();
          result = new SynchronizedAccountAccessKey();
          result.account = accountKey;
          result.keyName = keyName;
          result.expiry = expiry;
          result.limit = limit;
          result.accessMask = accessMask;
          result.randomSeed = seed;
          return EveKitUserAccountProvider.getFactory().getEntityManager().merge(result);
        }
      });
    } catch (Exception e) {
      log.log(Level.SEVERE, "query error", e);
    }
    if (newKey == null) throw new AccessKeyCreationException("Access key with name " + keyName + " already exists");
    return newKey;
  }

  public static SynchronizedAccountAccessKey getKeyByOwnerAndName(final SynchronizedEveAccount owner, final String name) {
    try {
      return EveKitUserAccountProvider.getFactory().runTransaction(new RunInTransaction<SynchronizedAccountAccessKey>() {
        @Override
        public SynchronizedAccountAccessKey run() throws Exception {
          TypedQuery<SynchronizedAccountAccessKey> getter = EveKitUserAccountProvider.getFactory().getEntityManager()
              .createNamedQuery("SynchronizedAccountAccessKey.findByAcctAndName", SynchronizedAccountAccessKey.class);
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

  public static SynchronizedAccountAccessKey getKeyByAccessKey(final long accessKey) {
    try {
      return EveKitUserAccountProvider.getFactory().runTransaction(new RunInTransaction<SynchronizedAccountAccessKey>() {
        @Override
        public SynchronizedAccountAccessKey run() throws Exception {
          TypedQuery<SynchronizedAccountAccessKey> getter = EveKitUserAccountProvider.getFactory().getEntityManager()
              .createNamedQuery("SynchronizedAccountAccessKey.findByAccessKey", SynchronizedAccountAccessKey.class);
          getter.setParameter("acesskey", accessKey);
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

  public static List<SynchronizedAccountAccessKey> getAllKeys(final SynchronizedEveAccount owner) {
    try {
      return EveKitUserAccountProvider.getFactory().runTransaction(new RunInTransaction<List<SynchronizedAccountAccessKey>>() {
        @Override
        public List<SynchronizedAccountAccessKey> run() throws Exception {
          TypedQuery<SynchronizedAccountAccessKey> getter = EveKitUserAccountProvider.getFactory().getEntityManager()
              .createNamedQuery("SynchronizedAccountAccessKey.findAllByAcct", SynchronizedAccountAccessKey.class);
          getter.setParameter("account", owner);
          return getter.getResultList();
        }
      });
    } catch (Exception e) {
      log.log(Level.SEVERE, "query error", e);
    }
    return null;
  }

  public static void deleteKey(final SynchronizedEveAccount owner, final String name) {
    try {
      EveKitUserAccountProvider.getFactory().runTransaction(new RunInVoidTransaction() {
        @Override
        public void run() throws Exception {
          SynchronizedAccountAccessKey key = getKeyByOwnerAndName(owner, name);
          if (key != null) EveKitUserAccountProvider.getFactory().getEntityManager().remove(key);
        }
      });
    } catch (Exception e) {
      log.log(Level.SEVERE, "query error", e);
    }
  }

  public static void updateKey(
                               final SynchronizedEveAccount owner,
                               final String keyName,
                               final String newKeyName,
                               final long expiry,
                               final long limit,
                               final byte[] accessMask) throws AccessKeyCreationException {
    AccessKeyCreationException result = null;
    try {
      result = EveKitUserAccountProvider.getFactory().runTransaction(new RunInTransaction<AccessKeyCreationException>() {
        @Override
        public AccessKeyCreationException run() throws Exception {
          SynchronizedAccountAccessKey key = getKeyByOwnerAndName(owner, keyName);
          if (key == null) return null;
          if (!keyName.equals(newKeyName)) {
            // Verify no key with new name already exists.
            if (getKeyByOwnerAndName(owner, newKeyName) != null) return new AccessKeyCreationException("Key already exists with new name: " + newKeyName);
            key.keyName = newKeyName;
          }
          key.setExpiry(expiry);
          key.setLimit(limit);
          key.setAccessMask(accessMask);
          EveKitUserAccountProvider.getFactory().getEntityManager().merge(key);
          return null;
        }
      });
    } catch (Exception e) {
      log.log(Level.SEVERE, "query error", e);
    }
    if (result != null) throw result;
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

  public static String generateHash(SynchronizedAccountAccessKey ref) {
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

  public static SynchronizedAccountAccessKey checkHash(long keyID, String submittedHash) throws NoSuchKeyException {
    SynchronizedAccountAccessKey accessKey = getKeyByAccessKey(keyID);

    if (accessKey == null) {
      log.fine("Can't find any access key with ID " + keyID + ", returning false");
      throw new NoSuchKeyException("No access key found with ID: " + String.valueOf(keyID));
    }

    return generateHash(accessKey).equals(submittedHash) ? accessKey : null;
  }

  public static void remove(final SynchronizedEveAccount acct) {
    try {
      EveKitUserAccountProvider.getFactory().runTransaction(new RunInVoidTransaction() {
        @Override
        public void run() throws Exception {
          for (SynchronizedAccountAccessKey next : getAllKeys(acct)) {
            EveKitUserAccountProvider.getFactory().getEntityManager().remove(next);
          }
        }
      });
    } catch (Exception e) {
      log.log(Level.SEVERE, "query error", e);
    }
  }

}
