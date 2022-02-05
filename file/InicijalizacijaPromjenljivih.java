class InicijalizacijaPromjenljivih{
	public static void main(String args[]){
		int vrijednost = 7;
		int duplaVrijednost = vrijednost * 2;
		double kolicnikSaDva = vrijednost / 2;
		char znakPodatka = 'S';
		//kolicnikSaDva = vrijednost;
		vrijednost = (int)kolicnikSaDva;
		System.out.println("Vrijednost je: " +vrijednost);
		System.out.println("Dupla vrijednost je: " +duplaVrijednost);
		System.out.println("Kolicnik sa 2 je: " +kolicnikSaDva);
		System.out.println("Prvo slovo mog imena je: " +znakPodatka);
	}
}