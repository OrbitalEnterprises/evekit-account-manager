package enterprises.orbital.evekit.account;

import enterprises.orbital.base.OrbitalProperties;
import enterprises.orbital.base.Stamper;

import javax.persistence.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A temporary record storing a new token we intend to create.  These keys
 * are normally removed when the new access key completes setup.  If creation
 * fails for some reason, then a separate process will periodically remove
 * all stale keys.
 */
@Entity
@Table(
    name = "esi_temp_token",
    indexes = {
        @Index(
            name = "keyIndex",
            columnList = "kid",
            unique = true),
        @Index(
            name = "credIndex",
            columnList = "stateKey",
            unique = true)
    })
@NamedQueries({
    @NamedQuery(
        name = "NewESIToken.findByID",
        query = "SELECT c FROM NewESIToken c where c.kid = :kid"),
    @NamedQuery(
        name = "NewESIToken.findByCred",
        query = "SELECT c FROM NewESIToken c where c.stateKey = :cred"),
    @NamedQuery(
        name = "NewESIToken.getExpired",
        query = "SELECT c FROM NewESIToken c where c.expiry <= :expiry")
})
public class NewESIToken {
  protected static final Logger log = Logger.getLogger(NewESIToken.class.getName());

  // Life time of a temporary token while waiting for ESI credential authorization to complete
  public static final  String            PROP_TEMP_TOKEN_LIFETIME = "enterprises.orbital.evekit.tempTokenLifetime";
  public static final  long              DEF_TEMP_TOKEN_LIFETIME  = TimeUnit.MILLISECONDS.convert(10, TimeUnit.MINUTES);

  // Set to true when the cleaned thread has been started
  private static boolean cleanerStarted = false;

  public static void init() {
    synchronized (NewESIToken.class) {
      if (cleanerStarted) return;
      new Thread(() -> {
        while (true) {
          try {
            long now = OrbitalProperties.getCurrentTime();
            NewESIToken.cleanExpired(now);
            Thread.sleep(TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES));
          } catch (Throwable e) {
            // Catch everything but log it
            log.log(Level.WARNING, "caught error in state cleanup loop (ignoring)", e);
          }
        }
      }).start();
      cleanerStarted = true;
    }
  }


  protected static ThreadLocal<ByteBuffer> assembly = new ThreadLocal<ByteBuffer>() {
    @Override
    protected ByteBuffer initialValue() {
      // Since we use the user's account name in the hash we need to
      // allocate to the largest possible size allowed for a data store
      // string (which is currently 500 bytes).
      return ByteBuffer.allocate(550);
    }
  };

  // Unique temporary key ID
  @Id
  @GeneratedValue(
      strategy = GenerationType.SEQUENCE,
      generator = "ek_seq")
  @SequenceGenerator(
      name = "ek_seq",
      initialValue = 100000,
      allocationSize = 10,
      sequenceName = "account_sequence")
  private long   kid;

  // EveKitUserAccount which owns the SynchronizedEveAccount which will store this token
  @ManyToOne
  @JoinColumn(
      name = "uid",
      referencedColumnName = "uid")
  private EveKitUserAccount user;

  // SynchronizedEveAccount which will store this credential
  @ManyToOne
  @JoinColumn(
      name = "aid",
      referencedColumnName = "aid")
  private SynchronizedEveAccount account;

  // Time when request was created
  private long   createTime;

  // Time when this request will expire
  private long   expiry;

  // Space separated named scopes requested for this key
  @Lob
  @Column(
      length = 102400)
  private String scopes;

  // Fixed at the time this key is created, we use this field to randomize the hash.
  private long   randomSeed;

  // OAuth state presented as part of request
  @Lob
  @Column(
      length = 102400)
  private String stateKey;

  public long getKid() {
    return kid;
  }

  public EveKitUserAccount getUser() {
    return user;
  }

  public SynchronizedEveAccount getAccount() {
    return account;
  }

  public long getCreateTime() {
    return createTime;
  }

  public long getExpiry() {
    return expiry;
  }

  public String getScopes() {
    return scopes;
  }

  public long getRandomSeed() {
    return randomSeed;
  }

  public String getStateKey() {
    return stateKey;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    NewESIToken that = (NewESIToken) o;

    if (kid != that.kid) return false;
    if (createTime != that.createTime) return false;
    if (expiry != that.expiry) return false;
    if (randomSeed != that.randomSeed) return false;
    if (!user.equals(that.user)) return false;
    if (!account.equals(that.account)) return false;
    if (!scopes.equals(that.scopes)) return false;
    return stateKey.equals(that.stateKey);
  }

  @Override
  public int hashCode() {
    int result = (int) (kid ^ (kid >>> 32));
    result = 31 * result + user.hashCode();
    result = 31 * result + account.hashCode();
    result = 31 * result + (int) (createTime ^ (createTime >>> 32));
    result = 31 * result + (int) (expiry ^ (expiry >>> 32));
    result = 31 * result + scopes.hashCode();
    result = 31 * result + (int) (randomSeed ^ (randomSeed >>> 32));
    result = 31 * result + stateKey.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "NewESIToken{" +
        "kid=" + kid +
        ", user=" + user +
        ", account=" + account +
        ", createTime=" + createTime +
        ", expiry=" + expiry +
        ", scopes='" + scopes + '\'' +
        ", randomSeed=" + randomSeed +
        ", stateKey='" + stateKey + '\'' +
        '}';
  }

  /**
   * Create a new temporary ESI token.
   *
   * @param user owning user
   * @param account owning account
   * @param createTime time when temporary token was created
   * @param expiry time when temporary token will expire
   * @param scopes desired scopes for this token
   * @return new temporary token
   * @throws IOException on any database error
   */
  public static NewESIToken createKey(final EveKitUserAccount user, final SynchronizedEveAccount account,
                                      final long createTime, final long expiry, final String scopes)
      throws IOException {
    init();
    try {
      // Generate and save the initial key
      NewESIToken newKey = EveKitUserAccountProvider.getFactory()
                                        .runTransaction(() -> {
                                          long seed = new Random(OrbitalProperties.getCurrentTime()).nextLong();
                                          NewESIToken result = new NewESIToken();
                                          result.user = user;
                                          result.account = account;
                                          result.createTime = createTime;
                                          result.expiry = expiry;
                                          result.scopes = scopes;
                                          result.randomSeed = seed;
                                          return EveKitUserAccountProvider.getFactory()
                                                                          .getEntityManager()
                                                                          .merge(result);
                                        });
      // If successful, then set the hash on the key and return it.  We do this in a separate transaction
      // because the temporary key ID is part of the hash and will not be set until the key is
      // committed.
      return EveKitUserAccountProvider.getFactory()
                                      .runTransaction(() -> {
                                        TypedQuery<NewESIToken> getter = EveKitUserAccountProvider.getFactory()
                                                                                                  .getEntityManager()
                                                                                                  .createNamedQuery("NewESIToken.findByID", NewESIToken.class);
                                        getter.setParameter("kid", newKey.kid);
                                        NewESIToken result = getter.getSingleResult();
                                        result.stateKey = generateHash(result);
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
   * Retrieve a temporary token by ID.
   *
   * @param kid token to retrieve
   * @return requested token, or null if not found
   * @throws IOException on any database error
   */
  public static NewESIToken getKeyByID(final long kid)
  throws IOException {
    try {
      return EveKitUserAccountProvider.getFactory().runTransaction(() -> {
          TypedQuery<NewESIToken> getter = EveKitUserAccountProvider.getFactory().getEntityManager()
              .createNamedQuery("NewESIToken.findByID", NewESIToken.class);
          getter.setParameter("kid", kid);
          try {
            return getter.getSingleResult();
          } catch (NoResultException e) {
            return null;
          }
        });
    } catch (Exception e) {
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  /**
   * Retrieve temporary token by state.
   *
   * @param state token to retrieve
   * @return requested token, or null if not found
   * @throws IOException on any database error
   */
  public static NewESIToken getKeyByState(final String state)
  throws IOException {
    try {
      return EveKitUserAccountProvider.getFactory().runTransaction(() -> {
          TypedQuery<NewESIToken> getter = EveKitUserAccountProvider.getFactory().getEntityManager()
              .createNamedQuery("NewESIToken.findByCred", NewESIToken.class);
          getter.setParameter("cred", state);
          try {
            return getter.getSingleResult();
          } catch (NoResultException e) {
            return null;
          }
        });
    } catch (Exception e) {
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  /**
   * Clean up all tokens with an expiry time before the given value.
   *
   * @param limit expiry upper bound (milliseconds UTC)
   * @throws IOException on any database error
   */
  public static void cleanExpired(final long limit) throws IOException {
    try {
      EveKitUserAccountProvider.getFactory().runTransaction(() -> {
          TypedQuery<NewESIToken> getter = EveKitUserAccountProvider.getFactory().getEntityManager()
              .createNamedQuery("NewESIToken.getExpired", NewESIToken.class);
          getter.setParameter("expiry", limit);
          for (NewESIToken next : getter.getResultList()) {
            EveKitUserAccountProvider.getFactory().getEntityManager().remove(next);
          }
        });
    } catch (Exception e) {
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  /**
   * Delete the requested token.  No op if the requested key is not found.
   *
   * @param kid token to delete.
   * @throws IOException on any database error
   */
  public static void deleteKey(final long kid) throws IOException {
    try {
      EveKitUserAccountProvider.getFactory().runTransaction(() -> {
          NewESIToken key = getKeyByID(kid);
          if (key != null) EveKitUserAccountProvider.getFactory().getEntityManager().remove(key);
        });
    } catch (Exception e) {
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  /**
   * Generate state (hash) for the given token.
   *
   * @param ref token to hash
   * @return hash string
   */
  private static String generateHash(NewESIToken ref) {
    ByteBuffer assemble = assembly.get();
    assemble.clear();

    assemble.putLong(ref.kid);
    assemble.putLong(ref.getRandomSeed());
    assemble.limit(assemble.position());
    assemble.rewind();

    return Stamper.digest(assemble);
  }

}
