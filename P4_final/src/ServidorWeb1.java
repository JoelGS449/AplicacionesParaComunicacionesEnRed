import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
		
class Manejador implements Runnable{
    protected Socket socket;
    protected PrintWriter pw;
    protected BufferedOutputStream bos;
    protected BufferedReader br;
    DataOutputStream dos;
    DataInputStream dis;
    protected String FileName;

    public Manejador(Socket _socket){
        this.socket=_socket;
    }

    public void run()
    {
        try{
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
            //String line=br.readLine();
            byte[] b = new byte[1024];
            int t = dis.read(b);
            String peticion = new String(b,0,t);
            //System.out.println("t: "+t);
            if(peticion==null)
            {
                StringBuffer sb = new StringBuffer();
                sb.append("<html><head><title>Servidor WEB\n");
                sb.append("</title><body bgcolor=\"#AACCFF\"<br>Linea Vacia</br>\n");
                sb.append("</body></html>\n");
                dos.write(sb.toString().getBytes());
                dos.flush();
                socket.close();
                return;
            }
            
            System.out.println("\nCliente Conectado desde: "+socket.getInetAddress());
            System.out.println("Por el puerto: "+socket.getPort());
            System.out.println("Datos: "+peticion+"\r\n\r\n");
								
            StringTokenizer st1= new StringTokenizer(peticion,"\n");
            String line = st1.nextToken();
            if(line.toUpperCase().startsWith("GET")){
                if(line.indexOf("?")==-1){
                    getArch(line);
                    if(FileName.compareTo("")==0)
                    {
                        System.out.println("SEND A");
                        SendA("index.html",dos);
                    }
                    else
                    {
                        SendA(FileName,dos);
                    }
                            //System.out.println(FileName);
                }
                else
                {
                    StringTokenizer tokens=new StringTokenizer(line,"?");
                    String req_a=tokens.nextToken();
                    String req=tokens.nextToken();
                    //System.out.println("Token1: "+req_a);
                    //System.out.println("Token2: "+req);
                    String parametros = req.substring(0, req.indexOf(" "))+"\n";
                    System.out.println("parametros: "+parametros);
                    StringBuffer respuesta= new StringBuffer();

                    respuesta.append("HTTP/1.0 200 Okay \n");
                    String fecha= "Date: " + new Date()+" \n";
                    respuesta.append(fecha);
                    String tipo_mime = "Content-Type: text/html; charset=utf-8\n\n";
                    respuesta.append(tipo_mime);
                    respuesta.append("<html><head><title>SERVIDOR WEB</title></head>\n");
                    respuesta.append("<body bgcolor=\"#AACCFF\"><center><h1><br>Parámetros obtenidos:</br></h1><h3><b>\n");
                    respuesta.append(parametros);
                    respuesta.append("</b></h3>\n");
                    respuesta.append("</center></body></html>\n\n");
                    System.out.println("Respuesta: "+respuesta);
                    dos.write(respuesta.toString().getBytes());
                    dos.flush();
                    dos.close();
                    socket.close();
                }						
            }else if(line.toUpperCase().startsWith("POST")){
                String[] datosUsuario = getDatosUsuario(peticion);
				
				StringBuffer respuesta= new StringBuffer();
				
				if(datosUsuario != null){
					respuesta.append("HTTP/1.0 200 Okay \n");
					String fecha= "Date: " + new Date()+" \n";
					respuesta.append(fecha);
					String tipo_mime = "Content-Type: text/html; charset=utf-8 \n\n";
					respuesta.append(tipo_mime);
					respuesta.append("<html><head><title>SERVIDOR WEB</title></head>\n");
					respuesta.append("<body bgcolor=\"#5A0000\" style=\"color:white;\"><center><h1><br>Datos obtenidos:</br></h1><h3><b>\n");
					respuesta.append("<br>Usuario: " + datosUsuario[0] + "\n");
					respuesta.append("<br>Contraseña: " + datosUsuario[1] + "\n");
					respuesta.append("</b></h3>\n");
					respuesta.append("</center></body></html>\n\n");
					System.out.println("Respuesta: "+respuesta);
				}else{
					respuesta.append("HTTP/1.0 400 Bad Request \n");
					String fecha= "Date: " + new Date()+" \n";
					respuesta.append(fecha);
					String tipo_mime = "Content-Type: text/html; charset=utf-8 \n\n";
					respuesta.append(tipo_mime);
					respuesta.append("<html><meta charset=\"utf-8\"></meta><body><h1>400 Petición Incorrecta</h1>La solicitud no tiene el formato esperado.</body></html>\n");
					System.out.println("Respuesta: "+respuesta);
				}
				dos.write(respuesta.toString().getBytes());
				
				dos.flush();
				dos.close();
				socket.close();
				
				
            }else if(line.toUpperCase().startsWith("DELETE")){
                String nombreArchivo = getNombreArchivoBorrar(line);

                File archivo = new File(nombreArchivo);
                boolean eliminado = archivo.delete();

                StringBuffer respuesta = new StringBuffer();

                if (eliminado) {
                    respuesta.append("HTTP/1.0 200 OK\r\n");
                    respuesta.append("Content-Type: text/html; charset=utf-8\r\n\r\n");
                    respuesta.append("<html><body><h1>Archivo eliminado correctamente</h1></body></html>\r\n");
                } else {
                    respuesta.append("HTTP/1.0 404 Not Found\r\n");
                    respuesta.append("Content-Type: text/html; charset=utf-8\r\n\r\n");
                    respuesta.append("<html><body><h1>Error al eliminar el archivo</h1></body></html>\r\n");
                }

                dos.write(respuesta.toString().getBytes());
                dos.flush();
                dos.close();
                socket.close();
            }else if(line.toUpperCase().startsWith("HEAD")){
                String nombreArchivo = getNombreArchivo(line);

                File ff = new File(nombreArchivo);
                long tam_archivo = ff.length();

                StringBuffer respuesta= new StringBuffer();

                if(tam_archivo > 0){
                    respuesta.append("HTTP/1.0 200 Okay \n");
                    String fecha= "Date: " + new Date()+" \n";
                    respuesta.append(fecha);
                    String tipo_mime = "Content-Type: text/html \n";
                    respuesta.append(tipo_mime);
                    String longitud = "Content-Length: " + tam_archivo + "\n\n";
                    respuesta.append(longitud);
                    respuesta.append("SERVIDOR HEAD");
                    System.out.println("Respuesta: "+respuesta);
		}else{
                    respuesta.append("HTTP/1.0 404 Not Found \n");
                    String fecha= "Date: " + new Date()+" \n";
                    respuesta.append(fecha);
                    String tipo_mime = "Content-Type: text/html \n\n";
                    respuesta.append(tipo_mime);
                    String longitud = "Content-Length: 0 \n\n";
                    respuesta.append(longitud);
                    respuesta.append("<html><meta charset=\"utf-8\"></meta><body><h1>404 No se encuentra</h1>No se encontró contexto para la solicitud.</body></html>\n");
                    System.out.println("Respuesta: "+respuesta);
		}
                dos.write(respuesta.toString().getBytes());

                dos.flush();
                dos.close();
                socket.close();
				
                }else if(line.toUpperCase().startsWith("PUT")){
                    String[] putTokens = line.split(" ");
                    String fileName = putTokens[1].substring(1); // Elimina el carácter "/" del nombre del archivo

                    Thread.sleep(10000);

                    Path filePath;
                    filePath = Path.of(fileName);
                    Files.copy(dis, filePath, StandardCopyOption.REPLACE_EXISTING);

                    StringBuffer respuesta = new StringBuffer();
                    respuesta.append("HTTP/1.0 200 Created \n");
                    String fecha = "Date: " + new Date() + " \n";
                    respuesta.append(fecha);
                    String tipo_mime = "Content-Type: text/html \n";
                    respuesta.append(tipo_mime);
                    String longitud = "Content-Length: 0 \n\n";
                    respuesta.append(longitud);
                    respuesta.append("SERVIDOR PUT");
                    System.out.println("Respuesta: " + respuesta);
                    dos.write(respuesta.toString().getBytes());

                    dos.flush();
                    dos.close();
                    socket.close();
				
		}else{
                    dos.write("HTTP/1.0 501 Not Implemented\r\n".getBytes());
                    dos.flush();
                    dos.close();
                    socket.close();
				//pw.println();
                }
            }catch(Exception e){
                e.printStackTrace();
            }			
    }//run
	
	public void getArch(String line)
	{
            int i;
            int f;
            if(line.toUpperCase().startsWith("GET"))
            {
                i=line.indexOf("/");
                f=line.indexOf(" ",i);
                FileName=line.substring(i+1,f);
            }
	}
	public void SendA(String fileName,Socket sc,DataOutputStream dos)
	{	
            int fSize = 0;
            byte[] buffer = new byte[4096];
            try{
		DataInputStream dis1 = new DataInputStream(new FileInputStream(fileName));	
		int x = 0;
                File ff = new File("fileName");
                long tam, cont=0;
                tam = ff.length();
                while(cont<tam)
                {
                    x = dis1.read(buffer);
                    dos.write(buffer,0,x);
                    cont =cont+x;
                    dos.flush();
                }
			//out.flush();
                dis.close();
                dos.close();
		}catch(FileNotFoundException e){
                    StringBuffer respuesta= new StringBuffer();
                    respuesta.append("HTTP/1.0 404 Not Found \n");
                    String fecha= "Date: " + new Date()+" \n";
                    respuesta.append(fecha);
                    String tipo_mime = "Content-Type: text/html \n\n";
                    respuesta.append(tipo_mime);
                    String longitud = "Content-Length: 0 \n\n";
                    respuesta.append(longitud);
                    respuesta.append("<html><meta charset=\"utf-8\"></meta><body><h1>404 No se encuentra</h1>No se encontró contexto para la solicitud.</body></html>\n");
                    System.out.println("Respuesta: "+respuesta);
                    try{
                            dos.write(respuesta.toString().getBytes());
                            dos.flush();
                            dos.close();
                            socket.close();
                    }catch(Exception e2){
                            e.printStackTrace();
                    }
		}catch(IOException e){
//			
		}
		
	}
	public void SendA(String arg, DataOutputStream dos1) 
	{
            try{    
            int b_leidos=0;
            DataInputStream dis2 = new DataInputStream(new FileInputStream(arg));
		// BufferedInputStream bis2=new BufferedInputStream(new FileInputStream(arg));
            byte[] buf=new byte[1024];
            int x=0;
            File ff = new File(arg);			
            long tam_archivo=ff.length(),cont=0;
	 /***********************************************/
            String sb = "";
            sb = sb+"HTTP/1.0 200 ok\n";
            sb = sb +"Server: Alvarez-Cortes Server/1.0 \n";
            sb = sb +"Date: " + new Date()+" \n";

            String ext = getExtension(arg);
            System.out.println("Archivo: " + arg + ", Extension: " + ext);

            if(ext.equals("css")){
                    sb = sb +"Content-Type: text/css \n"; 
            }else if(ext.equals("pdf")){
                    sb = sb +"Content-Type: application/pdf \n"; 
            }else if(ext.equals("webm")){
                    sb = sb +"Content-Type: video/webm \n";
            }else if(ext.equals("mp4")){
                    sb = sb +"Content-Type: video/mp4 \n";
            }else if(ext.equals("doc")){
                    sb = sb +"Content-Type: application/msrd \n";
            }else if(isImg(ext)){
                    sb = sb +"Content-Type: image/webp \n";
            }else{
                    sb = sb +"Content-Type: text/html \n"; 
            }
            
            sb = sb +"Content-Length: "+tam_archivo+" \n";
            sb = sb +"\n";
            dos1.write(sb.getBytes());
            dos1.flush();
	 /***********************************************/
	
            while(cont<tam_archivo)
            {
                    x = dis2.read(buf);
                    dos1.write(buf,0,x);
                    cont=cont+x;
                    dos1.flush();


            }
            //bos.flush();
            dis2.close();
            dos1.close();
			 
			 
            }catch(IOException e){
                System.out.println(e.getMessage());
                StringBuffer respuesta= new StringBuffer();			
                respuesta.append("HTTP/1.0 404 Not Found \n");
                String fecha= "Date: " + new Date()+" \n";
                respuesta.append(fecha);
                String tipo_mime = "Content-Type: text/html \n\n";
                respuesta.append(tipo_mime);
                respuesta.append("<html><meta charset=\"utf-8\"></meta><body><h1>404 No Encontrado</h1>No se encontró contexto para la solicitud.</body></html>\n");
                System.out.println("Respuesta: "+respuesta);
                try{
                        dos.write(respuesta.toString().getBytes());
                        dos.flush();
                        dos.close();
                        socket.close();
                }catch(Exception e2){
                        e.printStackTrace();
                }
            }
		
	}
		
	private boolean isImg(String ext){		
		if(ext.equals("jpg")){
			return true;
		}else if(ext.equals("jpeg")){
			return true;
		}else if(ext.equals("png")){
			return true;
		}else if(ext.equals("gif")){
			return true;
		}else if(ext.equals("webp")){
			return true;
		}
		return false;
	}
	
	private String getExtension(String fileName){
		String extension = "";
		int i = fileName.lastIndexOf('.');
		int p = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));

		if (i > p) {
			extension = fileName.substring(i+1);
		}
		return extension.toLowerCase();
	}
	
	private String[] getDatosUsuario(String data){
            String[] datosUsuario = new String[2];
		String temp;
		String[] tempArr;
		StringTokenizer st = new StringTokenizer(data,"\r\n");
		boolean userEncontrado = false;
		boolean passEncontrado = false;
		
		while (st.hasMoreTokens()) {
			temp = st.nextToken();
			if(temp.startsWith("User=")){
				tempArr = temp.split("=");
				if(tempArr.length > 1){
					datosUsuario[0] = tempArr[1];
				}else{
					datosUsuario[0] = " ";
				}
				userEncontrado = true;
			}else if(temp.startsWith("Pass=")){
				tempArr = temp.split("=");
				if(tempArr.length > 1){
					datosUsuario[1] = tempArr[1];
				}else{
					datosUsuario[1] = " ";
				}
				passEncontrado = true;
			}
		}
		
		if(userEncontrado && passEncontrado){
			System.out.println("Usuario: " + datosUsuario[0] + ", Contraseña: " + datosUsuario[1]);
			return datosUsuario;
		}else{
			return null;
		}
	}
	
        private String getNombreArchivoBorrar(String data){
            String nombreArchivo = "";
            String temp;
            StringTokenizer st = new StringTokenizer(data," ");
            boolean encontrado = false;

            while (st.hasMoreTokens()) {
                temp = st.nextToken();
                if (temp.startsWith("/")) {
                    nombreArchivo = temp.substring(1);
                    encontrado = true;
                    break;
                }
            }

            if (encontrado) {
                System.out.println("Nombre del archivo: " + nombreArchivo);
                return nombreArchivo;
            } else {
                return null;
            }
        }
        
	private String getNombreArchivo(String data){
		String nombreArchivo = "";
		String temp;
		String temp2;
		StringTokenizer st = new StringTokenizer(data,"?");
		boolean encontrado = false;
		
		while (st.hasMoreTokens()) {
                    temp = st.nextToken();
                    if(temp.startsWith("archivo=")){
                        temp2 = temp.substring(0, temp.indexOf("*"));
                        nombreArchivo = temp2.split("=")[1];
                        encontrado = true;
                        break;
                    }
		}
		
		if(encontrado){
                    System.out.println("Nombre del archivo: " + nombreArchivo );
                    return nombreArchivo;
		}else{
			return null;
		}
	}
}

public class ServidorWeb1 implements Runnable{
    protected int  puerto   = 8000;
    protected ServerSocket s = null;
    protected boolean      detenido    = false;
    protected Thread       runningThread= null;
    protected ExecutorService pool = Executors.newFixedThreadPool(100);
		
    public ServidorWeb1(int puerto){
            this.puerto = puerto;
    }

    @Override
    public void run(){
        synchronized(this){
            this.runningThread = Thread.currentThread();
        }
        iniciaServidor();
        while(! detenido()){
            Socket cl = null;
            try {
                cl = this.s.accept();
                System.out.println("Conexión aceptada...");
            } catch (IOException e) {
                if(detenido()) {
                    System.out.println("Servidor detenido.") ;
                    break;
                }throw new RuntimeException("Error al aceptar nueva conexión", e);
            }//catch
            this.pool.execute(new Manejador(cl));
        }//while
        this.pool.shutdown();
        System.out.println("Servidor detenido.") ;
    }


    private synchronized boolean detenido() {
        return this.detenido;
    }

    public synchronized void stop(){
        this.detenido = true;
        try {
            this.s.close();
        } catch (IOException e) {
            throw new RuntimeException("Error al cerrar el socket del servidor", e);
        }
    }
    
    

    
    private void iniciaServidor() {
        try {
            this.s = new ServerSocket(this.puerto);
            System.out.println("Servicio iniciado... esperando cliente...");
        } catch (IOException e) {
            throw new RuntimeException("No se puede iniciar el socket en el puerto: "+s.getLocalPort(), e);
        }
    }

	public static void main(String[] args) throws Exception{
		ServidorWeb1 server = new ServidorWeb1(8000);
		new Thread(server).start();
	}
	
}