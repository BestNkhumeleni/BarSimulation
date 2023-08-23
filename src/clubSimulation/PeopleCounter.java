package clubSimulation;
import java.util.concurrent.atomic.*;

public class PeopleCounter {
	private AtomicInteger peopleOutSide; //counter for people arrived but not yet in the building
	public AtomicInteger peopleInside; //counter for patrons inside club
	private AtomicInteger peopleLeft; //counter for patrons who have left the club
	private AtomicInteger maxPeople; //maximum patrons allowed in the club at one time
	
	PeopleCounter(int max) {
		peopleOutSide= new AtomicInteger(0);
		peopleInside=new AtomicInteger(0);
		peopleLeft=new AtomicInteger(0);
		maxPeople=new AtomicInteger(max);
	}
		
	public int getWaiting() {
		return peopleOutSide.get();
	}

	public int getInside() {
		return peopleInside.get();
	}
	
	public int getTotal() {
		return (peopleOutSide.get()+peopleInside.get()+peopleLeft.get());
	}

	public int getLeft() {
		return peopleLeft.get();
	}
	
	public int getMax() {
		return maxPeople.get();
	}
	
	//someone arrived outside
	public void personArrived() {
		peopleOutSide.getAndIncrement();
	}
	
	//someone got inside
	synchronized public void personEntered() {
		while(this.overCapacity()){try{this.wait();}catch(InterruptedException e){}}
		peopleOutSide.getAndDecrement();
		peopleInside.getAndIncrement();
	}
	//
	synchronized public void Andre(){
		peopleInside.getAndDecrement();
	}

	//someone left
	synchronized public void personLeft() {
		peopleInside.getAndDecrement();
		peopleLeft.getAndIncrement();
		this.notifyAll();
		
	}
	//too many people inside
	synchronized public boolean overCapacity() {
		if(peopleInside.get()>=maxPeople.get())
			return true;
		return false;
	}
	
	//not used
	synchronized public void resetScore() {
		peopleInside.set(0);
		peopleOutSide.set(0);
		peopleLeft.set(0);
	}
}
