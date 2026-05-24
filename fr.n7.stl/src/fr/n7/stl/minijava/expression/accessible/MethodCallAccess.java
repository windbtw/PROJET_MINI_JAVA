package fr.n7.stl.minijava.expression.accessible;

import java.util.List;

import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minijava.ast.type.ClassType;
import fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration;
import fr.n7.stl.minijava.expression.AbstractMethodCall;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Library;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;

public class MethodCallAccess extends AbstractMethodCall<AccessibleExpression> implements AccessibleExpression {

	public MethodCallAccess(AccessibleExpression _target, String _name, List<AccessibleExpression> _arguments) {
		super(_target, _name, _arguments);
	}

	public MethodCallAccess(String _name, List<AccessibleExpression> _arguments) {
		super(_name, _arguments);
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		Fragment result = _factory.createFragment();
		// Push `this`, then user args.
		result.append(this.target.getCode(_factory));
		int argsSize = 0;
		for (AccessibleExpression a : this.arguments) {
			result.append(a.getCode(_factory));
			argsSize += a.getType().length();
		}
		if (this.target instanceof SuperAccess) {
			// Direct (non-virtual) call to the resolved parent method.
			result.add(_factory.createCall(this.declaration.getFunction().getName(), Register.SB));
		} else {
			// Indirect call via vtable: look up obj[0][slot] and CALLI.
			ClassDeclaration staticClass = ((ClassType) this.target.getType()).getDeclaration();
			int slot = staticClass.getMethodSlot(this.name);
			result.add(_factory.createLoad(Register.ST, -(1 + argsSize), 1));
			result.add(_factory.createLoadI(1));
			result.add(_factory.createLoadL(slot));
			result.add(Library.IAdd);
			result.add(_factory.createLoadI(1));
			result.add(_factory.createCallI());
		}
		return result;
	}

}
