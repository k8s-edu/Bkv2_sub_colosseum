#!/usr/bin/env bash

echo "Fixing the code in UserScoreController.kt"
sed -i -e '/\/\/ culprit code > delete, logger.info > modify from abnormal to normal/ {
        n;N;N;d
    }' \
    -e 's#^\([[:space:]]*\)//[[:space:]]*\(logger\.info("Detected normal response duration")\)#\1\2#' UserScoreController.kt
git diff 
echo "From build to run, the code is fixed"
cd ../../../../../../..
