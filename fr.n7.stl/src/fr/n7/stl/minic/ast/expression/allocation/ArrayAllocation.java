package fr.n7.stl.minic.ast.expression.allocation;

import fr.n7.stl.minic.ast.expression.Expression;
import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minic.ast.expression.assignable.AssignableExpression;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.ArrayType;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Library;
import fr.n7.stl.tam.ast.TAMFactory;

public class ArrayAllocation implements AccessibleExpression, AssignableExpression {

	protected Type element;
	protected Expression size;

	public ArrayAllocation(Type _element, Expression _size) {
		this.element = _element;
		this.size = _size;
	}

	@Override
	public String toString() {
		return "new " + this.element + "[ " + this.size + " ]";
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		return this.size.collectAndPartialResolve(_scope);
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		boolean _ok = this.size.completeResolve(_scope);
		_ok &= this.element.completeResolve(_scope);
		return _ok;
	}

	@Override
	public Type getType() {
		return new ArrayType(this.element);
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		Fragment _result = this.size.getCode(_factory);
		_result.add(_factory.createLoadL(this.element.length()));
		_result.add(Library.IMul);
		_result.add(Library.MAlloc);
		return _result;
	}

}
