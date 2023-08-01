# Combine PDFs
A tool to combine all PDF documents within a directory into one long PDF
## Setup
Java SE 17 is required to run this program. An installer for Temurin/OpenJDK 17 should be included with the file, running the installer will open a window guiding you through the installation. Leaving everything as default should work perfectly, just click through the pages. If the installer is not included, it can be downloaded from [here](https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.8%2B7/OpenJDK17U-jdk_x64_windows_hotspot_17.0.8_7.msi). After installing Temurin/Java 17, the `.JAR` should run just by double-clicking.
## GUI
When `Combine PDFs.JAR` is run, it will open a large window, consisting of three parts:
1. Directory select: a window to select the directory that will be searched for the PDFs. Make sure that after you've selected the directory you press `Open`.
2. Output select: a window to select the output location. This can be an existing PDF, though the contents of the PDF will be entirely overwritten. For this reason, it is recommended to specify a new file, by navigating to a directory and typing any file name. Note that you can specify a different file extension (e.g. `.png`), but the file will save as a pdf, and thus may not open correctly with a different extension. Again, press `Open` after selecting an output.
3. `Cancel`, `Run`, and info panel: `Cancel` will close the application, and `Run` will start the program. If nothing happens when pressing `Run`, you have not correctly opened a directory or output location. Make sure you've pressed `Open` for each. The info panel below these buttons will give information about what's happening as the program's running. When it has completed, this panel will display '`Done!`' and the number of pages copied.
## Troubleshooting
> `Run` isn't working
* Ensure you've selected a directory and output file correctly, and pressed `Open` for each.
* The button should turn gray and the info text should change to say '`Running...`' when  pressed
---
> I can't open the output file
* The file outputted will always be a PDF, even if the extension is something else, or if there is no extension. When you choose an output and type a name in, you can manually type '.pdf' after the name you choose, and this will ensure that your computer recognizes the file as a pdf. Alternatively, you can manually rename the files to have this extension (make sure the box next to `File name extensions` is checked under `View` in your file explorer). Finally, if renaming isn't working, right clicking the file and choosing `Open with`, then selecting an application for opening PDFs, such as Revu, Adobe Acrobat, Sejda, or any other PDF editor should open it even without the correct extension
---
> The `.JAR` won't run
* Try right-clicking and selecting `Open with -> OpenJDK Platform binary`. If this doesn't work, or doesn't show up, ensure you've installed OpenJDK as specified under [Setup](#setup)
## Details of what it does
The program will deep search the given directory (that is, search not only the immediate directory but any directories within it and all of its 'children', all the way down). Any PDFs within those directories will be copied, one after the other, to the output directory. Any blank pages (determined by having at least 99.9% of the pixels on the page be white or light gray, lighter than `#F8F8F8`) will not be copied over. Additionally, the program will add bookmarks to the pdf at the beginning of each copied page, in the format '`AB###### filename 20##-MM-DD`'. This is the AB-number, pulled from the path of the file when it was copied; the original filename, including the extension, of the copied file; and the creation date of the file.
## Changing the code
The `.JAR` file is compiled and compressed, meaning it is not human-readable code. If you want to change how the program works, add new features, or anything else, I have uploaded the `.java` file to a github repository, along with this `README.md`(and a .HTML version in case you can't open the .md) and the `.JAR` itself. The `.java` file can be downloaded and edited like any other text file, though I'd recommend using an IDE. When compiling the code again, make sure to include the pdfbox classes specified in the imports section of the file (these are already included in the `.JAR` file I compiled). They can be downloaded from [here](https://pdfbox.apache.org/download.html).

The mentioned github repo is [here](https://github.com/Jaden-Unruh/Combine-PDFs-Tool).