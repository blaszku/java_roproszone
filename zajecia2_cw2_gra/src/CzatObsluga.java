/* Andrzej Piszczek (c) 2015 
 * andpis58@gmail.com
 * */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.BreakIterator;
import java.util.Arrays;
import java.util.Random;
import java.util.Vector;

public class CzatObsluga extends Thread {
	static Vector<CzatObsluga> czaty = new Vector<CzatObsluga>();
	private Socket socket;
	private BufferedReader wejscie;
	private PrintWriter wyjscie;
	private String nick;
	private Integer punkty = 0;
	static Integer cyfra;

	public CzatObsluga(Socket socket) {
		this.socket = socket;
	}

	private void wyslijDoWszystkich(String tekst) {
		for (CzatObsluga czat : czaty) {
			synchronized (czaty) {
				czat.wyjscie.println(tekst);
			}
		}
	}

	private void info() {
		wyjscie.println("Aktualny stan punktów: ");
		String listapunktow ="";
		for (CzatObsluga czat : czaty) {
			listapunktow += czat.nick+": "+czat.punkty+"\n";
		}
		for (CzatObsluga czat : czaty) {
			synchronized (czaty) {
				czat.wyjscie.println(listapunktow);
			}
		}
		wyjscie.println();
	}

	public void run() {
		String linia;
		synchronized (czaty) {
			czaty.add(this);
		}
		try {
			wejscie = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			wyjscie = new PrintWriter(new OutputStreamWriter(
					socket.getOutputStream()), true);
			wyjscie.println("Połączony z serwerem. Komenda /end kończy połączenie. Podaj nick:");
			nick = wejscie.readLine();
			
			wyjscie.println("Podaj cyfrę z przedzialu 0-100:");

			//info();
			while (!(linia = wejscie.readLine()).equalsIgnoreCase("/end")) {
				int userNumber = Integer.parseInt(linia);
				if (userNumber < CzatObsluga.cyfra){
					this.wyjscie.println("Za mała!");
				} else if (userNumber > CzatObsluga.cyfra) {
					this.wyjscie.println("Za duża!");
				} else {
					wyslijDoWszystkich("Bingo!! Użytkownik "+nick+" odgadNĄł cyfrę: "+CzatObsluga.cyfra);
					this.punkty += 10;
					info();
					Random generator = new Random(); 
					CzatObsluga.cyfra = generator.nextInt(100);
					wyslijDoWszystkich("Nowa liczba wygenerowana. Podaj cyfrę z zakresu 0-100");
				}

				
			}
			wyslijDoWszystkich("Opuścił grę "+ nick);

		} catch (IOException e) {
			System.out.println(e);
		} finally {
			try {
				wejscie.close();
				wyjscie.close();
				socket.close();
			} catch (IOException e) {
			} finally {
				synchronized (czaty) {
					czaty.removeElement(this);
				}
			}
		}
	}
}
