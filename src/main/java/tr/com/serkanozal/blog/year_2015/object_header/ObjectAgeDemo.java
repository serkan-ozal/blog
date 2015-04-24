package tr.com.serkanozal.blog.year_2015.object_header;

import tr.com.serkanozal.jillegal.util.JvmUtil;

@SuppressWarnings("restriction")
// -XX:-UseCompressedOops -XX:+PrintGCDetails -XX:+PrintTenuringDistribution -Xms1g -Xmx1g
public class ObjectAgeDemo extends ObjectHeaderDemo {

	public static void main(String[] args) {
		Foo foo = new Foo();
		
		printAddressAndAge(foo);

		Object[] array = new Object[2000000];
		
		for (int i = 0; i < 16; i++) {
			printAddressAndAge(foo);
			int iterationCount = 10 * array.length;
			for (int j = 0; j < iterationCount; j++) {
				array[j % array.length] = new Foo();
			}
		}
	}
	
	private static void printAddressAndAge(Foo foo) {
		System.out.println("Address of object: " + JvmUtil.toHexAddress(JvmUtil.addressOf(foo)));
		if (JvmUtil.getReferenceSize() == 4) { // 32 bit JVM
			// | identity_hashcode:25 | age:4 | biased_lock:1 | lock:2 |
			long header = UNSAFE.getLong(foo, 0L);
			int age = (int) ((header >> 3) & 0x0000000F);
			System.out.println("Age: " + age);
		} else { // 64 bit JVM
			// | unused:25 | identity_hashcode:31 | unused:1 | age:4 | biased_lock:1 | lock:2 |
			long header = UNSAFE.getLong(foo, 0L);
			int age = (int) ((header >> 3) & 0x0000000F);
			System.out.println("Age: " + age);
		}
	}
	
}
