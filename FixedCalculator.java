import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class FixedCalculator extends JFrame {
    private JTextField inputField;
    private JLabel resultLabel;
    private Map<String, MathFunction> functions = new HashMap<>();
    
    interface MathFunction {
        double apply(double x);
    }

    public FixedCalculator() {
        super("简易数学计算器");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // 初始化数学函数
        initFunctions();
        
        // 创建界面
        createUI();
        
        // 设置窗口位置居中
        setLocationRelativeTo(null);
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
        
        JButton clearButton = new JButton("清除");
        clearButton.addActionListener(e -> {
            inputField.setText("");
            resultLabel.setText("结果将显示在这里");
        });
        buttonPanel.add(clearButton);
        
        // 结果区域
        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        resultLabel = new JLabel("结果将显示在这里");
        resultLabel.setFont(resultLabel.getFont().deriveFont(Font.BOLD, 14f));
        resultPanel.add(resultLabel);
        
        // 函数提示
        JPanel hintPanel = new JPanel();
        hintPanel.add(new JLabel("支持函数: sin, cos, tan, sqrt, abs, log, ln"));
        
        // 示例面板
        JPanel examplePanel = new JPanel();
        examplePanel.setBorder(BorderFactory.createTitledBorder("示例"));
        examplePanel.add(new JLabel("2+3*4, sin(pi/2), sqrt(16)"));
        
        // 添加所有组件
        mainPanel.add(inputPanel);
        mainPanel.add(buttonPanel);
        mainPanel.add(resultPanel);
        mainPanel.add(hintPanel);
        mainPanel.add(examplePanel);
        
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
                       .replace("e", String.valueOf(Math.E))
                       .replaceAll("\\s+", ""); // 移除空格
            
            // 计算表达式
            double result = evaluateExpression(expr);
            
            // 格式化结果
            DecimalFormat df = new DecimalFormat("#.##########");
            resultLabel.setText("结果: " + df.format(result));
        } catch (Exception e) {
            resultLabel.setText("错误: " + e.getMessage());
        }
    }

    // 修复后的表达式求值方法
    private double evaluateExpression(String expr) throws Exception {
        // 检查空表达式
        if (expr.isEmpty()) {
            throw new Exception("空表达式");
        }
        
        // 处理括号
        int start = expr.lastIndexOf('(');
        if (start != -1) {
            int end = expr.indexOf(')', start);
            if (end == -1) {
                throw new Exception("括号不匹配");
            }
            
            String subExpr = expr.substring(start + 1, end);
            double subResult = evaluateExpression(subExpr);
            String newExpr = expr.substring(0, start) + subResult + expr.substring(end + 1);
            return evaluateExpression(newExpr);
        }
        
        // 处理函数
        for (String func : functions.keySet()) {
            if (expr.contains(func)) {
                int funcStart = expr.indexOf(func);
                int funcEnd = funcStart + func.length();
                
                // 查找函数参数
                int numStart = funcEnd;
                while (numStart < expr.length() && !Character.isDigit(expr.charAt(numStart)) 
                       && expr.charAt(numStart) != '-' && expr.charAt(numStart) != '.') {
                    numStart++;
                }
                
                int numEnd = numStart;
                while (numEnd < expr.length() && (Character.isDigit(expr.charAt(numEnd)) 
                       || expr.charAt(numEnd) == '.' || expr.charAt(numEnd) == '-')) {
                    numEnd++;
                }
                
                if (numStart >= expr.length()) {
                    throw new Exception("函数参数缺失: " + func);
                }
                
                String numStr = expr.substring(numStart, numEnd);
                double num = Double.parseDouble(numStr);
                double funcResult = functions.get(func).apply(num);
                
                String newExpr = expr.substring(0, funcStart) + funcResult + expr.substring(numEnd);
                return evaluateExpression(newExpr);
            }
        }
        
        // 处理基本运算 - 修复后的方法
        return evaluateBasic(expr);
    }

    // 修复后的基础运算求值
    private double evaluateBasic(String expr) throws Exception {
        // 先处理乘除
        int idx = -1;
        char op = '\0';
        
        // 查找乘除运算符
        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);
            if (c == '*' || c == '/') {
                idx = i;
                op = c;
                break;
            }
        }
        
        if (idx != -1) {
            // 获取左右操作数
            String left = expr.substring(0, idx);
            String right = expr.substring(idx + 1);
            
            // 递归计算左右操作数
            double a = evaluateBasic(left);
            double b = evaluateBasic(right);
            
            if (op == '*') {
                return a * b;
            } else {
                if (b == 0) throw new Exception("除以零错误");
                return a / b;
            }
        }
        
        // 处理加减
        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);
            if (c == '+' || c == '-') {
                // 跳过开头的负号
                if (i == 0 && c == '-') continue;
                
                idx = i;
                op = c;
                break;
            }
        }
        
        if (idx != -1) {
            // 获取左右操作数
            String left = expr.substring(0, idx);
            String right = expr.substring(idx + 1);
            
            // 递归计算左右操作数
            double a = evaluateBasic(left);
            double b = evaluateBasic(right);
            
            if (op == '+') {
                return a + b;
            } else {
                return a - b;
            }
        }
        
        // 没有运算符，直接返回数值
        try {
            return Double.parseDouble(expr);
        } catch (NumberFormatException e) {
            throw new Exception("无效数字: " + expr);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // 设置系统外观
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            
            new FixedCalculator().setVisible(true);
        });
    }
}
