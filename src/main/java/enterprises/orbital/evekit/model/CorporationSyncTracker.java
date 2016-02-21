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

import enterprises.orbital.base.OrbitalProperties;
import enterprises.orbital.db.ConnectionFactory.RunInTransaction;
import enterprises.orbital.evekit.account.EveKitUserAccountProvider;
import enterprises.orbital.evekit.account.SynchronizedEveAccount;

/**
 * Object to track synchronization request for corporation. A synchronization tracker starts out empty and fills in each supported field type until it is marked
 * as finished. This makes it relatively straightforward to cache a tracker since slightly stale copies are usually harmless.
 */
@Entity
@Table(name = "evekit_corp_sync")
@NamedQueries({
    @NamedQuery(name = "CorporationSyncTracker.get", query = "SELECT c FROM CorporationSyncTracker c where c.account = :account and c.syncStart = :start"),
    @NamedQuery(name = "CorporationSyncTracker.getAllUnfinished", query = "SELECT c FROM CorporationSyncTracker c where c.finished = false"),
    @NamedQuery(
        name = "CorporationSyncTracker.getHistory",
        query = "SELECT c FROM CorporationSyncTracker c where c.account = :account and c.finished = true and c.syncStart < :start order by c.syncStart desc"),
    @NamedQuery(name = "CorporationSyncTracker.getSummary", query = "SELECT c FROM CorporationSyncTracker c where c.finished = true and c.syncStart >= :start"),
})
public class CorporationSyncTracker extends SyncTracker {
  private static final Logger   log = Logger.getLogger(CorporationSyncTracker.class.getName());

  // Status of each element we're synchronizing. If status is
  // SYNC_ERROR, then the "detail" field contains text explaining the error.
  private SyncTracker.SyncState accountBalanceStatus;
  private String                accountBalanceDetail;
  private SyncTracker.SyncState assetListStatus;
  private String                assetListDetail;
  private SyncTracker.SyncState corporationSheetStatus;
  private String                corporationSheetDetail;
  private SyncTracker.SyncState contactListStatus;
  private String                contactListDetail;
  private SyncTracker.SyncState customsOfficeStatus;
  private String                customsOfficeDetail;
  private SyncTracker.SyncState blueprintsStatus;
  private String                blueprintsDetail;
  private SyncTracker.SyncState bookmarksStatus;
  private String                bookmarksDetail;
  private SyncTracker.SyncState contractsStatus;
  private String                contractsDetail;
  private SyncTracker.SyncState contractItemsStatus;
  private String                contractItemsDetail;
  private SyncTracker.SyncState contractBidsStatus;
  private String                contractBidsDetail;
  private SyncTracker.SyncState facWarStatsStatus;
  private String                facWarStatsDetail;
  private SyncTracker.SyncState facilitiesStatus;
  private String                facilitiesDetail;
  private SyncTracker.SyncState industryJobsStatus;
  private String                industryJobsDetail;
  private SyncTracker.SyncState industryJobsHistoryStatus;
  private String                industryJobsHistoryDetail;
  private SyncTracker.SyncState killlogStatus;
  private String                killlogDetail;
  private SyncTracker.SyncState marketOrdersStatus;
  private String                marketOrdersDetail;
  private SyncTracker.SyncState memberMedalsStatus;
  private String                memberMedalsDetail;
  private SyncTracker.SyncState standingsStatus;
  private String                standingsDetail;
  private SyncTracker.SyncState walletJournalStatus;
  private String                walletJournalDetail;
  private SyncTracker.SyncState walletTransactionsStatus;
  private String                walletTransactionsDetail;
  private SyncTracker.SyncState memberSecurityStatus;
  private String                memberSecurityDetail;
  private SyncTracker.SyncState containerLogStatus;
  private String                containerLogDetail;
  private SyncTracker.SyncState memberSecurityLogStatus;
  private String                memberSecurityLogDetail;
  private SyncTracker.SyncState memberTrackingStatus;
  private String                memberTrackingDetail;
  private SyncTracker.SyncState corpMedalsStatus;
  private String                corpMedalsDetail;
  private SyncTracker.SyncState outpostListStatus;
  private String                outpostListDetail;
  private SyncTracker.SyncState outpostDetailStatus;
  private String                outpostDetailDetail;
  private SyncTracker.SyncState shareholderStatus;
  private String                shareholderDetail;
  private SyncTracker.SyncState starbaseListStatus;
  private String                starbaseListDetail;
  private SyncTracker.SyncState starbaseDetailStatus;
  private String                starbaseDetailDetail;
  private SyncTracker.SyncState corpTitlesStatus;
  private String                corpTitlesDetail;

  public CorporationSyncTracker() {
    accountBalanceStatus = SyncTracker.SyncState.NOT_PROCESSED;
    assetListStatus = SyncTracker.SyncState.NOT_PROCESSED;
    corporationSheetStatus = SyncTracker.SyncState.NOT_PROCESSED;
    contactListStatus = SyncTracker.SyncState.NOT_PROCESSED;
    customsOfficeStatus = SyncTracker.SyncState.NOT_PROCESSED;
    blueprintsStatus = SyncTracker.SyncState.NOT_PROCESSED;
    bookmarksStatus = SyncTracker.SyncState.NOT_PROCESSED;
    contractsStatus = SyncTracker.SyncState.NOT_PROCESSED;
    contractItemsStatus = SyncTracker.SyncState.NOT_PROCESSED;
    contractBidsStatus = SyncTracker.SyncState.NOT_PROCESSED;
    facWarStatsStatus = SyncTracker.SyncState.NOT_PROCESSED;
    facilitiesStatus = SyncTracker.SyncState.NOT_PROCESSED;
    industryJobsStatus = SyncTracker.SyncState.NOT_PROCESSED;
    industryJobsHistoryStatus = SyncTracker.SyncState.NOT_PROCESSED;
    killlogStatus = SyncTracker.SyncState.NOT_PROCESSED;
    marketOrdersStatus = SyncTracker.SyncState.NOT_PROCESSED;
    memberMedalsStatus = SyncTracker.SyncState.NOT_PROCESSED;
    standingsStatus = SyncTracker.SyncState.NOT_PROCESSED;
    walletJournalStatus = SyncTracker.SyncState.NOT_PROCESSED;
    walletTransactionsStatus = SyncTracker.SyncState.NOT_PROCESSED;
    memberSecurityStatus = SyncTracker.SyncState.NOT_PROCESSED;
    containerLogStatus = SyncTracker.SyncState.NOT_PROCESSED;
    memberSecurityLogStatus = SyncTracker.SyncState.NOT_PROCESSED;
    memberTrackingStatus = SyncTracker.SyncState.NOT_PROCESSED;
    corpMedalsStatus = SyncTracker.SyncState.NOT_PROCESSED;
    outpostListStatus = SyncTracker.SyncState.NOT_PROCESSED;
    outpostDetailStatus = SyncTracker.SyncState.NOT_PROCESSED;
    shareholderStatus = SyncTracker.SyncState.NOT_PROCESSED;
    starbaseListStatus = SyncTracker.SyncState.NOT_PROCESSED;
    starbaseDetailStatus = SyncTracker.SyncState.NOT_PROCESSED;
    corpTitlesStatus = SyncTracker.SyncState.NOT_PROCESSED;
  }

  @Override
  public void setState(SynchronizationState state, SyncTracker.SyncState status, String msg) {
    switch (state) {

    case SYNC_CORP_ACCOUNTBALANCE:
      setAccountBalanceStatus(status);
      setAccountBalanceDetail(msg);
      break;

    case SYNC_CORP_ASSETLIST:
      setAssetListStatus(status);
      setAssetListDetail(msg);
      break;

    case SYNC_CORP_CORPSHEET:
      setCorporationSheetStatus(status);
      setCorporationSheetDetail(msg);
      break;

    case SYNC_CORP_CONTACTLIST:
      setContactListStatus(status);
      setContactListDetail(msg);
      break;

    case SYNC_CORP_BLUEPRINTS:
      setBlueprintsStatus(status);
      setBlueprintsDetail(msg);
      break;

    case SYNC_CORP_BOOKMARKS:
      setBookmarksStatus(status);
      setBookmarksDetail(msg);
      break;

    case SYNC_CORP_CONTRACTS:
      setContractsStatus(status);
      setContractsDetail(msg);
      break;

    case SYNC_CORP_CONTRACTITEMS:
      setContractItemsStatus(status);
      setContractItemsDetail(msg);
      break;

    case SYNC_CORP_CONTRACTBIDS:
      setContractBidsStatus(status);
      setContractBidsDetail(msg);
      break;

    case SYNC_CORP_FACWARSTATS:
      setFacWarStatsStatus(status);
      setFacWarStatsDetail(msg);
      break;

    case SYNC_CORP_FACILITIES:
      setFacilitiesStatus(status);
      setFacilitiesDetail(msg);
      break;

    case SYNC_CORP_INDUSTRYJOBS:
      setIndustryJobsStatus(status);
      setIndustryJobsDetail(msg);
      break;

    case SYNC_CORP_INDUSTRYJOBSHISTORY:
      setIndustryJobsHistoryStatus(status);
      setIndustryJobsHistoryDetail(msg);
      break;

    case SYNC_CORP_KILLLOG:
      setKilllogStatus(status);
      setKilllogDetail(msg);
      break;

    case SYNC_CORP_MARKETORDERS:
      setMarketOrdersStatus(status);
      setMarketOrdersDetail(msg);
      break;

    case SYNC_CORP_MEMBERMEDALS:
      setMemberMedalsStatus(status);
      setMemberMedalsDetail(msg);
      break;

    case SYNC_CORP_STANDINGS:
      setStandingsStatus(status);
      setStandingsDetail(msg);
      break;

    case SYNC_CORP_WALLETJOURNAL:
      setWalletJournalStatus(status);
      setWalletJournalDetail(msg);
      break;

    case SYNC_CORP_WALLETTRANSACTIONS:
      setWalletTransactionsStatus(status);
      setWalletTransactionsDetail(msg);
      break;

    case SYNC_CORP_SECURITY:
      setMemberSecurityStatus(status);
      setMemberSecurityDetail(msg);
      break;

    case SYNC_CORP_CONTAINERLOG:
      setContainerLogStatus(status);
      setContainerLogDetail(msg);
      break;

    case SYNC_CORP_MEMBERSECURITYLOG:
      setMemberSecurityLogStatus(status);
      setMemberSecurityLogDetail(msg);
      break;

    case SYNC_CORP_MEMBERTRACKING:
      setMemberTrackingStatus(status);
      setMemberTrackingDetail(msg);
      break;

    case SYNC_CORP_CORPMEDALS:
      setCorpMedalsStatus(status);
      setCorpMedalsDetail(msg);
      break;

    case SYNC_CORP_OUTPOSTLIST:
      setOutpostListStatus(status);
      setOutpostListDetail(msg);
      break;

    case SYNC_CORP_OUTPOSTDETAIL:
      setOutpostDetailStatus(status);
      setOutpostDetailDetail(msg);
      break;

    case SYNC_CORP_SHAREHOLDERS:
      setShareholderStatus(status);
      setShareholderDetail(msg);
      break;

    case SYNC_CORP_STARBASELIST:
      setStarbaseListStatus(status);
      setStarbaseListDetail(msg);
      break;

    case SYNC_CORP_STARBASEDETAIL:
      setStarbaseDetailStatus(status);
      setStarbaseDetailDetail(msg);
      break;

    case SYNC_CORP_CORPTITLES:
      setCorpTitlesStatus(status);
      setCorpTitlesDetail(msg);
      break;

    default:
      // NOP
      ;
    }
  }

  @Override
  public SynchronizationState trackerComplete(Set<SynchronizationState> checkState) {
    for (SynchronizationState next : checkState) {
      switch (next) {
      case SYNC_CORP_ACCOUNTBALANCE:
        if (accountBalanceStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CORP_ASSETLIST:
        if (assetListStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CORP_CORPSHEET:
        if (corporationSheetStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CORP_CONTACTLIST:
        if (contactListStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CORP_CUSTOMSOFFICE:
        if (customsOfficeStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CORP_BLUEPRINTS:
        if (blueprintsStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CORP_BOOKMARKS:
        if (bookmarksStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CORP_CONTRACTS:
        if (contractsStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CORP_CONTRACTITEMS:
        // Contract items can only be next if we've completed contracts
        if (contractsStatus != SyncTracker.SyncState.NOT_PROCESSED && contractItemsStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CORP_CONTRACTBIDS:
        if (contractBidsStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CORP_FACILITIES:
        if (facilitiesStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CORP_FACWARSTATS:
        if (facWarStatsStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CORP_INDUSTRYJOBS:
        if (industryJobsStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CORP_INDUSTRYJOBSHISTORY:
        if (industryJobsHistoryStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CORP_KILLLOG:
        if (killlogStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CORP_MARKETORDERS:
        if (marketOrdersStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CORP_MEMBERMEDALS:
        if (memberMedalsStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CORP_STANDINGS:
        if (standingsStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CORP_WALLETJOURNAL:
        if (walletJournalStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CORP_WALLETTRANSACTIONS:
        if (walletTransactionsStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CORP_SECURITY:
        if (memberSecurityStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CORP_CONTAINERLOG:
        if (containerLogStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CORP_MEMBERSECURITYLOG:
        if (memberSecurityLogStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CORP_MEMBERTRACKING:
        if (memberTrackingStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CORP_CORPMEDALS:
        if (corpMedalsStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CORP_OUTPOSTLIST:
        if (outpostListStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CORP_OUTPOSTDETAIL:
        // Outpost details can only be next if we've completed outpost list
        if (outpostListStatus != SyncTracker.SyncState.NOT_PROCESSED && outpostDetailStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CORP_SHAREHOLDERS:
        if (shareholderStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CORP_STARBASELIST:
        if (starbaseListStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CORP_STARBASEDETAIL:
        // Starbase details can only be next if we've completed starbase list
        if (starbaseListStatus != SyncTracker.SyncState.NOT_PROCESSED && starbaseDetailStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CORP_CORPTITLES:
        if (corpTitlesStatus == SyncTracker.SyncState.NOT_PROCESSED) return next;
        break;

      case SYNC_CORP_START:
      case SYNC_CORP_END:
      default:
        // NOP
        ;
      }
    }

    return null;
  }

  public void setMemberMedalsStatus(SyncTracker.SyncState memberMedalsStatus) {
    this.memberMedalsStatus = memberMedalsStatus;
  }

  public void setMemberSecurityStatus(SyncTracker.SyncState memberSecurityStatus) {
    this.memberSecurityStatus = memberSecurityStatus;
  }

  public void setContainerLogStatus(SyncTracker.SyncState containerLogStatus) {
    this.containerLogStatus = containerLogStatus;
  }

  public void setMemberSecurityLogStatus(SyncTracker.SyncState memberSecurityLogStatus) {
    this.memberSecurityLogStatus = memberSecurityLogStatus;
  }

  public void setMemberTrackingStatus(SyncTracker.SyncState memberTrackingStatus) {
    this.memberTrackingStatus = memberTrackingStatus;
  }

  public void setCorpMedalsStatus(SyncTracker.SyncState corpMedalsStatus) {
    this.corpMedalsStatus = corpMedalsStatus;
  }

  public void setOutpostListStatus(SyncTracker.SyncState outpostListStatus) {
    this.outpostListStatus = outpostListStatus;
  }

  public void setOutpostDetailStatus(SyncTracker.SyncState outpostDetailStatus) {
    this.outpostDetailStatus = outpostDetailStatus;
  }

  public void setShareholderStatus(SyncTracker.SyncState shareholderStatus) {
    this.shareholderStatus = shareholderStatus;
  }

  public void setStarbaseListStatus(SyncTracker.SyncState starbaseListStatus) {
    this.starbaseListStatus = starbaseListStatus;
  }

  public void setStarbaseDetailStatus(SyncTracker.SyncState starbaseDetailStatus) {
    this.starbaseDetailStatus = starbaseDetailStatus;
  }

  public void setCorpTitlesStatus(SyncTracker.SyncState corpTitlesStatus) {
    this.corpTitlesStatus = corpTitlesStatus;
  }

  public SyncTracker.SyncState getAccountBalanceStatus() {
    return accountBalanceStatus;
  }

  public void setAccountBalanceStatus(SyncTracker.SyncState accountBalanceStatus) {
    this.accountBalanceStatus = accountBalanceStatus;
  }

  public String getAccountBalanceDetail() {
    return accountBalanceDetail;
  }

  public void setAccountBalanceDetail(String accountBalanceDetail) {
    this.accountBalanceDetail = accountBalanceDetail;
  }

  public SyncTracker.SyncState getAssetListStatus() {
    return assetListStatus;
  }

  public void setAssetListStatus(SyncTracker.SyncState assetListStatus) {
    this.assetListStatus = assetListStatus;
  }

  public String getAssetListDetail() {
    return assetListDetail;
  }

  public void setAssetListDetail(String assetListDetail) {
    this.assetListDetail = assetListDetail;
  }

  public SyncTracker.SyncState getCorporationSheetStatus() {
    return corporationSheetStatus;
  }

  public void setCorporationSheetStatus(SyncTracker.SyncState corporationSheetStatus) {
    this.corporationSheetStatus = corporationSheetStatus;
  }

  public String getCorporationSheetDetail() {
    return corporationSheetDetail;
  }

  public void setCorporationSheetDetail(String corporationSheetDetail) {
    this.corporationSheetDetail = corporationSheetDetail;
  }

  public SyncTracker.SyncState getContactListStatus() {
    return contactListStatus;
  }

  public void setContactListStatus(SyncTracker.SyncState contactListStatus) {
    this.contactListStatus = contactListStatus;
  }

  public String getContactListDetail() {
    return contactListDetail;
  }

  public void setContactListDetail(String contactListDetail) {
    this.contactListDetail = contactListDetail;
  }

  public SyncTracker.SyncState getCustomsOfficeStatus() {
    return customsOfficeStatus;
  }

  public void setCustomsOfficeStatus(SyncTracker.SyncState customsOfficeStatus) {
    this.customsOfficeStatus = customsOfficeStatus;
  }

  public String getCustomsOfficeDetail() {
    return customsOfficeDetail;
  }

  public void setCustomsOfficeDetail(String customsOfficeDetail) {
    this.customsOfficeDetail = customsOfficeDetail;
  }

  public SyncTracker.SyncState getBlueprintsStatus() {
    return blueprintsStatus;
  }

  public void setBlueprintsStatus(SyncTracker.SyncState blueprintsStatus) {
    this.blueprintsStatus = blueprintsStatus;
  }

  public String getBlueprintsDetail() {
    return blueprintsDetail;
  }

  public void setBlueprintsDetail(String blueprintsDetail) {
    this.blueprintsDetail = blueprintsDetail;
  }

  public SyncTracker.SyncState getBookmarksStatus() {
    return bookmarksStatus;
  }

  public void setBookmarksStatus(SyncTracker.SyncState bookmarksStatus) {
    this.bookmarksStatus = bookmarksStatus;
  }

  public String getBookmarksDetail() {
    return bookmarksDetail;
  }

  public void setBookmarksDetail(String bookmarksDetail) {
    this.bookmarksDetail = bookmarksDetail;
  }

  public SyncTracker.SyncState getContractsStatus() {
    return contractsStatus;
  }

  public void setContractsStatus(SyncTracker.SyncState contractsStatus) {
    this.contractsStatus = contractsStatus;
  }

  public String getContractsDetail() {
    return contractsDetail;
  }

  public void setContractsDetail(String contractsDetail) {
    this.contractsDetail = contractsDetail;
  }

  public SyncTracker.SyncState getContractItemsStatus() {
    return contractItemsStatus;
  }

  public void setContractItemsStatus(SyncTracker.SyncState contractItemsStatus) {
    this.contractItemsStatus = contractItemsStatus;
  }

  public String getContractItemsDetail() {
    return contractItemsDetail;
  }

  public void setContractItemsDetail(String contractItemsDetail) {
    this.contractItemsDetail = contractItemsDetail;
  }

  public SyncTracker.SyncState getContractBidsStatus() {
    return contractBidsStatus;
  }

  public void setContractBidsStatus(SyncTracker.SyncState contractBidsStatus) {
    this.contractBidsStatus = contractBidsStatus;
  }

  public String getContractBidsDetail() {
    return contractBidsDetail;
  }

  public void setContractBidsDetail(String contractBidsDetail) {
    this.contractBidsDetail = contractBidsDetail;
  }

  public SyncTracker.SyncState getFacWarStatsStatus() {
    return facWarStatsStatus;
  }

  public void setFacWarStatsStatus(SyncTracker.SyncState facWarStatsStatus) {
    this.facWarStatsStatus = facWarStatsStatus;
  }

  public String getFacWarStatsDetail() {
    return facWarStatsDetail;
  }

  public void setFacWarStatsDetail(String facWarStatsDetail) {
    this.facWarStatsDetail = facWarStatsDetail;
  }

  public String getFacilitiesDetail() {
    return facilitiesDetail;
  }

  public void setFacilitiesDetail(String facilitiesDetail) {
    this.facilitiesDetail = facilitiesDetail;
  }

  public SyncTracker.SyncState getFacilitiesStatus() {
    return facilitiesStatus;
  }

  public void setFacilitiesStatus(SyncTracker.SyncState facilitiesStatus) {
    this.facilitiesStatus = facilitiesStatus;
  }

  public SyncTracker.SyncState getIndustryJobsStatus() {
    return industryJobsStatus;
  }

  public void setIndustryJobsStatus(SyncTracker.SyncState industryJobsStatus) {
    this.industryJobsStatus = industryJobsStatus;
  }

  public String getIndustryJobsDetail() {
    return industryJobsDetail;
  }

  public void setIndustryJobsDetail(String industryJobsDetail) {
    this.industryJobsDetail = industryJobsDetail;
  }

  public SyncTracker.SyncState getIndustryJobsHistoryStatus() {
    return industryJobsHistoryStatus;
  }

  public void setIndustryJobsHistoryStatus(SyncTracker.SyncState industryJobsHistoryStatus) {
    this.industryJobsHistoryStatus = industryJobsHistoryStatus;
  }

  public String getIndustryJobsHistoryDetail() {
    return industryJobsHistoryDetail;
  }

  public void setIndustryJobsHistoryDetail(String industryJobsHistoryDetail) {
    this.industryJobsHistoryDetail = industryJobsHistoryDetail;
  }

  public SyncTracker.SyncState getKilllogStatus() {
    return killlogStatus;
  }

  public void setKilllogStatus(SyncTracker.SyncState killlogStatus) {
    this.killlogStatus = killlogStatus;
  }

  public String getKilllogDetail() {
    return killlogDetail;
  }

  public void setKilllogDetail(String killlogDetail) {
    this.killlogDetail = killlogDetail;
  }

  public SyncTracker.SyncState getMarketOrdersStatus() {
    return marketOrdersStatus;
  }

  public void setMarketOrdersStatus(SyncTracker.SyncState marketOrdersStatus) {
    this.marketOrdersStatus = marketOrdersStatus;
  }

  public String getMarketOrdersDetail() {
    return marketOrdersDetail;
  }

  public void setMarketOrdersDetail(String marketOrdersDetail) {
    this.marketOrdersDetail = marketOrdersDetail;
  }

  public String getMemberMedalsDetail() {
    return memberMedalsDetail;
  }

  public void setMemberMedalsDetail(String memberMedalsDetail) {
    this.memberMedalsDetail = memberMedalsDetail;
  }

  public SyncTracker.SyncState getStandingsStatus() {
    return standingsStatus;
  }

  public void setStandingsStatus(SyncTracker.SyncState standingsStatus) {
    this.standingsStatus = standingsStatus;
  }

  public String getStandingsDetail() {
    return standingsDetail;
  }

  public void setStandingsDetail(String standingsDetail) {
    this.standingsDetail = standingsDetail;
  }

  public SyncTracker.SyncState getWalletJournalStatus() {
    return walletJournalStatus;
  }

  public void setWalletJournalStatus(SyncTracker.SyncState walletJournalStatus) {
    this.walletJournalStatus = walletJournalStatus;
  }

  public String getWalletJournalDetail() {
    return walletJournalDetail;
  }

  public void setWalletJournalDetail(String walletJournalDetail) {
    this.walletJournalDetail = walletJournalDetail;
  }

  public SyncTracker.SyncState getWalletTransactionsStatus() {
    return walletTransactionsStatus;
  }

  public void setWalletTransactionsStatus(SyncTracker.SyncState walletTransactionsStatus) {
    this.walletTransactionsStatus = walletTransactionsStatus;
  }

  public String getWalletTransactionsDetail() {
    return walletTransactionsDetail;
  }

  public void setWalletTransactionsDetail(String walletTransactionsDetail) {
    this.walletTransactionsDetail = walletTransactionsDetail;
  }

  public String getMemberSecurityDetail() {
    return memberSecurityDetail;
  }

  public void setMemberSecurityDetail(String memberSecurityDetail) {
    this.memberSecurityDetail = memberSecurityDetail;
  }

  public String getContainerLogDetail() {
    return containerLogDetail;
  }

  public void setContainerLogDetail(String containerLogDetail) {
    this.containerLogDetail = containerLogDetail;
  }

  public String getMemberSecurityLogDetail() {
    return memberSecurityLogDetail;
  }

  public void setMemberSecurityLogDetail(String memberSecurityLogDetail) {
    this.memberSecurityLogDetail = memberSecurityLogDetail;
  }

  public String getMemberTrackingDetail() {
    return memberTrackingDetail;
  }

  public void setMemberTrackingDetail(String memberTrackingDetail) {
    this.memberTrackingDetail = memberTrackingDetail;
  }

  public String getCorpMedalsDetail() {
    return corpMedalsDetail;
  }

  public void setCorpMedalsDetail(String corpMedalsDetail) {
    this.corpMedalsDetail = corpMedalsDetail;
  }

  public String getOutpostListDetail() {
    return outpostListDetail;
  }

  public void setOutpostListDetail(String outpostListDetail) {
    this.outpostListDetail = outpostListDetail;
  }

  public String getOutpostDetailDetail() {
    return outpostDetailDetail;
  }

  public void setOutpostDetailDetail(String outpostDetailDetail) {
    this.outpostDetailDetail = outpostDetailDetail;
  }

  public String getShareholderDetail() {
    return shareholderDetail;
  }

  public void setShareholderDetail(String shareholderDetail) {
    this.shareholderDetail = shareholderDetail;
  }

  public String getStarbaseListDetail() {
    return starbaseListDetail;
  }

  public void setStarbaseListDetail(String starbaseListDetail) {
    this.starbaseListDetail = starbaseListDetail;
  }

  public String getStarbaseDetailDetail() {
    return starbaseDetailDetail;
  }

  public void setStarbaseDetailDetail(String starbaseDetailDetail) {
    this.starbaseDetailDetail = starbaseDetailDetail;
  }

  public String getCorpTitlesDetail() {
    return corpTitlesDetail;
  }

  public void setCorpTitlesDetail(String corpTitlesDetail) {
    this.corpTitlesDetail = corpTitlesDetail;
  }

  public SyncTracker.SyncState getMemberMedalsStatus() {
    return memberMedalsStatus;
  }

  public SyncTracker.SyncState getMemberSecurityStatus() {
    return memberSecurityStatus;
  }

  public SyncTracker.SyncState getContainerLogStatus() {
    return containerLogStatus;
  }

  public SyncTracker.SyncState getMemberSecurityLogStatus() {
    return memberSecurityLogStatus;
  }

  public SyncTracker.SyncState getMemberTrackingStatus() {
    return memberTrackingStatus;
  }

  public SyncTracker.SyncState getCorpMedalsStatus() {
    return corpMedalsStatus;
  }

  public SyncTracker.SyncState getOutpostListStatus() {
    return outpostListStatus;
  }

  public SyncTracker.SyncState getOutpostDetailStatus() {
    return outpostDetailStatus;
  }

  public SyncTracker.SyncState getShareholderStatus() {
    return shareholderStatus;
  }

  public SyncTracker.SyncState getStarbaseListStatus() {
    return starbaseListStatus;
  }

  public SyncTracker.SyncState getStarbaseDetailStatus() {
    return starbaseDetailStatus;
  }

  public SyncTracker.SyncState getCorpTitlesStatus() {
    return corpTitlesStatus;
  }

  @Override
  public String toString() {
    return "CorporationSyncTracker [accountBalanceStatus=" + accountBalanceStatus + ", accountBalanceDetail=" + accountBalanceDetail + ", assetListStatus="
        + assetListStatus + ", assetListDetail=" + assetListDetail + ", corporationSheetStatus=" + corporationSheetStatus + ", corporationSheetDetail="
        + corporationSheetDetail + ", contactListStatus=" + contactListStatus + ", contactListDetail=" + contactListDetail + ", customsOfficeStatus="
        + customsOfficeStatus + ", customsOfficeDetail=" + customsOfficeDetail + ", blueprintsStatus=" + blueprintsStatus + ", blueprintsDetail="
        + blueprintsDetail + ", bookmarksStatus=" + bookmarksStatus + ", bookmarksDetail=" + bookmarksDetail + ", contractsStatus=" + contractsStatus
        + ", contractsDetail=" + contractsDetail + ", contractItemsStatus=" + contractItemsStatus + ", contractItemsDetail=" + contractItemsDetail
        + ", contractBidsStatus=" + contractBidsStatus + ", contractBidsDetail=" + contractBidsDetail + ", facWarStatsStatus=" + facWarStatsStatus
        + ", facWarStatsDetail=" + facWarStatsDetail + ", facilitiesStatus=" + facilitiesStatus + ", facilitiesDetail=" + facilitiesDetail
        + ", industryJobsStatus=" + industryJobsStatus + ", industryJobsDetail=" + industryJobsDetail + ", industryJobsHistoryStatus="
        + industryJobsHistoryStatus + ", industryJobsHistoryDetail=" + industryJobsHistoryDetail + ", killlogStatus=" + killlogStatus + ", killlogDetail="
        + killlogDetail + ", marketOrdersStatus=" + marketOrdersStatus + ", marketOrdersDetail=" + marketOrdersDetail + ", memberMedalsStatus="
        + memberMedalsStatus + ", memberMedalsDetail=" + memberMedalsDetail + ", standingsStatus=" + standingsStatus + ", standingsDetail=" + standingsDetail
        + ", walletJournalStatus=" + walletJournalStatus + ", walletJournalDetail=" + walletJournalDetail + ", walletTransactionsStatus="
        + walletTransactionsStatus + ", walletTransactionsDetail=" + walletTransactionsDetail + ", memberSecurityStatus=" + memberSecurityStatus
        + ", memberSecurityDetail=" + memberSecurityDetail + ", containerLogStatus=" + containerLogStatus + ", containerLogDetail=" + containerLogDetail
        + ", memberSecurityLogStatus=" + memberSecurityLogStatus + ", memberSecurityLogDetail=" + memberSecurityLogDetail + ", memberTrackingStatus="
        + memberTrackingStatus + ", memberTrackingDetail=" + memberTrackingDetail + ", corpMedalsStatus=" + corpMedalsStatus + ", corpMedalsDetail="
        + corpMedalsDetail + ", outpostListStatus=" + outpostListStatus + ", outpostListDetail=" + outpostListDetail + ", outpostDetailStatus="
        + outpostDetailStatus + ", outpostDetailDetail=" + outpostDetailDetail + ", shareholderStatus=" + shareholderStatus + ", shareholderDetail="
        + shareholderDetail + ", starbaseListStatus=" + starbaseListStatus + ", starbaseListDetail=" + starbaseListDetail + ", starbaseDetailStatus="
        + starbaseDetailStatus + ", starbaseDetailDetail=" + starbaseDetailDetail + ", corpTitlesStatus=" + corpTitlesStatus + ", corpTitlesDetail="
        + corpTitlesDetail + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((accountBalanceDetail == null) ? 0 : accountBalanceDetail.hashCode());
    result = prime * result + ((accountBalanceStatus == null) ? 0 : accountBalanceStatus.hashCode());
    result = prime * result + ((assetListDetail == null) ? 0 : assetListDetail.hashCode());
    result = prime * result + ((assetListStatus == null) ? 0 : assetListStatus.hashCode());
    result = prime * result + ((blueprintsDetail == null) ? 0 : blueprintsDetail.hashCode());
    result = prime * result + ((blueprintsStatus == null) ? 0 : blueprintsStatus.hashCode());
    result = prime * result + ((bookmarksDetail == null) ? 0 : bookmarksDetail.hashCode());
    result = prime * result + ((bookmarksStatus == null) ? 0 : bookmarksStatus.hashCode());
    result = prime * result + ((contactListDetail == null) ? 0 : contactListDetail.hashCode());
    result = prime * result + ((contactListStatus == null) ? 0 : contactListStatus.hashCode());
    result = prime * result + ((containerLogDetail == null) ? 0 : containerLogDetail.hashCode());
    result = prime * result + ((containerLogStatus == null) ? 0 : containerLogStatus.hashCode());
    result = prime * result + ((contractBidsDetail == null) ? 0 : contractBidsDetail.hashCode());
    result = prime * result + ((contractBidsStatus == null) ? 0 : contractBidsStatus.hashCode());
    result = prime * result + ((contractItemsDetail == null) ? 0 : contractItemsDetail.hashCode());
    result = prime * result + ((contractItemsStatus == null) ? 0 : contractItemsStatus.hashCode());
    result = prime * result + ((contractsDetail == null) ? 0 : contractsDetail.hashCode());
    result = prime * result + ((contractsStatus == null) ? 0 : contractsStatus.hashCode());
    result = prime * result + ((corpMedalsDetail == null) ? 0 : corpMedalsDetail.hashCode());
    result = prime * result + ((corpMedalsStatus == null) ? 0 : corpMedalsStatus.hashCode());
    result = prime * result + ((corpTitlesDetail == null) ? 0 : corpTitlesDetail.hashCode());
    result = prime * result + ((corpTitlesStatus == null) ? 0 : corpTitlesStatus.hashCode());
    result = prime * result + ((corporationSheetDetail == null) ? 0 : corporationSheetDetail.hashCode());
    result = prime * result + ((corporationSheetStatus == null) ? 0 : corporationSheetStatus.hashCode());
    result = prime * result + ((customsOfficeDetail == null) ? 0 : customsOfficeDetail.hashCode());
    result = prime * result + ((customsOfficeStatus == null) ? 0 : customsOfficeStatus.hashCode());
    result = prime * result + ((facWarStatsDetail == null) ? 0 : facWarStatsDetail.hashCode());
    result = prime * result + ((facWarStatsStatus == null) ? 0 : facWarStatsStatus.hashCode());
    result = prime * result + ((facilitiesDetail == null) ? 0 : facilitiesDetail.hashCode());
    result = prime * result + ((facilitiesStatus == null) ? 0 : facilitiesStatus.hashCode());
    result = prime * result + ((industryJobsDetail == null) ? 0 : industryJobsDetail.hashCode());
    result = prime * result + ((industryJobsHistoryDetail == null) ? 0 : industryJobsHistoryDetail.hashCode());
    result = prime * result + ((industryJobsHistoryStatus == null) ? 0 : industryJobsHistoryStatus.hashCode());
    result = prime * result + ((industryJobsStatus == null) ? 0 : industryJobsStatus.hashCode());
    result = prime * result + ((killlogDetail == null) ? 0 : killlogDetail.hashCode());
    result = prime * result + ((killlogStatus == null) ? 0 : killlogStatus.hashCode());
    result = prime * result + ((marketOrdersDetail == null) ? 0 : marketOrdersDetail.hashCode());
    result = prime * result + ((marketOrdersStatus == null) ? 0 : marketOrdersStatus.hashCode());
    result = prime * result + ((memberMedalsDetail == null) ? 0 : memberMedalsDetail.hashCode());
    result = prime * result + ((memberMedalsStatus == null) ? 0 : memberMedalsStatus.hashCode());
    result = prime * result + ((memberSecurityDetail == null) ? 0 : memberSecurityDetail.hashCode());
    result = prime * result + ((memberSecurityLogDetail == null) ? 0 : memberSecurityLogDetail.hashCode());
    result = prime * result + ((memberSecurityLogStatus == null) ? 0 : memberSecurityLogStatus.hashCode());
    result = prime * result + ((memberSecurityStatus == null) ? 0 : memberSecurityStatus.hashCode());
    result = prime * result + ((memberTrackingDetail == null) ? 0 : memberTrackingDetail.hashCode());
    result = prime * result + ((memberTrackingStatus == null) ? 0 : memberTrackingStatus.hashCode());
    result = prime * result + ((outpostDetailDetail == null) ? 0 : outpostDetailDetail.hashCode());
    result = prime * result + ((outpostDetailStatus == null) ? 0 : outpostDetailStatus.hashCode());
    result = prime * result + ((outpostListDetail == null) ? 0 : outpostListDetail.hashCode());
    result = prime * result + ((outpostListStatus == null) ? 0 : outpostListStatus.hashCode());
    result = prime * result + ((shareholderDetail == null) ? 0 : shareholderDetail.hashCode());
    result = prime * result + ((shareholderStatus == null) ? 0 : shareholderStatus.hashCode());
    result = prime * result + ((standingsDetail == null) ? 0 : standingsDetail.hashCode());
    result = prime * result + ((standingsStatus == null) ? 0 : standingsStatus.hashCode());
    result = prime * result + ((starbaseDetailDetail == null) ? 0 : starbaseDetailDetail.hashCode());
    result = prime * result + ((starbaseDetailStatus == null) ? 0 : starbaseDetailStatus.hashCode());
    result = prime * result + ((starbaseListDetail == null) ? 0 : starbaseListDetail.hashCode());
    result = prime * result + ((starbaseListStatus == null) ? 0 : starbaseListStatus.hashCode());
    result = prime * result + ((walletJournalDetail == null) ? 0 : walletJournalDetail.hashCode());
    result = prime * result + ((walletJournalStatus == null) ? 0 : walletJournalStatus.hashCode());
    result = prime * result + ((walletTransactionsDetail == null) ? 0 : walletTransactionsDetail.hashCode());
    result = prime * result + ((walletTransactionsStatus == null) ? 0 : walletTransactionsStatus.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!super.equals(obj)) return false;
    if (getClass() != obj.getClass()) return false;
    CorporationSyncTracker other = (CorporationSyncTracker) obj;
    if (accountBalanceDetail == null) {
      if (other.accountBalanceDetail != null) return false;
    } else if (!accountBalanceDetail.equals(other.accountBalanceDetail)) return false;
    if (accountBalanceStatus != other.accountBalanceStatus) return false;
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
    if (contactListDetail == null) {
      if (other.contactListDetail != null) return false;
    } else if (!contactListDetail.equals(other.contactListDetail)) return false;
    if (contactListStatus != other.contactListStatus) return false;
    if (containerLogDetail == null) {
      if (other.containerLogDetail != null) return false;
    } else if (!containerLogDetail.equals(other.containerLogDetail)) return false;
    if (containerLogStatus != other.containerLogStatus) return false;
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
    if (corpMedalsDetail == null) {
      if (other.corpMedalsDetail != null) return false;
    } else if (!corpMedalsDetail.equals(other.corpMedalsDetail)) return false;
    if (corpMedalsStatus != other.corpMedalsStatus) return false;
    if (corpTitlesDetail == null) {
      if (other.corpTitlesDetail != null) return false;
    } else if (!corpTitlesDetail.equals(other.corpTitlesDetail)) return false;
    if (corpTitlesStatus != other.corpTitlesStatus) return false;
    if (corporationSheetDetail == null) {
      if (other.corporationSheetDetail != null) return false;
    } else if (!corporationSheetDetail.equals(other.corporationSheetDetail)) return false;
    if (corporationSheetStatus != other.corporationSheetStatus) return false;
    if (customsOfficeDetail == null) {
      if (other.customsOfficeDetail != null) return false;
    } else if (!customsOfficeDetail.equals(other.customsOfficeDetail)) return false;
    if (customsOfficeStatus != other.customsOfficeStatus) return false;
    if (facWarStatsDetail == null) {
      if (other.facWarStatsDetail != null) return false;
    } else if (!facWarStatsDetail.equals(other.facWarStatsDetail)) return false;
    if (facWarStatsStatus != other.facWarStatsStatus) return false;
    if (facilitiesDetail == null) {
      if (other.facilitiesDetail != null) return false;
    } else if (!facilitiesDetail.equals(other.facilitiesDetail)) return false;
    if (facilitiesStatus != other.facilitiesStatus) return false;
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
    if (marketOrdersDetail == null) {
      if (other.marketOrdersDetail != null) return false;
    } else if (!marketOrdersDetail.equals(other.marketOrdersDetail)) return false;
    if (marketOrdersStatus != other.marketOrdersStatus) return false;
    if (memberMedalsDetail == null) {
      if (other.memberMedalsDetail != null) return false;
    } else if (!memberMedalsDetail.equals(other.memberMedalsDetail)) return false;
    if (memberMedalsStatus != other.memberMedalsStatus) return false;
    if (memberSecurityDetail == null) {
      if (other.memberSecurityDetail != null) return false;
    } else if (!memberSecurityDetail.equals(other.memberSecurityDetail)) return false;
    if (memberSecurityLogDetail == null) {
      if (other.memberSecurityLogDetail != null) return false;
    } else if (!memberSecurityLogDetail.equals(other.memberSecurityLogDetail)) return false;
    if (memberSecurityLogStatus != other.memberSecurityLogStatus) return false;
    if (memberSecurityStatus != other.memberSecurityStatus) return false;
    if (memberTrackingDetail == null) {
      if (other.memberTrackingDetail != null) return false;
    } else if (!memberTrackingDetail.equals(other.memberTrackingDetail)) return false;
    if (memberTrackingStatus != other.memberTrackingStatus) return false;
    if (outpostDetailDetail == null) {
      if (other.outpostDetailDetail != null) return false;
    } else if (!outpostDetailDetail.equals(other.outpostDetailDetail)) return false;
    if (outpostDetailStatus != other.outpostDetailStatus) return false;
    if (outpostListDetail == null) {
      if (other.outpostListDetail != null) return false;
    } else if (!outpostListDetail.equals(other.outpostListDetail)) return false;
    if (outpostListStatus != other.outpostListStatus) return false;
    if (shareholderDetail == null) {
      if (other.shareholderDetail != null) return false;
    } else if (!shareholderDetail.equals(other.shareholderDetail)) return false;
    if (shareholderStatus != other.shareholderStatus) return false;
    if (standingsDetail == null) {
      if (other.standingsDetail != null) return false;
    } else if (!standingsDetail.equals(other.standingsDetail)) return false;
    if (standingsStatus != other.standingsStatus) return false;
    if (starbaseDetailDetail == null) {
      if (other.starbaseDetailDetail != null) return false;
    } else if (!starbaseDetailDetail.equals(other.starbaseDetailDetail)) return false;
    if (starbaseDetailStatus != other.starbaseDetailStatus) return false;
    if (starbaseListDetail == null) {
      if (other.starbaseListDetail != null) return false;
    } else if (!starbaseListDetail.equals(other.starbaseListDetail)) return false;
    if (starbaseListStatus != other.starbaseListStatus) return false;
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

  public static CorporationSyncTracker createOrGetUnfinishedTracker(final SynchronizedEveAccount syncAccount) {
    try {
      return EveKitUserAccountProvider.getFactory().runTransaction(new RunInTransaction<CorporationSyncTracker>() {
        @Override
        public CorporationSyncTracker run() throws Exception {
          CorporationSyncTracker result = getUnfinishedTracker(syncAccount);
          if (result != null) return result;
          result = new CorporationSyncTracker();
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
  public static CorporationSyncTracker getUnfinishedTracker(final SynchronizedEveAccount syncAccount) {
    return SyncTracker.<CorporationSyncTracker> getUnfinishedTracker(syncAccount);
  }

  public static List<CorporationSyncTracker> getAllUnfinishedTrackers() {
    try {
      return EveKitUserAccountProvider.getFactory().runTransaction(new RunInTransaction<List<CorporationSyncTracker>>() {
        @Override
        public List<CorporationSyncTracker> run() throws Exception {
          TypedQuery<CorporationSyncTracker> getter = EveKitUserAccountProvider.getFactory().getEntityManager()
              .createNamedQuery("CorporationSyncTracker.getAllUnfinished", CorporationSyncTracker.class);
          return getter.getResultList();
        }
      });
    } catch (Exception e) {
      log.log(Level.SEVERE, "query error", e);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public static CorporationSyncTracker getLatestFinishedTracker(final SynchronizedEveAccount owner) {
    return SyncTracker.<CorporationSyncTracker> getLatestFinishedTracker(owner);
  }

  public static List<CorporationSyncTracker> getHistory(final SynchronizedEveAccount owner, final Long contid, final int maxResults) throws IOException {
    try {
      return EveKitUserAccountProvider.getFactory().runTransaction(new RunInTransaction<List<CorporationSyncTracker>>() {
        @Override
        public List<CorporationSyncTracker> run() throws Exception {
          TypedQuery<CorporationSyncTracker> getter = EveKitUserAccountProvider.getFactory().getEntityManager()
              .createNamedQuery("CorporationSyncTracker.getHistory", CorporationSyncTracker.class);
          getter.setParameter("account", owner);
          getter.setParameter("start", contid == null ? new Date(Long.MAX_VALUE) : new Date(contid));
          getter.setMaxResults(maxResults);
          return getter.getResultList();
        }
      });
    } catch (Exception e) {
      log.log(Level.SEVERE, "query error", e);
    }
    return null;
  }

  public static List<CorporationSyncTracker> getSummary(final Date fromDate) {
    try {
      return EveKitUserAccountProvider.getFactory().runTransaction(new RunInTransaction<List<CorporationSyncTracker>>() {
        @Override
        public List<CorporationSyncTracker> run() throws Exception {
          TypedQuery<CorporationSyncTracker> getter = EveKitUserAccountProvider.getFactory().getEntityManager()
              .createNamedQuery("CorporationSyncTracker.getSummary", CorporationSyncTracker.class);
          getter.setParameter("account", fromDate);
          return getter.getResultList();
        }
      });
    } catch (Exception e) {
      log.log(Level.SEVERE, "query error", e);
    }
    return null;
  }

  public static String summarizeErrors(Date day) throws IOException {
    StringBuilder summary = new StringBuilder();
    summary.append("Corporation Sync Tracker Error Summary on ");
    long days = day.getTime() / (1000 * 60 * 60 * 24);
    Date dayStart = new Date(days * 1000 * 60 * 60 * 24 + 1);
    Date nextDay = new Date(dayStart.getTime() + (1000 * 60 * 60 * 24) - 1);
    summary.append(DateFormat.getDateInstance().format(dayStart)).append('\n');
    List<CorporationSyncTracker> result = getSummary(dayStart);
    if (result == null) result = Collections.emptyList();

    // Process sync results with error.
    int errorCount = 0;
    Map<String, Map<String, AtomicInteger>> data = new HashMap<String, Map<String, AtomicInteger>>();
    for (CorporationSyncTracker next : result) {
      if (new Date(next.getSyncEnd()).after(nextDay)) continue;
      if (next.accountBalanceStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("accountBalance", next.accountBalanceDetail, data);
      } else if (next.assetListStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("assetList", next.assetListDetail, data);
      } else if (next.corporationSheetStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("corporationSheet", next.corporationSheetDetail, data);
      } else if (next.contactListStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("contactList", next.contactListDetail, data);
      } else if (next.customsOfficeStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("customsOffice", next.customsOfficeDetail, data);
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
      } else if (next.facilitiesStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("facilities", next.facilitiesDetail, data);
      } else if (next.industryJobsStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("industryJobs", next.industryJobsDetail, data);
      } else if (next.industryJobsHistoryStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("industryJobsHistory", next.industryJobsHistoryDetail, data);
      } else if (next.killlogStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("killlog", next.killlogDetail, data);
      } else if (next.marketOrdersStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("marketOrders", next.marketOrdersDetail, data);
      } else if (next.memberMedalsStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("memberMedals", next.memberMedalsDetail, data);
      } else if (next.standingsStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("standings", next.standingsDetail, data);
      } else if (next.walletJournalStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("walletJournal", next.walletJournalDetail, data);
      } else if (next.walletTransactionsStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("walletTransactions", next.walletTransactionsDetail, data);
      } else if (next.memberSecurityStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("memberSecurity", next.memberSecurityDetail, data);
      } else if (next.containerLogStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("containerLog", next.containerLogDetail, data);
      } else if (next.memberSecurityLogStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("memberSecurityLog", next.memberSecurityLogDetail, data);
      } else if (next.memberTrackingStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("memberTracking", next.memberTrackingDetail, data);
      } else if (next.corpMedalsStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("corpMedals", next.corpMedalsDetail, data);
      } else if (next.outpostListStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("outpostList", next.outpostListDetail, data);
      } else if (next.outpostDetailStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("outpostDetail", next.outpostDetailDetail, data);
      } else if (next.shareholderStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("shareholder", next.shareholderDetail, data);
      } else if (next.starbaseListStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("starbaseList", next.starbaseListDetail, data);
      } else if (next.starbaseDetailStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("starbaseDetail", next.starbaseDetailDetail, data);
      } else if (next.corpTitlesStatus == SyncState.SYNC_ERROR) {
        errorCount++;
        SyncTracker.incrementSummary("corpTitles", next.corpTitlesDetail, data);
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
