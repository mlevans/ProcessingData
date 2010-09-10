
package tsunamidata;

import java.io.*;

public class Main {

    BufferedReader reader = null;
    BufferedWriter writer = null;
    FileOutputStream fos = null;

    int ndata;

    //Arrays for Data
    int Months[] = new int[2400];
    int Days[] =  new int[2400];
    int Years[] = new int[2400];
    float Lats[] = new float[2400];
    float Lons[] = new float[2400];
    int Magnitudes[] = new int[2400];
    int Sources[] = new int[2400];
    String Countries[] = new String[2400];
    String Names[] = new String[2400];
    float TsunMagnitudes[] = new float[2400];
    float TsunIntensities[] = new float[2400];
    int Deaths[] = new int[2400];
    int Injuries[] = new int[2400];
    float Damages[] = new float[2400];


    public static void main(String[] args) {
        Main Tsunami = new Main();
        Tsunami.run(args);
    }

    public void run(String[] args){
        File inDir = new File("input");
        File inFile = inDir.listFiles()[0];
        File outFile = new File("output/tsunami.dat");

        ndata = 0;

        try {
            reader = new BufferedReader(new FileReader(inFile));
            writer = new BufferedWriter(new FileWriter(outFile));

            fos = new FileOutputStream(outFile);

        } catch (IOException e) {
            System.err.println(e);
            System.exit(1);
        }

        String s = null;

        String sYear = null;
        String sMonth = null;
        String sDay = null;
        String sProb = null;
        String sSource = null;
        String sMag = null;
        String sCountry = null;
        String sName = null;
        String sLat = null;
        String sLon = null;
        String sTsunMag = null;
        String sTsunInt = null;
        String sDeaths = null;
        String sInjuries = null;
        String sDamages = null;

        s = getString1();

        while (s != null) {

            try {
                sYear = s;
                //System.out.println(sYear);
                sMonth = getString1();
                //System.out.println(sMonth);
                sDay = getString1();
                sProb = getString1();
                sSource = getString1();
                sMag = getString1();
                sCountry = getString1();
                sName = getString1();
                sLat = getString1();
                sLon = getString1();
                sTsunMag = getString1();
                sTsunInt = getString1();
                sDeaths = getString1();
                sInjuries = getString1();
                sDamages = getString1();

                if ((sLat.length() != 0) && (sLon.length() != 0) && sYear.length() != 0 && (sProb.compareTo("3") == 0 || sProb.compareTo("4") == 0)){
                    //writeDate(sYear, sMonth, sDay);
                    dateArrayed(sYear, sMonth, sDay);
                    geoArrayed(sLat, sLon);
                    sourceArrayed(sSource);
                    magnitudeArrayed(sMag);
                    countryArrayed(sCountry);
                    nameArrayed(sName);
                    //tsunMagArrayed(sTsunMag); //excluding this data for the moment
                    tsunIntArrayed(sTsunInt);
                    deathsArrayed(sDeaths);
                    injuriesArrayed(sInjuries);
                    damagesArrayed(sDamages);

                    ndata += 1;
                }


            } catch (IOException e){
                System.out.println("Error");
            }

            s = getString1();

        }

        try {
            fos.write((ndata >> 8) & 0xff);
            fos.write(ndata & 0xff);
            for (int i = 0; i < ndata; i++){
                writeDate(i);
                writeLat(i);
                writeLon(i);
                writeSource(i);
                writeMagnitude(i);
                writeCountry(i);
                writeName(i);
                //writeTsunMag(i); //excluding this data for the moment
                writeTsunInt(i);
                writeDeaths(i);
                writeInjuries(i);
                writeDamages(i);
            }
            writer.close();
        } catch (IOException e) {
            System.err.println(e);
            System.exit(1);
        }

    }

    String line = null;
    String[] strs = null;
    int strindx = -1;
    String s0 = null;
    String s1 = null;
    String s2 = null;

    public String getString1() {

        String s = null;
        s0 = s1;
        s1 = s2;

        if (strindx >= 0){
            s = strs[strindx];
            strindx = strindx + 1;

            if (strindx >= strs.length){
                strindx = -1;
            }
            s2 = s;
            return s;
        }

        if (line == null){
            try {
                line = reader.readLine();
            } catch (IOException e) {
                System.err.println(e);
            }
        }
       if (line == null) {
           s2 = null;
           return null;
       }

        strs = line.split("\t", -1);
        line = null;

        s = strs[0];

        if (strs.length == 1){
            strindx = -1;
        } else {
            strindx = 1;
        }
        s2 = s;
        return s;
    }

     public String getString() {
	String s = getString1();
	while (s != null && (s.length() == 0)) {
            s = getString1();
	}

	return s;
     }
	
     //some of the original write methods
     /*
     private void writeProbable(String s) throws IOException{
        writer.write(s, 0, s.length());
     }
      */
     /*
     private void writeLat(String s) throws IOException{
         writer.write(s, 0, s.length());
     }

     private void writeLon(String s) throws IOException{
         writer.write(s,0,s.length());
     }
    */
     //

     private void magnitudeArrayed(String s) throws IOException{
         int Magnitude;

         if (s.length() == 0){
             Magnitude = 0;
         } else if (s.compareTo("*") == 0){
             Magnitude = 0;
         } else {
             Magnitude = (int)(Float.parseFloat(s) * 10); //0 to 99
         }

         Magnitudes[ndata] = Magnitude;
     }

     private void writeMagnitude(int i) throws IOException{
        //0 = NA

         
         fos.write(Magnitudes[i] & 0xff);
         /*
         if (s.compareTo("*") == 0){ //returns 0 if character by character the same string
            writer.write("NA",0,2);
         } else {
            writer.write(s,0,s.length());
        }
        */
    }

     private void sourceArrayed(String s) throws IOException{
        int Source;

         if (s.length() == 0){
             Source = -1; //-1 = NA
         } else {
             Source = Integer.parseInt(s);
         }

        Sources[ndata] = Source;
     }
     
     private void writeSource(int i) throws IOException{

         fos.write(Sources[i] & 0xff);

         //writer.write(s, 0, s.length());
     }

     private void dateArrayed(String sYear, String sMonth, String sDay) throws IOException{
            int Month;

            if (sMonth.length() == 0){
                Month = 0;
            } else {
                Month = Integer.parseInt(sMonth);
            }

            Months[ndata] = Month;

            int Day;

            if (sDay.length() == 0){
                Day = 0;
            } else {
                Day = Integer.parseInt(sDay);
            }

            Days[ndata] = Day;
            int Year = Integer.parseInt(sYear);

            Years[ndata] = Year;
     }
     
     private void writeDate(int i) throws IOException{
            
            fos.write(Months[i] & 0xff);

            fos.write(Days[i] & 0xff);

            fos.write((Years[i] >> 8) & 0xff);
            fos.write(Years[i] & 0xff);

            //System.out.println(Years[i]);
     }

     private void countryArrayed(String s) throws IOException{
        Countries[ndata] = s;
     }

     private void writeCountry(int i) throws IOException{
         char letter;

         int stringLength = Countries[i].length();
         fos.write(stringLength);

         int j;

         for (j=0; j < stringLength; j++){
            letter = Countries[i].charAt(j);

            fos.write(letter & 0xff); //writes to end of the string
         }

     }

     private void nameArrayed(String s) throws IOException{
         Names[ndata] = s;
     }

     private void writeName(int i) throws IOException{
         char letter;

         int stringLength = Names[i].length();
         fos.write(stringLength);

         int j;

         for (j=0; j < stringLength; j++){
             letter = Names[i].charAt(j);

             fos.write(letter & 0xff);
         }

     }


     private void geoArrayed(String sLat, String sLon) throws IOException{
         
	 //We have arrays of latitude and longitudes from the data.

	 float Lat;

         if (sLat.length() == 0){
             Lat = 0.0f;
         } else {
             Lat = Float.parseFloat(sLat); //convert Lat to a Float
         }

         Lats[ndata] = Lat;

         float Lon;

         if (sLon.length() == 0){
             Lon = 0.0f;
         } else {
             Lon = Float.parseFloat(sLon);
         }

         Lons[ndata] = Lon;
     }

     private void writeLat(int n) throws IOException{
         int i;
         float frac;
         int f;

	 //for debugging
         if (n <= 10){
             //System.out.println(n);
             //System.out.println(Years[n]);
             //System.out.println(Lats[n]);
         }

         if (Lats[n] < 0){
             i = (int)Lats[n];
             frac = Lats[n] - (float)i;
             frac = -frac; //making it positive
             f = (int)(frac * 65536.0); //might be able to use 8 bits instead of 16
         } else {
             i = (int)Lats[n];
             frac = Lats[n] - (float)i;
             f = (int)(frac * 65536.0);
         }

         fos.write(i & 0xff);
         fos.write((f >> 8) & 0xff);
         fos.write(f & 0xff);
     }

     private void writeLon(int n) throws IOException{
         int i;
         float frac;
         int f;

	 //for debugging
         if (n <= 10){
            //System.out.println(n);
            //System.out.println(Lons[n]);
         }

         if (Lons[n] < 0){
             i = (int)Lons[n];
             frac = Lons[n] - (float)i;
             frac = -frac;
             f = (int)(frac * 65536.0);
         } else {
             i = (int)Lons[n];
             frac = Lons[n] - (float)i;
             f = (int)(frac * 65536.0);
         }

         fos.write((i >> 8) & 0xff);
         fos.write(i & 0xff);
         fos.write((f >> 8) & 0xff);
         fos.write(f & 0xff);
     }

     private void tsunMagArrayed(String s) throws IOException{
        float TsunMag;

         if (s.length() == 0){
             TsunMag = 0.0f;
         } else {

             TsunMag = Float.parseFloat(s);
         }

        TsunMagnitudes[ndata] = TsunMag;
     }

     private void writeTsunMag(int i) throws IOException{
         int tm;

         tm = (int)(TsunMagnitudes[i]*10.0 + 0.5); //transmitting integer data, reconvert on the other end

         fos.write(tm & 0xff);

     }

     private void tsunIntArrayed(String s) throws IOException{
        float TsunInt;

         if (s.length() == 0){
             TsunInt = 0.0f;
         } else {
             TsunInt = Float.parseFloat(s);
         }

        TsunIntensities[ndata] = TsunInt;
     }

     private void writeTsunInt(int i) throws IOException{
         int ti;
         System.out.println(TsunIntensities[i]);
         ti = (int)((TsunIntensities[i] + 5.0)*10.0 + 0.5);

         fos.write(ti & 0xff);
     }

     private void deathsArrayed(String s) throws IOException{
         int D;

         if (s.length() == 0){
             D = 0;
         } else {
             D = Integer.parseInt(s);
             //System.out.println(D);
         }

         Deaths[ndata] = D;
     }

     private void writeDeaths(int i) throws IOException{
         fos.write((Deaths[i] >> 24) & 0xff);
         fos.write((Deaths[i] >> 16) & 0xff);
         fos.write((Deaths[i] >> 8) & 0xff);
         fos.write(Deaths[i] & 0xff);
     }

     private void injuriesArrayed(String s) throws IOException{
         int I;

         if (s.length() == 0){
             I = 0;
         } else {
            I = Integer.parseInt(s);
         }

         Injuries[ndata] = I;
     }

     private void writeInjuries(int i) throws IOException{
         //this is a comment about the binary compression: greatest number right now is 20,000, works up to 32,000, if more needed, write up to 4 bytes
         //can write with 2 bytes

         fos.write((Injuries[i] >> 8) & 0xff);
         fos.write(Injuries[i] & 0xff);
     }

     private void damagesArrayed(String s) throws IOException{
         float D;

         if (s.length() == 0){
             D = 0.0f;
         } else {
             D = Float.parseFloat(s);
         }

         Damages[ndata] = D;
     }

     private void writeDamages(int i) throws IOException{
         int dam;

         //using 2 bytes, greatest number right now is 30000

         dam = (int)(Damages[i]*2.0 + 0.5);

         fos.write((dam >> 8) & 0xff);
         fos.write(dam & 0xff);

     }

}