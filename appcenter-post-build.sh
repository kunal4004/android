#!/usr/bin/env bash

if [ "$APPCENTER_BRANCH" == "qa" ];
then
    curl -v \
    -F "status=2" \
    -F "ipa=@$APPCENTER_OUTPUT_DIRECTORY/app-qa-release.apk" \
    -H "X-HockeyAppToken: $HOCKEYAPP_API_TOKEN" \
    https://rink.hockeyapp.net/api/2/apps/$HOCKEYAPP_APP_ID/app_versions/upload
else
    echo "Current branch is $APPCENTER_BRANCH"
fi
