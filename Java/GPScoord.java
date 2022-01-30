/**
*  Clas that represents GPS coordinates to sepcificy coordinate of pick-up and drop-off locations
*
* @param x  x-coordinate
* @param y  y-coordinate
*/
class GPScoord{
	private double x;
	private double y;
	

	GPScoord(double x,double y){
		this.x=x;
		this.y=y;
		
	}
	//getters and setters

	public double getX() {
		return x;
	}
	public double getY() {
		return y;
	}

	public void setX(double x) {
		this.x = x;
	}
	public void setY(double y) {
		this.y = y;
	}
}
