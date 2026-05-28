package fr.n7.stl.minic.ast.expression.accessible;

import fr.n7.stl.minic.ast.expression.AbstractPointer;
import fr.n7.stl.minic.ast.type.PointerType;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.TAMFactory;

public class PointerAccess extends AbstractPointer<AccessibleExpression> implements AccessibleExpression {

	public PointerAccess(AccessibleExpression _pointer) {
		super(_pointer);
	}

	@Override
	public String toString() {
		return "* " + this.pointer.toString();
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		int _pointedSize = ((PointerType) this.pointer.getType()).getPointedType().length();
		Fragment _result = this.pointer.getCode(_factory);
		_result.add(_factory.createLoadI(_pointedSize));
		return _result;
	}

}
