package br.com.staroski.zipdiff;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.swing.UIManager;
import javax.swing.WindowConstants;

public final class ZipDiffExecutor {

    public static void main(String[] args) {
        try {
            ZipDiffExecutor program = new ZipDiffExecutor();
            if (args == null || args.length == 0) {
                program.openGUI();
            } else if (args.length == 2) {
                program.execute(args[0], args[1]);
            } else {
                program.showUsage();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private ZipDiffExecutor() {}

    private void execute(String in, String out) throws IOException {
        System.out.println("creating dump from \"" + in + "\" into \"" + out + "\"...");
        ZipDumper dumper = new ZipDumper(in);
        List<String> entries = dumper.ignore("*.class").expand("*.ear", "*.war").dump();
        PrintWriter writer = new PrintWriter(out);
        for (String entry : entries) {
            writer.println(entry);
        }
        writer.flush();
        writer.close();
        System.out.println("dump finished!");
    }

    private void openGUI() throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        ZipDiffFrame frame = new ZipDiffFrame();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void showUsage() {
        String mainClass = ZipDiffExecutor.class.getName();
        System.err.println("for GUI run:");
        System.err.println("    <java> " + mainClass);
        System.err.println();
        System.err.println("for command line run:");
        System.err.println("    <java> " + mainClass + " <input> <output>");
        System.err.println("where:");
        System.err.println("    <java>:    virtual machine to launch application");
        System.err.println("    <input>:   input zip file to be readed");
        System.err.println("    <output>:  output dump file to be writed");
    }
}
