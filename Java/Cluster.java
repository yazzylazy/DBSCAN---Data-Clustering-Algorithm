import java.util.ArrayList;

/** 
*  Cluster is a class that holds all the points beloging to a cluster as well as the center of that cluster
* 
* @param clusterID  every cluster had their own ID on creation
* @param average  average of all cordinates 
* @param totalPoints  number of all points in cluster
* @param listOfPoints  list of all the points in the cluster
*/
class Cluster{
	private int clusterID;
	private GPScoord average;
	private int totalPoints;
	private ArrayList<TripRecord> listOfPoints;


	Cluster(int clusterID,int totalPoints,ArrayList<TripRecord> listOfPoints){
		this.clusterID = clusterID;
		this.totalPoints = totalPoints;
		this.listOfPoints = listOfPoints;
	}

	Cluster(int clusterID){
		this.clusterID = clusterID;
		this.totalPoints = 0;
		this.listOfPoints = new ArrayList<>();
		this.average = new GPScoord(0, 0);
	}





	/** 
	* Calculates the average gps coordinate of the cluster
	*/
	public void calculateAverage(){

		double averageX=0;
		double averageY=0;

		for(TripRecord trip: listOfPoints){
			averageX +=trip.getPickup_Location().getX();
			averageY +=trip.getPickup_Location().getY();
		}


		this.average = new GPScoord(averageX/totalPoints, averageY/totalPoints);
	}

	/**
	 * Adds new point to list of cluster and increments total amount of points
	 * @param point
	 */
	public void add(TripRecord point){
		listOfPoints.add(point);
		totalPoints++;
	}

	// getters and setters

	public void setAverage(GPScoord average) {
		this.average = average;
	}
	public void setClusterID(int clusterID) {
		this.clusterID = clusterID;
	}
	public void setTotalPoints(int totalPoints) {
		this.totalPoints = totalPoints;
	}
	public void setListOfPoints(ArrayList<TripRecord> listOfPoints) {
		this.listOfPoints = listOfPoints;
	}

	public GPScoord getAverage() {
		return average;
	}
	public int getClusterID() {
		return clusterID;
	}
	public int getTotalPoints() {
		return totalPoints;
	}
	public ArrayList<TripRecord> getListOfPoints() {
		return listOfPoints;
	}


}
