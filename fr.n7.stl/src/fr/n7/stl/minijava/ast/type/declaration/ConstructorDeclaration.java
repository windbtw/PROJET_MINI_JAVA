package fr.n7.stl.minijava.ast.type.declaration;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import fr.n7.stl.minic.ast.Block;
import fr.n7.stl.minic.ast.instruction.Instruction;
import fr.n7.stl.minic.ast.instruction.declaration.FunctionDeclaration;
import fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.AtomicType;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minijava.ast.type.ClassType;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;

public class ConstructorDeclaration extends ClassElement implements Instruction {

	protected List<ParameterDeclaration> parameters;

	protected Block body;

	protected ClassDeclaration owner;

	protected FunctionDeclaration function;

	public ConstructorDeclaration(String _name, List<ParameterDeclaration> _parameters, Block _body) {
		super(_name);
		this.parameters = _parameters;
		this.body = _body;
	}

	public void setOwner(ClassDeclaration _owner) {
		this.owner = _owner;
	}

	public ClassDeclaration getOwner() {
		return this.owner;
	}

	public FunctionDeclaration getFunction() {
		return this.function;
	}

	public List<ParameterDeclaration> getParameters() {
		return this.parameters;
	}

	public int getArity() {
		return this.parameters.size();
	}

	@Override
	public Type getType() {
		return AtomicType.VoidType;
	}

	protected void buildFunction() {
		if (this.function != null) {
			return;
		}
		List<ParameterDeclaration> allParams = new LinkedList<>();
		allParams.add(new ParameterDeclaration("this", new ClassType(this.owner.getName())));
		allParams.addAll(this.parameters);
		String label = this.owner.getName() + "_init_" + this.parameters.size();
		this.function = new FunctionDeclaration(label, AtomicType.VoidType, allParams, this.body);
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		this.buildFunction();
		return this.function.collectAndPartialResolve(_scope);
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope, FunctionDeclaration _container) {
		return this.collectAndPartialResolve(_scope);
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		return this.function.completeResolve(_scope);
	}

	@Override
	public boolean checkType() {
		return this.function.checkType();
	}

	@Override
	public int allocateMemory(Register _register, int _offset) {
		return this.function.allocateMemory(_register, _offset);
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		return this.function.getCode(_factory);
	}

	@Override
	public String toString() {
		String image = "";
		image += this.accessRight + " " + this.name + "( ";
		Iterator<ParameterDeclaration> iterator = this.parameters.iterator();
		if (iterator.hasNext()) {
			ParameterDeclaration parameter = iterator.next();
			image += parameter;
			while (iterator.hasNext()) {
				parameter = iterator.next();
				image += " ," + parameter;
			}
		}
		image += ")";
		image += this.body;
		return image;
	}
}
