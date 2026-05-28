package fr.n7.stl.minic.ast.expression;

import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minic.ast.expression.assignable.AssignableExpression;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.TAMFactory;
import fr.n7.stl.util.Logger;

public abstract class AbstractConversion<TargetType> implements Expression {

	protected TargetType target;
	protected Type type;
	protected String name;

	public AbstractConversion(TargetType _target, String _type) {
		this.target = _target;
		this.name = _type;
		this.type = null;
	}

	public AbstractConversion(TargetType _target, Type _type) {
		this.target = _target;
		this.name = null;
		this.type = _type;
	}

	public String toString() {
		if (this.type == null) {
			return "(" + this.name + ") " + this.target;
		} else {
			return "(" + this.type + ") " + this.target;
		}
	}

	@Override
	public Type getType() {
		return this.type;
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		boolean _ok = ((Expression) this.target).collectAndPartialResolve(_scope);
		if (this.type == null && this.name != null) {
			if (_scope.knows(this.name)) {
				this.type = _scope.get(this.name).getType();
			} else {
				Logger.error("Type " + this.name + " not found in scope.");
				_ok = false;
			}
		}
		return _ok;
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		return ((Expression) this.target).completeResolve(_scope);
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		return ((Expression) this.target).getCode(_factory);
	}

}
