//M. M. Kuttel 2023 mkuttel@gmail.com
package clubSimulation;

import java.awt.Color;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 This is the basic AndreTheBarman Thread class, representing the patrons at the club
 */

public class AndreTheBarman extends Thread {
	public AtomicBoolean andrePause = new AtomicBoolean(false);
	public AtomicBoolean start = new AtomicBoolean(false);
	public static ClubGrid club; // shared club

	GridBlock currentBlock;
	private int movingSpeed;

	private PeopleLocation myLocation;
	private boolean inRoom;
	private int ID; // thread ID
	PeopleCounter tally;

	AndreTheBarman(int noClubgoers, AtomicBoolean pause, PeopleCounter tally) throws InterruptedException {
		this.ID = noClubgoers;
		movingSpeed = 500; // range of speeds for customers
		inRoom = true; // not in room yet
		new Random();
		myLocation = new PeopleLocation(ID);
		currentBlock = new GridBlock(0, 0, false, true, false);
		this.andrePause = pause;
		this.tally = tally;
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
	public PeopleLocation getLocation(){
		return myLocation;
	}

	// setter

	// check to see if user pressed andrePause button
	private void checkandrePause() {
		synchronized (andrePause) {
			while (andrePause.get()) {
				try {
					andrePause.wait();
				} catch (InterruptedException f) {
				}
			}
			// andrePause.notifyAll();
		}

	}

	synchronized void setConditionTrue() {
		andrePause.set(true);
		// notifyAll(); // Notifies all waiting threads to wake up
	}

	synchronized void setConditionFalse() {
		synchronized (andrePause) {
			andrePause.set(false);
			andrePause.notifyAll();
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
		currentBlock = club.enterClub(myLocation); // enter through entrance
		//tally.Andre();
		inRoom = true;
		myLocation.setColor(new Color(0));
		//tallys.personEntered();
		System.out.println(
				"Andre is in the building yall!");
		
	}
	// go to bar
	private void headToBar() throws InterruptedException {
		int x_mv = 0;
		int y = club.getBar_y() +1;
		
		currentBlock = club.move(currentBlock, x_mv, y, myLocation, this); // head toward bar
		//System.out.println(myLocation.getX());
		System.out.println("Andre is at the bar");
		sleep(movingSpeed / 2); // wait a bit
	}

	private void moveLeft() throws InterruptedException {
		int x_mv = -1;
		int y = 0;
		
		currentBlock = club.move(currentBlock, x_mv, y, myLocation, this);
	}

	private void moveRight() throws InterruptedException {
		int x_mv = 1;
		int y = 0;
		
		currentBlock = club.move(currentBlock, x_mv, y, myLocation, this);
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
			checkandrePause(); // check whether have been asked to andrePause
			enterClub();
			headToBar();
			
			while (inRoom) {
				checkandrePause(); // check every step
				while(!(myLocation.getX() == 0)){
					GridBlock serve = club.whichBlock(myLocation.getX()-1, myLocation.getY()-1);
					moveLeft();
					sleep(movingSpeed);
					checkandrePause();
					if(serve.occupied()){
						//moveRight();
						sleep(movingSpeed*5);
					}
					//System.out.println(myLocation.getX());
				}
				while(!(myLocation.getX() == club.getMaxX()-1)){
					GridBlock serve = club.whichBlock(myLocation.getX()+1, myLocation.getY()-1);
					moveRight();
					sleep(movingSpeed);
					checkandrePause();
					if(serve.occupied()){
						//moveLeft();
						sleep(movingSpeed*5);
					}
					//System.out.println(myLocation.getX());
				}
			}
			headTowardsExit();
			leave();
			System.out.println("The Bar is closed");
		} catch (InterruptedException e1) { // do nothing
		}

	}

}
