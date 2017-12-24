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
  // TODO
  REF_SERVER_STATUS("EVE server status", null);

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
