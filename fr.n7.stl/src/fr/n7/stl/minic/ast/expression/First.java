package fr.n7.stl.minic.ast.expression;

import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.CoupleType;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.TAMFactory;

public class First implements AccessibleExpression {

	protected AccessibleExpression target;

	public First(AccessibleExpression _target) {
		this.target = _target;
	}

	public String toString() {
		return "(fst" + this.target + ")";
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		return this.target.collectAndPartialResolve(_scope);
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		return this.target.completeResolve(_scope);
	}

	@Override
	public Type getType() {
		return ((CoupleType) this.target.getType()).getFirst();
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		CoupleType _type = (CoupleType) this.target.getType();
		int _secondSize = _type.getSecond().length();
		Fragment _result = this.target.getCode(_factory);
		if (_secondSize > 0) {
			_result.add(_factory.createPop(0, _secondSize));
		}
		return _result;
	}

}
