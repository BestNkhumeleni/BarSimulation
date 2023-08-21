//M. M. Kuttel 2023 mkuttel@gmail.com
package clubSimulation;

import java.awt.Color;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 This is the basic AndreTheBarman Thread class, representing the patrons at the club
 */

public class AndreTheBarman extends Thread {
	public AtomicBoolean pause = new AtomicBoolean(false);
	public AtomicBoolean start = new AtomicBoolean(false);
	public static ClubGrid club; // shared club

	GridBlock currentBlock;
	private Random rand;
	private int movingSpeed;

	private PeopleLocation myLocation;
	private boolean inRoom;
	private int ID; // thread ID

	AndreTheBarman(int noClubgoers) throws InterruptedException {
		this.ID = noClubgoers;
		movingSpeed = 10; // range of speeds for customers
		inRoom = true; // not in room yet
		rand = new Random();
		myLocation = new PeopleLocation(ID);
		currentBlock = new GridBlock(0, 0, false, true, false);
	}

	// getter

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
			// pause.notifyAll();
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

	// AndreTheBarman enters club
	public void enterClub() throws InterruptedException {
		inRoom = true;
		myLocation.setInRoom(inRoom);
		myLocation.setColor(new Color(0, 0, 0, 0));
		myLocation.setLocation(currentBlock);
		currentBlock = club.enterClub(myLocation); //enter through entrance
		System.out.println("Andre is here");
		sleep(movingSpeed / 2); // wait a bit at door
	}

	// go to bar
	private void headToBar() throws InterruptedException {
		int x_mv = rand.nextInt(3) - 1;
		currentBlock = club.move(currentBlock, x_mv, club.getBar_y() - 1, myLocation); // head toward bar
		System.out.println("Andre is at the bar");
		sleep(movingSpeed / 2); // wait a bit
	}

	private void moveLeft() throws InterruptedException {
		int x = myLocation.getX() - 1;
		currentBlock = club.move(currentBlock, club.getBar_y() - 1, x, myLocation);
	}

	private void moveRight() throws InterruptedException {
		int x = myLocation.getX() + 1;
		currentBlock = club.move(currentBlock, club.getBar_y() - 1, x, myLocation);
	}

	// go head towards exit
	private void headTowardsExit() throws InterruptedException {

		GridBlock exit = club.getExit();
		int x_mv = Integer.signum(exit.getX() - currentBlock.getX());// x_mv is -1,0 or 1
		int y_mv = Integer.signum(exit.getY() - currentBlock.getY());// -1,0 or 1
		currentBlock = club.move(currentBlock, x_mv, y_mv, myLocation);
		System.out.println(
				"Thread " + this.ID + " moved to towards exit: " + currentBlock.getX() + " " + currentBlock.getY());
		sleep(movingSpeed); // wait a bit
	}

	// leave club
	private void leave() throws InterruptedException {
		club.leaveClub(currentBlock, myLocation);
		inRoom = false;
	}

	synchronized void setStartTrue() {
		synchronized (start) {
			start.set(true);
			start.notifyAll();
		}
	}

	public void run() {
		try {
			startSim();
			checkPause(); // check whether have been asked to pause
			enterClub();

			while (inRoom) {
				checkPause(); // check every step
				headToBar();
				while (currentBlock.getX() > 0) {
					moveLeft();
				}
				while (currentBlock.getX() < club.getMaxX()) {
					moveRight();
				}
			}
			headTowardsExit();
			leave();
			System.out.println("The Bar is closed");
		} catch (InterruptedException e1) { // do nothing
		}

	}

}
