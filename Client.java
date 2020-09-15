package client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client
{
    static String adresa = "localhost";
    static int port = 2222;
    
    
    public static void main(String[] args)
    {
    	PrintStream tokKaServeruTekst = null;
		BufferedReader tokOdServeraTekst = null;
		BufferedReader br = null;
		OutputStream tokKaServeruBajtovi = null;
		InputStream tokOdServeraBajtovi = null;
		try 
		{
			Socket s = new Socket(adresa, port);
			tokKaServeruTekst = new PrintStream(s.getOutputStream());
			tokOdServeraBajtovi = s.getInputStream();
			tokOdServeraTekst = new BufferedReader(new InputStreamReader(s.getInputStream()));
			tokKaServeruBajtovi = s.getOutputStream();
			br = new BufferedReader(new InputStreamReader(System.in));
			String odgovor3 = "";
    	
    	while(true)
    	{
    			if(!odgovor3.equals("Error"))
    			{
    				String odgovor = tokOdServeraTekst.readLine();
    				System.out.println(odgovor);
    			}
				String komanda = br.readLine();
				tokKaServeruTekst.println(komanda);
				
				if(komanda.equals("login"))
				{
					String odgovor1 = tokOdServeraTekst.readLine();
					System.out.println(odgovor1);
					String username = br.readLine();
					tokKaServeruTekst.println(username);
					String odgovor2 = tokOdServeraTekst.readLine();
					System.out.println(odgovor2);
					String password = br.readLine();
					tokKaServeruTekst.println(password);
					odgovor3 = tokOdServeraTekst.readLine();
					System.out.println(odgovor3);
					if(odgovor3.equals("Uspesno ste se ulogovali"))
					{
						String odgovor4 = tokOdServeraTekst.readLine();
						int n = Integer.parseInt(odgovor4);
						for(int i = 0; i < n-1; i++)
						{
							String ime = tokOdServeraTekst.readLine();
							System.out.println(ime);
						}
					}
					if(odgovor3.equals("Error"))
					{
						
						continue;
					}
				}
				
				if(komanda.equals("upload"))
				{
					String odg1 = tokOdServeraTekst.readLine();
					System.out.println(odg1);
					if(odg1 == "Niste ulogovani.")
					{
						continue;
					}
					String imeFajla = br.readLine();
					tokKaServeruTekst.println(imeFajla);
					RandomAccessFile fajl = new RandomAccessFile(new File(imeFajla), "rw");
					byte[] bafer = new byte[1024];
					while(true)
					{
						int n = fajl.read(bafer, 0, 1024);
						if(n == -1)
							break;
						if(n < 1024)
						{
							byte[] bafer1 = new byte[n];
							
							for(int i = 0; i < n; i++)
								bafer1[i] = bafer[i];
							
							tokKaServeruBajtovi.write(bafer1, 0, n);
						}
						else
						{
							tokKaServeruBajtovi.write(bafer, 0, 1024);
						}
					}
					fajl.close();
				}
				
				if(komanda.equals("download"))
				{
					String odg2 = tokOdServeraTekst.readLine();
					System.out.println(odg2);
					if(odg2 == "Niste ulogovani.")
					{
						continue;
					}
					String imeFajlaDown = br.readLine();
					tokKaServeruTekst.println(imeFajlaDown);
					RandomAccessFile fajlDown = new RandomAccessFile(new File("Downloads\\" + imeFajlaDown), "rw");
					byte[] bafer = new byte[1024];
					while(true)
					{
						int n = tokOdServeraBajtovi.read(bafer, 0, 1024);
						if(n == 1 && bafer[0] == 0)
							break;
			    		if(n < 1024)
			    		{
			    			fajlDown.write(bafer, 0, n);
			    			break;
			    		}
			    		else
			    			fajlDown.write(bafer, 0, 1024);
					}
					fajlDown.close();
				}
				
				if(komanda.equals("ls"))
				{
					String odg3 = tokOdServeraTekst.readLine();
					int n = Integer.parseInt(odg3);
					for(int i = 0; i < n-1; i++)
					{
						String ime = tokOdServeraTekst.readLine();
						System.out.println(ime);
					}
				}
				
				if(komanda.equals("open"))
				{
					String odg4 = tokOdServeraTekst.readLine();
					System.out.println(odg4);
					if(odg4 == "Niste ulogovani.")
					{
						continue;
					}
					String imeFajlaOpen = br.readLine();
					tokKaServeruTekst.println(imeFajlaOpen);
				}
				
				if(komanda.equals("mkdir"))
				{
					String odg5 = tokOdServeraTekst.readLine();
					System.out.println(odg5);
					if(odg5 == "Niste ulogovani.")
					{
						continue;
					}
					String imeNovogFoldera = br.readLine();
					tokKaServeruTekst.println(imeNovogFoldera);
				}
				
				if(komanda.equals("rmdir"))
				{
					String odg6 = tokOdServeraTekst.readLine();
					System.out.println(odg6);
					if(odg6 == "Niste ulogovani.")
					{
						continue;
					}
					String imeFolderaBrisanje = br.readLine();
					tokKaServeruTekst.println(imeFolderaBrisanje);
				}
				
				if(komanda.equals("rename"))
				{
					String odg7 = tokOdServeraTekst.readLine();
					System.out.println(odg7);
					if(odg7 == "Niste ulogovani.")
					{
						continue;
					}
					String staroIme = br.readLine();
					tokKaServeruTekst.println(staroIme);
					String odg8 = tokOdServeraTekst.readLine();
					System.out.println(odg8);
					String novoIme = br.readLine();
					tokKaServeruTekst.println(novoIme);
				}
				if(komanda.equals("move"))
				{
					String odg9 = tokOdServeraTekst.readLine();
					System.out.println(odg9);
					if(odg9 == "Niste ulogovani.")
					{
						continue;
					}
					String imeFajlaPomeranje = br.readLine();
					tokKaServeruTekst.println(imeFajlaPomeranje);
					String odg10 = tokOdServeraTekst.readLine();
					System.out.println(odg10);
					String staraDestinacija = br.readLine();
					tokKaServeruTekst.println(staraDestinacija);
					String odg11 = tokOdServeraTekst.readLine();
					System.out.println(odg11);
					String novaDestinacija = br.readLine();
					tokKaServeruTekst.println(novaDestinacija);
				}
				
				if(komanda.equals("share"))
				{
					String odg12 = tokOdServeraTekst.readLine();
					System.out.println(odg12);
					String korisnik2 = br.readLine();
					tokKaServeruTekst.println(korisnik2);
					
				}
				
				if(komanda.equals("access"))
				{
					String odg12 = tokOdServeraTekst.readLine();
					System.out.println(odg12);
					String korisnik3 = br.readLine();
					tokKaServeruTekst.println(korisnik3);
				}
				
				if(komanda.equals("access link"))
				{
					String odg13 = tokOdServeraTekst.readLine();
					System.out.println(odg13);
					String link = br.readLine();
					tokKaServeruTekst.println(link);
				}
				
				if(komanda.equals("exit"))
				{
					break;
				}
			} 
    		s.close();

		}
		catch (UnknownHostException e1) 
		{
			e1.printStackTrace();
		} 
		catch (IOException e1) 
		{
			e1.printStackTrace();
		}
    }

}
