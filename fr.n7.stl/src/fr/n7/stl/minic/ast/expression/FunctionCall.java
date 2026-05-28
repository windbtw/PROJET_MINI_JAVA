package fr.n7.stl.minic.ast.expression;

import java.util.Iterator;
import java.util.List;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minic.ast.instruction.declaration.FunctionDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;
import fr.n7.stl.util.Logger;

public class FunctionCall implements AccessibleExpression {

	protected String name;
	protected FunctionDeclaration function;
	protected List<AccessibleExpression> arguments;

	public FunctionCall(String _name, List<AccessibleExpression> _arguments) {
		this.name = _name;
		this.function = null;
		this.arguments = _arguments;
	}

	@Override
	public String toString() {
		String _result = ((this.function == null) ? this.name : this.function.getName()) + "( ";
		Iterator<AccessibleExpression> _iter = this.arguments.iterator();
		if (_iter.hasNext()) {
			_result += _iter.next();
		}
		while (_iter.hasNext()) {
			_result += " ," + _iter.next();
		}
		return _result + ")";
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		boolean _ok = true;
		for (AccessibleExpression _arg : this.arguments) {
			_ok &= _arg.collectAndPartialResolve(_scope);
		}
		if (_scope.knows(this.name)) {
			Declaration _decl = _scope.get(this.name);
			if (_decl instanceof FunctionDeclaration) {
				this.function = (FunctionDeclaration) _decl;
			} else {
				Logger.error("Identifier " + this.name + " is not a function.");
				_ok = false;
			}
		} else {
			Logger.error("Function " + this.name + " is not declared.");
			_ok = false;
		}
		return _ok;
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		boolean _ok = true;
		for (AccessibleExpression _arg : this.arguments) {
			_ok &= _arg.completeResolve(_scope);
		}
		return _ok;
	}

	@Override
	public Type getType() {
		return this.function.getType();
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		Fragment _result = _factory.createFragment();
		for (AccessibleExpression _arg : this.arguments) {
			_result.append(_arg.getCode(_factory));
		}
		_result.add(_factory.createCall(this.function.getName(), Register.SB));
		return _result;
	}

}
