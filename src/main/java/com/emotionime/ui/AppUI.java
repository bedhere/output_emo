package com.emotionime.ui;

import com.emotionime.service.*;
import com.emotionime.util.GlobalKeyListener;
import com.emotionime.util.WindowsGlobalKeyboardListener;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.PopupMenu;
import java.awt.MenuItem;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;

public class AppUI {

    private Button btn1;
    private Button btn2;
    private Button btn3;
    private Button btnExit;
    private Button btnMinimize;
    private HBox emojiPanel;
    private Label emotionLabel;

    private volatile boolean hookEnabled = true;
    private volatile boolean skipFocusEvent = false;

    private EmotionService emotionService = new EmotionService();
    private EmojiService emojiService = new EmojiService();

    private Stage stage;
    private TextField input;

    private SystemTray systemTray;
    private TrayIcon trayIcon;
    private GlobalKeyListener globalKeyListener;
    private WindowsGlobalKeyboardListener windowsKeyboardListener;

    private double dragOffsetX;
    private double dragOffsetY;

    private Timeline fadeInTimeline;
    private float currentOpacity = 0f;

    private Timeline hideTimeline;
    private boolean isAutoHideEnabled = false;
    private int autoHideDelay = 8000;

    private String lastSelectedEmoji = "";
    private String currentEmotion = "neutral";

    private StringBuilder globalInputBuffer = new StringBuilder();
    private volatile boolean isGlobalListenerActive = false;

    public AppUI(Stage primaryStage) {
        this.stage = primaryStage;
        Platform.setImplicitExit(false);

        initSystemTray();

        stage.setTitle("情绪输入法");
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);
        stage.setOpacity(0);
        stage.setWidth(340);

        javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        stage.setX(screenBounds.getMaxX() - 360);
        stage.setY(screenBounds.getMaxY() - 250);

        StackPane root = new StackPane();
        root.setBackground(new Background(new BackgroundFill(
                Color.rgb(25, 25, 35, 1), new CornerRadii(8), Insets.EMPTY)));
        root.setBorder(new Border(new BorderStroke(
                Color.rgb(60, 60, 80), BorderStrokeStyle.SOLID, new CornerRadii(8), new BorderWidths(2))));
        root.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.5)));

        VBox mainLayout = new VBox();
        mainLayout.setAlignment(Pos.TOP_CENTER);

        // 标题栏
        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setBackground(new Background(new BackgroundFill(
                Color.rgb(35, 35, 50), new CornerRadii(8, 8, 0, 0, false), Insets.EMPTY)));
        titleBar.setPadding(new Insets(5, 5, 5, 8));

        emotionLabel = new Label("😊 情绪输入法");
        emotionLabel.setTextFill(Color.rgb(180, 180, 200));
        emotionLabel.setStyle("-fx-font-family: 'Segoe UI Emoji'; -fx-font-size: 14px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnMinimize = createMinimizeBtn();
        btnExit = createExitBtn();
        HBox rightButtons = new HBox(5, btnMinimize, btnExit);
        rightButtons.setAlignment(Pos.CENTER_RIGHT);

        titleBar.getChildren().addAll(emotionLabel, spacer, rightButtons);

        // 输入框 + 清除/钩子按钮
        input = new TextField();
        input.setBackground(new Background(new BackgroundFill(
                Color.rgb(30, 30, 45), null, Insets.EMPTY)));
        input.setStyle(
                "-fx-text-fill: #DCDCF0;" +
                "-fx-font-size: 15px;" +
                "-fx-font-family: 'Microsoft YaHei UI';" +
                "-fx-highlight-fill: #4A4A6A;" +
                "-fx-highlight-text-fill: white;" +
                "-fx-border-color: #3C3C50;" +
                "-fx-border-width: 0 0 1 0;" +
                "-fx-padding: 8 12 8 12;");
        input.setPromptText("在这里输入...");
        HBox.setHgrow(input, Priority.ALWAYS);

        Button inputClearBtn = createSmallBtn("清", "清除输入");
        Button hookToggleBtn = createSmallBtn("钩", "点击禁用全局监听");

        inputClearBtn.setOnAction(e -> {
            clearInput();
            input.requestFocus();
        });

        hookToggleBtn.setOnAction(e -> {
            hookEnabled = !hookEnabled;
            if (windowsKeyboardListener != null) {
                windowsKeyboardListener.setHookEnabled(hookEnabled);
            }
            if (hookEnabled) {
                hookToggleBtn.setText("钩");
                hookToggleBtn.setTooltip(new javafx.scene.control.Tooltip("点击禁用全局监听"));
                hookToggleBtn.setTextFill(Color.rgb(100, 200, 100));
            } else {
                hookToggleBtn.setText("闭");
                hookToggleBtn.setTooltip(new javafx.scene.control.Tooltip("点击启用全局监听"));
                hookToggleBtn.setTextFill(Color.rgb(200, 100, 100));
            }
        });

        HBox inputArea = new HBox(2, input, inputClearBtn, hookToggleBtn);
        inputArea.setAlignment(Pos.CENTER_LEFT);
        inputArea.setBackground(new Background(new BackgroundFill(
                Color.rgb(30, 30, 45), null, Insets.EMPTY)));

        // 颜文字按钮面板
        btn1 = createEmojiBtn();
        btn2 = createEmojiBtn();
        btn3 = createEmojiBtn();
        emojiPanel = new HBox(8, btn1, btn2, btn3);
        emojiPanel.setAlignment(Pos.CENTER);
        HBox.setHgrow(btn1, Priority.ALWAYS);
        HBox.setHgrow(btn2, Priority.ALWAYS);
        HBox.setHgrow(btn3, Priority.ALWAYS);
        emojiPanel.setBackground(new Background(new BackgroundFill(
                Color.rgb(25, 25, 35), null, Insets.EMPTY)));
        emojiPanel.setPadding(new Insets(6, 6, 6, 6));

        mainLayout.getChildren().addAll(titleBar, inputArea, emojiPanel);
        root.getChildren().add(mainLayout);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.sizeToScene();

        // 焦点监听：窗口聚焦时跳过全局键盘钩子，避免双重输入
        stage.focusedProperty().addListener((obs, oldVal, focused) -> {
            if (focused && skipFocusEvent) {
                skipFocusEvent = false;
                return;
            }
            if (windowsKeyboardListener != null) {
                windowsKeyboardListener.setWindowFocused(focused);
            }
        });

        // 边框拖拽缩放
        setupResize(root);

        // 事件监听
        btn1.setOnAction(e -> insertEmojiAndCopy(btn1.getText()));
        btn2.setOnAction(e -> insertEmojiAndCopy(btn2.getText()));
        btn3.setOnAction(e -> insertEmojiAndCopy(btn3.getText()));

        btnMinimize.setOnAction(e -> stage.hide());

        btnExit.setOnAction(e -> {
            cleanup();
            Platform.exit();
        });

        input.textProperty().addListener((obs, oldText, newText) -> {
            updateEmojiButtons(newText);
            resetHideTimer();
        });

        input.setOnKeyTyped(e -> {
            char c = e.getCharacter().charAt(0);
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
            }
        });

        input.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ENTER:
                    clearInput();
                    e.consume();
                    break;
                case ESCAPE:
                    startFadeOutAnimation();
                    e.consume();
                    break;
                case E:
                    if (e.isControlDown()) {
                        e.consume();
                    }
                    break;
                default:
                    break;
            }
        });

        // 拖拽支持
        titleBar.setOnMousePressed(e -> {
            dragOffsetX = e.getScreenX() - stage.getX();
            dragOffsetY = e.getScreenY() - stage.getY();
        });

        titleBar.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - dragOffsetX);
            stage.setY(e.getScreenY() - dragOffsetY);
        });

        titleBar.setOnMouseEntered(e -> {
            if (hideTimeline != null) hideTimeline.stop();
        });

        stage.setOnCloseRequest(e -> {
            e.consume();
            stage.hide();
        });

        stage.show();
        startFadeInAnimation();
        initKeyboardListener();
        updateEmojiButtons("");
    }

    private void clearInput() {
        input.clear();
        globalInputBuffer.setLength(0);
        updateEmojiButtons("");
        Platform.runLater(input::requestFocus);
    }

    private void initKeyboardListener() {
        try {
            System.out.println("[DEBUG] 启动全局输入监听...");
            windowsKeyboardListener = new WindowsGlobalKeyboardListener(new WindowsGlobalKeyboardListener.Listener() {
                @Override
                public void onCharacter(char c) {
                    Platform.runLater(() -> onGlobalCharacter(c));
                }

                @Override
                public void onBackspace() {
                    Platform.runLater(AppUI.this::onGlobalBackspace);
                }

                @Override
                public void onEnter() {
                    Platform.runLater(AppUI.this::onGlobalEnter);
                }

                @Override
                public void onClearRequest() {
                    Platform.runLater(() -> {
                        input.clear();
                        globalInputBuffer.setLength(0);
                        updateEmojiButtons("");
                    });
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
                trayIcon.displayMessage("情绪输入法", "全局监听失败，已切换为 Ctrl+Shift+E 呼出窗口",
                        TrayIcon.MessageType.WARNING);
            }
        }
    }

    private void enableHotKeyFallback() {
        try {
            if (globalKeyListener == null) {
                globalKeyListener = new GlobalKeyListener(keyCode -> Platform.runLater(this::showWindow));
                globalKeyListener.registerGlobalHotKey();
            }
        } catch (Throwable fallbackError) {
            System.err.println("[ERROR] 热键降级模式启动失败: " + fallbackError.getMessage());
        }
    }

    private void initSystemTray() {
        if (!SystemTray.isSupported()) {
            isAutoHideEnabled = false;
            return;
        }

        systemTray = SystemTray.getSystemTray();
        BufferedImage trayImage = createTrayImage();
        trayIcon = new TrayIcon(trayImage, "情绪输入法");
        trayIcon.setToolTip("情绪输入法 - 点击显示");

        trayIcon.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Platform.runLater(AppUI.this::showWindowWithFocus);
                }
            }
        });

        PopupMenu popup = new PopupMenu();
        MenuItem showItem = new MenuItem("显示窗口");
        showItem.addActionListener(e -> Platform.runLater(AppUI.this::showWindowWithFocus));
        MenuItem exitItem = new MenuItem("退出");
        exitItem.addActionListener(e -> {
            cleanup();
            Platform.exit();
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
    }

    private void showWindow() {
        skipFocusEvent = true;
        stage.setAlwaysOnTop(false);
        stage.show();
        // 恢复置顶但不抢焦点
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.millis(100));
        pause.setOnFinished(e -> stage.setAlwaysOnTop(true));
        pause.play();
        startFadeInAnimation();
        resetHideTimer();
    }

    /** 从托盘双击打开，需要抢焦点 */
    private void showWindowWithFocus() {
        stage.show();
        stage.toFront();
        input.requestFocus();
        startFadeInAnimation();
        resetHideTimer();
    }

    private void updateEmojiButtons(String text) {
        if (text == null) text = "";
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
        emotionLabel.setTextFill(Color.web(color));
    }

    private void insertEmojiAndCopy(String emoji) {
        String cleanEmoji = emoji.trim();
        if (cleanEmoji.isEmpty()) return;

        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(cleanEmoji);
        clipboard.setContent(content);

        clearInput();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("成功");
        alert.setHeaderText(null);
        alert.setContentText("已复制到剪贴板: " + cleanEmoji);
        alert.show();
    }

    private void startFadeInAnimation() {
        if (fadeInTimeline != null) fadeInTimeline.stop();
        currentOpacity = (float) stage.getOpacity();
        fadeInTimeline = new Timeline(new KeyFrame(Duration.millis(25), e -> {
            currentOpacity += 0.1f;
            if (currentOpacity >= 0.95f) {
                currentOpacity = 0.95f;
                fadeInTimeline.stop();
            }
            stage.setOpacity(currentOpacity);
        }));
        fadeInTimeline.setCycleCount(Timeline.INDEFINITE);
        fadeInTimeline.play();
    }

    private void startFadeOutAnimation() {
        if (fadeInTimeline != null) fadeInTimeline.stop();
        currentOpacity = (float) stage.getOpacity();
        fadeInTimeline = new Timeline(new KeyFrame(Duration.millis(25), e -> {
            currentOpacity -= 0.1f;
            if (currentOpacity <= 0f) {
                currentOpacity = 0f;
                fadeInTimeline.stop();
                stage.hide();
            }
            stage.setOpacity(currentOpacity);
        }));
        fadeInTimeline.setCycleCount(Timeline.INDEFINITE);
        fadeInTimeline.play();
    }

    private void startHideTimer() {
        if (hideTimeline != null) hideTimeline.stop();
        if (!isAutoHideEnabled) return;
        hideTimeline = new Timeline(new KeyFrame(Duration.millis(autoHideDelay), e -> startFadeOutAnimation()));
        hideTimeline.setCycleCount(1);
        hideTimeline.play();
    }

    private void resetHideTimer() {
        if (hideTimeline != null) hideTimeline.stop();
        startHideTimer();
    }

    private void cleanup() {
        if (fadeInTimeline != null) fadeInTimeline.stop();
        if (hideTimeline != null) hideTimeline.stop();
        if (trayIcon != null && systemTray != null) {
            systemTray.remove(trayIcon);
        }
        try {
            if (isGlobalListenerActive && windowsKeyboardListener != null) {
                windowsKeyboardListener.stop();
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

    private Button createEmojiBtn() {
        Button btn = new Button();
        btn.setFocusTraversable(false);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setBackground(new Background(new BackgroundFill(
                Color.rgb(40, 40, 60), new CornerRadii(6), Insets.EMPTY)));
        btn.setTextFill(Color.rgb(220, 220, 240));
        btn.setBorder(new Border(new BorderStroke(
                Color.rgb(60, 60, 80), BorderStrokeStyle.SOLID, new CornerRadii(6), new BorderWidths(1))));
        btn.setFont(Font.font("Segoe UI Symbol", FontWeight.NORMAL, 12));
        btn.setPadding(new Insets(8, 5, 8, 5));
        btn.setCursor(javafx.scene.Cursor.HAND);

        btn.setOnMouseEntered(e -> {
            btn.setBackground(new Background(new BackgroundFill(
                    Color.rgb(55, 55, 80), new CornerRadii(6), Insets.EMPTY)));
            btn.setBorder(new Border(new BorderStroke(
                    Color.rgb(100, 100, 140), BorderStrokeStyle.SOLID, new CornerRadii(6), new BorderWidths(2))));
        });
        btn.setOnMouseExited(e -> {
            btn.setBackground(new Background(new BackgroundFill(
                    Color.rgb(40, 40, 60), new CornerRadii(6), Insets.EMPTY)));
            btn.setBorder(new Border(new BorderStroke(
                    Color.rgb(60, 60, 80), BorderStrokeStyle.SOLID, new CornerRadii(6), new BorderWidths(1))));
        });

        return btn;
    }

    private Button createClearBtn() {
        Button btn = new Button("✕");
        btn.setFocusTraversable(false);
        btn.setBackground(Background.EMPTY);
        btn.setTextFill(Color.rgb(150, 150, 170));
        btn.setBorder(Border.EMPTY);
        btn.setPadding(new Insets(2, 8, 2, 8));
        btn.setCursor(javafx.scene.Cursor.HAND);
        btn.setTooltip(new javafx.scene.control.Tooltip("清除输入"));

        btn.setOnMouseEntered(e -> btn.setTextFill(Color.rgb(255, 200, 100)));
        btn.setOnMouseExited(e -> btn.setTextFill(Color.rgb(150, 150, 170)));
        return btn;
    }

    private Button createMinimizeBtn() {
        Button btn = new Button("—");
        btn.setFocusTraversable(false);
        btn.setBackground(Background.EMPTY);
        btn.setTextFill(Color.rgb(150, 150, 170));
        btn.setBorder(Border.EMPTY);
        btn.setPadding(new Insets(2, 8, 2, 8));
        btn.setCursor(javafx.scene.Cursor.HAND);
        btn.setTooltip(new javafx.scene.control.Tooltip("最小化到托盘"));

        btn.setOnMouseEntered(e -> btn.setTextFill(Color.rgb(100, 200, 255)));
        btn.setOnMouseExited(e -> btn.setTextFill(Color.rgb(150, 150, 170)));
        return btn;
    }

    private Button createSmallBtn(String text, String tooltip) {
        Button btn = new Button(text);
        btn.setFocusTraversable(false);
        btn.setMinWidth(28);
        btn.setMaxWidth(28);
        btn.setMinHeight(28);
        btn.setMaxHeight(28);
        btn.setBackground(new Background(new BackgroundFill(
                Color.rgb(40, 40, 60), new CornerRadii(4), Insets.EMPTY)));
        btn.setTextFill(Color.rgb(150, 150, 170));
        btn.setBorder(new Border(new BorderStroke(
                Color.rgb(60, 60, 80), BorderStrokeStyle.SOLID, new CornerRadii(4), new BorderWidths(1))));
        btn.setFont(Font.font("Microsoft YaHei UI", FontWeight.NORMAL, 11));
        btn.setCursor(javafx.scene.Cursor.HAND);
        btn.setTooltip(new javafx.scene.control.Tooltip(tooltip));

        btn.setOnMouseEntered(e -> {
            btn.setBackground(new Background(new BackgroundFill(
                    Color.rgb(55, 55, 80), new CornerRadii(4), Insets.EMPTY)));
            btn.setTextFill(Color.rgb(220, 220, 240));
        });
        btn.setOnMouseExited(e -> {
            btn.setBackground(new Background(new BackgroundFill(
                    Color.rgb(40, 40, 60), new CornerRadii(4), Insets.EMPTY)));
            btn.setTextFill(Color.rgb(150, 150, 170));
        });
        return btn;
    }

    private void setupResize(StackPane root) {
        final int EDGE = 5;
        final double MIN_W = 220, MIN_H = 120;

        root.setOnMouseMoved(e -> {
            if (stage.isMaximized()) return;
            boolean left = e.getX() < EDGE, right = e.getX() > root.getWidth() - EDGE;
            boolean top = e.getY() < EDGE, bottom = e.getY() > root.getHeight() - EDGE;
            if (top && left) root.setCursor(javafx.scene.Cursor.NW_RESIZE);
            else if (top && right) root.setCursor(javafx.scene.Cursor.NE_RESIZE);
            else if (bottom && left) root.setCursor(javafx.scene.Cursor.SW_RESIZE);
            else if (bottom && right) root.setCursor(javafx.scene.Cursor.SE_RESIZE);
            else if (left) root.setCursor(javafx.scene.Cursor.W_RESIZE);
            else if (right) root.setCursor(javafx.scene.Cursor.E_RESIZE);
            else if (top) root.setCursor(javafx.scene.Cursor.N_RESIZE);
            else if (bottom) root.setCursor(javafx.scene.Cursor.S_RESIZE);
            else root.setCursor(javafx.scene.Cursor.DEFAULT);
        });

        root.setOnMousePressed(e -> {
            if (stage.isMaximized()) return;
            boolean left = e.getX() < EDGE, right = e.getX() > root.getWidth() - EDGE;
            boolean top = e.getY() < EDGE, bottom = e.getY() > root.getHeight() - EDGE;
            if (left || right || top || bottom) {
                resizeLeft = left; resizeRight = right;
                resizeTop = top; resizeBottom = bottom;
                resizeStartX = e.getScreenX(); resizeStartY = e.getScreenY();
                resizeStartW = stage.getWidth(); resizeStartH = stage.getHeight();
                resizeStartNodeX = stage.getX(); resizeStartNodeY = stage.getY();
                e.consume();
            }
        });

        root.setOnMouseDragged(e -> {
            if (!resizeLeft && !resizeRight && !resizeTop && !resizeBottom) return;
            double dx = e.getScreenX() - resizeStartX, dy = e.getScreenY() - resizeStartY;
            double newX = resizeStartNodeX, newY = resizeStartNodeY;
            double newW = resizeStartW, newH = resizeStartH;

            if (resizeRight) newW = Math.max(MIN_W, resizeStartW + dx);
            if (resizeBottom) newH = Math.max(MIN_H, resizeStartH + dy);
            if (resizeLeft) { newW = Math.max(MIN_W, resizeStartW - dx); newX = resizeStartNodeX + resizeStartW - newW; }
            if (resizeTop) { newH = Math.max(MIN_H, resizeStartH - dy); newY = resizeStartNodeY + resizeStartH - newH; }

            javafx.geometry.Rectangle2D sb = javafx.stage.Screen.getPrimary().getVisualBounds();
            if (newX < sb.getMinX()) { newW -= sb.getMinX() - newX; newX = sb.getMinX(); }
            if (newY < sb.getMinY()) { newH -= sb.getMinY() - newY; newY = sb.getMinY(); }
            if (newX + newW > sb.getMaxX()) newW = sb.getMaxX() - newX;
            if (newY + newH > sb.getMaxY()) newH = sb.getMaxY() - newY;

            stage.setWidth(newW); stage.setHeight(newH);
            stage.setX(newX); stage.setY(newY);
            e.consume();
        });

        root.setOnMouseReleased(e -> {
            resizeLeft = resizeRight = resizeTop = resizeBottom = false;
            root.setCursor(javafx.scene.Cursor.DEFAULT);
        });
    }

    private boolean resizeLeft, resizeRight, resizeTop, resizeBottom;
    private double resizeStartX, resizeStartY, resizeStartW, resizeStartH, resizeStartNodeX, resizeStartNodeY;

    private Button createExitBtn() {
        Button btn = new Button("✕");
        btn.setFocusTraversable(false);
        btn.setBackground(Background.EMPTY);
        btn.setTextFill(Color.rgb(150, 150, 170));
        btn.setBorder(Border.EMPTY);
        btn.setPadding(new Insets(2, 5, 2, 5));
        btn.setCursor(javafx.scene.Cursor.HAND);
        btn.setTooltip(new javafx.scene.control.Tooltip("关闭"));

        btn.setOnMouseEntered(e -> btn.setTextFill(Color.RED));
        btn.setOnMouseExited(e -> btn.setTextFill(Color.rgb(150, 150, 170)));
        return btn;
    }

    private BufferedImage createTrayImage() {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new java.awt.Color(70, 130, 180));
        g2.fillOval(0, 0, 16, 16);

        g2.setColor(java.awt.Color.WHITE);
        g2.setFont(new java.awt.Font(java.awt.Font.DIALOG, java.awt.Font.BOLD, 11));
        java.awt.FontMetrics fm = g2.getFontMetrics();
        String smiley = ":-)";
        int x = (16 - fm.stringWidth(smiley)) / 2;
        int y = (16 - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(smiley, x, y);

        g2.dispose();
        return img;
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
        if (c < 32) return;
        globalInputBuffer.append(c);
        if (globalInputBuffer.length() > 50) {
            globalInputBuffer.deleteCharAt(0);
        }
        checkAndUpdateEmoji();
    }

    private void checkAndUpdateEmoji() {
        String text = globalInputBuffer.toString();
        if (text.isEmpty()) return;

        String emotion = emotionService.detect(text);
        if (!emotion.isEmpty() && !emotion.equalsIgnoreCase("neutral")) {
            Platform.runLater(() -> {
                input.setText(text);
                updateEmojiButtons(text);
                showWindow();
            });
        }
    }
}
