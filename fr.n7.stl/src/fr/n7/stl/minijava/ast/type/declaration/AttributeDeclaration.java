package fr.n7.stl.minijava.ast.type.declaration;

import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minic.ast.type.Type;

public class AttributeDeclaration extends ClassElement {

	protected Type type;

	protected int offset;

	protected int staticOffset;

	protected AccessibleExpression initializer;

	protected ClassDeclaration owner;

	public AttributeDeclaration(String _name, Type _type) {
		super(_name);
		this.type = _type;
		this.offset = 0;
		this.staticOffset = -1;
		this.initializer = null;
	}

	public AttributeDeclaration(String _name, Type _type, AccessibleExpression _initializer) {
		this(_name, _type);
		this.initializer = _initializer;
	}

	@Override
	public Type getType() {
		return this.type;
	}

	public int getOffset() {
		return this.offset;
	}

	public void setOffset(int _offset) {
		this.offset = _offset;
	}

	public int getStaticOffset() {
		return this.staticOffset;
	}

	public void setStaticOffset(int _offset) {
		this.staticOffset = _offset;
	}

	public AccessibleExpression getInitializer() {
		return this.initializer;
	}

	public ClassDeclaration getOwner() {
		return this.owner;
	}

	public void setOwner(ClassDeclaration _owner) {
		this.owner = _owner;
	}

	public boolean isStatic() {
		return this.elementKind == ElementKind.CLASS;
	}

	public int getLength() {
		return this.type.length();
	}

	@Override
	public String toString() {
		String prefix = (this.isStatic() ? "static " : "");
		String suffix = (this.initializer != null ? " = " + this.initializer : "");
		return this.accessRight + " " + prefix + type + " " + this.name + suffix + ";\n";
	}
}
