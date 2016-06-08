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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TypedQuery;

import com.fasterxml.jackson.annotation.JsonProperty;

import enterprises.orbital.base.OrbitalProperties;
import enterprises.orbital.db.ConnectionFactory.RunInTransaction;
import enterprises.orbital.evekit.account.EveKitUserAccountProvider;
import enterprises.orbital.evekit.account.SynchronizedEveAccount;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Object to track synchronization request for character. A synchronization tracker starts out empty and fills in each supported field type until it is marked
 * as finished. This makes it relatively straightforward to cache a tracker since slightly stale copies are usually harmless.
 */
@Entity
@Table(
    name = "evekit_char_sync")
@NamedQueries({
    @NamedQuery(
        name = "CapsuleerSyncTracker.get",
        query = "SELECT c FROM CapsuleerSyncTracker c where c.account = :account and c.syncStart = :start"),
    @NamedQuery(
        name = "CapsuleerSyncTracker.getAllUnfinished",
        query = "SELECT c FROM CapsuleerSyncTracker c where c.finished = false"),
    @NamedQuery(
        name = "CapsuleerSyncTracker.getHistory",
        query = "SELECT c FROM CapsuleerSyncTracker c where c.account = :account and c.finished = true and c.syncStart < :start order by c.syncStart desc"),
    @NamedQuery(
        name = "CapsuleerSyncTracker.getSummary",
        query = "SELECT c FROM CapsuleerSyncTracker c where c.finished = true and c.syncStart >= :start"),

})
@ApiModel(
    description = "Capsuleer synchronization tracker entry")
public class CapsuleerSyncTracker extends SyncTracker {
  private static final Logger   log = Logger.getLogger(CapsuleerSyncTracker.class.getName());

  // Status of each element we're synchronizing. If status is
  // SYNC_ERROR, then the "detail" field contains text explaining the error.
  @ApiModelProperty(
      value = "Account status status")
  @JsonProperty("accountStatusStatus")
  private SyncTracker.SyncState accountStatusStatus;
  @ApiModelProperty(
      value = "Account status detail message")
  @JsonProperty("accountStatusDetail")
  private String                accountStatusDetail;
  @ApiModelProperty(
      value = "Account balance status")
  @JsonProperty("accountBalanceStatus")
  private SyncTracker.SyncState accountBalanceStatus;
  @ApiModelProperty(
      value = "Account balance detail message")
  @JsonProperty("accountBalanceDetail")
  private String                accountBalanceDetail;
  @ApiModelProperty(
      value = "Asset list status")
  @JsonProperty("assetListStatus")
  private SyncTracker.SyncState assetListStatus;
  @ApiModelProperty(
      value = "Asset list detail message")
  @JsonProperty("assetListDetail")
  private String                assetListDetail;
  @ApiModelProperty(
      value = "Calendar event attendees status")
  @JsonProperty("calendarEventAttendeesStatus")
  private SyncTracker.SyncState calendarEventAttendeesStatus;
  @ApiModelProperty(
      value = "Calendar event attendees detail message")
  @JsonProperty("calendarEventAttendeesDetail")
  private String                calendarEventAttendeesDetail;
  @ApiModelProperty(
      value = "Character sheet status")
  @JsonProperty("characterSheetStatus")
  private SyncTracker.SyncState characterSheetStatus;
  @ApiModelProperty(
      value = "Character sheet detail message")
  @JsonProperty("characterSheetDetail")
  private String                characterSheetDetail;
  @ApiModelProperty(
      value = "Partial character sheet status (clones)")
  @JsonProperty("partialCharacterSheetStatus")
  private SyncTracker.SyncState partialCharacterSheetStatus;
  @ApiModelProperty(
      value = "Partial character sheet detail message (clones)")
  @JsonProperty("partialCharacterSheetDetail")
  private String                partialCharacterSheetDetail;
  @ApiModelProperty(
      value = "Chat channel status")
  @JsonProperty("chatChannelsStatus")
  private SyncTracker.SyncState chatChannelsStatus;
  @ApiModelProperty(
      value = "Chat channel detail message")
  @JsonProperty("chatChannelsDetail")
  private String                chatChannelsDetail;
  @ApiModelProperty(
      value = "Contact list status")
  @JsonProperty("contactListStatus")
  private SyncTracker.SyncState contactListStatus;
  @ApiModelProperty(
      value = "Contact list detail message")
  @JsonProperty("contactListDetail")
  private String                contactListDetail;
  @ApiModelProperty(
      value = "Contact notifications status")
  @JsonProperty("contactNotificationsStatus")
  private SyncTracker.SyncState contactNotificationsStatus;
  @ApiModelProperty(
      value = "Contact notifications detail message")
  @JsonProperty("contactNotificationsDetail")
  private String                contactNotificationsDetail;
  @ApiModelProperty(
      value = "Blueprints status")
  @JsonProperty("blueprintsStatus")
  private SyncTracker.SyncState blueprintsStatus;
  @ApiModelProperty(
      value = "Blueprints detail message")
  @JsonProperty("blueprintsDetail")
  private String                blueprintsDetail;
  @ApiModelProperty(
      value = "Bookmarks status")
  @JsonProperty("bookmarksStatus")
  private SyncTracker.SyncState bookmarksStatus;
  @ApiModelProperty(
      value = "Bookmarks detail message")
  @JsonProperty("bookmarksDetail")
  private String                bookmarksDetail;
  @ApiModelProperty(
      value = "Contracts status")
  @JsonProperty("contractsStatus")
  private SyncTracker.SyncState contractsStatus;
  @ApiModelProperty(
      value = "Contracts detail message")
  @JsonProperty("contractsDetail")
  private String                contractsDetail;
  @ApiModelProperty(
      value = "Contract items status")
  @JsonProperty("contractItemsStatus")
  private SyncTracker.SyncState contractItemsStatus;
  @ApiModelProperty(
      value = "Contract items detail message")
  @JsonProperty("contractItemsDetail")
  private String                contractItemsDetail;
  @ApiModelProperty(
      value = "Contract bids status")
  @JsonProperty("contractBidsStatus")
  private SyncTracker.SyncState contractBidsStatus;
  @ApiModelProperty(
      value = "Contract bids detail message")
  @JsonProperty("contractBidsDetail")
  private String                contractBidsDetail;
  @ApiModelProperty(
      value = "Faction war stats status")
  @JsonProperty("facWarStatsStatus")
  private SyncTracker.SyncState facWarStatsStatus;
  @ApiModelProperty(
      value = "Faction war stats detail message")
  @JsonProperty("facWarStatsDetail")
  private String                facWarStatsDetail;
  @ApiModelProperty(
      value = "Industry jobs status")
  @JsonProperty("industryJobsStatus")
  private SyncTracker.SyncState industryJobsStatus;
  @ApiModelProperty(
      value = "Industry jobs detail message")
  @JsonProperty("industryJobsDetail")
  private String                industryJobsDetail;
  @ApiModelProperty(
      value = "Industry jobs history status")
  @JsonProperty("industryJobsHistoryStatus")
  private SyncTracker.SyncState industryJobsHistoryStatus;
  @ApiModelProperty(
      value = "Industry jobs history detail message")
  @JsonProperty("industryJobsHistoryDetail")
  private String                industryJobsHistoryDetail;
  @ApiModelProperty(
      value = "Kill log status")
  @JsonProperty("killlogStatus")
  private SyncTracker.SyncState killlogStatus;
  @ApiModelProperty(
      value = "Kill log detail message")
  @JsonProperty("killlogDetail")
  private String                killlogDetail;
  @ApiModelProperty(
      value = "Mail bodies status")
  @JsonProperty("mailBodiesStatus")
  private SyncTracker.SyncState mailBodiesStatus;
  @ApiModelProperty(
      value = "Mail bodies detail message")
  @JsonProperty("mailBodiesDetail")
  private String                mailBodiesDetail;
  @ApiModelProperty(
      value = "Mailing lists status")
  @JsonProperty("mailingListsStatus")
  private SyncTracker.SyncState mailingListsStatus;
  @ApiModelProperty(
      value = "Mailing lists detail message")
  @JsonProperty("mailingListsDetail")
  private String                mailingListsDetail;
  @ApiModelProperty(
      value = "Mail messages status")
  @JsonProperty("mailMessagesStatus")
  private SyncTracker.SyncState mailMessagesStatus;
  @ApiModelProperty(
      value = "Mail messages detail message")
  @JsonProperty("mailMessagesDetail")
  private String                mailMessagesDetail;
  @ApiModelProperty(
      value = "Market orders status")
  @JsonProperty("marketOrdersStatus")
  private SyncTracker.SyncState marketOrdersStatus;
  @ApiModelProperty(
      value = "Market orders detail message")
  @JsonProperty("marketOrdersDetail")
  private String                marketOrdersDetail;
  @ApiModelProperty(
      value = "Medals status")
  @JsonProperty("medalsStatus")
  private SyncTracker.SyncState medalsStatus;
  @ApiModelProperty(
      value = "Medals detail message")
  @JsonProperty("medalsDetail")
  private String                medalsDetail;
  @ApiModelProperty(
      value = "Notifications status")
  @JsonProperty("notificationsStatus")
  private SyncTracker.SyncState notificationsStatus;
  @ApiModelProperty(
      value = "Notifications detail message")
  @JsonProperty("notificationsDetail")
  private String                notificationsDetail;
  @ApiModelProperty(
      value = "Notification texts status")
  @JsonProperty("notificationTextsStatus")
  private SyncTracker.SyncState notificationTextsStatus;
  @ApiModelProperty(
      value = "Notification texts detail message")
  @JsonProperty("notificationTextsDetail")
  private String                notificationTextsDetail;
  @ApiModelProperty(
      value = "Planetary colonies status")
  @JsonProperty("planetaryColoniesStatus")
  private SyncTracker.SyncState planetaryColoniesStatus;
  @ApiModelProperty(
      value = "Planetary colonies detail message")
  @JsonProperty("planetaryColoniesDetail")
  private String                planetaryColoniesDetail;
  @ApiModelProperty(
      value = "Research status")
  @JsonProperty("researchStatus")
  private SyncTracker.SyncState researchStatus;
  @ApiModelProperty(
      value = "Research detail message")
  @JsonProperty("researchDetail")
  private String                researchDetail;
  @ApiModelProperty(
      value = "Skill in training status")
  @JsonProperty("skillInTrainingStatus")
  private SyncTracker.SyncState skillInTrainingStatus;
  @ApiModelProperty(
      value = "Skill in training detail message")
  @JsonProperty("skillInTrainingDetail")
  private String                skillInTrainingDetail;
  @ApiModelProperty(
      value = "Skill queue status")
  @JsonProperty("skillQueueStatus")
  private SyncTracker.SyncState skillQueueStatus;
  @ApiModelProperty(
      value = "Skill queue detail message")
  @JsonProperty("skillQueueDetail")
  private String                skillQueueDetail;
  @ApiModelProperty(
      value = "Skills status")
  @JsonProperty("skillsStatus")
  private SyncTracker.SyncState skillsStatus;
  @ApiModelProperty(
      value = "Skills detail message")
  @JsonProperty("skillsDetail")
  private String                skillsDetail;
  @ApiModelProperty(
      value = "Standings status")
  @JsonProperty("standingsStatus")
  private SyncTracker.SyncState standingsStatus;
  @ApiModelProperty(
      value = "Standings detail message")
  @JsonProperty("standingsDetail")
  private String                standingsDetail;
  @ApiModelProperty(
      value = "Upcoming calendar events status")
  @JsonProperty("upcomingCalendarEventsStatus")
  private SyncTracker.SyncState upcomingCalendarEventsStatus;
  @ApiModelProperty(
      value = "Upcoming calendar events detail message")
  @JsonProperty("upcomingCalendarEventsDetail")
  private String                upcomingCalendarEventsDetail;
  @ApiModelProperty(
      value = "Wallet journal status")
  @JsonProperty("walletJournalStatus")
  private SyncTracker.SyncState walletJournalStatus;
  @ApiModelProperty(
      value = "Wallet journal detail message")
  @JsonProperty("walletJournalDetail")
  private String                walletJournalDetail;
  @ApiModelProperty(
      value = "Wallet transaction status")
  @JsonProperty("walletTransactionsStatus")
  private SyncTracker.SyncState walletTransactionsStatus;
  @ApiModelProperty(
      value = "Wallet transaction detail message")
  @JsonProperty("walletTransactionsDetail")
  private String                walletTransactionsDetail;

  public CapsuleerSyncTracker() {
    accountStatusStatus = SyncTracker.SyncState.NOT_PROCESSED;
    accountBalanceStatus = SyncTracker.SyncState.NOT_PROCESSED;
    assetListStatus = SyncTracker.SyncState.NOT_PROCESSED;
    calendarEventAttendeesStatus = SyncTracker.SyncState.NOT_PROCESSED;
    characterSheetStatus = SyncTracker.SyncState.NOT_PROCESSED;
    partialCharacterSheetStatus = SyncTracker.SyncState.NOT_PROCESSED;
    chatChannelsStatus = SyncTracker.SyncState.NOT_PROCESSED;
    contactListStatus = SyncTracker.SyncState.NOT_PROCESSED;
    contactNotificationsStatus = SyncTracker.SyncState.NOT_PROCESSED;
    blueprintsStatus = SyncTracker.SyncState.NOT_PROCESSED;
    bookmarksStatus = SyncTracker.SyncState.NOT_PROCESSED;
    contractsStatus = SyncTracker.SyncState.NOT_PROCESSED;
    contractItemsStatus = SyncTracker.SyncState.NOT_PROCESSED;
    contractBidsStatus = SyncTracker.SyncState.NOT_PROCESSED;
    facWarStatsStatus = SyncTracker.SyncState.NOT_PROCESSED;
    industryJobsStatus = SyncTracker.SyncState.NOT_PROCESSED;
    industryJobsHistoryStatus = SyncTracker.SyncState.NOT_PROCESSED;
    killlogStatus = SyncTracker.SyncState.NOT_PROCESSED;
    mailBodiesStatus = SyncTracker.SyncState.NOT_PROCESSED;
    mailingListsStatus = SyncTracker.SyncState.NOT_PROCESSED;
    mailMessagesStatus = SyncTracker.SyncState.NOT_PROCESSED;
    marketOrdersStatus = SyncTracker.SyncState.NOT_PROCESSED;
    medalsStatus = SyncTracker.SyncState.NOT_PROCESSED;
    notificationsStatus = SyncTracker.SyncState.NOT_PROCESSED;
    notificationTextsStatus = SyncTracker.SyncState.NOT_PROCESSED;
    planetaryColoniesStatus = SyncTracker.SyncState.NOT_PROCESSED;
    researchStatus = SyncTracker.SyncState.NOT_PROCESSED;
    skillInTrainingStatus = SyncTracker.SyncState.NOT_PROCESSED;
    skillQueueStatus = SyncTracker.SyncState.NOT_PROCESSED;
    skillsStatus = SyncTracker.SyncState.NOT_PROCESSED;
    standingsStatus = SyncTracker.SyncState.NOT_PROCESSED;
    upcomingCalendarEventsStatus = SyncTracker.SyncState.NOT_PROCESSED;
    walletJournalStatus = SyncTracker.SyncState.NOT_PROCESSED;
    walletTransactionsStatus = SyncTracker.SyncState.NOT_PROCESSED;
  }

  @Override
  public void setState(
                       SynchronizationState state,
                       SyncTracker.SyncState status,
                       String msg) {
    switch (state) {
    case SYNC_CHAR_ACCOUNTSTATUS:
      setAccountStatusStatus(status);
      setAccountStatusDetail(msg);
      break;

    case SYNC_CHAR_ACCOUNTBALANCE:
      setAccountBalanceStatus(status);
      setAccountBalanceDetail(msg);
      break;

    case SYNC_CHAR_ASSETLIST:
      setAssetListStatus(status);
      setAssetListDetail(msg);
      break;

    case SYNC_CHAR_CALENDAREVENTATTENDEES:
      setCalendarEventAttendeesStatus(status);
      setCalendarEventAttendeesDetail(msg);
      break;

    case SYNC_CHAR_CHARACTERSHEET:
      setCharacterSheetStatus(status);
      setCharacterSheetDetail(msg);
      break;

    case SYNC_CHAR_PARTIALCHARACTERSHEET:
      setPartialCharacterSheetStatus(status);
      setPartialCharacterSheetDetail(msg);
      break;

    case SYNC_CHAR_CHATCHANNELS:
      setChatChannelsStatus(status);
      setChatChannelsDetail(msg);
      break;

    case SYNC_CHAR_CONTACTLIST:
      setContactListStatus(status);
      setContactListDetail(msg);
      break;

    case SYNC_CHAR_CONTACTNOTIFICATIONS:
      setContactNotificationsStatus(status);
      setContactNotificationsDetail(msg);
      break;

    case SYNC_CHAR_BLUEPRINTS:
      setBlueprintsStatus(status);
      setBlueprintsDetail(msg);
      break;

    case SYNC_CHAR_BOOKMARKS:
      setBookmarksStatus(status);
      setBookmarksDetail(msg);
      break;

    case SYNC_CHAR_CONTRACTS:
      setContractsStatus(status);
      setContractsDetail(msg);
      break;

    case SYNC_CHAR_CONTRACTITEMS:
      setContractItemsStatus(status);
      setContractItemsDetail(msg);
      break;

    case SYNC_CHAR_CONTRACTBIDS:
      setContractBidsStatus(status);
      setContractBidsDetail(msg);
      break;

    case SYNC_CHAR_FACWARSTATS:
      setFacWarStatsStatus(status);
      setFacWarStatsDetail(msg);
      break;

    case SYNC_CHAR_INDUSTRYJOBS:
      setIndustryJobsStatus(status);
      setIndustryJobsDetail(msg);
      break;

    case SYNC_CHAR_INDUSTRYJOBSHISTORY:
      setIndustryJobsHistoryStatus(status);
      setIndustryJobsHistoryDetail(msg);
      break;

    case SYNC_CHAR_KILLLOG:
      setKilllogStatus(status);
      setKilllogDetail(msg);
      break;

    case SYNC_CHAR_MAILBODIES:
      setMailBodiesStatus(status);
      setMailBodiesDetail(msg);
      break;

    case SYNC_CHAR_MAILINGLISTS:
      setMailingListsStatus(status);
      setMailingListsDetail(msg);
      break;

    case SYNC_CHAR_MAILMESSAGES:
      setMailMessagesStatus(status);
      setMailMessagesDetail(msg);
      break;

    case SYNC_CHAR_MARKETORDERS:
      setMarketOrdersStatus(status);
      setMarketOrdersDetail(msg);
      break;

    case SYNC_CHAR_MEDALS:
      setMedalsStatus(status);
      setMedalsDetail(msg);
      break;

    case SYNC_CHAR_NOTIFICATIONS:
      setNotificationsStatus(status);
      setNotificationsDetail(msg);
      break;

    case SYNC_CHAR_NOTIFICATIONTEXTS:
      setNotificationTextsStatus(status);
      setNotificationTextsDetail(msg);
      break;

    case SYNC_CHAR_PLANETARY_COLONIES:
      setPlanetaryColoniesStatus(status);
      setPlanetaryColoniesDetail(msg);
      break;

    case SYNC_CHAR_RESEARCH:
      setResearchStatus(status);
      setResearchDetail(msg);
      break;

    case SYNC_CHAR_SKILLINTRAINING:
      setSkillInTrainingStatus(status);
      setSkillInTrainingDetail(msg);
      break;

    case SYNC_CHAR_SKILLQUEUE:
      setSkillQueueStatus(status);
      setSkillQueueDetail(msg);
      break;

    case SYNC_CHAR_SKILLS:
      setSkillsStatus(status);
      setSkillsDetail(msg);
      break;

    case SYNC_CHAR_STANDINGS:
      setStandingsStatus(status);
      setStandingsDetail(msg);
      break;

    case SYNC_CHAR_UPCOMINGCALENDAREVENTS:
      setUpcomingCalendarEventsStatus(status);
      setUpcomingCalendarEventsDetail(msg);
      break;

    case SYNC_CHAR_WALLETJOURNAL:
      setWalletJournalStatus(status);
      setWalletJournalDetail(msg);
      break;

    case SYNC_CHAR_WALLETTRANSACTIONS:
      setWalletTransactionsStatus(status);
      setWalletTransactionsDetail(msg);
      break;

    default:
      // NOP
      ;
    }
  }

  @Override
  public SynchronizationState trackerComplete(
                                              Set<SynchronizationState> checkState) {
    for (SynchronizationState next : checkState) {
      switch (next) {
      case SYNC_CHAR_ACCOUNTSTATUS:
        if (accountStatusStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CHAR_ACCOUNTBALANCE:
        if (accountBalanceStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CHAR_ASSETLIST:
        if (assetListStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CHAR_CALENDAREVENTATTENDEES:
        // This can only be next if UpcomingCalendarEvents and CharacterSheet have been done first.
        if (upcomingCalendarEventsStatus != SyncTracker.SyncState.NOT_PROCESSED && characterSheetStatus != SyncTracker.SyncState.NOT_PROCESSED
            && calendarEventAttendeesStatus == SyncTracker.SyncState.NOT_PROCESSED)
          return next;
        break;

      case SYNC_CHAR_CHARACTERSHEET:
        if (characterSheetStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CHAR_PARTIALCHARACTERSHEET:
        if (partialCharacterSheetStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CHAR_CHATCHANNELS:
        if (chatChannelsStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CHAR_CONTACTLIST:
        if (contactListStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CHAR_CONTACTNOTIFICATIONS:
        if (contactNotificationsStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CHAR_BLUEPRINTS:
        if (blueprintsStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CHAR_BOOKMARKS:
        if (bookmarksStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CHAR_CONTRACTS:
        if (contractsStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CHAR_CONTRACTITEMS:
        // Contract items can only be next if we've completed contracts
        if (contractsStatus != SyncTracker.SyncState.NOT_PROCESSED && contractItemsStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CHAR_CONTRACTBIDS:
        if (contractBidsStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CHAR_FACWARSTATS:
        if (facWarStatsStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CHAR_INDUSTRYJOBS:
        if (industryJobsStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CHAR_INDUSTRYJOBSHISTORY:
        if (industryJobsHistoryStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CHAR_KILLLOG:
        if (killlogStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CHAR_MAILBODIES:
        // Mail bodies can only be next if we've completed mail messages
        if (mailMessagesStatus != SyncTracker.SyncState.NOT_PROCESSED && mailBodiesStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CHAR_MAILINGLISTS:
        if (mailingListsStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CHAR_MAILMESSAGES:
        if (mailMessagesStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CHAR_MARKETORDERS:
        if (marketOrdersStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CHAR_MEDALS:
        if (medalsStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CHAR_NOTIFICATIONS:
        if (notificationsStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CHAR_NOTIFICATIONTEXTS:
        // Notification texts can only be next if we processed notifications
        if (notificationsStatus != SyncTracker.SyncState.NOT_PROCESSED && notificationTextsStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CHAR_PLANETARY_COLONIES:
        if (planetaryColoniesStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CHAR_RESEARCH:
        if (researchStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CHAR_SKILLINTRAINING:
        if (skillInTrainingStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CHAR_SKILLQUEUE:
        if (skillQueueStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CHAR_SKILLS:
        if (skillsStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CHAR_STANDINGS:
        if (standingsStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CHAR_UPCOMINGCALENDAREVENTS:
        if (upcomingCalendarEventsStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CHAR_WALLETJOURNAL:
        if (walletJournalStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CHAR_WALLETTRANSACTIONS:
        if (walletTransactionsStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CHAR_START:
      case SYNC_CHAR_END:
      default:
        // NOP
        ;
      }
    }

    return null;
  }

  public SyncTracker.SyncState getAccountStatusStatus() {
    return accountStatusStatus;
  }

  public void setAccountStatusStatus(
                                     SyncTracker.SyncState accountStatus) {
    this.accountStatusStatus = accountStatus;
  }

  public String getAccountStatusDetail() {
    return accountStatusDetail;
  }

  public void setAccountStatusDetail(
                                     String accountDetail) {
    this.accountStatusDetail = accountDetail;
  }

  public SyncTracker.SyncState getAccountBalanceStatus() {
    return accountBalanceStatus;
  }

  public void setAccountBalanceStatus(
                                      SyncTracker.SyncState accountBalanceStatus) {
    this.accountBalanceStatus = accountBalanceStatus;
  }

  public String getAccountBalanceDetail() {
    return accountBalanceDetail;
  }

  public void setAccountBalanceDetail(
                                      String accountBalanceDetail) {
    this.accountBalanceDetail = accountBalanceDetail;
  }

  public SyncTracker.SyncState getAssetListStatus() {
    return assetListStatus;
  }

  public void setAssetListStatus(
                                 SyncTracker.SyncState assetListStatus) {
    this.assetListStatus = assetListStatus;
  }

  public String getAssetListDetail() {
    return assetListDetail;
  }

  public void setAssetListDetail(
                                 String assetListDetail) {
    this.assetListDetail = assetListDetail;
  }

  public SyncTracker.SyncState getCalendarEventAttendeesStatus() {
    return calendarEventAttendeesStatus;
  }

  public void setCalendarEventAttendeesStatus(
                                              SyncTracker.SyncState calendarEventAttendeesStatus) {
    this.calendarEventAttendeesStatus = calendarEventAttendeesStatus;
  }

  public String getCalendarEventAttendeesDetail() {
    return calendarEventAttendeesDetail;
  }

  public void setCalendarEventAttendeesDetail(
                                              String calendarEventAttendeesDetail) {
    this.calendarEventAttendeesDetail = calendarEventAttendeesDetail;
  }

  public SyncTracker.SyncState getCharacterSheetStatus() {
    return characterSheetStatus;
  }

  public void setCharacterSheetStatus(
                                      SyncTracker.SyncState characterSheetStatus) {
    this.characterSheetStatus = characterSheetStatus;
  }

  public String getCharacterSheetDetail() {
    return characterSheetDetail;
  }

  public void setCharacterSheetDetail(
                                      String characterSheetDetail) {
    this.characterSheetDetail = characterSheetDetail;
  }

  public SyncTracker.SyncState getPartialCharacterSheetStatus() {
    return partialCharacterSheetStatus;
  }

  public void setPartialCharacterSheetStatus(
                                             SyncTracker.SyncState partialCharacterSheetStatus) {
    this.partialCharacterSheetStatus = partialCharacterSheetStatus;
  }

  public String getPartialCharacterSheetDetail() {
    return partialCharacterSheetDetail;
  }

  public void setPartialCharacterSheetDetail(
                                             String partialCharacterSheetDetail) {
    this.partialCharacterSheetDetail = partialCharacterSheetDetail;
  }

  public SyncTracker.SyncState getChatChannelsStatus() {
    return chatChannelsStatus;
  }

  public void setChatChannelsStatus(
                                    SyncTracker.SyncState chatChannelsStatus) {
    this.chatChannelsStatus = chatChannelsStatus;
  }

  public String getChatChannelsDetail() {
    return chatChannelsDetail;
  }

  public void setChatChannelsDetail(
                                    String chatChannelsDetail) {
    this.chatChannelsDetail = chatChannelsDetail;
  }

  public SyncTracker.SyncState getContactListStatus() {
    return contactListStatus;
  }

  public void setContactListStatus(
                                   SyncTracker.SyncState contactListStatus) {
    this.contactListStatus = contactListStatus;
  }

  public String getContactListDetail() {
    return contactListDetail;
  }

  public void setContactListDetail(
                                   String contactListDetail) {
    this.contactListDetail = contactListDetail;
  }

  public SyncTracker.SyncState getContactNotificationsStatus() {
    return contactNotificationsStatus;
  }

  public void setContactNotificationsStatus(
                                            SyncTracker.SyncState contactNotificationsStatus) {
    this.contactNotificationsStatus = contactNotificationsStatus;
  }

  public String getContactNotificationsDetail() {
    return contactNotificationsDetail;
  }

  public void setContactNotificationsDetail(
                                            String contactNotificationsDetail) {
    this.contactNotificationsDetail = contactNotificationsDetail;
  }

  public String getBlueprintsDetail() {
    return blueprintsDetail;
  }

  public void setBlueprintsDetail(
                                  String blueprintsDetail) {
    this.blueprintsDetail = blueprintsDetail;
  }

  public String getBookmarksDetail() {
    return bookmarksDetail;
  }

  public void setBookmarksDetail(
                                 String bookmarksDetail) {
    this.bookmarksDetail = bookmarksDetail;
  }

  public String getContractsDetail() {
    return contractsDetail;
  }

  public void setContractsDetail(
                                 String contractDetail) {
    this.contractsDetail = contractDetail;
  }

  public String getContractItemsDetail() {
    return contractItemsDetail;
  }

  public void setContractItemsDetail(
                                     String contractItemDetail) {
    this.contractItemsDetail = contractItemDetail;
  }

  public String getContractBidsDetail() {
    return contractBidsDetail;
  }

  public void setContractBidsDetail(
                                    String contractBidsDetail) {
    this.contractBidsDetail = contractBidsDetail;
  }

  public SyncTracker.SyncState getBlueprintsStatus() {
    return blueprintsStatus;
  }

  public void setBlueprintsStatus(
                                  SyncTracker.SyncState status) {
    this.blueprintsStatus = status;
  }

  public SyncTracker.SyncState getBookmarksStatus() {
    return bookmarksStatus;
  }

  public void setBookmarksStatus(
                                 SyncTracker.SyncState status) {
    this.bookmarksStatus = status;
  }

  public SyncTracker.SyncState getContractsStatus() {
    return contractsStatus;
  }

  public void setContractsStatus(
                                 SyncTracker.SyncState status) {
    this.contractsStatus = status;
  }

  public SyncTracker.SyncState getContractItemsStatus() {
    return contractItemsStatus;
  }

  public void setContractItemsStatus(
                                     SyncTracker.SyncState status) {
    this.contractItemsStatus = status;
  }

  public SyncTracker.SyncState getContractBidsStatus() {
    return contractBidsStatus;
  }

  public void setContractBidsStatus(
                                    SyncTracker.SyncState status) {
    this.contractBidsStatus = status;
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

  public SyncTracker.SyncState getIndustryJobsStatus() {
    return industryJobsStatus;
  }

  public void setIndustryJobsStatus(
                                    SyncTracker.SyncState industryJobsStatus) {
    this.industryJobsStatus = industryJobsStatus;
  }

  public String getIndustryJobsDetail() {
    return industryJobsDetail;
  }

  public void setIndustryJobsDetail(
                                    String industryJobsDetail) {
    this.industryJobsDetail = industryJobsDetail;
  }

  public SyncTracker.SyncState getIndustryJobsHistoryStatus() {
    return industryJobsHistoryStatus;
  }

  public void setIndustryJobsHistoryStatus(
                                           SyncTracker.SyncState industryJobsHistoryStatus) {
    this.industryJobsHistoryStatus = industryJobsHistoryStatus;
  }

  public String getIndustryJobsHistoryDetail() {
    return industryJobsHistoryDetail;
  }

  public void setIndustryJobsHistoryDetail(
                                           String industryJobsHistoryDetail) {
    this.industryJobsHistoryDetail = industryJobsHistoryDetail;
  }

  public SyncTracker.SyncState getKilllogStatus() {
    return killlogStatus;
  }

  public void setKilllogStatus(
                               SyncTracker.SyncState killlogStatus) {
    this.killlogStatus = killlogStatus;
  }

  public String getKilllogDetail() {
    return killlogDetail;
  }

  public void setKilllogDetail(
                               String killlogDetail) {
    this.killlogDetail = killlogDetail;
  }

  public SyncTracker.SyncState getMailBodiesStatus() {
    return mailBodiesStatus;
  }

  public void setMailBodiesStatus(
                                  SyncTracker.SyncState mailBodiesStatus) {
    this.mailBodiesStatus = mailBodiesStatus;
  }

  public String getMailBodiesDetail() {
    return mailBodiesDetail;
  }

  public void setMailBodiesDetail(
                                  String mailBodiesDetail) {
    this.mailBodiesDetail = mailBodiesDetail;
  }

  public SyncTracker.SyncState getMailingListsStatus() {
    return mailingListsStatus;
  }

  public void setMailingListsStatus(
                                    SyncTracker.SyncState mailingListsStatus) {
    this.mailingListsStatus = mailingListsStatus;
  }

  public String getMailingListsDetail() {
    return mailingListsDetail;
  }

  public void setMailingListsDetail(
                                    String mailingListsDetail) {
    this.mailingListsDetail = mailingListsDetail;
  }

  public SyncTracker.SyncState getMailMessagesStatus() {
    return mailMessagesStatus;
  }

  public void setMailMessagesStatus(
                                    SyncTracker.SyncState mailMessagesStatus) {
    this.mailMessagesStatus = mailMessagesStatus;
  }

  public String getMailMessagesDetail() {
    return mailMessagesDetail;
  }

  public void setMailMessagesDetail(
                                    String mailMessagesDetail) {
    this.mailMessagesDetail = mailMessagesDetail;
  }

  public SyncTracker.SyncState getMarketOrdersStatus() {
    return marketOrdersStatus;
  }

  public void setMarketOrdersStatus(
                                    SyncTracker.SyncState marketOrdersStatus) {
    this.marketOrdersStatus = marketOrdersStatus;
  }

  public String getMarketOrdersDetail() {
    return marketOrdersDetail;
  }

  public void setMarketOrdersDetail(
                                    String marketOrdersDetail) {
    this.marketOrdersDetail = marketOrdersDetail;
  }

  public SyncTracker.SyncState getMedalsStatus() {
    return medalsStatus;
  }

  public void setMedalsStatus(
                              SyncTracker.SyncState medalsStatus) {
    this.medalsStatus = medalsStatus;
  }

  public String getMedalsDetail() {
    return medalsDetail;
  }

  public void setMedalsDetail(
                              String medalsDetail) {
    this.medalsDetail = medalsDetail;
  }

  public SyncTracker.SyncState getNotificationsStatus() {
    return notificationsStatus;
  }

  public void setNotificationsStatus(
                                     SyncTracker.SyncState notificationsStatus) {
    this.notificationsStatus = notificationsStatus;
  }

  public String getNotificationsDetail() {
    return notificationsDetail;
  }

  public void setNotificationsDetail(
                                     String notificationsDetail) {
    this.notificationsDetail = notificationsDetail;
  }

  public SyncTracker.SyncState getNotificationTextsStatus() {
    return notificationTextsStatus;
  }

  public void setNotificationTextsStatus(
                                         SyncTracker.SyncState notificationTextsStatus) {
    this.notificationTextsStatus = notificationTextsStatus;
  }

  public String getNotificationTextsDetail() {
    return notificationTextsDetail;
  }

  public void setNotificationTextsDetail(
                                         String notificationTextsDetail) {
    this.notificationTextsDetail = notificationTextsDetail;
  }

  public SyncTracker.SyncState getPlanetaryColoniesStatus() {
    return planetaryColoniesStatus;
  }

  public void setPlanetaryColoniesStatus(
                                         SyncTracker.SyncState planetaryColoniesStatus) {
    this.planetaryColoniesStatus = planetaryColoniesStatus;
  }

  public String getPlanetaryColoniesDetail() {
    return planetaryColoniesDetail;
  }

  public void setPlanetaryColoniesDetail(
                                         String planetaryColoniesDetail) {
    this.planetaryColoniesDetail = planetaryColoniesDetail;
  }

  public SyncTracker.SyncState getResearchStatus() {
    return researchStatus;
  }

  public void setResearchStatus(
                                SyncTracker.SyncState researchStatus) {
    this.researchStatus = researchStatus;
  }

  public String getResearchDetail() {
    return researchDetail;
  }

  public void setResearchDetail(
                                String researchDetail) {
    this.researchDetail = researchDetail;
  }

  public SyncTracker.SyncState getSkillInTrainingStatus() {
    return skillInTrainingStatus;
  }

  public void setSkillInTrainingStatus(
                                       SyncTracker.SyncState skillInTrainingStatus) {
    this.skillInTrainingStatus = skillInTrainingStatus;
  }

  public String getSkillInTrainingDetail() {
    return skillInTrainingDetail;
  }

  public void setSkillInTrainingDetail(
                                       String skillInTrainingDetail) {
    this.skillInTrainingDetail = skillInTrainingDetail;
  }

  public SyncTracker.SyncState getSkillQueueStatus() {
    return skillQueueStatus;
  }

  public void setSkillQueueStatus(
                                  SyncTracker.SyncState skillQueueStatus) {
    this.skillQueueStatus = skillQueueStatus;
  }

  public String getSkillQueueDetail() {
    return skillQueueDetail;
  }

  public void setSkillQueueDetail(
                                  String skillQueueDetail) {
    this.skillQueueDetail = skillQueueDetail;
  }

  public SyncTracker.SyncState getSkillsStatus() {
    return skillsStatus;
  }

  public void setSkillsStatus(
                              SyncTracker.SyncState skillsStatus) {
    this.skillsStatus = skillsStatus;
  }

  public String getSkillsDetail() {
    return skillsDetail;
  }

  public void setSkillsDetail(
                              String skillsDetail) {
    this.skillsDetail = skillsDetail;
  }

  public SyncTracker.SyncState getStandingsStatus() {
    return standingsStatus;
  }

  public void setStandingsStatus(
                                 SyncTracker.SyncState standingsStatus) {
    this.standingsStatus = standingsStatus;
  }

  public String getStandingsDetail() {
    return standingsDetail;
  }

  public void setStandingsDetail(
                                 String standingsDetail) {
    this.standingsDetail = standingsDetail;
  }

  public SyncTracker.SyncState getUpcomingCalendarEventsStatus() {
    return upcomingCalendarEventsStatus;
  }

  public void setUpcomingCalendarEventsStatus(
                                              SyncTracker.SyncState upcomingCalendarEventsStatus) {
    this.upcomingCalendarEventsStatus = upcomingCalendarEventsStatus;
  }

  public String getUpcomingCalendarEventsDetail() {
    return upcomingCalendarEventsDetail;
  }

  public void setUpcomingCalendarEventsDetail(
                                              String upcomingCalendarEventsDetail) {
    this.upcomingCalendarEventsDetail = upcomingCalendarEventsDetail;
  }

  public SyncTracker.SyncState getWalletJournalStatus() {
    return walletJournalStatus;
  }

  public void setWalletJournalStatus(
                                     SyncTracker.SyncState walletJournalStatus) {
    this.walletJournalStatus = walletJournalStatus;
  }

  public String getWalletJournalDetail() {
    return walletJournalDetail;
  }

  public void setWalletJournalDetail(
                                     String walletJournalDetail) {
    this.walletJournalDetail = walletJournalDetail;
  }

  public SyncTracker.SyncState getWalletTransactionsStatus() {
    return walletTransactionsStatus;
  }

  public void setWalletTransactionsStatus(
                                          SyncTracker.SyncState walletTransactionsStatus) {
    this.walletTransactionsStatus = walletTransactionsStatus;
  }

  public String getWalletTransactionsDetail() {
    return walletTransactionsDetail;
  }

  public void setWalletTransactionsDetail(
                                          String walletTransactionsDetail) {
    this.walletTransactionsDetail = walletTransactionsDetail;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((accountBalanceDetail == null) ? 0 : accountBalanceDetail.hashCode());
    result = prime * result + ((accountBalanceStatus == null) ? 0 : accountBalanceStatus.hashCode());
    result = prime * result + ((accountStatusDetail == null) ? 0 : accountStatusDetail.hashCode());
    result = prime * result + ((accountStatusStatus == null) ? 0 : accountStatusStatus.hashCode());
    result = prime * result + ((assetListDetail == null) ? 0 : assetListDetail.hashCode());
    result = prime * result + ((assetListStatus == null) ? 0 : assetListStatus.hashCode());
    result = prime * result + ((blueprintsDetail == null) ? 0 : blueprintsDetail.hashCode());
    result = prime * result + ((blueprintsStatus == null) ? 0 : blueprintsStatus.hashCode());
    result = prime * result + ((bookmarksDetail == null) ? 0 : bookmarksDetail.hashCode());
    result = prime * result + ((bookmarksStatus == null) ? 0 : bookmarksStatus.hashCode());
    result = prime * result + ((calendarEventAttendeesDetail == null) ? 0 : calendarEventAttendeesDetail.hashCode());
    result = prime * result + ((calendarEventAttendeesStatus == null) ? 0 : calendarEventAttendeesStatus.hashCode());
    result = prime * result + ((characterSheetDetail == null) ? 0 : characterSheetDetail.hashCode());
    result = prime * result + ((characterSheetStatus == null) ? 0 : characterSheetStatus.hashCode());
    result = prime * result + ((chatChannelsDetail == null) ? 0 : chatChannelsDetail.hashCode());
    result = prime * result + ((chatChannelsStatus == null) ? 0 : chatChannelsStatus.hashCode());
    result = prime * result + ((contactListDetail == null) ? 0 : contactListDetail.hashCode());
    result = prime * result + ((contactListStatus == null) ? 0 : contactListStatus.hashCode());
    result = prime * result + ((contactNotificationsDetail == null) ? 0 : contactNotificationsDetail.hashCode());
    result = prime * result + ((contactNotificationsStatus == null) ? 0 : contactNotificationsStatus.hashCode());
    result = prime * result + ((contractBidsDetail == null) ? 0 : contractBidsDetail.hashCode());
    result = prime * result + ((contractBidsStatus == null) ? 0 : contractBidsStatus.hashCode());
    result = prime * result + ((contractItemsDetail == null) ? 0 : contractItemsDetail.hashCode());
    result = prime * result + ((contractItemsStatus == null) ? 0 : contractItemsStatus.hashCode());
    result = prime * result + ((contractsDetail == null) ? 0 : contractsDetail.hashCode());
    result = prime * result + ((contractsStatus == null) ? 0 : contractsStatus.hashCode());
    result = prime * result + ((facWarStatsDetail == null) ? 0 : facWarStatsDetail.hashCode());
    result = prime * result + ((facWarStatsStatus == null) ? 0 : facWarStatsStatus.hashCode());
    result = prime * result + ((industryJobsDetail == null) ? 0 : industryJobsDetail.hashCode());
    result = prime * result + ((industryJobsHistoryDetail == null) ? 0 : industryJobsHistoryDetail.hashCode());
    result = prime * result + ((industryJobsHistoryStatus == null) ? 0 : industryJobsHistoryStatus.hashCode());
    result = prime * result + ((industryJobsStatus == null) ? 0 : industryJobsStatus.hashCode());
    result = prime * result + ((killlogDetail == null) ? 0 : killlogDetail.hashCode());
    result = prime * result + ((killlogStatus == null) ? 0 : killlogStatus.hashCode());
    result = prime * result + ((mailBodiesDetail == null) ? 0 : mailBodiesDetail.hashCode());
    result = prime * result + ((mailBodiesStatus == null) ? 0 : mailBodiesStatus.hashCode());
    result = prime * result + ((mailMessagesDetail == null) ? 0 : mailMessagesDetail.hashCode());
    result = prime * result + ((mailMessagesStatus == null) ? 0 : mailMessagesStatus.hashCode());
    result = prime * result + ((mailingListsDetail == null) ? 0 : mailingListsDetail.hashCode());
    result = prime * result + ((mailingListsStatus == null) ? 0 : mailingListsStatus.hashCode());
    result = prime * result + ((marketOrdersDetail == null) ? 0 : marketOrdersDetail.hashCode());
    result = prime * result + ((marketOrdersStatus == null) ? 0 : marketOrdersStatus.hashCode());
    result = prime * result + ((medalsDetail == null) ? 0 : medalsDetail.hashCode());
    result = prime * result + ((medalsStatus == null) ? 0 : medalsStatus.hashCode());
    result = prime * result + ((notificationTextsDetail == null) ? 0 : notificationTextsDetail.hashCode());
    result = prime * result + ((notificationTextsStatus == null) ? 0 : notificationTextsStatus.hashCode());
    result = prime * result + ((notificationsDetail == null) ? 0 : notificationsDetail.hashCode());
    result = prime * result + ((notificationsStatus == null) ? 0 : notificationsStatus.hashCode());
    result = prime * result + ((planetaryColoniesDetail == null) ? 0 : planetaryColoniesDetail.hashCode());
    result = prime * result + ((planetaryColoniesStatus == null) ? 0 : planetaryColoniesStatus.hashCode());
    result = prime * result + ((researchDetail == null) ? 0 : researchDetail.hashCode());
    result = prime * result + ((researchStatus == null) ? 0 : researchStatus.hashCode());
    result = prime * result + ((skillInTrainingDetail == null) ? 0 : skillInTrainingDetail.hashCode());
    result = prime * result + ((skillInTrainingStatus == null) ? 0 : skillInTrainingStatus.hashCode());
    result = prime * result + ((skillQueueDetail == null) ? 0 : skillQueueDetail.hashCode());
    result = prime * result + ((skillQueueStatus == null) ? 0 : skillQueueStatus.hashCode());
    result = prime * result + ((standingsDetail == null) ? 0 : standingsDetail.hashCode());
    result = prime * result + ((standingsStatus == null) ? 0 : standingsStatus.hashCode());
    result = prime * result + ((upcomingCalendarEventsDetail == null) ? 0 : upcomingCalendarEventsDetail.hashCode());
    result = prime * result + ((upcomingCalendarEventsStatus == null) ? 0 : upcomingCalendarEventsStatus.hashCode());
    result = prime * result + ((walletJournalDetail == null) ? 0 : walletJournalDetail.hashCode());
    result = prime * result + ((walletJournalStatus == null) ? 0 : walletJournalStatus.hashCode());
    result = prime * result + ((walletTransactionsDetail == null) ? 0 : walletTransactionsDetail.hashCode());
    result = prime * result + ((walletTransactionsStatus == null) ? 0 : walletTransactionsStatus.hashCode());
    return result;
  }

  @Override
  public boolean equals(
                        Object obj) {
    if (this == obj) return true;
    if (!super.equals(obj)) return false;
    if (getClass() != obj.getClass()) return false;
    CapsuleerSyncTracker other = (CapsuleerSyncTracker) obj;
    if (accountBalanceDetail == null) {
      if (other.accountBalanceDetail != null) return false;
    } else if (!accountBalanceDetail.equals(other.accountBalanceDetail)) return false;
    if (accountBalanceStatus != other.accountBalanceStatus) return false;
    if (accountStatusDetail == null) {
      if (other.accountStatusDetail != null) return false;
    } else if (!accountStatusDetail.equals(other.accountStatusDetail)) return false;
    if (accountStatusStatus != other.accountStatusStatus) return false;
    if (assetListDetail == null) {
      if (other.assetListDetail != null) return false;
    } else if (!assetListDetail.equals(other.assetListDetail)) return false;
    if (assetListStatus != other.assetListStatus) return false;
    if (blueprintsDetail == null) {
      if (other.blueprintsDetail != null) return false;
    } else if (!blueprintsDetail.equals(other.blueprintsDetail)) return false;
    if (blueprintsStatus != other.blueprintsStatus) return false;
    if (bookmarksDetail == null) {
      if (other.bookmarksDetail != null) return false;
    } else if (!bookmarksDetail.equals(other.bookmarksDetail)) return false;
    if (bookmarksStatus != other.bookmarksStatus) return false;
    if (calendarEventAttendeesDetail == null) {
      if (other.calendarEventAttendeesDetail != null) return false;
    } else if (!calendarEventAttendeesDetail.equals(other.calendarEventAttendeesDetail)) return false;
    if (calendarEventAttendeesStatus != other.calendarEventAttendeesStatus) return false;
    if (characterSheetDetail == null) {
      if (other.characterSheetDetail != null) return false;
    } else if (!characterSheetDetail.equals(other.characterSheetDetail)) return false;
    if (characterSheetStatus != other.characterSheetStatus) return false;
    if (chatChannelsDetail == null) {
      if (other.chatChannelsDetail != null) return false;
    } else if (!chatChannelsDetail.equals(other.chatChannelsDetail)) return false;
    if (chatChannelsStatus != other.chatChannelsStatus) return false;
    if (contactListDetail == null) {
      if (other.contactListDetail != null) return false;
    } else if (!contactListDetail.equals(other.contactListDetail)) return false;
    if (contactListStatus != other.contactListStatus) return false;
    if (contactNotificationsDetail == null) {
      if (other.contactNotificationsDetail != null) return false;
    } else if (!contactNotificationsDetail.equals(other.contactNotificationsDetail)) return false;
    if (contactNotificationsStatus != other.contactNotificationsStatus) return false;
    if (contractBidsDetail == null) {
      if (other.contractBidsDetail != null) return false;
    } else if (!contractBidsDetail.equals(other.contractBidsDetail)) return false;
    if (contractBidsStatus != other.contractBidsStatus) return false;
    if (contractItemsDetail == null) {
      if (other.contractItemsDetail != null) return false;
    } else if (!contractItemsDetail.equals(other.contractItemsDetail)) return false;
    if (contractItemsStatus != other.contractItemsStatus) return false;
    if (contractsDetail == null) {
      if (other.contractsDetail != null) return false;
    } else if (!contractsDetail.equals(other.contractsDetail)) return false;
    if (contractsStatus != other.contractsStatus) return false;
    if (facWarStatsDetail == null) {
      if (other.facWarStatsDetail != null) return false;
    } else if (!facWarStatsDetail.equals(other.facWarStatsDetail)) return false;
    if (facWarStatsStatus != other.facWarStatsStatus) return false;
    if (industryJobsDetail == null) {
      if (other.industryJobsDetail != null) return false;
    } else if (!industryJobsDetail.equals(other.industryJobsDetail)) return false;
    if (industryJobsHistoryDetail == null) {
      if (other.industryJobsHistoryDetail != null) return false;
    } else if (!industryJobsHistoryDetail.equals(other.industryJobsHistoryDetail)) return false;
    if (industryJobsHistoryStatus != other.industryJobsHistoryStatus) return false;
    if (industryJobsStatus != other.industryJobsStatus) return false;
    if (killlogDetail == null) {
      if (other.killlogDetail != null) return false;
    } else if (!killlogDetail.equals(other.killlogDetail)) return false;
    if (killlogStatus != other.killlogStatus) return false;
    if (mailBodiesDetail == null) {
      if (other.mailBodiesDetail != null) return false;
    } else if (!mailBodiesDetail.equals(other.mailBodiesDetail)) return false;
    if (mailBodiesStatus != other.mailBodiesStatus) return false;
    if (mailMessagesDetail == null) {
      if (other.mailMessagesDetail != null) return false;
    } else if (!mailMessagesDetail.equals(other.mailMessagesDetail)) return false;
    if (mailMessagesStatus != other.mailMessagesStatus) return false;
    if (mailingListsDetail == null) {
      if (other.mailingListsDetail != null) return false;
    } else if (!mailingListsDetail.equals(other.mailingListsDetail)) return false;
    if (mailingListsStatus != other.mailingListsStatus) return false;
    if (marketOrdersDetail == null) {
      if (other.marketOrdersDetail != null) return false;
    } else if (!marketOrdersDetail.equals(other.marketOrdersDetail)) return false;
    if (marketOrdersStatus != other.marketOrdersStatus) return false;
    if (medalsDetail == null) {
      if (other.medalsDetail != null) return false;
    } else if (!medalsDetail.equals(other.medalsDetail)) return false;
    if (medalsStatus != other.medalsStatus) return false;
    if (notificationTextsDetail == null) {
      if (other.notificationTextsDetail != null) return false;
    } else if (!notificationTextsDetail.equals(other.notificationTextsDetail)) return false;
    if (notificationTextsStatus != other.notificationTextsStatus) return false;
    if (notificationsDetail == null) {
      if (other.notificationsDetail != null) return false;
    } else if (!notificationsDetail.equals(other.notificationsDetail)) return false;
    if (notificationsStatus != other.notificationsStatus) return false;
    if (planetaryColoniesDetail == null) {
      if (other.planetaryColoniesDetail != null) return false;
    } else if (!planetaryColoniesDetail.equals(other.planetaryColoniesDetail)) return false;
    if (planetaryColoniesStatus != other.planetaryColoniesStatus) return false;
    if (researchDetail == null) {
      if (other.researchDetail != null) return false;
    } else if (!researchDetail.equals(other.researchDetail)) return false;
    if (researchStatus != other.researchStatus) return false;
    if (skillInTrainingDetail == null) {
      if (other.skillInTrainingDetail != null) return false;
    } else if (!skillInTrainingDetail.equals(other.skillInTrainingDetail)) return false;
    if (skillInTrainingStatus != other.skillInTrainingStatus) return false;
    if (skillQueueDetail == null) {
      if (other.skillQueueDetail != null) return false;
    } else if (!skillQueueDetail.equals(other.skillQueueDetail)) return false;
    if (skillQueueStatus != other.skillQueueStatus) return false;
    if (standingsDetail == null) {
      if (other.standingsDetail != null) return false;
    } else if (!standingsDetail.equals(other.standingsDetail)) return false;
    if (standingsStatus != other.standingsStatus) return false;
    if (upcomingCalendarEventsDetail == null) {
      if (other.upcomingCalendarEventsDetail != null) return false;
    } else if (!upcomingCalendarEventsDetail.equals(other.upcomingCalendarEventsDetail)) return false;
    if (upcomingCalendarEventsStatus != other.upcomingCalendarEventsStatus) return false;
    if (walletJournalDetail == null) {
      if (other.walletJournalDetail != null) return false;
    } else if (!walletJournalDetail.equals(other.walletJournalDetail)) return false;
    if (walletJournalStatus != other.walletJournalStatus) return false;
    if (walletTransactionsDetail == null) {
      if (other.walletTransactionsDetail != null) return false;
    } else if (!walletTransactionsDetail.equals(other.walletTransactionsDetail)) return false;
    if (walletTransactionsStatus != other.walletTransactionsStatus) return false;
    return true;
  }

  @Override
  public String toString() {
    return "CapsuleerSyncTracker [accountStatusStatus=" + accountStatusStatus + ", accountStatusDetail=" + accountStatusDetail + ", accountBalanceStatus="
        + accountBalanceStatus + ", accountBalanceDetail=" + accountBalanceDetail + ", assetListStatus=" + assetListStatus + ", assetListDetail="
        + assetListDetail + ", calendarEventAttendeesStatus=" + calendarEventAttendeesStatus + ", calendarEventAttendeesDetail=" + calendarEventAttendeesDetail
        + ", characterSheetStatus=" + characterSheetStatus + ", characterSheetDetail=" + characterSheetDetail + ", chatChannelsStatus=" + chatChannelsStatus
        + ", chatChannelsDetail=" + chatChannelsDetail + ", contactListStatus=" + contactListStatus + ", contactListDetail=" + contactListDetail
        + ", contactNotificationsStatus=" + contactNotificationsStatus + ", contactNotificationsDetail=" + contactNotificationsDetail + ", blueprintsStatus="
        + blueprintsStatus + ", blueprintsDetail=" + blueprintsDetail + ", bookmarksStatus=" + bookmarksStatus + ", bookmarksDetail=" + bookmarksDetail
        + ", contractsStatus=" + contractsStatus + ", contractsDetail=" + contractsDetail + ", contractItemsStatus=" + contractItemsStatus
        + ", contractItemsDetail=" + contractItemsDetail + ", contractBidsStatus=" + contractBidsStatus + ", contractBidsDetail=" + contractBidsDetail
        + ", facWarStatsStatus=" + facWarStatsStatus + ", facWarStatsDetail=" + facWarStatsDetail + ", industryJobsStatus=" + industryJobsStatus
        + ", industryJobsDetail=" + industryJobsDetail + ", industryJobsHistoryStatus=" + industryJobsHistoryStatus + ", industryJobsHistoryDetail="
        + industryJobsHistoryDetail + ", killlogStatus=" + killlogStatus + ", killlogDetail=" + killlogDetail + ", mailBodiesStatus=" + mailBodiesStatus
        + ", mailBodiesDetail=" + mailBodiesDetail + ", mailingListsStatus=" + mailingListsStatus + ", mailingListsDetail=" + mailingListsDetail
        + ", mailMessagesStatus=" + mailMessagesStatus + ", mailMessagesDetail=" + mailMessagesDetail + ", marketOrdersStatus=" + marketOrdersStatus
        + ", marketOrdersDetail=" + marketOrdersDetail + ", medalsStatus=" + medalsStatus + ", medalsDetail=" + medalsDetail + ", notificationsStatus="
        + notificationsStatus + ", notificationsDetail=" + notificationsDetail + ", notificationTextsStatus=" + notificationTextsStatus
        + ", notificationTextsDetail=" + notificationTextsDetail + ", planetaryColoniesStatus=" + planetaryColoniesStatus + ", planetaryColoniesDetail="
        + planetaryColoniesDetail + ", researchStatus=" + researchStatus + ", researchDetail=" + researchDetail + ", skillInTrainingStatus="
        + skillInTrainingStatus + ", skillInTrainingDetail=" + skillInTrainingDetail + ", skillQueueStatus=" + skillQueueStatus + ", skillQueueDetail="
        + skillQueueDetail + ", standingsStatus=" + standingsStatus + ", standingsDetail=" + standingsDetail + ", upcomingCalendarEventsStatus="
        + upcomingCalendarEventsStatus + ", upcomingCalendarEventsDetail=" + upcomingCalendarEventsDetail + ", walletJournalStatus=" + walletJournalStatus
        + ", walletJournalDetail=" + walletJournalDetail + ", walletTransactionsStatus=" + walletTransactionsStatus + ", walletTransactionsDetail="
        + walletTransactionsDetail + "]";
  }

  public static CapsuleerSyncTracker createOrGetUnfinishedTracker(
                                                                  final SynchronizedEveAccount syncAccount) {
    try {
      return EveKitUserAccountProvider.getFactory().runTransaction(new RunInTransaction<CapsuleerSyncTracker>() {
        @Override
        public CapsuleerSyncTracker run() throws Exception {
          CapsuleerSyncTracker result = getUnfinishedTracker(syncAccount);
          if (result != null) return result;
          result = new CapsuleerSyncTracker();
          result.account = syncAccount;
          result.syncStart = OrbitalProperties.getCurrentTime();
          result.setFinished(false);
          return EveKitUserAccountProvider.getFactory().getEntityManager().merge(result);
        }
      });
    } catch (Exception e) {
      log.log(Level.SEVERE, "query error", e);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public static CapsuleerSyncTracker getUnfinishedTracker(
                                                          final SynchronizedEveAccount syncAccount) {
    return SyncTracker.<CapsuleerSyncTracker> getUnfinishedTracker(syncAccount);
  }

  public static List<CapsuleerSyncTracker> getAllUnfinishedTrackers() {
    try {
      return EveKitUserAccountProvider.getFactory().runTransaction(new RunInTransaction<List<CapsuleerSyncTracker>>() {
        @Override
        public List<CapsuleerSyncTracker> run() throws Exception {
          TypedQuery<CapsuleerSyncTracker> getter = EveKitUserAccountProvider.getFactory().getEntityManager()
              .createNamedQuery("CapsuleerSyncTracker.getAllUnfinished", CapsuleerSyncTracker.class);
          return getter.getResultList();
        }
      });
    } catch (Exception e) {
      log.log(Level.SEVERE, "query error", e);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public static CapsuleerSyncTracker getLatestFinishedTracker(
                                                              final SynchronizedEveAccount owner) {
    return SyncTracker.<CapsuleerSyncTracker> getLatestFinishedTracker(owner);
  }

  public static List<CapsuleerSyncTracker> getHistory(
                                                      final SynchronizedEveAccount owner,
                                                      final long contid,
                                                      final int maxResults) {
    try {
      return EveKitUserAccountProvider.getFactory().runTransaction(new RunInTransaction<List<CapsuleerSyncTracker>>() {
        @Override
        public List<CapsuleerSyncTracker> run() throws Exception {
          TypedQuery<CapsuleerSyncTracker> getter = EveKitUserAccountProvider.getFactory().getEntityManager()
              .createNamedQuery("CapsuleerSyncTracker.getHistory", CapsuleerSyncTracker.class);
          getter.setParameter("account", owner);
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

  public static List<CapsuleerSyncTracker> getSummary(
                                                      final Date fromDate) {
    try {
      return EveKitUserAccountProvider.getFactory().runTransaction(new RunInTransaction<List<CapsuleerSyncTracker>>() {
        @Override
        public List<CapsuleerSyncTracker> run() throws Exception {
          TypedQuery<CapsuleerSyncTracker> getter = EveKitUserAccountProvider.getFactory().getEntityManager()
              .createNamedQuery("CapsuleerSyncTracker.getSummary", CapsuleerSyncTracker.class);
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
    summary.append("Capsuleer Sync Tracker Error Summary on ");
    long days = day.getTime() / (1000 * 60 * 60 * 24);
    Date dayStart = new Date(days * 1000 * 60 * 60 * 24 + 1);
    Date nextDay = new Date(dayStart.getTime() + (1000 * 60 * 60 * 24) - 1);
    summary.append(DateFormat.getDateInstance().format(dayStart)).append('\n');
    List<CapsuleerSyncTracker> result = getSummary(dayStart);
    if (result == null) result = Collections.emptyList();

    // Process sync results with error.
    int errorCount = 0;
    Map<String, Map<String, AtomicInteger>> data = new HashMap<String, Map<String, AtomicInteger>>();
    for (CapsuleerSyncTracker next : result) {
      if (new Date(next.getSyncEnd()).after(nextDay)) continue;
      if (next.accountStatusStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("accountStatus", next.accountStatusDetail, data);
      } else if (next.accountBalanceStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("accountBalance", next.accountBalanceDetail, data);
      } else if (next.assetListStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("assetList", next.assetListDetail, data);
      } else if (next.calendarEventAttendeesStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("calendarEventAttendees", next.calendarEventAttendeesDetail, data);
      } else if (next.characterSheetStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("characterSheet", next.characterSheetDetail, data);
      } else if (next.chatChannelsStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("chatChannels", next.chatChannelsDetail, data);
      } else if (next.contactListStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("contactList", next.contactListDetail, data);
      } else if (next.contactNotificationsStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("contactNotifications", next.contactNotificationsDetail, data);
      } else if (next.blueprintsStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("blueprints", next.blueprintsDetail, data);
      } else if (next.bookmarksStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("bookmarks", next.bookmarksDetail, data);
      } else if (next.contractsStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("contracts", next.contractsDetail, data);
      } else if (next.contractItemsStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("contractItems", next.contractItemsDetail, data);
      } else if (next.contractBidsStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("contractBids", next.contractBidsDetail, data);
      } else if (next.facWarStatsStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("facWarStats", next.facWarStatsDetail, data);
      } else if (next.industryJobsStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("industryJobs", next.industryJobsDetail, data);
      } else if (next.industryJobsHistoryStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("industryJobsHistory", next.industryJobsHistoryDetail, data);
      } else if (next.killlogStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("killlog", next.killlogDetail, data);
      } else if (next.mailBodiesStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("mailBodies", next.mailBodiesDetail, data);
      } else if (next.mailingListsStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("mailingLists", next.mailingListsDetail, data);
      } else if (next.mailMessagesStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("mailMessages", next.mailMessagesDetail, data);
      } else if (next.marketOrdersStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("marketOrders", next.marketOrdersDetail, data);
      } else if (next.medalsStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("medals", next.medalsDetail, data);
      } else if (next.notificationsStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("notifications", next.notificationsDetail, data);
      } else if (next.notificationTextsStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("notificationTexts", next.notificationTextsDetail, data);
      } else if (next.planetaryColoniesStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("planetaryColonies", next.planetaryColoniesDetail, data);
      } else if (next.researchStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("research", next.researchDetail, data);
      } else if (next.skillInTrainingStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("skillInTraining", next.skillInTrainingDetail, data);
      } else if (next.skillQueueStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("skillQueue", next.skillQueueDetail, data);
      } else if (next.standingsStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("standings", next.standingsDetail, data);
      } else if (next.upcomingCalendarEventsStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("upcomingCalendarEvents", next.upcomingCalendarEventsDetail, data);
      } else if (next.walletJournalStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("walletJournal", next.walletJournalDetail, data);
      } else if (next.walletTransactionsStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("walletTransactions", next.walletTransactionsDetail, data);
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
