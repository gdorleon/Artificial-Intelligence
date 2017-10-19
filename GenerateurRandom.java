package emploitemps;

import java.util.ArrayList;

public class GenerateurRandom {

	private ArrayList<Integer> pfich;
	private int mdme;
	private int adjou;
	private int prodX;

	
	
	// Generateur ici @ginelDorleon
	
	public GenerateurRandom (int n){
		pfich=new ArrayList<Integer>();
		genHasard(n);		
        for (int i=0; i<n; i++) {
        	pfich.add(i, new Integer(map(i)));
        }
	}
	public int getFileValue(int i) {
		return pfich.get(i).intValue();
	}
	
	
	
	
	
	
	private void genHasard(int n) {
		if (n < 0) {
			throw new IllegalArgumentException();
		}
		mdme = n;
		adjou = (int) (Math.random()*n);
		if (n == 1) {
			return;
		}
		
		


		prodX = (int) Math.sqrt(n);
		while (gcd(prodX, n) != 1) {
			if (++prodX == n) {
				prodX = 1;
			}
		}
	}
	
	

	private int map(int i) {
        return (int) ((prodX * i + adjou) % mdme);
	}

	private static int gcd(int a, int b) {
		while (b != 0) {
			
			
			int tmp = a % b;
			a = b;
			b = tmp;
		}
		return a;
	}
}
