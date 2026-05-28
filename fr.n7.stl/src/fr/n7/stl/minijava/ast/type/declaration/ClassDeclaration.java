package fr.n7.stl.minijava.ast.type.declaration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	/** Constructors of this class (indexed lookup by arity). */
	protected List<ConstructorDeclaration> constructors = new ArrayList<>();

	/** Resolved parent class (null if this class has no `extends`). */
	protected ClassDeclaration parent;

	/** Virtual Method Table holding ordered method declarations. */
	protected List<MethodDeclaration> vmt = new ArrayList<>();

	protected int vmtPointerOffset = -1;

	public int getVmtPointerOffset() {
		return this.vmtPointerOffset;
	}

	public void setVmtPointerOffset(int offset) {
		this.vmtPointerOffset = offset;
	}

	/** Guard to make allocateMemory recursion idempotent. */
	protected boolean memoryAllocated = false;

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

	public ClassDeclaration getParent() {
		return this.parent;
	}

	public boolean isConcrete() {
		return this.concrete;
	}

	/** Walks the class chain looking for an attribute or method by name. */
	public Declaration lookupMember(String memberName) {
		if (this.members != null && this.members.contains(memberName)) {
			return this.members.get(memberName);
		}
		if (this.parent != null) {
			return this.parent.lookupMember(memberName);
		}
		return null;
	}

	/** True if this class is `other` or a (transitive) subclass of it. */
	public boolean isSubclassOf(ClassDeclaration other) {
		ClassDeclaration c = this;
		while (c != null) {
			if (c == other) {
				return true;
			}
			c = c.parent;
		}
		return false;
	}

	/** Find a constructor whose user-arity matches (or null). */
	public ConstructorDeclaration findConstructor(int arity) {
		for (ConstructorDeclaration c : this.constructors) {
			if (c.getArity() == arity) {
				return c;
			}
		}
		return null;
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
			} else if (e instanceof ConstructorDeclaration) {
				ConstructorDeclaration c = (ConstructorDeclaration) e;
				if (!c.getName().equals(this.name)) {
					System.err.println("Constructor name " + c.getName() + " does not match class " + this.name + ".");
					ok = false;
				}
				if (this.findConstructor(c.getArity()) != null) {
					System.err.println("Duplicate constructor of arity " + c.getArity() + " in class " + this.name + ".");
					ok = false;
				}
				c.setOwner(this);
				this.constructors.add(c);
				ok &= c.collectAndPartialResolve(_scope);
			}
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
		if (this.ancestor != null) {
			Declaration d = _scope.get(this.ancestor);
			if (d instanceof ClassDeclaration) {
				this.parent = (ClassDeclaration) d;
				if (this.parent == this) {
					System.err.println("Class " + this.name + " cannot extend itself.");
					ok = false;
				}
			} else {
				System.err.println("Unknown parent class " + this.ancestor + " for " + this.name + ".");
				ok = false;
			}
		}
		for (ClassElement e : this.elements) {
			if (e instanceof AttributeDeclaration) {
				ok &= ((AttributeDeclaration) e).getType().completeResolve(_scope);
			} else if (e instanceof MethodDeclaration) {
				ok &= ((MethodDeclaration) e).completeResolve(_scope);
			} else if (e instanceof ConstructorDeclaration) {
				ok &= ((ConstructorDeclaration) e).completeResolve(_scope);
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
			} else if (e instanceof ConstructorDeclaration) {
				ok &= ((ConstructorDeclaration) e).checkType();
			}
		}
		if (this.concrete) {
			// Walk most-derived to ancestor: first definition wins per name.
			Map<String, MethodDeclaration> effective = new HashMap<>();
			ClassDeclaration c = this;
			while (c != null) {
				for (ClassElement e : c.elements) {
					if (e instanceof MethodDeclaration) {
						MethodDeclaration m = (MethodDeclaration) e;
						if (!effective.containsKey(m.getName())) {
							effective.put(m.getName(), m);
						}
					}
				}
				c = c.parent;
			}
			for (MethodDeclaration m : effective.values()) {
				if (!m.isConcrete()) {
					System.err.println("Concrete class " + this.name + " must implement abstract method " + m.getName() + ".");
					ok = false;
				}
			}
		}
		return ok;
	}

	public List<MethodDeclaration> getVmt() {
		return this.vmt;
	}

	@Override
	public int allocateMemory(Register _register, int _offset) {
		if (this.memoryAllocated) {
			return 0;
		}
		this.memoryAllocated = true;
		// Layout: VMT pointer at offset 0, then parent attributes, then own attributes.
		int current = 1;
		if (this.parent != null) {
			this.parent.allocateMemory(_register, _offset);
			current = this.parent.getObjectSize();
			this.vmt.addAll(this.parent.getVmt());
		}

		for (ClassElement e : this.elements) {
			if (e instanceof MethodDeclaration) {
				MethodDeclaration m = (MethodDeclaration) e;
				if (m.isStatic()) {
					continue;
				}
				boolean overrides = false;
				for (int i = 0; i < this.vmt.size(); i++) {
					if (this.vmt.get(i).getName().equals(m.getName())) {
						this.vmt.set(i, m);
						m.setVmtOffset(i);
						overrides = true;
						break;
					}
				}
				if (!overrides) {
					m.setVmtOffset(this.vmt.size());
					this.vmt.add(m);
				}
			}
		}

		for (ClassElement e : this.elements) {
			if (e instanceof AttributeDeclaration) {
				AttributeDeclaration a = (AttributeDeclaration) e;
				if (a.isStatic()) {
					continue;
				}
				a.setOffset(current);
				current += a.getLength();
			}
		}
		this.objectSize = current;
		for (ClassElement e : this.elements) {
			if (e instanceof MethodDeclaration) {
				((MethodDeclaration) e).allocateMemory(Register.LB, 0);
			} else if (e instanceof ConstructorDeclaration) {
				((ConstructorDeclaration) e).allocateMemory(Register.LB, 0);
			}
		}
		return 0;
	}

	/** Reserve SB-relative slots for this class' static attributes starting at {@code base}.
	 *  Returns the total number of words consumed. */
	public int allocateStaticMemory(int base) {
		int consumed = 0;
		for (ClassElement e : this.elements) {
			if (e instanceof AttributeDeclaration) {
				AttributeDeclaration a = (AttributeDeclaration) e;
				if (a.isStatic()) {
					a.setStaticOffset(base + consumed);
					consumed += a.getLength();
				}
			}
		}
		return consumed;
	}

	/** Emit initialization code for this class' static attributes (evaluated then stored). */
	public Fragment getStaticInitCode(TAMFactory _factory) {
		Fragment result = _factory.createFragment();
		for (ClassElement e : this.elements) {
			if (e instanceof AttributeDeclaration) {
				AttributeDeclaration a = (AttributeDeclaration) e;
				if (a.isStatic() && a.getInitializer() != null) {
					result.append(a.getInitializer().getCode(_factory));
					result.add(_factory.createStore(Register.SB, a.getStaticOffset(), a.getLength()));
				}
			}
		}
		return result;
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		Fragment result = _factory.createFragment();
		for (ClassElement e : this.elements) {
			if (e instanceof MethodDeclaration) {
				result.append(((MethodDeclaration) e).getCode(_factory));
			} else if (e instanceof ConstructorDeclaration) {
				result.append(((ConstructorDeclaration) e).getCode(_factory));
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
