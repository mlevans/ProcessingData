This is a Java program for processing graffiti data from New York City's datamine.  The
dataset includes geolocation data of graffiti incidents, which will ultimately be
displayed on an interactive map.

The program achieves the following:

1. cleans and orders the data
2. compresses the data in a machine-independent binary format
3. calculates levels of detail of the data at each zoom level of the map
4. calculates the density of graffiti data around the five boroughs of New York City
(This is referred to as proximity in the code.)

The program takes an ordered (tabular) text file of the data, which you place in a folder called "input". It creates a binary data file, which will be placed in a folder called "output."

The program organizes, cleans, and compresses the data.  The data is ultimately compressed
by 70%.

A quick note:

Why do we put the data in arrays?  Why not simply write it out?

We store the data into individual arrays.  We want to read all of the data before we write it out.  If we do this, we can additionally write out a count of the data.  With a count of the data, we can allocate storage for all of the data at once.  This allows us to avoid using the less efficient array.push() method (to incrementally grow the storage).