package fr.n7.stl.minijava.expression.accessible;

import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minijava.expression.AbstractAttribute;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Library;
import fr.n7.stl.tam.ast.TAMFactory;

public class AttributeAccess extends AbstractAttribute<AccessibleExpression> implements AccessibleExpression {

	public AttributeAccess(AccessibleExpression _object, String _name) {
		super(_object, _name);
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		Fragment result = this.object.getCode(_factory);
		result.add(_factory.createLoadL(this.attribute.getOffset()));
		result.add(Library.IAdd);
		result.add(_factory.createLoadI(this.attribute.getType().length()));
		return result;
	}

}
