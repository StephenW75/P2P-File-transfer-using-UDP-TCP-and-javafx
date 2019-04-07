package TEST;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class DhtServerTester {

	public static void main(String[] args) throws Exception {

		System.out.println(getAddress("192"));

	}
	
	static InetAddress getAddress(String seq) throws Exception {
		Enumeration<NetworkInterface> allNetInterface = NetworkInterface.getNetworkInterfaces();

		while (allNetInterface.hasMoreElements()) {
			NetworkInterface nInterface = allNetInterface.nextElement();
			Enumeration<InetAddress> addressOfInterface = nInterface.getInetAddresses();
			while (addressOfInterface.hasMoreElements()) {
				InetAddress address = addressOfInterface.nextElement();
				if (address.getHostAddress().contains(seq)) {
					return address;
				}
			}
		}
		return null;
	}

}
