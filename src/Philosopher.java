import java.util.concurrent.locks.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Philosopher extends Thread {
	private static int WAITING = 0, EATING = 1, THINKING = 2;
	private Lock lock;
	private Condition phil[];
	private int states[];
	private int timeEated[];
	private int NUM_PHILS;
	private int id;
	private final int TURNS = 20;
	private long waitTimes = 0;
	private long executionTimes = 0;
	private CopyOnWriteArrayList<Philosopher> waiting;
	private int count_stall;
	boolean ableToEat;

	public Philosopher(Lock l, Condition p[], int s[], int num, CopyOnWriteArrayList<Philosopher> arr, int[] eat) {
		lock = l;
		phil = p;
		states = s;
		NUM_PHILS = num;
		waiting = arr;
		timeEated = eat;
		count_stall = 0;
	}

	public void run() {
		long time = System.nanoTime(); // Time for phil to execute
		id = ThreadID.get();
		for (int k = 0; k < TURNS; k++) {
			try {
				sleep(100);
			} catch (Exception ex) {
				/* lazy */
			}
			takeSticks(id);
			try {
				sleep(20);
			} catch (Exception ex) {

			}
			putSticks(id);
		}
		executionTimes = System.nanoTime() - time;

	}// end run

	public void takeSticks(int id) {
		long time = System.nanoTime();
		lock.lock();
		waitTimes += System.nanoTime() - time;
		try {
			ableToEat = true; // condition for checking to see if waiting
								// threads are able to eat
			if (!waiting.isEmpty()) {// check if queue is null
				for (int i = 0; i < waiting.size(); i++) {// waiting threads
					int temp = waiting.get(i).getID();
					if (id == leftof(temp) || id == rightof(temp)) {
						// if either side is waiting, thread does not proceed
						ableToEat = false;
						break;
					} // end if
				} // end for
			} // end if

			// protocol so that threads cannot bypass other threads in the queue
			if (states[leftof(id)] == THINKING && states[rightof(id)] == THINKING && ableToEat) {
				states[id] = EATING;
				timeEated[id]++;
			} else {
				count_stall++;
				states[id] = WAITING;
				waiting.add(this); // will add thread in the queue
				time = System.nanoTime();
				// philosopher is waiting for status update
				// await is a synch method to make it starvation free
				phil[id].await(); // will set thread to wait
				waitTimes += System.nanoTime() - time;
			}
		} // end try block
		catch (InterruptedException e) {
			System.exit(-1);
		} finally {
			lock.unlock();
		}
	}// end takeSticks

	public void putSticks(int id) {
		long time = System.nanoTime();
		lock.lock();
		waitTimes += System.nanoTime() - time;
		try {
			states[id] = THINKING;
			// will check the queue to see which thread can start anytime and
			// which thread stops eating goes at the front of the queue
			// to give priority to the threads that has waited longer
			if (!waiting.isEmpty()) {
				for (int i = 0; i < waiting.size(); i++) {
					Philosopher p = waiting.get(i);
					// thread came from list = ignores if others are waiting
					if (states[leftof(p.getID())] != EATING && states[rightof(p.getID())] != EATING) {
						phil[p.getID()].signal();// Signal an awaiting phil
						states[p.getID()] = EATING;
						timeEated[p.getID()]++;
						waiting.remove(i);
					}
				}
			}
		}
		finally {
			lock.unlock();
		}
	}

	private int leftof(int id) { // clockwise
		int retval = id - 1;
		if (retval < 0) // not valid id
			retval = NUM_PHILS - 1;
		return retval;
	}

	private int rightof(int id) {
		int retval = id + 1;
		if (retval == NUM_PHILS) // not valid id
			retval = 0;
		return retval;
	}

	public int getID() {
		return id;
	}

	public long getWaitTime(){
		return waitTimes;
	}

	public long getExtTime(){
		return executionTimes;
	}

	public int getWaitCount(){
		return count_stall;
	}

	public int getThreadID(){
		return id;
	}
}
