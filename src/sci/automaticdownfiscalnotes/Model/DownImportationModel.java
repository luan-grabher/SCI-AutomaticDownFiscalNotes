package sci.automaticdownfiscalnotes.Model;

import SimpleDotEnv.Env;
import fileManager.FileManager;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sci.automaticdownfiscalnotes.Model.Entities.Down;
import sql.Database;

public class DownImportationModel {

    private List<Down> downs;
    private StringBuilder log = new StringBuilder();

    public List<Down> getDowns() {
        return downs;
    }

    public void setDowns(List<Down> downs) {
        this.downs = downs;
    }

    public void importDowns() {
        String sqlGetDocumentPortion = FileManager.getText(FileManager.getFile(".\\sql\\getDocumentPortion.sql"));
        String sqlGetFiscalEntryCFOP = FileManager.getText(FileManager.getFile(".\\sql\\getFiscalEntryCFOP.sql"));
        String sqlGetPayValue = FileManager.getText(FileManager.getFile(".\\sql\\getPayValue.sql"));
        Integer enterpriseCode = Integer.valueOf(Env.get("enterpriseCode"));

        Map<String, String> variableChanges = new HashMap<>();
        variableChanges.put("enterpriseCode", enterpriseCode.toString());

        //Percorre todas linhas
        for (Down down : downs) {
            //Troca nro documento
            variableChanges.put("document", down.getDocument());

            //Pega parcelas do documento
            ArrayList<String[]> portionResults = Database.getDatabase().select(sqlGetDocumentPortion, variableChanges);
            if (portionResults.size() > 0) {
                String[] portion = portionResults.get(0);
                Integer key = Integer.valueOf(portion[1]);
                BigDecimal liquidValue = new BigDecimal(portion[7]);

                BigDecimal pis = new BigDecimal(portion[12]);
                BigDecimal cofins = new BigDecimal(portion[13]);
                BigDecimal csll = new BigDecimal(portion[14]);
                BigDecimal irrf = new BigDecimal(portion[15]);
                BigDecimal issqn = new BigDecimal(portion[16]);
                BigDecimal inss = new BigDecimal(portion[17]);

                BigDecimal grossValue = new BigDecimal(portion[18]);

                //define troca sql a chave
                variableChanges.put("key", key.toString());

                //Busca CFOP
                ArrayList<String[]> cfopResults = Database.getDatabase().select(sqlGetFiscalEntryCFOP, variableChanges);
                if (cfopResults.size() > 0) {
                    String cfop = cfopResults.get(0)[0];

                    //Buscar valores já pagos da parcela
                    ArrayList<String[]> payValueResults = Database.getDatabase().select(sqlGetPayValue, variableChanges);
                    BigDecimal payValue = new BigDecimal(payValueResults.get(0)[0] == null ? "0" : payValueResults.get(0)[0]);
                    BigDecimal missingValue = liquidValue.add(payValue.negate());

                    //Se valor que vai ser pago for maior que o valor que falta que falta pagar, mostra aviso e nao paga
                    if(down.getValue().compareTo(missingValue) < 1){
                        System.out.println(
                                "Doc: " + down.getDocument()
                                + " - Chave: " + key
                                + " - Cfop: " + cfop
                                + " - Valor: " + down.getValue().toPlainString()
                                + " - Valor Liquido: " + liquidValue.toPlainString()
                        );
                    }else{
                        log
                            .append("\r\nO documento ")
                            .append(down.getDocument())
                            .append(" Quer baixar ")
                            .append(down.getValue())
                            .append(" e falta apenas ")
                            .append(missingValue);
                    }
                } else {
                    log
                            .append("\r\nO documento ")
                            .append(down.getDocument())
                            .append(" não possui CFOP no banco de dados da empresa ")
                            .append(enterpriseCode);
                }
            } else {
                log
                        .append("\r\nO documento ")
                        .append(down.getDocument())
                        .append(" não possui parcelas no banco de dados da empresa ")
                        .append(enterpriseCode);
            }
        }
        
        System.out.println("Log:\n" + log.toString());
    }

    private String getCFOP() {
        return "";
    }
}
