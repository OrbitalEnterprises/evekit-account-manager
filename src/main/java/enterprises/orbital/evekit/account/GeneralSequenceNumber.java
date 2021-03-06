package enterprises.orbital.evekit.account;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import enterprises.orbital.db.ConnectionFactory.RunInTransaction;

/**
 * This class is used to provide unique sequence values for non-ID columns in other tables. A one-to-one relation in the referring table is all that is needed
 * to use the value, then extraction via the getter to retrieve the value. Not great given the number of selects, but it's hard to avoid this currently with
 * Hibernate and still be general across many databases.
 */
@Entity
@Table(
    name = "evekit_sequence")
public class GeneralSequenceNumber {
  private static final Logger log = Logger.getLogger(GeneralSequenceNumber.class.getName());
  @Id
  @GeneratedValue(
      strategy = GenerationType.SEQUENCE,
      generator = "ek_seq")
  @SequenceGenerator(
      name = "ek_seq",
      initialValue = 100000,
      allocationSize = 10,
      sequenceName = "account_sequence")
  private long                value;

  public long getValue() {
    return value;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (value ^ (value >>> 32));
    return result;
  }

  @Override
  public boolean equals(
                        Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    GeneralSequenceNumber other = (GeneralSequenceNumber) obj;
    if (value != other.value) return false;
    return true;
  }

  public static GeneralSequenceNumber create() {
    try {
      return EveKitUserAccountProvider.getFactory().runTransaction(new RunInTransaction<GeneralSequenceNumber>() {
        @Override
        public GeneralSequenceNumber run() throws Exception {
          GeneralSequenceNumber result = new GeneralSequenceNumber();
          return EveKitUserAccountProvider.getFactory().getEntityManager().merge(result);
        }
      });
    } catch (Exception e) {
      log.log(Level.SEVERE, "query error", e);
    }
    return null;
  }
}
