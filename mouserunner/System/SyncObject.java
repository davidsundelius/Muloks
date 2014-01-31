/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mouserunner.System;

import java.io.Serializable;

/**
 *
 * @author Erik
 */
public class SyncObject implements Serializable, Comparable<SyncObject> {
	public final int id,dir;
	public final float x, y;
	public final Class type;

	public SyncObject(Class type, int id, Direction dir) {
		this(type, id,-1, -1, dir);
	}
	
	public SyncObject(Class type, int id,float x, float y, Direction dir) {
		this.type = type;
		this.id = id;
		this.x = x;
		this.y = y;
		this.dir = Direction.dirTiInt(dir);
	}

	public int compareTo(SyncObject o) {
		return this.id - o.id;
	}

}
