package enterprises.orbital.evekit.model;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Model data attribute selector. Precedence works as follows:
 * <ol>
 * <li>If any == true, then all other values are ignored and this becomes a wildcard selector.
 * <li>If any == false and like != null, then all other values are ignored and this becomes a like selector.
 * <li>If any == false and like == null and !value.isEmpty(), then all other values are ignored and this becomes a set selector.
 * <li>If any == false and like == null and value.isEmpty() and start != null and end != null, then this becomes a range selector.
 * <li>Otherwise, this becomes a wildcard selector.
 * </ol>
 */
@SuppressWarnings("WeakerAccess")
public class AttributeSelector {
  public enum SelectorType {
    WILDCARD,
    SET,
    RANGE,
    LIKE,
    AT
  }

  public interface EnumMapper<A extends Enum<?>> {
    A mapEnumValue(
        String value);
  }

  /**
   * If true, allow any value for the attribute associated with this selector.
   */
  public boolean any;
  /**
   * If not-null, make this a string based "like" selector. Ignored for non-string attributes.
   */
  public String like;
  /**
   * If non-empty, only select elements in which the attribute has a value which is a member of the given set. Attributes are converted to the appropriate type
   * (an error is thrown if the conversion fails).
   */
  public Set<String> values = new HashSet<>();
  /**
   * The lower bound of a range selector.
   */
  public String start;
  /**
   * The upper bound of a range selector.
   */
  public String end;

  @SuppressWarnings("unused")
  private AttributeSelector() {}

  private static final AttributeSelector ANY_TRUE = new AttributeSelector("{ any: true }");

  public AttributeSelector(String json) {
    Gson gson = new Gson();
    AttributeSelector convert = gson.fromJson(json, AttributeSelector.class);
    copy(convert);
  }

  public void copy(
      AttributeSelector other) {
    this.any = other.any;
    this.like = other.like;
    this.values.addAll(other.values);
    this.start = other.start;
    this.end = other.end;
  }

  public SelectorType type() {
    if (any) return SelectorType.WILDCARD;
    if (like != null) return SelectorType.LIKE;
    if (!values.isEmpty()) return SelectorType.SET;
    if (start != null && end != null) return SelectorType.RANGE;
    return SelectorType.WILDCARD;
  }

  public String getLikeValue() {
    return like;
  }

  public Set<String> getStringValues() {
    return values;
  }

  public Set<Long> getLongValues() {
    Set<Long> result = new HashSet<>();
    for (String next : values) {
      result.add(Long.parseLong(next));
    }
    return result;
  }

  public Set<Integer> getIntValues() {
    Set<Integer> result = new HashSet<>();
    for (String next : values) {
      result.add(Integer.parseInt(next));
    }
    return result;
  }

  public Set<Double> getDoubleValues() {
    Set<Double> result = new HashSet<>();
    for (String next : values) {
      result.add(Double.parseDouble(next));
    }
    return result;
  }

  public Set<Float> getFloatValues() {
    Set<Float> result = new HashSet<>();
    for (String next : values) {
      result.add(Float.parseFloat(next));
    }
    return result;
  }

  public String getStringStart() {
    return start;
  }

  public String getStringEnd() {
    return end;
  }

  public long getLongStart() {
    return Long.parseLong(start);
  }

  public long getLongEnd() {
    return Long.parseLong(end);
  }

  public int getIntStart() {
    return Integer.parseInt(start);
  }

  public int getIntEnd() {
    return Integer.parseInt(end);
  }

  public double getDoubleStart() {
    return Double.parseDouble(start);
  }

  public double getDoubleEnd() {
    return Double.parseDouble(end);
  }

  public float getFloatStart() {
    return Float.parseFloat(start);
  }

  public float getFloatEnd() {
    return Float.parseFloat(end);
  }

  public static void addLifelineSelector(
      StringBuilder builder,
      String target,
      AttributeSelector at) {
    switch (at.type()) {
      case SET:
        // Return items which were live at the given selected points in time
        builder.append(" AND (");
        for (long l : at.getLongValues()) {
          builder.append("(")
                 .append(target)
                 .append(".lifeStart <= ")
                 .append(l)
                 .append(" AND ")
                 .append(target)
                 .append(".lifeEnd > ")
                 .append(l)
                 .append(") OR");
        }
        builder.setLength(builder.length() - 3);
        builder.append(")");
        break;
      case RANGE:
        // Fetch all values live in the given range
        // lifeStart <= max and lifeEnd > min
        long max = at.getLongEnd();
        long min = at.getLongStart();
        builder.append(" AND ")
               .append(target)
               .append(".lifeStart <= ")
               .append(max);
        builder.append(" AND ")
               .append(target)
               .append(".lifeEnd > ")
               .append(min);
        break;
      case WILDCARD:
      case LIKE:
      default:
        // No constraint, skip
        break;
    }
  }

  public static void addIntSelector(
      StringBuilder builder,
      String target,
      String column,
      AttributeSelector as) {
    String sel = target == null ? column : target + "." + column;
    switch (as.type()) {
      case SET:
        builder.append(" AND ")
               .append(sel)
               .append(" IN (");
        for (int l : as.getIntValues()) {
          builder.append(l)
                 .append(", ");
        }
        builder.setLength(builder.length() - 2);
        builder.append(")");
        break;
      case RANGE:
        builder.append(" AND ")
               .append(sel)
               .append(" BETWEEN ")
               .append(as.getIntStart())
               .append(" AND ")
               .append(as.getIntEnd());
        break;
      case WILDCARD:
      case LIKE:
      default:
        // No constraint, skip
        break;
    }
  }

  public static void addLongSelector(
      StringBuilder builder,
      String target,
      String column,
      AttributeSelector as) {
    String sel = target == null ? column : target + "." + column;
    switch (as.type()) {
      case SET:
        builder.append(" AND ")
               .append(sel)
               .append(" IN (");
        for (long l : as.getLongValues()) {
          builder.append(l)
                 .append(", ");
        }
        builder.setLength(builder.length() - 2);
        builder.append(")");
        break;
      case RANGE:
        builder.append(" AND ")
               .append(sel)
               .append(" BETWEEN ")
               .append(as.getLongStart())
               .append(" AND ")
               .append(as.getLongEnd());
        break;
      case WILDCARD:
      case LIKE:
      default:
        // No constraint, skip
        break;
    }
  }

  public static void addDoubleSelector(
      StringBuilder builder,
      String target,
      String column,
      AttributeSelector as) {
    String sel = target == null ? column : target + "." + column;
    switch (as.type()) {
      case SET:
        builder.append(" AND ")
               .append(sel)
               .append(" IN (");
        for (double l : as.getDoubleValues()) {
          builder.append(l)
                 .append(", ");
        }
        builder.setLength(builder.length() - 2);
        builder.append(")");
        break;
      case RANGE:
        builder.append(" AND ")
               .append(sel)
               .append(" BETWEEN ")
               .append(as.getDoubleStart())
               .append(" AND ")
               .append(as.getDoubleEnd());
        break;
      case WILDCARD:
      case LIKE:
      default:
        // No constraint, skip
        break;
    }
  }

  public static void addFloatSelector(
      StringBuilder builder,
      String target,
      String column,
      AttributeSelector as) {
    String sel = target == null ? column : target + "." + column;
    switch (as.type()) {
      case SET:
        builder.append(" AND ")
               .append(sel)
               .append(" IN (");
        for (float l : as.getFloatValues()) {
          builder.append(l)
                 .append(", ");
        }
        builder.setLength(builder.length() - 2);
        builder.append(")");
        break;
      case RANGE:
        builder.append(" AND ")
               .append(sel)
               .append(" BETWEEN ")
               .append(as.getFloatStart())
               .append(" AND ")
               .append(as.getFloatEnd());
        break;
      case WILDCARD:
      case LIKE:
      default:
        // No constraint, skip
        break;
    }
  }


  public static void addStringSelector(
      StringBuilder builder,
      String target,
      String column,
      AttributeSelector as,
      AttributeParameters p) {
    String sel = target == null ? column : target + "." + column;
    switch (as.type()) {
      case SET:
        builder.append(" AND ")
               .append(sel)
               .append(" IN (");
        for (String l : as.getStringValues()) {
          String next = ":" + p.addStringParam(l);
          builder.append(next)
                 .append(", ");
        }
        builder.setLength(builder.length() - 2);
        builder.append(")");
        break;
      case RANGE:
        String minParam = ":" + p.addStringParam(as.getStringStart());
        String maxParam = ":" + p.addStringParam(as.getStringEnd());
        builder.append(" AND ")
               .append(sel)
               .append(" BETWEEN ")
               .append(minParam)
               .append(" AND ")
               .append(maxParam);
        break;
      case LIKE:
        // Like clause. Attribute should contain any needed wildcards.
        String likeParam = ":" + p.addStringParam(as.getLikeValue());
        builder.append(" AND ")
               .append(sel)
               .append(" LIKE ")
               .append(likeParam);
        break;
      case WILDCARD:
      default:
        // No constraint, skip
        break;
    }
  }

  public static <A extends Enum<?>> void addEnumSelector(
      StringBuilder builder,
      String target,
      String column,
      AttributeSelector as,
      EnumMapper<A> mapper,
      AttributeParameters p) {
    String sel = target == null ? column : target + "." + column;
    switch (as.type()) {
      case SET:
        builder.append(" AND ")
               .append(sel)
               .append(" IN (");
        for (String l : as.getStringValues()) {
          String next = ":" + p.addEnumParam(mapper.mapEnumValue(l));
          builder.append(next)
                 .append(", ");
        }
        builder.setLength(builder.length() - 2);
        builder.append(")");
        break;
      case RANGE:
      case LIKE:
      case WILDCARD:
      default:
        // No constraint, skip
        break;
    }
  }

  public static void addBooleanSelector(
      StringBuilder builder,
      String target,
      String column,
      AttributeSelector as) {
    String sel = target == null ? column : target + "." + column;
    switch (as.type()) {
      case SET:
        // Only take the first value in the set (according to the iterator). This value determines the value for comparison.
        boolean value = Boolean.valueOf(as.getStringValues()
                                          .iterator()
                                          .next());
        builder.append(" AND ")
               .append(sel)
               .append(" = ")
               .append(value);
        break;
      case RANGE:
      case LIKE:
      case WILDCARD:
      default:
        // No constraint, skip
        break;
    }
  }

  @SuppressWarnings("Duplicates")
  public static void addSetLongSelector(
      StringBuilder builder,
      String target,
      String column,
      AttributeSelector as) {
    String sel = target == null ? column : target + "." + column;
    switch (as.type()) {
      case SET:
        // Check that at least one set member is a member of the long valued target collection
        builder.append(" AND SOME ELEMENTS(")
               .append(sel)
               .append(") IN (");
        for (long next : as.getLongValues()) {
          builder.append(next)
                 .append(", ");
        }
        builder.setLength(builder.length() - 2);
        builder.append(")");
        break;
      case RANGE:
        // Check that some of the elements of the set are within the range.
        long min = as.getLongStart();
        long max = as.getLongEnd();
        builder.append(" AND SOME ELEMENTS(")
               .append(sel)
               .append(") BETWEEN ")
               .append(min)
               .append(" AND ")
               .append(max);
        break;
      case LIKE:
      case WILDCARD:
      default:
        // No constraint, skip
        break;
    }
  }

  public static void addSetIntSelector(
      StringBuilder builder,
      String target,
      String column,
      AttributeSelector as) {
    String sel = target == null ? column : target + "." + column;
    switch (as.type()) {
      case SET:
        // Check that at least one set member is a member of the long valued target collection
        builder.append(" AND SOME ELEMENTS(")
               .append(sel)
               .append(") IN (");
        for (int next : as.getIntValues()) {
          builder.append(next)
                 .append(", ");
        }
        builder.setLength(builder.length() - 2);
        builder.append(")");
        break;
      case RANGE:
        // Check that some of the elements of the set are within the range.
        int min = as.getIntStart();
        int max = as.getIntEnd();
        builder.append(" AND SOME ELEMENTS(")
               .append(sel)
               .append(") BETWEEN ")
               .append(min)
               .append(" AND ")
               .append(max);
        break;
      case LIKE:
      case WILDCARD:
      default:
        // No constraint, skip
        break;
    }
  }

  public static void addSetStringSelector(
      StringBuilder builder,
      String target,
      String column,
      AttributeSelector as,
      AttributeParameters p) {
    String sel = target == null ? column : target + "." + column;
    switch (as.type()) {
      case SET:
        // Check that at least one set member is a member of the long valued target collection
        builder.append(" AND SOME ELEMENTS(")
               .append(sel)
               .append(") IN (");
        for (String l : as.getStringValues()) {
          String next = ":" + p.addStringParam(l);
          builder.append(next)
                 .append(", ");
        }
        builder.setLength(builder.length() - 2);
        builder.append(")");
        break;
      case RANGE:
        // Check that some of the elements of the set are within the range.
        String minParam = ":" + p.addStringParam(as.getStringStart());
        String maxParam = ":" + p.addStringParam(as.getStringEnd());
        builder.append(" AND SOME ELEMENTS(")
               .append(sel)
               .append(") BETWEEN ")
               .append(minParam)
               .append(" AND ")
               .append(maxParam);
        break;
      case LIKE:
        // Like clause. Attribute should contain any needed wildcards.
        String likeParam = ":" + p.addStringParam(as.getLikeValue());
        builder.append(" AND SOME ELEMENTS(")
               .append(sel)
               .append(") LIKE ")
               .append(likeParam);
        break;
      case WILDCARD:
      default:
        // No constraint, skip
        break;
    }
  }

  ///////////////////////////////////////
  // Convenience methods
  ///////////////////////////////////////

  public static AttributeSelector any() {
    return ANY_TRUE;
  }

  public static AttributeSelector values(Object... vals) {
    AttributeSelector sel = new AttributeSelector();
    Arrays.stream(vals).forEach(x -> sel.values.add(String.valueOf(x)));
    return sel;
  }

  public static AttributeSelector like(Object val) {
    AttributeSelector sel = new AttributeSelector();
    sel.like = String.valueOf(val);
    return sel;
  }

  public static AttributeSelector range(Object start, Object end) {
    AttributeSelector sel = new AttributeSelector();
    sel.start = String.valueOf(start);
    sel.end = String.valueOf(end);
    return sel;
  }

}
