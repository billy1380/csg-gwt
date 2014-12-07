//
//  Vertex.java
//  javacsg
//
//  Created by William Shakour (billy1380) on 6 Dec 2014.
//  Copyright © 2014 SPACEHOPPER STUDIOS Ltd. All rights reserved.
//
package csg.gwt.csg;

//Represents a vertex of a polygon. Use your own vertex class instead of this
//one to provide additional features like texture coordinates and vertex
//colors. Custom vertex classes need to provide a `pos` property and `clone()`,
//`flip()`, and `interpolate()` methods that behave analogous to the ones
//defined by `CSG.Vertex`. This class provides `normal` so convenience
//functions like `CSG.sphere()` can return a smooth vertex normal, but `normal`
//is not used anywhere else.
public class Vertex {

	public Vector pos;
	public Vector normal;

	public Vertex(Vector pos, Vector normal) {
		this.pos = pos.clone();
		this.normal = normal.clone();
	}

	public Vertex clone() {
		return new Vertex(this.pos.clone(), this.normal.clone());
	}

	// Invert all orientation-specific data (e.g. vertex normal). Called when the
	// orientation of a polygon is flipped.
	public void flip() {
		this.normal = this.normal.negated();
	}

	// Create a new vertex between this vertex and `other` by linearly
	// interpolating all properties using a parameter of `t`. Subclasses should
	// override this to interpolate additional properties.
	public Vertex interpolate(Vertex other, Float t) {
		return new Vertex(this.pos.lerp(other.pos, t), this.normal.lerp(
				other.normal, t));
	}
}
