
package com.emotionime;

import com.emotionime.ui.AppUI;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(AppUI::new);
    }
}