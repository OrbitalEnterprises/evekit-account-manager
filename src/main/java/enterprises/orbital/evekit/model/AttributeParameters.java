package enterprises.orbital.evekit.model;

import javax.persistence.TypedQuery;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class AttributeParameters {
  private String               prefix;
  private int                  count         = 0;
  private Map<String, String>  stringParams  = new HashMap<String, String>();
  private Map<String, Integer> integerParams = new HashMap<String, Integer>();
  private Map<String, Long>    longParams    = new HashMap<String, Long>();
  private Map<String, Enum<?>> enumParams    = new HashMap<String, Enum<?>>();

  public AttributeParameters(String prefix) {
    this.prefix = prefix;
  }

  protected String getNextParam() {
    String next = prefix + count;
    count++;
    return next;
  }

  public String addStringParam(
                               String value) {
    String next = getNextParam();
    stringParams.put(next, value);
    return next;
  };

  public String addEnumParam(
                             Enum<?> value) {
    String next = getNextParam();
    enumParams.put(next, value);
    return next;
  };

  public String addIntegerParam(
                                int value) {
    String next = getNextParam();
    integerParams.put(next, value);
    return next;
  }

  public String addLongParam(
                             long value) {
    String next = getNextParam();
    longParams.put(next, value);
    return next;
  }

  public void fillParams(
                         TypedQuery<?> query) {
    for (Entry<String, String> e : stringParams.entrySet()) {
      query.setParameter(e.getKey(), e.getValue());
    }
    for (Entry<String, Integer> e : integerParams.entrySet()) {
      query.setParameter(e.getKey(), e.getValue());
    }
    for (Entry<String, Long> e : longParams.entrySet()) {
      query.setParameter(e.getKey(), e.getValue());
    }
    for (Entry<String, Long> e : longParams.entrySet()) {
      query.setParameter(e.getKey(), e.getValue());
    }
    for (Entry<String, Enum<?>> e : enumParams.entrySet()) {
      query.setParameter(e.getKey(), e.getValue());
    }
  }
}
