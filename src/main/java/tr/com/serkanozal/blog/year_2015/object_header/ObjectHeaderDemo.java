package tr.com.serkanozal.blog.year_2015.object_header;

import tr.com.serkanozal.jillegal.util.JvmUtil;

public class ObjectHeaderDemo {

	public static void main(String[] args) {
		Foo foo = new Foo();
		JvmUtil.dump(foo, 32);
	}
	
	static class Foo {
		
	}

}
