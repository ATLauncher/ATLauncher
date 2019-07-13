cd ../src/main/java/com/atlauncher
dir *.java /b/s > ../../../../../scripts/.files-to-extract
cd ../../../../../scripts/

xgettext -D ../ --keyword=GetText.tr -f .files-to-extract -L Java --from-code utf-8 --force-po --add-comments="#. " --omit-header -o ../src/main/resources/assets/lang/template.pot

del .files-to-extract
