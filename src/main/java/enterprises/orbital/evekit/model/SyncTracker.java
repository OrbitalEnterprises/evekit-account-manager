package enterprises.orbital.evekit.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import enterprises.orbital.base.OrbitalProperties;
import enterprises.orbital.db.ConnectionFactory.RunInTransaction;
import enterprises.orbital.evekit.account.EveKitUserAccountProvider;
import enterprises.orbital.evekit.account.SynchronizedEveAccount;

/**
 * Generic indexer class for any synchronizations in progress.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "evekit_sync", indexes = {
    @Index(name = "accountIndex", columnList = "aid", unique = false), @Index(name = "finishedIndex", columnList = "aid, finished", unique = false),
    @Index(name = "syncEndIndex", columnList = "aid, syncEnd", unique = false),
})
public abstract class SyncTracker {
  private static final Logger log = Logger.getLogger(SyncTracker.class.getName());

  // State values to be stored in fields of a specific synchronization type.
  public enum SyncState {
                         NOT_PROCESSED, // haven't started processing this field yet.
                         UPDATED, // updated this field without issue.
                         NOT_EXPIRED, // skipped this field because it was not expired.
                         SYNC_ERROR, // field not updated because of an error.
                         SYNC_WARNING, // synchronization completed but not in an expected way. This may be benign (e.g. not participating in faction warfare).
                         NOT_ALLOWED // skipped this field because the API key does not have the required privilege to update
  }

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ek_seq")
  @SequenceGenerator(name = "ek_seq", initialValue = 100000, allocationSize = 10)
  protected long                   tid;
  @ManyToOne
  @JoinColumn(name = "aid", referencedColumnName = "aid")
  protected SynchronizedEveAccount account;
  protected long                   syncStart = -1;
  protected boolean                finished;
  protected long                   syncEnd   = -1;

  // No-args ctor required by Objectify
  public SyncTracker() {}

  public SynchronizedEveAccount getOwner() {
    return account;
  }

  public boolean getFinished() {
    return finished;
  }

  public void setFinished(boolean finished) {
    this.finished = finished;
  }

  public long getSyncStart() {
    return syncStart;
  }

  public void setSyncStart(long syncStart) {
    this.syncStart = syncStart;
  }

  public long getSyncEnd() {
    return syncEnd;
  }

  public void setSyncEnd(long syncEnd) {
    this.syncEnd = syncEnd;
  }

  /**
   * Either return the first SynchronizationState still to be completed, or return null if this tracker is complete.
   * 
   * @return the first SynchronizationState yet to be completed, or null if this tracker is complete.
   */
  public SynchronizationState trackerComplete(Set<SynchronizationState> checkState) {
    return null;
  }

  /**
   * Change the status for this tracker at the given state.
   * 
   * @param state
   *          the state of the tracker to change.
   * @param status
   *          the new status for the given state.
   * @param msg
   *          the new detail message for the given state.
   */
  public void setState(SynchronizationState state, SyncTracker.SyncState status, String msg) {
    // NOP
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((account == null) ? 0 : account.hashCode());
    result = prime * result + (finished ? 1231 : 1237);
    result = prime * result + (int) (syncEnd ^ (syncEnd >>> 32));
    result = prime * result + (int) (syncStart ^ (syncStart >>> 32));
    result = prime * result + (int) (tid ^ (tid >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    SyncTracker other = (SyncTracker) obj;
    if (account == null) {
      if (other.account != null) return false;
    } else if (!account.equals(other.account)) return false;
    if (finished != other.finished) return false;
    if (syncEnd != other.syncEnd) return false;
    if (syncStart != other.syncStart) return false;
    if (tid != other.tid) return false;
    return true;
  }

  @Override
  public String toString() {
    return "SyncTracker [tid=" + tid + ", account=" + account + ", syncStart=" + syncStart + ", finished=" + finished + ", syncEnd=" + syncEnd + "]";
  }

  public static void incrementSummary(String cat, String reason, Map<String, Map<String, AtomicInteger>> summary) {
    Map<String, AtomicInteger> category = summary.get(cat);
    if (category == null) {
      category = new HashMap<String, AtomicInteger>();
      summary.put(cat, category);
    }
    AtomicInteger counter = category.get(reason);
    if (counter == null) {
      counter = new AtomicInteger();
      category.put(reason, counter);
    }
    counter.incrementAndGet();
  }

  public static SyncTracker finishTracker(final SyncTracker tracker) {
    try {
      return EveKitUserAccountProvider.getFactory().runTransaction(new RunInTransaction<SyncTracker>() {
        @Override
        public SyncTracker run() throws Exception {
          tracker.setFinished(true);
          tracker.setSyncEnd(OrbitalProperties.getCurrentTime());
          return EveKitUserAccountProvider.getFactory().getEntityManager().merge(tracker);
        }
      });
    } catch (Exception e) {
      log.log(Level.SEVERE, "query error", e);
    }
    return null;
  }

  public static <A extends SyncTracker> A updateTracker(final A tracker) {
    try {
      return EveKitUserAccountProvider.getFactory().runTransaction(new RunInTransaction<A>() {
        @Override
        public A run() throws Exception {
          return EveKitUserAccountProvider.getFactory().getEntityManager().merge(tracker);
        }
      });
    } catch (Exception e) {
      log.log(Level.SEVERE, "query error", e);
    }
    return null;
  }

}
