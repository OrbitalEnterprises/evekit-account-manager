package enterprises.orbital.evekit.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import enterprises.orbital.base.OrbitalProperties;
import enterprises.orbital.evekit.account.EveKitUserAccountProvider;
import enterprises.orbital.evekit.account.SynchronizedEveAccount;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Object to track synchronization of an ESI endpoint (see ESISyncEndpoint) for a synchronized account.
 * An instance of this class tracks the synchronization status for a given endpoint.
 *
 * The state stored here is:
 *
 * <ul>
 *   <li>Unique tracker ID</li>
 *   <li>The account for which data will be synchronized</li>
 *   <li>The endpoint being synchronized (an enum)</li>
 *   <li>The time when this synchronization is scheduled to start</li>
 *   <li>The actual start time of this synchronization</li>
 *   <li>The end time of this synchronization</li>
 *   <li>The status of the synchronization (an enum)</li>
 *   <li>A detail message related to the status of this synchronization.</li>
 *   <li>Optional context which is used by some synchronization code</li>
 * </ul>
 *
 */

@Entity
@Table(
    name = "evekit_esi_sync_tracker",
    indexes = {
        @Index(
            name = "scheduledIndex",
            columnList = "scheduled"),
        @Index(
            name = "syncStartIndex",
            columnList = "syncStart"),
        @Index(
            name = "syncEndIndex",
            columnList = "syncEnd"),
    })
@NamedQueries({
    @NamedQuery(
        name = "ESIEndpointSyncTracker.get",
        query = "SELECT c FROM ESIEndpointSyncTracker c where c.tid = :tid"),
    @NamedQuery(
        name = "ESIEndpointSyncTracker.getUnfinished",
        query = "SELECT c FROM ESIEndpointSyncTracker c where c.account = :account and c.endpoint = :endpoint and c.syncEnd = -1"),
    @NamedQuery(
        name = "ESIEndpointSyncTracker.getAllStartedUnfinished",
        query = "SELECT c FROM ESIEndpointSyncTracker c where c.syncStart <> -1 and c.syncEnd = -1"),
    @NamedQuery(
        name = "ESIEndpointSyncTracker.getAllUnfinished",
        query = "SELECT c FROM ESIEndpointSyncTracker c where c.syncEnd = -1"),
    @NamedQuery(
        name = "ESIEndpointSyncTracker.getLastFinished",
        query = "SELECT c FROM ESIEndpointSyncTracker c where c.account = :account and c.endpoint = :endpoint and c.syncEnd <> -1 order by c.syncEnd desc"),
    @NamedQuery(
        name = "ESIEndpointSyncTracker.getAnyLastFinished",
        query = "SELECT c FROM ESIEndpointSyncTracker c where c.account = :account and c.syncEnd <> -1 order by c.syncEnd desc"),
    @NamedQuery(
        name = "ESIEndpointSyncTracker.getHistory",
        query = "SELECT c FROM ESIEndpointSyncTracker c where c.account = :account and c.endpoint = :endpoint and c.syncEnd <> -1 and c.syncStart < :start order by c.syncStart desc"),
    @NamedQuery(
        name = "ESIEndpointSyncTracker.getAllHistory",
        query = "SELECT c FROM ESIEndpointSyncTracker c where c.account = :account and c.syncEnd <> -1 and c.syncStart < :start order by c.syncStart desc"),
})
@ApiModel(
    description = "ESI endpoint synchronization tracker")
public class ESIEndpointSyncTracker {
  private static final Logger   log       = Logger.getLogger(ESIEndpointSyncTracker.class.getName());

  // Unique tracker ID
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
      value = "Uniquer tracker ID")
  @JsonProperty("tid")
  protected long                   tid;

  // Account with which this tracker is associated
  @ManyToOne
  @JoinColumn(
      name = "aid",
      referencedColumnName = "aid")
  @JsonProperty("account")
  protected SynchronizedEveAccount account;

  // Endpoint to be synchronized
  @ApiModelProperty(value = "endpoint")
  @JsonProperty("endpoint")
  @Enumerated(EnumType.STRING)
  protected ESISyncEndpoint endpoint;

  // Time when this tracker is scheduled to start
  @ApiModelProperty(
      value = "Scheduled tracker start time (milliseconds UTC)")
  @JsonProperty("scheduled")
  protected long                   scheduled;

  // Time when this tracker started synchronization
  // -1 if not yet started
  @ApiModelProperty(
      value = "Tracker start time (milliseconds UTC)")
  @JsonProperty("syncStart")
  protected long                   syncStart = -1;

  // Time when this tracked completed synchronization
  // -1 if not yet completed
  @ApiModelProperty(
      value = "Tracker end time (milliseconds UTC)")
  @JsonProperty("syncEnd")
  protected long                   syncEnd   = -1;

  // Synchronization status
  @ApiModelProperty(
      value = "status")
  @JsonProperty("status")
  private ESISyncState status = ESISyncState.NOT_PROCESSED;

  @ApiModelProperty(
      value = "status detail message")
  @JsonProperty("detail")
  private String  detail;

  // Opaque context used by some synchronization code
  @JsonIgnore
  private String context;

  public ESIEndpointSyncTracker() {
  }

  public void setSyncStart(long syncStart) {
    this.syncStart = syncStart;
  }

  public void setSyncEnd(long syncEnd) {
    this.syncEnd = syncEnd;
  }

  public void setStatus(ESISyncState status) {
    this.status = status;
  }

  public void setDetail(String detail) {
    this.detail = detail;
  }

  public long getTid() {
    return tid;
  }

  public SynchronizedEveAccount getAccount() {
    return account;
  }

  public ESISyncEndpoint getEndpoint() {
    return endpoint;
  }

  public long getScheduled() {
    return scheduled;
  }

  public long getSyncStart() {
    return syncStart;
  }

  public long getSyncEnd() {
    return syncEnd;
  }

  public ESISyncState getStatus() {
    return status;
  }

  public String getDetail() {
    return detail;
  }

  public String getContext() {
    return context;
  }

  public void setContext(String context) {
    this.context = context;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ESIEndpointSyncTracker that = (ESIEndpointSyncTracker) o;

    if (tid != that.tid) return false;
    if (scheduled != that.scheduled) return false;
    if (syncStart != that.syncStart) return false;
    if (syncEnd != that.syncEnd) return false;
    if (!account.equals(that.account)) return false;
    if (endpoint != that.endpoint) return false;
    if (status != that.status) return false;
    return detail != null ? detail.equals(that.detail) : that.detail == null;
  }

  @Override
  public int hashCode() {
    int result = (int) (tid ^ (tid >>> 32));
    result = 31 * result + account.hashCode();
    result = 31 * result + endpoint.hashCode();
    result = 31 * result + (int) (scheduled ^ (scheduled >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return "ESIEndpointSyncTracker{" +
        "tid=" + tid +
        ", account=" + account +
        ", endpoint=" + endpoint +
        ", scheduled=" + scheduled +
        ", syncStart=" + syncStart +
        ", syncEnd=" + syncEnd +
        ", status=" + status +
        ", detail='" + detail + '\'' +
        ", context='" + context + '\'' +
        '}';
  }

  /**
   * Check whether this tracker has been refreshed.
   *
   * @return true if status is not NOT_PROCESSED, false otherwise.
   */
  public boolean isRefreshed() {
    return status != ESISyncState.NOT_PROCESSED;
  }

  /**
   * Mark a tracker as finished by assigning a "sync end".  Sync end time is always assigned to the current
   * local time as retrieved from OrbitalProperties.getCurrentTime.
   *
   * @param tracker the tracker to finish.
   * @return the updated tracker
   * @throws IOException on any database error.
   */
  public static ESIEndpointSyncTracker finishTracker(ESIEndpointSyncTracker tracker) throws IOException {
    tracker.setSyncEnd(OrbitalProperties.getCurrentTime());
    return EveKitUserAccountProvider.update(tracker);
  }

  /**
   * Get a tracker by ID.
   *
   * @param tid the ID of the tracker to retrieve
   * @return the tracker with the given ID
   * @throws IOException on any database error
   * @throws TrackerNotFoundException if a tracker with the given ID could not be found
   */
  public static ESIEndpointSyncTracker get(long tid) throws IOException, TrackerNotFoundException {
    try {
      return EveKitUserAccountProvider.getFactory().runTransaction(() -> {
          TypedQuery<ESIEndpointSyncTracker> getter = EveKitUserAccountProvider.getFactory().getEntityManager().createNamedQuery("ESIEndpointSyncTracker.get",
                                                                                                                             ESIEndpointSyncTracker.class);
          getter.setParameter("tid", tid);
          try {
            return getter.getSingleResult();
          } catch (NoResultException e) {
            throw new TrackerNotFoundException();
          }
        });
    } catch (Exception e) {
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      if (e.getCause() instanceof TrackerNotFoundException) throw (TrackerNotFoundException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  /**
   * Get the current unfinished tracker for the given account and endpoint.  There should be at most once such
   * tracker at any given time.
   *
   * @param account the owner of the given tracker
   * @param endpoint the endpoint of the given tracker
   * @return the current unfinished tracker for the given account and endpoint, if one exists
   * @throws IOException on any database error
   * @throws TrackerNotFoundException if an unfinished tracker could not be found
   */
  public static ESIEndpointSyncTracker getUnfinishedTracker(SynchronizedEveAccount account, ESISyncEndpoint endpoint) throws IOException, TrackerNotFoundException {
    synchronized (SynchronizedEveAccount.getSyncAccountLock(account)) {
      try {
        return EveKitUserAccountProvider.getFactory()
                                        .runTransaction(() -> {
                                          TypedQuery<ESIEndpointSyncTracker> getter = EveKitUserAccountProvider.getFactory()
                                                                                                               .getEntityManager()
                                                                                                               .createNamedQuery(
                                                                                                                   "ESIEndpointSyncTracker.getUnfinished",
                                                                                                                   ESIEndpointSyncTracker.class);
                                          getter.setParameter("account", account);
                                          getter.setParameter("endpoint", endpoint);
                                          try {
                                            return getter.getSingleResult();
                                          } catch (NoResultException e) {
                                            throw new TrackerNotFoundException();
                                          }
                                        });
      } catch (Exception e) {
        if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
        if (e.getCause() instanceof TrackerNotFoundException) throw (TrackerNotFoundException) e.getCause();
        log.log(Level.SEVERE, "query error", e);
        throw new IOException(e.getCause());
      }
    }
  }

  /**
   * Get the list of all unfinished trackers.
   *
   * @return the list of all unfinished trackers
   * @throws IOException on any database error.
   */
  public static synchronized List<ESIEndpointSyncTracker> getAllUnfinishedTrackers() throws IOException {
    try {
      return EveKitUserAccountProvider.getFactory().runTransaction(() -> {
        TypedQuery<ESIEndpointSyncTracker> getter = EveKitUserAccountProvider.getFactory().getEntityManager().createNamedQuery("ESIEndpointSyncTracker.getAllUnfinished",
                                                                                                                               ESIEndpointSyncTracker.class);
        return getter.getResultList();
      });
    } catch (Exception e) {
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  /**
   * Get the list of all started but unfinished trackers.
   *
   * @return the list of all started but unfinished trackers
   * @throws IOException on any database error.
   */
  public static synchronized List<ESIEndpointSyncTracker> getAllStartedUnfinishedTrackers() throws IOException {
    try {
      return EveKitUserAccountProvider.getFactory().runTransaction(() -> {
        TypedQuery<ESIEndpointSyncTracker> getter = EveKitUserAccountProvider.getFactory().getEntityManager().createNamedQuery("ESIEndpointSyncTracker.getAllStartedUnfinished",
                                                                                                                               ESIEndpointSyncTracker.class);
        return getter.getResultList();
      });
    } catch (Exception e) {
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  /**
   * Get the unfinished tracker for the given account and endpoint.  If no such tracker exists, then create one
   * and assign a scheduled start time.
   *
   * @param account the owner of the unfinished tracker
   * @param endpoint the endpoint of the unfinished tracker
   * @param scheduled the scheduled start time if we need to create a new tracker
   * @param context optional context for this sync event
   * @return an existing unfinished tracker, or a new one created with the specified schedules start time
   * @throws IOException on any database error
   */
  public static ESIEndpointSyncTracker getOrCreateUnfinishedTracker(SynchronizedEveAccount account, ESISyncEndpoint endpoint, long scheduled, String context) throws IOException {
    synchronized (SynchronizedEveAccount.getSyncAccountLock(account)) {
      try {
        return EveKitUserAccountProvider.getFactory()
                                        .runTransaction(() -> {
                                          try {
                                            // If this call succeeds without throwing an exception then we already have an unfinished tracker
                                            return getUnfinishedTracker(account, endpoint);
                                          } catch (TrackerNotFoundException e) {
                                            // Otherwise, create and schedule a tracker
                                            ESIEndpointSyncTracker tracker = new ESIEndpointSyncTracker();
                                            tracker.account = account;
                                            tracker.endpoint = endpoint;
                                            tracker.scheduled = scheduled;
                                            tracker.context = context;
                                            return EveKitUserAccountProvider.update(tracker);
                                          }
                                        });
      } catch (Exception e) {
        if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
        log.log(Level.SEVERE, "query error", e);
        throw new IOException(e.getCause());
      }
    }
  }

  /**
   * Retrieve the last finished tracker (ordered by end time) for the given account and endpoint, if one exists.
   *
   * @param account the owner of the last finished tracker.
   * @param endpoint the endpoint of the last finished tracker.
   * @return the last finished tracker if one exists.
   * @throws IOException on any database error.
   * @throws TrackerNotFoundException if no tracker could be found.
   */
  public static ESIEndpointSyncTracker getLatestFinishedTracker(SynchronizedEveAccount account, ESISyncEndpoint endpoint) throws IOException, TrackerNotFoundException {
    synchronized (SynchronizedEveAccount.getSyncAccountLock(account)) {
      try {
        return EveKitUserAccountProvider.getFactory()
                                        .runTransaction(() -> {
                                          TypedQuery<ESIEndpointSyncTracker> getter = EveKitUserAccountProvider.getFactory()
                                                                                                               .getEntityManager()
                                                                                                               .createNamedQuery(
                                                                                                                   "ESIEndpointSyncTracker.getLastFinished",
                                                                                                                   ESIEndpointSyncTracker.class);
                                          getter.setParameter("account", account);
                                          getter.setParameter("endpoint", endpoint);
                                          getter.setMaxResults(1);
                                          try {
                                            return getter.getSingleResult();
                                          } catch (NoResultException e) {
                                            throw new TrackerNotFoundException();
                                          }
                                        });
      } catch (Exception e) {
        if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
        if (e.getCause() instanceof TrackerNotFoundException) throw (TrackerNotFoundException) e.getCause();
        log.log(Level.SEVERE, "query error", e);
        throw new IOException(e.getCause());
      }
    }
  }

  /**
   * Retrieve the last finished tracker (ordered by end time) for the given account, if one exists.
   *
   * @param account the owner of the last finished tracker.
   * @return the last finished tracker if one exists.
   * @throws IOException on any database error.
   * @throws TrackerNotFoundException if no tracker could be found.
   */
  public static ESIEndpointSyncTracker getAnyLatestFinishedTracker(SynchronizedEveAccount account) throws IOException, TrackerNotFoundException {
    synchronized (SynchronizedEveAccount.getSyncAccountLock(account)) {
      try {
        return EveKitUserAccountProvider.getFactory()
                                        .runTransaction(() -> {
                                          TypedQuery<ESIEndpointSyncTracker> getter = EveKitUserAccountProvider.getFactory()
                                                                                                               .getEntityManager()
                                                                                                               .createNamedQuery(
                                                                                                                   "ESIEndpointSyncTracker.getAnyLastFinished",
                                                                                                                   ESIEndpointSyncTracker.class);
                                          getter.setParameter("account", account);
                                          getter.setMaxResults(1);
                                          List<ESIEndpointSyncTracker> results = getter.getResultList();
                                          if (results.isEmpty()) throw new TrackerNotFoundException();
                                          return results.get(0);
                                        });
      } catch (Exception e) {
        if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
        if (e.getCause() instanceof TrackerNotFoundException) throw (TrackerNotFoundException) e.getCause();
        log.log(Level.SEVERE, "query error", e);
        throw new IOException(e.getCause());
      }
    }
  }

  /**
   * Retrieve history of finished trackers for a given account and endpoint.  Retrieved items are ordered in
   * descending order by start time.
   *
   * @param account the owner of retrieved trackers.
   * @param endpoint the endpoint of retrieved trackers.
   * @param contid the upper bound on tracker start time.
   * @param maxResults the maximum number of trackers to retrieve.
   * @return a list of finished trackers order in descending order by start time.
   * @throws IOException on any database error.
   */
  public static List<ESIEndpointSyncTracker> getHistory(SynchronizedEveAccount account, ESISyncEndpoint endpoint, long contid, int maxResults) throws IOException {
    try {
      return EveKitUserAccountProvider.getFactory().runTransaction(() -> {
          TypedQuery<ESIEndpointSyncTracker> getter = EveKitUserAccountProvider.getFactory().getEntityManager().createNamedQuery("ESIEndpointSyncTracker.getHistory",
                                                                                                                                 ESIEndpointSyncTracker.class);
          getter.setParameter("account", account);
          getter.setParameter("endpoint", endpoint);
          getter.setParameter("start", contid < 0 ? Long.MAX_VALUE : contid);
          getter.setMaxResults(maxResults);
          return getter.getResultList();
        });
    } catch (Exception e) {
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  /**
   * Retrieve ordered history of finished trackers for a given account and all endpoints.  Retrieved items are ordered in
   * descending order by start time.
   *
   * @param account the owner of retrieved trackers.
   * @param contid the upper bound on tracker start time.
   * @param maxResults the maximum number of trackers to retrieve.
   * @return a list of finished trackers order in descending order by start time.
   * @throws IOException on any database error.
   */
  public static List<ESIEndpointSyncTracker> getAllHistory(SynchronizedEveAccount account, long contid, int maxResults) throws IOException {
    try {
      return EveKitUserAccountProvider.getFactory().runTransaction(() -> {
        TypedQuery<ESIEndpointSyncTracker> getter = EveKitUserAccountProvider.getFactory().getEntityManager().createNamedQuery("ESIEndpointSyncTracker.getAllHistory",
                                                                                                                               ESIEndpointSyncTracker.class);
        getter.setParameter("account", account);
        getter.setParameter("start", contid < 0 ? Long.MAX_VALUE : contid);
        getter.setMaxResults(maxResults);
        return getter.getResultList();
      });
    } catch (Exception e) {
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

}
