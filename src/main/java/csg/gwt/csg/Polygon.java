//
//  Polygon.java
//  javacsg
//
//  Created by William Shakour (billy1380) on 6 Dec 2014.
//  Copyright © 2014 SPACEHOPPER STUDIOS Ltd. All rights reserved.
//
package csg.gwt.csg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

//Represents a convex polygon. The vertices used to initialize a polygon must
//be coplanar and form a convex loop. They do not have to be `CSG.Vertex`
//instances but they must behave similarly (duck typing can be used for
//customization).
//
//Each convex polygon has a `shared` property, which is shared between all
//polygons that are clones of each other or were split from the same polygon.
//This can be used to define per-polygon properties (such as surface color).
public class Polygon {

	public List<Vertex> vertices;
	public Map<String, Object> shared;
	public Plane plane;

	public Polygon(List<Vertex> vertices) {
		this(vertices, null);
	}

	public Polygon(List<Vertex> vertices, Map<String, Object> shared) {
		this.vertices = vertices;
		this.shared = shared;
		this.plane = Plane.fromPoints(vertices.get(0).pos, vertices.get(1).pos,
				vertices.get(2).pos);
	}

	public Polygon(Vertex... vertex) {
		this(Arrays.asList(vertex), null);
	}

	public Polygon clone() {
		List<Vertex> vertexClone = new ArrayList<Vertex>();

		for (Vertex vertex : this.vertices) {
			vertexClone.add(vertex);
		}

		return new Polygon(vertexClone, this.shared);
	}

	public void flip() {
		Collections.reverse(this.vertices);

		for (Vertex vertex : this.vertices) {
			vertex.flip();
		}

		this.plane.flip();
	}
}
