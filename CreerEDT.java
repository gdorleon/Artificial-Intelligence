package emploitemps;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;

import donnees.AssignationCours;
import structdonnees.EnsembleActivite;
import structdonnees.EnsembeDonnees;
import structdonnees.Matrice2D;
import structdonnees.Matrice3D;
import structdonnees.EnsembleCour;

public class CreerEDT {
	private int p, r, c;
	private int Lprof, lEtu, salL, coursL, acitvityLo;
	Matrice2D<String> ACTPlaced;
	private String Brpause, massdiv, history = "";

	Matrice3D<String> etudEmp;
	Matrice2D<Integer> THPlaced;
	Matrice3D<String> etud1Emp;
	Matrice3D<String> profEm;
	Matrice3D<String> salEm;

	Vector<Boolean> eActifOK;
	
	boolean AcOK = false;
	boolean crEDTOK = false;

	private ResourceBundle bundme = null;
	private EnsembeDonnees ds;
	Matrice3D<Boolean> sallEDT;

	Matrice3D<Integer> PRPlaced;
	Matrice3D<Boolean> profEDT;
	Matrice3D<Boolean> etuEDT;

	private EnsembleActivite allActiv;
	private ArrayList<AssignationCours> coursAss;

	
	

	
	

	public CreerEDT() {
	}
	/**
	 * @gervais
	 * @param ds
	 * permet la récupération des données provenant du fichier XML
	 */

	public void SetData(EnsembeDonnees ds) {
		this.ds = ds;
		this.coursAss = ds.getListeAssigneCours();
		
//Nombre d'enseignants
		Lprof = ds.getPROFTaille();
		//nombre d'élèves
		lEtu = ds.getLongueurTouteCLasses();
		
		//Nombre de salles
		salL = ds.getLongueurSalle();
		
		//nombre de classes
		coursL = ds.getCoursLongueur();
		acitvityLo = ds.getLongueurActivite();

		p = Lprof + lEtu + salL;
		r = ds.getCreneaux().getHeure().size();
		c = ds.getCreneaux().getJours().size();
		if (ds.getContrainte().getTeacher() == null)
			ds.getContrainte().setTeacher(new Matrice3D<Boolean>(Lprof, r, c));
		if (ds.getContrainte().getRoom() == null)
			ds.getContrainte().setRoom(new Matrice3D<Boolean>(salL, r, c));
		if (ds.getContrainte().getStudent() == null)
			ds.getContrainte().setStudent(new Matrice3D<Boolean>(lEtu, r, c));
		
	}

	public void initMatrices() {
		System.out.println("Init is Start");
		crEDTOK = false;

		eActifOK = new Vector<Boolean>();
		for (int i = 0; i < ds.getLongueurActivite(); i++) {
			eActifOK.add(false);
		}

		THPlaced = new Matrice2D<Integer>(coursL, c);
		PRPlaced = new Matrice3D<Integer>(coursL, r, c);
		ACTPlaced = new Matrice2D<String>(r, c);

		THPlaced.DonneeInit(0);
		PRPlaced.Init(0);
		ACTPlaced.DonneeInit("#");

		etuEDT = new Matrice3D<Boolean>(lEtu, r, c);
		profEDT = new Matrice3D<Boolean>(Lprof, r, c);
		sallEDT = new Matrice3D<Boolean>(salL, r, c);
		etuEDT.Init(true);
		profEDT.Init(true);
		sallEDT.Init(true);

		etudEmp = new Matrice3D<String>(lEtu, r, c);
		profEm = new Matrice3D<String>(Lprof, r, c);
		salEm = new Matrice3D<String>(salL, r, c);

		
		etudEmp.Init("[Empty]");
		profEm.Init("[Empty]");
		salEm.Init("[Empty]");

		Brpause = ds.getContrainte().getBreak();

		SetBusy(etuEDT, ds.getContrainte().getStudent());
		SetBusy(profEDT, ds.getContrainte().getTeacher());
		SetBusy(sallEDT, ds.getContrainte().getRoom());

		SetBreaks(etudEmp, etuEDT);
		SetBreaks(profEm, profEDT);
		SetBreaks(salEm, sallEDT);

		System.out.println("Init is over");
	}

	private void SetBusy(Matrice3D<Boolean> A, Matrice3D<Boolean> B) {
		if (A.getP() == B.getP() && A.getR() == B.getR()
				&& A.getC() == B.getC())
			for (int i = 0; i < A.getP(); i++)
				for (int j = 0; j < A.getR(); j++)
					for (int k = 0; k < A.getC(); k++) {
						boolean t = B.getData(i, j, k);
						A.setContent(i, j, k, t);
					}
	}

	private void SetBreaks(Matrice3D<String> A, Matrice3D<Boolean> B) {
		for (int T = 0; T < B.getP(); T++)
			for (int Days = 0; Days < B.getC(); Days++)
				for (int TimeSlot = 0; TimeSlot < B.getR(); TimeSlot++)
					if (ds.getCreneaux().getHeure().get(TimeSlot)
							.equalsIgnoreCase(Brpause)) {
						A.setContent(T, TimeSlot, Days, "[ Break ]");
						B.setContent(T, TimeSlot, Days, false);
					}
	}

	String temp, s;

	public void MainAlgo(int itration) {
		if (crEDTOK) {
			System.out.println("FIN" + itration);
		} else {
			initMatrices();
			GenerateurRandom ActR = new GenerateurRandom(ds.getLongueurActivite());
			history += "\nStart MainAlgo >>" + "Iteration " + itration;
			for (int Days = 0; Days < c; Days++) {
				for (int TimeSlot = 0; TimeSlot < r; TimeSlot++) {
					for (int activity = 0; activity < ds.getLongueurActivite(); activity++) {
						int randAct = ActR.getFileValue(activity);
						if (!eActifOK.get(randAct).booleanValue()) {
							if (placeActivity(randAct, TimeSlot, Days)) {
								eActifOK.set(randAct, true);
								EnsembleActivite aa = ds.getActivite(randAct);
								history += "\n " + randAct + "[" + aa.getProfesseur()
										+ "," + aa.getCour() + ","
										+ aa.getClasse() + "," + aa.getTag()
										+ "]" + "(" + TimeSlot + ":" + Days
										+ ")";
								
							}
						}
					}
				}
			}
			history += "\nEnd MainAlgo >>";

			for (int i = 0; i < ds.getLongueurActivite(); i++) {
				int randAct = ActR.getFileValue(i);
				if (!eActifOK.get(randAct).booleanValue()) {
					crEDTOK = false;
					history += "\nEchoue a >>" + randAct;
					break;
				} else {
					crEDTOK = true;
					createCombinedTT();
				}
			}

			if (crEDTOK)
				ACTPlaced.imprimer();
		}

	}

	private void createCombinedTT() {
		int StudLen = ds.getLongueurClasse();
		etud1Emp = new Matrice3D<String>(StudLen, r, c);
		etud1Emp.Init("[Empty]");
		massdiv = "&nbsp;&nbsp;&nbsp;:&nbsp;&nbsp;&nbsp; ";
		for (int i = 0; i < StudLen; i++) {
			String sname = ds.getClasse(i).getNom();
			for (int Days = 0; Days < c; Days++)
				for (int TimeSlot = 0; TimeSlot < r; TimeSlot++) {
					int j = searchStudent(sname);
					int lim = j + ds.getClasse(i).getListeGroupe().size();
					String stemp = "<table border=0>";
					for (; j < lim + 1; j++) {
						String sname1 = ds.getNomClasse().get(j);
						String entry = etudEmp.getData(j, TimeSlot,
								Days);
						if (entry.contains("Break")) {
							if (stemp.contains("Break"))
								break;
							stemp += "<tr><td align=center>" + entry
									+ "</td></tr>";
						} else {
							if (j == searchStudent(sname)) {
								if (entry.contains("Vide")) {
									stemp += "";
								} else {
									stemp += "";
									stemp += "" + sname1
											+ massdiv + entry + "";
									break;
								}
							} else {
								if (entry.contains("Vide")) {
									stemp += "";
								} else {
									stemp += "" + sname1
											+ massdiv + entry + "";
								}
							}
						}
					}
					stemp += "</table>~";
					etud1Emp.setContent(i, TimeSlot, Days, stemp);
				}
		}
	}

	private boolean placeActivity(int actnum, int TimeSlot, int Days) {
		int T, S, R = -1, subno, dur;
		String tag, tname, sname, roomname, subname;
		String stemp, stemp1;
		String ttemp, ttemp1;
		String rtemp, rtemp1;

		dur = ds.getActivite(actnum).getDuree();
		tag = ds.getActivite(actnum).getTag();
		subname = ds.getActivite(actnum).getCour();
		subno = ds.searchSubject(subname);

		tname = ds.getActivite(actnum).getProfesseur();
		T = ds.chercherProfesseur(tname);

		sname = ds.getActivite(actnum).getClasse();
		S = searchStudent(sname);

		roomname = "";
		for (int i = 0; i < ds.getLongueurSalle(); i++) {
			roomname = ds.getSalle(i).getNom();
			String type = ds.getSalle(i).getType();
			if ((tag.equals("PR") || (tag.equals("TUT")))
					&& type.equals("LABO"))

			{

				
				System.out.println("pratique");
				R = ds.chercherSalle(roomname);
				if (salleDispo(R, TimeSlot, Days))
					break;
			} else if (tag.equalsIgnoreCase("TH")
					&& type.equalsIgnoreCase("SALLE")) {
				R = ds.chercherSalle(roomname);
				System.out.println("Théorique");
				if (salleDispo(R, TimeSlot, Days))
					break;
			}
		}

		massdiv = "&nbsp;:&nbsp;";
		stemp = roomname + "$(" + tname + massdiv + subname + ")$" + tag;
		stemp1 = roomname + "$(" + tname + massdiv + subname + ")$" + tag;

		ttemp = roomname + "$(" + sname + massdiv + subname + ")$" + tag;
		ttemp1 = roomname + "$(" + sname + massdiv + subname + ")$" + tag;

		rtemp = sname + "$(" + tname + massdiv + subname + ")$" + tag;
		rtemp1 = sname + "$(" + tname + massdiv + subname + ")$" + tag;

		boolean TSR = profDispo(T, TimeSlot, Days)
				&& etuDispo(S, TimeSlot, Days)
				&& salleDispo(R, TimeSlot, Days);

		if (TSR) {
			if (tag.equals("TH")) {
				int tH = THPlaced.getData(subno, Days);
				if (tH < dur) {
					THPlaced.setData(subno, Days, tH + dur);
				} else {
					return false;
				}
				setStudUnAvail(S, TimeSlot, Days, actnum);
				etudEmp.setContent(S, TimeSlot, Days, stemp);
				profEDT.setContent(T, TimeSlot, Days, false);
				profEm.setContent(T, TimeSlot, Days, ttemp);
				sallEDT.setContent(R, TimeSlot, Days, false);
				salEm.setContent(R, TimeSlot, Days, rtemp);

				return true;
			} else {
				if (!isLast(TimeSlot)) {
					boolean TSR1 = profDispo(T, TimeSlot + 1, Days)
							&& etuDispo(S, TimeSlot + 1, Days)
							&& salleDispo(R, TimeSlot + 1, Days);

					if (!isBreak(TimeSlot) && TSR1) {
						int pR = PRPlaced.getData(subno, TimeSlot, Days);
						if (pR < dur) {
							PRPlaced.setContent(subno, TimeSlot, Days, pR + dur);
						} else {
							return false;
						}

						setStudUnAvail(S, TimeSlot, Days, actnum);
						etudEmp.setContent(S, TimeSlot, Days, stemp);
						etudEmp.setContent(S, TimeSlot + 1, Days, stemp1);

						profEDT.setContent(T, TimeSlot, Days, false);
						profEDT.setContent(T, TimeSlot + 1, Days, false);
						profEm.setContent(T, TimeSlot, Days, ttemp);
						profEm.setContent(T, TimeSlot + 1, Days, ttemp1);

						sallEDT.setContent(R, TimeSlot, Days, false);
						sallEDT.setContent(R, TimeSlot + 1, Days, false);
						salEm.setContent(R, TimeSlot, Days, rtemp);
						salEm.setContent(R, TimeSlot + 1, Days, rtemp1);

						return true;
					} else {
						return false;
					}
				} else {
					return false;
				}
			}
		} else
			return false;

	}

	private boolean isLast(int timeSlot) {
		if (r - 1 == timeSlot)
			return true;
		else
			return false;
	}

	private boolean isBreak(int TimeSlot) {
		if (ds.getCreneaux().getHeure().get(TimeSlot + 1).equalsIgnoreCase(Brpause))
			return true;
		else
			return false;
	}

	public int searchStudent(String sname) {
		int S = 0;
		for (int i = 0; i < ds.getLongueurTouteCLasses(); i++) {
			if (ds.getNomClasse().get(i).equals(sname)) {
				S = i;
				break;
			}
		}
		return S;
	}

	private void setStudUnAvail(int s, int timeSlot, int days, int actnum) {
		int size = 0;
		int gsize = 0;
		for (int i = 0; i < ds.getLongueurClasse(); i++) {
			gsize = ds.getClasse(i).getListeGroupe().size();
			size += gsize + 1;
			if (s < size) {
				if (ds.getActivite(actnum).getTag().equals("TH")) {
					for (; s < size; s++)
						etuEDT.setContent(s, timeSlot, days, false);
					break;
				} else {
					int parent = size - gsize - 1;
					etuEDT.setContent(parent, timeSlot, days, false);
					etuEDT.setContent(parent, timeSlot + 1, days, false);
					etuEDT.setContent(s, timeSlot, days, false);
					etuEDT.setContent(s, timeSlot + 1, days, false);

					setOtherStudUnAvail(parent, timeSlot, days);
					break;
				}
			}
		}
	}

	private void setOtherStudUnAvail(int s, int timeSlot, int days) {
		int size = 0;
		int gsize = 0;
		for (int i = 0; i < ds.getLongueurClasse(); i++) {
			if (size == s) {
				gsize = ds.getClasse(i).getListeGroupe().size();
				size += gsize + 1;
			} else {
				gsize = ds.getClasse(i).getListeGroupe().size();
				for (int z = 0; z < gsize; z++) {
					int OtherS = size + z + 1;
					etuEDT.setContent(OtherS, timeSlot, days, false);
					etuEDT.setContent(OtherS, timeSlot + 1, days, false);
				}
				size += gsize + 1;
			}
		}
	}

	private boolean ifPraOK(int s, int timeSlot, int days) {
		int size = 0;
		int gsize = 0;
		for (int i = 0; i < ds.getLongueurClasse(); i++) {
			gsize = ds.getClasse(i).getListeGroupe().size();
			if (size == s) {
				for (int z = s; z < gsize; z++) {
					int OtherS = size + z + 1;
					etuEDT.getData(OtherS, timeSlot, days);
					etuEDT.setContent(OtherS, timeSlot + 1, days, false);
				}
			}
			size += gsize + 1;
		}
		return false;
	}

	private boolean etuDispo(int i, int j, int k) {
		return etuEDT.getData(i, j, k).booleanValue();
	}

	private boolean profDispo(int i, int j, int k) {
		return profEDT.getData(i, j, k).booleanValue();
	}

	private boolean salleDispo(int i, int j, int k) {
		if (i >= 0) {
			return sallEDT.getData(i, j, k);
		} else
			return false;
	}

	public String getString(String key) {
		String value = "nada";
		if (bundme == null) {
			bundme = ResourceBundle.getBundle("resources.edt");
		}
		try {
			value = bundme.getString(key);
		} catch (MissingResourceException e) {
			System.out
					.println("error: "
							+ key);
		}
		return value;
	}

	public Matrice2D<String> getStudTimetable(int i) {
		Matrice2D<String> TimeTable;
		TimeTable = etudEmp.get2DPart(i);
		return TimeTable;
	}

	public Matrice2D<String> getStud1Timetable(int i) {
		Matrice2D<String> TimeTable;
		TimeTable = etud1Emp.get2DPart(i);
		return TimeTable;
	}

	public Matrice2D<String> getTchTimetable(int i) {
		Matrice2D<String> TimeTable;
		TimeTable = profEm.get2DPart(i);
		return TimeTable;
	}

	public Matrice2D<String> getRoomTimetable(int i) {
		Matrice2D<String> TimeTable;
		TimeTable = salEm.get2DPart(i);
		return TimeTable;
	}

	public boolean isAllActivitiesPlace() {
		return crEDTOK;
	}

	public boolean isActivitiesGenerated() {
		return AcOK;
	}

	public void GenerateActivities() {
		try {
			int i = 0, TH = 0, PR = 0;
			int thour = Integer.parseInt(getString("DataInput.Dureetheorie"));
			int phour = Integer.parseInt(getString("DataInput.Dureepratique"));
			if (ds.getListeActivite().size() > 0)
				ds.getListeActivite().clear();

			for (int j = 0; j < coursAss.size(); j++) {
				EnsembleCour temp = coursAss.get(j).getSubj();
				for (int ta = 0; ta < temp.getnbrePrati(); ta++, i++) {
					allActiv = new EnsembleActivite(i);
					allActiv.setTag("TH");
					allActiv.setClasse(coursAss.get(j).getStudTH());
					allActiv.setCour(temp.getNom());
					allActiv.setProfesseur(coursAss.get(j).getTchTH());
					allActiv.setDuree(thour);
					TH++;
					ds.ajouterActivite(i, allActiv);
				}

				int n = 0;
				int size = coursAss.get(j).getTchPR().size();

				for (int m = 0; m < coursAss.get(j).getStudPR().size(); m++, i++) {
					if (size == 1)
						n = 0;
					else
						n = m / size;
					allActiv = new EnsembleActivite(i);
					if (temp.getNBreTheo() > 1)
						allActiv.setTag("PR");
					else
						allActiv.setTag("TUT");
					allActiv.setClasse(coursAss.get(j).getStudPR().get(m));
					allActiv.setProfesseur(coursAss.get(j).getTchPR().get(n));
					allActiv.setCour(temp.getNom());
					allActiv.setDuree(phour);
					PR++;
					ds.ajouterActivite(i, allActiv);
				}
			}
			ds.setTheoPrat(TH, PR);

			if ((ds.getListeActivite().size() == (ds.getPRat() + ds.getTheoPrat()))
					&& (ds.getListeActivite().size() != 0))
				AcOK = true;
			else
				AcOK = false;
		} catch (Exception e) {
			JOptionPane
					.showMessageDialog(
							null,
							"Veuillez spécifier toutes les relations entre cours théoriques,pratiques, enseignant et département");

		}
	}

	public void MainAlgo(JFrame f) {
		initMatrices();
		EDTbar tpb = new EDTbar(
				ds.getListeActivite().size() / 2, f);
		File fo = new File("log.txt");
		try {
			OutputStream fos = new FileOutputStream(fo);
			fos.write(history.getBytes());
			fos.close();
			history = "";
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	class EDTbar {
		final static int interval = 200;
		int i, ewr;
		JLabel dtlabel;
		JProgressBar pb;
		Timer tmp;
		JButton lebre;
		JDialog dlg;
		String tname, str1;

		public EDTbar(int work1, JFrame baseFrame) {
			this.ewr = work1;
			lebre = new JButton("Start");
			lebre.addActionListener(new ButtonListener());
			if (!isActivitiesGenerated()) {
				tname = "Aucune activité crée";
				lebre.setEnabled(false);
			} else
				tname = "Génération de l'emploi de temps !";

			pb = new JProgressBar(0, ewr);
			pb.setValue(0);
			pb.setStringPainted(true);

			dtlabel = new JLabel("Génération de l'emploi de temps en cours :");

			
			tmp = new Timer(interval, new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					if (i == ewr || isAllActivitiesPlace()) {
						Toolkit.getDefaultToolkit().beep();
						tmp.stop();
						lebre.setEnabled(true);
						pb.setValue(0);
						if (isAllActivitiesPlace()) {
							str1 = "<html><font color=\"#00FF00\"><b>Génération de l'emploi de temps terminée.</b>"
									+ "</font>" + "</html>";
						} else {
							str1 = "<html><font color=\"#FF0000\"><b>Génération de l'emploi de temps non terminée</b></font></html>";
						}
						dtlabel.setText(str1);
						if (isAllActivitiesPlace()) {
							pb.setValue(ewr);
							JOptionPane.showMessageDialog(null,
									"Emploi de temps généré avec succès");
							dlg.setVisible(false);
						} else {
							pb.setValue(0);
							JOptionPane.showMessageDialog(null,
									"Emploi de temps non généré !");
							dlg.setVisible(false);
						}
					}
					MainAlgo(i);
					i = i + 1;
					pb.setValue(i);
				}
			});

			JPanel panel = new JPanel();
			panel.add(lebre);
			panel.add(pb);

			JPanel panel1 = new JPanel();
			panel1.setLayout(new BorderLayout());
			panel1.add(panel, BorderLayout.NORTH);
			panel1.add(dtlabel, BorderLayout.CENTER);
			panel1.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

			dlg = new JDialog(baseFrame, tname, true);
			dlg.setContentPane(panel1);
			dlg.pack();
			dlg.setVisible(true);
			dlg.setBounds(200, 200, dlg.getWidth(), dlg.getHeight());
			dlg.setResizable(false);
		}

		class ButtonListener implements ActionListener {
			public void actionPerformed(ActionEvent ae) {
				lebre.setEnabled(false);
				i = 0;
				String str = "<html>" + "<font color=\"#008000\">" + "<b>"
						+ "Génération de l'emploi du temps en cours ........."
						+ "</b>" + "</font>" + "</html>";
				dtlabel.setText(str);
				tmp.start();
			}
		}
	}

}
