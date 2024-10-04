import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.rendering.PDFRenderer;

import com.sun.jdi.InvalidTypeException;

/**
 * Entry class for PDF reader - searches selected directory for all pdfs and combines them,
 * removing blank pages and duplicated pages, into a specified output PDF.
 * @author Jaden Unruh
 *
 */
public class Main {
	
	static JFrame options;
	static JLabel info;

	public static void main(String[] args) {
		openWindow();
	}
	
	/**
	 * Constructs the GUI
	 */
	static void openWindow() {
		options = new JFrame("Combine PDF tool");
		options.setSize(800, 700);
		options.setLayout(new GridBagLayout());
		options.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		GridBagConstraints text1Constr = new GridBagConstraints();
		text1Constr.gridx = 0;
		text1Constr.gridy = 0;
		text1Constr.gridwidth = 2;
		options.add(new JLabel("Select an input directory"), text1Constr);
		
		JFileChooser dirSelect = new JFileChooser();
		dirSelect.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		GridBagConstraints dirSelectConstr = new GridBagConstraints();
		dirSelectConstr.gridx = 0;
		dirSelectConstr.gridy = 1;
		dirSelectConstr.gridwidth = 2;
		options.add(dirSelect, dirSelectConstr);
		
		GridBagConstraints text2Constr = new GridBagConstraints();
		text2Constr.gridx = 0;
		text2Constr.gridy = 2;
		text2Constr.gridwidth = 2;
		options.add(new JLabel("Select an output PDF"), text2Constr);
		
		JFileChooser outputSelect = new JFileChooser();
		GridBagConstraints outputSelectConstr = new GridBagConstraints();
		outputSelectConstr.gridx = 0;
		outputSelectConstr.gridy = 3;
		outputSelectConstr.gridwidth = 2;
		options.add(outputSelect, outputSelectConstr);
		
		JButton cancel = new JButton("Close");
		cancel.setSize(200, 150);
		GridBagConstraints cancelConstr = new GridBagConstraints();
		cancelConstr.gridx = 0;
		cancelConstr.gridy = 4;
		cancelConstr.weightx = cancelConstr.weighty = 1;
		options.add(cancel, cancelConstr);
		JButton run = new JButton("Run");
		run.setSize(200, 150);
		GridBagConstraints runConstr = new GridBagConstraints();
		runConstr.gridx = 1;
		runConstr.gridy = 4;
		runConstr.weightx = runConstr.weighty = 1;
		options.add(run, runConstr);
		
		info = new JLabel("<html><div style='text-align: center;'>Select an input directory and output file and press 'Open' for each, then press 'Run'<br>You can type in a new file for output</html>", SwingConstants.CENTER);
		GridBagConstraints infoConstr = new GridBagConstraints();
		infoConstr.gridx = 0;
		infoConstr.gridy = 5;
		infoConstr.gridwidth = 2;
		options.add(info, infoConstr);
		
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		
		run.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					File directory = dirSelect.getSelectedFile();
					File output = outputSelect.getSelectedFile();
					if(!output.toString().toLowerCase().endsWith(".pdf"))
						output = new File(output.toString() + ".pdf");
					final File reOutput = output;
					info.setText(String.format("<html><div style='text-align: center;'>Directory: %s, Output: %s<br>Running...</html>", directory.getName(), output.getName()));
					options.pack();
					
					SwingWorker<Boolean, Void> sw = new SwingWorker<Boolean, Void>() {
						@Override
						protected Boolean doInBackground() throws Exception {
							combinePDFs(directory, reOutput);
							run.setEnabled(true);
							return true;
						}
						
						@Override
						protected void done() {
							try {
								get();
							} catch (InterruptedException | ExecutionException e) {
								run.setEnabled(true);
								try {
									showErrorMessage(e);
								} catch (IOException e1) {
								}
							}
						}
					};
					run.setEnabled(false);
					sw.execute();
				} catch(NullPointerException e1) {
					info.setText("<html><div style='text-align: center;'>Select an input directory and output file and press 'Open' for each, then press 'Run'<br>You can type in a new file for output</html>");
				}
			}
		});
		
		options.pack();
		options.setVisible(true);
	}
	
	static void showErrorMessage(Exception e) throws IOException {
		e.printStackTrace();
		String[] choices = {"Close", "More Info..."};
		if (JOptionPane.showOptionDialog(options, String.format("Unexpected Problem:\n%s", e.toString()), "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, choices, choices[0]) == 1) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			JTextArea jta = new JTextArea(25, 50);
			jta.setText(String.format("Full Error Stack Trace:\n%s", sw.toString()));
			jta.setEditable(false);
			JOptionPane.showMessageDialog(options, new JScrollPane(jta), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	static int count = 0;
	
	/**
	 * Deep searches the given directory for any pdfs that don't contain the word "report" in their path and adds them, except for blank pages, to the given output pdf
	 * @param input Search directory
	 * @param output PDF file
	 */
	static void combinePDFs(File input, File output) {
		ArrayList<File> allPDFs = new ArrayList<>();
		try {
			addPDFs(input, allPDFs);
		} catch (InvalidTypeException e) {
			e.printStackTrace();
		}
		info.setText(String.format("<html><div style='text-align: center;'>Directory: %s, Output: %s<br>Running...<br>Found %d files, adding to output...</html>", input.getName(), output.getName(), count));
		options.pack();
	//	options.repaint();
		
		PDDocument outputPDF = new PDDocument();
		PDDocumentOutline outline = new PDDocumentOutline();
		outputPDF.getDocumentCatalog().setDocumentOutline(outline);
		
		PDOutlineItem root = new PDOutlineItem();
		root.setTitle("Combined PDFs");
		outline.addFirst(root);
		
		try {
			outputPDF.save(output);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int totalPages = 0;
		
		HashSet<PDDocument> docsToClose = new HashSet<>();
		
		for(File pdf : allPDFs) {
			try {
				PDDocument activePDF = PDDocument.load(pdf);
				
				PDOutlineItem activeBookmark = new PDOutlineItem();
				FileTime creationTime = Files.readAttributes(pdf.toPath(), BasicFileAttributes.class).creationTime();
				String creationTimeString = creationTime.toString().substring(0, 10);
				
				Matcher abMatcher = Pattern.compile("AB\\d{6,6}").matcher(pdf.getAbsolutePath());
				abMatcher.find();
				String abNumString = pdf.getAbsolutePath().substring(abMatcher.start(), abMatcher.end());
				
				activeBookmark.setTitle(abNumString + " " + pdf.getName() + " " + creationTimeString);
				
				int targetPage = totalPages;
				
				PDPageTree pages = activePDF.getPages();
				PDFRenderer renderedDoc = new PDFRenderer(activePDF);
				int currentPage = 0;
				for(PDPage page : pages) {
					if(isBlank(renderedDoc.renderImage(currentPage)))
						System.out.printf("Page %d of %s is empty, skipping...\n", currentPage + 1, pdf.getAbsolutePath());
					else {
						outputPDF.addPage(page);
						totalPages++;
					}
					currentPage++;
				}
				docsToClose.add(activePDF);
				
				PDPage bookmarkedPage = (PDPage)outputPDF.getPages().get(targetPage);
				activeBookmark.setDestination(bookmarkedPage);
				root.addLast(activeBookmark);
			} catch (IOException e) {
				System.out.printf("Failed to open %s\n", pdf.getAbsolutePath());
			}
		}
		
		try {
			outputPDF.save(output);
			outputPDF.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for(PDDocument doc : docsToClose) {
			try {
				doc.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		info.setText(String.format("<html><div style='text-align: center;'>Done!<br>Added %d pages to %s</html>", totalPages, output.getName()));
		options.pack();
		options.repaint();
	}
	
	/**
	 * Checks if a given image is empty, i.e. at least 99.9% of the pixels in it are white or very light gray
	 * 
	 * @param pageImage The image to check
	 * @return A boolean, true if the image is empty
	 * @throws IOException
	 */
	private static boolean isBlank(BufferedImage pageImage) throws IOException {
	    BufferedImage bufferedImage = pageImage;
	    long count = 0;
	    int height = bufferedImage.getHeight();
	    int width = bufferedImage.getWidth();
	    Double areaFactor = (width * height) * 0.999;

	    for (int x = 0; x < width; x++) {
	        for (int y = 0; y < height; y++) {
	            Color c = new Color(bufferedImage.getRGB(x, y));
	            if (c.getRed() == c.getGreen() && c.getRed() == c.getBlue() && c.getRed() >= 248) {
	                count++;
	            }
	        }
	    }
	    if (count >= areaFactor) {
	        return true;
	    }
	    return false;
	}
	
	/**
	 * Recursive method adding all pdfs within the given directory to the given list
	 * 
	 * @param activeDirectory The directory currently being searched
	 * @param list The list of all pdfs found so far
	 * @throws InvalidTypeException If the given file is not a directory
	 */
	static void addPDFs(File activeDirectory, ArrayList<File> list) throws InvalidTypeException {
		if(!activeDirectory.isDirectory())
			throw new InvalidTypeException();
		File[] files = activeDirectory.listFiles();
		for (File file : files) {
			if(file.isDirectory())
				addPDFs(file, list);
			else if (file.getAbsolutePath().toLowerCase().endsWith(".pdf") && !file.getAbsolutePath().toLowerCase().contains("report") && !file.getName().toLowerCase().contains("report")) {
				list.add(file);
				count++;
			}
		}
	}
}