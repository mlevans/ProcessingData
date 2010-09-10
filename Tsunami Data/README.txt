This is a Java Class for processing USGS data on tsunamis.  

The program takes a text file of the data, which you place in a folder called "input". It creates a binary data file, which will be placed in a folder called "output."

The program organizes, cleans, and compresses the data.  The data is ultimately compressed
by 63%.

A quick note:

Why do we put the data in arrays?  Why not simply write it out?

We store the data into individual arrays.  We want to read all of the data before we write it out.  If we do this, we can additionally write out a count of the data.  With a count of the data, we can allocate storage for all of the data at once.  This allows us to avoid using the less efficient array.push() method (to incrementally grow the storage).

I hope that makes sense?