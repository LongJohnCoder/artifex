package io.lacuna.artifex.utils.regions;

import io.lacuna.artifex.Curve2;
import io.lacuna.artifex.LineSegment2;
import io.lacuna.artifex.Vec2;
import io.lacuna.artifex.utils.EdgeList;
import io.lacuna.artifex.utils.EdgeList.HalfEdge;
import io.lacuna.artifex.utils.SweepQueue;
import io.lacuna.bifurcan.IMap;
import io.lacuna.bifurcan.LinearMap;

import static io.lacuna.artifex.Vec2.angleBetween;
import static io.lacuna.artifex.Vec2.cross;

/**
 * Wraps triangular hulls around all parametric curves, per https://developer.nvidia.com/gpugems/GPUGems3/gpugems3_ch25.html
 * and the associated 2005 paper.
 *
 * @author ztellman
 */
public class Hulls {

  public static final int OUTSIDE = 0, INSIDE = 1, HULL = 2;

  public static boolean isConvex(Curve2 c) {
    return -angleBetween(c.direction(0).negate(), c.direction(1)) < Math.PI;
  }

  public static Vec2 tangentIntersection(Curve2 c) {
    Vec2 pv = c.direction(0);
    Vec2 qv = c.direction(1).negate();

    double d = cross(pv, qv);
    if (d == 0) {
      throw new IllegalStateException();
    }

    double s = cross(qv, c.start().sub(c.end())) / d;
    return c.start().add(pv.mul(s));
  }

  public static double hullArea(Curve2 curve) {
    if (curve instanceof LineSegment2) {
      return 0;
    }

    Vec2 a = curve.start();
    Vec2 b = curve.end();
    Vec2 c = tangentIntersection(curve);
    return Math.abs(((a.x - c.x) * (b.y - a.y)) - ((a.x - b.x) * (c.y - a.y))) / 2;
  }

  public static LineSegment2 intersector(Curve2 c) {
    if (c instanceof LineSegment2) {
      return (LineSegment2) c;
    } else if (isConvex(c)) {
      return LineSegment2.from(c.start(), c.end());
    } else {
      return LineSegment2.from(c.end(), tangentIntersection(c));
    }
  }

  public static void add(SweepQueue<HalfEdge> queue, IMap<HalfEdge, LineSegment2> intersectors, HalfEdge e) {
    LineSegment2 l = intersector(e.curve);
    intersectors.put(e, l);
    queue.add(e, l.start().x, l.end().x);
  }

  public static void create(EdgeList edges) {
    IMap<HalfEdge, LineSegment2> intersectors = new LinearMap<>();
    SweepQueue<HalfEdge> queue = new SweepQueue<>();
    for (Vec2 v : edges.vertices()) {
      add(queue, intersectors, edges.edge(v, INSIDE));
    }

    HalfEdge curr = queue.take();
    while (curr != null) {
      LineSegment2 a = intersectors.get(curr, null);
      if (a != null) {
        for (HalfEdge e : queue.active()) {
          if (e == curr || e == curr.next || e == curr.prev) {
            continue;
          }

          LineSegment2 b = intersectors.get(e, null);
          if (b != null && a.intersections(b).length > 0) {
            HalfEdge toSplit = hullArea(e.curve) < hullArea(curr.curve) ? curr : e;
            HalfEdge split = edges.split(toSplit, 0.5);

            intersectors.remove(toSplit);
            add(queue, intersectors, split);
            add(queue, intersectors, split.prev);

            if (toSplit == curr) {
              break;
            }
          }
        }
      }

      curr = queue.take();
    }

    for (HalfEdge e : intersectors.keys()) {
      Curve2 c = e.curve;
      if (c instanceof LineSegment2) {
        continue;
      }

      e.flag = HULL;
      if (isConvex(c)) {
        edges.add(c.end(), c.start(), HULL, INSIDE);
      } else {
        Vec2 v = tangentIntersection(c);
        edges.add(c.end(), v, HULL, INSIDE);
        edges.add(v, c.start(), HULL, INSIDE);
      }
    }
  }
}
