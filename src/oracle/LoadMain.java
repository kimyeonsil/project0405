package oracle;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;

public class LoadMain extends JFrame implements ActionListener {
	JPanel p_north;
	JTextField t_path;
	JButton bt_open, bt_load, bt_excel, bt_del;
	JTable table;
	JScrollPane scroll;
	JFileChooser chooser;
	FileReader reader = null;
	BufferedReader buffer = null;

	DBManager manager = DBManager.getInstance();
	Connection con;

	public LoadMain() {
		p_north = new JPanel();
		bt_open = new JButton("���Ͽ���");
		bt_load = new JButton("�ε��ϱ�");
		bt_excel = new JButton("�����ε�");
		bt_del = new JButton("�����ϱ�");
		t_path = new JTextField(25);

		table = new JTable();
		scroll = new JScrollPane();
		chooser = new JFileChooser("C:/animal");

		p_north.add(t_path);
		p_north.add(bt_open);
		p_north.add(bt_load);
		p_north.add(bt_excel);
		p_north.add(bt_del);

		add(p_north, BorderLayout.NORTH);
		add(scroll);

		bt_open.addActionListener(this);
		bt_load.addActionListener(this);
		bt_excel.addActionListener(this);
		bt_del.addActionListener(this);

		// ������� �����ʿ� ����
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				manager.disConnect(con);

				System.exit(0);
			}
		});

		setVisible(true);
		setSize(800, 600);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		init();
	}

	public void init() {
		// Ŀ�ؼ� ���
		con = manager.getConnection();
	}

	// ���� �ƻ��� ����
	public void open() {
		int result = chooser.showOpenDialog(this);

		// ���⸦ ������ ���� ���Ͽ� ��Ʈ���� ����
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			t_path.setText(file.getAbsolutePath());

			try {
				reader = new FileReader(file);
				buffer = new BufferedReader(reader);

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	// csv>oracle�� ������ �����ϱ�
	public void load() {
		// ���۽�Ʈ���� �̿��Ͽ� csv�� �����͸� 1�� �� �о�鿩 insert ��Ű��
		// ���ڵ尡 ���� �� ����
		// while ������ ������ �ʹ� �����Ƿ� ��Ʈ��ũ�� ������ �� ���� ������ �Ϻη� ������Ű�鼭
		String data;
		StringBuffer sb = new StringBuffer();
		PreparedStatement pt = null;
		try {
			while (true) {
				data = buffer.readLine();

				if (data == null)
					break;

				String[] value = data.split(",");

				// seq�� �����ϰ� insert�ϰڴ�
				if (!value[0].equals("seq")) {
					sb.append("insert into hospital(seq,name,addr,regdate,status,dimension,type)");
					sb.append(" values(" + value[0] + ",'" + value[1] + "','" + value[2] + "','" + value[3] + "','"
							+ value[4] + "'," + value[5] + ",'" + value[6] + "')");

					System.out.println(sb.toString());
					pt = con.prepareStatement(sb.toString());

					int result = pt.executeUpdate();// ���� ����

					// ������ ������ ��Ʈ�������� �����͸� ��� �����
					sb.delete(0, sb.length());

				} else {
					System.out.println("�� 1���̹Ƿ� ����");

				}
			}
			JOptionPane.showMessageDialog(this, "���̱׷��̼� �Ϸ�");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (pt != null) {
				try {
					pt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// ���� ���� �о db�� ���̱׷��̼� �ϱ�
	// javaSE �������� ���̺귯�� �ִ�? ����!
	// open source ���� ����Ʈ����
	// copyright<==>copyleft (����ġ��ü)
	// POI ���̺귯�� apache.org
	/*
	 * HSSFWorkbook:�������� HSSFSheet:��Ʈ HSSFRow:row HSSFCell:��
	 */
	public void loadExcel() {
		int result = chooser.showOpenDialog(this);

		if (result == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			FileInputStream fis = null;

			try {
				fis = new FileInputStream(file);

				HSSFWorkbook book = null;
				book = new HSSFWorkbook(fis);

				HSSFSheet sheet = null;
				sheet = book.getSheet("��������");

				int total = sheet.getLastRowNum();
				DataFormatter df=new DataFormatter();

				for (int a = 1; a <= total; a++) {
					HSSFRow row = sheet.getRow(a);

					int columnCount = row.getLastCellNum();
						
					
					for (int i = 0; i < columnCount; i++) {
						HSSFCell cell=row.getCell(i);
						//�ڷ����� ���ѵ����ʰ� ��� stringó��
						String value=df.formatCellValue(cell);
						
						System.out.print(value);
						
					}
					System.out.println("");
				}

				// HSSFRow row = sheet.getRow(0);// 0��° �ҷ���
				// HSSFCell cell = row.getCell(0);
				// System.out.println(cell.getStringCellValue());
				// }

			} catch (FileNotFoundException e) {
				e.printStackTrace();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// ������ ���ڵ� ����
	public void delete() {

	}

	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();

		if (obj == bt_open) {
			open();
		} else if (obj == bt_load) {
			load();
		} else if (obj == bt_excel) {
			loadExcel();
		} else if (obj == bt_del) {
			delete();
		}
	}

	public static void main(String[] args) {
		new LoadMain();

	}

}
