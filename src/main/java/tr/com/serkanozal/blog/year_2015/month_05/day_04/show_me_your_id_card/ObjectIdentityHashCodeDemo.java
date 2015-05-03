package tr.com.serkanozal.blog.year_2015.month_05.day_04.show_me_your_id_card;

import sun.misc.Unsafe;
import tr.com.serkanozal.jillegal.util.JvmUtil;

// -XX:-UseCompressedOops
public class ObjectIdentityHashCodeDemo {

	private static final Unsafe UNSAFE = JvmUtil.getUnsafe();
	
	public static void main(String[] args) {
		Foo foo = new Foo();
		
		// "hashCode" field is empty and it is initialized when it is firstly accessed on demand
		foo.hashCode();
		
		if (JvmUtil.getReferenceSize() == 4) { // 32 bit JVM
			// | identity_hashcode:25 | age:4 | biased_lock:1 | lock:2 |
			long header = UNSAFE.getInt(foo, 0L);
			int identityHashCode = (int) (header >> 7);
			System.out.println("Identity hashcode from object header: " + identityHashCode);
		} else { // 64 bit JVM
			// | unused:25 | identity_hashcode:31 | unused:1 | age:4 | biased_lock:1 | lock:2 |
			long header = UNSAFE.getLong(foo, 0L);
			int identityHashCode = (int) ((header >> 8) & 0x7FFFFFFF); // UNSAFE.getInt(foo,  1) << 1 >>> 1;
			System.out.println("Identity hashcode from object header: " + identityHashCode);
		}
		
		System.out.println("System.identityHashCode(obj): " + System.identityHashCode(foo));
	}
	
	protected static class Foo {
		
	}
	
}
