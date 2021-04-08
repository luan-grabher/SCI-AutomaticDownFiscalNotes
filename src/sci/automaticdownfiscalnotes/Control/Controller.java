package sci.automaticdownfiscalnotes.Control;

import Entity.Executavel;
import fileManager.FileManager;
import java.io.File;
import sci.automaticdownfiscalnotes.Model.DownFileModel;
import sci.automaticdownfiscalnotes.Model.DownImportationModel;
import static sci.automaticdownfiscalnotes.SCIAutomaticDownFiscalNotes.ini;
import sql.Database;

public class Controller {

    public Integer onlineConferenceKey = Integer.valueOf(ini.get("Config", "onlineConferenceKey"));
    public Integer onlinePlan = Integer.valueOf(ini.get("Config", "onlinePlan"));
    public Integer downType = Integer.valueOf(ini.get("Config", "downType"));
    public Integer enterpriseCode = Integer.valueOf(ini.get("Config", "enterpriseCode"));

    private File downFile;

    //Models
    private DownFileModel downFileModel = new DownFileModel();
    private DownImportationModel downImportationModel = new DownImportationModel();

    public File getDownFile() {
        return downFile;
    }

    public void setDownFile(File downFile) {
        this.downFile = downFile;
    }

    public class connectToDatabase extends Executavel {

        public connectToDatabase() {
            name = "Conectando ao banco de dados...";
        }

        @Override
        public void run() {
            Database.setStaticObject(new Database(FileManager.getFile(ini.get("Config", "databaseFilePath"))));
            if (!Database.getDatabase().testConnection()) {
                throw new Error("Erro ao conectar ao banco de dados.");
            }
        }

    }

    public class setDownFile extends Executavel {

        public setDownFile() {
            name = "Definindo o arquivo do modelo de baixas.";
        }

        @Override
        public void run() {
            downFileModel.setFile(downFile);
            downFileModel.setDowns();

            downImportationModel.setDowns(downFileModel.getDowns());
        }

    }

    public class importDowns extends Executavel {

        public importDowns() {
            name = "Importando baixas";
        }

        @Override
        public void run() {
            downImportationModel.importDowns();
        }
    }

    public class saveLog extends Executavel {

        public saveLog() {
            name = "Salvando Log";
        }

        @Override
        public void run() {
            FileManager.save(
                    new File(
                            System.getProperty("user.home")
                            + "\\desktop\\LOG Baixar Notas.csv"),
                     downImportationModel.getLog().toString()
            );
        }

    }
}
