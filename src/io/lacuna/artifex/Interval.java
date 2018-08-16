package io.lacuna.artifex;

import io.lacuna.artifex.utils.Hashes;
import io.lacuna.artifex.utils.Scalars;

import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;
import static java.lang.Math.PI;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * @author ztellman
 */
public class Interval {

  private static final Interval EMPTY = new Interval(Double.NaN, Double.NaN);

  public final double lo, hi;

  public Interval(double a, double b) {
    if (a < b) {
      this.lo = a;
      this.hi = b;
    } else {
      this.lo = b;
      this.hi = a;
    }
  }

  public static Interval empty() {
    return EMPTY;
  }

  /// predicates

  public boolean intersects(Interval i) {
    return hi > i.lo && i.hi > lo;
  }

  public boolean contains(double n) {
    return !isEmpty() && lo <= n && n <= hi;
  }

  public boolean contains(Interval i) {
    return !isEmpty() && contains(i.lo) && contains(i.hi);
  }

  public boolean isEmpty() {
    return this == EMPTY;
  }

  ///

  public Interval map(DoubleUnaryOperator f) {
    return new Interval(f.applyAsDouble(lo), f.applyAsDouble(hi));
  }

  public Interval add(Interval i) {
    return isEmpty() || i.isEmpty() ? empty() : new Interval(lo + i.lo, hi + i.hi);
  }

  public Interval sub(Interval i) {
    return isEmpty() || i.isEmpty() ? empty() : new Interval(lo - i.lo, hi - i.hi);
  }

  public Interval mul(Interval i) {
    return isEmpty() || i.isEmpty()
      ? empty()
      : new Interval(lo * hi, i.lo * i.hi).union(new Interval(lo * i.hi, i.lo * hi));
  }

  public Interval div(Interval i) {
    if (i.lo == 0 && i.hi == 0) {
      return new Interval(NEGATIVE_INFINITY, POSITIVE_INFINITY);
    } else if (i.lo == 0) {
      return mul(new Interval(1 / i.hi, POSITIVE_INFINITY));
    } else if (i.hi == 0) {
      return mul(new Interval(NEGATIVE_INFINITY, 1 / i.lo));
    } else {
      return mul(new Interval(1 / i.hi, 1 / i.lo));
    }
  }

  ///

  public Interval union(Interval i) {
    return new Interval(min(lo, i.lo), max(hi, i.hi));
  }

  public Interval intersection(Interval i) {
    if (isEmpty() || i.isEmpty() || !intersects(i)) {
      return empty();
    }

    return new Interval(max(lo, i.lo), min(hi, i.hi));
  }

  public double normalize(double n) {
    return (n - lo) / size();
  }

  public Interval normalize(Interval i) {
    return new Interval(normalize(i.lo), normalize(i.hi));
  }

  public double lerp(double t) {
    return Scalars.lerp(lo, hi, t);
  }

  public Interval lerp(Interval i) {
    return new Interval(lerp(i.lo), lerp(i.hi));
  }

  public double size() {
    return hi - lo;
  }

  ///


  @Override
  public int hashCode() {
    return Hashes.hash(lo, hi);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (obj instanceof Interval) {
      Interval o = (Interval) obj;
      return lo == o.lo && hi == o.hi;
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return "[" + lo + ", " + hi + "]";
  }
}
