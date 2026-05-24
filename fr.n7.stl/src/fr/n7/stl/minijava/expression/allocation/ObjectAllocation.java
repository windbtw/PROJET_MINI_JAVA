package fr.n7.stl.minijava.expression.allocation;

import java.util.Iterator;
import java.util.List;

import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minic.ast.expression.assignable.AssignableExpression;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minijava.ast.type.ClassType;
import fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration;
import fr.n7.stl.minijava.ast.type.declaration.ConstructorDeclaration;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Library;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;
import fr.n7.stl.util.Logger;

public class ObjectAllocation implements AccessibleExpression, AssignableExpression {

	protected String name;

	protected List<AccessibleExpression> arguments;

	protected ClassType type;

	protected ConstructorDeclaration constructor;

	public ObjectAllocation(String _name, List<AccessibleExpression> _arguments) {
		this.name = _name;
		this.arguments = _arguments;
		this.type = new ClassType(_name);
		this.constructor = null;
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		boolean ok = true;
		for (AccessibleExpression a : this.arguments) {
			ok &= a.collectAndPartialResolve(_scope);
		}
		return ok;
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		boolean ok = this.type.completeResolve(_scope);
		for (AccessibleExpression a : this.arguments) {
			ok &= a.completeResolve(_scope);
		}
		if (!ok) {
			return false;
		}
		ClassDeclaration cd = this.type.getDeclaration();
		if (cd == null) {
			Logger.error("Cannot allocate unknown class " + this.name);
			return false;
		}
		this.constructor = cd.findConstructor(this.arguments.size());
		if (this.constructor == null && this.arguments.size() > 0) {
			Logger.error("No constructor in " + this.name + " matches " + this.arguments.size() + " argument(s).");
			return false;
		}
		return true;
	}

	@Override
	public Type getType() {
		return this.type;
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		Fragment result = _factory.createFragment();
		ClassDeclaration cd = this.type.getDeclaration();
		result.add(_factory.createLoadL(cd.getObjectSize()));
		result.add(Library.MAlloc);
		if (this.constructor != null) {
			// Duplicate the reference: one copy is consumed by the constructor (as `this`),
			// the other remains as the result of `new`.
			result.add(_factory.createLoad(Register.ST, -1, 1));
			for (AccessibleExpression a : this.arguments) {
				result.append(a.getCode(_factory));
			}
			result.add(_factory.createCall(this.constructor.getFunction().getName(), Register.SB));
		}
		return result;
	}

	@Override
	public String toString() {
		String image = "";
		image += "new " + this.name + "( ";
		Iterator<AccessibleExpression> iterator = this.arguments.iterator();
		if (iterator.hasNext()) {
			AccessibleExpression argument = iterator.next();
			image += argument;
			while (iterator.hasNext()) {
				argument = iterator.next();
				image += " ," + argument;
			}
		}
		image += ")";
		return image;
	}

}
