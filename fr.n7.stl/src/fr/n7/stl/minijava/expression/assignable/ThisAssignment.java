package fr.n7.stl.minijava.expression.assignable;

import fr.n7.stl.minic.ast.expression.assignable.AssignableExpression;
import fr.n7.stl.minijava.expression.AbstractThis;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.TAMFactory;

public class ThisAssignment extends AbstractThis<AssignableExpression> implements AssignableExpression {

	public ThisAssignment() {
		super();
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		// `this = ...` is not a meaningful assignment in our subset. Use `this.x = ...` instead.
		throw new UnsupportedOperationException("Assignment to 'this' is not supported.");
	}

}
