package tr.com.serkanozal.blog.year_2015.month_05.day_04.show_me_your_id_card;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import sun.misc.Unsafe;
import tr.com.serkanozal.jillegal.util.JvmUtil;

// -XX:-UseCompressedOops
public class RandomNumberGeneratorWithObjectIdentityHashCodeDemo {

	private static final Unsafe UNSAFE = JvmUtil.getUnsafe();
	private static final int WARM_UP_RANDOM_NUMBER_COUNT = 1_000_000;
	private static final int RANDOM_NUMBER_COUNT = WARM_UP_RANDOM_NUMBER_COUNT * 100;
	private static final Random RANDOM = new Random();
	private static final ThreadLocalRandom THREAD_LOCAL_RANDOM = ThreadLocalRandom.current();
	
	public static void main(String[] args) {
		long start, finish;
		
		// Warm-up
		generateNumbersWithRandom(WARM_UP_RANDOM_NUMBER_COUNT);
		generateNumbersWithThreadLocalRandom(WARM_UP_RANDOM_NUMBER_COUNT);
		generateNumbersWithObjectIdentityHashCode(WARM_UP_RANDOM_NUMBER_COUNT);
		
		start = System.currentTimeMillis();
		generateNumbersWithRandom(RANDOM_NUMBER_COUNT);
		finish = System.currentTimeMillis();
		System.out.println("Generating random numbers with 'java.util.Random' finished in " + 
				(finish - start) + " milliseconds ...");
		
		start = System.currentTimeMillis();
		generateNumbersWithThreadLocalRandom(RANDOM_NUMBER_COUNT);
		finish = System.currentTimeMillis();
		System.out.println("Generating random numbers with 'java.util.concurrent.ThreadLocalRandom' finished in " + 
				(finish - start) + " milliseconds ...");
		
		start = System.currentTimeMillis();
		generateNumbersWithObjectIdentityHashCode(RANDOM_NUMBER_COUNT);
		finish = System.currentTimeMillis();
		System.out.println("Generating random numbers by resetting identity hashcode of object finished in " + 
				(finish - start) + " milliseconds ...");
	}
	
	protected static class Foo {
		
	}
	
	private static void generateNumbersWithRandom(int numberCount) {
		for (int i = 0; i < numberCount; i++) {
			RANDOM.nextInt();
		}
	}
	
	private static void generateNumbersWithThreadLocalRandom(int numberCount) {
		for (int i = 0; i < numberCount; i++) {
			THREAD_LOCAL_RANDOM.nextInt();
		}
	}
	
	private static void generateNumbersWithObjectIdentityHashCode(int numberCount) {
		Foo foo = new Foo();
		if (JvmUtil.getReferenceSize() == 4) { // 32 bit JVM
			// | identity_hashcode:25 | age:4 | biased_lock:1 | lock:2 |
			for (int i = 0; i < numberCount; i++) {
				foo.hashCode();
				// Not set to 0x00. Because if the last 2 bits is set to zero, it means object is "Lightweight Locked".
				// In this case the second call of this line will wait forever.
				UNSAFE.putInt(foo, 0L, 0x01);
			}
		} else { // 64 bit JVM
			// | unused:25 | identity_hashcode:31 | unused:1 | age:4 | biased_lock:1 | lock:2 |
			for (int i = 0; i < numberCount; i++) {
				foo.hashCode();
				// Not set to 0x00. Because if the last 2 bits is set to zero, it means object is "Lightweight Locked".
				// In this case the second call of this line will wait forever.
				UNSAFE.putLong(foo, 0L, 0x01);
			}
		}
	}
	
}
