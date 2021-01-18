package sci.automaticdownfiscalnotes;

import Entity.Executavel;
import Executor.Execution;
import SimpleDotEnv.Env;
import fileManager.FileManager;
import fileManager.Selector;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import sci.automaticdownfiscalnotes.Control.Controller;

public class SCIAutomaticDownFiscalNotes {

    private static String envPath = "";

    public static void main(String[] args) {
        envPath = args.length > 0 ?args[0]:"";
        envPath = "335";
        
        JOptionPane.showMessageDialog(null, "Selecione o arquivo de Recebimentos com Retenção do plune com as baixas a serem feitas:");
        File file = Selector.selectFile(System.getProperty("user.home"), "XLS", "xls");

        execute(file);
    }

    public static void execute(File file) {
        try {
            //Define o env path se estiver definido, se não, fica como está
            if (!envPath.equals("")) {
                Env.setPath(envPath);
            }
            
            if(FileManager.getFile(envPath + ".env").exists()){            
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
            }else{
                throw new Exception("Arquivo de configuração '" + envPath + ".env' não encontrado! Contate o programador!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erro: " + e.getMessage(), "Ocorreu um erro!", JOptionPane.ERROR_MESSAGE);
        }
    }
}
