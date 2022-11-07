import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;   
File fileToUpload = null;
String fileProcessingIndicator = null;
StringBuffer response = new StringBuffer();
HttpURLConnection urlConnection  = null;
BufferedReader in = null;
String endPointSIOPI = (String)wfc.getWFContent("endPoint");
String proposalNumber = (String)wfc.getWFContent("numeroPropostaIndividual");
String ssoToken = (String)wfc.getWFContent("sso_token");
String fileName = (String)wfc.getWFContent("fileName");
String antivirus = (String)wfc.getWFContent("antivirus");
String hashMD5 = (String)wfc.getWFContent("hashMD5");
String documentNumber = (String)wfc.getWFContent("numeroDocumento");
String antivirusMountPoint = (String)wfc.getWFContent("antivirusMountPoint");
try{
   URL serverUrl = new URL(endPointSIOPI+proposalNumber+"/documents/"+documentNumber);
   urlConnection = (HttpURLConnection) serverUrl.openConnection();
   String boundaryString = Long.toHexString(System.currentTimeMillis());
   String fileUrl = antivirusMountPoint+fileName;
   if (antivirus == "OK"){
      fileToUpload = new File(fileUrl);
   }  
   urlConnection.setDoOutput(true);
   urlConnection.setRequestMethod("PUT");
   urlConnection.addRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundaryString);
   urlConnection.addRequestProperty("Authorization","Bearer " + ssoToken);
   urlConnection.addRequestProperty("Accept","application/json");
   OutputStream outputStreamToRequestBody = urlConnection.getOutputStream();
   BufferedWriter httpRequestBodyWriter =     new BufferedWriter(new OutputStreamWriter(outputStreamToRequestBody));
   httpRequestBodyWriter.write("--" + boundaryString + "\r\n");
   httpRequestBodyWriter.write("Content-Disposition: form-data; name=\"hashMD5\"\r\n");
   httpRequestBodyWriter.write("\r\n");
   httpRequestBodyWriter.write(hashMD5);
   httpRequestBodyWriter.write("\r\n--" + boundaryString + "\r\n");
   if (antivirus == "OK"){
      fileProcessingIndicator = "1";
   }
   else{    
      fileProcessingIndicator = "2";  
   }  
   httpRequestBodyWriter.write("Content-Disposition: form-data; name=\"processamentoArquivo\"\r\n");
   httpRequestBodyWriter.write("\r\n");
   httpRequestBodyWriter.write(fileProcessingIndicator);
   httpRequestBodyWriter.write("\r\n--" + boundaryString + "\r\n");
   if (antivirus == "OK"){
       httpRequestBodyWriter.write("Content-Disposition: form-data;" + "name=\"arquivo\";" + "filename=\""+ fileToUpload.getName() +"\""+"\r\n"
       + "Content-Type: application/octet-stream\r\n");
       httpRequestBodyWriter.write("\r\n");
   }else{   
       httpRequestBodyWriter.write("Content-Disposition: form-data; name=\"codigoMensagemProcessamento\"\r\n");
       httpRequestBodyWriter.write("\r\n");
       httpRequestBodyWriter.write("1");
       httpRequestBodyWriter.write("\r\n--" + boundaryString + "\r\n");
       httpRequestBodyWriter.write("Content-Disposition: form-data; name=\"mensagemProcessamento\"\r\n");
       httpRequestBodyWriter.write("\r\n");
       httpRequestBodyWriter.write(antivirus);
    }
    httpRequestBodyWriter.flush();    
    if (antivirus == "OK"){
       FileInputStream inputStreamToLogFile = new FileInputStream(fileToUpload);
       int bytesRead;
       byte[] dataBuffer = new byte[1024];
       while((bytesRead = inputStreamToLogFile.read(dataBuffer)) != -1) {
          outputStreamToRequestBody.write(dataBuffer, 0, bytesRead);
       }
       outputStreamToRequestBody.flush();          
       httpRequestBodyWriter.write("\r\n--" + boundaryString + "--\r\n");         
       httpRequestBodyWriter.flush();          
       outputStreamToRequestBody.close();  
    }else{        
       httpRequestBodyWriter.write("\r\n--" + boundaryString + "--\r\n");             
       httpRequestBodyWriter.flush();      
    }
    httpRequestBodyWriter.close();    
    if(urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
       in = new BufferedReader(  new InputStreamReader(urlConnection.getErrorStream()));
    }else{       
       in = new BufferedReader(  new InputStreamReader(urlConnection.getInputStream()));
    }  
    String inputLine;
    while ((inputLine = in.readLine()) != null) {
      response.append(inputLine);        
    }        
}catch(Exception ex){          
    log.log("ERROR /hashMD5/DOCUMENTO/PROPOSTA_INDIVIDUAL " +hashMD5+"/"+documentNumber+"/"+proposalNumber + ex.getMessage() + " HTTP STATUS CODE: " + Integer.toString(urlConnection.getResponseCode()));
    wfc.addWFContent("HTTPResponse",ex.getMessage());   
    wfc.addWFContent("ResponseCode",Integer.toString(urlConnection.getResponseCode()));
    throw new Exception("ERROR: "+ ex.getMessage() ); 
}finally{   
    urlConnection.disconnect();
    if (in != null){
       in.close();
    }
}
wfc.setAdvancedStatus("PUT");         
wfc.setBasicStatus(000);         
wfc.addWFContent("HTTPResponse",response.toString());   
String responseCode = Integer.toString(urlConnection.getResponseCode());  
wfc.addWFContent("ResponseCode",responseCode);   
log.log("/hashMD5/DOCUMENTO/PROPOSTA " +hashMD5+"/"+documentNumber+"/"+proposalNumber + response.toString()+"Code:"+responseCode);  
log.log("ENDPOINT: " + endPointSIOPI+proposalNumber+"/documents/"+documentNumber);
return "000";
