package fr.n7.stl.minic.ast.expression.assignable;

import fr.n7.stl.minic.ast.expression.AbstractPointer;
import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minic.ast.type.PointerType;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.TAMFactory;

public class PointerAssignment extends AbstractPointer<AccessibleExpression> implements AssignableExpression {

	public PointerAssignment(AccessibleExpression _pointer) {
		super(_pointer);
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		int _pointedSize = ((PointerType) this.pointer.getType()).getPointedType().length();
		Fragment _result = this.pointer.getCode(_factory);
		_result.add(_factory.createStoreI(_pointedSize));
		return _result;
	}

}
