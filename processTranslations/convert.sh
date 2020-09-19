for file in `find in/ -type f -name "*.po"`; do
    msgattrib --clear-fuzzy --empty --translated -o tmp/`basename $file` $file
    head -c -1 tmp/`basename $file` > out/`basename $file`
done
