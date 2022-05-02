package automaticdownfiscalnotes;

import Entity.Executavel;
import Executor.Execution;
import automaticdownfiscalnotes.Control.Controller;
import fileManager.FileManager;
import fileManager.Selector;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import org.ini4j.Ini;

public class SCIAutomaticDownFiscalNotes {

    private static String iniPath = "";
    public static Ini ini;

    public static void main(String[] args) {
        iniPath = args.length > 0 ? args[0] : "";

        //Get Config fileType in ini, if not exist, set default ".xls"
        String fileType = ini.get("Config", "fileType");
        if (fileType == null) {
            fileType = ".xls";
        }

        //if fileType not start with ".", add "."
        if (!fileType.startsWith(".")) {
            fileType = "." + fileType;
        }

        JOptionPane.showMessageDialog(null, "Selecione o arquivo de Recebimentos com Retenção do plune com as baixas a serem feitas:");
        File file = Selector.selectFile(
            System.getProperty("user.home"), 
            fileType.substring(1).toUpperCase(),
             fileType.toLowerCase()
        );

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
