package sci.automaticdownfiscalnotes.Control;

import Entity.Executavel;
import SimpleDotEnv.Env;
import fileManager.FileManager;
import java.io.File;
import sci.automaticdownfiscalnotes.Model.DownFileModel;
import sql.Database;

public class Controller {
    public Integer onlineConferenceKey = Integer.valueOf(Env.get("onlineConferenceKey"));
    public Integer onlinePlan = Integer.valueOf(Env.get("onlinePlan"));
    public Integer downType = Integer.valueOf(Env.get("downType"));
    public Integer enterpriseCode = Integer.valueOf(Env.get("enterpriseCode"));
    
    private File downFile;
    
    //Models
    private DownFileModel downFileModel = new DownFileModel();

    public File getDownFile() {
        return downFile;
    }

    public void setDownFile(File downFile) {
        this.downFile = downFile;
    }        
    
    public class connectToDatabase extends Executavel{

        public connectToDatabase() {
            name = "Conectando ao banco de dados...";
        }

        @Override
        public void run() {
            Database.setStaticObject(new Database(FileManager.getFile(Env.get("databaseFilePath"))));
            if(!Database.getDatabase().testConnection()){
                throw new Error("Erro ao conectar ao banco de dados.");
            }
        }
        
        
    }
    
    public class setDownFile extends Executavel{

        public setDownFile() {
            name = "Definindo o arquivo do modelo de baixas.";
        }

        @Override
        public void run() {
            downFileModel.setFile(downFile);
            downFileModel.setDowns();
        }
        
    }
}
