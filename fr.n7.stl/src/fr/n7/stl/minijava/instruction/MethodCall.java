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
import fr.n7.stl.minijava.expression.accessible.SuperAccess;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Library;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;
import fr.n7.stl.util.Logger;

public class MethodCall implements Instruction {

	protected AccessibleExpression target;

	protected String name;

	protected MethodDeclaration method;

	protected List<AccessibleExpression> arguments;

	protected String receiverIdentifier;

	protected boolean staticCall;

	public MethodCall(AccessibleExpression _target, String _name, List<AccessibleExpression> _arguments) {
		this.name = _name;
		this.method = null;
		this.target = _target;
		this.arguments = _arguments;
	}

	public MethodCall(String _name, List<AccessibleExpression> _arguments) {
		this(null, _name, _arguments);
	}

	public void setReceiverIdentifier(String _name) {
		this.receiverIdentifier = _name;
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
		if (this.receiverIdentifier != null && _scope.knows(this.receiverIdentifier)) {
			Declaration rd = _scope.get(this.receiverIdentifier);
			if (rd instanceof ClassDeclaration) {
				ClassDeclaration cd = (ClassDeclaration) rd;
				boolean ok = true;
				for (AccessibleExpression a : this.arguments) {
					ok &= a.completeResolve(_scope);
				}
				if (!ok) {
					return false;
				}
				Declaration d = cd.lookupMember(this.name);
				if (!(d instanceof MethodDeclaration)) {
					Logger.error("Method " + this.name + " not found in class " + cd.getName());
					return false;
				}
				MethodDeclaration md = (MethodDeclaration) d;
				if (!md.isStatic()) {
					Logger.error("Method " + this.name + " of class " + cd.getName() + " is not static.");
					return false;
				}
				this.method = md;
				this.staticCall = true;
				return true;
			}
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
		Declaration d = cd.lookupMember(this.name);
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
		if (this.staticCall) {
			for (AccessibleExpression a : this.arguments) {
				result.append(a.getCode(_factory));
			}
			result.add(_factory.createCall(this.method.getFunction().getName(), Register.SB));
			int returnSize = this.method.getType().length();
			if (returnSize > 0) {
				result.add(_factory.createPop(0, returnSize));
			}
			return result;
		}
		result.append(this.target.getCode(_factory));
		for (AccessibleExpression a : this.arguments) {
			result.append(a.getCode(_factory));
		}

		if (this.target instanceof SuperAccess) {
			result.add(_factory.createCall(this.method.getFunction().getName(), Register.SB));
		} else {
			int argsSize = 0;
			for (AccessibleExpression a : this.arguments) {
				argsSize += a.getType().length();
			}
			result.add(_factory.createLoadL(0)); // Push static link
			result.add(_factory.createLoad(Register.ST, -argsSize - 2, 1)); // duplicate target
			result.add(_factory.createLoadI(1));
			result.add(_factory.createLoadL(this.method.getVmtOffset()));
			result.add(Library.IAdd);
			result.add(_factory.createLoadI(1));
			result.add(_factory.createCallI());
		}

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
