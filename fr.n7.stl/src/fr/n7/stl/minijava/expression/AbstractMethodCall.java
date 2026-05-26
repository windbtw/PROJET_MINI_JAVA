package fr.n7.stl.minijava.expression;

import java.util.Iterator;
import java.util.List;

import fr.n7.stl.minic.ast.expression.Expression;
import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minijava.ast.type.ClassType;
import fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration;
import fr.n7.stl.minijava.ast.type.declaration.MethodDeclaration;
import fr.n7.stl.util.Logger;

public abstract class AbstractMethodCall<ObjectKind extends Expression> implements Expression {

	protected String name;

	protected MethodDeclaration declaration;

	protected ObjectKind target;

	protected List<AccessibleExpression> arguments;

	public AbstractMethodCall(ObjectKind _target, String _name, List<AccessibleExpression> _arguments) {
		this.target = _target;
		this.name = _name;
		this.arguments = _arguments;
	}

	public AbstractMethodCall(String _name, List<AccessibleExpression> _arguments) {
		this(null, _name, _arguments);
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		boolean ok = true;
		if (this.target != null) {
			ok &= this.target.collectAndPartialResolve(_scope);
		}
		for (AccessibleExpression a : this.arguments) {
			ok &= a.collectAndPartialResolve(_scope);
		}
		return ok;
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		boolean ok = true;
		if (this.target == null) {
			Logger.error("Implicit method calls (without target) are not supported yet.");
			return false;
		}
		ok &= this.target.completeResolve(_scope);
		for (AccessibleExpression a : this.arguments) {
			ok &= a.completeResolve(_scope);
		}
		if (!ok) {
			return false;
		}
		Type t = this.target.getType();
		if (!(t instanceof ClassType)) {
			Logger.error("Method call on non-class type: " + t);
			return false;
		}
		ClassDeclaration cd = ((ClassType) t).getDeclaration();
		if (cd == null) {
			Logger.error("Class type for method call is not resolved.");
			return false;
		}
		Declaration d = cd.lookupMember(this.name);
		if (!(d instanceof MethodDeclaration)) {
			Logger.error("Method " + this.name + " not found in class " + cd.getName());
			return false;
		}
		this.declaration = (MethodDeclaration) d;
		return true;
	}

	@Override
	public Type getType() {
		return this.declaration.getType();
	}

	@Override
	public String toString() {
		String image = "";
		if (this.target != null) {
			image += this.target + ".";
		}
		image += this.name + "( ";
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
