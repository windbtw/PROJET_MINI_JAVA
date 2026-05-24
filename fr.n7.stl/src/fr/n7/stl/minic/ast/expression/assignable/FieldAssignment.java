package fr.n7.stl.minic.ast.expression.assignable;

import fr.n7.stl.minic.ast.expression.AbstractField;
import fr.n7.stl.minic.ast.instruction.declaration.VariableDeclaration;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.TAMFactory;

public class FieldAssignment extends AbstractField<AssignableExpression> implements AssignableExpression {

	public FieldAssignment(AssignableExpression _record, String _name) {
		super(_record, _name);
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		Fragment _result = _factory.createFragment();
		if (this.record instanceof VariableAssignment) {
			VariableDeclaration _decl = ((VariableAssignment) this.record).declaration;
			_result.add(_factory.createStore(
				_decl.getRegister(),
				_decl.getOffset() + this.field.getOffset(),
				this.field.getType().length()));
		}
		return _result;
	}

}
