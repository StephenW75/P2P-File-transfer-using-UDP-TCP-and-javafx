package P2P_ClientServer;

public class File {
	private String name;
	private double size;
	
	//Constructor
	File(String name, double size) {
		this.name = name;
		this.size = size;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String newName) {
		this.name = newName;
	}
	
	public double getSize() {
		return size;
	}
	
	public void setSize(double newSize) {
		this.size = newSize;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
