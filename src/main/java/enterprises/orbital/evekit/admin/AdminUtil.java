package enterprises.orbital.evekit.admin;

import enterprises.orbital.base.OrbitalProperties;

/**
 * Collection of utilities used by many EveKit modules.
 */
public abstract class AdminUtil {
  // Constants for all known global system property names
  public static final String PROP_DEFAULT_STATIC_DB_ACCESS_LIMIT = "enterprises.orbital.evekit.default_static_db_access_limit";
  public static final String PROP_SYNC_STEP_MAX                  = "enterprises.orbital.evekit.sync_step_max";
  public static final String PROP_SKIP_SYNC                      = "enterprises.orbital.evekit.model.skip_sync";
  public static final String PROP_SYNC_WARNING_DELAY             = "enterprises.orbital.evekit.sync_warning_delay";
  public static final String PROP_SYNC_ATTEMPT_SEPARATION        = "enterprises.orbital.evekit.sync_attempt_separation";
  public static final String PROP_SYNC_TERM_DELAY                = "enterprises.orbital.evekit.sync_terminate_delay";
  public static final String PROP_ADMIN_EMAIL                    = "enterprises.orbital.evekit.admin_email";
  public static final String PROP_SITE_EMAIL                     = "enterprises.orbital.evekit.site_email";
  // Maximum number of access keys per account
  public static final String PROP_ACCESS_KEY_LIMIT               = "enterprises.orbital.evekit.access_key_limit";
  public static final int    DEF_ACCESS_KEY_LIMIT                = 20;
  public static final String PROP_SYNC_ACCOUNT_LIMIT             = "enterprises.orbital.evekit.sync_account_limit";
  public static final String PROP_SNAP_GEN_LIMIT                 = "enterprises.orbital.evekit.snapshot_generator_limit";
  public static final String PROP_END_CHECK_CAP                  = "enterprises.orbital.evekit.sync_end_check_cap";
  public static final String PROP_WALLET_CACHE_TIME_LIMIT        = "enterprises.orbital.evekit.wallet_cache_time_limit";
  public static final String PROP_DISABLE_SYNC_TRACKER_CACHE     = "enterprises.orbital.evekit.disable_sync_tracker_cache";
  public static final String PROP_STANDING_CACHE_TIME_LIMIT      = "enterprises.orbital.evekit.standing_cache_time_limit";
  public static final String PROP_SNAPSHOT_MACHINE_TYPE          = "enterprises.orbital.evekit.snapshot_machine_type";
  public static final String PROP_KILLLOG_CACHE_TIME_LIMIT       = "enterprises.orbital.evekit.killlog_cache_time_limit";
  public static final String PROP_SYNCLOCK_CACHE_TIME_LIMIT      = "enterprises.orbital.evekit.synclock_cache_time_limit";
  public static final String PROP_TESTSITE_LIVE_SERVER           = "enterprises.orbital.evekit.testsite.live_server";
  public static final String PROP_TESTSITE_LIVE_PORT             = "enterprises.orbital.evekit.testsite.live_port";
  public static final String PROP_TESTSITE_LIVE_USERNAME         = "enterprises.orbital.evekit.testsite.live_username";
  public static final String PROP_TESTSITE_LIVE_PASSWD           = "enterprises.orbital.evekit.testsite.live_passwd";
  public static final String PROP_MIN_TIME_BETWEEN_SSCODE        = "enterprises.orbital.evekit.serverside.mindelay";
  public static final String PROP_MAX_SSCODE_LOG_SIZE            = "enterprises.orbital.evekit.serverside.maxlogsize";
  public static final String PROP_ASSET_CACHE_TIME_LIMIT         = "enterprises.orbital.evekit.asset_cache_time_limit";

  public static String getEveKitBuild() {
    return OrbitalProperties.getGlobalProperty("enterprises.orbital.evekit.build");
  }

  public static String getEveKitBuildDate() {
    return OrbitalProperties.getGlobalProperty("enterprises.orbital.evekit.builddate");
  }

  public static String getEveKitBuildSeries() {
    return OrbitalProperties.getGlobalProperty("enterprises.orbital.evekit.buildseries");
  }

  public static String getEveKitBuildBranch() {
    return OrbitalProperties.getGlobalProperty("enterprises.orbital.evekit.branch");
  }

  public static String getEveKitBuildString() {
    return getEveKitBuild() + "-" + getEveKitBuildDate() + " (" + getEveKitBuildSeries() + ")";
  }

  public static boolean isTestSite() {
    return OrbitalProperties.getGlobalProperty("enterprises.orbital.evekit.site", "live").equals("test");
  }

}
