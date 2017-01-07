package himabindu.example.com.linegame;

// this class jni interface to get the system information

public class JNISysInfo {
	static {
		System.loadLibrary("LG_SysInfo");
		}
	
	public native int getPowerUsage();
	public native int getStorageSpace();
}
