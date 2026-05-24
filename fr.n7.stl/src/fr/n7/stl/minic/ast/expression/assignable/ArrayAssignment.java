package fr.n7.stl.minic.ast.expression.assignable;

import fr.n7.stl.minic.ast.expression.AbstractArray;
import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minic.ast.instruction.declaration.VariableDeclaration;
import fr.n7.stl.minic.ast.type.ArrayType;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Library;
import fr.n7.stl.tam.ast.TAMFactory;

public class ArrayAssignment extends AbstractArray<AssignableExpression> implements AssignableExpression {

	public ArrayAssignment(AssignableExpression _array, AccessibleExpression _index) {
		super(_array, _index);
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		int _elementSize = ((ArrayType) this.array.getType()).getType().length();
		Fragment _result = _factory.createFragment();
		if (this.array instanceof VariableAssignment) {
			VariableDeclaration _decl = ((VariableAssignment) this.array).declaration;
			_result.add(_factory.createLoad(_decl.getRegister(), _decl.getOffset(), 1));
		} else {
			_result.append(this.array.getCode(_factory));
		}
		_result.append(this.index.getCode(_factory));
		_result.add(_factory.createLoadL(_elementSize));
		_result.add(Library.IMul);
		_result.add(Library.IAdd);
		_result.add(_factory.createStoreI(_elementSize));
		return _result;
	}

}
