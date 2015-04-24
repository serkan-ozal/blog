package tr.com.serkanozal.blog.year_2015.object_header;

import sun.misc.Unsafe;
import tr.com.serkanozal.jillegal.util.JvmUtil;

@SuppressWarnings("restriction")
public abstract class ObjectHeaderDemo {

	protected static final Unsafe UNSAFE = JvmUtil.getUnsafe();
	
	protected static class Foo {
		
	}

}
