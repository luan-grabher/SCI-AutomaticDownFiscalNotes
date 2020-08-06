package sci.automaticdownfiscalnotes;

import Entity.Executavel;
import Executor.Execution;
import SimpleDotEnv.Env;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import sci.automaticdownfiscalnotes.Control.Controller;

public class SCIAutomaticDownFiscalNotes {

    public static void main(String[] args) {
        JOptionPane.showMessageDialog(null, "Selecione o arquivo de Recebimentos com Retenção do plune com as baixas a serem feitas:");
        File file = Selector.Arquivo.selecionar(System.getProperty("user.home"), "XLS", "xls");

        execute(file);
    }

    public static void execute(File file) {
        try {
            Env.setPath("AutomaticDownFiscalNotes");

            Controller controller = new Controller();
            controller.setDownFile(file);
            
            List<Executavel> execs = new ArrayList<>();

            execs.add(controller.new connectToDatabase());
            execs.add(controller.new setDownFile());
            execs.add(controller.new importDowns());
            execs.add(controller.new saveLog());
            
            Execution execution =  new Execution("Baixar notas " + file.getName());
            execution.setExecutables(execs);
            execution.runExecutables();
            execution.endExecution();            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erro: " + e.getMessage(), "Ocorreu um erro!", JOptionPane.ERROR_MESSAGE);
        }
    }
}
