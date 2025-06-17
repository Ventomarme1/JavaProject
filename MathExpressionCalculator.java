import java.util.Scanner;
import java.util.Stack;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MathExpressionCalculator {
    // 支持的函数映射表
    private static final Map<String, Function> FUNCTIONS = new HashMap<>();
    static {
        FUNCTIONS.put("abs", x -> Math.abs(x));
        FUNCTIONS.put("round", x -> (double) Math.round(x));
        FUNCTIONS.put("floor", x -> Math.floor(x));
        FUNCTIONS.put("ceil", x -> Math.ceil(x));
        FUNCTIONS.put("sin", x -> Math.sin(Math.toRadians(x)));
        FUNCTIONS.put("cos", x -> Math.cos(Math.toRadians(x)));
        FUNCTIONS.put("tan", x -> Math.tan(Math.toRadians(x)));
        FUNCTIONS.put("sqrt", x -> Math.sqrt(x));
        FUNCTIONS.put("square", x -> x * x);
        FUNCTIONS.put("cube", x -> x * x * x);
        FUNCTIONS.put("reciprocal", x -> 1 / x);
    }

    @FunctionalInterface
    private interface Function {
        double apply(double x);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入数学表达式: ");
        String expression = scanner.nextLine().replaceAll("\\s+", "").toLowerCase();
        
        try {
            double result = evaluateExpression(expression);
            System.out.println("计算结果: " + result);
        } catch (Exception e) {
            System.out.println("计算错误: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    private static double evaluateExpression(String expression) {
        // 处理函数调用
        Pattern funcPattern = Pattern.compile("([a-z]+)\\(([^()]+)\\)");
        Matcher matcher = funcPattern.matcher(expression);
        while (matcher.find()) {
            String funcName = matcher.group(1);
            String arg = matcher.group(2);
            if (!FUNCTIONS.containsKey(funcName)) {
                throw new IllegalArgumentException("不支持的函数: " + funcName);
            }
            double argValue = evaluateExpression(arg);
            double funcResult = FUNCTIONS.get(funcName).apply(argValue);
            expression = expression.replace(matcher.group(0), String.valueOf(funcResult));
            matcher = funcPattern.matcher(expression);
        }
        
        // 使用双栈法计算表达式
        Stack<Double> numbers = new Stack<>();
        Stack<Character> operators = new Stack<>();
        
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            
            if (Character.isDigit(c) || c == '.') {
                // 处理数字
                StringBuilder numBuilder = new StringBuilder();
                while (i < expression.length() && 
                      (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    numBuilder.append(expression.charAt(i++));
                }
                i--;
                numbers.push(Double.parseDouble(numBuilder.toString()));
            } else if (c == '(') {
                operators.push(c);
            } else if (c == ')') {
                while (operators.peek() != '(') {
                    evaluateTop(numbers, operators);
                }
                operators.pop(); // 弹出 '('
            } else if (isOperator(c)) {
                // 处理运算符优先级
                while (!operators.isEmpty() && precedence(operators.peek()) >= precedence(c)) {
                    evaluateTop(numbers, operators);
                }
                operators.push(c);
            } else {
                throw new IllegalArgumentException("无效字符: " + c);
            }
        }
        
        // 处理剩余运算符
        while (!operators.isEmpty()) {
            evaluateTop(numbers, operators);
        }
        
        if (numbers.size() != 1 || !operators.isEmpty()) {
            throw new IllegalArgumentException("无效表达式");
        }
        
        return numbers.pop();
    }

    private static void evaluateTop(Stack<Double> numbers, Stack<Character> operators) {
        char op = operators.pop();
        double b = numbers.pop();
        double a = numbers.isEmpty() ? 0 : numbers.pop();
        
        switch (op) {
            case '+': numbers.push(a + b); break;
            case '-': numbers.push(a - b); break;
            case '*': numbers.push(a * b); break;
            case '/': 
                if (b == 0) throw new ArithmeticException("除以零错误");
                numbers.push(a / b); 
                break;
        }
    }

    private static boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private static int precedence(char op) {
        if (op == '+' || op == '-') return 1;
        if (op == '*' || op == '/') return 2;
        return 0;
    }
}