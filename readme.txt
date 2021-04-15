- Start the Spring Boot web server. By default it starts at port 9090. Command: "java -jar safedeedriskanalysis-1.0-SNAPSHOT"
- Open a browser at localhost:9090
- There are 3 different types of data. The folder "sampledata" contains samples.

--------------------------------------------------------------------------------------

Tabular (different name for non-aggregated)
- Choose the file from the file system.
- Specify its radio button
- Separator ;
- Click "Upload"
- "Select All" button to select all QIs
- Click "Analyse"
- Specify k (2 is the fastest)
- Click "Anonymise" (takes ~10 seconds on 8 cores)
- You will be prompted to download the anonymised file


-------------------------------------------------------------------------------------------

Aggregated
- Choose the file from the file system.
- Specify its radio button
- Separator ;
- Click "Upload"
- Choose "shares" from the list
- Specify k (2 for demo)
- Click "Analyse"


----------------------------------------------------------------------------------------------


Invoices
- Choose the file from the file system.
- Specify its radio button
- Separator \t
- Click "Upload"
- Specify the following for the columns:
	Identifier of Individuals: "Customer Code"
	Invoice Date: "Order Entry Date"
	Date Format: MM.yyyy
	Invoice Amount: "Order quantity"
- Specify the privacy parameters:
	At least [integer] other individuals (for demo 1)
	Having an invoice amount within [integer or double with . as decimal separator] (for demo 1000)
	Within a timeframe of [integer] (0 or 1 for demo) Months (data is monthly)
- Click "Analyse"