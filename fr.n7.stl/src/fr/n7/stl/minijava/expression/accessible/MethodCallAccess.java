package fr.n7.stl.minijava.expression.accessible;

import java.util.List;

import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minijava.expression.AbstractMethodCall;
import fr.n7.stl.tam.ast.Fragment;
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
		// Calling convention: push `this` first, then user args, then CALL.
		result.append(this.target.getCode(_factory));
		for (AccessibleExpression a : this.arguments) {
			result.append(a.getCode(_factory));
		}
		result.add(_factory.createCall(this.declaration.getFunction().getName(), Register.SB));
		return result;
	}

}
