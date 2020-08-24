#!/bin/bash
now=$(date)
echo "Current date: $now"
MESS="$1"

echo $MESS

git add .
git commit -m "$(echo $MESS)"
git push origin master
git status
