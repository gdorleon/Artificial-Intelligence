package emploitemps;

import structdonnees.Matrice3D;

public class Contraintes {
	private String pause;
	private String nomUniversite;
	private String nomDepartement;
	private Matrice3D<Boolean> contrainteSalle;
	private Matrice3D<Boolean> profContrainte;
	private Matrice3D<Boolean> classeContrainte;

	public Contraintes(){
		setPause("");
		setUniversite("IFI");
		setDepartement("IFI");
	}

	public void setUniversite(String _collageName) {
		this.nomUniversite = _collageName;
	}

	public String getCollageName() {
		return nomUniversite;
	}

	
	// departement comme :
	
	public void setDepartement(String _deptName) {
		this.nomDepartement = _deptName;
	}

	public String getDeptName() {
		return nomDepartement;
	}

	public void setPause(String _break) {
		pause = _break;
	}

	public String getBreak() {
		return pause;
	}

	public void setRoom(Matrice3D<Boolean> room) {

		this.contrainteSalle = room;
		contrainteSalle.Init(true);
	}

	public Matrice3D<Boolean> getRoom() {
		return contrainteSalle;
	}

	public void setTeacher(Matrice3D<Boolean> tch) {
		
		this.profContrainte = tch;
		profContrainte.Init(true);
	}

	public Matrice3D<Boolean> getTeacher() {
		return profContrainte;
	}
	
	public void setStudent(Matrice3D<Boolean> stud) {

		this.classeContrainte = stud;
		classeContrainte.Init(true);
	}

	public Matrice3D<Boolean> getStudent() {
		return classeContrainte;
	}
}
