package sci.automaticdownfiscalnotes;

import Entity.Executavel;
import Executor.Execution;
import fileManager.FileManager;
import fileManager.Selector;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import org.ini4j.Ini;
import sci.automaticdownfiscalnotes.Control.Controller;

public class SCIAutomaticDownFiscalNotes {

    private static String iniPath = "";
    public static Ini ini;

    public static void main(String[] args) {
        iniPath = args.length > 0 ? args[0] : "";

        JOptionPane.showMessageDialog(null, "Selecione o arquivo de Recebimentos com Retenção do plune com as baixas a serem feitas:");
        File file = Selector.selectFile(System.getProperty("user.home"), "XLS", ".xls");

        execute(file);
    }

    public static void execute(File file) {
        try {
            if (file != null) {
                ini = new Ini(FileManager.getFile(iniPath + ".ini"));

                Controller controller = new Controller();
                controller.setDownFile(file);

                List<Executavel> execs = new ArrayList<>();

                execs.add(controller.new connectToDatabase());
                execs.add(controller.new setDownFile());
                execs.add(controller.new importDowns());
                execs.add(controller.new saveLog());

                Execution execution = new Execution("Baixar notas " + file.getName());
                execution.setExecutables(execs);
                execution.runExecutables();
                execution.endExecution();
            } else {
                throw new Exception("Arquivo inválido!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erro: " + e.getMessage(), "Ocorreu um erro!", JOptionPane.ERROR_MESSAGE);
        }
    }
}
