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
  REF_SERVER_STATUS("EVE server status", null),
  REF_ALLIANCE("EVE alliance list", null),
  REF_SOV_MAP("EVE sovereignty map", null),
  REF_SOV_CAMPAIGN("EVE sovereignty campaigns and participants", null),
  REF_SOV_STRUCTURE("EVE sovereignty structures", null),
  REF_FW_WARS("EVE faction wars", null),
  REF_FW_STATS("EVE faction war statistical overview", null),
  REF_FW_SYSTEMS("EVE faction war systems", null),
  REF_FW_FACTION_LEADERBOARD("EVE faction war faction leader board", null),
  REF_FW_CORP_LEADERBOARD("EVE faction war corporation leader board", null),
  REF_FW_CHAR_LEADERBOARD("EVE faction war character leader board", null);

  String description;
  String scope;

  ESIRefSyncEndpoint(String description, String scope) {
    this.description = description;
    this.scope = scope;
  }

  public String getDescription() {
    return description;
  }

  public String getScope() {
    return scope;
  }

}
