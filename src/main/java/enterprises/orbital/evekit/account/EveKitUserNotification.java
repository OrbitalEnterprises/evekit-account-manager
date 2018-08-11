package enterprises.orbital.evekit.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import enterprises.orbital.base.OrbitalProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User notification.  Used to convey a short message which can be displayed in various
 * UIs.  Can also be retrieved from the backend for authenticated clients.
 */
@Entity
@Table(
    name = "evekit_user_note",
    indexes = {
        @Index(
            name = "accountIndex",
            columnList = "uid"),
        @Index(
            name = "trashIndex",
            columnList = "trash")
    })
@NamedQueries({
    @NamedQuery(
        name = "EveKitUserNotification.findByAcctAndID",
        query = "SELECT c FROM EveKitUserNotification c where c.account = :account and c.nid = :nid"),
    @NamedQuery(
        name = "EveKitUserNotification.allByAcct",
        query = "SELECT c FROM EveKitUserNotification c where c.account = :account and c.trash = false order by c.noteTime asc"),
})
@ApiModel(
    description = "User notification")
public class EveKitUserNotification {
  private static final Logger log = Logger.getLogger(EveKitUserNotification.class.getName());

  // Unique notification ID ID
  @Id
  @GeneratedValue(
      strategy = GenerationType.SEQUENCE,
      generator = "ek_seq")
  @SequenceGenerator(
      name = "ek_seq",
      initialValue = 100000,
      allocationSize = 10,
      sequenceName = "account_sequence")
  @ApiModelProperty(
      value = "Unique notification ID")
  @JsonProperty("nid")
  protected long nid;

  // Owner to which this note is attached
  @ManyToOne
  @JoinColumn(
      name = "uid",
      referencedColumnName = "uid")
  @JsonProperty("account")
  private EveKitUserAccount account;

  // DateTime when note created (ms UTC)
  @ApiModelProperty(
      value = "Note creation time")
  @JsonProperty("time")
  private long noteTime;

  // Note content (HTML)
  @ApiModelProperty(
      value = "Note content")
  @JsonProperty("content")
  @Lob
  @Column(
      length = 102400)
  private String content;

  // DateTime when note has been read (or <= 0 if not read yet) (ms UTC)
  @ApiModelProperty(
      value = "Note read time")
  @JsonProperty("read")
  private long readTime = 0;

  // True if note has been deleted, false otherwise
  @ApiModelProperty(
      value = "Note deleted")
  @JsonProperty("trash")
  private boolean trash = false;

  protected EveKitUserNotification() {}

  public EveKitUserNotification(EveKitUserAccount account, long noteTime, String content) {
    this.account = account;
    this.noteTime = noteTime;
    this.content = content;
  }

  public long getNid() {
    return nid;
  }

  public EveKitUserAccount getAccount() {
    return account;
  }

  public long getNoteTime() {
    return noteTime;
  }

  public String getContent() {
    return content;
  }

  public long getReadTime() {
    return readTime;
  }

  public boolean isTrash() {
    return trash;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    EveKitUserNotification that = (EveKitUserNotification) o;
    return nid == that.nid &&
        noteTime == that.noteTime &&
        readTime == that.readTime &&
        trash == that.trash &&
        Objects.equals(account, that.account) &&
        Objects.equals(content, that.content);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nid, account, noteTime, content, readTime, trash);
  }

  @Override
  public String toString() {
    return "EveKitUserNotification{" +
        "nid=" + nid +
        ", account=" + account +
        ", noteTime=" + noteTime +
        ", content='" + content + '\'' +
        ", readTime=" + readTime +
        ", trash=" + trash +
        '}';
  }

  public static EveKitUserNotification makeNote(final EveKitUserAccount acct,
                                                final String content) throws IOException {
    try {
      return EveKitUserAccountProvider.getFactory()
                                      .runTransaction(() -> {
                                        EveKitUserNotification newNote = new EveKitUserNotification(acct,
                                                                                                    OrbitalProperties.getCurrentTime(),
                                                                                                    content);
                                        return EveKitUserAccountProvider.getFactory()
                                                                        .getEntityManager()
                                                                        .merge(newNote);
                                      });
    } catch (Exception e) {
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }


  public static EveKitUserNotification getNote(final EveKitUserAccount acct, final long noteID) throws IOException {
    try {
      return EveKitUserAccountProvider.getFactory()
                                      .runTransaction(() -> {
                                        TypedQuery<EveKitUserNotification> getter = EveKitUserAccountProvider.getFactory()
                                                                                                             .getEntityManager()
                                                                                                             .createNamedQuery(
                                                                                                                 "EveKitUserNotification.findByAcctAndID",
                                                                                                                 EveKitUserNotification.class);
                                        getter.setParameter("account", acct);
                                        getter.setParameter("nid", noteID);
                                        try {
                                          return getter.getSingleResult();
                                        } catch (NoResultException e) {
                                          return null;
                                        }
                                      });
    } catch (Exception e) {
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  public static List<EveKitUserNotification> getAllNotes(final EveKitUserAccount acct) throws IOException {
    try {
      return EveKitUserAccountProvider.getFactory()
                                      .runTransaction(() -> {
                                        TypedQuery<EveKitUserNotification> getter = EveKitUserAccountProvider.getFactory()
                                                                                                             .getEntityManager()
                                                                                                             .createNamedQuery(
                                                                                                                 "EveKitUserNotification.allByAcct",
                                                                                                                 EveKitUserNotification.class);
                                        getter.setParameter("account", acct);
                                        return getter.getResultList();
                                      });
    } catch (Exception e) {
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  public static EveKitUserNotification markNoteDeleted(final EveKitUserAccount acct,
                                                       final long nid) throws IOException {
    try {
      return EveKitUserAccountProvider.getFactory()
                                      .runTransaction(() -> {
                                        EveKitUserNotification target = getNote(acct, nid);
                                        if (target == null) {
                                          throw new IOException("Target note does not exist");
                                        }
                                        target.trash = true;
                                        return EveKitUserAccountProvider.getFactory()
                                                                        .getEntityManager()
                                                                        .merge(target);
                                      });
    } catch (Exception e) {
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

  public static EveKitUserNotification markNoteRead(final EveKitUserAccount acct, final long nid) throws IOException {
    try {
      return EveKitUserAccountProvider.getFactory()
                                      .runTransaction(() -> {
                                        EveKitUserNotification target = getNote(acct, nid);
                                        if (target == null) {
                                          throw new IOException("Target note does not exist");
                                        }
                                        target.readTime = OrbitalProperties.getCurrentTime();
                                        return EveKitUserAccountProvider.getFactory()
                                                                        .getEntityManager()
                                                                        .merge(target);
                                      });
    } catch (Exception e) {
      if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
      log.log(Level.SEVERE, "query error", e);
      throw new IOException(e.getCause());
    }
  }

}
