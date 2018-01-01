package enterprises.orbital.evekit.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import enterprises.orbital.base.OrbitalProperties;
import enterprises.orbital.evekit.account.EveKitRefDataProvider;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Object to track synchronization of an ESI reference endpoint (see ESIRefSyncEndpoint).
 * An instance of this class tracks the synchronization status for a given endpoint.
 * <p>
 * The state stored here is:
 * <p>
 * <ul>
 * <li>Unique tracker ID</li>
 * <li>The endpoint being synchronized (an enum)</li>
 * <li>The time when this synchronization is scheduled to start</li>
 * <li>The actual start time of this synchronization</li>
 * <li>The end time of this synchronization</li>
 * <li>The status of the synchronization (an enum)</li>
 * <li>A detail message related to the status of this synchronization.</li>
 * </ul>
 */

@Entity
@Table(
    name = "evekit_esi_ref_sync_tracker",
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
        name = "ESIRefEndpointSyncTracker.get",
        query = "SELECT c FROM ESIRefEndpointSyncTracker c where c.tid = :tid"),
    @NamedQuery(
        name = "ESIRefEndpointSyncTracker.getUnfinished",
        query = "SELECT c FROM ESIRefEndpointSyncTracker c where c.endpoint = :endpoint and c.syncEnd = -1"),
    @NamedQuery(
        name = "ESIRefEndpointSyncTracker.getAllUnfinished",
        query = "SELECT c FROM ESIRefEndpointSyncTracker c where c.syncEnd = -1"),
    @NamedQuery(
        name = "ESIRefEndpointSyncTracker.getLastFinished",
        query = "SELECT c FROM ESIRefEndpointSyncTracker c where c.endpoint = :endpoint and c.syncEnd <> -1 order by c.syncEnd desc"),
    @NamedQuery(
        name = "ESIRefEndpointSyncTracker.getHistory",
        query = "SELECT c FROM ESIRefEndpointSyncTracker c where c.syncEnd <> -1 and c.syncStart < :start order by c.syncStart desc"),
})
@ApiModel(
    description = "ESI reference endpoint synchronization tracker")
public class ESIRefEndpointSyncTracker {
  private static final Logger log = Logger.getLogger(ESIRefEndpointSyncTracker.class.getName());

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
  protected long tid;

  // Endpoint to be synchronized
  @ApiModelProperty(value = "endpoint")
  @JsonProperty("endpoint")
  @Enumerated(EnumType.STRING)
  protected ESIRefSyncEndpoint endpoint;

  // Time when this tracker is scheduled to start
  @ApiModelProperty(
      value = "Scheduled tracker start time (milliseconds UTC)")
  @JsonProperty("scheduled")
  protected long scheduled;

  // Time when this tracker started synchronization
  // -1 if not yet started
  @ApiModelProperty(
      value = "Tracker start time (milliseconds UTC)")
  @JsonProperty("syncStart")
  protected long syncStart = -1;

  // Time when this tracked completed synchronization
  // -1 if not yet completed
  @ApiModelProperty(
      value = "Tracker end time (milliseconds UTC)")
  @JsonProperty("syncEnd")
  protected long syncEnd = -1;

  // Synchronization status
  @ApiModelProperty(
      value = "status")
  @JsonProperty("status")
  private ESISyncState status = ESISyncState.NOT_PROCESSED;

  @ApiModelProperty(
      value = "status detail message")
  @JsonProperty("detail")
  private String detail;

  public ESIRefEndpointSyncTracker() {
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

  public ESIRefSyncEndpoint getEndpoint() {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ESIRefEndpointSyncTracker that = (ESIRefEndpointSyncTracker) o;
    return tid == that.tid &&
        scheduled == that.scheduled &&
        syncStart == that.syncStart &&
        syncEnd == that.syncEnd &&
        endpoint == that.endpoint &&
        status == that.status &&
        Objects.equals(detail, that.detail);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tid, endpoint, scheduled, syncStart, syncEnd, status, detail);
  }

  @Override
  public String toString() {
    return "ESIRefEndpointSyncTracker{" +
        "tid=" + tid +
        ", endpoint=" + endpoint +
        ", scheduled=" + scheduled +
        ", syncStart=" + syncStart +
        ", syncEnd=" + syncEnd +
        ", status=" + status +
        ", detail='" + detail + '\'' +
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
  public static ESIRefEndpointSyncTracker finishTracker(ESIRefEndpointSyncTracker tracker) throws IOException {
    tracker.setSyncEnd(OrbitalProperties.getCurrentTime());
    return EveKitRefDataProvider.update(tracker);
  }

  /**
   * Get a tracker by ID.
   *
   * @param tid the ID of the tracker to retrieve
   * @return the tracker with the given ID
   * @throws IOException              on any database error
   * @throws TrackerNotFoundException if a tracker with the given ID could not be found
   */
  public static ESIRefEndpointSyncTracker get(long tid) throws IOException, TrackerNotFoundException {
    try {
      return EveKitRefDataProvider.getFactory()
                                  .runTransaction(() -> {
                                    TypedQuery<ESIRefEndpointSyncTracker> getter = EveKitRefDataProvider.getFactory()
                                                                                                        .getEntityManager()
                                                                                                        .createNamedQuery("ESIRefEndpointSyncTracker.get",
                                                                                                                          ESIRefEndpointSyncTracker.class);
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
   * Get the current unfinished tracker for the given reference endpoint.  There should be at most once such
   * tracker at any given time.
   *
   * @param endpoint the endpoint of the given tracker
   * @return the current unfinished tracker for the given endpoint, if one exists
   * @throws IOException              on any database error
   * @throws TrackerNotFoundException if an unfinished tracker could not be found
   */
  public static ESIRefEndpointSyncTracker getUnfinishedTracker(
      ESIRefSyncEndpoint endpoint) throws IOException, TrackerNotFoundException {
    try {
      return EveKitRefDataProvider.getFactory()
                                  .runTransaction(() -> {
                                    TypedQuery<ESIRefEndpointSyncTracker> getter = EveKitRefDataProvider.getFactory()
                                                                                                        .getEntityManager()
                                                                                                        .createNamedQuery("ESIRefEndpointSyncTracker.getUnfinished",
                                                                                                                          ESIRefEndpointSyncTracker.class);
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

  /**
   * Get the unfinished tracker for the given reference endpoint.  If no such tracker exists, then create one
   * and assign a scheduled start time.
   *
   * @param endpoint  the endpoint of the unfinished tracker
   * @param scheduled the scheduled start time if we need to create a new tracker
   * @return an existing unfinished tracker, or a new one created with the specified schedules start time
   * @throws IOException on any database error
   */
  public static ESIRefEndpointSyncTracker getOrCreateUnfinishedTracker(ESIRefSyncEndpoint endpoint,
                                                                       long scheduled) throws IOException {
    try {
      return EveKitRefDataProvider.getFactory()
                                  .runTransaction(() -> {
                                    try {
                                      // If this call succeeds without throwing an exception then we already have an unfinished tracker
                                      return getUnfinishedTracker(endpoint);
                                    } catch (TrackerNotFoundException e) {
                                      // Otherwise, create and schedule a tracker
                                      ESIRefEndpointSyncTracker tracker = new ESIRefEndpointSyncTracker();
                                      tracker.endpoint = endpoint;
                                      tracker.scheduled = scheduled;
                                      return EveKitRefDataProvider.update(tracker);
                                    }
                                  });
    } catch (Exception e) {
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  /**
   * Retrieve the last finished tracker (ordered by end time) for the given reference endpoint, if one exists.
   *
   * @param endpoint the endpoint of the last finished tracker.
   * @return the last finished tracker if one exists.
   * @throws IOException              on any database error.
   * @throws TrackerNotFoundException if no tracker could be found.
   */
  public static ESIRefEndpointSyncTracker getLatestFinishedTracker(
      ESIRefSyncEndpoint endpoint) throws IOException, TrackerNotFoundException {
    try {
      return EveKitRefDataProvider.getFactory()
                                  .runTransaction(() -> {
                                    TypedQuery<ESIRefEndpointSyncTracker> getter = EveKitRefDataProvider.getFactory()
                                                                                                        .getEntityManager()
                                                                                                        .createNamedQuery("ESIRefEndpointSyncTracker.getLastFinished",
                                                                                                                          ESIRefEndpointSyncTracker.class);
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

  /**
   * Retrieve history of finished trackers for a given reference endpoint.  Retrieved items are ordered in
   * descending order by start time.
   *
   * @param contid     the upper bound on tracker start time.
   * @param maxResults the maximum number of trackers to retrieve.
   * @return a list of finished trackers order in descending order by start time.
   * @throws IOException on any database error.
   */
  public static List<ESIRefEndpointSyncTracker> getHistory(long contid, int maxResults) throws IOException {
    try {
      return EveKitRefDataProvider.getFactory()
                                  .runTransaction(() -> {
                                    TypedQuery<ESIRefEndpointSyncTracker> getter = EveKitRefDataProvider.getFactory()
                                                                                                        .getEntityManager()
                                                                                                        .createNamedQuery("ESIRefEndpointSyncTracker.getHistory",
                                                                                                                          ESIRefEndpointSyncTracker.class);
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
   * Return all unfinished trackers (regardless of endpoint).
   *
   * @return the list of all unfinished trackers
   * @throws IOException on any database error
   */
  public static List<ESIRefEndpointSyncTracker> getAllUnfinishedTrackers() throws IOException {
    try {
      return EveKitRefDataProvider.getFactory()
                                  .runTransaction(() -> {
                                    TypedQuery<ESIRefEndpointSyncTracker> getter = EveKitRefDataProvider.getFactory()
                                                                                                        .getEntityManager()
                                                                                                        .createNamedQuery("ESIRefEndpointSyncTracker.getAllUnfinished",
                                                                                                                          ESIRefEndpointSyncTracker.class);
                                    return getter.getResultList();
                                  });
    } catch (Exception e) {
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }
}
