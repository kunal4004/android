#!/usr/bin/env bash


echo "BUILD_NUMBER: $BUILD_NUMBER"
# Extract build number from latest HockeyApp build.
BUILD_NUMBER=$(curl -H "X-HockeyAppToken: $HOCKEYAPP_API_TOKEN" https://rink.hockeyapp.net/api/2/apps/$HOCKEYAPP_APP_ID/app_versions | sed -e 's/^.*"app_versions":\[{"version":"\([^"]*\)".*$/\1/')

echo "BUILD_NUMBER: $BUILD_NUMBER"
export BUILD_NUMBER=$(($BUILD_NUMBER + 1))
echo "NEXT_BUILD_NUMBER: $BUILD_NUMBER"
