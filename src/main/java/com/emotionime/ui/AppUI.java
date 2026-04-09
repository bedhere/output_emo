package com.emotionime.ui;

import com.emotionime.service.*;
import com.emotionime.util.GlobalKeyListener;
import com.emotionime.util.WindowsGlobalKeyboardListener;
import com.emotionime.util.KaomojiRenderer;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class AppUI {

    private JButton btn1;
    private JButton btn2;
    private JButton btn3;
    private JButton btnExit;
    private JButton btnClear;
    private JPanel emojiPanel;
    private JLabel emotionLabel;

    private EmotionService emotionService = new EmotionService();
    private EmojiService emojiService = new EmojiService();

    private JFrame frame;
    private JPanel panel;
    private JTextField input;
    private SystemTray systemTray;
    private TrayIcon trayIcon;
    private GlobalKeyListener globalKeyListener;
    private WindowsGlobalKeyboardListener windowsKeyboardListener;

    private Point mousePoint;

    private Timer fadeInTimer;
    private float currentOpacity = 0f;

    private Timer hideTimer;
    private boolean isAutoHideEnabled = false;
    private int autoHideDelay = 8000;

    private String lastSelectedEmoji = "";
    private String currentEmotion = "neutral";
    
    // 全局键盘输入缓存
    private StringBuilder globalInputBuffer = new StringBuilder();
    private volatile boolean isGlobalListenerActive = false;

    public AppUI() {
        // 设置Swing全局字体渲染优化
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        initSystemTray();

        frame = new JFrame();
        frame.setSize(340, 180);
        frame.setUndecorated(true);
        frame.setAlwaysOnTop(true);
        frame.setOpacity(0f);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(screen.width - 360, screen.height - 250);

        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(25, 25, 35));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 80), 2),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        // 标题栏
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(new Color(35, 35, 50));
        titleBar.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 5));

        // 情绪标签 - 表情用emoji字体，中文用微软雅黑
        emotionLabel = new JLabel("😊 情绪输入法");
        emotionLabel.setForeground(new Color(180, 180, 200));
        // 使用微软雅黑确保中文正常显示
        emotionLabel.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
        emotionLabel.setIcon(createEmojiIcon("😊"));

        btnClear = createClearBtn();
        btnExit = createExitBtn();
        
        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        rightButtons.setBackground(new Color(35, 35, 50));
        rightButtons.add(btnClear);
        rightButtons.add(btnExit);
        
        titleBar.add(emotionLabel, BorderLayout.WEST);
        titleBar.add(rightButtons, BorderLayout.EAST);

        // 输入框 - 使用Segoe UI Symbol统一字体
        input = new JTextField();
        input.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 60, 80)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        input.setBackground(new Color(30, 30, 45));
        input.setForeground(new Color(220, 220, 240));
        input.setCaretColor(new Color(150, 150, 255));
        input.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 15));
        input.setCaretColor(Color.WHITE);

        // 颜文字按钮面板
        emojiPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        emojiPanel.setBackground(new Color(25, 25, 35));

        btn1 = createEmojiBtn();
        btn2 = createEmojiBtn();
        btn3 = createEmojiBtn();

        emojiPanel.add(btn1);
        emojiPanel.add(btn2);
        emojiPanel.add(btn3);

        panel.add(titleBar, BorderLayout.NORTH);
        panel.add(input, BorderLayout.CENTER);
        panel.add(emojiPanel, BorderLayout.SOUTH);

        // 事件监听
        btn1.addActionListener(e -> insertEmojiAndCopy(btn1.getText()));
        btn2.addActionListener(e -> insertEmojiAndCopy(btn2.getText()));
        btn3.addActionListener(e -> insertEmojiAndCopy(btn3.getText()));

        btnExit.addActionListener(e -> {
            cleanup();
            System.exit(0);
        });

        btnClear.addActionListener(e -> clearInput());

        input.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            public void changedUpdate(DocumentEvent e) { update(); }

            private void update() {
                String text = input.getText();
                updateEmojiButtons(text);
                resetHideTimer();
            }
        });

        // 快捷键支持
        input.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (c == '1') {
                    lastSelectedEmoji = btn1.getText();
                    insertEmojiAndCopy(btn1.getText());
                    e.consume();
                } else if (c == '2') {
                    lastSelectedEmoji = btn2.getText();
                    insertEmojiAndCopy(btn2.getText());
                    e.consume();
                } else if (c == '3') {
                    lastSelectedEmoji = btn3.getText();
                    insertEmojiAndCopy(btn3.getText());
                    e.consume();
                } else if (c == '\n' || c == '\r') {
                    clearInput();
                    e.consume();
                }
            }
            
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    startFadeOutAnimation();
                    e.consume();
                }
            }
        });

        // 拖拽支持
        titleBar.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                mousePoint = e.getPoint();
            }
            public void mouseEntered(MouseEvent e) {
                if (hideTimer != null && hideTimer.isRunning()) {
                    hideTimer.stop();
                }
            }
        });

        titleBar.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point location = frame.getLocation();
                frame.setLocation(
                        location.x + e.getX() - mousePoint.x,
                        location.y + e.getY() - mousePoint.y
                );
            }
        });

        frame.add(panel);
        frame.setVisible(true);
        startFadeInAnimation();
        initKeyboardListener();
        updateEmojiButtons("");
    }

    private void clearInput() {
        input.setText("");
        globalInputBuffer.setLength(0);
        updateEmojiButtons("");
        input.requestFocus();
    }

    private void initKeyboardListener() {
        try {
            System.out.println("[DEBUG] 启动全局输入监听...");
            windowsKeyboardListener = new WindowsGlobalKeyboardListener(new WindowsGlobalKeyboardListener.Listener() {
                @Override
                public void onCharacter(char c) {
                    SwingUtilities.invokeLater(() -> onGlobalCharacter(c));
                }

                @Override
                public void onBackspace() {
                    SwingUtilities.invokeLater(AppUI.this::onGlobalBackspace);
                }

                @Override
                public void onEnter() {
                    SwingUtilities.invokeLater(AppUI.this::onGlobalEnter);
                }
            });
            windowsKeyboardListener.start();
            isGlobalListenerActive = true;
            System.out.println("[DEBUG] 情绪输入法已启动，全程监听中...");
        } catch (Throwable t) {
            isGlobalListenerActive = false;
            System.err.println("[ERROR] 全局监听初始化失败，已降级为仅窗口模式: " + t.getMessage());
            enableHotKeyFallback();
            if (trayIcon != null) {
                trayIcon.displayMessage("情绪输入法", "全局监听失败，已切换为 Ctrl+Shift+E 呼出窗口", TrayIcon.MessageType.WARNING);
            }
        }
    }

    private void enableHotKeyFallback() {
        try {
            if (globalKeyListener == null) {
                globalKeyListener = new GlobalKeyListener(keyCode -> SwingUtilities.invokeLater(this::showWindow));
                globalKeyListener.registerGlobalHotKey();
            }
        } catch (Throwable fallbackError) {
            System.err.println("[ERROR] 热键降级模式启动失败: " + fallbackError.getMessage());
        }
    }

    private void initSystemTray() {
        if (SystemTray.isSupported()) {
            systemTray = SystemTray.getSystemTray();

            BufferedImage trayImage = createTrayImage();
            trayIcon = new TrayIcon(trayImage, "情绪输入法", null);
            trayIcon.setToolTip("情绪输入法 - 点击显示");

            trayIcon.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        showWindow();
                    }
                }
            });

            PopupMenu popup = new PopupMenu();
            MenuItem showItem = new MenuItem("显示窗口");
            showItem.addActionListener(e -> showWindow());
            MenuItem exitItem = new MenuItem("退出");
            exitItem.addActionListener(e -> {
                cleanup();
                System.exit(0);
            });
            popup.add(showItem);
            popup.addSeparator();
            popup.add(exitItem);
            trayIcon.setPopupMenu(popup);

            try {
                systemTray.add(trayIcon);
            } catch (Exception e) {
                System.out.println("托盘图标添加失败: " + e.getMessage());
            }
        } else {
            isAutoHideEnabled = false;
        }
    }

    private void showWindow() {
        frame.setVisible(true);
        frame.toFront();
        input.requestFocus();
        startFadeInAnimation();
        resetHideTimer();
    }

    private void updateEmojiButtons(String text) {
        String emotion = emotionService.detect(text);
        currentEmotion = emotion;
        updateEmotionLabel(emotion);
        List<String> list = emojiService.getEmojiList(emotion);
        btn1.setText(getNonDuplicate(list.get(0)));
        btn2.setText(getNonDuplicate(list.get(1)));
        btn3.setText(getNonDuplicate(list.get(2)));
    }

    private void updateEmotionLabel(String emotion) {
        String emoji = "😊";
        String color = "#B4B4C8";
        
        switch (emotion) {
            case "happy":
                emoji = "😄";
                color = "#FFD93D";
                break;
            case "sad":
                emoji = "😢";
                color = "#6C8EBF";
                break;
            case "angry":
                emoji = "😠";
                color = "#FF6B6B";
                break;
            case "surprised":
                emoji = "😲";
                color = "#95E1D3";
                break;
            case "shy":
                emoji = "😊";
                color = "#F38181";
                break;
            case "tired":
                emoji = "😴";
                color = "#AA96DA";
                break;
            case "thinking":
                emoji = "🤔";
                color = "#FCBAD3";
                break;
            default:
                emoji = "😊";
                color = "#B4B4C8";
                break;
        }
        
        emotionLabel.setText(emoji + " 情绪输入法");
        emotionLabel.setForeground(Color.decode(color));
    }

    private void insertEmojiAndCopy(String emoji) {
        String cleanEmoji = emoji.trim();
        if (cleanEmoji.isEmpty()) return;

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(cleanEmoji), null);

        clearInput();
        
        // 显示成功提示
        JOptionPane.showMessageDialog(frame, 
            "已复制到剪贴板: " + cleanEmoji, 
            "✓ 成功", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void startFadeInAnimation() {
        if (fadeInTimer != null) fadeInTimer.stop();
        fadeInTimer = new Timer(25, null);
        fadeInTimer.addActionListener(e -> {
            currentOpacity += 0.1f;
            if (currentOpacity >= 0.95f) {
                currentOpacity = 0.95f;
                fadeInTimer.stop();
            }
            frame.setOpacity(currentOpacity);
        });
        fadeInTimer.start();
    }

    private void startFadeOutAnimation() {
        if (fadeInTimer != null) fadeInTimer.stop();
        fadeInTimer = new Timer(25, null);
        fadeInTimer.addActionListener(e -> {
            currentOpacity -= 0.1f;
            if (currentOpacity <= 0f) {
                currentOpacity = 0f;
                fadeInTimer.stop();
                frame.setVisible(false);
            }
            frame.setOpacity(currentOpacity);
        });
        fadeInTimer.start();
    }

    private void startHideTimer() {
        if (hideTimer != null) hideTimer.stop();
        if (!isAutoHideEnabled) return;

        hideTimer = new Timer(autoHideDelay, e -> startFadeOutAnimation());
        hideTimer.setRepeats(false);
        hideTimer.start();
    }

    private void resetHideTimer() {
        if (hideTimer != null) hideTimer.stop();
        startHideTimer();
    }

    private void cleanup() {
        if (fadeInTimer != null) fadeInTimer.stop();
        if (hideTimer != null) hideTimer.stop();
        if (trayIcon != null && systemTray != null) {
            systemTray.remove(trayIcon);
        }
        try {
            if (isGlobalListenerActive) {
                if (windowsKeyboardListener != null) {
                    windowsKeyboardListener.stop();
                }
                System.out.println("[DEBUG] 全局键盘监听已停止");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] 停止键盘监听失败: " + e.getMessage());
        }
        if (globalKeyListener != null) {
            try {
                globalKeyListener.unregisterGlobalHotKey();
            } catch (Exception e) {
                System.err.println("[ERROR] 卸载全局热键失败: " + e.getMessage());
            }
        }
    }

    private String getNonDuplicate(String emoji) {
        if (emoji.equals(lastSelectedEmoji)) {
            return emoji + " ";
        }
        return emoji;
    }

    private JButton createEmojiBtn() {
        JButton btn = new JButton();
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setBackground(new Color(40, 40, 60));
        btn.setForeground(new Color(220, 220, 240));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 80), 1),
            BorderFactory.createEmptyBorder(10, 18, 10, 18)
        ));
        // 使用Segoe UI Symbol统一字体，支持所有Unicode颜文字
        btn.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // 启用文本抗锯齿渲染
        btn.putClientProperty("textAntialiasing", true);
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(55, 55, 80));
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(100, 100, 140), 2),
                    BorderFactory.createEmptyBorder(9, 17, 9, 17)
                ));
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(40, 40, 60));
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(60, 60, 80), 1),
                    BorderFactory.createEmptyBorder(10, 18, 10, 18)
                ));
            }
        });
        
        return btn;
    }

    private JButton createClearBtn() {
        JButton btn = new JButton("✕");
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setForeground(new Color(150, 150, 170));
        btn.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("清除输入");

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { 
                btn.setForeground(new Color(255, 200, 100)); 
            }
            public void mouseExited(MouseEvent e) { 
                btn.setForeground(new Color(150, 150, 170)); 
            }
        });
        return btn;
    }

    private JButton createExitBtn() {
        JButton btn = new JButton("✕");
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setForeground(new Color(150, 150, 170));
        btn.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("关闭");

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setForeground(Color.RED); }
            public void mouseExited(MouseEvent e) { btn.setForeground(new Color(150, 150, 170)); }
        });
        return btn;
    }

    private ImageIcon createEmojiIcon(String emoji) {
        JLabel label = new JLabel(emoji);
        label.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        label.setSize(16, 16);
        label.paint(g2);
        g2.dispose();
        return new ImageIcon(img);
    }

    private BufferedImage createTrayImage() {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 绘制圆形背景
        g2.setColor(new Color(70, 130, 180));
        g2.fillOval(0, 0, 16, 16);
        
        // 绘制表情
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        g2.drawString("😊", 1, 13);
        
        g2.dispose();
        return img;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AppUI::new);
    }

    private void onGlobalBackspace() {
        if (globalInputBuffer.length() > 0) {
            globalInputBuffer.deleteCharAt(globalInputBuffer.length() - 1);
            checkAndUpdateEmoji();
        }
    }

    private void onGlobalEnter() {
        globalInputBuffer.setLength(0);
    }

    private void onGlobalCharacter(char c) {
        if (c < 32) {
            return;
        }
        globalInputBuffer.append(c);
        if (globalInputBuffer.length() > 50) {
            globalInputBuffer.deleteCharAt(0);
        }
        checkAndUpdateEmoji();
    }
    
    /**
     * 检测情绪并更新UI显示
     */
    private void checkAndUpdateEmoji() {
        String text = globalInputBuffer.toString();
        if (text.isEmpty()) {
            return;
        }
        
        String emotion = emotionService.detect(text);
        
        // 如果检测到情绪，弹出窗口并更新表情符号
        if (!emotion.isEmpty() && !emotion.equalsIgnoreCase("neutral")) {
            SwingUtilities.invokeLater(() -> {
                input.setText(text);
                updateEmojiButtons(text);
                showWindow();
            });
        }
    }

}
