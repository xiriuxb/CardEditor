package controlador;

import java.io.IOException;
import java.net.IDN;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class GetFromURL {
    
    private String correctURL(String originalURL) {
        URI uri = null;
        try {
        	System.out.println(originalURL);
            URL url= new URL(originalURL);
            uri = new URI(url.getProtocol(), url.getUserInfo(), IDN.toASCII(url.getHost()), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
            
        } catch (MalformedURLException ex) {
            Logger.getLogger(GetFromURL.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(GetFromURL.class.getName()).log(Level.SEVERE, null, ex);
        }
        return  uri.toASCIIString(); 
    }

    public String textFromURL(String carta) throws IOException {
        Document doc = Jsoup.connect(correctURL("https://yugioh.fandom.com/es/wiki/"+carta.replace(":", ""))).get();
        Elements newsHeadlines = doc.select(".InfoboxDescripcion i");
        Elements pendulo = doc.select(".InfoboxPendulo i");
        String efectos = newsHeadlines.text();
        if (pendulo.hasText()) {
			efectos = efectos+"\nEFECTO PÉNDULO:\n"+pendulo.text(); 
		}
        return efectos;
    }
    
    public String getSpanishName(String engName) throws IOException {
        Document doc = Jsoup.connect(correctURL("https://yugioh.fandom.com/wiki/"+engName)).get();
        Element newsHeadlines = doc.selectFirst(".cardtable span[lang=\"es\"]");
        System.out.println(newsHeadlines.text());
        return newsHeadlines.text();
    }
    
    public ArrayList getAttrib(String carta) throws IOException {
        ArrayList<String> datos = new ArrayList<>();
        Document doc = Jsoup.connect(correctURL("https://yugioh.fandom.com/es/wiki/"+carta.replace(":", ""))).get();
        Elements newsHeadlines = doc.select(".pi-item .pi-data-value div");
        StringTokenizer st = new StringTokenizer(newsHeadlines.get(9).text()," / ");
        datos.add(newsHeadlines.get(1).text());
        datos.add(newsHeadlines.get(2).text());
        datos.add(newsHeadlines.get(5).text());
        datos.add(newsHeadlines.get(6).text());
        datos.add(newsHeadlines.get(7).text().replace("x ", ""));
        datos.add(newsHeadlines.get(8).text());
        if(datos.get(3).equals("MÁGICA")||datos.get(3).equals("TRAMPA")) {
        	datos.add("0");
        	datos.add("0");
        }else {
        	datos.add(st.nextToken());
        	if (st.hasMoreTokens()) {
				datos.add(st.nextToken());
			}else {
				datos.add("0");
			}
        }
        System.out.println(""+datos.toString());
        return datos;
    }
}


