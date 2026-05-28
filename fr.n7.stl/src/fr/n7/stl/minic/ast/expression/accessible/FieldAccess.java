package fr.n7.stl.minic.ast.expression.accessible;

import fr.n7.stl.minic.ast.expression.AbstractField;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.TAMFactory;

public class FieldAccess extends AbstractField<AccessibleExpression> implements AccessibleExpression {

	public FieldAccess(AccessibleExpression _record, String _name) {
		super(_record, _name);
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		Fragment _result = this.record.getCode(_factory);
		int _recordSize = this.record.getType().length();
		int _fieldSize = this.field.getType().length();
		int _tailSize = _recordSize - this.field.getOffset() - _fieldSize;
		if (_tailSize > 0) {
			_result.add(_factory.createPop(0, _tailSize));
		}
		if (this.field.getOffset() > 0) {
			_result.add(_factory.createPop(_fieldSize, this.field.getOffset()));
		}
		return _result;
	}

}
