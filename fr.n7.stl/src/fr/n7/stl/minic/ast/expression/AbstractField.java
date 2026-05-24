package fr.n7.stl.minic.ast.expression;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.RecordType;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minic.ast.type.declaration.FieldDeclaration;
import fr.n7.stl.util.Logger;

public abstract class AbstractField<RecordKind extends Expression> implements Expression {

	protected RecordKind record;
	protected String name;
	protected FieldDeclaration field;

	public AbstractField(RecordKind _record, String _name) {
		this.record = _record;
		this.name = _name;
	}

	@Override
	public String toString() {
		return this.record + "." + this.name;
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		boolean _ok = this.record.collectAndPartialResolve(_scope);
		if (_ok) {
			Type _type = this.record.getType();
			if (_type instanceof RecordType) {
				RecordType _recordType = (RecordType) _type;
				int _offset = 0;
				for (FieldDeclaration _f : _recordType.getFields()) {
					_offset += _f.computerOffset(_offset);
				}
				if (_recordType.contains(this.name)) {
					this.field = _recordType.get(this.name);
				} else {
					Logger.error("Field " + this.name + " not found in " + _type);
					_ok = false;
				}
			} else {
				Logger.error("Field access on non-record type " + _type);
				_ok = false;
			}
		}
		return _ok;
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		return this.record.completeResolve(_scope);
	}

	public Type getType() {
		return this.field.getType();
	}

}
