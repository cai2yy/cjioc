package demo;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import iockids.Injector;

@Singleton
class Root {

	@Inject
	@Named("a")
	Node a;

	@Inject
	@Named("b")
	Node b;

	@Override
	public String toString() {
		return String.format("root(%s, %s)", a.name(), b.name());
	}

}

interface Node {

	String name();

}

interface Bode {

	String name();
}

@Singleton
@Named("a")
class NodeA implements Node {

	@Inject
	Leaf leaf;

	@Inject
	@Named("b")
	Node b;

	@Override
	public String name() {
		if (b == null)
			return String.format("nodeA(%s)", leaf);
		else
			return String.format("nodeAWithB(%s)", leaf);
	}

}

@Singleton
@Named("b")
class NodeB implements Node, Bode {

	Leaf leaf;

	@Inject
	@Named("a")
	Node a;

	@Inject
	public NodeB(Leaf leaf) {
		this.leaf = leaf;
	}

	@Override
	public String name() {
		if (a == null)
			return String.format("nodeB(%s)", leaf);
		else
			return String.format("nodeBWithA(%s)", leaf);
	}

}
@Singleton
@Named("Ia")
class Ia{
	Ib b;
	@Inject
	Ia(Ib b){
		this.b = b;
	}
}
@Singleton
@Named("Ib")
class Ib{
	Ia a;
	@Inject
	Ib(Ia a){
		this.a = a;
	}
}

class Leaf {

	@Inject
	Root root;

	int index;

	static int sequence;

	public Leaf() {
		index = sequence++;
	}

	public String toString() {
		if (root == null)
			return "leaf" + index;
		else
			return "leafwithroot" + index;
	}

}

public class Demo {

	public static void main(String[] args) throws ClassNotFoundException {
		var injector = new Injector();
		injector.scanInit();
		System.out.println("------------------");

		// 调用Bean，容器将递归进行依赖注入
		var root = injector.getInstance(Root.class);
		System.out.println(root);

		// Ia和Ib为循环依赖，创建失败
		//var ia = injector.getInstance(Ia.class);
		//System.out.println(ia);

	}

}
