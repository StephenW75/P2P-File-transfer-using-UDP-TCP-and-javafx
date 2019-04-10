package DHT_Server;

public class CircularNum {
	
	private int circumference;
	
	CircularNum (int circ) {
		circumference = circ;
	}
	
	int getNumber(int number) {
		
		int tmp = number;

		while (tmp > circumference) {
			tmp -= circumference;
		}
		
		while (tmp < 1) {
			tmp += circumference;
		}
		return tmp;
	}
	
}
