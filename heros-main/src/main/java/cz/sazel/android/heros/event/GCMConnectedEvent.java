package cz.sazel.android.heros.event;

import cz.sazel.android.heros_control.Id;

/**
 * Created on 31.7.15.
 */
public class GCMConnectedEvent {

	private final Id mId;

	public GCMConnectedEvent(Id id) {
		mId = id;
	}

	public Id getId() {
		return mId;
	}
}
