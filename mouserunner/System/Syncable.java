package mouserunner.System;

/**
 *
 * @author Erik
 */
public interface Syncable {

	/**
	 * This method syncs the updatable object with the given parameter. How this
	 * sync is performed and what requirements are required is determined by the 
	 * implemented class.
	 * @param s
	 */
	public void synchronize(SyncObject s);

	public SyncObject getSyncable();
}
