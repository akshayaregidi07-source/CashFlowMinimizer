package sample.Com;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

class Balance {
    String name;
    int amount;

    Balance(String name, int amount) {
        this.name = name;
        this.amount = amount;
    }
}

class Transaction {
    String from;
    String to;
    int amount;

    Transaction(String from, String to, int amount) {
        this.from = from;
        this.to = to;
        this.amount = amount;
    }

    public String toString() {
        return from + " ➜ " + to + " : ₹" + amount;
    }
}

public class CashFlowMinimizer {

    private Frame window;

    private java.util.List<String> friends = new ArrayList<>();
    private java.util.List<String> expenses = new ArrayList<>();
    private Map<String, Integer> net = new HashMap<>();

    private java.util.List<TextField> friendFields = new ArrayList<>();

    private Choice payerChoice;
    private TextArea expenseArea;

    // ----------- COLORS -----------

    private Color primary = new Color(52, 152, 219);
    private Color background = new Color(245, 245, 245);
    private Color success = new Color(46, 204, 113);
    private Color danger = new Color(231, 76, 60);

    public CashFlowMinimizer() {
        showFriendCountWindow();
    }

    // -------- FRIEND COUNT WINDOW --------

    private void showFriendCountWindow() {

        window = new Frame("💰 Cash Flow Minimizer");

        window.setLayout(new FlowLayout());
        window.setBackground(background);

        Label label = new Label("Enter number of friends:");
        label.setFont(new Font("Arial", Font.BOLD, 14));

        TextField countField = new TextField(10);

        Button nextBtn = new Button("Next");
        nextBtn.setBackground(primary);
        nextBtn.setForeground(Color.WHITE);

        nextBtn.addActionListener(e -> {
            try {

                int n = Integer.parseInt(countField.getText());

                if (n <= 1) {
                    showError("Minimum 2 friends required");
                    return;
                }

                window.dispose();
                showFriendNameWindow(n);

            } catch (Exception ex) {
                showError("Enter a valid number!");
            }
        });

        window.add(label);
        window.add(countField);
        window.add(nextBtn);

        window.setSize(350, 150);
        window.setVisible(true);

        window.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    // -------- FRIEND NAME WINDOW --------

    private void showFriendNameWindow(int n) {

        window = new Frame("Enter Friend Names");

        window.setLayout(new GridLayout(n + 2, 2, 10, 10));
        window.setBackground(background);

        friendFields.clear();

        for (int i = 0; i < n; i++) {

            Label l = new Label("Friend " + (i + 1) + ":");
            l.setFont(new Font("Arial", Font.PLAIN, 13));

            TextField tf = new TextField(15);

            friendFields.add(tf);

            window.add(l);
            window.add(tf);
        }

        Button nextBtn = new Button("Continue");
        nextBtn.setBackground(primary);
        nextBtn.setForeground(Color.WHITE);

        nextBtn.addActionListener(e -> processFriendNames());

        window.add(new Label(""));
        window.add(nextBtn);

        window.setSize(350, 200 + n * 40);
        window.setVisible(true);
    }

    private void processFriendNames() {

        friends.clear();
        net.clear();

        for (TextField tf : friendFields) {

            String name = tf.getText().trim();

            if (name.isEmpty()) {
                showError("Friend names cannot be empty!");
                return;
            }

            friends.add(name);
            net.put(name, 0);
        }

        window.dispose();
        showExpenseWindow();
    }

    // -------- EXPENSE WINDOW --------

    private void showExpenseWindow() {

        window = new Frame("Add Expenses");

        window.setLayout(new BorderLayout());
        window.setBackground(background);

        Panel inputPanel = new Panel();
        inputPanel.setBackground(background);

        payerChoice = new Choice();

        for (String f : friends)
            payerChoice.add(f);

        TextField amountField = new TextField(10);

        Button addBtn = new Button("Add Expense");
        addBtn.setBackground(success);
        addBtn.setForeground(Color.WHITE);

        inputPanel.add(new Label("Payer:"));
        inputPanel.add(payerChoice);

        inputPanel.add(new Label("Amount:"));
        inputPanel.add(amountField);

        inputPanel.add(addBtn);

        window.add(inputPanel, BorderLayout.NORTH);

        expenseArea = new TextArea();
        expenseArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        expenseArea.setBackground(Color.WHITE);

        window.add(expenseArea, BorderLayout.CENTER);

        Panel bottomPanel = new Panel();
        bottomPanel.setBackground(background);

        Button clearBtn = new Button("Clear");
        clearBtn.setBackground(danger);
        clearBtn.setForeground(Color.WHITE);

        Button calcBtn = new Button("Calculate Settlement");
        calcBtn.setBackground(primary);
        calcBtn.setForeground(Color.WHITE);

        bottomPanel.add(clearBtn);
        bottomPanel.add(calcBtn);

        window.add(bottomPanel, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> {

            try {

                String payer = payerChoice.getSelectedItem();
                int amt = Integer.parseInt(amountField.getText());

                if (amt <= 0) {
                    showError("Amount must be positive!");
                    return;
                }

                expenses.add(payer + ":" + amt);

                expenseArea.append("✔ " + payer + " paid ₹" + amt + "\n");

                amountField.setText("");

            } catch (Exception ex) {
                showError("Invalid amount!");
            }
        });

        clearBtn.addActionListener(e -> {

            expenses.clear();
            expenseArea.setText("");

        });

        calcBtn.addActionListener(e -> calculate());

        window.setSize(500, 400);
        window.setVisible(true);
    }

    // -------- CALCULATION --------

    private void calculate() {

        for (String f : friends)
            net.put(f, 0);

        int n = friends.size();

        for (String exp : expenses) {

            String[] parts = exp.split(":");

            String payer = parts[0];
            int amount = Integer.parseInt(parts[1]);

            int share = amount / n;

            for (String f : friends) {

                if (f.equals(payer))
                    net.put(f, net.get(f) + (amount - share));

                else
                    net.put(f, net.get(f) - share);
            }
        }

        java.util.List<Transaction> result = minimize(net);

        showOutput(result);
    }

    // -------- MINIMIZE TRANSACTIONS --------

    private java.util.List<Transaction> minimize(Map<String, Integer> net) {

        PriorityQueue<Balance> maxHeap =
                new PriorityQueue<>((a, b) -> b.amount - a.amount);

        PriorityQueue<Balance> minHeap =
                new PriorityQueue<>((a, b) -> a.amount - b.amount);

        for (String name : net.keySet()) {

            int amt = net.get(name);

            if (amt > 0)
                maxHeap.add(new Balance(name, amt));

            else if (amt < 0)
                minHeap.add(new Balance(name, amt));
        }

        java.util.List<Transaction> result = new ArrayList<>();

        while (!maxHeap.isEmpty() && !minHeap.isEmpty()) {

            Balance cr = maxHeap.poll();
            Balance db = minHeap.poll();

            int settleAmount = Math.min(cr.amount, -db.amount);

            result.add(new Transaction(db.name, cr.name, settleAmount));

            cr.amount -= settleAmount;
            db.amount += settleAmount;

            if (cr.amount > 0)
                maxHeap.add(cr);

            if (db.amount < 0)
                minHeap.add(db);
        }

        return result;
    }

    // -------- OUTPUT --------

    private void showOutput(java.util.List<Transaction> list) {

        window.dispose();

        Frame outFrame = new Frame("Final Settlement");

        TextArea out = new TextArea();
        out.setEditable(false);

        out.setFont(new Font("Monospaced", Font.BOLD, 14));
        out.setBackground(new Color(250, 250, 250));

        out.append("===== NET BALANCES =====\n");

        for (String f : net.keySet())
            out.append(f + " : ₹" + net.get(f) + "\n");

        out.append("\n===== MINIMUM TRANSACTIONS =====\n");

        for (Transaction t : list)
            out.append(t + "\n");

        Button restartBtn = new Button("Restart");

        restartBtn.setBackground(primary);
        restartBtn.setForeground(Color.WHITE);

        restartBtn.addActionListener(e -> {

            outFrame.dispose();
            new CashFlowMinimizer();

        });

        outFrame.setLayout(new BorderLayout());

        outFrame.add(out, BorderLayout.CENTER);
        outFrame.add(restartBtn, BorderLayout.SOUTH);

        outFrame.setSize(600, 500);
        outFrame.setVisible(true);
    }

    // -------- ERROR WINDOW --------

    private void showError(String message) {

        Dialog d = new Dialog(window, "Error", true);

        d.setLayout(new FlowLayout());
        d.setBackground(new Color(255, 240, 240));

        d.add(new Label(message));

        Button ok = new Button("OK");

        ok.setBackground(primary);
        ok.setForeground(Color.WHITE);

        ok.addActionListener(e -> d.dispose());

        d.add(ok);

        d.setSize(250, 120);
        d.setVisible(true);
    }

    public static void main(String[] args) {

        new CashFlowMinimizer();

    }
}