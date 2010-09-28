package sfcrimekmltojson;

/**
 *
 * @author Michael Lawrence Evans :: michael@longliveman.com
 */
import java.io.*;

public class Main {
    
     BufferedReader reader = null;
     BufferedWriter writer = null;

     int count = 0;

     String Categories[] = new String[3000];
     String Dates[] = new String[3000];
     String Lons[] = new String[3000];
     String Lats[] = new String[3000];

    public static void main(String[] args) {
        Main sfKMLToJSON = new Main();
        sfKMLToJSON.run(args);
    }

    public void run(String[] args) {

       String s = null;

       File inputDirectory = new File("input"); //The input directory is just another file in the file system
       File inputFile = inputDirectory.listFiles()[0];
       //listFiles returns an array of File objects for every file in the directory; listFiles has already
       //created file objects for every file in the directory
       File outputFile = new File("output/output.json");

       try{
           reader = new BufferedReader(new FileReader(inputFile));
           writer = new BufferedWriter(new FileWriter(outputFile));
       } catch(IOException e) {
           System.err.println(e); //coerce e to be a string; every object has a toString method
           System.exit(1);
       }
       
       s = getNonEmptyString();

       while(s.compareTo("<Placemark") != 0){
           s = getNonEmptyString(); //Reading through the data until we get to our data blocks
       }

       while(s.compareTo("<Placemark") == 0){

           while (true) { //always do it
                int placemarkTopicStringIndex = 0;
                int placemarkTopicStringStartIndex;
                int placemarkTopicEndIndex = 0;

                String placemarkLine = getLine();

                placemarkTopicStringIndex = placemarkLine.indexOf("\"Category\""); //assign

                if (placemarkTopicStringIndex >= 0) {
                    placemarkTopicStringStartIndex = placemarkTopicStringIndex + 11;

                    placemarkTopicEndIndex = placemarkLine.indexOf("<", placemarkTopicStringStartIndex); //Starting from
                    //placemarkTopicStringIndex+11; find the next "<"

                    String category = placemarkLine.substring(placemarkTopicStringStartIndex, placemarkTopicEndIndex);

                    Categories[count] = category;

                }

                placemarkTopicStringIndex = placemarkLine.indexOf("\"Date\"");

                if (placemarkTopicStringIndex >= 0) {
                    placemarkTopicStringStartIndex = placemarkTopicStringIndex + 7;

                    placemarkTopicEndIndex = placemarkLine.indexOf("<", placemarkTopicStringStartIndex); //Starting from
                    //placemarkTopicStringIndex+7; find the next "<"

                    String date = placemarkLine.substring(placemarkTopicStringStartIndex, placemarkTopicEndIndex);

                    Dates[count] = date;
                }

                placemarkTopicStringIndex = placemarkLine.indexOf("coordinate"); //gives you the position of the first letter of the
                //first occurrence of coordinate

                if (placemarkTopicStringIndex >= 0) {
                    //Handle the Longitudes
                    placemarkTopicStringStartIndex = placemarkTopicStringIndex + 12;

                    placemarkTopicEndIndex = placemarkLine.indexOf(".", placemarkTopicStringStartIndex); //Starting from
                    //placemarkTopicStringIndex+12; find the next "."

                    String startLon = placemarkLine.substring(placemarkTopicStringStartIndex, placemarkTopicEndIndex);

                    //Set placemarkTopicStringStartIndex to previous placemarkTopicEndIndex
                    placemarkTopicStringStartIndex = placemarkTopicEndIndex;

                    placemarkTopicEndIndex += 5;

                    String endLon = placemarkLine.substring(placemarkTopicStringStartIndex, placemarkTopicEndIndex);

                    String Lon = startLon + endLon; //concatenate the string

                    Lons[count] = Lon;

                    //Handle the Latitudes
                    placemarkTopicStringStartIndex = placemarkLine.indexOf(",", placemarkTopicStringStartIndex) + 1;

                    placemarkTopicEndIndex = placemarkLine.indexOf(".", placemarkTopicStringStartIndex);

                    String startLat = placemarkLine.substring(placemarkTopicStringStartIndex, placemarkTopicEndIndex);

                    //Set placemarkTopicStringStartIndex to previous placemarkTopicEndIndex
                    placemarkTopicStringStartIndex = placemarkTopicEndIndex;

                    placemarkTopicEndIndex += 5;

                    String endLat = placemarkLine.substring(placemarkTopicStringStartIndex, placemarkTopicEndIndex);

                    String Lat = startLat + endLat; //concatenate the string

                    Lats[count] = Lat;

                    break;
                }
            }

            count++;
            
             s = getNonEmptyString();

             while(s.compareTo("</Placemark>") != 0){
                s = getNonEmptyString();
            }

             s = getNonEmptyString();
       }

       try{
           outputFile();
           writer.close();
       } catch(IOException e) {
           System.exit(1);
       }
    }

    String line = null;
    String[] strings = null;
    int stringIndex = -1;

    public String getString() {

        String s = null;

        //See if we have any data already read in
        if (stringIndex >= 0){
            s = strings[stringIndex];
            stringIndex = stringIndex + 1;

            if (stringIndex >= strings.length){
                stringIndex = -1; //Resetting, because you are at the end of the line
            }
            return s; //Gets you out of the function
        }

        if (line == null){
            try {
                line = reader.readLine();
            } catch (IOException e) {
                System.err.println(e);
            }
        }
       //See if the reader has run out of data to read
       if (line == null) {
           return null;
       }

        strings = line.split(" ", -1); //splitting by space; the second argument ensures that the split
        //method will be applied on the entire line
        line = null; //Resetting the line

        s = strings[0]; //Getting the first string in the strings array

        if (strings.length == 1){
            //Rarely Happens, but this occurs when there is only one string on the line
            stringIndex = -1;
        } else {
            stringIndex = 1;
        }
        return s;
    }

     public String getNonEmptyString() {
	String s = getString();
	while (s != null && (s.length() == 0)) {
            s = getString();
	}
	return s;
     }

     public String getLine() {
        line = null;
        strings = null;
        stringIndex = -1;

        String result = null;

         try{
            result = reader.readLine();
         } catch (IOException e) {
            System.err.println(e);
            System.exit(1);
         }

        return result;
     }

     public void outputFile() throws IOException{

	 //Use the data to create a file in JSON format

         int i;
         
         System.out.println(count);
         writer.write("{", 0, 1);
         writer.newLine();
         writer.write(" " + "\"incidents\":{", 0, 14);
         writer.newLine();
         writer.write(" " + "\t" + "\"incident\":[", 0, 14);
         writer.newLine();

         for (i=0; i < count; i++){

             writer.write(" " + "\t" + "{",0,3);
             writer.newLine();
             writer.write(" " + "\t" + "\"category\":" + "\"" + Categories[i] + "\",", 0, Categories[i].length() + 16);
             writer.newLine();
             writer.write(" " + "\t" + "\"date\":" + "\"" + Dates[i] + "\",", 0, Dates[i].length() + 12);
             writer.newLine();
             writer.write(" " + "\t" + "\"long\":" + "\"" + Lons[i] + "\",", 0, Lons[i].length() + 12);
             writer.newLine();
             writer.write(" " + "\t" + "\"lat\":" + "\"" + Lats[i] + "\"", 0, Lats[i].length() + 10);
             writer.newLine();

             if (i < count - 1){
                writer.write(" " + "\t" + "},",0,4);
             } else {
                 writer.write(" " + "\t" + "}",0,3);
             }
             writer.newLine();
         }
             /* Using an if test above, so we don't need to use so much code
             writer.write(" " + "\t" + "{",0,3);
             writer.newLine();
             writer.write(" " + "\t" + "\"category\":" + "\"" + Categories[count-1] + "\"", 0, Categories[count-1].length() + 15);
             writer.newLine();
             writer.write(" " + "\t" + "\"date\":" + "\"" + Dates[count-1] + "\"", 0, Dates[count-1].length() + 11);
             writer.newLine();
             writer.write(" " + "\t" + "\"long\":" + "\"" + Lons[count-1] + "\"", 0, Lons[count-1].length() + 11);
             writer.newLine();
             writer.write(" " + "\t" + "\"lat\":" + "\"" + Lats[count-1] + "\"", 0, Lats[count-1].length() + 10);
             writer.newLine();
             writer.write(" " + "\t" + "}",0,3);
             writer.newLine();
             */
             writer.write("\t" + "]",0,2);
             writer.newLine();
             writer.write(" " + "}",0,2);
             writer.newLine();
             writer.write("}",0,1);

     }
}