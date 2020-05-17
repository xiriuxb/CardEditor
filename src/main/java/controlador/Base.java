/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controlador;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Jorge
 */
public class Base {
    
    private JSONArray arrayJSON = new JSONArray();
    private String rutaJSONArray;
    
    public Base(){
    }
    
    public Base(String ruta){
    	this.rutaJSONArray = ruta;
    }
    
    public String getRutaJSONArray() {
		return rutaJSONArray;
	}

	public void setRutaJSONArray(String rutaJSONArray) {
		this.rutaJSONArray = rutaJSONArray;
	}

	public void setArrayJSON(JSONArray arrayJSON) {
    	this.arrayJSON = arrayJSON;
    }
    
    public JSONArray getArrayJSON() {
    	return this.arrayJSON;
    }
    
    private void readJSON() throws IOException, FileNotFoundException {
        JSONParser jsonParser = new JSONParser();
		FileReader reader = new FileReader(this.rutaJSONArray);
		try {
			Object obj = jsonParser.parse(reader);
			setArrayJSON( (JSONArray) obj);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    }
    
    public Object[] getCampo(String campo) throws IOException, ParseException{
    	ArrayList<String> datosDeCampo = new ArrayList<>();
    	System.out.println("==============");
    	try {
			readJSON();
			arrayJSON.forEach( (t) -> {
				JSONObject name = (JSONObject) t;
				System.out.println(name.get(campo));
				datosDeCampo.add((String) name.get(campo));
			});
			return datosDeCampo.toArray();
		} catch (NullPointerException e) {
			System.err.println("Looool");
			return datosDeCampo.toArray();
		}
        
    }
    
    public void saveCard(JSONObject card){
		this.arrayJSON.add(card);
		writeJSON(this.arrayJSON);
    }
    
    public void deleteCard(int numCarta){
        getArrayJSON().remove(arrayJSON.get(numCarta));
        writeJSON(arrayJSON);
    }
    
    private void writeJSON(JSONArray cartas){
        try (FileWriter file = new FileWriter(this.rutaJSONArray)) {
            file.write(cartas.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public JSONObject getCard(int numeroCarta){
        return (JSONObject)getArrayJSON().get(numeroCarta);
    }
}
