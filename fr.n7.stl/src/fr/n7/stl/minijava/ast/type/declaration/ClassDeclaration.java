package fr.n7.stl.minijava.ast.type.declaration;

import java.util.List;

import fr.n7.stl.minic.ast.instruction.Instruction;
import fr.n7.stl.minic.ast.instruction.declaration.FunctionDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.scope.SymbolTable;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minijava.ast.type.ClassType;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;

public class ClassDeclaration implements Instruction, Declaration {

	protected List<ClassElement> elements;

	protected boolean concrete;

	protected String name;

	protected String ancestor;

	/** Internal scope holding the class members (attributes for now). */
	protected SymbolTable members;

	/** Total size in TAM words of an instance of this class. */
	protected int objectSize;

	public ClassDeclaration(boolean _concrete, String _name, String _ancestor, List<ClassElement> _elements) {
		this.concrete = _concrete;
		this.name = _name;
		this.ancestor = _ancestor;
		this.elements = _elements;
		this.objectSize = 0;
	}

	public ClassDeclaration(boolean _concrete, String _name, List<ClassElement> _elements) {
		this(_concrete, _name, null, _elements);
	}

	public int getObjectSize() {
		return this.objectSize;
	}

	public SymbolTable getMembers() {
		return this.members;
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		if (!_scope.accepts(this)) {
			System.err.println("Class " + this.name + " already declared.");
			return false;
		}
		_scope.register(this);
		this.members = new SymbolTable(_scope);
		boolean ok = true;
		for (ClassElement e : this.elements) {
			if (e instanceof AttributeDeclaration) {
				if (this.members.accepts(e)) {
					this.members.register(e);
				} else {
					System.err.println("Attribute " + e.getName() + " duplicated in class " + this.name + ".");
					ok = false;
				}
			} else if (e instanceof MethodDeclaration) {
				MethodDeclaration m = (MethodDeclaration) e;
				m.setOwner(this);
				if (this.members.accepts(m)) {
					this.members.register(m);
				} else {
					System.err.println("Method " + m.getName() + " duplicated in class " + this.name + ".");
					ok = false;
				}
				// Method body's local scope: parent = outer scope (so other classes / globals are visible).
				ok &= m.collectAndPartialResolve(_scope);
			}
			// Constructors: handled in a later step.
		}
		return ok;
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope, FunctionDeclaration _container) {
		return this.collectAndPartialResolve(_scope);
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		boolean ok = true;
		for (ClassElement e : this.elements) {
			if (e instanceof AttributeDeclaration) {
				ok &= ((AttributeDeclaration) e).getType().completeResolve(_scope);
			} else if (e instanceof MethodDeclaration) {
				ok &= ((MethodDeclaration) e).completeResolve(_scope);
			}
		}
		return ok;
	}

	@Override
	public boolean checkType() {
		boolean ok = true;
		for (ClassElement e : this.elements) {
			if (e instanceof MethodDeclaration) {
				ok &= ((MethodDeclaration) e).checkType();
			}
		}
		return ok;
	}

	@Override
	public int allocateMemory(Register _register, int _offset) {
		int current = 0;
		for (ClassElement e : this.elements) {
			if (e instanceof AttributeDeclaration) {
				AttributeDeclaration a = (AttributeDeclaration) e;
				a.setOffset(current);
				current += a.getLength();
			}
		}
		this.objectSize = current;
		for (ClassElement e : this.elements) {
			if (e instanceof MethodDeclaration) {
				((MethodDeclaration) e).allocateMemory(Register.LB, 0);
			}
		}
		return 0;
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		Fragment result = _factory.createFragment();
		for (ClassElement e : this.elements) {
			if (e instanceof MethodDeclaration) {
				result.append(((MethodDeclaration) e).getCode(_factory));
			}
		}
		return result;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Type getType() {
		return new ClassType(this.name);
	}

	@Override
	public String toString() {
		String image = "";
		if (!this.concrete) {
			image += "abstract ";
		}
		image += "class " + this.name + " ";
		if (this.ancestor != null) {
			image += "extends " + this.ancestor + " ";
		}
		image += "{\n";
		for (ClassElement e : this.elements) {
			image += e;
		}
		image += "}\n";
		return image;
	}

}
