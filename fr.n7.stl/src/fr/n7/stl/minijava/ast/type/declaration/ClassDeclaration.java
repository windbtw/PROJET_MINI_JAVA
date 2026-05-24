package fr.n7.stl.minijava.ast.type.declaration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import fr.n7.stl.minic.ast.instruction.Instruction;
import fr.n7.stl.minic.ast.instruction.declaration.FunctionDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.scope.SymbolTable;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minijava.ast.type.ClassType;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Library;
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

	/** Guard to make allocateMemory recursion idempotent. */
	protected boolean memoryAllocated = false;

	/**
	 * Virtual-method table: ordered map from method name → method.
	 * Inherits parent's entries first (same slot indices), then own methods
	 * overwrite or append. Iteration order = slot order.
	 */
	protected LinkedHashMap<String, MethodDeclaration> vtable = new LinkedHashMap<>();

	/** SB-relative offset of the slot that holds the heap address of this class's vtable. */
	protected int vtableSBOffset;

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

	public int getVtableSBOffset() {
		return this.vtableSBOffset;
	}

	public void setVtableSBOffset(int o) {
		this.vtableSBOffset = o;
	}

	public int getVtableSize() {
		return this.vtable.size();
	}

	/** Slot index of a method in this class's vtable (-1 if not found). */
	public int getMethodSlot(String memberName) {
		int i = 0;
		for (String k : this.vtable.keySet()) {
			if (k.equals(memberName)) {
				return i;
			}
			i++;
		}
		return -1;
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
		return ok;
	}

	@Override
	public int allocateMemory(Register _register, int _offset) {
		if (this.memoryAllocated) {
			return 0;
		}
		this.memoryAllocated = true;
		// Layout: parent attributes first, then own attributes.
		int current = 0;
		if (this.parent != null) {
			this.parent.allocateMemory(_register, _offset);
			current = this.parent.getObjectSize();
		}
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
			} else if (e instanceof ConstructorDeclaration) {
				((ConstructorDeclaration) e).allocateMemory(Register.LB, 0);
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
			} else if (e instanceof ConstructorDeclaration) {
				result.append(((ConstructorDeclaration) e).getCode(_factory));
			}
		}
		return result;
	}

	/**
	 * Code that builds this class's vtable on the heap and stores its address in SB[vtableSBOffset].
	 * Run once at program startup, before main.
	 */
	public Fragment getVTableInitCode(TAMFactory _factory) {
		Fragment result = _factory.createFragment();
		if (this.vtable.isEmpty()) {
			return result;
		}
		// vtable_addr = MAlloc(vtable.size)
		result.add(_factory.createLoadL(this.vtable.size()));
		result.add(Library.MAlloc);
		result.add(_factory.createStore(Register.SB, this.vtableSBOffset, 1));
		// For each slot: vtable[slot] = code address of the method bound to that slot.
		int slot = 0;
		for (MethodDeclaration m : this.vtable.values()) {
			result.add(_factory.createLoadA(m.getFunction().getName()));
			result.add(_factory.createLoad(Register.SB, this.vtableSBOffset, 1));
			result.add(_factory.createLoadL(slot));
			result.add(Library.IAdd);
			result.add(_factory.createStoreI(1));
			slot++;
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
