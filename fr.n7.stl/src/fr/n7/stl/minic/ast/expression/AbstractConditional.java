package fr.n7.stl.minic.ast.expression;

import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.TAMFactory;

public class AbstractConditional<ExpressionKind extends Expression> implements Expression {

	protected Expression condition;
	protected ExpressionKind thenExpression;
	protected ExpressionKind elseExpression;

	public AbstractConditional(Expression _condition, ExpressionKind _then, ExpressionKind _else) {
		this.condition = _condition;
		this.thenExpression = _then;
		this.elseExpression = _else;
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		return this.condition.collectAndPartialResolve(_scope)
			& this.thenExpression.collectAndPartialResolve(_scope)
			& this.elseExpression.collectAndPartialResolve(_scope);
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		return this.condition.completeResolve(_scope)
			& this.thenExpression.completeResolve(_scope)
			& this.elseExpression.completeResolve(_scope);
	}

	@Override
	public String toString() {
		return "(" + this.condition + " ? " + this.thenExpression + " : " + this.elseExpression + ")";
	}

	@Override
	public Type getType() {
		return this.thenExpression.getType().merge(this.elseExpression.getType());
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		String _elseLabel = "elsecond" + _factory.createLabelNumber();
		String _endLabel = "endcond" + _factory.createLabelNumber();
		Fragment _result = this.condition.getCode(_factory);
		_result.add(_factory.createJumpIf(_elseLabel, 0));
		Fragment _thenCode = this.thenExpression.getCode(_factory);
		_thenCode.add(_factory.createJump(_endLabel));
		_thenCode.addSuffix(_elseLabel);
		_result.append(_thenCode);
		Fragment _elseCode = this.elseExpression.getCode(_factory);
		_elseCode.add(_factory.createJump(_endLabel));
		_elseCode.addSuffix(_endLabel);
		_result.append(_elseCode);
		return _result;
	}

}
