# Grammar of the Lambda Calculus with Extensions

The following defines the grammar of the lambda calculus extended with boolean literals, integer literals, conditional expressions, recursion, and various operators. The grammar is presented in a simplified Backus-Naur Form (BNF).

```bnf
<program> ::= <term>

<term> ::= <variable>
         | <abstraction>
         | <application>
         | <boolean>
         | <integer>
         | <if_expression>
         | <recursion>
         | "(" <term> ")"

<variable> ::= <identifier>
<abstraction> ::= "λ" <identifier> "." <term>
<application> ::= <term> <term>
<boolean> ::= "True" | "False"
<integer> ::= <digit>+
<if_expression> ::= "if" <term> "then" <term> "else" <term>
<recursion> ::= "rec" <identifier> "." <term>
<identifier> ::= <letter> <alphanumeric>*
<letter> ::= 'a'...'z' | 'A'...'Z'
<digit> ::= '0'...'9'
<alphanumeric> ::= <letter> | <digit>
<operator> ::= "+" | "-" | "*" | "and" | "or" | "=" | "<=" | "not"

// Handling infix and prefix operators with precedence (simplified for BNF)
<term> ::= <prefix_term>
         | <infix_term>

<prefix_term> ::= <prefix_op> <term>
              | <atomic_term>

<atomic_term> ::= <variable>
                | <abstraction>
                | <boolean>
                | <integer>
                | <if_expression>
                | <recursion>
                | "(" <term> ")"

<prefix_op> ::= "not"

<infix_term> ::= <term> <infix_op> <term>

<infix_op> ::= "or"
             | "and"
             | "="
             | "<="
             | "+"
             | "-"
             | "*"

// Incorporating the special handling of operators in parentheses
<term> ::= ...
         | "(" <operator_symbol_standalone> ")"
         | "(" <operator_symbol_right_section> <term> ")"
         | "(" <term> <operator_symbol_left_section> ")"

<operator_symbol_standalone> ::= "*" | "+" | "-"
<operator_symbol_right_section> ::= "*" | "+" | "-"
<operator_symbol_left_section> ::= "*" | "+" | "-"