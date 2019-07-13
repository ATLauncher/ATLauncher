find ../src/main/java/com/atlauncher/ -name '*.java' | sed -n 's|^../||p' > .files-to-extract
xgettext -D ../ --keyword=GetText.tr -f .files-to-extract -L Java --from-code utf-8 --force-po --add-comments="#. " --omit-header -o ../src/main/resources/assets/lang/template.pot
sed -i 's|^#. #.|#.|p' ../src/main/resources/assets/lang/template.pot
uniq ../src/main/resources/assets/lang/template.pot temp
rm -f ../src/main/resources/assets/lang/template.pot
mv temp ../src/main/resources/assets/lang/template.pot
rm .files-to-extract
