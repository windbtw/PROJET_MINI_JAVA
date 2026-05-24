package fr.n7.stl.minijava.instruction;

import java.util.Iterator;
import java.util.List;

import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minic.ast.instruction.Instruction;
import fr.n7.stl.minic.ast.instruction.declaration.FunctionDeclaration;
import fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minijava.ast.type.ClassType;
import fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration;
import fr.n7.stl.minijava.ast.type.declaration.ConstructorDeclaration;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;
import fr.n7.stl.util.Logger;

public class ThisCall implements Instruction {

	protected ConstructorDeclaration constructor;

	protected List<AccessibleExpression> arguments;

	protected ParameterDeclaration thisDeclaration;

	public ThisCall(List<AccessibleExpression> _arguments) {
		this.arguments = _arguments;
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		boolean ok = true;
		Declaration d = _scope.get("this");
		if (!(d instanceof ParameterDeclaration)) {
			Logger.error("'this(...)' used outside a constructor body.");
			return false;
		}
		this.thisDeclaration = (ParameterDeclaration) d;
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
		boolean ok = true;
		for (AccessibleExpression a : this.arguments) {
			ok &= a.completeResolve(_scope);
		}
		if (!ok) {
			return false;
		}
		Type t = this.thisDeclaration.getType();
		if (!(t instanceof ClassType)) {
			Logger.error("'this' has non-class type, cannot resolve 'this(...)'.");
			return false;
		}
		ClassDeclaration current = ((ClassType) t).getDeclaration();
		if (current == null) {
			Logger.error("Enclosing class not resolved for 'this(...)'.");
			return false;
		}
		this.constructor = current.findConstructor(this.arguments.size());
		if (this.constructor == null) {
			Logger.error("No constructor in " + current.getName() + " matches " + this.arguments.size() + " argument(s).");
			return false;
		}
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
		result.add(_factory.createLoad(Register.LB, this.thisDeclaration.getOffset(), 1));
		for (AccessibleExpression a : this.arguments) {
			result.append(a.getCode(_factory));
		}
		result.add(_factory.createCall(this.constructor.getFunction().getName(), Register.SB));
		return result;
	}

	@Override
	public String toString() {
		String image = "";
		image += "this( ";
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
