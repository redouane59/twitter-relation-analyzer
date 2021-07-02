package model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserStatistics {

  private double followersCountAverage;
  private double followingsCountAverage;
  private int    followersCountMedian;
  private int    followingsCountMedian;

  public double getMedianRatio() {
    if (followersCountMedian > 0 && followingsCountMedian > 0) {
      return (double) followersCountMedian / followingsCountMedian;
    }
    return -1;
  }

  public double getAverageRatio() {
    if (followersCountAverage > 0 && followingsCountAverage > 0) {
      return (double) followersCountAverage / followingsCountAverage;
    }
    return -1;
  }
}
