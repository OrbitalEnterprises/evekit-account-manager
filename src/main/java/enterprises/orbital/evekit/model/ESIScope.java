package enterprises.orbital.evekit.model;

import java.util.Arrays;
import java.util.stream.Collectors;

import static enterprises.orbital.evekit.model.ESISyncEndpoint.CHAR_ASSETS;

public enum ESIScope {

  CHAR_READ_ASSETS("esi-assets.read_assets.v1", "Access to character assets, names and locations."),
  CORP_READ_ASSETS("esi-assets.read_corporation_assets.v1", "Access to corporation assets, names and locations."),
  CHAR_READ_BOOKMARKS("esi-bookmarks.read_character_bookmarks.v1", "Access to character personal bookmarks and bookmark folders."),
  CORP_READ_BOOKMARKS("esi-bookmarks.read_corporation_bookmarks.v1", "Access to corporation bookmarks and bookmark folder."),
  CHAR_READ_CALENDAR("esi-calendar.read_calendar_events.v1", "Access to character calendar events, summaries and attendees."),
  CHAR_READ_MEDALS("esi-characters.read_medals.v1", "Access to character medals."),
  CHAR_READ_STANDINGS("esi-characters.read_standings.v1", "Access to character standings."),
  CHAR_READ_AGENTS("esi-characters.read_agents_research.v1", "Access to character agent research."),
  CHAR_READ_BLUEPRINTS("esi-characters.read_blueprints.v1", "Access to character blueprint list."),
  CHAR_READ_FATIGUE("esi-characters.read_fatigue.v1", "Access to character jump activation and fatigue information."),
  CHAR_READ_NOTIFICATIONS("esi-characters.read_notifications.v1", "Access to character notifications and contacts."),
  CHAR_READ_CORP_ROLES("esi-characters.read_corporation_roles.v1", "Access to character corporation roles."),
  CHAR_READ_TITLES("esi-characters.read_titles.v1", "Access to character titles."),
  CHAR_READ_CLONES("esi-clones.read_clones.v1", "Access to character clone list."),
  CHAR_READ_IMPLANTS("esi-clones.read_implants.v1", "Access to active character implant list."),
  CHAR_READ_CONTACTS("esi-characters.read_contacts.v1", "Access to character contact list and labels."),
  CORP_READ_CONTACTS("esi-corporations.read_contacts.v1", "Access to corporation contact list."),
  CHAR_READ_ALLIANCE_CONTACTS("esi-alliances.read_contacts.v1", "Access to alliance contact list."),
  CHAR_READ_CONTRACTS("esi-contracts.read_character_contracts.v1", "Access to character contracts, items and bids."),
  CORP_READ_CONTRACTS("esi-contracts.read_corporation_contracts.v1", "Access to corporation contracts, items and bids."),
  CORP_READ_BLUEPRINTS("esi-corporations.read_blueprints.v1", "Access to corporation blueprint list."),
  CORP_READ_CONTAINER_LOGS("esi-corporations.read_container_logs.v1", "Access to the logs of corporation audited secure containers."),
  CORP_READ_MEMBERSHIP("esi-corporations.read_corporation_membership.v1", "Access to corporation membership list, roles and role history."),
  CORP_READ_DIVISIONS("esi-corporations.read_divisions.v1", "Access to corporation division list."),
  CORP_READ_FACILITIES("esi-corporations.read_facilities.v1", "Access to corporation facilities list and details."),
  CORP_READ_MEDALS("esi-corporations.read_medals.v1", "Access to corporation medal list and issued medals."),
  CORP_READ_STANDINGS("esi-corporations.read_standings.v1", "Access to corporation standings."),
  CORP_READ_STARBASES("esi-corporations.read_starbases.v1", "Access to corporation starbase list and details."),
  CORP_READ_STRUCTURES("esi-corporations.read_structures.v1", "Access to list of corporation structures."),
  CORP_READ_TITLES("esi-corporations.read_titles.v1", "Access to corporation title list and member titles."),
  CORP_READ_TRACK_MEMBERS("esi-corporations.track_members.v1", "Access to corporation member tracking information and limits."),
  CORP_READ_WALLET("esi-wallet.read_corporation_wallets.v1", "Access to corporation wallet account balances, journal, transactions and shareholder list"),
  CORP_READ_FACTION_WAR("esi-corporations.read_fw_stats.v1", "Access to corporation faction warfare statistics."),
  CHAR_READ_FACTION_WAR("esi-characters.read_fw_stats.v1", "Access to character faction warfare statistics."),
  CHAR_READ_FITTINGS("esi-fittings.read_fittings.v1", "Access to character ship fittings list."),
  CHAR_READ_FLEETS("esi-fleets.read_fleet.v1", "Access to current character fleet, status, wings and membership."),
  CHAR_READ_INDUSTRY("esi-industry.read_character_jobs.v1", "Access to character industry jobs."),
  CHAR_READ_MINING("esi-industry.read_character_mining.v1", "Access to character mining ledger."),
  CORP_READ_INDUSTRY("esi-industry.read_corporation_jobs.v1", "Access to corporation industry jobs."),
  CORP_READ_MINING("esi-industry.read_corporation_mining.v1", "Access to corporation mining ledger, observers, and observations."),
  CHAR_READ_KILL_MAIL("esi-killmails.read_killmails.v1", "Access to character kill mails."),
  CORP_READ_KILL_MAIL("esi-killmails.read_corporation_killmails.v1", "Access to corporation kill mails."),
  CHAR_READ_LOCATION("esi-location.read_location.v1", "Access to character location."),
  CHAR_READ_SHIP_TYPE("esi-location.read_ship_type.v1", "Access to current character ship type."),
  CHAR_READ_ONLINE("esi-location.read_online.v1", "Access to whether the character is currently online."),
  CHAR_READ_LOYALTY("esi-characters.read_loyalty.v1", "Access to character loyalty point totals."),
  CHAR_READ_MAIL("esi-mail.read_mail.v1", "Access to character mail headers, labels, unread count, mailing lists, and mail bodies."),
  CHAR_READ_MARKET("esi-markets.read_character_orders.v1", "Access to character market orders."),
  CORP_READ_MARKET("esi-markets.read_corporation_orders.v1", "Access to corporation market orders."),
  CHAR_READ_OPPORTUNITIES("esi-characters.read_opportunities.v1", "Access to character list of completed tasks."),
  CHAR_READ_PLANETS("esi-planets.manage_planets.v1", "Access to character list of planetary colonies and layout."),
  CORP_READ_CUSTOMS("esi-planets.read_customs_offices.v1", "Access to corporation custom office list."),
  CHAR_READ_SKILL_QUEUE("esi-skills.read_skillqueue.v1", "Access to character skill queue."),
  CHAR_READ_SKILLS("esi-skills.read_skills.v1", "Access to character skills and attributes."),
  CHAR_READ_WALLET("esi-wallet.read_character_wallet.v1", "Access to character wallet balance, journal and transactions.");

  private String name;
  private String description;

  ESIScope(String name, String description) {
    this.name = name;
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public static ESIScope[] getCharScopes() {
    return Arrays.stream(values()).filter(x -> x.name().startsWith("CHAR_")).collect(Collectors.toList()).toArray(new ESIScope[0]);
  }

  public static ESIScope[] getCorpScopes() {
    return Arrays.stream(values()).filter(x -> x.name().startsWith("CORP_")).collect(Collectors.toList()).toArray(new ESIScope[0]);
  }

}
