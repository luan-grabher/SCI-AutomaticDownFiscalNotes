package automaticdownfiscalnotes;

import Entity.Executavel;
import Executor.Execution;
import automaticdownfiscalnotes.Control.Controller;
import fileManager.FileManager;
import fileManager.Selector;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

public class SCIAutomaticDownFiscalNotes {

    private static String iniPath = "";
    public static Ini ini;

    public static void main(String[] args) {
        try{

            //user select the INI file
            userSelectIniOnList();

            System.out.println("iniPath: " + iniPath);

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
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    private static void userSelectIniOnList() throws Exception{
        //get ini 'AutomaticDownFiscalNotes' as config
        Ini config = new Ini(FileManager.getFile("AutomaticDownFiscalNotes.ini"));

        //get section 'inis'
        Ini.Section inis = config.get("inis");

        //for each ini in 'inis', create option to select, after show Joption pane to user 'Qual empresa você deseja executar?'
        List<String> options = new ArrayList<>();
        for (String ini : inis.keySet()) {
            //add ini to options
            options.add(ini);
        }

        String iniSelected = (String) JOptionPane.showInputDialog(null, "Qual empresa você deseja executar?", "Selecione a empresa", JOptionPane.QUESTION_MESSAGE, null, options.toArray(), options.get(0));

        //if user not select ini, throw exception
        if (iniSelected == null) {
            throw new Exception("Não foi selecionado nenhuma empresa.");
        }

        //set iniPath to ini selected
        iniPath = inis.get(iniSelected);

        ini = new Ini(FileManager.getFile(iniPath + ".ini"));
    }

    public static void execute(File file) {
        try {
            if (file != null) {                

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
