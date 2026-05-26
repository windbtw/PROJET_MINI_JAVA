package fr.n7.stl.minijava.ast.type.declaration;

import fr.n7.stl.minic.ast.type.Type;

public class AttributeDeclaration extends ClassElement {

	protected Type type;

	protected int offset;

	public AttributeDeclaration(String _name, Type _type) {
		super(_name);
		this.type = _type;
		this.offset = 0;
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

	public int getLength() {
		return this.type.length();
	}

	@Override
	public String toString() {
		return this.accessRight + " " + type + " " + this.name + ";\n";
	}
}
