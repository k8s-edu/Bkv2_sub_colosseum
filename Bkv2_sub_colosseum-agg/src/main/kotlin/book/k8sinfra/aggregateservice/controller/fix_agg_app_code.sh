#!/usr/bin/env bash

echo "Fixing the code in UserScoreController.kt"
sed -i -e '/\/\/ culprit code > delete, logger.info > modify from abnormal to normal/{
        N;N;N;d
    }' \
    -e 's#^\([[:space:]]*\)//[[:space:]]*\(logger\.info("Detected normal response duration")\)#\1\2#' UserScoreController.kt
git diff 
echo "Now go to build to ~/_Book_k8sInfra/ch6/6.6.3/Bkv2_sub_colosseum/"
