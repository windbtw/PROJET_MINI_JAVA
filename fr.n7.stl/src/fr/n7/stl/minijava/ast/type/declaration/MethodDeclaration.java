package fr.n7.stl.minijava.ast.type.declaration;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import fr.n7.stl.minic.ast.Block;
import fr.n7.stl.minic.ast.instruction.Instruction;
import fr.n7.stl.minic.ast.instruction.Instruction;
import fr.n7.stl.minic.ast.instruction.declaration.FunctionDeclaration;
import fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minijava.ast.type.ClassType;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;

public class MethodDeclaration extends ClassElement implements Instruction {

	protected boolean concrete;

	protected List<ParameterDeclaration> parameters;

	protected Block body;

	protected Type type;

	/** Owner class (set by ClassDeclaration.collectAndPartialResolve). */
	protected ClassDeclaration owner;

	/** Synthetic FunctionDeclaration used to delegate all semantic methods. */
	protected FunctionDeclaration function;

	protected int vmtOffset = -1;

	public int getVmtOffset() {
		return this.vmtOffset;
	}

	public void setVmtOffset(int offset) {
		this.vmtOffset = offset;
	}

	public MethodDeclaration(String _name, Type _type, List<ParameterDeclaration> _parameters, Block _body) {
		super(_name);
		this.parameters = _parameters;
		this.body = _body;
		this.concrete = (_body != null);
		this.type = _type;
	}

	public MethodDeclaration(String _name, Type _type, List<ParameterDeclaration> _parameters) {
		this(_name, _type, _parameters, null);
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

	public boolean isConcrete() {
		return this.concrete;
	}

	@Override
	public Type getType() {
		return this.type;
	}

	/**
	 * Build the synthetic FunctionDeclaration. Must be called after owner is set
	 * and before delegating semantics.
	 */
	public boolean isStatic() {
		return this.elementKind == ElementKind.CLASS;
	}

	protected void buildFunction() {
		if (this.function != null) {
			return;
		}
		List<ParameterDeclaration> allParams = new LinkedList<>();
		String label;
		if (this.isStatic()) {
			allParams.addAll(this.parameters);
			label = this.owner.getName() + "_static_" + this.name;
		} else {
			allParams.add(new ParameterDeclaration("this", new ClassType(this.owner.getName())));
			allParams.addAll(this.parameters);
			label = this.owner.getName() + "_" + this.name;
		}
		// Abstract methods have no body. Build with an empty block so the function still
		// has a label (needed for VMT slot placeholders) and downstream semantics never NPE.
		Block effectiveBody = (this.body != null) ? this.body : new Block(new LinkedList<Instruction>());
		this.function = new FunctionDeclaration(label, this.type, allParams, effectiveBody);
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
		if (!this.concrete) {
			image += "abstract ";
		}
		image += this.accessRight + " " + this.type + " " + this.name + "( ";
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
		if (this.concrete) {
			image += this.body;
		} else {
			image += ";";
		}
		return image;
	}
}
