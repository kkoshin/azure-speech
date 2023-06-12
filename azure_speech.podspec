Pod::Spec.new do |spec|
    spec.name                     = 'azure_speech'
    spec.version                  = '0.1.0-LOCAL'
    spec.homepage                 = 'Link to the Shared Module homepage'
    spec.source                   = { :http=> ''}
    spec.authors                  = ''
    spec.license                  = ''
    spec.summary                  = 'Some description for the Shared Module'
    spec.vendored_frameworks      = 'build/cocoapods/framework/azure_speech.framework'
    spec.libraries                = 'c++'
    spec.ios.deployment_target = '13.5'
    spec.dependency 'MicrosoftCognitiveServicesSpeech-iOS', '~> 1.25'
                
    spec.pod_target_xcconfig = {
        'KOTLIN_PROJECT_PATH' => '',
        'PRODUCT_MODULE_NAME' => 'azure_speech',
    }
                
    spec.script_phases = [
        {
            :name => 'Build azure_speech',
            :execution_position => :before_compile,
            :shell_path => '/bin/sh',
            :script => <<-SCRIPT
                if [ "YES" = "$OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED" ]; then
                  echo "Skipping Gradle build task invocation due to OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED environment variable set to \"YES\""
                  exit 0
                fi
                set -ev
                REPO_ROOT="$PODS_TARGET_SRCROOT"
                "$REPO_ROOT/gradlew" -p "$REPO_ROOT" $KOTLIN_PROJECT_PATH:syncFramework \
                    -Pkotlin.native.cocoapods.platform=$PLATFORM_NAME \
                    -Pkotlin.native.cocoapods.archs="$ARCHS" \
                    -Pkotlin.native.cocoapods.configuration="$CONFIGURATION"
            SCRIPT
        }
    ]
                
end