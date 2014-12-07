//
//  Node.java
//  javacsg
//
//  Created by William Shakour (billy1380) on 6 Dec 2014.
//  Copyright © 2014 SPACEHOPPER STUDIOS Ltd. All rights reserved.
//
package csg.gwt.csg;

import java.util.ArrayList;
import java.util.List;

//Holds a node in a BSP tree. A BSP tree is built from a collection of polygons
//by picking a polygon to split along. That polygon (and all other coplanar
//polygons) are added directly to that node and the other polygons are added to
//the front and/or back subtrees. This is not a leafy BSP tree since there is
//no distinction between internal and leaf nodes.
public class Node {

	private Plane plane;
	private Node front;
	private Node back;

	private List<Polygon> polygons;

	public Node() {
		this(null);
	}

	/**
	 * @param polygons
	 */
	public Node(List<Polygon> polygons) {
		this.plane = null;
		this.front = null;
		this.back = null;
		this.polygons = new ArrayList<Polygon>();

		if (polygons != null)
			this.build(polygons);
	}

	public Node clone() {
		Node node = new Node();
		node.plane = this.plane == null ? null : this.plane.clone();
		node.front = this.front == null ? null : this.front.clone();
		node.back = this.back == null ? null : this.back.clone();

		if (this.polygons != null) {
			node.polygons = new ArrayList<Polygon>();

			for (Polygon polygon : this.polygons) {
				node.polygons.add(polygon.clone());
			}
		}

		return node;
	}

	// Convert solid space to empty space and empty space to solid space.
	public void invert() {
		for (int i = 0; i < this.polygons.size(); i++) {
			this.polygons.get(i).flip();
		}

		this.plane.flip();

		if (this.front != null)
			this.front.invert();
		if (this.back != null)
			this.back.invert();

		Node temp = this.front;
		this.front = this.back;
		this.back = temp;
	}

	// Recursively remove all polygons in `polygons` that are inside this BSP
	// tree.
	public List<Polygon> clipPolygons(List<Polygon> polygons) {
		if (this.plane == null)
			return new ArrayList<Polygon>(polygons);
		List<Polygon> front = new ArrayList<Polygon>(), back = new ArrayList<Polygon>();
		for (int i = 0; i < polygons.size(); i++) {
			this.plane.splitPolygon(polygons.get(i), front, back, front, back);
		}
		if (this.front != null)
			front = this.front.clipPolygons(front);
		if (this.back != null)
			back = this.back.clipPolygons(back);
		else
			back = new ArrayList<Polygon>();

		List<Polygon> all = new ArrayList<Polygon>(front);
		all.addAll(back);

		return all;
	}

	// Remove all polygons in this BSP tree that are inside the other BSP tree
	// `bsp`.
	public void clipTo(Node bsp) {
		this.polygons = bsp.clipPolygons(this.polygons);
		if (this.front != null)
			this.front.clipTo(bsp);

		if (this.back != null)
			this.back.clipTo(bsp);
	}

	// Return a list of all polygons in this BSP tree.
	public List<Polygon> allPolygons() {
		List<Polygon> polygons = new ArrayList<Polygon>(this.polygons);

		if (this.front != null)
			polygons.addAll(this.front.allPolygons());

		if (this.back != null)
			polygons.addAll(this.back.allPolygons());

		return polygons;
	}

	// Build a BSP tree out of `polygons`. When called on an existing tree, the
	// new polygons are filtered down to the bottom of the tree and become new
	// nodes there. Each set of polygons is partitioned using the first polygon
	// (no heuristic is used to pick a good split).
	public void build(List<Polygon> polygons) {
		if (polygons.size() == 0)
			return;
		if (this.plane == null)
			this.plane = polygons.get(0).plane.clone();

		List<Polygon> front = new ArrayList<Polygon>(), back = new ArrayList<Polygon>();
		for (int i = 0; i < polygons.size(); i++) {
			this.plane.splitPolygon(polygons.get(i), this.polygons, this.polygons, front, back);
		}
		if (front.size() > 0) {
			if (this.front == null)
				this.front = new Node();
			this.front.build(front);
		}
		if (back.size() > 0) {
			if (this.back == null)
				this.back = new Node();
			this.back.build(back);
		}
	}
}
