package processnycgraffitidata;

/**
 *
 * @author Michael Lawrence Evans :: michael@longliveman.com
 */

import java.io.*;
import java.util.Random;

public class Main {

    BufferedReader reader = null;
    FileOutputStream fos = null;
    Random random;
    int ndata;

    float minLat;
    float maxLat;
    float minLon;
    float maxLon;

    float latDelta, lonDelta;
    int[] latlonBuckets = new int[100];
    int[] days = new int[26500];
    int[] months = new int[26500];
    float[] progress = new float[26500];
    float[] lats = new float[26500];
    float[] longs = new float[26500];
    int[] boroughs = new int[26500];
    int[] stats = new int[26500];
    int[] tileNums = new int[26500];
    int[] lods = new int[26500];
    TileData[] tiles = new TileData[100];
    int tileCount = 0;
    int visByMarkerMax;
    int visByTileMax;
    int[] LODindices = new int[65000]; //upper limit of LODindices = number of levels of detail * ndata (incidents)
    int LODindexCount;


    public static void main(String[] args) {
        Main nycd = new Main();
        nycd.run(args);
    }

    public void run(String[] args) {

        File inDir = new File("input");
        File inFile = inDir.listFiles()[0];
        File outFile = new File("output/data.dat");

        ndata = 0;
        for(int i = 0; i < 100; i++){
            latlonBuckets[i] = 0;
        }

        try {
            reader = new BufferedReader(new FileReader(inFile));
            fos = new FileOutputStream(outFile);
            //line = reader.readLine();

        } catch (IOException e) {
            System.err.println(e);
            System.exit(1);
        }

        String s = null;

        String sStatus = null;
        String sDay = null; //new
        String sDate = null;
        String sLat = null;
        String sLong = null;
        String sBorough = null;
        String sDescription = null;

        s = getString();

        while (s != null) {
            sStatus = s;
            //sDay = getString();
            sDate = getString();
            sLat = getString();
            sLong = getString();
            sBorough = getString();
            sDescription = getString();

            if ((sLat.compareTo("NA") == 0) || (sLong.compareTo("NA") == 0)){
                s = getString();

                continue;
            }

            if (sDescription.compareTo("Cleaning crew dispatched. No graffiti on property.") == 0){
                s = getString();

                continue;
            }

            if (sDescription.compareTo("Graffiti is not on property reported") == 0){
                s = getString();

                continue;
            }

            try {
                //days[ndata] = readDate(sDay);
                //months[ndata] = readDate(sDate);
                readDate(sDate,ndata);

                lats[ndata] = readLat(sLat);
                longs[ndata] = readLong(sLong);
                boroughs[ndata] = readBorough(sBorough);
                stats[ndata] = readStatus(sStatus);
            } catch (IOException e){
                System.out.println("Error");
            }

            ndata += 1;

            s = getString();

        }
        
	/*
	//debugging
	System.out.println("ndata: " + ndata);
        System.out.println("minLat = " + minLat);
        System.out.println("maxLat = " + maxLat);
        System.out.println("minLon = " + minLon);
        System.out.println("maxLon = " + maxLon);
	*/

	computeMinMax();
        computeTileNums();
        computeMaxVisibles();
        computeLatLongMonthBins();
        computeLODs();
        calculateMarkerProximity();

        try {
            writeLatf(minLat);
            writeLatf(maxLat);
            writeLongf(minLon);
            writeLongf(maxLon);
            fos.write((ndata >> 8) & 0xff);
            fos.write(ndata & 0xff);
        } catch (IOException e) {
            System.out.println("Error");
        }
        for (int i = 0; i < ndata; i++) {
            try {
                writeDaysMonthsAndBorough(i); //combining data to compress the data file
                writeLatLong(i);
                writeProximity(i);
                //writeProgress(i);
                // writeBorough(i);
                // writeStatus(i);
                writeTileNum(i);
                writeLOD(i);
            } catch (IOException e){
                System.out.println("Error");
            }
        }

        try {
            writeTileData();
            fos.close();
        } catch (IOException e) {
            System.out.println("Error");
        }

        for (int i = 0; i < 100; i++){
            System.out.println(latlonBuckets[i]);
        }
    }


    String line = null;
    String[] strs = null;
    int strindx = -1;
    String s0 = null;
    String s1 = null;
    String s2 = null;

    public String getString(){

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
            try{
                line = reader.readLine();
            } catch(IOException e){
                System.err.println(e);
            }
        }
       if (line == null){
           s2 = null;
           return null;
       }

        strs = line.split("\t");
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

    public void flushLine() {
        line = null;
        strs = null;
        strindx = -1;
    }

    private void readDate(String date, int ndata) throws IOException{
        int month = Integer.parseInt(date.substring(0,2));
        int day = Integer.parseInt(date.substring(3,5));
        int year = Integer.parseInt(date.substring(6,8));

        year = year - 8; //start with 8, denoted as year zero
        month = (year * 12) + month; //1 <= month <= 28 (April 2010)
        
        months[ndata] = month;
        days[ndata] = day;
        //return month;
    }

    private float readLat(String sLat) throws IOException{
        float lat = Float.parseFloat(sLat);

        return lat;
    }

    private float readLong(String sLong) throws IOException{
        float lon = Float.parseFloat(sLong);

        return lon;
    }

    private int readBorough(String sBorough) throws IOException{
        if(sBorough.compareTo("BRONX") == 0){
            return 0;
        }
        if(sBorough.compareTo("BROOKLYN") == 0){
            return 1;
        }
        if(sBorough.compareTo("MANHATTAN") == 0){
            return 2;
        }
        if(sBorough.compareTo("QUEENS") == 0){
            return 3;
        }
        if(sBorough.compareTo("STATEN ISLAND") == 0){
            return 4;
        }

        //System.out.println("Error: " + sBorough);
        return -1;

    }

    private int readStatus(String sStatus) throws IOException{
        if(sStatus.compareTo("Open") == 0){
            return 0;
        } else if (sStatus.compareTo("Closed") == 0){
            return 1;
        } else if (sStatus.compareTo("Pending") == 0){
            return 2;
        } else {
            System.out.println("Error: " + sStatus);
            return -1;
        }
    }

    double r = 6371.009;

    private float distance(float lat1, float lon1, float lat2, float lon2){
        double rlat1 = Math.toRadians(lat1);
        double rlat2 = Math.toRadians(lat2);
        double rlon1 = Math.toRadians(lon1);
        double rlon2 = Math.toRadians(lon2);

        double deltaLat = rlat2 - rlat1;
        double deltaLon = rlon2 - rlon1;

        double a = Math.sin(.5*deltaLat);
        double b = Math.sin(.5*deltaLon);

        double c = a*a + Math.cos(rlat1)*Math.cos(rlat2)*b*b;

        double d = 2*Math.atan2(Math.sqrt(c), Math.sqrt(1.0-c));

        return (float) (r * d);
    }

    int[] markerProximity = new int[26500];
    //float[] progress = new float[26500];

    double setDistance = 1.609 / 13;

    private void calculateMarkerProximity(){
        int i;
        int j;
        int maxWithinOneBlock = 0;
        int maxTest;
        int minWithinOneBlock = 26500;
        int minTest;

        int[] breakPoints = new int[4];

        for (i = 0; i < ndata; i++){
            //initialize to 0
            markerProximity[i] = 0;
        }

        for (i = 0; i < ndata; i++) {
            for (j = i; j < ndata; j++){
                if ((distance(lats[i], longs[i], lats[j], longs[j])) < setDistance){
                    markerProximity[i] += 1;
                    markerProximity[j] += 1;
                }
            }
        }

        for (i = 0; i < ndata; i++) {
            maxTest = markerProximity[i];
            minTest = markerProximity[i];

            if (maxTest > maxWithinOneBlock){
                maxWithinOneBlock = maxTest;
            }

            if (minTest < minWithinOneBlock) {
                minWithinOneBlock = minTest;
            }
        }
        /*
        for (i =0; i < ndata; i++) {
            progress[i] = ((float) (markerProximity[i] - 1)) / ((float) (maxWithinOneBlock - 1));
            System.out.println("prog i " + progress[i]);
        }
        */

        //System.out.println(minWithinOneBlock);
        //System.out.println(maxWithinOneBlock);

        int[] progressDistribution = new int[maxWithinOneBlock];

        int k ;

        for (k = 0; k < maxWithinOneBlock; k++){
            progressDistribution[k] = 0;
        }

        for (k = 0; k < ndata; k++){
            progressDistribution[markerProximity[k] - 1] += 1;
        }

        int breakPointCount = (int) (0.2 * ndata);
        int l = 0;
        int total = 0;

        for (k = 0; k < maxWithinOneBlock; k++){
            System.out.println("Progress Distribution: " + k + " " + progressDistribution[k]);
            total = total + progressDistribution[k];

            if ((total > breakPointCount) && (l < 4)) {
                breakPoints[l] = k;
                l = l + 1;
                breakPointCount = breakPointCount * 2;
            }
        }
        /*
        for (k = 0; k < ndata; k ++) {
            if (markerProximity[k] < breakPoints[0]){
                markerProximity[k] = 0;
            } else if (markerProximity[k] < breakPoints[1]) {
                markerProximity[k] = 1;
            } else if (markerProximity[k] < breakPoints[2]) {
                markerProximity[k] = 2;
            } else if (markerProximity[k] < breakPoints[3]) {
                markerProximity[k] = 3;
            } else {
                markerProximity[k] = 4;
            }
        }
         */

         for (k = 0; k < ndata; k ++) {
            if (markerProximity[k] < 3){
                markerProximity[k] = 0;
            } else if (markerProximity[k] < 7) {
                markerProximity[k] = 1;
            } else if (markerProximity[k] < 13) {
                markerProximity[k] = 2;
            } else if (markerProximity[k] < 17) {
                markerProximity[k] = 3;
            } else {
                markerProximity[k] = 4;
            }
        }

    }
    /*
    private void calculateGradualMarkerProximity(){
        int i;
        int j;
        int maxWithinOneBlock = 0;
        int maxTest;
        int minWithinOneBlock = 26500;
        int minTest;

        for (i = 0; i < ndata; i++){
            //initialize to 0
            markerProximity[i] = 0;
        }

        for (i = 0; i < ndata; i++) {
            for (j = i; j < ndata; j++){
                if ((distance(lats[i], longs[i], lats[j], longs[j])) < setDistance){
                    markerProximity[i] += 1;
                    markerProximity[j] += 1;
                }
            }
        }

        for (i = 0; i < ndata; i++) {
            maxTest = markerProximity[i];
            minTest = markerProximity[i];

            if (maxTest > maxWithinOneBlock){
                maxWithinOneBlock = maxTest;
            }

            if (minTest < minWithinOneBlock) {
                minWithinOneBlock = minTest;
            }
        }

        for (i =0; i < ndata; i++) {
            progress[i] = ((float) (markerProximity[i] - 1)) / ((float) (maxWithinOneBlock - 1));
            System.out.println("prog i " + progress[i]);
        }

        System.out.println(minWithinOneBlock);
        System.out.println(maxWithinOneBlock);

        int[] progressDistribution = new int[maxWithinOneBlock];

        int k ;

        for (k = 0; k < maxWithinOneBlock; k++){
            progressDistribution[k] = 0;
        }

        for (k = 0; k < ndata; k++){
            progressDistribution[markerProximity[k] - 1] += 1;
        }

        for (k = 0; k < maxWithinOneBlock; k++){
            System.out.println("Progress Distribution: " + k + " " + progressDistribution[k]);
        }

    }
    */
    private void computeTileNums() {
        int markerIndex, i, latBucketIndex, tileNum;
        float lat, lon;

        for (markerIndex = 0; markerIndex < ndata; markerIndex++) {
            lat = lats[markerIndex];
            lon = longs[markerIndex];
            i = (int) ((lat - minLat) / latDelta);

            if (i > 9) {
                i = 9;
            }
            latBucketIndex = 9 - i;

           i = (int) ((lon - minLon) / lonDelta);
           if (i > 9) {
               i = 9;
           }
           tileNum = 10 * latBucketIndex + i;
           latlonBuckets[tileNum] += 1;
           tileNums[markerIndex] = tileNum;
        }
    }

    private void writeDaysMonthsAndBorough(int i) throws IOException{
        int m = months[i]; //This will work until end of 2010; we allocate 6 bits for 	//month, 5 bits for day, and 3 bits for borough.
        int db = days[i] | (boroughs[i] << 5);
        
        fos.write(m & 0xff);
        fos.write(db & 0xff);
    }

    private void writeLatLong(int markerIndex) throws IOException{

        float lat = lats[markerIndex];
        float lon = longs[markerIndex];
        int tileNum = tileNums[markerIndex];
        int latBucketIndex = tileNum / 10;

        lat = lat - 40.0f;

        int iLat = (int)(lat * 65536.0f);

        fos.write((iLat >> 8) & 0xff);
        fos.write(iLat & 0xff);


        int i = tileNum % 10;

        lon = -lon;

        lon = lon - 70;

        int iLong = (int) lon;

        fos.write(iLong);

        lon = lon - ((float) iLong);

        iLong = (int) (lon*65536.0f);

        fos.write((iLong >> 8) & 0xff);
        fos.write(iLong & 0xff);
    }

    private void writeProximity(int i) throws IOException{
        fos.write(markerProximity[i]);
    }

    private void writeProgress(int i) throws IOException{
        int j = (int) (65535.0f * progress[i]);
        //System.out.println("j " + j);
        //int j = Float.floatToIntBits(progress[i]);
        //fos.write((j >> 24) & 0xff);
        //fos.write((j >> 16) & 0xff);
        fos.write((j >> 8) & 0xff);
        fos.write((j) & 0xff);
    }

    private void writeBorough(int i) throws IOException{
        fos.write(boroughs[i]);
    }

    private void writeStatus(int i) throws IOException{
        fos.write(stats[i]);
    }

    private void writeTileNum(int i) throws IOException{
        fos.write(tileNums[i]);
    }

    private void writeLOD(int i) throws IOException{
        fos.write(lods[i]);
    }

    private void writeLatf(float lat) throws IOException{

        lat = lat - 40.0f;

        int iLat = (int)(lat * 65536.0f);

        fos.write((iLat >> 8) & 0xff);
        fos.write(iLat & 0xff);
    }

    private void writeLongf(float lon) throws IOException{

        lon = -lon;

        lon = lon - 70;

        int iLong = (int) lon;

        fos.write(iLong);

        lon = lon - ((float) iLong);

        iLong = (int) (lon*65536.0f);

        fos.write((iLong >> 8) & 0xff);
        fos.write(iLong & 0xff);
    }

    private void computeMinMax() {
        minLat = lats[0];
        maxLat = lats[0];
        minLon = longs[0];
        maxLon = longs[0];
        for (int i = 1; i < ndata; i++) {
            if (lats[i] <= minLat) {
                minLat = lats[i];
            }
            if (lats[i] >= maxLat) {
                maxLat = lats[i];
            }
            if (longs[i] <= minLon) {
                minLon = longs[i];
            }
            if (longs[i] >= maxLon) {
                maxLon = longs[i];
            }
        }
        latDelta = (maxLat - minLat) / 10.0f;
        lonDelta = (maxLon - minLon) / 10.0f;
    }

    float[] latDeltas = {0.0f, 0.0f, 0.0f, 0.0f,
                         0.0f, 0.0f, 0.0f, 0.0f,
                         2.095f /* zoom level 8 */,
                         1.045f /* zoom level 9 */,
                         0.521f /* zoom level 10 */,
                         0.261f /* zoom level 11 */,
                         0.131f /* zoom level 12 */,
                         0.066f /* zoom level 13 */,
                         0.033f /* zoom level 14 */,
                         0.017f /* zoom level 15 */,
                         0.009f /* zoom level 16 */,
                         0.005f /* zoom level 17 */
                        };

    float[] lonDeltas = {0.0f, 0.0f, 0.0f, 0.0f,
                         0.0f, 0.0f, 0.0f, 0.0f,
                         5.384f /* zoom level 8 */,
                         2.692f /* zoom level 9 */,
                         1.346f /* zoom level 10 */,
                         0.673f /* zoom level 11 */,
                         0.337f /* zoom level 12 */,
                         0.169f /* zoom level 13 */,
                         0.085f /* zoom level 14 */,
                         0.043f /* zoom level 15 */,
                         0.022f /* zoom level 16 */,
                         0.011f /* zoom level 17 */
                        };

    private void computeMaxVisibles() {
        int z;

        initTileData();

        for (z = 8; z <= 17; z++) {
            computeMaxVisiblesAtZoom(z);
        }
    }

    private void initTileData() {
        int i, j;
        int tileNum;
        TileData tile;

        for (i = 0; i < ndata; i++) {
            tileNum = tileNums[i];
            tile = tiles[tileNum];
            if (tile == null) {
                int tileRow;
                int tileCol;

                tileRow = tileNum / 10;
                tileCol = tileNum % 10;
                tile = new TileData();
                tiles[tileNum] = tile;
                tile.tileNum = tileNum;
                tile.tileRow = tileRow;
                tile.tileCol = tileCol;
                tile.tlLat = minLat + ((float) (9 - tileRow + 1)) * latDelta;
                tile.tlLon = minLon + ((float) tileCol) * lonDelta;
                tile.brLat = minLat + ((float) (9 - tileRow)) * latDelta;
                tile.brLon = minLon + ((float) (tileCol + 1)) * lonDelta;
                tile.nmarker = latlonBuckets[tileNum];
                tile.markerIndices = new int[tile.nmarker];
                tile.nextMarker = 0;
                tile.dateBins = new int[29][];
                tile.binCounts = new int[29];
                for (j = 8; j <= 28; j++) {
                    tile.dateBins[j] = new int[800]; //in a tile, maximum number of incidents/month
                    tile.binCounts[j] = 0;
                }
                tileCount = tileCount + 1;
            }
            tile.markerIndices[tile.nextMarker] = i;
            tile.nextMarker = tile.nextMarker + 1;
        }
        System.out.println("tileCount: " + tileCount);
    }

    private void computeMaxVisiblesAtZoom(int z) {
        int i, j;
        float lat, lon, dlat, dlon;
        int vbmvbt[] = new int[2];

        visByMarkerMax = 0;
        visByTileMax = 0;
        lat = maxLat;
        dlat = (maxLat - minLat) / 100.0f;
        dlon = (maxLon - minLon) / 100.0f;
        for (i = 0; i < 100; i++) {
            lon = minLon;
            for (j = 0; j < 100; j++) {
                computeVisibles(lat, lon, z, vbmvbt);
                if (vbmvbt[0] > visByMarkerMax) {
                    visByMarkerMax = vbmvbt[0];
                }
                if (vbmvbt[1] > visByTileMax) {
                    visByTileMax = vbmvbt[1];
                }
                lon = lon + dlon;
            }
            lat = lat - dlat;
        }
        System.out.println("Zoom Level: " + z);
        System.out.println("visByMarkerMax: " + visByMarkerMax);
        System.out.println("visByTileMax: " + visByTileMax);
        System.out.println("");
    }

    private void computeVisibles(float lat, float lon, int z, int[] vbmvbt) {
        int vbm, vbt;
        int i, j, k;
        float winLeft, winRight, winTop, winBottom;
        TileData tile;

        vbm = 0;
        vbt = 0;
        winLeft = lon;
        winRight = lon + lonDeltas[z];
        winTop = lat;
        winBottom = lat - latDeltas[z];
        for (i = 0; i < 100; i++) {
            tile = tiles[i];
            if (tile == null) {
                continue;
            }
            if ((winLeft > tile.brLon) ||
                (tile.tlLon > winRight) ||
                (winTop < tile.brLat) ||
                (tile.tlLat < winBottom)) {
                continue;
            }
            vbt = vbt + tile.nmarker;
            for (j = 0; j < tile.nmarker; j++) {
                k = tile.markerIndices[j];
                if ((longs[k] >= winLeft) &&
                    (longs[k] <= winRight) &&
                    (lats[k] <= winTop) &&
                    (lats[k] >= winBottom)) {
                    vbm = vbm + 1;
                }
            }
        }
        vbmvbt[0] = vbm;
        vbmvbt[1] = vbt;
    }

    private void computeLatLongMonthBins() {
        int i, j, k;
        TileData tile;
        int[] bins = new int[2000]; // 2000 = maximum number of incidents in a single tile in a single month
        int date;

        for (i = 0; i < 2000; i++) { // 2000 = maximum number of incidents in a single tile in a single month
            bins[i] = 0;
        }

        for (i = 0; i < 100; i++) {
            tile = tiles[i];
            if (tile == null) {
                continue;
            }
            for (j = 0; j < tile.nmarker; j++) {
                k = tile.markerIndices[j];
                date = months[k];
                if ((date < 8) || (date > 28)) { //1 is January 2008, 24 is December of 2009
                    System.out.println("Out of bounds date: " + k + " " + date);
                }
                tile.dateBins[date][tile.binCounts[date]] = k;
                tile.binCounts[date] = tile.binCounts[date] + 1;
            }
            for (j = 8; j <= 28; j++) { //Month 1 is January 2008; month 24 is December of 2009, and so on..
                if (tile.binCounts[j] > 0) {
                    bins[tile.binCounts[j]] = bins[tile.binCounts[j]] + 1;
                }
            }
        }

        for (i = 0; i < 2000; i++) { // 2000 = maximum number of incidents in a single tile in a single month
            if (bins[i] > 0) {
                System.out.println("Bin " + i + ": " + bins[i]);
            }
        }
    }

    private void computeLODs() {
        int i, j;
        TileData tile;

        for (i = 0; i < 100; i++) {
            tile = tiles[i];
            if (tile == null) {
                continue;
            }
            for (j = 8; j <= 28; j++) { //Month 1 is January 2008; month 24 is December of 2009, and so on..
                if (tile.binCounts[j] > 0) {
                    computeLODs(tile.dateBins[j], tile.binCounts[j]);
                }
            }
        }
        checkLODs();
    }

    private void computeLODs(int[] bin, int count) {
        int[] nAtLOD = getNatLOD(count);
        int[] tmpbin = new int[count];
        int i;
        int nselected;

        for (i = 0; i < count; i++ ) {
            tmpbin[i] = bin[i];
        }
        random = new Random(12929L);
        nselected = 0;
        for (i = 0; i < 6; i++) {
            selectNofM(tmpbin, nAtLOD[i], count - nselected, i);
            nselected = nselected + nAtLOD[i];
        }
    }

    private void selectNofM(int[] bin, int n, int m, int lod) {
        float prob = (float) n / (float) m;
        int index = 0;
        while (n > 0) {
            while (bin[index] == -1) {
                index = (index + 1) % (bin.length);
            }
            if (random.nextFloat() < prob) {
                lods[bin[index]] = lod;
                bin[index] = -1;
                n = n - 1;
            }
            index = (index + 1) % (bin.length);
        }
    }

    int[][] smallNatLODs = {{1,0,0,0,0,0},
                            {2,0,0,0,0,0},
                            {2,1,0,0,0,0},
                            {2,1,0,1,0,0},
                            {2,1,0,1,1,0},
                            {2,1,0,1,1,1},
                            {2,1,0,1,1,2},
                            {2,1,0,1,1,3},
                            {2,1,0,1,1,4},
                            {2,1,0,1,1,5}
                           };

    private int[] getNatLOD(int n) {
        if (n <= 10) {
            return smallNatLODs[n - 1];
        }
        int[] nAtLOD = new int[6];
        nAtLOD[0] = (int) ((0.075f * (float) n) + 0.5f); //percentages from .12
        int count = nAtLOD[0];
        nAtLOD[1] = ((int) ((0.16f * (float) n) + 0.5f)) - count; //from .24
        count = count + nAtLOD[1];
        nAtLOD[2] = ((int) ((0.26f * (float) n) + 0.5f)) - count; //from .3
        count = count + nAtLOD[2];
        nAtLOD[3] = ((int) ((0.36f * (float) n) + 0.5f)) - count;
        count = count + nAtLOD[3];
        nAtLOD[4] = ((int) ((0.48f * (float) n) + 0.5f)) - count;
        count = count + nAtLOD[4];
        nAtLOD[5] = n - count;
        return nAtLOD;
    }

    private void checkLODs() {
        int i, lod;
        int[] lodCount = new int[6];

        for (i = 0; i < 6; i++) {
            lodCount[i] = 0;
        }
        for (i = 0; i < ndata; i++) {
            lod = lods[i];
            lodCount[lod] = lodCount[lod] + 1;
        }
        System.out.println("LOD 0: " + lodCount[0]);
        System.out.println("LOD 1: " + lodCount[1]);
        System.out.println("LOD 2: " + lodCount[2]);
        System.out.println("LOD 3: " + lodCount[3]);
        System.out.println("LOD 4: " + lodCount[4]);
        System.out.println("LOD 5: " + lodCount[5]);
    }

    private void writeTileData() throws IOException {
        int i, j;
        TileData tile;

        LODindexCount = 0;
        fos.write(tileCount);
        for (i = 0; i < 100; i++) {
            tile = tiles[i];
            if (tile == null) {
                continue;
            }
            fos.write(tile.tileNum);
            writeLatf(tile.tlLat);
            writeLongf(tile.tlLon);
            writeLatf(tile.brLat);
            writeLongf(tile.brLon);
            generateLODindices(tile);
        }
        writeLODindices();
    }

    private void generateLODindices(TileData tile)  throws IOException {
        int tileNum = tile.tileNum;
        int lod, i, count;
        int[] TileLODindices = new int[4000]; //*

        for (lod = 0; lod < 6; lod++ ) {
            count = 0;
            for (i = 0; i < ndata; i++) {
                if ((tileNums[i] == tileNum) && (lods[i] <= lod)) {
                    TileLODindices[count] = i;
                    count = count + 1;
                }
            }
            fos.write((LODindexCount >> 8) & 0xff);
            fos.write(LODindexCount & 0xff); // LODindices offset for tile/lod
            fos.write((count >> 8) & 0xff);
            fos.write(count & 0xff); //LODindices count for tile/lod
            for (i = 0; i < count; i++) {
                LODindices[LODindexCount] = TileLODindices[i];
                LODindexCount = LODindexCount + 1;
            }
        }
    }

    private void writeLODindices()  throws IOException {
        System.out.println("LODindexCount: " + LODindexCount);
        fos.write((LODindexCount >> 8) & 0xff);
        fos.write(LODindexCount & 0xff); // LODindices count for all tiles/lods
        for (int i = 0; i < LODindexCount; i++) {
            fos.write((LODindices[i] >> 8) & 0xff);
            fos.write(LODindices[i] & 0xff);
        }
    }

}
