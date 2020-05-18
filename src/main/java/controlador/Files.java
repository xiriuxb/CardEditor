/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controlador;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import modelo.Card;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Jorge
 */
public class Files {
    private String ruta;
    static String engName = "english_name";
    static String spaName = "spanish_name";
    static String attrib = "attribute";
    static String type = "type";
    static String text = "text";
    static String level = "level";
    static String atk = "ATK";
    static String def = "DEF";
    Base db = new Base(this.getRuta());
    
    public Files() {
    	this.ruta = "";
    }
    public String getRuta() {
		return ruta;
	}

	public void setRuta(String ruta) {
		this.ruta = ruta;
	}

    public TableModel loadJsonToTable() throws NullPointerException, IOException, ParseException{
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
            return false;
            }
        };
        tableModel.addColumn("Cartas", db.getCampo(spaName).toArray());
        return tableModel;
    }
    
    public int verificarCampo(Object jObject) {
    	int error = 0;
    	switch (jObject.getClass().toString()) {
		case "class javax.swing.JTextArea":
			if (((JTextArea)jObject).getText().equals("")) {
				((JTextArea)jObject).setBackground(Color.red);
				error++;
			}
			break;
		
		case "class javax.swing.JTextField":
			if (((JTextField)jObject).getText().equals("")) {
				((JTextField)jObject).setBackground(Color.red);
				error++;
			}
			break;
			
		case "class javax.swing.JComboBox":
			if (((JComboBox<?>)jObject).getSelectedItem().toString().equals("")) {
				((JComboBox<?>)jObject).setBackground(Color.red);
				error++;
			}
			break;

		default:
			break;
		}
    	return error;
    }
    
    public void addCard(String spanish_name, String english_name, String attribute, 
            String type, String text, int level, int atk, int def){
        
        if(db.getRutaJSONArray()== "") {
        	JOptionPane.showMessageDialog(null, "No se ha cargado ninguna base");
        }else {
            JSONObject newCard = new JSONObject();
            newCard.put("english_name", english_name);
            newCard.put("spanish_name", spanish_name);
            newCard.put("attribute", attribute);
            newCard.put("type", type);
            newCard.put("text", text);
            newCard.put("level", level);
            newCard.put("ATK", atk);
            newCard.put("DEF", def);
        	db.saveCard(newCard);
        }
    }
    
    public Card getSelectedCard(int numeroCarta){
        Card carta = new Card();
        carta.setEngName((String) db.getCard(numeroCarta).get(engName));
        carta.setSpaName((String) db.getCard(numeroCarta).get(spaName));
        carta.setAttribute((String) db.getCard(numeroCarta).get(attrib));
        carta.setType((String) db.getCard(numeroCarta).get(type));
        carta.setText((String) db.getCard(numeroCarta).get(text));
        carta.setLevel( Math.toIntExact((Long)db.getCard(numeroCarta).get(level)));
        carta.setAttak(Math.toIntExact((Long) db.getCard(numeroCarta).get(atk)));
        carta.setDefense(Math.toIntExact((Long) db.getCard(numeroCarta).get(def)));
        return carta;
    }
    
    public void deleteCrd(int numCard){
        db.deleteCard(numCard);
    }
    
    private static FileFilter filtroParaFChoser(){
        return new FileFilter() {
        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
            	return true;
            } else {
            	return f.getName().toLowerCase().endsWith(".json");
            }
        }
        @Override
        public String getDescription() {
            return "JSON Documents (*.json)";
        }
        };
    }
    
    public void saveChooser(Component parent) { 
    	String ruta = "";
    	JFileChooser jFileCh = new JFileChooser("user.home");
    	jFileCh.setFileFilter(filtroParaFChoser());
    	int userSelection = jFileCh.showSaveDialog(parent);
    	if (userSelection == JFileChooser.APPROVE_OPTION) {
    		try {
                ruta = jFileCh.getSelectedFile().getAbsolutePath()+".json";
                FileWriter fw = new FileWriter(ruta);
                this.setRuta(ruta);
                db.setRutaJSONArray(ruta);
                fw.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
    	}
    }
    
    public void loadChooser(Component parent) { 
    	JFileChooser jFileCh = new JFileChooser("user.home");
    	jFileCh.setFileFilter(filtroParaFChoser());
    	int result = jFileCh.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jFileCh.getSelectedFile();
            db.setRutaJSONArray(selectedFile.getAbsolutePath());
            this.setRuta(db.getRutaJSONArray());
        }
    }
    
    public boolean verificarSiExiste(String value) {
    	boolean existe = false;
    	try {
			if(db.getCampo(spaName).contains(value)) {
				existe= true;
			}
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return existe;
    }
}

