package enterprises.orbital.evekit.model;

/**
 * Possible states for an ESI category sync tracker.
 */
public enum ESISyncState {
  /**
   * Haven't started processing this category yet.
   */
  NOT_PROCESSED,

  /**
   * Updated category with no errors.
   */
  FINISHED,

  /**
   * Category not updated because of an unrecoverable error.
   */
  ERROR,

  /**
   * Category completed but not in an expected way.  This may be benign (e.g. one ore more missing scoped).
   */
  WARNING
}
