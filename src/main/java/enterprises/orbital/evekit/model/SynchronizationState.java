package enterprises.orbital.evekit.model;

// character and corporation account synchronization states
public enum SynchronizationState {
                                  SYNC_CHAR_START(-1),
                                  SYNC_CHAR_ACCOUNTSTATUS(33554432L),
                                  SYNC_CHAR_ACCOUNTBALANCE(1L),
                                  SYNC_CHAR_ASSETLIST(2L),
                                  SYNC_CHAR_CALENDAREVENTATTENDEES(4L),
                                  SYNC_CHAR_CHARACTERSHEET(8L),
                                  SYNC_CHAR_PARTIALCHARACTERSHEET(2147483648L),
                                  SYNC_CHAR_CHATCHANNELS(536870912L),
                                  SYNC_CHAR_CONTACTLIST(16L),
                                  SYNC_CHAR_CONTACTNOTIFICATIONS(32L),
                                  SYNC_CHAR_BLUEPRINTS(2L),
                                  SYNC_CHAR_BOOKMARKS(268435456L),
                                  SYNC_CHAR_CONTRACTS(67108864L),
                                  SYNC_CHAR_CONTRACTITEMS(67108864L),
                                  SYNC_CHAR_CONTRACTBIDS(67108864L),
                                  SYNC_CHAR_FACWARSTATS(64L),
                                  SYNC_CHAR_INDUSTRYJOBS(128L),
                                  SYNC_CHAR_INDUSTRYJOBSHISTORY(128L),
                                  SYNC_CHAR_KILLLOG(256L),
                                  SYNC_CHAR_MAILBODIES(512L),
                                  SYNC_CHAR_MAILINGLISTS(1024L),
                                  SYNC_CHAR_MAILMESSAGES(2048L),
                                  SYNC_CHAR_MARKETORDERS(4096L),
                                  SYNC_CHAR_MEDALS(8192L),
                                  SYNC_CHAR_NOTIFICATIONS(16384L),
                                  SYNC_CHAR_NOTIFICATIONTEXTS(32768L),
                                  SYNC_CHAR_PLANETARY_COLONIES(2L),
                                  SYNC_CHAR_RESEARCH(65536L),
                                  SYNC_CHAR_SKILLINTRAINING(131072L),
                                  SYNC_CHAR_SKILLQUEUE(262144L),
                                  SYNC_CHAR_SKILLS(1073741824L),
                                  SYNC_CHAR_STANDINGS(524288L),
                                  SYNC_CHAR_UPCOMINGCALENDAREVENTS(1048576L),
                                  SYNC_CHAR_WALLETJOURNAL(2097152L),
                                  SYNC_CHAR_WALLETTRANSACTIONS(4194304L),
                                  SYNC_CHAR_END(-1),
                                  SYNC_CORP_START(-1),
                                  SYNC_CORP_ACCOUNTBALANCE(1L),
                                  SYNC_CORP_ASSETLIST(2L),
                                  SYNC_CORP_CORPSHEET(8L),
                                  SYNC_CORP_CONTACTLIST(16L),
                                  SYNC_CORP_CUSTOMSOFFICE(2L),
                                  SYNC_CORP_BLUEPRINTS(2L),
                                  SYNC_CORP_BOOKMARKS(67108864L),
                                  SYNC_CORP_CONTRACTS(8388608L),
                                  SYNC_CORP_CONTRACTITEMS(8388608L),
                                  SYNC_CORP_CONTRACTBIDS(8388608L),
                                  SYNC_CORP_FACWARSTATS(64L),
                                  SYNC_CORP_FACILITIES(128L),
                                  SYNC_CORP_INDUSTRYJOBS(128L),
                                  SYNC_CORP_INDUSTRYJOBSHISTORY(128L),
                                  SYNC_CORP_KILLLOG(256L),
                                  SYNC_CORP_MARKETORDERS(4096L),
                                  SYNC_CORP_MEMBERMEDALS(4L),
                                  SYNC_CORP_STANDINGS(262144L),
                                  SYNC_CORP_WALLETJOURNAL(1048576L),
                                  SYNC_CORP_WALLETTRANSACTIONS(2097152L),
                                  SYNC_CORP_SECURITY(512L),
                                  SYNC_CORP_CONTAINERLOG(32L),
                                  SYNC_CORP_MEMBERSECURITYLOG(1024L),
                                  SYNC_CORP_MEMBERTRACKING(33554432L + 2048L),
                                  SYNC_CORP_CORPMEDALS(8192L),
                                  SYNC_CORP_OUTPOSTLIST(16384L),
                                  SYNC_CORP_OUTPOSTDETAIL(32768L),
                                  SYNC_CORP_SHAREHOLDERS(65536L),
                                  SYNC_CORP_STARBASELIST(524288L),
                                  SYNC_CORP_STARBASEDETAIL(131072L),
                                  SYNC_CORP_CORPTITLES(4194304L),
                                  SYNC_CORP_END(-1);

  private final long mask;

  private SynchronizationState(long m) {
    this.mask = m;
  }

  public boolean isAllowed(
                           long test) {
    return (test & mask) != 0L;
  }

}