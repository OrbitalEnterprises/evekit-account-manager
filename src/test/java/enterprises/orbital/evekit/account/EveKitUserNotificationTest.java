package enterprises.orbital.evekit.account;

import enterprises.orbital.base.OrbitalProperties;
import enterprises.orbital.evekit.TestBase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EveKitUserNotificationTest extends TestBase {
  private long testTime = 1234L;
  private EveKitUserAccount userAccount;

  @Before
  public void setupAccount() throws IOException {
    userAccount = EveKitUserAccount.createNewUserAccount(true, true);
  }

  @After
  public void cleanup() {
    OrbitalProperties.setTimeGenerator(null);
  }

  @Test
  public void testMakeNote() throws IOException {
    OrbitalProperties.setTimeGenerator(() -> testTime);
    String testContent = "test content";
    EveKitUserNotification testNote = EveKitUserNotification.makeNote(userAccount, testContent);
    Assert.assertEquals(userAccount, testNote.getAccount());
    Assert.assertEquals(testTime, testNote.getNoteTime());
    Assert.assertEquals(testContent, testNote.getContent());
    Assert.assertEquals(0L, testNote.getReadTime());
    Assert.assertFalse(testNote.isTrash());
  }

  @Test
  public void testGetNote() throws IOException {
    OrbitalProperties.setTimeGenerator(() -> testTime);
    String testContent = "test content";
    EveKitUserNotification testNote = EveKitUserNotification.makeNote(userAccount, testContent);
    EveKitUserNotification otherNote = EveKitUserNotification.makeNote(userAccount, testContent + "A");
    EveKitUserNotification out = EveKitUserNotification.getNote(userAccount, testNote.getNid());
    Assert.assertEquals(testNote, out);
    Assert.assertNotEquals(otherNote, out);
    EveKitUserNotification missing = EveKitUserNotification.getNote(userAccount,
                                                                    testNote.getNid() + otherNote.getNid());
    Assert.assertNull(missing);
  }

  @Test
  public void testGetAllNotes() throws IOException {
    List<EveKitUserNotification> liveNotes = new ArrayList<>();
    int liveNoteCount = TestBase.getRandomInt(5) + 5;
    int deadNoteCount = TestBase.getRandomInt(5) + 5;
    for (int i = 0; i < liveNoteCount; i++) {
      EveKitUserNotification next = EveKitUserNotification.makeNote(userAccount, TestBase.getRandomText(50));
      liveNotes.add(next);
    }
    for (int i = 0; i < deadNoteCount; i++) {
      EveKitUserNotification next = EveKitUserNotification.makeNote(userAccount, TestBase.getRandomText(50));
      EveKitUserNotification.markNoteDeleted(userAccount, next.getNid());
    }
    List<EveKitUserNotification> noteList = EveKitUserNotification.getAllNotes(userAccount);
    Assert.assertEquals(liveNoteCount, noteList.size());
    for (int i = 0; i < liveNoteCount; i++) {
      EveKitUserNotification original = liveNotes.get(i);
      EveKitUserNotification stored = noteList.get(i);
      Assert.assertEquals(original, stored);
    }
  }

  @Test
  public void testMarkDeleted() throws IOException {
    EveKitUserNotification testNote = EveKitUserNotification.makeNote(userAccount, TestBase.getRandomText(50));
    EveKitUserNotification deletedNote = EveKitUserNotification.markNoteDeleted(userAccount, testNote.getNid());
    Assert.assertEquals(testNote.getAccount(), deletedNote.getAccount());
    Assert.assertEquals(testNote.getNoteTime(), deletedNote.getNoteTime());
    Assert.assertEquals(testNote.getContent(), deletedNote.getContent());
    Assert.assertEquals(testNote.getReadTime(), deletedNote.getReadTime());
    Assert.assertFalse(testNote.isTrash());
    Assert.assertTrue(deletedNote.isTrash());
  }

  @Test
  public void testMarkRead() throws IOException {
    EveKitUserNotification testNote = EveKitUserNotification.makeNote(userAccount, TestBase.getRandomText(50));
    OrbitalProperties.setTimeGenerator(() -> testTime);
    EveKitUserNotification readNote = EveKitUserNotification.markNoteRead(userAccount, testNote.getNid());
    Assert.assertEquals(testNote.getAccount(), readNote.getAccount());
    Assert.assertEquals(testNote.getNoteTime(), readNote.getNoteTime());
    Assert.assertEquals(testNote.getContent(), readNote.getContent());
    Assert.assertEquals(testNote.isTrash(), readNote.isTrash());
    Assert.assertEquals(0L, testNote.getReadTime());
    Assert.assertEquals(testTime, readNote.getReadTime());
  }

}
