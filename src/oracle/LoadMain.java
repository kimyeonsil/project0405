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
		bt_open = new JButton("파일열기");
		bt_load = new JButton("로드하기");
		bt_excel = new JButton("엑셀로드");
		bt_del = new JButton("삭제하기");
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

		// 윈도우와 리스너와 연결
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
		// 커넥션 얻기
		con = manager.getConnection();
	}

	// 파일 탬색기 띄우기
	public void open() {
		int result = chooser.showOpenDialog(this);

		// 열기를 누르면 목적 파일에 스트림을 생성
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

	// csv>oracle로 데이터 이전하기
	public void load() {
		// 버퍼스트림을 이용하여 csv의 데이터를 1줄 씩 읽어들여 insert 시키자
		// 레코드가 없을 때 까지
		// while 문으로 돌리면 너무 빠르므로 네트워크가 감당할 수 없기 때문에 일부러 지연시키면서
		String data;
		StringBuffer sb = new StringBuffer();
		PreparedStatement pt = null;
		try {
			while (true) {
				data = buffer.readLine();

				if (data == null)
					break;

				String[] value = data.split(",");

				// seq줄 제외하고 insert하겠다
				if (!value[0].equals("seq")) {
					sb.append("insert into hospital(seq,name,addr,regdate,status,dimension,type)");
					sb.append(" values(" + value[0] + ",'" + value[1] + "','" + value[2] + "','" + value[3] + "','"
							+ value[4] + "'," + value[5] + ",'" + value[6] + "')");

					System.out.println(sb.toString());
					pt = con.prepareStatement(sb.toString());

					int result = pt.executeUpdate();// 쿼리 수행

					// 기존에 누적된 스트링버퍼의 데이터를 모두 지우기
					sb.delete(0, sb.length());

				} else {
					System.out.println("난 1줄이므로 제외");

				}
			}
			JOptionPane.showMessageDialog(this, "마이그레이션 완료");
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

	// 엑셀 파일 읽어서 db에 마이그레이션 하기
	// javaSE 엑셀제어 라이브러리 있다? 없음!
	// open source 공개 소프트웨어
	// copyright<==>copyleft (아파치단체)
	// POI 라이브러리 apache.org
	/*
	 * HSSFWorkbook:엑셀파일 HSSFSheet:시트 HSSFRow:row HSSFCell:셀
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
				sheet = book.getSheet("동물병원");

				int total = sheet.getLastRowNum();
				DataFormatter df=new DataFormatter();

				for (int a = 1; a <= total; a++) {
					HSSFRow row = sheet.getRow(a);

					int columnCount = row.getLastCellNum();
						
					
					for (int i = 0; i < columnCount; i++) {
						HSSFCell cell=row.getCell(i);
						//자료형에 국한되지않고 모두 string처리
						String value=df.formatCellValue(cell);
						
						System.out.print(value);
						
					}
					System.out.println("");
				}

				// HSSFRow row = sheet.getRow(0);// 0번째 불러옴
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

	// 선택한 레코드 삭제
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
