package fr.n7.stl.minijava.expression.assignable;

import fr.n7.stl.minic.ast.expression.assignable.AssignableExpression;
import fr.n7.stl.minic.ast.expression.assignable.VariableAssignment;
import fr.n7.stl.minic.ast.instruction.declaration.VariableDeclaration;
import fr.n7.stl.minijava.expression.AbstractAttribute;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Library;
import fr.n7.stl.tam.ast.TAMFactory;

public class AttributeAssignment extends AbstractAttribute<AssignableExpression> implements AssignableExpression {

	public AttributeAssignment(AssignableExpression _object, String _name) {
		super(_object, _name);
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		Fragment result = _factory.createFragment();
		// Step 4 supports only simple `var.attr = ...` (object is a plain variable).
		// Chained `a.b.c = ...` will come in a later step.
		if (this.object instanceof VariableAssignment) {
			VariableDeclaration vd = ((VariableAssignment) this.object).getDeclaration();
			result.add(_factory.createLoad(vd.getRegister(), vd.getOffset(), 1));
		} else {
			throw new UnsupportedOperationException(
				"Attribute assignment is only supported on plain variables for now.");
		}
		result.add(_factory.createLoadL(this.attribute.getOffset()));
		result.add(Library.IAdd);
		result.add(_factory.createStoreI(this.attribute.getType().length()));
		return result;
	}

}
