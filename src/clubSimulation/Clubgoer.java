//M. M. Kuttel 2023 mkuttel@gmail.com
package clubSimulation;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 This is the basic ClubGoer Thread class, representing the patrons at the club
 */

public class Clubgoer extends Thread {
	public AtomicBoolean pause;
	public AtomicBoolean start = new AtomicBoolean(false);
	public static ClubGrid club; // shared club

	GridBlock currentBlock;
	private Random rand;
	private int movingSpeed;

	private PeopleLocation myLocation;
	private boolean inRoom;
	private boolean thirsty;
	private boolean wantToLeave;
	PeopleCounter tallys;

	private int ID; // thread ID

	Clubgoer(int ID, PeopleLocation loc, int speed, PeopleCounter tallys, AtomicBoolean pause) {
		this.ID = ID;
		movingSpeed = speed; // range of speeds for customers
		this.myLocation = loc; // for easy lookups
		inRoom = false; // not in room yet
		thirsty = true; // thirsty when arrive
		wantToLeave = false; // want to stay when arrive
		rand = new Random();
		this.tallys = tallys;
		this.pause = pause;
	}

	// getter
	public boolean inRoom() {
		return inRoom;
	}

	synchronized void setStartTrue() {
		synchronized (start) {
			start.set(true);
			start.notifyAll();
		}
	}

	// getter
	public int getX() {
		return currentBlock.getX();
	}

	// getter
	public int getY() {
		return currentBlock.getY();
	}

	// getter
	public int getSpeed() {
		return movingSpeed;
	}

	// setter

	// check to see if user pressed pause button
	private void checkPause() {
		synchronized (pause) {
			while (pause.get()) {
				try {
					pause.wait();
				} catch (InterruptedException f) {
				}
			}	
		}
	}
	public synchronized void Capacity() throws InterruptedException{
		Random nap = new Random();
		sleep(rand.nextInt(100));
		if(!this.inRoom()){
			
			tallys.personArrived();
			
			while(tallys.overCapacity()){
				try{
				sleep(nap.nextInt(100));}// check if the capacity has changed in random intervals
				catch(InterruptedException e){}
			}
			tallys.personEntered();
		}

	}

	synchronized void setConditionTrue() {
		pause.set(true);
		// notifyAll(); // Notifies all waiting threads to wake up
	}

	synchronized void setConditionFalse() {
		synchronized (pause) {
			pause.set(false);
			pause.notifyAll();
		}
	}

	private void startSim() {
		synchronized (start) {
			while (!start.get()) {
				try {
					start.wait();
				} catch (InterruptedException r) {
				}
			}
		}
	}

	// get drink at bar
	private void getDrink() throws InterruptedException {
		// FIX SO BARMAN GIVES THE DRINK AND IT IS NOT AUTOMATIC
		// andre moves left and right and stops when there a someone at the bar
		thirsty = false;
		System.out.println(
				"Thread " + this.ID + " got drink at bar position: " + currentBlock.getX() + " " + currentBlock.getY());
		sleep(movingSpeed * 5); // wait a bit
	}

	// --------------------------------------------------------
	// DO NOT CHANGE THE CODE BELOW HERE - it is not necessary
	// clubgoer enters club
	public synchronized void enterClub() throws InterruptedException {
		currentBlock = club.enterClub(myLocation); // enter through entrance
		inRoom = true;
		System.out.println(
				"Thread " + this.ID + " entered club at position: " + currentBlock.getX() + " " + currentBlock.getY());
		sleep(movingSpeed / 2); // wait a bit at door
	}

	// go to bar
	private void headToBar() throws InterruptedException {
		int x_mv = rand.nextInt(3) - 1; // -1,0 or 1
		int y_mv = Integer.signum(club.getBar_y() - currentBlock.getY());// -1,0 or 1
		currentBlock = club.move(currentBlock, x_mv, y_mv, myLocation); // head toward bar
		System.out.println("Thread " + this.ID + " moved toward bar to position: " + currentBlock.getX() + " "
				+ currentBlock.getY());
		sleep(movingSpeed / 2); // wait a bit
	}

	// go head towards exit
	private void headTowardsExit() throws InterruptedException {
		GridBlock exit = club.getExit();
		while(exit.occupied()){
			sleep(rand.nextInt(100));
		} // waits until the exit is unocupied to head outside
		int x_mv = Integer.signum(exit.getX() - currentBlock.getX());// x_mv is -1,0 or 1
		int y_mv = Integer.signum(exit.getY() - currentBlock.getY());// -1,0 or 1
		currentBlock = club.move(currentBlock, x_mv, y_mv, myLocation);
		System.out.println(
				"Thread " + this.ID + " moved to towards exit: " + currentBlock.getX() + " " + currentBlock.getY());
		sleep(movingSpeed); // wait a bit
	}

	// dancing in the club
	private void dance() throws InterruptedException {
		for (int i = 0; i < 3; i++) { // sequence of 3

			int x_mv = rand.nextInt(3) - 1; // -1,0 or 1
			int y_mv = Integer.signum(1 - x_mv);

			for (int j = 0; j < 4; j++) { // do four fast dance steps
				currentBlock = club.move(currentBlock, x_mv, y_mv, myLocation);
				sleep(movingSpeed / 5);
				x_mv *= -1;
				y_mv *= -1;
			}
			checkPause();
		}
	}

	// wandering about in the club
	private void wander() throws InterruptedException {
		for (int i = 0; i < 2; i++) { //// wander for two steps
			int x_mv = rand.nextInt(3) - 1; // -1,0 or 1
			int y_mv = Integer.signum(-rand.nextInt(4) + 1); // -1,0 or 1 (more likely to head away from bar)
			currentBlock = club.move(currentBlock, x_mv, y_mv, myLocation);
			sleep(movingSpeed);
		}
	}

	// leave club
	private synchronized void leave() throws InterruptedException {
		club.leaveClub(currentBlock, myLocation);
		inRoom = false;
	}

	public void run() {
		try {
	
			startSim();
			checkPause();
			sleep(movingSpeed * (rand.nextInt(100) + 1)); // arriving takes a while
			checkPause();
			myLocation.setArrived();
			System.out.println("Thread " + this.ID + " arrived at club"); // output for checking
			Capacity(); //checks if the club is at capacity and waits outside
			checkPause(); // check whether have been asked to pause
			enterClub();

			while (inRoom) {
				checkPause(); // check every step
				if ((!thirsty) && (!wantToLeave)) {
					if (rand.nextInt(100) > 95)
						thirsty = true; // thirsty every now and then
					else if (rand.nextInt(100) > 98)
						wantToLeave = true; // at some point want to leave
				}

				if (wantToLeave) { // leaving overides thirsty
					sleep(movingSpeed / 5); // wait a bit
					if (currentBlock.isExit()) {
						leave();
						System.out.println("Thread " + this.ID + " left club");
					} else {
						System.out.println("Thread " + this.ID + " going to exit");
						headTowardsExit();
					}
				} else if (thirsty) {
					sleep(movingSpeed / 5); // wait a bit
					if (currentBlock.isBar()) {
						getDrink();
						System.out.println("Thread " + this.ID + " got drink ");
					} else {
						System.out.println("Thread " + this.ID + " going to getDrink ");
						headToBar();
					}
				} else {
					if (currentBlock.isDanceFloor()) {
						dance();
						System.out.println("Thread " + this.ID + " dancing ");
					}
					wander();
					System.out.println("Thread " + this.ID + " wandering about ");
				}

			}
			System.out.println("Thread " + this.ID + " is done");

		} catch (InterruptedException e1) { // do nothing
		}
	}

}
