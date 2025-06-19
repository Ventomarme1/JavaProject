import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class UltraSimpleCalculator extends JFrame {
    private JTextField inputField;
    private JLabel resultLabel;
    private Map<String, MathFunction> functions = new HashMap<>();
    
    interface MathFunction {
        double apply(double x);
    }

    public UltraSimpleCalculator() {
        super("极简数学计算器");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // 初始化数学函数
        initFunctions();
        
        // 创建界面
        createUI();
    }

    private void initFunctions() {
        functions.put("sin", Math::sin);
        functions.put("cos", Math::cos);
        functions.put("tan", Math::tan);
        functions.put("sqrt", Math::sqrt);
        functions.put("abs", Math::abs);
        functions.put("log", x -> Math.log10(x));
        functions.put("ln", Math::log);
    }

    private void createUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // 输入区域
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        inputPanel.add(new JLabel("输入表达式:"));
        inputField = new JTextField(20);
        inputPanel.add(inputField);
        
        // 按钮区域
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton calcButton = new JButton("计算");
        calcButton.addActionListener(e -> calculate());
        buttonPanel.add(calcButton);
        
        // 结果区域
        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        resultLabel = new JLabel("结果将显示在这里");
        resultPanel.add(resultLabel);
        
        // 添加所有组件
        mainPanel.add(inputPanel);
        mainPanel.add(buttonPanel);
        mainPanel.add(resultPanel);
        
        // 函数提示
        JLabel hint = new JLabel("支持函数: sin, cos, tan, sqrt, abs, log, ln");
        hint.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        hint.setForeground(Color.GRAY);
        mainPanel.add(hint);
        
        add(mainPanel, BorderLayout.CENTER);
    }

    private void calculate() {
        String expr = inputField.getText().trim();
        if (expr.isEmpty()) {
            resultLabel.setText("错误: 表达式不能为空");
            return;
        }
        
        try {
            // 替换常量
            expr = expr.replace("pi", String.valueOf(Math.PI))
                       .replace("e", String.valueOf(Math.E));
            
            // 计算表达式
            double result = evaluate(expr);
            
            // 格式化结果
            DecimalFormat df = new DecimalFormat("#.######");
            resultLabel.setText("结果: " + df.format(result));
        } catch (Exception e) {
            resultLabel.setText("错误: " + e.getMessage());
        }
    }

    private double evaluate(String expr) {
        // 处理括号
        while (expr.contains("(")) {
            int start = expr.lastIndexOf("(");
            int end = expr.indexOf(")", start);
            if (end == -1) throw new RuntimeException("括号不匹配");
            
            String subExpr = expr.substring(start + 1, end);
            double subResult = evaluate(subExpr);
            expr = expr.substring(0, start) + subResult + expr.substring(end + 1);
        }
        
        // 处理函数
        for (String func : functions.keySet()) {
            if (expr.contains(func)) {
                int start = expr.indexOf(func);
                int end = start + func.length();
                
                // 找到函数参数
                int numStart = end;
                while (numStart < expr.length() && !Character.isDigit(expr.charAt(numStart)) 
                       && expr.charAt(numStart) != '-') {
                    numStart++;
                }
                
                int numEnd = numStart;
                while (numEnd < expr.length() && (Character.isDigit(expr.charAt(numEnd)) 
                       || expr.charAt(numEnd) == '.' || expr.charAt(numEnd) == '-')) {
                    numEnd++;
                }
                
                if (numStart >= expr.length()) throw new RuntimeException("函数参数缺失");
                
                String numStr = expr.substring(numStart, numEnd);
                double num = Double.parseDouble(numStr);
                double funcResult = functions.get(func).apply(num);
                
                expr = expr.substring(0, start) + funcResult + expr.substring(numEnd);
            }
        }
        
        // 处理基本运算
        return evalBasic(expr);
    }

    private double evalBasic(String expr) {
        // 处理乘除
        while (expr.contains("*") || expr.contains("/")) {
            int idx = Math.max(expr.indexOf("*"), expr.indexOf("/"));
            if (idx == -1) break;
            
            char op = expr.charAt(idx);
            String left = findLeftOperand(expr, idx);
            String right = findRightOperand(expr, idx);
            
            double a = Double.parseDouble(left);
            double b = Double.parseDouble(right);
            double result = op == '*' ? a * b : a / b;
            
            expr = expr.replace(left + op + right, String.valueOf(result));
        }
        
        // 处理加减
        while (expr.contains("+") || (expr.contains("-") && expr.indexOf('-') > 0)) {
            int idx = Math.max(expr.indexOf("+"), expr.indexOf("-", 1));
            if (idx == -1) break;
            
            char op = expr.charAt(idx);
            String left = findLeftOperand(expr, idx);
            String right = findRightOperand(expr, idx);
            
            double a = Double.parseDouble(left);
            double b = Double.parseDouble(right);
            double result = op == '+' ? a + b : a - b;
            
            expr = expr.replace(left + op + right, String.valueOf(result));
        }
        
        return Double.parseDouble(expr);
    }

    private String findLeftOperand(String expr, int idx) {
        int start = idx - 1;
        while (start > 0 && (Character.isDigit(expr.charAt(start)) 
               || expr.charAt(start) == '.' || expr.charAt(start) == '-')) {
            start--;
        }
        if (expr.charAt(start) == '-') {
            // 检查负号是否属于此操作数
            if (start == 0 || !Character.isDigit(expr.charAt(start - 1))) {
                start--;
            }
        }
        return expr.substring(start + 1, idx);
    }

    private String findRightOperand(String expr, int idx) {
        int end = idx + 1;
        if (expr.charAt(end) == '-') end++; // 处理负数
        while (end < expr.length() && (Character.isDigit(expr.charAt(end)) 
               || expr.charAt(end) == '.')) {
            end++;
        }
        return expr.substring(idx + 1, end);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new UltraSimpleCalculator().setVisible(true);
        });
    }
}