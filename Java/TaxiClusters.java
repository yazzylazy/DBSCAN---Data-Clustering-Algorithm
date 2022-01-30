// Yasin Elmi 300163765
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;
import java.io.*;  
import java.util.Scanner;  


/** 
*  Class TaxiClusters provides my own implemntation of a Density-Based Spatial Clustering of Applications with Noise algorithm
*  and takes as input a csv file of taxi trips and outputs all the clusters depending on epsilon and minimum number of points given
* @author Yasin Elmi 2022
*/


class TaxiClusters{
	/** 
	* creates a Cluster by calculating the distance eps from initial 
	* points to all other points surrounding it to form the database
	* @param eps  the episolon, distance measure that is used to identify the points in the neighborhood of any point
	* @param point  a gps coordinate to base off distance from neighbooring points
	* @param database  the constructed database that holds records from taxi trips made in 2009
	*/
	public ArrayList<TripRecord> rangeQuery(double eps,GPScoord point, ArrayList<TripRecord> database){
		
		ArrayList<TripRecord> neighboorPoints = new ArrayList<>();

		for(int i=0;i<database.size();i++){ 									// loop through every the database
			if(database.get(i).getPickup_Location()==point){continue;}			// SKIPS the point as to not repeat it
			TripRecord trip = database.get(i);
			if(distance(point,trip.getPickup_Location())<= eps){		// verifying point is in the cluster before adding it 
				neighboorPoints.add(trip);
			}
		}

		return neighboorPoints;
	}

	/** 
	* Calculates euclidean distance between two points
	* @param x  GPS coordinate x used to calculate distance from y
	* @param y  GPS coordinate y used to calculate distance from x
	*/
	public double distance(GPScoord x,GPScoord y){

		double X = (y.getX()-x.getX())*(y.getX()-x.getX());
		double Y = (y.getY()-x.getY())*(y.getY()-x.getY());
		
		return Math.sqrt(X+Y);
	}

	/**
	* adds list of neighbours to existing lists merging them
	* @param NeighborPts1
	* @param NeighborPts2
	*/
	public void addNeighboors(ArrayList<TripRecord> NeighborPts1,ArrayList<TripRecord> NeighborPts2){
		for( int i=0;i< NeighborPts2.size();i++){
			if(!NeighborPts1.contains(NeighborPts2.get(i))){
				NeighborPts1.add(NeighborPts2.get(i));
			}
		}
	}
	

	/** 
	* My own implementation of a Density-Based Spatial Clustering of Applications with Noise algorithm
	* @param database  the constructed database that holds records from taxi trips made in 2009
	* @param eps  the episolon, distance measure that is used to identify the points in the neighborhood of any point
	* @param minPoints the minimum number of points in the neighborhood of a point for this one to be considered to belong to a dense region
	 * @throws IOException
	*/
	public void DBScan(ArrayList<TripRecord> database,double eps,int minPoints) throws IOException{

		ArrayList<Cluster> clusters= new ArrayList<>();   // initialize list of clusters
		
		int clusterCounter=1;
		for(int i=0;i<database.size();i++){

			
			TripRecord P = database.get(i);

			if((P.getIsNoise().equals("C") || P.getIsNoise().equals("Noise"))) continue;	// if the point has already been processed then continue to the next iteration
			
			
			ArrayList<TripRecord> NeighborPtsP = rangeQuery(eps, P.getPickup_Location(), database); // find all the nearest points to form a list of points
			
			if(NeighborPtsP.size()<minPoints){  // if the size of the list is smaller than the mimum amount of points then just set the point to Noise and continue to next iteration
				P.setIsNoise("Noise");   // label the point as Noise
				continue; 
			}
			
			Cluster cluster = new Cluster(clusterCounter);
			clusterCounter++; // increment cluster counter
			
			P.setIsNoise("C");  // label the point as start of a cluster
			cluster.add(P);

			for(int k=0;k<NeighborPtsP.size();k++){  // loops through every point in the cluster

				TripRecord Q = NeighborPtsP.get(k);
				if(Q.getIsNoise().equals("C")) continue; // if the point has already been processed the continue to next iteration

				Q.setIsNoise("C");  	// label the point as a part of the cluster now
				cluster.add(Q);

				ArrayList<TripRecord> NeighborPtsQ = rangeQuery(eps, Q.getPickup_Location(), database);  // find all the nearest points to form a list of points

				if(NeighborPtsQ.size() >= minPoints){ 	// if the size of the list is not considered Noise then merge the new calculated list with the intial one
					addNeighboors(NeighborPtsP,NeighborPtsQ);
				}
				
			}			

			cluster.calculateAverage(); // average GPS coordinate is calculated
			clusters.add(cluster); // cluster is added to cluster list
		}
		
		
		csvFileMaker(clusters); // function to form csv file is called


	}
	

	public static void main(String[] args) throws IOException{
		
		TaxiClusters T = new TaxiClusters();

		ArrayList<TripRecord> database = T.databaseReader(); // holds lists of all trips recorded in 2009 by taxis

		double eps = 0.0001;
		int minPoints = 5;

		T.DBScan(database,eps,minPoints);  // executes DBScan algorithm on chosen eps and minPoints

	}


	/** 
	* Reads csv files of yellow taxis trips in 2009
	*/
	public ArrayList<TripRecord> databaseReader() throws IOException{
		ArrayList<TripRecord> database =  new ArrayList<>();
		String line="";

		BufferedReader br=new BufferedReader(new FileReader("yellow_tripdata_2009-01-15_1hour_clean.csv")); // scans the csv file;
		br.readLine();// reads the titles of every collumn first
		while ((line = br.readLine()) != null){   //returns a Boolean value  
			
			String[] tripDatabse = line.split(",");    // use comma as separator 
			
			GPScoord pickUp = new GPScoord(Double.parseDouble(tripDatabse[8]),Double.parseDouble(tripDatabse[9]));
			GPScoord dropOff = new GPScoord(Double.parseDouble(tripDatabse[12]),Double.parseDouble(tripDatabse[13]));

			TripRecord trip = new TripRecord(tripDatabse[4],pickUp,dropOff,distance(pickUp,dropOff));
			
			database.add(trip);
		}  

		
		
		return database;
	}

	/** 
	* Constructs csv file from the list off clusters 
	* @param clusters  List of all clusters found with the DBScan Algorithm
	*/
	public void csvFileMaker(ArrayList<Cluster> clusters) throws IOException{
		FileWriter csvWriter = new FileWriter("clusters.csv");

		// Adding titles of each collumn
		csvWriter.append("ClusterID");
		csvWriter.append(",");
		csvWriter.append("Longitude");
		csvWriter.append(",");
		csvWriter.append("Latitude");
		csvWriter.append(",");
		csvWriter.append("Points");
		csvWriter.append("\n");

		// loops through adding all the elements from clusters to the csv file
		for(int i =0;i<clusters.size();i++){
			csvWriter.append(Integer.toString(clusters.get(i).getClusterID()));
			csvWriter.append(",");
			csvWriter.append(Double.toString(clusters.get(i).getAverage().getX()));
			csvWriter.append(",");
			csvWriter.append(Double.toString(clusters.get(i).getAverage().getY()));
			csvWriter.append(",");
			csvWriter.append(Integer.toString(clusters.get(i).getListOfPoints().size()));
			csvWriter.append("\n");
		}


		csvWriter.flush();   // closes the file
		csvWriter.close();

	}
	

}