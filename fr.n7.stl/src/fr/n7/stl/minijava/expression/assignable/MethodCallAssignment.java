package fr.n7.stl.minijava.expression.assignable;

import java.util.List;

import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minic.ast.expression.assignable.AssignableExpression;
import fr.n7.stl.minijava.expression.AbstractMethodCall;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.TAMFactory;

public class MethodCallAssignment extends AbstractMethodCall<AssignableExpression> implements AssignableExpression {

	public MethodCallAssignment(AssignableExpression _target, String _name, List<AccessibleExpression> _arguments) {
		super(_target, _name, _arguments);
	}

	public MethodCallAssignment(String _name, List<AccessibleExpression> _arguments) {
		this(null, _name, _arguments);
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		// Chained LHS like `a.m().x = ...` isn't supported in this step.
		throw new UnsupportedOperationException(
			"Assignment via method call result is not supported yet.");
	}

}
