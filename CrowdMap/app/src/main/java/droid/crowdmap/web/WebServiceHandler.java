package br.com.danieljr.crowdmap.web;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import droid.crowdmap.basededados.Dados;


/**
 * Created by ton on 08/10/15.
 */
public class WebServiceHandler {

    private static WebServiceHandler wsh = null;
    private String address = "http://flechas.ic.uff.br/urbandata";
    private WebServiceHandler(){
    }

    public static WebServiceHandler getInstance(){
        if(wsh == null) {
            wsh = new WebServiceHandler();
        }
        return wsh;
    }

    public int post(HashMap<String, Object> params){
        String post = address + "/rest/regs";
           int resp_code = 0;
        try {
            URL url = new URL(post);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setConnectTimeout(15000);
            conn.setDoInput(true);
            StringBuilder postData = new StringBuilder();

            for(Map.Entry<String, Object> param: params.entrySet()){
                if( postData.length() != 0 ) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(),"UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
            String urlParameters = postData.toString();

            OutputStreamWriter os = (OutputStreamWriter) new OutputStreamWriter(conn.getOutputStream());
            os.write(urlParameters);
            os.flush();
            os.close();
            resp_code = conn.getResponseCode();
        } catch (Exception e){}
        return resp_code;
    }

    public List<Dados> getAllData(){
        List<Dados> data = null;
        String post = address + "/rest/regs";
        String xml = "";
        try {
            URL url = new URL(post);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("Content-Type", "application/xml");
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while( ( line = reader.readLine() ) != null ){
                xml += line;
            }
            reader.close();
        } catch (Exception e){}
        data = parseXML(xml);
        return data;
    }

    private List<Dados> parseXML(String xml){
        List<Dados> data = new ArrayList<Dados>();
        try {
            DocumentBuilder docB = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = docB.parse(new ByteArrayInputStream(xml.getBytes()));
            doc.getDocumentElement().normalize();
            NodeList listOfRegs = doc.getElementsByTagName("reg");
            for(int s = 0; s < listOfRegs.getLength(); s++){
                Node regNode = listOfRegs.item(s);
                Dados dados = new Dados();
                Element regElement = (Element) regNode;

                NodeList latList = regElement.getElementsByTagName("lat");
                Element latElement = (Element) latList.item(0);
                NodeList textLatList = latElement.getChildNodes();
                dados.setLatitude(Double.valueOf(((Node) textLatList.item(0)).getNodeValue().trim()));

                NodeList lngList = regElement.getElementsByTagName("lng");
                Element lngElement = (Element) lngList.item(0);
                NodeList textLngList = lngElement.getChildNodes();
                dados.setLongitude(Double.valueOf(((Node) textLngList.item(0)).getNodeValue().trim()));

                NodeList sinalList = regElement.getElementsByTagName("sinal");
                Element sinalElement = (Element) sinalList.item(0);
                NodeList textSinalList = sinalElement.getChildNodes();
                dados.setSinal(Double.valueOf(((Node) textSinalList.item(0)).getNodeValue().trim()));

                NodeList opList = regElement.getElementsByTagName("operadora");
                Element opElement = (Element) opList.item(0);
                NodeList textOpList = opElement.getChildNodes();
                dados.setOperadora(((Node) textOpList.item(0)).getNodeValue().trim());

                data.add(dados);
            }
        } catch (Exception e) {}
        return data;
    }
}
