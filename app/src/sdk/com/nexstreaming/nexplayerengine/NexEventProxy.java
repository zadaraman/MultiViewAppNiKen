package com.nexstreaming.nexplayerengine;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by jake.you on 2015-04-01.
 */
class NexEventProxy {

	// Private member constant & variables
	private final static int DEFAULT_COLLECTION_CNT = 5;
	private ConcurrentHashMap< Integer, CopyOnWriteArrayList<WeakReference<INexEventReceiver>>> mEventReceivers;

	// Constructor
	NexEventProxy() {
		mEventReceivers = new ConcurrentHashMap<>(DEFAULT_COLLECTION_CNT);
	}

	// Interface
	protected interface INexEventReceiver {
		NexPlayerEvent[] eventsAccepted();
		void onReceive(NexPlayer nexplayer, NexPlayerEvent event);
	}

	// Protected methods
	protected void registerReceiver(INexEventReceiver receiver) {
		if( receiver != null ) {
			NexLog.d("eventProxy", "register eventProxy receiver");
			NexPlayerEvent[] events = receiver.eventsAccepted();

			for (NexPlayerEvent event : events) {
				addEventReceiver(event, receiver);
			}
		}
	}

	private void addEventReceiver(NexPlayerEvent event, INexEventReceiver receiver) {
		CopyOnWriteArrayList<WeakReference<INexEventReceiver>> receiverList = mEventReceivers.get(event.what);

		NexLog.d("eventProxy", "add eventProxy receiver");

		if( receiverList == null ) {
			receiverList = new CopyOnWriteArrayList<>();
			receiverList.add(new WeakReference<>(receiver) );
			mEventReceivers.put(event.what, receiverList);
		}
		else {
			receiverList.add(new WeakReference<>(receiver));
		}
	}

	protected void handleEvent(NexPlayer nexplayer, NexPlayerEvent event) {
		if ( nexplayer != null ) {
			if( mEventReceivers.size() > 0 ) {
				notifyEvent(nexplayer, event);
			}
		}
	}

	// Private methods
	private void notifyEvent( NexPlayer nexplayer, NexPlayerEvent event) {

		CopyOnWriteArrayList<WeakReference<INexEventReceiver>> receivers = mEventReceivers.get( event.what );

		if( receivers != null ) {
			if( receivers.size() > 0 ) {
				CopyOnWriteArrayList<WeakReference<INexEventReceiver>> removalList
						= new CopyOnWriteArrayList<>();

				for (WeakReference<INexEventReceiver> receiver : receivers) {
					INexEventReceiver _receiver = receiver.get();

					if (_receiver != null) {
						_receiver.onReceive(nexplayer, event);
					}
					else {
						removalList.add(receiver);
					}
				}
				receivers.removeAll(removalList);

				if ( receivers.size() == 0 ) {
					mEventReceivers.remove( event.what );
				}
			}
		}
	}
}