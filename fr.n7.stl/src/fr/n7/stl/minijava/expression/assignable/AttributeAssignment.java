package fr.n7.stl.minijava.expression.assignable;

import fr.n7.stl.minic.ast.expression.assignable.AssignableExpression;
import fr.n7.stl.minic.ast.expression.assignable.VariableAssignment;
import fr.n7.stl.minic.ast.instruction.declaration.VariableDeclaration;
import fr.n7.stl.minijava.expression.AbstractAttribute;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Library;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;

public class AttributeAssignment extends AbstractAttribute<AssignableExpression> implements AssignableExpression {

	public AttributeAssignment(AssignableExpression _object, String _name) {
		super(_object, _name);
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		Fragment result = _factory.createFragment();
		if (this.staticAccess) {
			result.add(_factory.createStore(Register.SB, this.attribute.getStaticOffset(), this.attribute.getType().length()));
			return result;
		}
		// Push the object reference (the address holding the attribute).
		if (this.object instanceof VariableAssignment) {
			VariableDeclaration vd = ((VariableAssignment) this.object).getDeclaration();
			result.add(_factory.createLoad(vd.getRegister(), vd.getOffset(), 1));
		} else if (this.object instanceof ThisAssignment) {
			ThisAssignment ta = (ThisAssignment) this.object;
			result.add(_factory.createLoad(Register.LB, ta.getDeclaration().getOffset(), 1));
		} else if (this.object instanceof SuperAssignment) {
			SuperAssignment sa = (SuperAssignment) this.object;
			result.add(_factory.createLoad(Register.LB, sa.getThisDeclaration().getOffset(), 1));
		} else {
			throw new UnsupportedOperationException(
				"Attribute assignment on " + this.object.getClass().getSimpleName() + " not supported yet.");
		}
		result.add(_factory.createLoadL(this.attribute.getOffset()));
		result.add(Library.IAdd);
		result.add(_factory.createStoreI(this.attribute.getType().length()));
		return result;
	}

}
