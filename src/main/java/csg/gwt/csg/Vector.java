//
//  Vector.java
//  javacsg
//
//  Created by William Shakour (billy1380) on 6 Dec 2014.
//  Copyright © 2014 SPACEHOPPER STUDIOS Ltd. All rights reserved.
//
package csg.gwt.csg;

import java.util.Arrays;
import java.util.List;

//Represents a 3D vector.
//
//Example usage:
//
//  new CSG.Vector(1, 2, 3);
//  new CSG.Vector([1, 2, 3]);
//  new CSG.Vector({ x: 1, y: 2, z: 3 });
public class Vector {

	public Float x;
	public Float y;
	public Float z;

	public Vector(List<Float> o) {
		this.x = o.get(0);
		this.y = o.get(1);
		this.z = o.get(2);
	}

	public Vector clone() {
		return new Vector(this.x, this.y, this.z);
	}

	public Vector(Float... o) {
		this(Arrays.asList(o));
	}

	/**
	 * @param a
	 * @return
	 */
	public Vector times(Float a) {
		return new Vector(this.x * a, this.y * a, this.z * a);
	}

	/**
	 * @param a
	 * @return
	 */
	public Vector dividedBy(Float a) {
		return new Vector(this.x / a, this.y / a, this.z / a);
	}

	/**
	 * @param a
	 * @return
	 */
	public Vector plus(Vector a) {
		return new Vector(this.x + a.x, this.y + a.y, this.z + a.z);
	}

	/**
	 * @param a
	 * @return
	 */
	public Vector minus(Vector a) {
		return new Vector(this.x - a.x, this.y - a.y, this.z - a.z);
	}

	/**
	 * @return
	 */
	public Vector unit() {
		return this.dividedBy(this.length());
	}

	/**
	 * @return
	 */
	public Float length() {
		return (float) Math.sqrt(this.dot(this));
	}

	/**
	 * @param a
	 * @return
	 */
	public Float dot(Vector a) {
		return this.x * a.x + this.y * a.y + this.z * a.z;
	}

	/**
	 * @param a
	 * @return
	 */
	public Vector cross(Vector a) {
		return new Vector(this.y * a.z - this.z * a.y, this.z * a.x - this.x
				* a.z, this.x * a.y - this.y * a.x);
	}

	/**
	 * @return
	 */
	public Vector negated() {
		return new Vector(-this.x, -this.y, -this.z);
	}

	/**
	 * 
	 * @param a
	 * @param t
	 * @return
	 */
	public Vector lerp(Vector a, Float t) {
		return this.plus(a.minus(this).times(t));
	}

}
