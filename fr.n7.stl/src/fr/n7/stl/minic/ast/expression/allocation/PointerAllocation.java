package fr.n7.stl.minic.ast.expression.allocation;

import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minic.ast.expression.assignable.AssignableExpression;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.PointerType;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Library;
import fr.n7.stl.tam.ast.TAMFactory;

public class PointerAllocation implements AccessibleExpression, AssignableExpression {

	protected Type element;

	public PointerAllocation(Type _element) {
		this.element = _element;
	}

	@Override
	public String toString() {
		return "new " + this.element;
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		return true;
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		return this.element.completeResolve(_scope);
	}

	@Override
	public Type getType() {
		return new PointerType(this.element);
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		Fragment _result = _factory.createFragment();
		_result.add(_factory.createLoadL(this.element.length()));
		_result.add(Library.MAlloc);
		return _result;
	}

}
