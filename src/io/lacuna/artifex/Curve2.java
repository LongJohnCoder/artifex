package io.lacuna.artifex;

import io.lacuna.artifex.utils.Intersections;
import io.lacuna.artifex.utils.Scalars;

import java.util.List;

import static io.lacuna.artifex.Box.box;
import static io.lacuna.artifex.utils.Scalars.EPSILON;

/**
 * @author ztellman
 */
public interface Curve2 {

  /**
   * @param t a value within [0,1]
   * @return the interpolated position on the curve
   */
  Vec2 position(double t);

  default Vec2 start() {
    return position(0);
  }

  default Vec2 end() {
    return position(1);
  }

  /**
   * @return an updated curve with the start point as {@code pos}
   */
  default Curve2 start(Vec2 pos) {
    return reverse().end(pos).reverse();
  }

  /**
   * @return an updated curve with the end point at {@code pos}
   */
  Curve2 end(Vec2 pos);

  /**
   * @param t a value within [0,1]
   * @return the tangent at the interpolated position on the curve, which is not normalized
   */
  Vec2 direction(double t);

  /**
   * @param t a value within [0,1]
   * @return an array representing the lower and upper regions of the curve, split at {@code t}
   */
  Curve2[] split(double t);

  default Curve2[] split(double[] ts) {
    Curve2[] result = new Curve2[ts.length + 1];
    Curve2 c = this;

    double scale = 1;
    for (int i = 0; i < ts.length; i++) {
      double p = ts[i];
      Curve2[] parts = c.split(p * scale);
      result[i] = parts[0];
      c = parts[1];
      scale /= p;
    }
    result[result.length - 1] = c;

    return result;
  }

  /**
   * @param p a point in 2D space
   * @return the {@code t} parameter representing the closest point on the curve, not necessarily within [0,1].  If
   * outside that range, it indicates the distance from the respective endpoints.
   */
  double nearestPoint(Vec2 p);

  default Box2 bounds() {
    Box2 bounds = box(start(), end());
    for (double t : inflections()) {
      bounds = bounds.union(position(t));
    }
    return bounds;
  }

  Vec2[] subdivide(double error);

  Curve2 transform(Matrix3 m);

  Curve2 reverse();

  double[] inflections();

  default boolean collinear(Curve2 c) {
    double[] ts = intersections(c);
    Vec2 u = direction(ts[0]).norm();
    Vec2 v = c.direction(ts[1]).norm();
    return Vec.equals(u, v, EPSILON) || Vec.equals(u, v.negate(), EPSILON);
  }

  default boolean intersects(Curve2 c) {
    return intersections(c).length > 0;
  }

  default double[] intersections(Curve2 c, double epsilon) {
    return Intersections.intersections(this, c, epsilon);
  }

  default double[] intersections(Curve2 c) {
    return intersections(c, EPSILON);
  }
}
