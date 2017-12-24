package enterprises.orbital.evekit.model;

/**
 * List of ESI synchronization endpoints.  Each endpoint consists of:
 * <p>
 * <ul>
 * <li>a description</li>
 * <li>an ESI scope required to access the endpoint</li>
 * <li>an indication as to whether the endpoint supports characters or corporations</li>
 * </ul>
 * <p>
 * These endpoints represent all the current data which can be synchronized against a SynchronizedEveAccount.
 * In particular, each endpoint is backed by a particular "synchronizer" which is responsible for updating
 * data for the endpoint.
 */
public enum ESISyncEndpoint {
  // TODO
  CHAR_ASSETS("Access to character assets, names and locations.", "esi-assets.read_assets.v1", true),
  // TODO
  CORP_ASSETS("Access to corporation assets, names and locations.", "esi-assets.read_corporation_assets.v1", false),
  // TODO
  CHAR_BOOKMARKS("Access to character personal bookmarks and bookmark folders.", "esi-bookmarks.read_character_bookmarks.v1", true),
  // TODO
  CORP_BOOKMARKS("Access to corporation bookmarks and bookmark folder.", "esi-bookmarks.read_corporation_bookmarks.v1", false),
  // TODO
  CHAR_CALENDAR("Access to character calendar events, summaries and attendees.", "esi-calendar.read_calendar_events.v1", true),
  // TODO
  CHAR_CHANNELS("Access to character chat channel list.", "esi-characters.read_chat_channels.v1", true),
  // TODO
  CHAR_MEDALS("Access to character medals.", "esi-characters.read_medals.v1", true),
  // TODO
  CHAR_STANDINGS("Access to character standings.", "esi-characters.read_standings.v1", true),
  // TODO
  CHAR_AGENTS("Access to character agent research.", "esi-characters.read_agents_research.v1", true),
  // TODO
  CHAR_BLUEPRINTS("Access to character blueprint list.", "esi-characters.read_blueprints.v1", true),
  // TODO
  CHAR_FATIGUE("Access to character jump activation and fatigue information.", "esi-characters.read_fatigue.v1", true),
  // TODO
  CHAR_NOTIFICATIONS("Access to character notifications and contacts.", "esi-characters.read_notifications.v1", true),
  // TODO
  CHAR_CORP_ROLES("Access to character corporation roles.", "esi-characters.read_corporation_roles.v1", true),
  // TODO
  CHAR_TITLES("Access to character titles.", "esi-characters.read_titles.v1", true),
  // TODO
  CHAR_CLONES("Access to character clone list.", "esi-clones.read_clones.v1", true),
  // TODO
  CHAR_IMPLANTS("Access to active character implant list.", "esi-clones.read_implants.v1", true),
  // TODO
  CHAR_CONTACTS("Access to character contact list and labels.", "esi-characters.read_contacts.v1", true),
  // TODO
  CORP_CONTACTS("Access to corporation contact list.", "esi-corporations.read_contacts.v1", false),
  // TODO
  CORP_ALLIANCE_CONTACTS("Access to alliance contact list.", "esi-alliances.read_contacts.v1", false),
  // TODO
  CHAR_CONTRACTS("Access to character contracts, items and bids.", "esi-contracts.read_character_contracts.v1", true),
  // TODO
  CORP_CONTRACTS("Access to corporation contracts, items and bids.", "esi-contracts.read_corporation_contracts.v1", false),
  // TODO
  CORP_BLUEPRINTS("Access to corporation blueprint list.", "esi-corporations.read_blueprints.v1", false),
  // TODO
  CORP_CONTAINER_LOGS("Access to the logs of corporation audited secure containers.", "esi-corporations.read_container_logs.v1", false),
  // TODO
  CORP_MEMBERSHIP("Access to corporation membership list, roles and role history.", "esi-corporations.read_corporation_membership.v1", false),
  // TODO
  CORP_DIVISIONS("Access to corporation division list.", "esi-corporations.read_divisions.v1", false),
  // TODO
  CORP_FACILITIES("Access to corporation facilities list and details.", "esi-corporations.read_facilities.v1", false),
  // TODO
  CORP_MEDALS("Access to corporation medal list and issued medals.", "esi-corporations.read_medals.v1", false),
  // TODO
  CORP_STANDINGS("Access to corporation standings.", "esi-corporations.read_standings.v1", false),
  // TODO
  CORP_STARBASES("Access to corporation starbase list and details.", "esi-corporations.read_starbases.v1", false),
  // TODO
  CORP_STRUCTURES("Access to list of corporation structures.", "esi-corporations.read_structures.v1", false),
  // TODO
  CORP_TITLES("Access to corporation title list and member titles.", "esi-corporations.read_titles.v1", false),
  // TODO
  CORP_TRACK_MEMBERS("Access to corporation member tracking information and limits.", "esi-corporations.track_members.v1", false),
  // TODO
  CORP_WALLET("Access to corporation wallet account balances, journal, transactions and shareholder list", "esi-wallet.read_corporation_wallets.v1", false),
  // TODO
  CORP_FACTION_WAR("Access to corporation faction warfare statistics.", "esi-corporations.read_fw_stats.v1", false),
  // TODO
  CHAR_FACTION_WAR("Access to character faction warfare statistics.", "esi-characters.read_fw_stats.v1", true),
  // TODO
  CHAR_FITTINGS("Access to character ship fittings list.", "esi-fittings.read_fittings.v1", true),
  // TODO
  CHAR_FLEETS("Access to current character fleet, status, wings and membership.", "esi-fleets.read_fleet.v1", true),
  // TODO
  CHAR_INDUSTRY("Access to character industry jobs.", "esi-industry.read_character_jobs.v1", true),
  // TODO
  CHAR_MINING("Access to character mining ledger.", "esi-industry.read_character_mining.v1", true),
  // TODO
  CORP_INDUSTRY("Access to corporation industry jobs.", "esi-industry.read_corporation_jobs.v1", false),
  // TODO
  CORP_MINING("Access to corporation mining ledger, observers, and observations.", "esi-industry.read_corporation_mining.v1", false),
  // TODO
  CHAR_KILL_MAIL("Access to character kill mails.", "esi-killmails.read_killmails.v1", true),
  // TODO
  CORP_KILL_MAIL("Access to corporation kill mails.", "esi-killmails.read_corporation_killmails.v1", false),
  // TODO
  CHAR_LOCATION("Access to character location.", "esi-location.read_location.v1", true),
  // TODO
  CHAR_SHIP_TYPE("Access to current character ship type.", "esi-location.read_ship_type.v1", true),
  // TODO
  CHAR_ONLINE("Access to whether the character is currently online.", "esi-location.read_online.v1", true),
  // TODO
  CHAR_LOYALTY("Access to character loyalty point totals.", "esi-characters.read_loyalty.v1", true),
  // TODO
  CHAR_MAIL("Access to character mail headers, labels, unread count, mailing lists, and mail bodies.", "esi-mail.read_mail.v1", true),
  // TODO
  CHAR_MARKET("Access to character market orders.", "esi-markets.read_character_orders.v1", true),
  // TODO
  CORP_MARKET("Access to corporation market orders.", "esi-markets.read_corporation_orders.v1", false),
  // TODO
  CHAR_OPPORTUNITIES("Access to character list of completed tasks.", "esi-characters.read_opportunities.v1", true),
  // TODO
  CHAR_PLANETS("Access to character list of planetary colonies and layout.", "esi-planets.manage_planets.v1", true),
  // TODO
  CORP_CUSTOMS("Access to corporation custom office list.", "esi-planets.read_customs_offices.v1", false),
  // TODO
  CHAR_SKILL_QUEUE("Access to character skill queue.", "esi-skills.read_skillqueue.v1", true),
  // TODO
  CHAR_SKILLS("Access to character skills and attributes.", "esi-skills.read_skills.v1", true),
  // TODO
  CHAR_WALLET("Access to character wallet balance, journal and transactions.", "esi-wallet.read_character_wallet.v1", true);

  String description;
  String scope;
  boolean isChar;

  ESISyncEndpoint(String description, String scope, boolean isChar) {
    this.description = description;
    this.scope = scope;
    this.isChar = isChar;
  }

  public String getDescription() {
    return description;
  }

  public String getScope() {
    return scope;
  }

  public boolean isChar() {
    return isChar;
  }

  public static ESISyncEndpoint[] getCharEndpoints() {
    return new ESISyncEndpoint[]{
        CHAR_ASSETS,
        CHAR_BOOKMARKS,
        CHAR_CALENDAR,
        CHAR_CHANNELS,
        CHAR_MEDALS,
        CHAR_STANDINGS,
        CHAR_AGENTS,
        CHAR_BLUEPRINTS,
        CHAR_FATIGUE,
        CHAR_NOTIFICATIONS,
        CHAR_CORP_ROLES,
        CHAR_TITLES,
        CHAR_CLONES,
        CHAR_IMPLANTS,
        CHAR_CONTACTS,
        CHAR_CONTRACTS,
        CHAR_FACTION_WAR,
        CHAR_FITTINGS,
        CHAR_FLEETS,
        CHAR_INDUSTRY,
        CHAR_MINING,
        CHAR_KILL_MAIL,
        CHAR_LOCATION,
        CHAR_SHIP_TYPE,
        CHAR_ONLINE,
        CHAR_LOYALTY,
        CHAR_MAIL,
        CHAR_MARKET,
        CHAR_OPPORTUNITIES,
        CHAR_PLANETS,
        CHAR_SKILL_QUEUE,
        CHAR_SKILLS,
        CHAR_WALLET
    };
  }

  public static ESISyncEndpoint[] getCorpEndpoints() {
    return new ESISyncEndpoint[]{
        CORP_ASSETS,
        CORP_BOOKMARKS,
        CORP_CONTACTS,
        CORP_ALLIANCE_CONTACTS,
        CORP_CONTRACTS,
        CORP_BLUEPRINTS,
        CORP_CONTAINER_LOGS,
        CORP_MEMBERSHIP,
        CORP_DIVISIONS,
        CORP_FACILITIES,
        CORP_MEDALS,
        CORP_STANDINGS,
        CORP_STARBASES,
        CORP_STRUCTURES,
        CORP_TITLES,
        CORP_TRACK_MEMBERS,
        CORP_WALLET,
        CORP_FACTION_WAR,
        CORP_INDUSTRY,
        CORP_MINING,
        CORP_KILL_MAIL,
        CORP_MARKET,
        CORP_CUSTOMS
    };
  }
}
