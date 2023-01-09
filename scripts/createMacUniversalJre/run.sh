#!/usr/bin/env bash

ARM_JRE=$1
X86_JRE=$2
OUT_JRE=$3

echo "Making universal JRE from $ARM_JRE and $X86_JRE to $OUT_JRE"

rm -rf $OUT_JRE
rm -f ${OUT_JRE%/}.tgz

cp -R $ARM_JRE $OUT_JRE

function combine
{
    rm -f $1
    filepath=${1#*/}

    echo "Combining $ARM_JRE$filepath and $X86_JRE$filepath into $OUT_JRE$filepath"
    lipo $ARM_JRE$filepath $X86_JRE$filepath -create -output $OUT_JRE$filepath
    lipo -info $OUT_JRE$filepath
    echo
}

# all executable files need to be made universal
for file in `find $OUT_JRE -type f -perm +0111`
do
    combine $file
done

# all dylib files need to be made universal
for file in `find $OUT_JRE -type f -name "*.dylib"`
do
    combine $file
done

cd $OUT_JRE && tar -czf ../${OUT_JRE%/}.tar * && cd ..

echo "Universal JRE created and archived at ${OUT_JRE%/}.tar"
