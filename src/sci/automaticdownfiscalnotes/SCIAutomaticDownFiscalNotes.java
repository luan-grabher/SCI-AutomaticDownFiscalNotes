package sci.automaticdownfiscalnotes;

import java.io.File;
import javax.swing.JOptionPane;
import sci.automaticdownfiscalnotes.Control.Controller;

public class SCIAutomaticDownFiscalNotes {
    
    public static void main(String[] args) {
        JOptionPane.showMessageDialog(null, "Selecione o arquivo de Recebimentos com Retenção do plune com as baixas a serem feitas:");
        File file = Selector.Arquivo.selecionar(System.getProperty("user.home"), "XLS", "xls");
        
        execute(file);
    }
    
    public static void execute(File file){
        Controller controller =  new Controller();
        controller.setDownFile(file);
        
        controller.new connectToDatabase().run();
        controller.new setDownFile().run();
        controller.new importDowns().run();
    }
}
