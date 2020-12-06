cpu jvm {
	name = "Java Virtual Machine"
	word : int32 | float32 | address32 | int64[0:31] | int64[32:63] | float64[0:31] | float64[32:63]
	state = {
		stack<word> operand_stack
		stack<frame> call_stack
		memory<byte> heap
		uint pc
	}
	frame : {
		unit pc
		memory<byte> bytecode
		memory<word> locals
	}
	instructions = {
		iload(var) = { operand_stack.push(locals[var]) }
		istore(var) = { locals[var] = operand_stack.pop() }
	}
}