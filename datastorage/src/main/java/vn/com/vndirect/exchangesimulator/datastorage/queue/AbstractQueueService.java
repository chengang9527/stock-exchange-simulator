package vn.com.vndirect.exchangesimulator.datastorage.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.log4j.Logger;

public abstract class AbstractQueueService<T> implements QueueService<T> {

	private static final Logger LOGGER = Logger.getLogger(AbstractQueueService.class);
	
	protected final BlockingQueue<T> queue = new LinkedBlockingQueue<T>();

	private final List<QueueListener> queueListeners = new ArrayList<QueueListener>();

	public AbstractQueueService() {
		init();
	}

	private void init() {
		new Thread() {
			public void run() {
				try {
					T t;
					while(true) {
						if (queueListeners.size() > 0) {
							t = queue.take();
							callListener(t);
						} else {
							Thread.sleep(100);
						}
					}
				} catch (InterruptedException ie) {
				}
			};
		}.start();
	}

	@Override
	public T peek() {
		return queue.peek();
	}

	private void callListener(T t) {
		for (QueueListener listener : queueListeners) {
			try {
				listener.onEvent(t);
			} catch(Exception e) {
				LOGGER.error("error when call listener", e);
			}
		}
	}

	@Override
	public T poll() {
		return queue.poll();
	}

	@Override
	public boolean add(T obj) {
		return queue.add(obj);
	}

	@Override
	public int size() {
		return queue.size();
	}

	public void addListener(QueueListener queueListener) {
		queueListeners.add(queueListener);
	}

}
