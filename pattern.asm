.intel_syntax noprefix

.section .bss
    .lcomm HEAP, 67108864
    .global heap_ptr

.section .data
    heap_ptr: .quad HEAP

.section .text
.global _start

.p2align 3
_start:
    lea rax, [HEAP]
    mov [heap_ptr], rax
    push rbp
    mov rbp, rsp
    lea rax, [lbl_main]
    or rax, 2
.force_eval_loop:
    call lbl_eval
    test rax, 1
    jnz .force_eval_done
    jmp .force_eval_loop
.force_eval_done:
    call lbl_print_int
    mov rax, 60
    mov rdi, 0
    syscall
.p2align 3
.quad 1
lbl_unbox:
    pop r15
    lea rax, [comb_I]
    or rax, 2
    push rax
    mov rax, 1
    push rax
    lea rax, [comb_CStar]
    or rax, 2
    push rax
    pop rbx
    pop rcx
    call lbl_alloc_node
    mov [rax], rbx
    mov [rax+8], rcx
    push rax
    lea rax, [comb_C]
    or rax, 2
    push rax
    pop rbx
    pop rcx
    call lbl_alloc_node
    mov [rax], rbx
    mov [rax+8], rcx
    push rax
    pop rbx
    pop rcx
    call lbl_alloc_node
    mov [rax], rbx
    mov [rax+8], rcx
    push r15
    ret
.p2align 3
.quad 0
lbl_main:
    pop r15
    mov rax, 85
    push r15
    ret
.p2align 3
lbl_eval:
    push rbp
    mov rbp, rsp
.eval_loop:
    test rax, 1
    jnz .eval_done
    test rax, 2
    jnz .eval_func
    mov rbx, [rax+8]
    push rbx
    mov rax, [rax]
    jmp .eval_loop
.eval_func:
    mov rcx, rax
    and rcx, -3
    mov rdx, [rcx-8]
    mov r8, rbp
    sub r8, rsp
    shr r8, 3
    cmp r8, rdx
    jl .partial_application
    call rcx
    jmp .eval_loop
.partial_application:
    cmp rsp, rbp
    je .eval_done
    pop rcx
    mov rbx, rax
    call lbl_alloc_node
    mov [rax], rbx
    mov [rax+8], rcx
    jmp .partial_application
.eval_done:
    mov rsp, rbp
    pop rbp
    ret
.p2align 3
lbl_alloc_node:
    push r15
    mov rax, [heap_ptr]
    lea r15, [rax + 16]
    lea r11, [HEAP + 67108864]
    cmp r15, r11
	jae lbl_heap_overflow
.alloc_ok:
    mov [heap_ptr], r15
    pop r15
    ret
.p2align 3
lbl_heap_overflow:
    mov rax, 60
    mov rdi, 1
    syscall
.p2align 3
.quad 1
comb_I:
    pop r15
    pop rax
    push r15
    ret
.p2align 3
.quad 2
comb_K:
    pop r15
    pop rax
    pop rbx
    push r15
    ret
.p2align 3
.quad 3
comb_B:
    pop r15
    pop rbx
    pop rcx
    pop rdx
    call lbl_alloc_node
    mov [rax], rcx
    mov [rax+8], rdx
    mov r8, rax
    call lbl_alloc_node
    mov [rax], rbx
    mov [rax+8], r8
    push r15
    ret
.p2align 3
.quad 3
comb_C:
    pop r15
    pop rbx
    pop rcx
    pop rdx
    call lbl_alloc_node
    mov [rax], rbx
    mov [rax+8], rdx
    mov r8, rax
    call lbl_alloc_node
    mov [rax], r8
    mov [rax+8], rcx
    push r15
    ret
.p2align 3
.quad 2
comb_CStar:
    pop r15
    pop rbx
    pop rcx
    call lbl_alloc_node
    mov [rax], rcx
    mov [rax+8], rbx
    push r15
    ret
.p2align 3
.quad 3
comb_S:
    pop r15
    pop rbx
    pop rcx
    pop rdx
    call lbl_alloc_node
    mov [rax], rcx
    mov [rax+8], rdx
    mov r8, rax
    call lbl_alloc_node
    mov [rax], rbx
    mov [rax+8], rdx
    mov r9, rax
    call lbl_alloc_node
    mov [rax], r9
    mov [rax+8], r8
    push r15
    ret
.p2align 3
.quad 1
comb_Y:
    pop r15
    pop rbx
    call lbl_alloc_node
    mov [rax], rbx
    mov [rax+8], rax
    push r15
    ret
.p2align 3
.quad 2
comb_W:
    pop r15
    pop rbx
    pop rcx
    call lbl_alloc_node
    mov [rax], rbx
    mov [rax+8], rcx
    mov r8, rax
    call lbl_alloc_node
    mov [rax], r8
    mov [rax+8], rcx
    push r15
    ret
.p2align 3
.quad 2
lbl_plus:
    pop r15
    pop rbx
    pop rcx
    push rcx
    push r15
    mov rax, rbx
    call lbl_eval
    pop r15
    pop rcx
    mov rbx, rax
    push rbx
    push r15
    mov rax, rcx
    call lbl_eval
    pop r15
    pop rbx
    mov rcx, rax
    sar rbx, 1
    sar rcx, 1
    add rbx, rcx
    mov rax, rbx
    shl rax, 1
    or rax, 1
    push r15
    ret
.p2align 3
.quad 2
lbl_mul:
    pop r15
    pop rbx
    pop rcx
    push rcx
    push r15
    mov rax, rbx
    call lbl_eval
    pop r15
    pop rcx
    mov rbx, rax
    push rbx
    push r15
    mov rax, rcx
    call lbl_eval
    pop r15
    pop rbx
    mov rcx, rax
    sar rbx, 1
    sar rcx, 1
    imul rbx, rcx
    mov rax, rbx
    shl rax, 1
    or rax, 1
    push r15
    ret
.p2align 3
.quad 2
lbl_minus:
    pop r15
    pop rbx
    pop rcx
    push rcx
    push r15
    mov rax, rbx
    call lbl_eval
    pop r15
    pop rcx
    mov rbx, rax
    push rbx
    push r15
    mov rax, rcx
    call lbl_eval
    pop r15
    pop rbx
    mov rcx, rax
    sar rbx, 1
    sar rcx, 1
    sub rbx, rcx
    mov rax, rbx
    shl rax, 1
    or rax, 1
    push r15
    ret
.p2align 3
.quad 2
lbl_lteq:
    pop r15
    pop rbx
    pop rcx
    push rcx
    push r15
    mov rax, rbx
    call lbl_eval
    pop r15
    pop rcx
    mov rbx, rax
    push rbx
    push r15
    mov rax, rcx
    call lbl_eval
    pop r15
    pop rbx
    mov rcx, rax
    cmp rbx, rcx
    jle .is_true
    mov rax, 1
    jmp .cmp_done
.is_true:
    mov rax, 3
.cmp_done:
    push r15
    ret
.p2align 3
.quad 3
lbl_IF:
    pop r15
    pop rax
    pop rbx
    pop rcx
    push rcx
    push rbx
    push r15
    call lbl_eval
    pop r15
    pop rbx
    pop rcx
    cmp rax, 3
    je .do_true
    mov rax, rcx
    jmp .if_done
.do_true:
    mov rax, rbx
.if_done:
    push r15
    ret
.p2align 3
lbl_print_int:
    push rbp
    mov rbp, rsp
    push rbx
    push r12
    sar rax, 1
    sub rsp, 32
    mov rcx, 0
    mov rbx, 10
    lea r12, [rsp+31]
    mov byte ptr [r12], 10
    dec r12
    inc rcx
.print_loop:
    xor rdx, rdx
    div rbx
    add dl, '0'
    mov [r12], dl
    dec r12
    inc rcx
    cmp rax, 0
    jnz .print_loop
    inc r12
    mov rax, 1
    mov rdi, 1
    mov rsi, r12
    mov rdx, rcx
    syscall
    add rsp, 32
    pop r12
    pop rbx
    pop rbp
    ret
.p2align 3
lbl_print_list:
    call lbl_print_list_raw
    ret
.p2align 3
lbl_print_list_raw:
    push rbx
    push rcx
    mov rbx, rax
    lea rcx, [lbl_list_empty_handler]
    or rcx, 2
    call lbl_alloc_node
    mov [rax], rbx
    mov [rax+8], rcx
    mov rbx, rax
    lea rcx, [lbl_list_cons_handler]
    or rcx, 2
    call lbl_alloc_node
    mov [rax], rbx
    mov [rax+8], rcx
    call lbl_eval
    pop rcx
    pop rbx
    ret
.p2align 3
.quad 0
lbl_list_empty_handler:
    pop r15
    mov rax, 1
    push r15
    ret
.p2align 3
.quad 2
lbl_list_cons_handler:
    pop r15
    pop rbx
    pop rcx
    push rcx
    push r15
    mov rax, rbx
    call lbl_eval
    call lbl_print_int
    pop r15
    pop rcx
    push r15
    mov rax, rcx
    call lbl_print_list_raw
    pop r15
    mov rax, 1
    push r15
    ret
