package csvconverter.patrykpacocha.csvconverter;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Converter extends Component {

	private final String csvBeforePath;
	private final String csvAfterPath;
	private final String selectedColumns;
	private int maxThreads;
	private ExecutorService pool;
	private String csvName;

	public Converter(int maxThreads, String csvBeforePath, String csvAfterPath, String selectedColumns, String csvName) throws IOException {
		this.maxThreads = maxThreads;
		this.csvBeforePath = csvBeforePath;
		this.csvAfterPath = csvAfterPath;
		this.selectedColumns = selectedColumns;
		this.pool = Executors.newFixedThreadPool(this.maxThreads);
		this.csvName = csvName;
		int[] selectedColumnsArray = selectedColumnsArray(selectedColumns);
		run(selectedColumnsArray);
	}

	public int[] selectedColumnsArray(String selectedColumns){
		String[] selectArray = selectedColumns.split(",");
		int[] selected = new int[selectArray.length];
		for(int i = 0; i < selectArray.length; i++){
			selected[i] = Integer.parseInt(selectArray[i]);
		}
		return selected;
	}

	public void run(int[] selected) throws IOException {
		if (csvBeforePath.isEmpty()) {
			throw new IllegalArgumentException("Invalid arguments");
		}
		ArrayList<String> linesFormatted = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(csvBeforePath))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String finalLine = line;
				pool.submit(() -> {
					String[] lineSplit = finalLine.split(";");
					StringBuilder sb = new StringBuilder();
					for (int i : selected) {
						sb.append(lineSplit[i-1]).append(";");
					}
					sb.append("\n");
					String output = sb.toString();
					linesFormatted.add(output);
				});
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		pool.shutdown();
		while (!pool.isTerminated()) {}
		File file = new File(csvAfterPath, csvName + "-CONVERTED.csv");
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			for (String line : linesFormatted) {
				writer.write(line);
			}
		} catch (IOException e) {
			throw new RuntimeException("Error writing to file", e);
		}
		JOptionPane.showMessageDialog(this, "Twój plik znajduje się w wybranym przez ciebie folderze", "SUKCES!", JOptionPane.INFORMATION_MESSAGE);

		System.exit(5000);
	}
}
