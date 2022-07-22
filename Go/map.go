// Project CSI2120/CSI2520
// Winter 2022
// Robert Laganiere, uottawa.ca

package main

import (
	"encoding/csv"
	"fmt"
	"io"
	"math"
	"os"
	"runtime"
	"strconv"
	"sync"
	"time"
)

type GPScoord struct {
	lat  float64
	long float64
}

type LabelledGPScoord struct {
	GPScoord
	ID    int // point ID
	Label int // cluster ID
}

const N int = 4
const MinPts int = 5
const eps float64 = 0.0003
const filename string = "yellow_tripdata_2009-01-15_9h_21h_clean.csv"

func main() {

	start := time.Now()

	gps, minPt, maxPt := readCSVFile(filename)
	fmt.Printf("Number of points: %d\n", len(gps))

	minPt = GPScoord{40.7, -74.}
	maxPt = GPScoord{40.8, -73.93}

	// geographical limits
	fmt.Printf("SW:(%f , %f)\n", minPt.lat, minPt.long)
	fmt.Printf("NE:(%f , %f) \n\n", maxPt.lat, maxPt.long)

	// Parallel DBSCAN STEP 1.
	incx := (maxPt.long - minPt.long) / float64(N)
	incy := (maxPt.lat - minPt.lat) / float64(N)

	var grid [N][N][]LabelledGPScoord // a grid of GPScoord slices

	// Create the partition
	// triple loop! not very efficient, but easier to understand

	partitionSize := 0
	for j := 0; j < N; j++ {
		for i := 0; i < N; i++ {

			for k := 0; k < len(gps); k++ {
				pt := gps[k]
				// is it inside the expanded grid cell
				if (pt.long >= minPt.long+float64(i)*incx-eps) && (pt.long < minPt.long+float64(i+1)*incx+eps) &&
					(pt.lat >= minPt.lat+float64(j)*incy-eps) && (pt.lat < minPt.lat+float64(j+1)*incy+eps) {

					grid[i][j] = append(grid[i][j], pt) // add the point to this slide
					partitionSize++
				}
			}
		}
	}

	jobs := make(chan []LabelledGPScoord, N*N) // make channels for each paritions of points
	offset := make(chan int, N*N)              // make channels for each paritions's offset

	var mutex sync.WaitGroup // create a wait group

	NumberOfConsumerThreads := 4 // number of consumer threadds

	mutex.Add(NumberOfConsumerThreads) // adds to the waitgroup the number of threads

	// launches every thread concurrently
	for i := 0; i < NumberOfConsumerThreads; i++ {
		go consume(jobs, MinPts, eps, offset, &mutex)
	}

	// prdocucer produces N*N jobs
	for j := 0; j < N; j++ {
		for i := 0; i < N; i++ {
			jobs <- grid[i][j]
			offset <- i*10000000 + j*1000000
		}
	}

	//closes both channels
	close(offset)
	close(jobs)

	// wait for waitgroup
	mutex.Wait()

	end := time.Now()
	fmt.Printf("\nExecution time: %s of %d points\n", end.Sub(start), partitionSize)
	fmt.Printf("Number of CPUs: %d", runtime.NumCPU())
}

// consume function that launches DBScan for each partition of points
func consume(jobs chan []LabelledGPScoord, mintPoints int, eps float64, offset chan int, done *sync.WaitGroup) {

	for {
		job, more := <-jobs // take out list of coords from jobs channel

		// if there are still lists then do DBScan and if not end the waitgroup
		if more {
			DBscan(job, mintPoints, eps, <-offset)
		} else {
			done.Done()
			return
		}
	}

}

//calculates distance between 2 gps coordinates
func distance(x GPScoord, y GPScoord) float64 {

	X := (y.lat - x.lat) * (y.lat - x.lat)
	Y := (y.long - x.long) * (y.long - x.long)

	return math.Sqrt(X + Y)
}

//creates a list of points by calculating the distance eps from initial points to all other points surrounding it
func rangeQuery(coords *[]LabelledGPScoord, point LabelledGPScoord, eps float64) []LabelledGPScoord {
	NewCoords := make([]LabelledGPScoord, 0)

	for i := 0; i < len(*coords); i++ {

		if (*coords)[i] == point {
			continue
		}
		if distance(point.GPScoord, (*coords)[i].GPScoord) <= eps {
			NewCoords = append(NewCoords, (*coords)[i])
		}

	}
	return NewCoords
}

// adds list of neighbours to existing slice merging them
func mergeCoords(c1 *[]LabelledGPScoord, c2 *[]LabelledGPScoord) {

	for i := 0; i < len(*c2); i++ {
		*c1 = append(*c1, (*c2)[i])
	}

}

// Applies DBSCAN algorithm on LabelledGPScoord points
// LabelledGPScoord: the slice of LabelledGPScoord points
// MinPts, eps: parameters for the DBSCAN algorithm
// offset: label of first cluster (also used to identify the cluster)
// returns number of clusters found
func DBscan(coords []LabelledGPScoord, MinPts int, eps float64, offset int) (nclusters int) {

	nclusters = 0
	visited := map[LabelledGPScoord]string{}

	for i := 0; i < len(coords); i++ {
		pt := &coords[i]

		if visited[*pt] == "C" || visited[*pt] == "Noise" { // if point has already been labelled conitnue
			continue
		}

		NewCoords := rangeQuery(&coords, *pt, eps) // find all the nearest points to form a slice of points

		if len(NewCoords) < MinPts { // if the size of the list is smaller than the mimum amount of points then just set the point to Noise and continue to next iteration
			visited[*pt] = "Noise" // label point as noise
			continue
		}

		nclusters++        // increment cluster counter
		visited[*pt] = "C" // label the point as start of a cluster
		pt.Label = offset + nclusters
		NewCoords = append(NewCoords, *pt)

		for k := 0; k < len(NewCoords); k++ { // loops through every point in neighbours found
			ptQ := &NewCoords[k]

			if visited[*ptQ] == "C" { // if the point has already been processed the continue to next iteration
				continue
			}

			visited[*ptQ] = "C" // label the point as a part of the cluster now

			NearCoords := rangeQuery(&coords, *ptQ, eps) // find all the nearest points to form a slice of points

			if len(NearCoords) >= MinPts { // if the size of the list is not considered Noise then merge the new calculated list with the intial one
				mergeCoords(&NewCoords, &NearCoords)
			}

		}

	}

	// Printing the result (do not remove)
	fmt.Printf("Partition %10d : [%4d,%6d]\n", offset, nclusters, len(coords))

	return nclusters
}

// reads a csv file of trip records and returns a slice of the LabelledGPScoord of the pickup locations
// and the minimum and maximum GPS coordinates
func readCSVFile(filename string) (coords []LabelledGPScoord, minPt GPScoord, maxPt GPScoord) {

	coords = make([]LabelledGPScoord, 0, 5000)

	// open csv file
	src, err := os.Open(filename)
	defer src.Close()
	if err != nil {
		panic("File not found...")
	}

	// read and skip first line
	r := csv.NewReader(src)
	record, err := r.Read()
	if err != nil {
		panic("Empty file...")
	}

	minPt.long = 1000000.
	minPt.lat = 1000000.
	maxPt.long = -1000000.
	maxPt.lat = -1000000.

	var n int = 0

	for {
		// read line
		record, err = r.Read()

		// end of file?
		if err == io.EOF {
			break
		}

		if err != nil {
			panic("Invalid file format...")
		}

		// get lattitude
		lat, err := strconv.ParseFloat(record[9], 64)
		if err != nil {
			panic("Data format error (lat)...")
		}

		// is corner point?
		if lat > maxPt.lat {
			maxPt.lat = lat
		}
		if lat < minPt.lat {
			minPt.lat = lat
		}

		// get longitude
		long, err := strconv.ParseFloat(record[8], 64)
		if err != nil {
			panic("Data format error (long)...")
		}

		// is corner point?
		if long > maxPt.long {
			maxPt.long = long
		}

		if long < minPt.long {
			minPt.long = long
		}

		// add point to the slice
		n++
		pt := GPScoord{lat, long}
		coords = append(coords, LabelledGPScoord{pt, n, 0})
	}

	return coords, minPt, maxPt
}
