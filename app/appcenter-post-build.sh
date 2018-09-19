#!/usr/bin/env bash

file="$APPCENTER_SOURCE_DIRECTORY/releasenotes.txt"
while IFS= read -r releasenotes
do
# display $line or do somthing with $line
printf '%s\n' "$releasenotes"
done <"$file"

tags=""
if [ "$APPCENTER_BRANCH" == "build_qa" ];
then
    tags="qa,dev"
fi

if [ "$APPCENTER_BRANCH" == "cug" ]
then
    gradle --debug publishApkProductionRelease
elif [ "$APPCENTER_BRANCH" == "qa" ] || [ "$APPCENTER_BRANCH" == "build_qa" ]
then
    curl -v \
    -F "status=2" \
    -F "ipa=@$APPCENTER_OUTPUT_DIRECTORY/app-qa-release.apk" \
    -F "notes=$releasenotes" \
    -F "notify=1" \
    -F "tags=$tags" \
    -H "X-HockeyAppToken: $HOCKEYAPP_API_TOKEN" \
    https://rink.hockeyapp.net/api/2/apps/$HOCKEYAPP_APP_ID/app_versions/upload

    # SIT
    curl -v \
    -F "status=2" \
    -F "ipa=@$APPCENTER_OUTPUT_DIRECTORY/app-qa-release.apk" \
    -F "notes=$releasenotes" \
    -F "notify=1" \
    -F "tags=$tags" \
    -H "X-HockeyAppToken: $HOCKEYAPP_API_TOKEN" \
    https://rink.hockeyapp.net/api/2/apps/ec56155fa76e4c09bca465d68089525c/app_versions/upload
else
    echo "Current branch is $APPCENTER_BRANCH"
fi
