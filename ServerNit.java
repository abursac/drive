package server;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

public class ServerNit extends Thread
{
	final String rootFolder = "Drive";
    Socket soket;
    PrintStream tokKaKlijentuTekst;
    OutputStream tokKaKlijentuBajtovi;
    BufferedReader tokOdKlijentaTekst;
    InputStream tokOdKlijentaBajtovi;
    User ulogovani;
    String shareovani;
    
    public ServerNit(Socket s)
    {
	soket = s;
	try
	{
	    tokKaKlijentuTekst = new PrintStream(s.getOutputStream());
	    tokKaKlijentuBajtovi = s.getOutputStream();
	    tokOdKlijentaBajtovi = s.getInputStream();
	    tokOdKlijentaTekst = new BufferedReader(new InputStreamReader(s.getInputStream()));
	} catch (IOException e)
	{
	    e.printStackTrace();
	}
    }
    
    private boolean login(String username, String password) throws IOException
    {
		BufferedReader bR=new BufferedReader(new FileReader("baza_korisnika.txt"));
		while(true)
		{
		    String line = bR.readLine();
		    if(line == null)
		    {
		    	bR.close();
		    	break;
		    }
		    User u = new User(line);
		    if(u.username.equals(username) && u.password.equals(password))
		    {
		    	ulogovani = u;
		    	bR.close();
		    	return true;
		    }
		}
		bR.close();
		return false;
    }
    
    private boolean exists(String username) throws IOException
    {
    	
		BufferedReader bR=new BufferedReader(new FileReader("baza_korisnika.txt"));
		while(true)
		{
		    String line = bR.readLine();
		    if(line == null)
		    {
		    	bR.close();
		    	return false;
		    }
		    User u = new User(line);
		    if(u.username.equals(username))
		    {
		    	bR.close();
		    	return true;
		    }
		}

    }
    
    private boolean share_exists(String njegov, String moj) throws IOException
    {
    	BufferedReader bR=new BufferedReader(new FileReader("baza_deljenja.txt"));
    	while(true)
    	{
    		String line = bR.readLine();
    		if(line == null)
		    {
		    	bR.close();
		    	return false;
		    }
    		String[] podaci = line.split("#");
    		if(njegov.equals(podaci[0]) && (moj.equals(podaci[1])))
    		{
    			bR.close();
    			return true;
    		}
    	}
    }
    
    private boolean registration(String username, String password, boolean premium) throws IOException
    {
    	FileWriter fw = new FileWriter("baza_korisnika.txt", true);
        PrintWriter out = new PrintWriter(fw);
		if(username == "" || password == "" || username.contains("#") || password.contains("#") || exists(username))
		{
			out.close();
		    return false;
		}
		User u = new User(username, password, premium);
		out.println(u.toString());
		out.close();
		return true;
    }
    
    private boolean upload(String imeFajla) throws IOException
    {
    	File projekat = new File("");
    	String putanja = projekat.getAbsolutePath();
    	String folder = rootFolder+"\\"+ulogovani.username;
    	RandomAccessFile noviFajl = new RandomAccessFile(new File(putanja + "\\" + folder + "\\" + imeFajla), "rw");
    	byte[] bafer = new byte[1024];
    	while(true)
    	{
    		int n = tokOdKlijentaBajtovi.read(bafer, 0, 1024);
    		if(n < 1024)
    			break;
    		noviFajl.write(bafer, 0, n);
    	}
    	noviFajl.close();
    	return true;
    }
    
    private boolean download(String imeFajlaDown) throws IOException
    {
    	RandomAccessFile targetFajl;
    	if(shareovani == null)
    	{
    		targetFajl = new RandomAccessFile(new File(rootFolder+"\\"+ulogovani.username +"\\" + imeFajlaDown), "r");
    	}
    	else
    	{
    		targetFajl = new RandomAccessFile(new File(rootFolder+"\\"+shareovani +"\\" + imeFajlaDown), "r");
    	}
    	byte[] bafer = new byte[1024];
    	while(true)
    	{
    		int n = targetFajl.read(bafer, 0, 1024);
    		if(n == -1)
    		{
    			tokKaKlijentuBajtovi.write(0);
    			break;
    		}
    		if(n < 1024)
    		{
    			byte[] bafer1 = new byte[n];
    			for(int i = 0; i < n; i++)
					bafer1[i] = bafer[i];
    			tokKaKlijentuBajtovi.write(bafer1, 0, n);
    			break;
    		}
    		else
    		{
    			tokKaKlijentuBajtovi.write(bafer, 0, 1024);
    		}
    	}
    	targetFajl.close();
    	return true;
    }
    
    private boolean open(String imeFajlaOpen) throws IOException
    {
    	String user;
    	if(shareovani == null)
    		user = ulogovani.username;
    	else
    		user = shareovani;
    	File targetFajl = new File(rootFolder+"\\"+user +"\\" + imeFajlaOpen);
    	if(!Desktop.isDesktopSupported())
    	{
    		return false;
    	}
    	if(binarni(imeFajlaOpen))
    	{
    		if(getFileExtension(imeFajlaOpen).equals(".jpg"))
    		{
    			Desktop desktop = Desktop.getDesktop();
        		desktop.open(targetFajl);
    		}
    		Path path = Paths.get(rootFolder+"\\"+user +"\\" + imeFajlaOpen);
    		byte[] fileContents =  Files.readAllBytes(path);
    		String ispis = Base64.getEncoder().encodeToString(fileContents);
    		tokKaKlijentuTekst.println(ispis);
    	}
    	else
    	{
    		Desktop desktop = Desktop.getDesktop();
    		desktop.open(targetFajl);
    		tokKaKlijentuTekst.println("Uspesno otvoren fajl.");
    		
    	}
        return true;
    }
    
    public static boolean isBinaryFile(File f) throws FileNotFoundException, IOException {
        FileInputStream in = new FileInputStream(f);
        int size = in.available();
        if(size > 1024) size = 1024;
        byte[] data = new byte[size];
        in.read(data);
        in.close();

        int ascii = 0;
        int other = 0;

        for(int i = 0; i < data.length; i++) {
            byte b = data[i];
            if( b < 0x09 ) return true;

            if( b == 0x09 || b == 0x0A || b == 0x0C || b == 0x0D ) ascii++;
            else if( b >= 0x20  &&  b <= 0x7E ) ascii++;
            else other++;
        }

        if( other == 0 ) return false;

        return 100 * other / (ascii + other) > 95;
    }
    
    private void ls()
    {
    	File folder;
    	if(shareovani == null)
    	{
    		folder = new File(rootFolder+"\\"+ulogovani.username);
    	}
    	else
    	{
    		folder = new File(rootFolder+"\\"+shareovani);
    	}
    	File[] files = folder.listFiles();
    	int n = files.length;
    	tokKaKlijentuTekst.println(n);
    	for(File file : files)
    	{
    		tokKaKlijentuTekst.println(file.getName());
    	}
    }
    
    private boolean mkdir(String imeNovogFoldera)
    {
    	File targetFajl = new File(rootFolder+"\\"+ulogovani.username +"\\" + imeNovogFoldera);
    	targetFajl.mkdir();
    	return true;
    }
    
    private boolean prazanFolder(String imeFoldera)
    {
    	File folder = new File(rootFolder+"\\"+ulogovani.username +"\\" + imeFoldera);
    	if(folder.list().length == 0)
    		return true;
    	return false;
    }
    
    private boolean rmdir(String imeFolderaBrisanje)
    {
    	File folder = new File(rootFolder+"\\"+ulogovani.username +"\\" + imeFolderaBrisanje);
    	if(prazanFolder(imeFolderaBrisanje))
    	{
    		folder.delete();
    		return true;
    	}
    	return false;
    }
    
    private boolean rename(String staroIme, String novoIme)
    {
    	File folder = new File(rootFolder+"\\"+ulogovani.username +"\\" + staroIme);
    	File folder1 = new File(rootFolder+"\\"+ulogovani.username +"\\" + novoIme);
    	folder.renameTo(folder1);
    	return true;
    }
    
    private boolean move(String fajl, String stariFolder, String noviFolder)
    {
    	File file = new File(rootFolder+"\\"+ulogovani.username +"\\" + stariFolder+"\\"+fajl);
    	if(file.renameTo(new File(rootFolder+"\\"+ulogovani.username +"\\" + noviFolder+"\\"+fajl)))
    	{
    		file.delete();
    		return true;
    	}
    	return false;
    }
    
	private String getFileExtension(String name) 
	{
	    int lastIndexOf = name.lastIndexOf(".");
	    if (lastIndexOf == -1) 
	    {
	        return ""; // empty extension
	    }
	    return name.substring(lastIndexOf);
	}

    private boolean binarni(String name)
    {
    	if(getFileExtension(name).equals(".txt"))
    	{
    		return false;
    	}
    	return true;
    	
    }
    
    public void run()
    {
	tokKaKlijentuTekst.println("Dobro dosli, unesite komandu: ");
	try
	{
	    
	    while(true)
	    {
	    	String komanda = tokOdKlijentaTekst.readLine();
	    	switch(komanda)
	    	{
	    		case "login":
        	    	tokKaKlijentuTekst.println("Unesite korisnicko ime: ");
        			String username = tokOdKlijentaTekst.readLine();
        			tokKaKlijentuTekst.println("Unesite sifru: ");
        			String password = tokOdKlijentaTekst.readLine();
        			if(ulogovani != null)
        			{
        				tokKaKlijentuTekst.println("Vec ste ulogovani.");
        			}
        			else if(login(username, password))
        			{
        			    tokKaKlijentuTekst.println("Uspesno ste se ulogovali");
        			    File folder = new File(rootFolder+"\\"+ulogovani.username);
        		    	File[] files = folder.listFiles();
        		    	int n = files.length;
        		    	tokKaKlijentuTekst.println(n);
        		    	for(File file : files)
        		    	{
        		    		tokKaKlijentuTekst.println(file.getName());
        		    	}
        			}
        			else
        			{
        			    tokKaKlijentuTekst.println("Error");
        			}
        			break;
        			
        			
	    		case "register":
	    				boolean premium;
        	    		tokKaKlijentuTekst.println("Odaberite korisnicko ime: ");
        	    		String username1 = tokOdKlijentaTekst.readLine();
        	    		tokKaKlijentuTekst.println("Odaberite sifru: ");
        	    		String password1 = tokOdKlijentaTekst.readLine();
        	    		tokKaKlijentuTekst.println("Obicni/Premium? ");
        	    		String pr = tokOdKlijentaTekst.readLine();
        	    		if(pr.equals("Premium"))
        	    			premium = true;
        	    		else
        	    			premium = false;
        	    		if(ulogovani != null)
            			{
            				tokKaKlijentuTekst.println("Vec ste ulogovani.");
            			}
        	    		else if(registration(username1, password1, premium))
        	    		{
        	    		    tokKaKlijentuTekst.println("Uspesno ste se registrovali");
        	    		    File fajl=new File(rootFolder+"\\"+username1);
        	    			fajl.mkdir();
        	    		}
        	    		else
        	    		{
        	    		    tokKaKlijentuTekst.println("Error");
        	    		}
        	    		break;
        	    		
        	    		
	    		case "logout":
	    			if(ulogovani != null)
	    			{
	    				ulogovani = null;
	    				tokKaKlijentuTekst.println("Uspesan logout.");
	    			}
	    			else
	    			{
	    				tokKaKlijentuTekst.println("Niste ulogovani.");
	    			}
	    			break;
	    			
	    			
	    		case "upload":
	    			if(ulogovani == null)
	    			{
	    				tokKaKlijentuTekst.println("Niste ulogovani.");
	    				break;
	    			}
	    			if(ulogovani.premium == false)
	    			{
	    				File folder = new File(rootFolder+"\\"+ulogovani.username);
	    		    	File[] files = folder.listFiles();
	    		    	int n = files.length;
	    		    	if(n == 5)
	    		    	{
	    		    		tokKaKlijentuTekst.println("Uploadovali ste maksimalan broj fajlova.");
	    		    		break;
	    		    	}
	    			}
	    			tokKaKlijentuTekst.println("Upisite naziv fajla: ");
	    			String imeFajla = tokOdKlijentaTekst.readLine();
	    			if(upload(imeFajla))
	    			{
	    				tokKaKlijentuTekst.println("Fajl uspesno postavljen.");
	    			}
	    			else
	    			{
	    				tokKaKlijentuTekst.println("Error.");
	    			}
	    			
	    			break;
	    			
	    		case "download":
	    			if(ulogovani == null  && shareovani == null)
	    			{
	    				tokKaKlijentuTekst.println("Niste ulogovani.");
	    				break;
	    			}
	    			
	    			tokKaKlijentuTekst.println("Koji fajl preuzimate? ");
	    			String imeFajlaDown = tokOdKlijentaTekst.readLine();
	    			if(download(imeFajlaDown))
	    			{
	    				tokKaKlijentuTekst.println("Fajl uspesno preuzet.");
	    			}
	    			else
	    			{
	    				tokKaKlijentuTekst.println("Error.");
	    			}
	    			
	    			break;
	    			
	    		case "ls":
	    			if(ulogovani == null && shareovani == null)
	    			{
	    				tokKaKlijentuTekst.println("Niste ulogovani.");
	    				break;
	    			}
	    			ls();
	    			break;
	    			
	    			
	    		case "open":
	    			if(ulogovani == null  && shareovani == null)
	    			{
	    				tokKaKlijentuTekst.println("Niste ulogovani.");
	    				break;
	    			}
	    			tokKaKlijentuTekst.println("Koji fajl otvarate? ");
	    			String imeFajlaOpen = tokOdKlijentaTekst.readLine();
	    			if(!open(imeFajlaOpen))
	    			{
	    				tokKaKlijentuTekst.println("Error");
	    			}
	    			
	    			break;
	    			
	    			
	    		case "mkdir":
	    			if(ulogovani == null)
	    			{
	    				tokKaKlijentuTekst.println("Niste ulogovani.");
	    				break;
	    			}
	    			if(ulogovani.premium == false)
	    			{
	    				tokKaKlijentuTekst.println("Morate biti premium korisnik.");
	    				break;
	    			}
	    			tokKaKlijentuTekst.println("Upisite naziv foldera: ");
	    			String imeNovogFoldera = tokOdKlijentaTekst.readLine();
	    			if(mkdir(imeNovogFoldera))
	    			{
	    				tokKaKlijentuTekst.println("Folder uspesno napravljen.");
	    			}
	    			else
	    			{
	    				tokKaKlijentuTekst.println("Error.");
	    			}
	    			break;
	    			
	    		case "rmdir":
	    			if(ulogovani == null)
	    			{
	    				tokKaKlijentuTekst.println("Niste ulogovani.");
	    				break;
	    			}
	    			if(ulogovani.premium == false)
	    			{
	    				tokKaKlijentuTekst.println("Morate biti premium korisnik.");
	    				break;
	    			}
	    			tokKaKlijentuTekst.println("Upisite naziv foldera: ");
	    			String imeFolderaBrisanje = tokOdKlijentaTekst.readLine();
	    			if(rmdir(imeFolderaBrisanje))
	    			{
	    				tokKaKlijentuTekst.println("Folder uspesno obrisan.");
	    			}
	    			else
	    			{
	    				tokKaKlijentuTekst.println("Error.");
	    			}
	    			break;
	    			
	    		case "rename":
	    			if(ulogovani == null)
	    			{
	    				tokKaKlijentuTekst.println("Niste ulogovani.");
	    				break;
	    			}
	    			if(ulogovani.premium == false)
	    			{
	    				tokKaKlijentuTekst.println("Morate biti premium korisnik.");
	    				break;
	    			}
	    		tokKaKlijentuTekst.println("Kom fajlu menjate ime: ");
    			String staroIme = tokOdKlijentaTekst.readLine();
    			tokKaKlijentuTekst.println("Upisite novo ime: ");
    			String novoIme = tokOdKlijentaTekst.readLine();
    			if(rename(staroIme, novoIme))
    			{
    				tokKaKlijentuTekst.println("Uspesno promenjeno ime");
    			}
    			else
    			{
    				tokKaKlijentuTekst.println("Error.");
    			}
	    		break;
	    			
	    		case "move":
	    			if(ulogovani == null)
	    			{
	    				tokKaKlijentuTekst.println("Niste ulogovani.");
	    				break;
	    			}
	    			if(ulogovani.premium == false)
	    			{
	    				tokKaKlijentuTekst.println("Morate biti premium korisnik.");
	    				break;
	    			}
	    			tokKaKlijentuTekst.println("Koji fajl pomerate: ");
	    			String imeFajlaPomeranje = tokOdKlijentaTekst.readLine();
	    			tokKaKlijentuTekst.println("U kom folderu se nalazi: ");
	    			String staraDestinacija = tokOdKlijentaTekst.readLine();
	    			tokKaKlijentuTekst.println("U koji folder ga pomerate: ");
	    			String novaDestinacija = tokOdKlijentaTekst.readLine();
	    			if(move(imeFajlaPomeranje, staraDestinacija, novaDestinacija))
	    			{
	    				tokKaKlijentuTekst.println("Fajl uspesno pomeren.");
	    			}
	    			else
	    			{
	    				tokKaKlijentuTekst.println("Error.");
	    			}
	    		break;
	    		
	    		
	    		case "share":
	    			if(ulogovani == null)
	    			{
	    				tokKaKlijentuTekst.println("Niste ulogovani.");
	    				break;
	    			}
	    			tokKaKlijentuTekst.println("Unesite username korisnika kom delite pristup: ");
	    			String korisnik2 = tokOdKlijentaTekst.readLine();
	    			if(!exists(korisnik2))
	    			{
	    				tokKaKlijentuTekst.println("Ne postoji taj korisnik.");
	    			}
	    			FileWriter fw = new FileWriter("baza_deljenja.txt", true);
	    	        PrintWriter out = new PrintWriter(fw);
	    	        out.println(ulogovani.username + "#" + korisnik2);
	    	        out.close();
	    	        tokKaKlijentuTekst.println("Uspesno podeljen pristup.");
	    			break;
	    		
	    		case "access":
	    			if(ulogovani == null)
	    			{
	    				tokKaKlijentuTekst.println("Niste ulogovani.");
	    				break;
	    			}
	    			tokKaKlijentuTekst.println("Unesite username korisnika cijim fajlovima pristupate: ");
	    			String korisnik3 = tokOdKlijentaTekst.readLine();
	    			if(!exists(korisnik3))
	    			{
	    				tokKaKlijentuTekst.println("Taj korisnik ne postoji.");
	    			}
	    			if(share_exists(korisnik3, ulogovani.username))
	    			{
	    				shareovani = korisnik3;
	    				tokKaKlijentuTekst.println("Uspesno ste pristupili.");
	    			}
	    			else
	    			{
	    				tokKaKlijentuTekst.println("Zabranjen pristup.");
	    			}
	    			break;
	    			
	    		case "generisi link":
	    			if(ulogovani == null)
	    			{
	    				tokKaKlijentuTekst.println("Niste ulogovani.");
	    				break;
	    			}
	    			byte[] ime = ulogovani.username.getBytes();
	    			String link = Base64.getEncoder().encodeToString(ime);
	    			tokKaKlijentuTekst.println(link);
	    			break;
	    			
	    		case "access link":
	    			if(ulogovani != null)
	    			{
	    				tokKaKlijentuTekst.println("Niste neregistrovani korisnik.");
	    				break;
	    			}
	    			tokKaKlijentuTekst.println("Unesite link: ");
	    			String link2 = tokOdKlijentaTekst.readLine();
	    			byte[] kodiran = Base64.getDecoder().decode(link2);
	    			String s = new String(kodiran);
	    			if(exists(s))
	    			{
	    				shareovani = s;
	    				tokKaKlijentuTekst.println("Uspesan pristup.");
	    			}
	    			else
	    			{
	    				tokKaKlijentuTekst.println("Nevazeci link.");
	    			}
	    			break;
	    			
	    			
	    		default: 
	    			tokKaKlijentuTekst.println("Neispravan unos.");
	    	} 
	    }
	} 
	catch (Exception e)
	{
		System.out.println("Korisnik je napustio server.");
	    //e.printStackTrace();
	}
    }
}
