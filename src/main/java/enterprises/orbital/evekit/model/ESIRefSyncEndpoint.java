package enterprises.orbital.evekit.model;

/**
 * List of ESI reference synchronization endpoints.  Each endpoint consists of:
 * <p>
 * <ul>
 * <li>a description</li>
 * <li>an ESI scope required to access the endpoint (may be null)</li>
 * </ul>
 * <p>
 * These endpoints represent all the current data which can be synchronized for reference purposes.
 * In particular, each endpoint is backed by a particular "synchronizer" which is responsible for updating
 * data for the endpoint.
 */
public enum ESIRefSyncEndpoint {
  REF_SERVER_STATUS(null),
  REF_ALLIANCE(null),
  REF_SOV_MAP(null),
  REF_SOV_CAMPAIGN(null),
  REF_SOV_STRUCTURE(null),
  REF_FW_WARS(null),
  REF_FW_STATS(null),
  REF_FW_SYSTEMS(null),
  REF_FW_FACTION_LEADERBOARD(null),
  REF_FW_CORP_LEADERBOARD(null),
  REF_FW_CHAR_LEADERBOARD(null);

  ESIScope scope;

  ESIRefSyncEndpoint(ESIScope scope) {
    this.scope = scope;
  }

  public ESIScope getScope() {
    return scope;
  }

}
