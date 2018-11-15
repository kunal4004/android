#!/usr/bin/env bash

#Param HOCKEYAPP_NOTIFY_CODE
#0 - Don't notify testers
#1 - Notify all testers that can install this app
#2 - Notify all testers

file="$APPCENTER_SOURCE_DIRECTORY/releasenotes.txt"
while IFS= read -r releasenotes
do
# display $line or do somthing with $line
printf '%s\n' "$releasenotes"
done <"$file"

tags=""
if [ "$APPCENTER_BRANCH" == "build_qa" ];
then
    tags="qa,dev,internal"
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
    -F "notify=$HOCKEYAPP_NOTIFY_CODE" \
    -F "tags=$tags" \
    -H "X-HockeyAppToken: $HOCKEYAPP_API_TOKEN" \
    https://rink.hockeyapp.net/api/2/apps/$HOCKEYAPP_APP_ID/app_versions/upload

    if [ "$APPCENTER_BRANCH" == "qa" ]
    then
        # SIT
        curl -v \
        -F "status=2" \
        -F "ipa=@$APPCENTER_OUTPUT_DIRECTORY/app-qa-release.apk" \
        -F "notes=$releasenotes" \
        -F "notify=$HOCKEYAPP_NOTIFY_CODE" \
        -H "X-HockeyAppToken: $HOCKEYAPP_API_TOKEN" \
        https://rink.hockeyapp.net/api/2/apps/b13879387c2147daba77c37b82023ef1/app_versions/upload
    fi
else
    echo "Current branch is $APPCENTER_BRANCH"
fi
