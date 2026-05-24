package fr.n7.stl.minijava.ast.type;

import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration;

public class ClassType implements Type {

	protected String name;

	protected ClassDeclaration declaration;

	public ClassType(String _name) {
		this.name = _name;
		this.declaration = null;
	}

	public String getName() {
		return this.name;
	}

	public ClassDeclaration getDeclaration() {
		return this.declaration;
	}

	@Override
	public boolean equalsTo(Type _other) {
		if (_other instanceof ClassType) {
			return this.name.equals(((ClassType) _other).name);
		}
		return false;
	}

	@Override
	public boolean compatibleWith(Type _other) {
		// Inheritance handled in a later step; for now strict equality.
		return this.equalsTo(_other);
	}

	@Override
	public Type merge(Type _other) {
		if (this.equalsTo(_other)) {
			return this;
		}
		return null;
	}

	@Override
	public int length() {
		// A variable of class type holds an object reference (1 word).
		// Use declaration.getObjectSize() if you need the full object size.
		return 1;
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		if (!_scope.knows(this.name)) {
			System.err.println("Unknown class type: " + this.name);
			return false;
		}
		Declaration d = _scope.get(this.name);
		if (!(d instanceof ClassDeclaration)) {
			System.err.println(this.name + " is not a class.");
			return false;
		}
		this.declaration = (ClassDeclaration) d;
		return true;
	}

	public String toString() {
		return " " + this.name + " ";
	}

}
