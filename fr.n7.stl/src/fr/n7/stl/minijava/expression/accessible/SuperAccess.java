package fr.n7.stl.minijava.expression.accessible;

import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minijava.expression.AbstractSuper;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;

public class SuperAccess extends AbstractSuper<AccessibleExpression> implements AccessibleExpression {

	public SuperAccess() {
		super();
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		Fragment result = _factory.createFragment();
		// Same value as `this` — only the static type differs (to find parent's members
		// and trigger direct dispatch).
		result.add(_factory.createLoad(Register.LB, this.thisDeclaration.getOffset(), 1));
		return result;
	}

}
