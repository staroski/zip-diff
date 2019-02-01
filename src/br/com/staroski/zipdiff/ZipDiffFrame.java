package br.com.staroski.zipdiff;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

@SuppressWarnings("serial")
final class ZipDiffFrame extends JFrame {

    final class DiffCellRenderer implements TableCellRenderer {

        private final JLabel label;
        private final JCheckBox checkBox;

        DiffCellRenderer() {
            label = new JLabel();
            label.setOpaque(true);
            checkBox = new JCheckBox();
            checkBox.setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object contents, boolean selected, boolean focus, int row, int col) {
            int alignment = getAlignment(col);
            Color color = getColor(row, col, selected);
            if (col == 0) {
                label.setText(String.valueOf(contents));
                label.setHorizontalAlignment(alignment);
                label.setBackground(color);
                return label;
            }
            checkBox.setSelected((Boolean) contents);
            checkBox.setHorizontalAlignment(alignment);
            checkBox.setBackground(color);
            return checkBox;
        }

        private int getAlignment(int col) {
            switch (col) {
                case 1:
                case 2:
                    return SwingConstants.CENTER;
                default:
                    return SwingConstants.LEFT;
            }
        }

        private Color getColor(int row, int col, boolean selected) {
            // if (selected) {
            // return LIGHT_BLUE;
            // }
            final Color color;
            ZipDiff diff = entries.get(row);
            if (diff.isOnLeft() && diff.isOnRight()) {
                color = LIGHT_GREEN;
            } else if (diff.isOnLeft() && col == 2) {
                color = LIGHT_RED;
            } else if (diff.isOnRight() && col == 1) {
                color = LIGHT_RED;
            } else {
                color = LIGHT_YELLOW;
            }
            if (selected) {
                return color.darker();
            }
            return color;
        }
    }

    final class TableModelDiff extends AbstractTableModel {

        private String[] columns = new String[] { "Entry", "", "" };

        @Override
        public Class<?> getColumnClass(int col) {
            switch (col) {
                case 1:
                case 2:
                    return Boolean.class;
                default:
                    return String.class;
            }
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int col) {
            return columns[col];
        }

        @Override
        public int getRowCount() {
            return entries.size();
        }

        @Override
        public Object getValueAt(int row, int col) {
            ZipDiff diff = entries.get(row);
            switch (col) {
                case 0:
                    return diff.getEntry();
                case 1:
                    return diff.isOnLeft();
                case 2:
                    return diff.isOnRight();
                default:
                    return null;
            }
        }

        public void update(File left, File right) {
            this.columns[1] = left != null ? left.getName() : "";
            this.columns[2] = right != null ? right.getName() : "";
            fireTableStructureChanged();
            fireTableDataChanged();
        }
    }

    private static final Color LIGHT_GREEN = new Color(0xCCFFCC);
    private static final Color LIGHT_YELLOW = new Color(0xFFFF99);
    private static final Color LIGHT_RED = new Color(0xFF8080);

    private FilePanel filePanelLeft;
    private FilePanel filePanelRight;
    private TableModelDiff tableModel;

    private List<ZipDiff> entries = new ArrayList<>();

    ZipDiffFrame() {
        super("ZipDiffExecutor");
        setIconImage(loadIcon());
        filePanelLeft = new FilePanel("Left file:");
        filePanelRight = new FilePanel("Right file:");

        JPanel panelNorth = new JPanel(new GridLayout(1, 2));
        panelNorth.add(filePanelLeft);
        panelNorth.add(filePanelRight);

        JButton buttonCompare = new JButton("Compare");
        buttonCompare.addActionListener(event -> compare());
        JPanel panelSouth = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelSouth.add(buttonCompare);

        tableModel = new TableModelDiff();
        JTable table = new JTable(tableModel);
        DiffCellRenderer renderer = new DiffCellRenderer();
        table.setDefaultRenderer(String.class, renderer);
        table.setDefaultRenderer(Boolean.class, renderer);
        JScrollPane panelCenter = new JScrollPane(table);
        panelCenter.setBorder(BorderFactory.createTitledBorder("Differences"));

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(panelNorth, BorderLayout.NORTH);
        panel.add(panelCenter, BorderLayout.CENTER);
        panel.add(panelSouth, BorderLayout.SOUTH);
        setContentPane(panel);
        setMinimumSize(new Dimension(640, 480));
    }

    private void compare() {
        File leftFile = filePanelLeft.getSelectedFile();
        File rightFile = filePanelRight.getSelectedFile();
        if (leftFile == null || rightFile == null) {
            JOptionPane.showMessageDialog(this, "Please choose both the left and right files to be compared!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        final Cursor cursor = getCursor();
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            List<String> leftDump = new ZipDumper(leftFile).ignore("*.class").expand("*.ear", "*.war").dump();
            List<String> rightDump = new ZipDumper(rightFile).ignore("*.class").expand("*.ear", "*.war").dump();
            entries = ZipDiff.compare(leftDump, rightDump);
            tableModel.update(leftFile, rightFile);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, e.getMessage(), e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(cursor);
        }
    }

    private Image loadIcon() {
        try {
            InputStream input = getClass().getResourceAsStream("/box_128x128.png");
            return ImageIO.read(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
