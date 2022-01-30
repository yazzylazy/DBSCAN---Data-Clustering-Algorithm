# 🚕DBSCAN---Data-Clustering-Algorithm🚕
This is a Density-Based Spatial Clustering of Applications with Noise. Using a dataset of taxi trip records in New York City, in 2009, 
I identified the best waiting areas for vehicles. Since the taxi's are located in relatively small areas, I can simply use the Euclidean distance 
between the GPS coordinates.

<h2>☕Java Implementation☕</h2> 

I used <h4> 3 different objects </h4> to from my DBScan algorithm in Java. 
<strong>GPSCoord</strong> which held the GPS coordinate of the taxi's starting location. 
<strong>TripRecord</strong> holds information read from the CSV file: the date,the pickup location,the drop off location and the trip distance. I used the TripRecord as a sort of point
in my DBScan algorithm so we can consider TripRecords as part of clusters or Noise. 
<strong>Cluster</strong> class holds the average GPS coordinate as well as the list of TripRecord held in a Cluster. These Clusters are used to calculate and held values that
will be listed in a csv file.

Finally TaxiCluster holds all the the DBSCAN algorithm itself as well as a multitude of helper function to read the csv file,calculate distances and create csv files

<h2>🐻Go Implementation🐻</h2> 

coming soon....

<h2>💾Prolog Implementation 💾</h2> 

coming soon....

<h2>💻Scheme Implementation💻</h2> 

coming soon....
