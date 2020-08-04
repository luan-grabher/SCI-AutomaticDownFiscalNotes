package sci.automaticdownfiscalnotes.Model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import sci.automaticdownfiscalnotes.Model.Entities.Down;

public class DownFileModel {
    private File file;
    
    private List<Down> downs =  new ArrayList<>();

    public DownFileModel(File file) {
        this.file = file;
    }

    public void setDowns() {
        
    }
    
    
}
