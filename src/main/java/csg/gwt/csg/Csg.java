//
//  Csg.java
//  javacsg
//
//  Created by William Shakour (billy1380) on 6 Dec 2014.
//  Copyright © 2014 SPACEHOPPER STUDIOS Ltd. All rights reserved.
//
package csg.gwt.csg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Constructive Solid Geometry (CSG) is a modeling technique that uses Boolean
//operations like union and intersection to combine 3D solids. This library
//implements CSG operations on meshes elegantly and concisely using BSP trees,
//and is meant to serve as an easily understandable implementation of the
//algorithm. All edge cases involving overlapping coplanar polygons in both
//solids are correctly handled.
//
//Example usage:
//
//  var cube = CSG.cube();
//  var sphere = CSG.sphere({ radius: 1.3 });
//  var polygons = cube.subtract(sphere).toPolygons();
//
//## Implementation Details
//
//All CSG operations are implemented in terms of two functions, `clipTo()` and
//`invert()`, which remove parts of a BSP tree inside another BSP tree and swap
//solid and empty space, respectively. To find the union of `a` and `b`, we
//want to remove everything in `a` inside `b` and everything in `b` inside `a`,
//then combine polygons from `a` and `b` into one solid:
//
//  a.clipTo(b);
//  b.clipTo(a);
//  a.build(b.allPolygons());
//
//The only tricky part is handling overlapping coplanar polygons in both trees.
//The code above keeps both copies, but we need to keep them in one tree and
//remove them in the other tree. To remove them from `b` we can clip the
//inverse of `b` against `a`. The code for union now looks like this:
//
//  a.clipTo(b);
//  b.clipTo(a);
//  b.invert();
//  b.clipTo(a);
//  b.invert();
//  a.build(b.allPolygons());
//
//Subtraction and intersection naturally follow from set operations. If
//union is `A | B`, subtraction is `A - B = ~(~A | B)` and intersection is
//`A & B = ~(~A | ~B)` where `~` is the complement operator.
//
//## License
//
//Copyright (c) 2011 Evan Wallace (http://madebyevan.com/), under the MIT license.

//Holds a binary space partition tree representing a 3D solid. Two solids can
//be combined using the `union()`, `subtract()`, and `intersect()` methods.
class Csg {
	private List<Polygon> polygons = new ArrayList<Polygon>();

	// Construct a CSG solid from a list of `CSG.Polygon` instances.
	static Csg fromPolygons(List<Polygon> polygons) {
		Csg csg = new Csg();
		csg.polygons = polygons;
		return csg;
	}

	public Csg clone() {
		Csg csg = new Csg();

		for (Polygon p : this.polygons) {
			csg.polygons.add(p.clone());
		}

		return csg;
	}

	public List<Polygon> toPolygons() {
		return this.polygons;
	}

	//Return a new CSG solid representing space in either this solid or in the
	// solid `csg`. Neither this solid nor the solid `csg` are modified.
	// 
	//     A.union(B)
	// 
	//     +-------+            +-------+
	//     |       |            |       |
	//     |   A   |            |       |
	//     |    +--+----+   =   |       +----+
	//     +----+--+    |       +----+       |
	//          |   B   |            |       |
	//          |       |            |       |
	//          +-------+            +-------+
	// 
	public Csg union(Csg csg) {
		Node a = new Node(this.clone().polygons);
		Node b = new Node(csg.clone().polygons);
		a.clipTo(b);
		b.clipTo(a);
		b.invert();
		b.clipTo(a);
		b.invert();
		a.build(b.allPolygons());
		return Csg.fromPolygons(a.allPolygons());
	}

	//Return a new CSG solid representing space in this solid but not in the
	// solid `csg`. Neither this solid nor the solid `csg` are modified.
	// 
	//     A.subtract(B)
	// 
	//     +-------+            +-------+
	//     |       |            |       |
	//     |   A   |            |       |
	//     |    +--+----+   =   |    +--+
	//     +----+--+    |       +----+
	//          |   B   |
	//          |       |
	//          +-------+
	// 
	public Csg subtract(Csg csg) {
		Node a = new Node(this.clone().polygons);
		Node b = new Node(csg.clone().polygons);
		a.invert();
		a.clipTo(b);
		b.clipTo(a);
		b.invert();
		b.clipTo(a);
		b.invert();
		a.build(b.allPolygons());
		a.invert();
		return Csg.fromPolygons(a.allPolygons());
	}

	// Return a new CSG solid representing space both this solid and in the
	// solid `csg`. Neither this solid nor the solid `csg` are modified.
	// 
	//     A.intersect(B)
	// 
	//     +-------+
	//     |       |
	//     |   A   |
	//     |    +--+----+   =   +--+
	//     +----+--+    |       +--+
	//          |   B   |
	//          |       |
	//          +-------+
	// 
	public Csg intersect(Csg csg) {
		Node a = new Node(this.clone().polygons);
		Node b = new Node(csg.clone().polygons);
		a.invert();
		b.clipTo(a);
		b.invert();
		a.clipTo(b);
		b.clipTo(a);
		a.build(b.allPolygons());
		a.invert();
		return Csg.fromPolygons(a.allPolygons());
	}

	// Return a new CSG solid with solid and empty space switched. This solid is
	// not modified.
	public Csg inverse() {
		Csg csg = this.clone();

		for (Polygon polygon : csg.polygons) {
			polygon.flip();
		}

		return csg;
	}

	// Construct an axis-aligned solid cuboid. Optional parameters are `center` and
	// `radius`, which default to `[0, 0, 0]` and `[1, 1, 1]`. The radius can be
	// specified using a single number or a list of three numbers, one for each axis.
	// 
	// Example code:
	// 
	//	     var cube = CSG.cube({
	//	       center: [0, 0, 0],
	//	       radius: 1
	//	     });
	public static Csg cube(Map<String, Object> options) {
		options = (options == null ? new HashMap<String, Object>() : options);
		@SuppressWarnings("unchecked")
		Vector c = new Vector(options.get("center") == null ? Arrays.asList(Float.valueOf(0), Float.valueOf(0), Float.valueOf(0))
				: (List<Float>) options.get("center"));
		@SuppressWarnings("unchecked")
		List<Float> r = options.get("radius") == null ? Arrays.asList(Float.valueOf(1), Float.valueOf(1), Float.valueOf(1))
				: options.get("radius") instanceof List ? (List<Float>) options.get("radius") : Arrays.asList((Float) options.get("radius"),
						(Float) options.get("radius"), (Float) options.get("radius"));

		List<List<List<Float>>> one = Arrays.asList(Arrays.asList(Arrays.asList(0.0f, 4.0f, 6.0f, 2.0f), Arrays.asList(-1.0f, 0.0f, 0.0f)),
				Arrays.asList(Arrays.asList(1.0f, 3.0f, 7.0f, 5.0f), Arrays.asList(+1.0f, 0.0f, 0.0f)),
				Arrays.asList(Arrays.asList(0.0f, 1.0f, 5.0f, 4.0f), Arrays.asList(0.0f, -1.0f, 0.0f)),
				Arrays.asList(Arrays.asList(2.0f, 6.0f, 7.0f, 3.0f), Arrays.asList(0.0f, +1.0f, 0.0f)),
				Arrays.asList(Arrays.asList(0.0f, 2.0f, 3.0f, 1.0f), Arrays.asList(0.0f, 0.0f, -1.0f)),
				Arrays.asList(Arrays.asList(4.0f, 5.0f, 7.0f, 6.0f), Arrays.asList(0.0f, 0.0f, +1.0f)));

		List<Polygon> polygons = new ArrayList<Polygon>();

		for (List<List<Float>> two : one) {
			List<Vertex> vertices = new ArrayList<Vertex>();
			for (Float three : two.get(0)) {
				Vector pos = new Vector(c.x + r.get(0) * (2.0f * (three.intValue() & 1) != 0 ? 1.0f : 0.0f - 1.0f), c.y + r.get(1)
						* (2.0f * (three.intValue() & 2) != 0 ? 1.0f : 0.0f - 1), c.z + r.get(2) * (2.0f * (three.intValue() & 4) != 0 ? 1.0f : 0.0f - 1));
				Vertex v = new Vertex(pos, new Vector(two.get(1)));
				vertices.add(v);
			}

			polygons.add(new Polygon(vertices));
		}

		return Csg.fromPolygons(polygons);
	}

	// Construct a solid sphere. Optional parameters are `center`, `radius`,
	// `slices`, and `stacks`, which default to `[0, 0, 0]`, `1`, `16`, and `8`.
	// The `slices` and `stacks` parameters control the tessellation along the
	// longitude and latitude directions.
	// 
	// Example usage:
	// 
	//	     var sphere = CSG.sphere({
	//	       center: [0, 0, 0],
	//	       radius: 1,
	//	       slices: 16,
	//	       stacks: 8
	//	     });
	public static Csg sphere(Map<String, Object> options) {
		options = (options == null ? new HashMap<String, Object>() : options);

		@SuppressWarnings("unchecked")
		Vector c = new Vector(options.get("center") == null ? Arrays.asList(Float.valueOf(0), Float.valueOf(0), Float.valueOf(0))
				: (List<Float>) options.get("center"));
		Float r = options.get("radius") == null ? Float.valueOf(1) : (Float) options.get("radius");
		Float slices = options.get("slices") == null ? Float.valueOf(16) : (Float) options.get("slices");
		Float stacks = options.get("stacks") == null ? Float.valueOf(8) : (Float) options.get("stacks");
		List<Polygon> polygons = new ArrayList<Polygon>();
		List<Vertex> vertices;

		for (int i = 0; i < slices; i++) {
			for (int j = 0; j < stacks; j++) {
				vertices = new ArrayList<Vertex>();
				vertex(i / slices, j / stacks, r, c, vertices);
				if (j > 0)
					vertex((i + 1) / slices, j / stacks, r, c, vertices);
				if (j < stacks - 1)
					vertex((i + 1) / slices, (j + 1) / stacks, r, c, vertices);
				vertex(i / slices, (j + 1) / stacks, r, c, vertices);
				polygons.add(new Polygon(vertices));
			}
		}
		return Csg.fromPolygons(polygons);
	};

	private static void vertex(Float theta, Float phi, Float r, Vector c, List<Vertex> vertices) {
		theta = (float) (theta * Math.PI * 2.0f);
		phi = (float) (phi * Math.PI);
		Vector dir = new Vector((float) (Math.cos(theta) * Math.sin(phi)), (float) Math.cos(phi), (float) (Math.sin(theta) * Math.sin(phi)));
		vertices.add(new Vertex(c.plus(dir.times(r)), dir));
	}

	// Construct a solid cylinder. Optional parameters are `start`, `end`,
	// `radius`, and `slices`, which default to `[0, -1, 0]`, `[0, 1, 0]`, `1`, and
	// `16`. The `slices` parameter controls the tessellation.
	// 
	// Example usage:
	// 
	//	     var cylinder = CSG.cylinder({
	//	       start: [0, -1, 0],
	//	       end: [0, 1, 0],
	//	       radius: 1,
	//	       slices: 16
	//	     });
	public static Csg cylinder(Map<String, Object> options) {
		options = (options == null ? new HashMap<String, Object>() : options);

		@SuppressWarnings("unchecked")
		Vector s = new Vector(options.get("start") == null ? Arrays.asList(Float.valueOf(0), Float.valueOf(-1), Float.valueOf(0))
				: (List<Float>) options.get("start"));
		@SuppressWarnings("unchecked")
		Vector e = new Vector(options.get("end") == null ? Arrays.asList(Float.valueOf(0), Float.valueOf(1), Float.valueOf(0))
				: (List<Float>) options.get("end"));
		Vector ray = e.minus(s);
		Float r = options.get("radius") == null ? Float.valueOf(1) : (Float) options.get("radius");
		Float slices = options.get("slices") == null ? Float.valueOf(16) : (Float) options.get("slices");

		Vector axisZ = ray.unit();
		boolean isY = (Math.abs(axisZ.y) > 0.5);
		Vector axisX = new Vector(isY ? 1.0f : 0.0f, isY ? 0.0f : 1.0f, 0.0f).cross(axisZ).unit();
		Vector axisY = axisX.cross(axisZ).unit();
		Vertex start = new Vertex(s, axisZ.negated());
		Vertex end = new Vertex(e, axisZ.unit());
		List<Polygon> polygons = new ArrayList<Polygon>();

		for (int i = 0; i < slices; i++) {
			Float t0 = i / slices, t1 = (i + 1) / slices;
			polygons.add(new Polygon(start, point(0.0f, t0, -1.0f, s, axisX, axisY, axisZ, ray, r), point(0.0f, t1, -1.0f, s, axisX, axisY, axisZ, ray, r)));
			polygons.add(new Polygon(point(0.0f, t1, 0.0f, s, axisX, axisY, axisZ, ray, r), point(0.0f, t0, 0.0f, s, axisX, axisY, axisZ, ray, r), point(1.0f,
					t0, 0.0f, s, axisX, axisY, axisZ, ray, r), point(1.0f, t1, 0.0f, s, axisX, axisY, axisZ, ray, r)));
			polygons.add(new Polygon(end, point(1.0f, t1, 1.0f, s, axisX, axisY, axisZ, ray, r), point(1.0f, t0, 1.0f, s, axisX, axisY, axisZ, ray, r)));
		}

		return Csg.fromPolygons(polygons);
	};

	private static Vertex point(Float stack, Float slice, Float normalBlend, Vector s, Vector axisX, Vector axisY, Vector axisZ, Vector ray, Float r) {
		Float angle = (float) (slice * Math.PI * 2.0f);
		Vector out = axisX.times((float) Math.cos(angle)).plus(axisY.times((float) Math.sin(angle)));
		Vector pos = s.plus(ray.times(stack)).plus(out.times(r));
		Vector normal = out.times(1 - Math.abs(normalBlend)).plus(axisZ.times(normalBlend));
		return new Vertex(pos, normal);
	}

}