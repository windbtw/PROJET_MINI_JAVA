package fr.n7.stl.minic.ast.expression.accessible;

import fr.n7.stl.minic.ast.expression.AbstractArray;
import fr.n7.stl.minic.ast.type.ArrayType;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Library;
import fr.n7.stl.tam.ast.TAMFactory;

public class ArrayAccess extends AbstractArray<AccessibleExpression> implements AccessibleExpression {

	public ArrayAccess(AccessibleExpression _array, AccessibleExpression _index) {
		super(_array, _index);
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		int _elementSize = ((ArrayType) this.array.getType()).getType().length();
		Fragment _result = this.array.getCode(_factory);
		_result.append(this.index.getCode(_factory));
		_result.add(_factory.createLoadL(_elementSize));
		_result.add(Library.IMul);
		_result.add(Library.IAdd);
		_result.add(_factory.createLoadI(_elementSize));
		return _result;
	}

}
