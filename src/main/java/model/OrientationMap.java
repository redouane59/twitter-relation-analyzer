package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class OrientationMap extends LinkedHashMap<OrientationEnum, Integer> {


  @Override
  public String toString() {
    this.sort();
    StringBuilder result = new StringBuilder();
    double        sum    = this.values().stream().reduce(0, Integer::sum);
    for (OrientationEnum orientationEnum : this.keySet()) {
      long percentValue = Math.round((double) 100 * this.get(orientationEnum) / sum);
      if (percentValue > 1 && orientationEnum != OrientationEnum.UNKNOWN) {
        result.append(orientationEnum.getLabel()).append("-> ").append(percentValue).append("%");
        result.append("\n");
      }
    }
    return result.toString();
  }

  private void sort() {

    List<Entry<OrientationEnum, Integer>> list = new ArrayList<>(this.entrySet());
    // Comparing entries
    Collections.sort(list, (entry1, entry2) -> entry2.getValue()
                                               - entry1.getValue());
    this.clear();
    for (Map.Entry<OrientationEnum, Integer> entry : list) {
      this.put(entry.getKey(), entry.getValue());
    }

  }
}