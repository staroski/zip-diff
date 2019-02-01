package br.com.staroski.zipdiff;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
final class FilePanel extends JPanel {

    private static JFileChooser fileChooser;

    private static JFileChooser getFileChooser() {
        if (fileChooser == null) {
            fileChooser = new JFileChooser(System.getProperty("user.dir"));
            fileChooser.setFileHidingEnabled(false);
        }
        return fileChooser;
    }

    private JTextField textFieldFile;

    FilePanel(String label) {
        textFieldFile = new JTextField();
        JButton button = new JButton("...");
        button.setToolTipText("Choose file");
        button.setPreferredSize(new Dimension(26, 26));
        button.addActionListener(event -> chooseFile());

        setLayout(new BorderLayout());
        add(textFieldFile, BorderLayout.CENTER);
        add(button, BorderLayout.EAST);
        setBorder(BorderFactory.createTitledBorder(label));
    }

    public File getSelectedFile() {
        String path = textFieldFile.getText();
        if (path == null || (path = path.trim()).isEmpty()) {
            return null;
        }
        File file = new File(path);
        if (file.exists()) {
            return file;
        }
        return null;
    }

    private void chooseFile() {
        JFileChooser chooser = getFileChooser();
        int option = chooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            textFieldFile.setText(file.getAbsolutePath());
        }
    }
}
