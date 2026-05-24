package fr.n7.stl.minijava.instruction;

import java.util.Iterator;
import java.util.List;

import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minic.ast.instruction.Instruction;
import fr.n7.stl.minic.ast.instruction.declaration.FunctionDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minijava.ast.type.ClassType;
import fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration;
import fr.n7.stl.minijava.ast.type.declaration.MethodDeclaration;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;
import fr.n7.stl.util.Logger;

public class MethodCall implements Instruction {

	protected AccessibleExpression target;

	protected String name;

	protected MethodDeclaration method;

	protected List<AccessibleExpression> arguments;

	public MethodCall(AccessibleExpression _target, String _name, List<AccessibleExpression> _arguments) {
		this.name = _name;
		this.method = null;
		this.target = _target;
		this.arguments = _arguments;
	}

	public MethodCall(String _name, List<AccessibleExpression> _arguments) {
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
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope, FunctionDeclaration _container) {
		return this.collectAndPartialResolve(_scope);
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		if (this.target == null) {
			Logger.error("Implicit method calls (without target) are not supported yet.");
			return false;
		}
		boolean ok = this.target.completeResolve(_scope);
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
		Declaration d = cd.getMembers().get(this.name);
		if (!(d instanceof MethodDeclaration)) {
			Logger.error("Method " + this.name + " not found in class " + cd.getName());
			return false;
		}
		this.method = (MethodDeclaration) d;
		return true;
	}

	@Override
	public boolean checkType() {
		return true;
	}

	@Override
	public int allocateMemory(Register _register, int _offset) {
		return 0;
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		Fragment result = _factory.createFragment();
		result.append(this.target.getCode(_factory));
		for (AccessibleExpression a : this.arguments) {
			result.append(a.getCode(_factory));
		}
		result.add(_factory.createCall(this.method.getFunction().getName(), Register.SB));
		// Discard return value when used as an instruction.
		int returnSize = this.method.getType().length();
		if (returnSize > 0) {
			result.add(_factory.createPop(0, returnSize));
		}
		return result;
	}

	@Override
	public String toString() {
		String image = "";
		if (this.target != null) {
			image += target + ".";
		}
		image += this.name;
		image += "( ";
		Iterator<AccessibleExpression> iterator = this.arguments.iterator();
		if (iterator.hasNext()) {
			AccessibleExpression argument = iterator.next();
			image += argument;
			while (iterator.hasNext()) {
				argument = iterator.next();
				image += " ," + argument;
			}
		}
		image += ");\n";
		return image;
	}

}
