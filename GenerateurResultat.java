package emploitemps;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import structdonnees.EnsembeDonnees;
import structdonnees.Matrice2D;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Cell;
import com.lowagie.text.Chapter;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Section;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.PdfWriter;

public class GenerateurResultat {

	String linkp,cedtname,dedtname;
	String[] listeJour = { "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi" };
	String[] listeHeure = { "Heure 1", "Heure 2", "Heure 3", "Heure 4", "Heure 5",
			"Heure 6", "Heure 7", "Heure 8" };
	int ii;

	Vector<String> jour;
	Vector<String> heure;
	Vector<String> professeur;
	Vector<String> salle;
	Vector<String> classe;

	EnsembeDonnees ds;
	CreerEDT edtgen;
	Matrice2D<String> emploiTemps, vide;

	public GenerateurResultat(String path) {
		this.linkp = path;
		vide = new Matrice2D<String>(listeHeure.length, listeJour.length);
		vide.DonneeInit("\n---xx---\n");
	}

	public String getJTEntree(String entry) {
		if (entry.contains("TUT")) {
			entry = entry.replace("TUT", "").replace("$", "<br>");
			entry = "<body text=#123ABC><b>" + entry + "</b></body>";
		} else if (entry.contains("PR")) {
			entry = entry.replace("PR", "").replace("$", "<br>");
			entry = "<body text=#123456><b>" + entry + "</b></body>";
		} else if (entry.contains("TH")) {
			entry = entry.replace("TH", "").replace("$", "<br>");
			entry = "<body text=blue><b>" + entry + "</b></body>";
		} else if (entry.contains("Break")) {
			entry = entry.replace("[", "<br>").replace("]", "<br>");
			entry = "<body text=red><b>" + entry + "</b></body>";
		} else {
			entry = entry.replace("[", "<br>").replace("]", "<br>");
			entry = "<body text=blue><b>" + "...." + "</b></body>";
		}
		return entry;
	}

	public String getHTMLInput(String entry) {
		if (entry.contains("TUT")) {
			entry = entry.replace("TUT", "").replace("$", "");
			entry = "<body text=#123ABC><b>" + entry + "</b></body>";
		} else if (entry.contains("PR")) {
			entry = entry.replace("PR", "").replace("$", "<br>");
			entry = "<" + entry + "";
		} else if (entry.contains("TH")) {
			entry = entry.replace("TH", "").replace("$", "<br>");
			entry = "<font color=blue><b>" + entry + "</b></font>";
		} else if (entry.contains("Break")) {
			entry = entry.replace("[", "<br>").replace("]", "<br>");
			entry = "<font color=red><b>" + entry + "";
		} else {
			entry = entry.replace("[", "<br>").replace("]", "");
			entry = "<" + "xxx" + "";
		}
		return entry;
	}

	public String getEntreeFull(String entry) {
		if (entry.contains("TUT")) {
			entry = entry.replace("TUT", "").replace("$", "");
		} else if (entry.contains("PR")) {
			entry = entry.replace("PR", "").replace("$", "");
		} else if (entry.contains("TH")) {
			entry = entry.replace("TH", "").replace("$", "");
		} else if (entry.contains("Break")) {
			entry = entry.replace("[", "").replace("]", "");
		} else {
			entry = entry.replace("[", "").replace("]", "");
		}
		return entry;
	}

	public JTable getEmploiTemps(int i, int whichTable) {
		ii = i;

		if (i == -1) {
			emploiTemps = vide;
		} else {
			switch (whichTable) {
			case 0:
				emploiTemps = edtgen.getStud1Timetable(i);
				break;
			case EnsembeDonnees.STUDENT:
				emploiTemps = edtgen.getStudTimetable(i);
				break;
			case EnsembeDonnees.TEACHER:
				emploiTemps = edtgen.getTchTimetable(i);
				break;
			case EnsembeDonnees.ROOM:
				emploiTemps = edtgen.getRoomTimetable(i);
				break;
			default:
				break;
			}
		}

		final JTable tableView;

		TableModel dataModel = new AbstractTableModel() {
			public int getColumnCount() {
				return emploiTemps.getp() + 1;
			}

			public int getRowCount() {
				return emploiTemps.getn();
			}

			public Object getValueAt(int row, int col) {
				if (col == 0) {
					if (ii < 0)
						return listeHeure[row];
					else
						return ds.getCreneaux().getHeure().get(row);
				} else {
					String entry = emploiTemps.getData(row, col - 1);
					if (entry.contains("~")) {
						entry = entry.replace("~", "");
						entry = getJTEntree(entry);
					} else {
						entry = getJTEntree(entry);
					}
					entry = "<html>" + entry + "</html>";
					return entry;
				}
			}

			public String getColumnName(int column) {
				if (column == 0)
					return "Temps\\Jour";
				else {
					if (ii < 0)
						return listeJour[column - 1];
					else
						return ds.getCreneaux().getJours().get(column - 1);
				}
			}

			@SuppressWarnings("unchecked")
			public Class getColumnClass(int c) {
				return getValueAt(0, c).getClass();
			}

			public boolean isCellEditable(int row, int col) {
				return col == -1;
			}

			public void setValueAt(Object aValue, int row, int column) {
				// TimeTableO.setContent(row,column,aValue.toString());
			}
		};

		tableView = new JTable(dataModel);
		tableView.setRowSelectionAllowed(false);
		tableView.setColumnSelectionAllowed(false);
		tableView.getTableHeader().setReorderingAllowed(false);
		tableView.getTableHeader().setBackground(Color.LIGHT_GRAY);
		tableView.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		tableView.setRowHeight(75);
		return tableView;
	}
	public Table HorizontalTable(Matrice2D<String> tt) {
		Table t1;
		Table t2;
		Table tm;

		try {
			t1 = new Table(jour.size() + 1, 1);
			t2 = new Table(jour.size() + 1, heure.size());
			t1.setAlignment(Table.ALIGN_CENTER);
			t2.setAlignment(Table.ALIGN_CENTER);

			for (int j = 0; j < jour.size() + 1; j++) {
				t1.setCellsFitPage(true);
				if (j == 0)
					t1.addCell(new Cell("Temps/Jours"));
				else
					t1.addCell(new Cell(jour.get(j - 1)));
			}

			for (int j = 0; j < heure.size(); j++) {
				for (int i = 0; i < jour.size() + 1; i++) {
					t2.setCellsFitPage(true);
					if (i == 0)
						t2.addCell(heure.get(j));
					else {
						String entry = tt.getData(j, i - 1);
						if (entry.contains("~")) {
							entry = entry.replace("~", "");
							entry = getJTEntree(entry);
						} else {
							entry = getJTEntree(entry);
						}
						t2.addCell(entry);
					}
				}
			}

			tm = new Table(1, 3);
			tm.setAlignment(Table.ALIGN_CENTER);
			tm.setBorderWidth(1);
			tm.setPadding(5);
			tm.setSpacing(5);
			tm.addCell(new Cell(cedtname+"\n "+dedtname));
			tm.insertTable(t1);
			tm.insertTable(t2);
			return tm;
		} catch (BadElementException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String OnlyFormat() {
		String str = "";
		if (edtgen.isAllActivitiesPlace()) {
			if (ds.getPROFTaille() == 0)
				MSGBOX("Emploi du temps enseignant", "Aucun emploi du temps enseignant généré");
			else
				str += "Teacher's ,";
			if (ds.getLongueurClasse() == 0)
				MSGBOX("Emploi de temps élève", "Aucun emploi du temps pour élève généré");
			else
				str += " Student's ,";
			if (ds.getLongueurSalle() == 0)
				MSGBOX("Repartition des salles", "Aucune repartition de salle générée");
			else
				str += " Room's ";
		} else
			JOptionPane.showMessageDialog(null, "Commencez par générer un emploi de temps !");

		return str;
	}
	public void MSGBOX(String title, String msg) {
		JOptionPane.showMessageDialog(null, msg, title,
				JOptionPane.INFORMATION_MESSAGE);
	}

	public void SetData(EnsembeDonnees ds, CreerEDT ttgsGen) {
		this.ds = ds;
		this.edtgen = ttgsGen;

		jour = ds.getCreneaux().getJours();
		heure = ds.getCreneaux().getHeure();
		professeur = ds.getNomProf();
		classe = ds.getNomClasse();
		salle = ds.getNomSalle();
		cedtname=ds.getContrainte().getCollageName();
		dedtname=ds.getContrainte().getDeptName();
	}

	class EDTChargement {
		final static int interval = 200;
		int i, format;
		JLabel label;
		JProgressBar pb;
		Timer timer;
		JButton button;
		JDialog dlg;
		String str;

		public EDTChargement(String str1, String title, int format1,
				JFrame baseFrame) {
			this.format = format1;
			this.str = str1;
			pb = new JProgressBar(0, 20);
			pb.setValue(0);
			pb.setStringPainted(true);

			label = new JLabel("Export to :" + title);
			JPanel panel = new JPanel();
			panel.add(button);
			panel.add(pb);

			JPanel panel1 = new JPanel();
			panel1.setLayout(new BorderLayout());
			panel1.add(panel, BorderLayout.NORTH);
			panel1.add(label, BorderLayout.CENTER);
			panel1.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

			dlg = new JDialog(baseFrame, title, true);
			dlg.setContentPane(panel1);
			dlg.pack();
			dlg.setVisible(true);
			dlg.setBounds(200, 200, dlg.getWidth(), dlg.getHeight());
			dlg.setResizable(false);

		}
	}
}
