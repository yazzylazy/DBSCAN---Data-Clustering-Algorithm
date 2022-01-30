	/** 
	*	TripRecord represents every taxi cab's trip, we can use it to hold data from our csv file as well as 
	*   using it for our DBscan Algorithm as possible labels
	* @param isNoise  label of the point/used during the DBScan algorithm
	*/
	class TripRecord{
		private String pickup_DateTime;
		private GPScoord pickup_Location;
		private GPScoord dropoff_Location;
		private double trip_Distance;
		private String isNoise; // equal to "Noise" if the point is Noise or equal to "C" if it is a cluster otherwise empty string("")


		/** 
		*   TripRecord is a class that holds the information read from the csv file about the taxi trips in 2009
		*
		* @param pickup_DateTime  the date of the trip
		* @param pickup_Location  the gps coordinates of the location the trip started
		* @param dropoff_Location the gps coordinates of the location the trip ended
		* @param d  the distance of the entire trip(using eucladienne distance)
		*/
		TripRecord(String pickup_DateTime,GPScoord pickup_Location,GPScoord dropoff_Location,double d){
			this.pickup_DateTime = pickup_DateTime;
			this.pickup_Location = pickup_Location;
			this.dropoff_Location = dropoff_Location;
			this.trip_Distance = d;
			this.isNoise="";
		}

		

		// getters ans setters

		public GPScoord getDropoff_Location() {
			return dropoff_Location;
		}
		public String getPickup_DateTime() {
			return pickup_DateTime;
		}
		public GPScoord getPickup_Location() {
			return pickup_Location;
		}
		public double getTrip_Distance() {
			return trip_Distance;
		}
		public String getIsNoise() {
			return isNoise;
		}
		public void setDropoff_Location(GPScoord dropoff_Location) {
			this.dropoff_Location = dropoff_Location;
		}
		public void setPickup_DateTime(String pickup_DateTime) {
			this.pickup_DateTime = pickup_DateTime;
		}
		public void setPickup_Location(GPScoord pickup_Location) {
			this.pickup_Location = pickup_Location;
		}
		public void setTrip_Distance(float trip_Distance) {
			this.trip_Distance = trip_Distance;
		}
		public void setIsNoise(String isNoise) {
			this.isNoise = isNoise;
		}

	}

