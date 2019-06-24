# fram

Annotate photographs for display in a digital photoframe.

This is a program for preparing the files for a digital photo
frame. These devices show a stream of photographs loaded from a memory
stick, changing the photo every few seconds or minutes. The simpler
ones just read the memory stick and display the photos, whilst the
more complicated ones are based on tablets.

This program takes a directory containing jpeg format files and
generates a new directory containing the same files. These photos can
be a mixture of scanned photographs and digital images.  It does
several things during the transfer:

- It adds the name of the original directory containing each the photo
to the new jpeg image.  If the photos are stored in directories which
say when and where the photo was taken, then each photo picks up this
annotation which can be seen when the photo is displayed. It can also
add the name of the original photograph file to the image.

- It rotates the photo to match the orientation specified by its exif
data.  Some photo frames aren't very good at doing this automatically.

- It randomises the order of the photos, putting them into
sub-directories with a random number and giving them random number
names within those directories.  This means that they should be
displayed in random order even when played in sequence. Most photo
frames have the ability to randomise the order of photographs but some
are not very good at it.

The contents of the output directory can then be copied onto a memory
stick and plugged into the photoframe.  At the moment, it only knows about
jpeg format files.

It is intended to be run every night in a cron job, so it also counts the
number of photos in the input directory and only actually does anything if
the number of photos has changed.

As it will delete the entire contents of the output directory before
regenerating it, there is an option for it to carry out a check first to
make sure that the output directory contains just files produced by the
program, so that messing up the invocation doesn't do any damage to another
directory.

It also optionally has a cache directory where it caches files from
each run, so that it does not have to process every single image again
when it runs, only the files that have changed. I have tens of
thousands of photos and it runs on a really slow computer so without
caching takes several hours in my case.

## Excluding files

Sometimes you don't want specific files or directory trees to be
included in the photographs displayed.  You do this by creating files
in the directory you want to exclude.

A file called _photoframe_exclude.txt in the directory means that the
directory, and any sub-directories, are excluded.

A file called _photoframe_exclude_list.txt means that the files inside
the directory that are listed inside it will be excluded. For example:

`My photo 0002.jpg`

`My photo 0003.jpg`

will exclude "My photo 0002".jpg and "My photo 0003".jpg from the output.

The path names of files excluded from the output are appended to a
text file, exclusion_list.txt, each time the program is run so that
you can check the exclusion is working.

## Usage

It is written in Java, and intended to be run from the command line. e.g.

`java -cp $HOME/fram/lib/metadata-extractor.jar:$HOME/fram/lib/xmpcore.jar:$HOME/fram/lib/sqlite-jdbc-3.23.1.jar:$HOME/fram/lib/commons-math3-3.6.1.jar:$HOME/fram/dist/fram.jar fram.Fram test_data/testInput/ testOutput/ --verbose --check --cache`

It relies upon four widely used third party jar files for processing
exif data, manipulating the image, interfacing to SQLite 3 (which is
used in the caching) and generating statistics.  for convenience, I
have included them in this reposotory, in the lib sib0directory, along
with the licence information for them.  This is in lib/licences.txt.

## Options

Arguments are `<input dir> <output dir>`

Input dir is a pre-existing directory tree containing the input
files. Only .JPG, .jpg (i.e. any case) etc Jpeg format files are
processed, all others are skipped.

Output dir is a pre-existing directory where the files for the
photoframe are to be copied to.  This is typically the name of the USB
or SD device which is going to be plugged into the photoframe.  The
alphabetical order of the files in the output directory is jumbled up
so that photos are displayed in a random order.

To make human navigation of the output tree easier, for example if
your operating system tries to display thumbnails, it is split into
sub-directories containing 100 files each.  Doing things this way means
that there are not thousands of files for the operating system to have
to display in any directory.  The directories have numeric names, such
as 000010, 000011, etc.  Within them, the files also have numeric
names such as 000001.jpg, 000002.jpg

IF the output directory tree only contains files with these names (and
thumbs.db) then the entire tree is deleted before the new images are
put into the tree.  If the tree contains any file which doesn't match
this name then the tree is not deleted.

The program currently supports about a million image files, although
in practice I use it with a few tens of thousands.

The image is copied into the output directory tree and the original
containing directory written at the top of the output image.

The command line options are as follows. They are all case-insensitive.

`--check`

This counts the number of files in the input directory tree and does
not run the copying code if the number has not changed since last time
it was run.  This is intended for using the program in a batch job,
where the program is regularly run but only has to regenerate the
output tree if something has changed.  The check number is contained
in the current directory, in a file called check_*long random
number*.txt.  This ensures a unique name, as the program can be run
several times from the same place on different directory trees.

`--date`

Add the date that the photo was taken to the annotation.  This works
for digital photos but if the photo has been scanned from a negative
then the date, if any, will have been written by the device which
scanned the photos and so will not relate to the date that the picture
was taken.

`--noDirectory`

Suppressing adding the directory name text to the output file.

`--noRotate`

Do not rotate the output file. If not specified the image is rotated
according to the exif information about the image orientation found
in the input file.

`--showFilename`

This adds text to the bottom left corner of the output images which
specifies their original filenames.  It is useful for tracking down an
image which needs modifying or fixing in some way.

'--showIndex'

This adds test to the bottom left corner of the output images which
specifies the numeric index of the file, i.e. the output filename
(e.g. 393945.jpg). This can be subsequently mapped onto the original
file path using the log file copy_list.txt generated by the program.

`--verbose`

This produces more logging output than usual.


## Libraries

The run-time libraries needed by the program are in dist/lib. Note
that whilst these are all FOSS libraries, they are licensed 
differently to my code.  The file licences.txt in dist/lib details
the licences that they use.

To run the ant script, which is the best way of building and testing
the program, you will need to have ant, junit and the ant-junit
library installed.  Then set the current directory to the top
level fram directory and run `ant`.  It will build the code if
necessary and run the unit tests.  To force it to rebuild everything
run `ant clean` first.

On Ubuntu (19.04), these are the ant and ant-optional packages.  And
you will need the Java runtime library.  I wrote this all using the
[Netbeans IDE](https://netbeans.apache.org/), so you might want to
install that too.

