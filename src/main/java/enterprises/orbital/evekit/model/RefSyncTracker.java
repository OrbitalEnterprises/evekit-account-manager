package enterprises.orbital.evekit.model;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.NoResultException;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TypedQuery;

import com.fasterxml.jackson.annotation.JsonProperty;

import enterprises.orbital.base.OrbitalProperties;
import enterprises.orbital.db.ConnectionFactory.RunInTransaction;
import enterprises.orbital.evekit.account.EveKitRefDataProvider;
import enterprises.orbital.evekit.model.SyncTracker.SyncState;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Object to track synchronization requests for reference data. A synchronization tracker starts out empty and fills in each supported field type until it is
 * marked as finished. This makes it relatively straightforward to cache a tracker since slightly stale copies are usually harmless.
 */

@Entity
@Table(
    name = "evekit_ref_sync",
    indexes = {
        @Index(
            name = "finishedIndex",
            columnList = "finished",
            unique = false),
        @Index(
            name = "syncEndIndex",
            columnList = "syncEnd",
            unique = false),
    })
@NamedQueries({
    @NamedQuery(
        name = "RefSyncTracker.get",
        query = "SELECT c FROM RefSyncTracker c where c.tid = :tid"),
    @NamedQuery(
        name = "RefSyncTracker.getUnfinished",
        query = "SELECT c FROM RefSyncTracker c where c.finished = false"),
    @NamedQuery(
        name = "RefSyncTracker.getLatestFinished",
        query = "SELECT c FROM RefSyncTracker c where c.finished = true order by c.syncEnd desc"),
    @NamedQuery(
        name = "RefSyncTracker.getStart",
        query = "SELECT c FROM RefSyncTracker c where c.syncStart = :start"),
    @NamedQuery(
        name = "RefSyncTracker.getHistory",
        query = "SELECT c FROM RefSyncTracker c where c.finished = true and c.syncStart < :start order by c.syncStart desc"),
    @NamedQuery(
        name = "RefSyncTracker.getSummary",
        query = "SELECT c FROM RefSyncTracker c where c.finished = true and c.syncStart >= :start"),
})
@ApiModel(
    description = "Reference data synchronization tracker entity")
public class RefSyncTracker {
  private static final Logger   log       = Logger.getLogger(RefSyncTracker.class.getName());

  @Id
  @GeneratedValue(
      strategy = GenerationType.SEQUENCE,
      generator = "ek_ref_seq")
  @SequenceGenerator(
      name = "ek_ref_seq",
      initialValue = 100000,
      allocationSize = 10,
      sequenceName = "account_sequence")
  @ApiModelProperty(
      value = "Uniquer tracker ID")
  @JsonProperty("tid")
  protected long                tid;
  @ApiModelProperty(
      value = "Tracker start time (milliseconds UTC)")
  @JsonProperty("syncStart")
  protected long                syncStart = -1;
  @ApiModelProperty(
      value = "True if this tracker has been completed")
  @JsonProperty("finished")
  protected boolean             finished;
  @ApiModelProperty(
      value = "Tracker end time (milliseconds UTC)")
  @JsonProperty("syncEnd")
  protected long                syncEnd   = -1;

  // Status of each element we're synchronizing. If status is
  // SYNC_ERROR, then the "detail" field contains text explaining the error.
  @ApiModelProperty(
      value = "Server status")
  @JsonProperty("serverStatusStatus")
  private SyncTracker.SyncState serverStatusStatus;
  @ApiModelProperty(
      value = "Server status detail message")
  @JsonProperty("serverStatusDetail")
  private String                serverStatusDetail;
  @ApiModelProperty(
      value = "API Call List status")
  @JsonProperty("callListStatus")
  private SyncTracker.SyncState callListStatus;
  @ApiModelProperty(
      value = "API Call List detail")
  @JsonProperty("callListDetail")
  private String                callListDetail;
  @ApiModelProperty(
      value = "Alliance list status")
  @JsonProperty("allianceListStatus")
  private SyncTracker.SyncState allianceListStatus;
  @ApiModelProperty(
      value = "Alliance list detail")
  @JsonProperty("allianceListDetail")
  private String                allianceListDetail;
  @ApiModelProperty(
      value = "Conquerable station list status")
  @JsonProperty("conquerableStationsStatus")
  private SyncTracker.SyncState conquerableStationsStatus;
  @ApiModelProperty(
      value = "Conquerable station list detail")
  @JsonProperty("conquerableStationsDetail")
  private String                conquerableStationsDetail;
  @ApiModelProperty(
      value = "Error list status")
  @JsonProperty("errorListStatus")
  private SyncTracker.SyncState errorListStatus;
  @ApiModelProperty(
      value = "Error list detail")
  @JsonProperty("errorListDetail")
  private String                errorListDetail;
  @ApiModelProperty(
      value = "Faction war stats status")
  @JsonProperty("facWarStatsStatus")
  private SyncTracker.SyncState facWarStatsStatus;
  @ApiModelProperty(
      value = "Faction war stats detail")
  @JsonProperty("facWarStatsDetail")
  private String                facWarStatsDetail;
  @ApiModelProperty(
      value = "Faction war top stats status")
  @JsonProperty("facWarTopStatsStatus")
  private SyncTracker.SyncState facWarTopStatsStatus;
  @ApiModelProperty(
      value = "Faction war top stats detail")
  @JsonProperty("facWarTopStatsDetail")
  private String                facWarTopStatsDetail;
  @ApiModelProperty(
      value = "Reference type status")
  @JsonProperty("refTypeStatus")
  private SyncTracker.SyncState refTypeStatus;
  @ApiModelProperty(
      value = "Reference type detail")
  @JsonProperty("refTypeDetail")
  private String                refTypeDetail;
  @ApiModelProperty(
      value = "Skill tree status")
  @JsonProperty("skillTreeStatus")
  private SyncTracker.SyncState skillTreeStatus;
  @ApiModelProperty(
      value = "Skill tree detail")
  @JsonProperty("skillTreeDetail")
  private String                skillTreeDetail;
  @ApiModelProperty(
      value = "Faction war systems status")
  @JsonProperty("facWarSystemsStatus")
  private SyncTracker.SyncState facWarSystemsStatus;
  @ApiModelProperty(
      value = "Faction war systems detail")
  @JsonProperty("facWarSystemsDetail")
  private String                facWarSystemsDetail;
  @ApiModelProperty(
      value = "Map jump status")
  @JsonProperty("mapJumpStatus")
  private SyncTracker.SyncState mapJumpStatus;
  @ApiModelProperty(
      value = "Map jump detail")
  @JsonProperty("mapJumpDetail")
  private String                mapJumpDetail;
  @ApiModelProperty(
      value = "Map kill status")
  @JsonProperty("mapKillStatus")
  private SyncTracker.SyncState mapKillStatus;
  @ApiModelProperty(
      value = "Map kill detail")
  @JsonProperty("mapKillDetail")
  private String                mapKillDetail;
  @ApiModelProperty(
      value = "Sovereignty status")
  @JsonProperty("sovereigntyStatus")
  private SyncTracker.SyncState sovereigntyStatus;
  @ApiModelProperty(
      value = "Sovereignty detail")
  @JsonProperty("sovereigntyDetail")
  private String                sovereigntyDetail;

  public RefSyncTracker() {
    serverStatusStatus = SyncTracker.SyncState.NOT_PROCESSED;
    callListStatus = SyncTracker.SyncState.NOT_PROCESSED;
    allianceListStatus = SyncTracker.SyncState.NOT_PROCESSED;
    conquerableStationsStatus = SyncTracker.SyncState.NOT_PROCESSED;
    errorListStatus = SyncTracker.SyncState.NOT_PROCESSED;
    facWarStatsStatus = SyncTracker.SyncState.NOT_PROCESSED;
    facWarTopStatsStatus = SyncTracker.SyncState.NOT_PROCESSED;
    refTypeStatus = SyncTracker.SyncState.NOT_PROCESSED;
    skillTreeStatus = SyncTracker.SyncState.NOT_PROCESSED;
    facWarSystemsStatus = SyncTracker.SyncState.NOT_PROCESSED;
    mapJumpStatus = SyncTracker.SyncState.NOT_PROCESSED;
    mapKillStatus = SyncTracker.SyncState.NOT_PROCESSED;
    sovereigntyStatus = SyncTracker.SyncState.NOT_PROCESSED;
  }

  public long getTid() {
    return tid;
  }

  public boolean getFinished() {
    return finished;
  }

  public void setFinished(
                          boolean finished) {
    this.finished = finished;
  }

  public long getSyncStart() {
    return syncStart;
  }

  public void setSyncStart(
                           long syncStart) {
    this.syncStart = syncStart;
  }

  public long getSyncEnd() {
    return syncEnd;
  }

  public void setSyncEnd(
                         long syncEnd) {
    this.syncEnd = syncEnd;
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
  public void setState(
                       SynchronizationState state,
                       SyncTracker.SyncState status,
                       String msg) {
    switch (state) {

    case SYNC_REF_SERVERSTATUS:
      setServerStatusStatus(status);
      setServerStatusDetail(msg);
      break;
    case SYNC_REF_CALLS_LIST:
      setCallListStatus(status);
      setCallListDetail(msg);
      break;
    case SYNC_REF_ALLIANCES:
      setAllianceListStatus(status);
      setAllianceListDetail(msg);
      break;
    case SYNC_REF_CONQUERABLE:
      setConquerableStationsStatus(status);
      setConquerableStationsDetail(msg);
      break;
    case SYNC_REF_ERRORLIST:
      setErrorListStatus(status);
      setErrorListDetail(msg);
      break;
    case SYNC_REF_FACWARSTATS:
      setFacWarStatsStatus(status);
      setFacWarStatsDetail(msg);
      break;
    case SYNC_REF_FACWARTOPSTATS:
      setFacWarTopStatsStatus(status);
      setFacWarTopStatsDetail(msg);
      break;
    case SYNC_REF_REFTYPES:
      setRefTypeStatus(status);
      setRefTypeDetail(msg);
      break;
    case SYNC_REF_SKILLTREE:
      setSkillTreeStatus(status);
      setSkillTreeDetail(msg);
      break;
    case SYNC_REF_FACWARSYSTEMS:
      setFacWarSystemsStatus(status);
      setFacWarSystemsDetail(msg);
      break;
    case SYNC_REF_MAPJUMPS:
      setMapJumpStatus(status);
      setMapJumpDetail(msg);
      break;
    case SYNC_REF_MAPKILLS:
      setMapKillStatus(status);
      setMapKillDetail(msg);
      break;
    case SYNC_REF_SOVEREIGNTY:
      setSovereigntyStatus(status);
      setSovereigntyDetail(msg);
      break;
    default:
      // NOP
      ;
    }
  }

  /**
   * Either return the first SynchronizationState still to be completed, or return null if this tracker is complete.
   * 
   * @param checkState
   *          the set of tracker states to check this tracker against.
   * @return the first SynchronizationState yet to be completed, or null if this tracker is complete.
   */
  public SynchronizationState trackerComplete(
                                              Set<SynchronizationState> checkState) {
    for (SynchronizationState next : checkState) {
      switch (next) {
      case SYNC_REF_SERVERSTATUS:
        if (serverStatusStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;
      case SYNC_REF_CALLS_LIST:
        if (callListStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;
      case SYNC_REF_ALLIANCES:
        if (allianceListStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;
      case SYNC_REF_CONQUERABLE:
        if (conquerableStationsStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;
      case SYNC_REF_ERRORLIST:
        if (errorListStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;
      case SYNC_REF_FACWARSTATS:
        if (facWarStatsStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;
      case SYNC_REF_FACWARTOPSTATS:
        if (facWarTopStatsStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;
      case SYNC_REF_REFTYPES:
        if (refTypeStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;
      case SYNC_REF_SKILLTREE:
        if (skillTreeStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;
      case SYNC_REF_FACWARSYSTEMS:
        if (facWarSystemsStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;
      case SYNC_REF_MAPJUMPS:
        if (mapJumpStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;
      case SYNC_REF_MAPKILLS:
        if (mapKillStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;
      case SYNC_REF_SOVEREIGNTY:
        if (sovereigntyStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_REF_START:
      case SYNC_REF_END:
      default:
        // NOP
        ;
      }
    }

    return null;
  }

  public void setServerStatusStatus(
                                    SyncTracker.SyncState serverStatusStatus) {
    this.serverStatusStatus = serverStatusStatus;
  }

  public SyncTracker.SyncState getServerStatusStatus() {
    return serverStatusStatus;
  }

  public String getServerStatusDetail() {
    return serverStatusDetail;
  }

  public void setServerStatusDetail(
                                    String serverStatusDetail) {
    this.serverStatusDetail = serverStatusDetail;
  }

  public SyncTracker.SyncState getCallListStatus() {
    return callListStatus;
  }

  public void setCallListStatus(
                                SyncTracker.SyncState callListStatus) {
    this.callListStatus = callListStatus;
  }

  public String getCallListDetail() {
    return callListDetail;
  }

  public void setCallListDetail(
                                String callListDetail) {
    this.callListDetail = callListDetail;
  }

  public SyncTracker.SyncState getAllianceListStatus() {
    return allianceListStatus;
  }

  public void setAllianceListStatus(
                                    SyncTracker.SyncState allianceListStatus) {
    this.allianceListStatus = allianceListStatus;
  }

  public String getAllianceListDetail() {
    return allianceListDetail;
  }

  public void setAllianceListDetail(
                                    String allianceListDetail) {
    this.allianceListDetail = allianceListDetail;
  }

  public SyncTracker.SyncState getConquerableStationsStatus() {
    return conquerableStationsStatus;
  }

  public void setConquerableStationsStatus(
                                           SyncTracker.SyncState conquerableStationsStatus) {
    this.conquerableStationsStatus = conquerableStationsStatus;
  }

  public String getConquerableStationsDetail() {
    return conquerableStationsDetail;
  }

  public void setConquerableStationsDetail(
                                           String conquerableStationsDetail) {
    this.conquerableStationsDetail = conquerableStationsDetail;
  }

  public SyncTracker.SyncState getErrorListStatus() {
    return errorListStatus;
  }

  public void setErrorListStatus(
                                 SyncTracker.SyncState errorListStatus) {
    this.errorListStatus = errorListStatus;
  }

  public String getErrorListDetail() {
    return errorListDetail;
  }

  public void setErrorListDetail(
                                 String errorListDetail) {
    this.errorListDetail = errorListDetail;
  }

  public SyncTracker.SyncState getFacWarStatsStatus() {
    return facWarStatsStatus;
  }

  public void setFacWarStatsStatus(
                                   SyncTracker.SyncState facWarStatsStatus) {
    this.facWarStatsStatus = facWarStatsStatus;
  }

  public String getFacWarStatsDetail() {
    return facWarStatsDetail;
  }

  public void setFacWarStatsDetail(
                                   String facWarStatsDetail) {
    this.facWarStatsDetail = facWarStatsDetail;
  }

  public SyncTracker.SyncState getFacWarTopStatsStatus() {
    return facWarTopStatsStatus;
  }

  public void setFacWarTopStatsStatus(
                                      SyncTracker.SyncState facWarTopStatsStatus) {
    this.facWarTopStatsStatus = facWarTopStatsStatus;
  }

  public String getFacWarTopStatsDetail() {
    return facWarTopStatsDetail;
  }

  public void setFacWarTopStatsDetail(
                                      String facWarTopStatsDetail) {
    this.facWarTopStatsDetail = facWarTopStatsDetail;
  }

  public SyncTracker.SyncState getRefTypeStatus() {
    return refTypeStatus;
  }

  public void setRefTypeStatus(
                               SyncTracker.SyncState refTypeStatus) {
    this.refTypeStatus = refTypeStatus;
  }

  public String getRefTypeDetail() {
    return refTypeDetail;
  }

  public void setRefTypeDetail(
                               String refTypeDetail) {
    this.refTypeDetail = refTypeDetail;
  }

  public SyncTracker.SyncState getSkillTreeStatus() {
    return skillTreeStatus;
  }

  public void setSkillTreeStatus(
                                 SyncTracker.SyncState skillTreeStatus) {
    this.skillTreeStatus = skillTreeStatus;
  }

  public String getSkillTreeDetail() {
    return skillTreeDetail;
  }

  public void setSkillTreeDetail(
                                 String skillTreeDetail) {
    this.skillTreeDetail = skillTreeDetail;
  }

  public SyncTracker.SyncState getFacWarSystemsStatus() {
    return facWarSystemsStatus;
  }

  public void setFacWarSystemsStatus(
                                     SyncTracker.SyncState facWarSystemsStatus) {
    this.facWarSystemsStatus = facWarSystemsStatus;
  }

  public String getFacWarSystemsDetail() {
    return facWarSystemsDetail;
  }

  public void setFacWarSystemsDetail(
                                     String facWarSystemsDetail) {
    this.facWarSystemsDetail = facWarSystemsDetail;
  }

  public SyncTracker.SyncState getMapJumpStatus() {
    return mapJumpStatus;
  }

  public void setMapJumpStatus(
                               SyncTracker.SyncState mapJumpStatus) {
    this.mapJumpStatus = mapJumpStatus;
  }

  public String getMapJumpDetail() {
    return mapJumpDetail;
  }

  public void setMapJumpDetail(
                               String mapJumpDetail) {
    this.mapJumpDetail = mapJumpDetail;
  }

  public SyncTracker.SyncState getMapKillStatus() {
    return mapKillStatus;
  }

  public void setMapKillStatus(
                               SyncTracker.SyncState mapKillStatus) {
    this.mapKillStatus = mapKillStatus;
  }

  public String getMapKillDetail() {
    return mapKillDetail;
  }

  public void setMapKillDetail(
                               String mapKillDetail) {
    this.mapKillDetail = mapKillDetail;
  }

  public SyncTracker.SyncState getSovereigntyStatus() {
    return sovereigntyStatus;
  }

  public void setSovereigntyStatus(
                                   SyncTracker.SyncState sovereigntyStatus) {
    this.sovereigntyStatus = sovereigntyStatus;
  }

  public String getSovereigntyDetail() {
    return sovereigntyDetail;
  }

  public void setSovereigntyDetail(
                                   String sovereigntyDetail) {
    this.sovereigntyDetail = sovereigntyDetail;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((allianceListDetail == null) ? 0 : allianceListDetail.hashCode());
    result = prime * result + ((allianceListStatus == null) ? 0 : allianceListStatus.hashCode());
    result = prime * result + ((callListDetail == null) ? 0 : callListDetail.hashCode());
    result = prime * result + ((callListStatus == null) ? 0 : callListStatus.hashCode());
    result = prime * result + ((conquerableStationsDetail == null) ? 0 : conquerableStationsDetail.hashCode());
    result = prime * result + ((conquerableStationsStatus == null) ? 0 : conquerableStationsStatus.hashCode());
    result = prime * result + ((errorListDetail == null) ? 0 : errorListDetail.hashCode());
    result = prime * result + ((errorListStatus == null) ? 0 : errorListStatus.hashCode());
    result = prime * result + ((facWarStatsDetail == null) ? 0 : facWarStatsDetail.hashCode());
    result = prime * result + ((facWarStatsStatus == null) ? 0 : facWarStatsStatus.hashCode());
    result = prime * result + ((facWarSystemsDetail == null) ? 0 : facWarSystemsDetail.hashCode());
    result = prime * result + ((facWarSystemsStatus == null) ? 0 : facWarSystemsStatus.hashCode());
    result = prime * result + ((facWarTopStatsDetail == null) ? 0 : facWarTopStatsDetail.hashCode());
    result = prime * result + ((facWarTopStatsStatus == null) ? 0 : facWarTopStatsStatus.hashCode());
    result = prime * result + (finished ? 1231 : 1237);
    result = prime * result + ((mapJumpDetail == null) ? 0 : mapJumpDetail.hashCode());
    result = prime * result + ((mapJumpStatus == null) ? 0 : mapJumpStatus.hashCode());
    result = prime * result + ((mapKillDetail == null) ? 0 : mapKillDetail.hashCode());
    result = prime * result + ((mapKillStatus == null) ? 0 : mapKillStatus.hashCode());
    result = prime * result + ((refTypeDetail == null) ? 0 : refTypeDetail.hashCode());
    result = prime * result + ((refTypeStatus == null) ? 0 : refTypeStatus.hashCode());
    result = prime * result + ((serverStatusDetail == null) ? 0 : serverStatusDetail.hashCode());
    result = prime * result + ((serverStatusStatus == null) ? 0 : serverStatusStatus.hashCode());
    result = prime * result + ((skillTreeDetail == null) ? 0 : skillTreeDetail.hashCode());
    result = prime * result + ((skillTreeStatus == null) ? 0 : skillTreeStatus.hashCode());
    result = prime * result + ((sovereigntyDetail == null) ? 0 : sovereigntyDetail.hashCode());
    result = prime * result + ((sovereigntyStatus == null) ? 0 : sovereigntyStatus.hashCode());
    result = prime * result + (int) (syncEnd ^ (syncEnd >>> 32));
    result = prime * result + (int) (syncStart ^ (syncStart >>> 32));
    result = prime * result + (int) (tid ^ (tid >>> 32));
    return result;
  }

  @Override
  public boolean equals(
                        Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    RefSyncTracker other = (RefSyncTracker) obj;
    if (allianceListDetail == null) {
      if (other.allianceListDetail != null) return false;
    } else if (!allianceListDetail.equals(other.allianceListDetail)) return false;
    if (allianceListStatus != other.allianceListStatus) return false;
    if (callListDetail == null) {
      if (other.callListDetail != null) return false;
    } else if (!callListDetail.equals(other.callListDetail)) return false;
    if (callListStatus != other.callListStatus) return false;
    if (conquerableStationsDetail == null) {
      if (other.conquerableStationsDetail != null) return false;
    } else if (!conquerableStationsDetail.equals(other.conquerableStationsDetail)) return false;
    if (conquerableStationsStatus != other.conquerableStationsStatus) return false;
    if (errorListDetail == null) {
      if (other.errorListDetail != null) return false;
    } else if (!errorListDetail.equals(other.errorListDetail)) return false;
    if (errorListStatus != other.errorListStatus) return false;
    if (facWarStatsDetail == null) {
      if (other.facWarStatsDetail != null) return false;
    } else if (!facWarStatsDetail.equals(other.facWarStatsDetail)) return false;
    if (facWarStatsStatus != other.facWarStatsStatus) return false;
    if (facWarSystemsDetail == null) {
      if (other.facWarSystemsDetail != null) return false;
    } else if (!facWarSystemsDetail.equals(other.facWarSystemsDetail)) return false;
    if (facWarSystemsStatus != other.facWarSystemsStatus) return false;
    if (facWarTopStatsDetail == null) {
      if (other.facWarTopStatsDetail != null) return false;
    } else if (!facWarTopStatsDetail.equals(other.facWarTopStatsDetail)) return false;
    if (facWarTopStatsStatus != other.facWarTopStatsStatus) return false;
    if (finished != other.finished) return false;
    if (mapJumpDetail == null) {
      if (other.mapJumpDetail != null) return false;
    } else if (!mapJumpDetail.equals(other.mapJumpDetail)) return false;
    if (mapJumpStatus != other.mapJumpStatus) return false;
    if (mapKillDetail == null) {
      if (other.mapKillDetail != null) return false;
    } else if (!mapKillDetail.equals(other.mapKillDetail)) return false;
    if (mapKillStatus != other.mapKillStatus) return false;
    if (refTypeDetail == null) {
      if (other.refTypeDetail != null) return false;
    } else if (!refTypeDetail.equals(other.refTypeDetail)) return false;
    if (refTypeStatus != other.refTypeStatus) return false;
    if (serverStatusDetail == null) {
      if (other.serverStatusDetail != null) return false;
    } else if (!serverStatusDetail.equals(other.serverStatusDetail)) return false;
    if (serverStatusStatus != other.serverStatusStatus) return false;
    if (skillTreeDetail == null) {
      if (other.skillTreeDetail != null) return false;
    } else if (!skillTreeDetail.equals(other.skillTreeDetail)) return false;
    if (skillTreeStatus != other.skillTreeStatus) return false;
    if (sovereigntyDetail == null) {
      if (other.sovereigntyDetail != null) return false;
    } else if (!sovereigntyDetail.equals(other.sovereigntyDetail)) return false;
    if (sovereigntyStatus != other.sovereigntyStatus) return false;
    if (syncEnd != other.syncEnd) return false;
    if (syncStart != other.syncStart) return false;
    if (tid != other.tid) return false;
    return true;
  }

  @Override
  public String toString() {
    return "RefSyncTracker [tid=" + tid + ", syncStart=" + syncStart + ", finished=" + finished + ", syncEnd=" + syncEnd + ", serverStatusStatus="
        + serverStatusStatus + ", serverStatusDetail=" + serverStatusDetail + ", callListStatus=" + callListStatus + ", callListDetail=" + callListDetail
        + ", allianceListStatus=" + allianceListStatus + ", allianceListDetail=" + allianceListDetail + ", conquerableStationsStatus="
        + conquerableStationsStatus + ", conquerableStationsDetail=" + conquerableStationsDetail + ", errorListStatus=" + errorListStatus + ", errorListDetail="
        + errorListDetail + ", facWarStatsStatus=" + facWarStatsStatus + ", facWarStatsDetail=" + facWarStatsDetail + ", facWarTopStatsStatus="
        + facWarTopStatsStatus + ", facWarTopStatsDetail=" + facWarTopStatsDetail + ", refTypeStatus=" + refTypeStatus + ", refTypeDetail=" + refTypeDetail
        + ", skillTreeStatus=" + skillTreeStatus + ", skillTreeDetail=" + skillTreeDetail + ", facWarSystemsStatus=" + facWarSystemsStatus
        + ", facWarSystemsDetail=" + facWarSystemsDetail + ", mapJumpStatus=" + mapJumpStatus + ", mapJumpDetail=" + mapJumpDetail + ", mapKillStatus="
        + mapKillStatus + ", mapKillDetail=" + mapKillDetail + ", sovereigntyStatus=" + sovereigntyStatus + ", sovereigntyDetail=" + sovereigntyDetail + "]";
  }

  public static RefSyncTracker finishTracker(
                                             final RefSyncTracker tracker) {
    try {
      return EveKitRefDataProvider.getFactory().runTransaction(new RunInTransaction<RefSyncTracker>() {
        @Override
        public RefSyncTracker run() throws Exception {
          tracker.setFinished(true);
          tracker.setSyncEnd(OrbitalProperties.getCurrentTime());
          return EveKitRefDataProvider.getFactory().getEntityManager().merge(tracker);
        }
      });
    } catch (Exception e) {
      log.log(Level.SEVERE, "query error", e);
    }
    return null;
  }

  public static RefSyncTracker updateTracker(
                                             final RefSyncTracker tracker) {
    try {
      return EveKitRefDataProvider.getFactory().runTransaction(new RunInTransaction<RefSyncTracker>() {
        @Override
        public RefSyncTracker run() throws Exception {
          return EveKitRefDataProvider.getFactory().getEntityManager().merge(tracker);
        }
      });
    } catch (Exception e) {
      log.log(Level.SEVERE, "query error", e);
    }
    return null;
  }

  public static RefSyncTracker get(
                                   final long tid) {
    try {
      return EveKitRefDataProvider.getFactory().runTransaction(new RunInTransaction<RefSyncTracker>() {
        @Override
        public RefSyncTracker run() throws Exception {
          TypedQuery<RefSyncTracker> getter = EveKitRefDataProvider.getFactory().getEntityManager().createNamedQuery("RefSyncTracker.get",
                                                                                                                     RefSyncTracker.class);
          getter.setParameter("tid", tid);
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

  public static RefSyncTracker getUnfinishedTracker() {
    try {
      return EveKitRefDataProvider.getFactory().runTransaction(new RunInTransaction<RefSyncTracker>() {
        @Override
        public RefSyncTracker run() throws Exception {
          TypedQuery<RefSyncTracker> getter = EveKitRefDataProvider.getFactory().getEntityManager().createNamedQuery("RefSyncTracker.getUnfinished",
                                                                                                                     RefSyncTracker.class);
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

  public static RefSyncTracker getLatestFinishedTracker() {
    try {
      return EveKitRefDataProvider.getFactory().runTransaction(new RunInTransaction<RefSyncTracker>() {
        @Override
        public RefSyncTracker run() throws Exception {
          TypedQuery<RefSyncTracker> getter = EveKitRefDataProvider.getFactory().getEntityManager().createNamedQuery("RefSyncTracker.getLatestFinished",
                                                                                                                     RefSyncTracker.class);
          getter.setMaxResults(1);
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

  public static RefSyncTracker createOrGetUnfinishedTracker() {
    try {
      return EveKitRefDataProvider.getFactory().runTransaction(new RunInTransaction<RefSyncTracker>() {
        @Override
        public RefSyncTracker run() throws Exception {
          RefSyncTracker result = getUnfinishedTracker();
          if (result != null) return result;
          result = new RefSyncTracker();
          result.syncStart = OrbitalProperties.getCurrentTime();
          result.setFinished(false);
          return EveKitRefDataProvider.getFactory().getEntityManager().merge(result);
        }
      });
    } catch (Exception e) {
      log.log(Level.SEVERE, "query error", e);
    }
    return null;
  }

  public static List<RefSyncTracker> getAllUnfinishedTrackers() {
    try {
      return EveKitRefDataProvider.getFactory().runTransaction(new RunInTransaction<List<RefSyncTracker>>() {
        @Override
        public List<RefSyncTracker> run() throws Exception {
          TypedQuery<RefSyncTracker> getter = EveKitRefDataProvider.getFactory().getEntityManager().createNamedQuery("RefSyncTracker.getUnfinished",
                                                                                                                     RefSyncTracker.class);
          return getter.getResultList();
        }
      });
    } catch (Exception e) {
      log.log(Level.SEVERE, "query error", e);
    }
    return null;
  }

  public static List<RefSyncTracker> getHistory(
                                                final long contid,
                                                final int maxResults) {
    try {
      return EveKitRefDataProvider.getFactory().runTransaction(new RunInTransaction<List<RefSyncTracker>>() {
        @Override
        public List<RefSyncTracker> run() throws Exception {
          TypedQuery<RefSyncTracker> getter = EveKitRefDataProvider.getFactory().getEntityManager().createNamedQuery("RefSyncTracker.getHistory",
                                                                                                                     RefSyncTracker.class);
          getter.setParameter("start", contid < 0 ? Long.MAX_VALUE : contid);
          getter.setMaxResults(maxResults);
          return getter.getResultList();
        }
      });
    } catch (Exception e) {
      log.log(Level.SEVERE, "query error", e);
    }
    return null;
  }

  public static List<RefSyncTracker> getSummary(
                                                final Date fromDate) {
    try {
      return EveKitRefDataProvider.getFactory().runTransaction(new RunInTransaction<List<RefSyncTracker>>() {
        @Override
        public List<RefSyncTracker> run() throws Exception {
          TypedQuery<RefSyncTracker> getter = EveKitRefDataProvider.getFactory().getEntityManager().createNamedQuery("RefSyncTracker.getSummary",
                                                                                                                     RefSyncTracker.class);
          getter.setParameter("account", fromDate);
          return getter.getResultList();
        }
      });
    } catch (Exception e) {
      log.log(Level.SEVERE, "query error", e);
    }
    return null;
  }

  public static String summarizeErrors(
                                       Date day)
    throws IOException {
    StringBuilder summary = new StringBuilder();
    summary.append("Reference Sync Tracker Error Summary on ");
    long days = day.getTime() / (1000 * 60 * 60 * 24);
    Date dayStart = new Date(days * 1000 * 60 * 60 * 24 + 1);
    Date nextDay = new Date(dayStart.getTime() + (1000 * 60 * 60 * 24) - 1);
    summary.append(DateFormat.getDateInstance().format(dayStart)).append('\n');
    List<RefSyncTracker> result = getSummary(dayStart);
    if (result == null) result = Collections.emptyList();

    // Process sync results with error.
    int errorCount = 0;
    Map<String, Map<String, AtomicInteger>> data = new HashMap<String, Map<String, AtomicInteger>>();
    for (RefSyncTracker next : result) {
      if (new Date(next.getSyncEnd()).after(nextDay)) continue;
      if (next.serverStatusStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("serverStatus", next.serverStatusDetail, data);
      } else if (next.callListStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("callList", next.callListDetail, data);
      } else if (next.allianceListStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("allianceList", next.allianceListDetail, data);
      } else if (next.conquerableStationsStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("conquerableStations", next.conquerableStationsDetail, data);
      } else if (next.errorListStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("errorList", next.errorListDetail, data);
      } else if (next.facWarStatsStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("facWarStats", next.facWarStatsDetail, data);
      } else if (next.facWarTopStatsStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("facWarTopStats", next.facWarTopStatsDetail, data);
      } else if (next.refTypeStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("refType", next.refTypeDetail, data);
      } else if (next.skillTreeStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("skillTree", next.skillTreeDetail, data);
      } else if (next.facWarSystemsStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("facWarSystems", next.facWarSystemsDetail, data);
      } else if (next.mapJumpStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("mapJump", next.mapJumpDetail, data);
      } else if (next.mapKillStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("mapKillStatus", next.mapKillDetail, data);
      } else if (next.sovereigntyStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("sovereignty", next.sovereigntyDetail, data);
      }

    }

    summary.append(errorCount).append(" trackers with errors\n");

    for (String category : data.keySet()) {
      summary.append("Category - ").append(category).append(":\n");
      for (String reason : data.get(category).keySet()) {
        summary.append("    ").append(reason).append(" - ").append(data.get(category).get(reason).get()).append('\n');
      }
      summary.append('\n');
    }

    return summary.toString();
  }

}
