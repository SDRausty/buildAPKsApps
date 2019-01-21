#!/bin/sh

for file in `ls $1`
do
    echo $1/$file en cours...
    cat $1/$file | perl -0 -pe 's/<div class="navheader">.*?<\/div>//gs' >> $1/tmp ; mv $1/tmp $1/$file
done
