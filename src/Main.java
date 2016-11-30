import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main{

	private static int WAITING = 0, EATING = 1, THINKING = 2;
	private static final int NUM_PHILS 	= 20;
	private static Condition[] phil		= new Condition[NUM_PHILS];
	private static int[] states 		= new int[NUM_PHILS];
	private static Lock lock 			= new ReentrantLock();
	private static Lock lock_que				= new ReentrantLock();
	private static int[] count_eat		= new int[NUM_PHILS];
	private static CopyOnWriteArrayList<Philosopher> multArrayList = new CopyOnWriteArrayList<>();


	public static void main(String[] args){
		init();

		Philosopher[] p = new Philosopher[NUM_PHILS];
		Queue<Philosopher> que = new LinkedList<Philosopher>();
		for(int i = 0; i < p.length; i++){
			p[i] = new Philosopher(lock, phil, states, NUM_PHILS, multArrayList, count_eat);
			p[i].start();
		}

		for(int i = 0; i < p.length; i++){
			try {
				p[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		int minEat = 1000000000;
		System.out.println("Thread | Execution Time | Eat Count | Wait Count | Wait Time");
		for(int i = 0; i < p.length; i++){

			System.out.printf("%7d: %10.3f %10d %10d %15.3f\n", i,(float)p[i].getExtTime(), count_eat[(int)p[i].getThreadID()], p[i].getWaitCount(), (float)p[i].getWaitTime());
		}
//		System.out.println("Minimum number of eats : " + minEat);
	}

	public static void init(){
		for(int i = 0; i < NUM_PHILS; i++){
			phil[i] = lock.newCondition();
			states[i] = THINKING;
		}
	}
}